package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;
    /**
     * 测试thymeleaf模板
     * @return
     */
    @RequestMapping("index")
    public String index(Model model){
        // 字符串取值
        model.addAttribute("text1", "hello");
        model.addAttribute("text2", "<span style='color:red;'>我是 springboot的亲儿子</span>");
        // 对象取值
        UmsMember member = new UmsMember();
        member.setId("1");
        member.setBirthday(new Date());
        member.setGender(1);
        member.setNickname("jakson");
        model.addAttribute("member", member);
        return "index";
    }

    /**
     * 商品详情页
     * @param skuId
     * @param modelMap
     * @return
     */
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap){
        // 根据skuId查询skuInfo
        PmsSkuInfo skuInfo =  skuService.getSkuById(skuId);
        modelMap.addAttribute("skuInfo", skuInfo);

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(skuInfo.getProductId(),skuInfo.getId());
        modelMap.addAttribute("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
        return "item";
    }

}
