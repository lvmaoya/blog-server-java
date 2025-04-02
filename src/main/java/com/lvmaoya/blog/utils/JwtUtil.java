package com.lvmaoya.blog.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JwtUtil {

    private static final String SECRET_KEY = "LumoyooLumoyooLumoyooLumoyooLumoyooLumoyooLumoyoo";
    // 这里的密钥应该妥善保管，在实际应用中可以从配置文件等地方获取更安全的密钥

    private static final long EXPIRATION_TIME = 86400000; // 示例设置1天的过期时间，单位是毫秒，可按需调整

    // 从JWT令牌中获取用户名的方法（假设用户名存储在名为"sub"的claim中，可根据实际情况修改）
    public static String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从JWT令牌中获取用户信息的方法（假设用户信息存储在名为"sub"和其他自定义 claims 中，可根据实际情况修改）
    public static Map<String, Object> getUserInfoFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims;
    }

    // 通用的获取claim的方法
    public static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 解析JWT令牌获取所有的Claims（注意如果令牌无效、过期等会抛出异常，调用方需处理）
    public static Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    // 检查令牌是否过期
    private static boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 从令牌中获取过期时间
    public static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 生成JWT令牌的方法
    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username);
    }

    // 实际执行生成令牌的内部方法
    private static String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 验证令牌有效性的方法
    public static boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) &&!isTokenExpired(token));
    }
}