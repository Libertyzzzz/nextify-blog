package com.nextify.blog.utils; // 换成你自己的项目包名

import java.util.Arrays;

/**
 * 国际标准坐标 (WGS-84) 转 火星坐标 (GCJ-02) 后端工具类
 */
public class CoordinateUtils {

    private static final double A = 6378245.0; // 地球赤道半径(米)
    private static final double EE = 0.00669342162296594323; // 偏心率平方

    /**
     * WGS-84 原始卫星坐标 转 高德/腾讯所需的 GCJ-02(火星) 坐标
     *
     * @param lng 原始经度
     * @param lat 原始纬度
     * @return double[0] 为火星经度, double[1] 为火星纬度
     */
    public static double[] wgs84ToGcj02(double lng, double lat) {
        if (outOfChina(lng, lat)) {
            return new double[]{lng, lat}; // 如果不在中国境内，不进行加密偏移
        }
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * Math.PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * Math.PI);

        double mgLat = lat + dLat;
        double mgLng = lng + dLng;
        return new double[]{mgLng, mgLat};
    }

    private static boolean outOfChina(double lng, double lat) {
        if (lng < 72.004 || lng > 137.8347) {
            return true;
        }
        return lat < 0.8293 || lat > 55.8271;
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    public static void main(String[] args) {
        double[] gcj02 = wgs84ToGcj02(113.950817, 2.584546);
        System.out.println(Arrays.toString(gcj02));
    }

}