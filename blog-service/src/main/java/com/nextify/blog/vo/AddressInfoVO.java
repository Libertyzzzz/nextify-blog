package com.nextify.blog.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

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
    }
}
