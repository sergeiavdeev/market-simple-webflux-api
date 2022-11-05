package ru.avdeev.marketsimpleapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.avdeev.marketsimpleapi.entities.Role;
import ru.avdeev.marketsimpleapi.entities.User;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String salt;

    @Value("${jwt.expiration}")
    private String expirationTime;

    public String extractUsername(String authToken) {

        return getClaims(authToken)
                .getSubject();
    }

    public Claims getClaims(String authToken) {
        return Jwts.parserBuilder()
                .setSigningKey(salt.getBytes())
                .build()
                .parseClaimsJws(authToken)
                .getBody();
    }

    public boolean validateToken(String authToken) {
        return getClaims(authToken)
                .getExpiration()
                .after(new Date());
    }

    public String generateToken(User user) {

        HashMap<String, List<Role>> claims = new HashMap<>();
        claims.put("roles", user.getRoles());

        long expirationSeconds = Long.parseLong(expirationTime);
        Date creationDate = new Date();
        Date expirationDate = new Date(creationDate.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(creationDate)
                .setExpiration(expirationDate)
                .signWith(Keys.hmacShaKeyFor(salt.getBytes()))
                .compact();
    }
}
