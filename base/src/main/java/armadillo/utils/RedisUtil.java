package armadillo.utils;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.TransportMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisUtil {
    private final JedisPool pool;
    private final String ip;
    private final int port;
    private final String password;
    private final RedissonClient redissonClient;
    private static RedisUtil redisUtil;

    private RedisUtil() {
        ip = SysConfigUtil.getSysConfigUtil("redis.properties").getString("redis.host");
        port = SysConfigUtil.getSysConfigUtil("redis.properties").getInteger("redis.port");
        password = SysConfigUtil.getSysConfigUtil("redis.properties").getString("redis.password");
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(SysConfigUtil.getSysConfigUtil("redis.properties").getInteger("redis.maxTotal"));
        jedisPoolConfig.setMaxIdle(SysConfigUtil.getSysConfigUtil("redis.properties").getInteger("redis.maxIdle"));
        jedisPoolConfig.setMaxWaitMillis(SysConfigUtil.getSysConfigUtil("redis.properties").getLong("redis.maxWaitMillis"));
        jedisPoolConfig.setTestOnBorrow(SysConfigUtil.getSysConfigUtil("redis.properties").getBoolean("redis.testOnBorrow"));
        jedisPoolConfig.setTestOnReturn(SysConfigUtil.getSysConfigUtil("redis.properties").getBoolean("redis.testOnReturn"));
        if (password != null && !"".equals(password)) {
            pool = new JedisPool(jedisPoolConfig, ip, port, 10000, password);
        } else {
            pool = new JedisPool(jedisPoolConfig, ip, port, 10000);
        }
        Config config = new Config();
        config.setTransportMode(OsUtils.isOSLinux() ? TransportMode.EPOLL : TransportMode.NIO);
        SingleServerConfig singleServer = config.useSingleServer();
        singleServer.setAddress("redis://" + ip + ":" + port);
        if (password != null && !"".equals(password))
            singleServer.setPassword(password);
        singleServer.setConnectionPoolSize(500);
        singleServer.setIdleConnectionTimeout(10000);
        singleServer.setConnectTimeout(30000);
        singleServer.setTimeout(3000);
        redissonClient = Redisson.create(config);
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    /**
     * 获取指定key的值,如果key不存在返回null，如果该Key存储的不是字符串，会抛出一个错误
     *
     * @param key
     * @return
     */
    public String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            String value = null;
            value = jedis.get(key);
            return value;
        }
    }

    /**
     * 设置key的值为value
     *
     * @param key
     * @param value
     * @return
     */
    public String set(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value);
        }
    }

    /**
     * 删除指定的key,也可以传入一个包含key的数组
     *
     * @param keys
     * @return
     */
    public Long del(String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.del(keys);
        }
    }

    /**
     * 通过key向指定的value值追加值
     *
     * @param key
     * @param str
     * @return
     */
    public Long append(String key, String str) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.append(key, str);
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public Boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key);
        }
    }

    /**
     * 设置key value,如果key已经存在则返回0
     *
     * @param key
     * @param value
     * @return
     */
    public Long setnx(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.setnx(key, value);
        }
    }

    /**
     * 设置key value并指定这个键值的有效期
     *
     * @param key
     * @param seconds
     * @param value
     * @return
     */
    public String setex(String key, int seconds, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.setex(key, seconds, value);
        }
    }

    /**
     * 通过key 和offset 从指定的位置开始将原先value替换
     *
     * @param key
     * @param offset
     * @param str
     * @return
     */
    public Long setrange(String key, int offset, String str) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.setrange(key, offset, str);
        }
    }

    /**
     * 通过批量的key获取批量的value
     *
     * @param keys
     * @return
     */
    public List<String> mget(String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.mget(keys);
        }
    }

    /**
     * 批量的设置key:value,也可以一个
     *
     * @param keysValues
     * @return
     */
    public String mset(String... keysValues) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.mset(keysValues);
        }
    }

    /**
     * 批量的设置key:value,可以一个,如果key已经存在则会失败,操作会回滚
     *
     * @param keysValues
     * @return
     */
    public Long msetnx(String... keysValues) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.msetnx(keysValues);
        }
    }

    /**
     * 设置key的值,并返回一个旧值
     *
     * @param key
     * @param value
     * @return
     */
    public String getSet(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.getSet(key, value);
        }
    }

    /**
     * 通过下标 和key 获取指定下标位置的 value
     *
     * @param key
     * @param startOffset
     * @param endOffset
     * @return
     */
    public String getrange(String key, int startOffset, int endOffset) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.getrange(key, startOffset, endOffset);
        }
    }

    /**
     * 通过key 对value进行加值+1操作,当value不是int类型时会返回错误,当key不存在是则value为1
     *
     * @param key
     * @return
     */
    public Long incr(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.incr(key);
        }
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则这是value为该值
     *
     * @param key
     * @param integer
     * @return
     */
    public Long incrBy(String key, long integer) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.incrBy(key, integer);
        }
    }

    /**
     * 对key的值做减减操作,如果key不存在,则设置key为-1
     *
     * @param key
     * @return
     */
    public Long decr(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.decr(key);
        }
    }

    /**
     * 减去指定的值
     *
     * @param key
     * @param integer
     * @return
     */
    public Long decrBy(String key, long integer) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.decrBy(key, integer);
        }
    }

    /**
     * 通过key获取value值的长度
     *
     * @param key
     * @return
     */
    public Long strLen(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.strlen(key);
        }
    }

    /**
     * 通过key给field设置指定的值,如果key不存在则先创建,如果field已经存在,返回0
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hsetnx(String key, String field, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hsetnx(key, field, value);
        }
    }

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hset(String key, String field, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }

    /**
     * 通过key同时设置 hash的多个field
     *
     * @param key
     * @param hash
     * @return
     */
    public String hmset(String key, Map<String, String> hash) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hmset(key, hash);
        }
    }

    /**
     * 通过key 和 field 获取指定的 value
     *
     * @param key
     * @param failed
     * @return
     */
    public String hget(String key, String failed) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hget(key, failed);
        }
    }

    /**
     * 设置key的超时时间为seconds
     *
     * @param key
     * @param seconds
     * @return
     */
    public Long expire(String key, int seconds) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }

    /**
     * 通过key 和 fields 获取指定的value 如果没有对应的value则返回null
     *
     * @param key
     * @param fields 可以是 一个String 也可以是 String数组
     * @return
     */
    public List<String> hmget(String key, String... fields) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hmget(key, fields);
        }
    }

    /**
     * 通过key给指定的field的value加上给定的值
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hincrby(String key, String field, Long value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hincrBy(key, field, value);
        }
    }

    /**
     * 通过key和field判断是否有指定的value存在
     *
     * @param key
     * @param field
     * @return
     */
    public Boolean hexists(String key, String field) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hexists(key, field);
        }
    }

    /**
     * 通过key返回field的数量
     *
     * @param key
     * @return
     */
    public Long hlen(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hlen(key);
        }
    }

    /**
     * 通过key 删除指定的 field
     *
     * @param key
     * @param fields 可以是 一个 field 也可以是 一个数组
     * @return
     */
    public Long hdel(String key, String... fields) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }

    /**
     * 通过key返回所有的field
     *
     * @param key
     * @return
     */
    public Set<String> hkeys(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hkeys(key);
        }
    }

    /**
     * 通过key返回所有和key有关的value
     *
     * @param key
     * @return
     */
    public List<String> hvals(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hvals(key);
        }
    }

    /**
     * 通过key获取所有的field和value
     *
     * @param key
     * @return
     */
    public Map<String, String> hgetall(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hgetAll(key);
        }
    }

    /**
     * 通过key向list头部添加字符串
     *
     * @param key
     * @param strs 可以是一个string 也可以是string数组
     * @return 返回list的value个数
     */
    public Long lpush(String key, String... strs) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lpush(key, strs);
        }
    }

    /**
     * 通过key向list尾部添加字符串
     *
     * @param key
     * @param strs 可以是一个string 也可以是string数组
     * @return 返回list的value个数
     */
    public Long rpush(String key, String... strs) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.rpush(key, strs);
        }
    }

    /**
     * 通过key设置list指定下标位置的value
     * 如果下标超过list里面value的个数则报错
     *
     * @param key
     * @param index 从0开始
     * @param value
     * @return 成功返回OK
     */
    public String lset(String key, Long index, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lset(key, index, value);
        }
    }

    /**
     * 通过key从对应的list中删除指定的count个 和 value相同的元素
     *
     * @param key
     * @param count 当count为0时删除全部
     * @param value
     * @return 返回被删除的个数
     */
    public Long lrem(String key, long count, String value) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lrem(key, count, value);
        }
    }

    /**
     * 通过key保留list中从strat下标开始到end下标结束的value值
     *
     * @param key
     * @param start
     * @param end
     * @return 成功返回OK
     */
    public String ltrim(String key, long start, long end) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.ltrim(key, start, end);
        }
    }

    /**
     * 通过key从list的头部删除一个value,并返回该value
     *
     * @param key
     * @return
     */
    public synchronized String lpop(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lpop(key);
        }
    }

    /**
     * 通过key从list尾部删除一个value,并返回该元素
     *
     * @param key
     * @return
     */
    synchronized public String rpop(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.rpop(key);
        }
    }

    /**
     * 通过key从一个list的尾部删除一个value并添加到另一个list的头部,并返回该value
     * 如果第一个list为空或者不存在则返回null
     *
     * @param srckey
     * @param dstkey
     * @return
     */
    public String rpoplpush(String srckey, String dstkey) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.rpoplpush(srckey, dstkey);
        }
    }

    /**
     * 通过key获取list中指定下标位置的value
     *
     * @param key
     * @param index
     * @return 如果没有返回null
     */
    public String lindex(String key, long index) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lindex(key, index);
        }
    }

    /**
     * 通过key返回list的长度
     *
     * @param key
     * @return
     */
    public Long llen(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.llen(key);
        }
    }

    /**
     * 通过key获取list指定下标位置的value
     * 如果start 为 0 end 为 -1 则返回全部的list中的value
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> lrange(String key, long start, long end) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lrange(key, start, end);
        }
    }

    /**
     * 通过key向指定的set中添加value
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 添加成功的个数
     */
    public Long sadd(String key, String... members) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sadd(key, members);
        }
    }

    /**
     * 通过key删除set中对应的value值
     *
     * @param key
     * @param members 可以是一个String 也可以是一个String数组
     * @return 删除的个数
     */
    public Long srem(String key, String... members) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.srem(key, members);
        }
    }

    /**
     * 通过key随机删除一个set中的value并返回该值
     *
     * @param key
     * @return
     */
    public String spop(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.spop(key);
        }
    }

    /**
     * 通过key获取set中的差集
     * 以第一个set为标准
     *
     * @param keys 可以 是一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    public Set<String> sdiff(String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sdiff(keys);
        }
    }

    /**
     * 通过key获取set中的差集并存入到另一个key中
     * 以第一个set为标准
     *
     * @param dstkey 差集存入的key
     * @param keys   可以 是一个string 则返回set中所有的value 也可以是string数组
     * @return
     */
    public Long sdiffstore(String dstkey, String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sdiffstore(dstkey, keys);
        }
    }

    /**
     * 通过key获取指定set中的交集
     *
     * @param keys 可以 是一个string 也可以是一个string数组
     * @return
     */
    public Set<String> sinter(String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sinter(keys);
        }
    }

    /**
     * 通过key获取指定set中的交集 并将结果存入新的set中
     *
     * @param dstkey
     * @param keys   可以 是一个string 也可以是一个string数组
     * @return
     */
    public Long sinterstore(String dstkey, String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sinterstore(dstkey, keys);
        }
    }

    /**
     * 通过key返回所有set的并集
     *
     * @param keys 可以 是一个string 也可以是一个string数组
     * @return
     */
    public Set<String> sunion(String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sunion(keys);
        }
    }

    /**
     * 通过key返回所有set的并集,并存入到新的set中
     *
     * @param dstkey
     * @param keys   可以 是一个string 也可以是一个string数组
     * @return
     */
    public Long sunionstore(String dstkey, String... keys) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sunionstore(dstkey, keys);
        }
    }

    /**
     * 通过key将set中的value移除并添加到第二个set中
     *
     * @param srckey 需要移除的
     * @param dstkey 添加的
     * @param member set中的value
     * @return
     */
    public Long smove(String srckey, String dstkey, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smove(srckey, dstkey, member);
        }
    }

    /**
     * 通过key获取set中value的个数
     *
     * @param key
     * @return
     */
    public Long scard(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.scard(key);
        }
    }

    /**
     * 通过key判断value是否是set中的元素
     *
     * @param key
     * @param member
     * @return
     */
    public Boolean sismember(String key, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.sismember(key, member);
        }
    }

    /**
     * 通过key获取set中随机的value,不删除元素
     *
     * @param key
     * @return
     */
    public String srandmember(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.srandmember(key);
        }
    }

    /**
     * 通过key获取set中所有的value
     *
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.smembers(key);
        }
    }


    /**
     * 通过key向zset中添加value,score,其中score就是用来排序的
     * 如果该value已经存在则根据score更新元素
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    public Long zadd(String key, double score, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zadd(key, score, member);
        }
    }

    /**
     * 通过key删除在zset中指定的value
     *
     * @param key
     * @param members 可以 是一个string 也可以是一个string数组
     * @return
     */
    public Long zrem(String key, String... members) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zrem(key, members);
        }
    }

    /**
     * 通过key增加该zset中value的score的值
     *
     * @param key
     * @param score
     * @param member
     * @return
     */
    public Double zincrby(String key, double score, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zincrby(key, score, member);
        }
    }

    /**
     * 通过key返回zset中value的排名
     * 下标从小到大排序
     *
     * @param key
     * @param member
     * @return
     */
    public Long zrank(String key, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zrank(key, member);
        }
    }

    /**
     * 通过key返回zset中value的排名
     * 下标从大到小排序
     *
     * @param key
     * @param member
     * @return
     */
    public Long zrevrank(String key, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zrevrank(key, member);
        }
    }

    /**
     * 通过key将获取score从start到end中zset的value
     * socre从大到小排序
     * 当start为0 end为-1时返回全部
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrevrange(String key, long start, long end) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zrevrange(key, start, end);
        }
    }

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    public Set<String> zrangebyscore(String key, String max, String min) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min);
        }
    }

    /**
     * 通过key返回指定score内zset中的value
     *
     * @param key
     * @param max
     * @param min
     * @return
     */
    public Set<String> zrangeByScore(String key, double max, double min) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zrevrangeByScore(key, max, min);
        }
    }

    /**
     * 返回指定区间内zset中value的数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zcount(String key, String min, String max) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zcount(key, min, max);
        }
    }

    /**
     * 通过key返回zset中的value个数
     *
     * @param key
     * @return
     */
    public Long zcard(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zcard(key);
        }
    }

    /**
     * 通过key获取zset中value的score值
     *
     * @param key
     * @param member
     * @return
     */
    public Double zscore(String key, String member) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zscore(key, member);
        }
    }

    /**
     * 通过key删除给定区间内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zremrangeByRank(String key, long start, long end) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zremrangeByRank(key, start, end);
        }
    }

    /**
     * 通过key删除指定score内的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zremrangeByScore(String key, double start, double end) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.zremrangeByScore(key, start, end);
        }
    }

    /**
     * 返回满足pattern表达式的所有key
     * keys(*)
     * 返回所有的key
     *
     * @param pattern
     * @return
     */
    public Set<String> keys(String pattern) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.keys(pattern);
        }
    }

    /**
     * 通过key判断值得类型
     *
     * @param key
     * @return
     */
    public String type(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.type(key);
        }
    }

    public static RedisUtil getRedisUtil() {
        if (redisUtil == null) {
            synchronized (RedisUtil.class) {
                if (redisUtil == null)
                    redisUtil = new RedisUtil();
                return redisUtil;
            }
        }
        return redisUtil;
    }

    public JedisPool getPool() {
        return pool;
    }
}
