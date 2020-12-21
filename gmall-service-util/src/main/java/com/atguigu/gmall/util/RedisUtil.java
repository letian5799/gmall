package com.atguigu.gmall.util;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import java.util.ArrayList;
import java.util.List;

public class RedisUtil {
    private JedisCluster jedisCluster;
    /**
     * lua释放锁脚本：判断锁住值是否为当前线程持有，是的话解锁，不是的话解锁失败
     */
    private static final String SCRIPT_UNLOCK = "if" +
            " redis.call('get', KEYS[1]) == ARGV[1]" +
            " then" +
            " return redis.call('del', KEYS[1])" +
            " else" +
            " return 0" +
            " end";
    private static String SCRIPT_UNLOCK_SHA1 = "";
    private static final Long UNLOCK_SUCCESS_CODE = 1L;
    private static final String LOCK_SUCCESS_CODE = "ok";
    public RedisUtil(JedisCluster jedisCluster){
        this.jedisCluster = jedisCluster;
    }

    /**
     * 根据loopTryTime循环重试，根据重试时间
     * @param lockKey 锁key
     * @param lockVal 锁值，用于解锁校验
     * @param expiryTime 锁过期时间
     * @param loopTryTime 获取失败时，循环重试获取锁的时长
     * @return 是否获得锁
     */
    public boolean tryLock(String lockKey, String lockVal, long expiryTime, long loopTryTime){
        Long endTime = System.currentTimeMillis() + loopTryTime;
        while (System.currentTimeMillis() < endTime){
            if (tryLock(lockKey, lockVal, expiryTime)){
                return true;
            }
        }
        return false;
    }

    /**
     * 根据loopTryTime循环重试,根据重试次数
     * @param lockKey 锁key
     * @param lockVal 锁值，用于解锁校验
     * @param expiryTime 锁过期时间
     * @param retryTimes 重试次数
     * @param setpTime 每次重试间隔 mills
     * @return 是否获得锁
     */
    public boolean tryLock(String lockKey, String lockVal, long expiryTime, int retryTimes, long setpTime){
        while (retryTimes > 0){
            if (tryLock(lockKey, lockVal, expiryTime)){
                return true;
            }
            retryTimes--;
            try {
                Thread.sleep(setpTime);
            } catch (InterruptedException e) {
                System.out.println("get distribute lock error" +e.getLocalizedMessage());
            }
        }
        return false;
    }

    /**
     * 一次尝试，快速失败。不支持重入
     * @param lockKey 锁key
     * @param lockVal 锁值，用于解锁校验
     * @param expiryTime 锁过期时间 MILLS
     * @return 是否获得锁
     */
    public boolean tryLock(String lockKey, String lockVal, long expiryTime){
        //相比一般的分布式锁，这里把setNx和setExpiry操作合并到一起，jedis保证原子性，避免连个命令之间出现宕机等问题
        //这里我们也使用lua脚本实现
        String result = jedisCluster.set(lockKey, lockVal, "NX", "PX", expiryTime);
        return LOCK_SUCCESS_CODE.equalsIgnoreCase(result);
    }

    /**
     * 由于使用redis集群，因此每个节点都需要各自缓存一份脚本数据
     * @param slotKey 用来定位对应的slot的slotKey
     */
    public void storeScript(String slotKey){
        if (StringUtils.isEmpty(SCRIPT_UNLOCK_SHA1) || !jedisCluster.scriptExists(SCRIPT_UNLOCK_SHA1, slotKey)){
            //redis支持脚本缓存，返回哈希码，后续可以继续用来调用脚本
            SCRIPT_UNLOCK_SHA1 = jedisCluster.scriptLoad(SCRIPT_UNLOCK, slotKey);
        }
    }
    /**
     * 释放分布式锁，释放失败最可能是业务执行时间长于lockKey过期时间，应当结合业务场景调整过期时间
     * @param lockKey 锁key
     * @param lockVal 锁值
     * @return 是否释放成功
     */
    public boolean tryUnLock(String lockKey, String lockVal){
        List<String> keys = new ArrayList<>();
        keys.add(lockKey);
        List<String> argv = new ArrayList<>();
        argv.add(lockVal);
        try {
            Object result = jedisCluster.evalsha(SCRIPT_UNLOCK_SHA1, keys, argv);
            return UNLOCK_SUCCESS_CODE.equals(result);
        }catch (JedisNoScriptException e){
            //没有脚本缓存时，重新发送缓存
            System.out.println("try to store script......");
            storeScript(lockKey);
            Object result = jedisCluster.evalsha(SCRIPT_UNLOCK_SHA1, keys, argv);
            return UNLOCK_SUCCESS_CODE.equals(result);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
