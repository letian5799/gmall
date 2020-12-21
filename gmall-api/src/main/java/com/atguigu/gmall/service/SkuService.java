package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;

/**
 * @author letian
 */
public interface SkuService {
    /**
     * 保存商品库存单元信息
     * @param pmsSkuInfo
     */
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    /**
     * 商品详情-获取skuInfo
     * @param skuId
     * @return
     */
    PmsSkuInfo getSkuById(String skuId);
}
