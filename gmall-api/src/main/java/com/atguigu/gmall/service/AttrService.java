package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

public interface AttrService {
    /**
     * 查询平台属性及属性值
     * @param catalog3Id 三级类目id
     * @return 属性列表（包含属性值）
     */
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    /**
     * 添加平台属性
     * @param pmsBaseAttrInfo 前台传过来的参数
     * @return 结果值，success or fail
     */
    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    /**
     * 根据属性id查询属性值列表
     * @param attrId
     * @return
     */
    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    /**
     * 获取平台基础属性
     * @return
     */
    List<PmsBaseSaleAttr> baseSaleAttrList();
}
