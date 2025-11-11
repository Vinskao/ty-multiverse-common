package tw.com.ty.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static tw.com.ty.common.security.config.BaseSecurityConfig.SecurityConstants.*;

/**
 * JWT Token 提供者
 *
 * <p>提供 JWT token 的生成、解析和驗證功能：</p>
 * <ul>
 *   <li>Token 生成</li>
 *   <li>Token 解析</li>
 *   <li>Token 驗證</li>
 *   <li>用戶信息提取</li>
 * </ul>
 *
 * @author TY Backend Team
 * @version 1.0
 * @since 2025
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:ty-multiverse-default-secret-key-for-development-only}")
            String jwtSecret,
            @Value("${jwt.expiration:86400000}") // 24小時
            long jwtExpirationInMs) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    /**
     * 生成 JWT token
     *
     * @param username 用戶名
     * @param claims 額外聲明
     * @return JWT token
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成簡單的 JWT token
     *
     * @param username 用戶名
     * @return JWT token
     */
    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    /**
     * 從 token 中提取用戶名
     *
     * @param token JWT token
     * @return 用戶名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 從 token 中提取用戶 ID
     *
     * @param token JWT token
     * @return 用戶 ID
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(JWT_CLAIMS_USER_ID, String.class));
    }

    /**
     * 從 token 中提取角色列表
     *
     * @param token JWT token
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get(JWT_CLAIMS_ROLES, java.util.List.class));
    }

    /**
     * 驗證 token 是否有效
     *
     * @param token JWT token
     * @param username 用戶名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (username.equals(extractedUsername) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 檢查 token 是否過期
     *
     * @param token JWT token
     * @return 是否過期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 提取 token 過期時間
     *
     * @param token JWT token
     * @return 過期時間
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 從 token 中提取聲明
     *
     * @param token JWT token
     * @param claimsResolver 聲明解析器
     * @return 聲明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取所有聲明
     *
     * @param token JWT token
     * @return 所有聲明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 從授權頭部提取 token
     *
     * @param authHeader 授權頭部
     * @return JWT token（去除 Bearer 前綴）
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(JWT_PREFIX)) {
            return authHeader.substring(JWT_PREFIX.length());
        }
        return null;
    }

    /**
     * 檢查 token 格式是否正確
     *
     * @param token JWT token
     * @return 是否為 Bearer token
     */
    public boolean isBearerToken(String token) {
        return token != null && token.startsWith(JWT_PREFIX);
    }
}
