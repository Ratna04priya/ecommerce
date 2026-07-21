package com.example.ecommerce.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(UserPrincipal principal) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);

		String roles = principal.getAuthorities().stream()
				.map(auth -> auth.getAuthority())
				.collect(Collectors.joining(","));

		return Jwts.builder()
				.subject(principal.getUsername())
				.claim("uid", principal.getId())
				.claim("roles", roles)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserPrincipal principal) {
		String username = extractUsername(token);
		return username.equals(principal.getUsername()) && !isExpired(token);
	}

	private boolean isExpired(String token) {
		return parseClaims(token).getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
