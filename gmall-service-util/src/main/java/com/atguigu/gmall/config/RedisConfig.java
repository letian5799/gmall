package com.atguigu.gmall.config;

import com.atguigu.gmall.util.RedisUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class RedisConfig {
    private static final String REDIS_HOST_1 = "192.168.19.101";
    private static final String REDIS_HOST_2 = "192.168.19.101";
    private static final String REDIS_HOST_3 = "192.168.19.101";
    private static final String REDIS_HOST_4 = "192.168.19.101";
    private static final String REDIS_HOST_5 = "192.168.19.101";
    private static final String REDIS_HOST_6 = "192.168.19.101";
    private static final int REDIS_PORT_1 = 7000;
    private static final int REDIS_PORT_2 = 7001;
    private static final int REDIS_PORT_3 = 7002;
    private static final int REDIS_PORT_4 = 7003;
    private static final int REDIS_PORT_5 = 7004;
    private static final int REDIS_PORT_6 = 7005;
    private static final int REDIS_MAX_IDLE = 10;
    private static final int REDIS_MIN_IDLE = 1;
    private static final int REDIS_MAX_TOTAL = 10;
    private static final int REDIS_MAX_WAIT_MILLIS = 2000;
    private static final String REDIS_PASSWORD= "123456";
    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        // Jedis连接池配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 最大空闲连接数, 默认8个
        jedisPoolConfig.setMaxIdle(REDIS_MAX_IDLE);
        // 最大连接数, 默认8个
        jedisPoolConfig.setMaxTotal(REDIS_MAX_TOTAL);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(REDIS_MIN_IDLE);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(REDIS_MAX_WAIT_MILLIS); // 设置2秒
        //对拿到的connection进行validateObject校验
        jedisPoolConfig.setTestOnBorrow(false);
        return jedisPoolConfig;
    }
    @Bean
    public JedisCluster jedisCluster(){
        Set<HostAndPort> nodesList=new HashSet<>();
        nodesList.add(new HostAndPort(REDIS_HOST_1,REDIS_PORT_1));
        nodesList.add(new HostAndPort(REDIS_HOST_2,REDIS_PORT_2));
        nodesList.add(new HostAndPort(REDIS_HOST_3,REDIS_PORT_3));
        nodesList.add(new HostAndPort(REDIS_HOST_4,REDIS_PORT_4));
        nodesList.add(new HostAndPort(REDIS_HOST_5,REDIS_PORT_5));
        nodesList.add(new HostAndPort(REDIS_HOST_6,REDIS_PORT_6));
        JedisCluster jedisCluster=new JedisCluster(nodesList,10000,10000,10,REDIS_PASSWORD,jedisPoolConfig());
        return jedisCluster;
    }
    @Bean
    public RedissonClient redissonClient(){
        String[] clusterNodes = new String[]{
                "redis://" + REDIS_HOST_1 + ":" +REDIS_PORT_1,
                "redis://" + REDIS_HOST_2 + ":" +REDIS_PORT_2,
                "redis://" + REDIS_HOST_3 + ":" +REDIS_PORT_3,
                "redis://" + REDIS_HOST_4 + ":" +REDIS_PORT_4,
                "redis://" + REDIS_HOST_5 + ":" +REDIS_PORT_5,
                "redis://" + REDIS_HOST_6 + ":" +REDIS_PORT_6,
        };
        Config config = new Config();
        // 添加集群地址
        ClusterServersConfig clusterServersConfig = config.useClusterServers().addNodeAddress(clusterNodes);
        // 设置密码
        clusterServersConfig.setPassword(REDIS_PASSWORD);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
    @Bean
    public RedisUtil redisUtil(){
        RedisUtil redisUtil = new RedisUtil(jedisCluster());
        return redisUtil;
    }
}
