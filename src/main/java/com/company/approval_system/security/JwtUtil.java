package com.company.approval_system.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final String SECRET = "chsuchasiuhw3y383kvv3uyrg873rg78gr38rg893gr83";

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts
                .builder()
                .subject(userDetails.getUsername())
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }
}
