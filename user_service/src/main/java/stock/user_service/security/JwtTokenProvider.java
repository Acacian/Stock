package stock.user_service.security;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.accessTokenExpirationInMs}")
    private int accessTokenExpirationInMs;

    @Value("${app.jwt.refreshTokenExpirationInMs}")
    private int refreshTokenExpirationInMs;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = new SecretKeySpec(hashSecret(jwtSecret), SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, accessTokenExpirationInMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenExpirationInMs);
    }

    public String generateToken(Authentication authentication, int expirationInMs) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationInMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private byte[] hashSecret(String secret) {
        try {
            return MessageDigest.getInstance("SHA-512")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize JWT signing key", e);
        }
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }
}
