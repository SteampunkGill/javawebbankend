package com.milktea.app.service;

import com.milktea.app.dto.home.HomePageResDTO;
import com.milktea.app.dto.product.ProductListResDTO;

import java.math.BigDecimal;

public interface HomeService {

    /**
     * 获取首页数据
     * @param latitude 纬度
     * @param longitude 经度
     * @return 首页数据
     */
    HomePageResDTO getHomePageData(BigDecimal latitude, BigDecimal longitude);

    /**
     * 获取推荐商品
     * @param limit 限制数量
     * @param type 推荐类型
     * @return 推荐商品列表
     */
    ProductListResDTO getRecommendedProducts(Integer limit, String type);
}