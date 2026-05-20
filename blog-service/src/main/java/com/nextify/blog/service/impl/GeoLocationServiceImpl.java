package com.nextify.blog.service.impl;

import com.nextify.blog.service.GeoLocationService;
import com.nextify.blog.utils.IPUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.IPv4;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {

    private Searcher searcher;


    /**
     * 加载ip to region
     */
    @PostConstruct
    public void init() {
        try {
            InputStream inputStream = new ClassPathResource("ip2region_v4.xdb").getInputStream();
            LongByteArray content = Searcher.loadContentFromInputStream(inputStream);
            this.searcher = Searcher.newWithBuffer(Version.IPv4, content);
            log.info("ip2region_v4 loads successfully");
        } catch (Exception e) {
            log.warn("ip2region parse error");
        }
    }

    @Override
    public String getRegionByIp(String ip) {
        if (searcher == null) {
            return "IP定位服务未初始化";
        }
        if(ip == null || ip.isEmpty())
            return "";
        if(IPUtils.isIPv6(ip)){
            log.info("ipv6 is not supported currently");
            return "IPV6";
        }
        try {
            return searcher.search(ip);
        } catch (Exception e) {
            log.info("ip2region_v4 error");
            return "unknown region";
        }
    }
}
