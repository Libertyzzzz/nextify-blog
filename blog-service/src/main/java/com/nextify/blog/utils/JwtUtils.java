package com.nextify.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
public class JwtUtils {

    @Value("${nextify.jwt.secret}")
    private String secret;

    @Value("${nextify.jwt.expire}")
    private Long expire;

    /**
     * 生成 Token
     * @param username 用户名
     * @return JWT 字符串
     */
    public String createToken(String username) {
        Date nowDate = new Date();
        // 计算过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        // 使用 HMAC-SHA 算法生成密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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

    public static void main(String[] args) {
        // 实例化加密器
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- 在这里输入你想要的明文密码 ---
        String rawPassword = "cyril.xu.work@gmail.com";

        // 执行加密
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("========================================");
        System.out.println("明文密码: " + rawPassword);
        System.out.println("加密后(请复制到数据库): " + encodedPassword);
        System.out.println("========================================");
    }
}