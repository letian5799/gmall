package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    JedisCluster jedisCluster;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        // 插入skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        // 插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        // 插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        // 插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }


    }
    public PmsSkuInfo getSkuByIdFromDB(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> skuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }

    /**
     * 使用redis缓存，商品详情查询是一个高并发业务
     * redis的key结构为object:id:filed
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = null;
        try {
            pmsSkuInfo = new PmsSkuInfo();
            String key = "sku:" + skuId + ":info";
            // 1.查询缓存
            String skuJson = jedisCluster.get(key);
            // 2.判断缓存是否存在，若存在，则直接返回，若不存在则查询数据库
            if(StringUtils.isNotBlank(skuJson)){
                pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
            }else{
                //如果缓存中没有，则查询数据库
                // 设置分布式锁,采用redis原生API实现锁
                String token = UUID.randomUUID().toString();
                // 加锁
                //String ok = jedisCluster.set("sku:" + skuId + ":lock",token,"nx","px",5*1000);
                boolean tryLock = redisUtil.tryLock("sku:" + skuId + ":lock", token, 5 * 1000);
                // 判断拿到当前锁，并且锁的值要等于当前的token值
                if(tryLock){
                    pmsSkuInfo = getSkuByIdFromDB(skuId);
                    if(pmsSkuInfo != null){
                        // 更新缓存
                        jedisCluster.set(key, JSON.toJSONString(pmsSkuInfo));
                    }else{
                        // 数据库中没有该sku信息，我们应该在缓存中设置空值，防止缓存穿透
                        jedisCluster.setex(key,60*3,JSON.toJSONString(""));
                    }
                    // 释放锁,设置删除锁的权限，比对两次的锁值是否相同进行判断有无删除权限
                    // 不严谨的写法
                    /*String delToken = jedisCluster.get("sku:" + skuId + ":lock");
                    if(StringUtils.isNotBlank(delToken) && delToken.equals(token)){
                        jedisCluster.del("sku:" + skuId + ":lock");
                    }*/
                    // 使用lua脚本，在查询到key的同时删除key,防止高并发下的意外发生
                    redisUtil.tryUnLock("sku:" + skuId + ":lock",token);
                }else{
                    // 设置失败，自旋
                    System.out.println("未拿到分布式锁，开始自旋...");
                    // 不加return的话会重新开辟一个线程，是错误写法
                    return getSkuById(skuId);
                }
            }
            // jedisCluster内部使用了池化技术，每次使用完毕都会自动释放Jedis因此不需要关闭。
            // 如果调用close方法后再调用jedisCluster的api进行操作时就会出现如上错误
            //jedisCluster.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pmsSkuInfo;
    }
}
