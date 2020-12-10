package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.atguigu.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        // 根据三级类目id查询平台属性信息
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        // 查询所有属性的属性值，并塞入pmsBaseAttrInfos
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> attrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(attrValues);
        }
        return pmsBaseAttrInfos;
    }

    /**
     * 添加平台属性和属性值
     * @param pmsBaseAttrInfo （三级类目id，属性id，属性名称，属性值列表）
     */
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        try {
            String id = pmsBaseAttrInfo.getId();
            if(StringUtils.isBlank(id)){
                // 属性id为空，保存属性
                //insert insertSelective 是否将null插入数据库
                pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
                // 保存属性值
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    // 为属性值绑定属性id
                    pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                    pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
                }
            }else{
                // id不空，则修改属性
                Example example = new Example(PmsBaseAttrInfo.class);
                // andEqualTo的第一个参数是bean映射的字段名字，而不是数据库中存储的字段名
                example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
                pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

                // 属性值修改
                // 按照属性id删除所有属性值
                PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
                pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);

                // 删除后，将新的属性值插入
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                    pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
                }
            }
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }

    /**
     * 根据属性id查询属性值列表
     * @param attrId
     * @return
     */
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

    /**
     * 查询平台的基本销售属性
     * @return
     */
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }
}
