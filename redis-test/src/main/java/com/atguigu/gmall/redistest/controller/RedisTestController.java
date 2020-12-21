package com.atguigu.gmall.redistest.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;

@Controller
public class RedisTestController {
    @Autowired
    JedisCluster jedisCluster;
    @Autowired
    RedissonClient redissonClient;
    //@Autowired
    //RedisUtil redisUtil;

    @RequestMapping("testJedis")
    @ResponseBody
    public String testJedis(){
        jedisCluster.set("aaa", "111");
        String aaa = jedisCluster.get("aaa");
        return aaa;
    }

    @RequestMapping("testRedisson")
    @ResponseBody
    public String testRedisson(){
        RLock lock = redissonClient.getLock("lock");// 声明锁
        lock.lock();//上锁
        try {
            String v = jedisCluster.get("k");
            if (StringUtils.isBlank(v)) {
                v = "1";
            }
            System.out.println("---->" + v);
            jedisCluster.set("k", (Integer.parseInt(v) + 1) + "");
        }finally {
            lock.unlock();// 解锁
            // jedisCluster内部使用了池化技术，每次使用完毕都会自动释放Jedis因此不需要关闭。
            // 如果调用close方法后再调用jedisCluster的api进行操作时就会出现如上错误
        }
        return "success";
    }


}
