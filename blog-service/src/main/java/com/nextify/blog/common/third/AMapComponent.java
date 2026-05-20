package com.nextify.blog.common.third;

import com.nextify.blog.utils.CoordinateUtils;
import com.nextify.blog.vo.AddressInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AMapComponent {
    private static final String KEY = "3a3539b556c835297d7881ac67c08c09";
    private static final String PREFIX = "https://restapi.amap.com/v3/geocode/regeo?key=%s&location=%s";

    @Resource
    private RestTemplate restTemplate;

    public String  getDetailAddress(String longitude, String latitude) {
        if (!StringUtils.hasText(longitude) || !StringUtils.hasText(latitude))
            return "unknown address";
        double[] coordinate =
                CoordinateUtils.wgs84ToGcj02(Double.parseDouble(longitude), Double.parseDouble(latitude));
        String location = coordinate[0] + "," + coordinate[1];
        String url = String.format(PREFIX, KEY, location);
        log.info("url: {}", url);
        ResponseEntity<AddressInfoVO> response = restTemplate.getForEntity(url, AddressInfoVO.class);
        log.info(response.toString());
        if (ObjectUtils.isEmpty(response) || response.getStatusCode().value() != 200 || ObjectUtils.isEmpty(response.getBody())) {
            log.info("获取地址信息失败");
            return "unknown address";
        }
        return extractPureAddress(response.getBody());
    }
    public String extractPureAddress(AddressInfoVO vo) {
        if(ObjectUtils.isEmpty(vo.getRegeocode())){
            return "unknown address";
        }

        AddressInfoVO.Regeocode regeocode = vo.getRegeocode();
        String baseAddress = regeocode.getFormattedAddress();

        // ==== 1. 绝杀：优先地毯式扫描 20 米内的精准 POI (如星巴克、大楼)
        if (regeocode.getPois() != null && !regeocode.getPois().isEmpty()) {
            for (AddressInfoVO.PoiInfo poi : regeocode.getPois()) {
                if (Double.parseDouble(poi.getDistance()) <= 20.0) {
                    return baseAddress + " (" + poi.getName() + ")";
                }
            }
        }

        // 2. 次选：扫描 50 米内的 AOI 围栏 (如大学园区、封闭小区) ====
        if (regeocode.getAois() != null && !regeocode.getAois().isEmpty()) {
            AddressInfoVO.AoiInfo closestAoi = regeocode.getAois().get(0);
            if (Double.parseDouble(closestAoi.getDistance()) <= 50.0) {
                return baseAddress + " (" + closestAoi.getName() + ")";
            }
        }

        // ==== 3. 保底 ====
        return baseAddress;
    }
}
