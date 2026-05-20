package com.nextify.blog.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // 告诉解析器：遇到 VO 里没有的字段，直接当没看见
@ToString
public class AddressInfoVO {
    // 高德返回的是 "formatted_address"，进行映射

    private Regeocode regeocode;



    @Getter
    @Setter
    public static class Regeocode {
        @JsonProperty("formatted_address")
        private String formattedAddress;


        private List<AoiInfo> aois;

        private List<PoiInfo> pois;


    }



    // 类名随你起（叫 AoiInfo 挺好），但外层引用它的变量名必须叫 aois
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AoiInfo {
        private String name;
        private String distance;
    }

    // 类名随你起（叫 PoiInfo 挺好），但外层引用它的变量名必须叫 pois
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PoiInfo {
        private String name;
        private String distance;
        private String type;
        private String address;
    }
}
