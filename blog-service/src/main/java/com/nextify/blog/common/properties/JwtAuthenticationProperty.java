package com.nextify.blog.common.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "nextify.jwt")
@Data
@Configuration
public class JwtAuthenticationProperty {

    /**
     * 请求头名称
     */
    private String header = "Authorization";

    /**
     * 密钥（
     */
    private String secret = "NextifyBlogAppleStyleRomanticLogicSecretKey2025";

    /**
     * Token 过期时间（秒），默认 30 分钟
     */
    private Long expire = 1800L;

    /**
     * 最大刷新窗口（秒），默认 14 天
     */
    private Long maxRefresh = 1209600L;

    /**
     * 刷新窗口
     */
    private Long refresh = 86400L;

    /**
     * Cookie 路径
     */
    private String cookiePath = "/";

    /** Cookie 名称 */
    private String cookieName = "refresh_token";

    /**
     * Cookie 域名（空则使用当前域名）
     */
    private String cookieDomain = "";

    /**
     * 是否启用 Secure（HTTPS 环境设为 true）
     */
    private Boolean cookieSecure = false;

    /**
     * 是否启用 HttpOnly
     */
    private Boolean cookieHttpOnly = true;
}
