local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]

local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])
local ttl_times = tonumber(ARGV[5])
-- 1 :-> first request
-- 2 :-> last allowed request
-- 3 :-> last request
local ttl_policy = tonumber(ARGV[6])

local fill_time = math.floor(capacity / rate)
local ttl = fill_time * ttl_times * 2

local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
    last_tokens = capacity
end

local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
    last_refreshed = 0
    if ttl_policy == 1 then
        redis.call("setex", timestamp_key, ttl, now)
    end
end

local delta = math.max(0, math.floor((now - last_refreshed) / ttl_times))
if ttl_policy == 1 and delta > 0 then
    redis.call("setex", timestamp_key, ttl, now)
end
local filled_tokens = math.min(capacity, last_tokens + (delta * rate))
local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
if allowed then
    new_tokens = filled_tokens - requested
    allowed_num = 1
    if ttl_policy == 2 then
        redis.call("setex", timestamp_key, ttl, now)
    end
end

redis.call("setex", tokens_key, ttl, new_tokens)
if ttl_policy == 3 then
    redis.call("setex", timestamp_key, ttl, now)
end


return { allowed_num, new_tokens, delta, last_tokens, now, last_refreshed, ttl, math.floor((now - last_refreshed) / ttl_times) }
