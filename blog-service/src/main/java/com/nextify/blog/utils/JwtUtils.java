package com.nextify.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 负责令牌的签发、解析和验证
 */
@Component
@Slf4j
@Getter
public class JwtUtils {

    @Value("${nextify.jwt.secret}")
    private String secret;

    @Value("${nextify.jwt.expire}")
    private Long expire;

    @Value("${nextify.jwt.max-refresh}")
    private Long maxRefresh;


    /**
     * 生成 Token
     * @param username 用户名
     * @return JWT 字符串
     */
    public String createToken(String username, Long initLoginTime) {
        Date nowDate = new Date();
        // 计算过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        // 说明是首次登陆
        if(initLoginTime == null)
            initLoginTime = nowDate.getTime();

        // 计算最大过期时间，始终是initLoginTime + 24小时
        long maxExpireTime = initLoginTime + maxRefresh * 1000;
        // 使用 HMAC-SHA 算法生成密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .claim("initLoginTime", initLoginTime)
                .claim("maxExpire", nowDate.getTime() + maxRefresh * 1000) //Token存续最大时间
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String encrypt(String plainText){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(plainText);
    }

    public static boolean verify(String plainText, String cipherText){
        return new BCryptPasswordEncoder().matches(plainText, cipherText);
    }

    /**
     * 解析 Token 里的 Claims (荷载)
     */
    public Claims getClaimsByToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 解析失败（Token 被篡改或格式错误）返回 null
            return null;
        }
    }

    /**
     * 检查 Token 是否已过期
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 检查是否超过token最大存续时间
     * @param claims
     */
    public boolean isMaxExpired(Claims claims) {
        Long maxExpire = claims.get("maxExpire", Long.class);
        return maxExpire != null && maxExpire < System.currentTimeMillis();
    }

    public static void main(String[] args) {
        // 实例化加密器
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- 在这里输入你想要的明文密码 ---
        String rawPassword = "ylq123456";

        // 执行加密
        String encodedPassword = encoder.encode(rawPassword);
        String s = "nextify-wsx";
        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密后(请复制到数据库): " + encodedPassword);
        System.out.println("========================================");
        System.out.println("加密后(请复制到数据库): " + encoder.matches(s, "$2a$10$fcK43wBcpSylswltIXjGWeC47Kzhuc23TMt8eupT/GbOTvjuQDsR6"));
    }
}