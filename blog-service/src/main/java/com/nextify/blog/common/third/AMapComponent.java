package com.nextify.blog.common.third;

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
        if(!StringUtils.hasText(longitude) || !StringUtils.hasText(latitude))
            return "unknown address";
        String location = longitude + "," + latitude;
        String url =  String.format(PREFIX, KEY, location);
        log.info("url: {}", url);
        ResponseEntity<AddressInfoVO> response =  restTemplate.getForEntity(url, AddressInfoVO.class);
        log.info(response.toString());
        if(ObjectUtils.isEmpty(response) || response.getStatusCode().value() != 200|| ObjectUtils.isEmpty(response.getBody())) {
            log.info("获取地址信息失败");
            return "unknown address";
        }
        AddressInfoVO res = response.getBody();
        log.info(res.toString());
        if(ObjectUtils.isEmpty(res.getRegeocode())){
            return "unknown address";
        }

        return res.getRegeocode().getFormattedAddress();
    }
}
