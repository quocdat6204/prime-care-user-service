package com.example.user_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.user_service.dto.JwtInfo;
import com.example.user_service.dto.TokenPayload;
import com.example.user_service.model.RedisToken;
import com.example.user_service.model.User;
import com.example.user_service.repository.RedisTokenRepository;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretkey;

    private final RedisTokenRepository redisTokenRepository;

    public TokenPayload generateAccessToken(User user){

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expiredTime = Date.from(issueTime.toInstant().plus(30, ChronoUnit.MINUTES));
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUuid())
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .jwtID(jwtId)
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);
        try {
            jwsObject.sign(new MACSigner(secretkey));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        String token = jwsObject.serialize();

        return TokenPayload.builder()
                .jwtId(jwtId)
                .token(token)
                .expiredTime(expiredTime)
                .build();
    }

    public TokenPayload generateRefreshToken(User user){

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Date issueTime = new Date();
        Date expiredTime = Date.from(issueTime.toInstant().plus(14, ChronoUnit.DAYS));
        String jwtId = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUuid())
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .jwtID(jwtId)
                .claim("id", user.getId())
                .claim("role", user.getRole().name())
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);
        try {
            jwsObject.sign(new MACSigner(secretkey));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        String token = jwsObject.serialize();

        return TokenPayload.builder()
                .token(token)
                .jwtId(jwtId)
                .expiredTime(expiredTime)
                .build();
    }

    public void verifyToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (expirationTime.before(new Date())) {
            throw new RuntimeException("Token expired");
        }

        String jwtID = signedJWT.getJWTClaimsSet().getJWTID();
        Optional<RedisToken> byId = redisTokenRepository.findById(jwtID);
        if(byId.isPresent()){
            throw new RuntimeException("Token already logout");
        }

        if (!signedJWT.verify(new MACVerifier(secretkey))) {
            throw new RuntimeException("Invalid token");
        }
    }

    public String verifyRefreshToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (expirationTime.before(new Date())) {
            throw new RuntimeException("Refresh token expired");
        }

        if (!signedJWT.verify(new MACVerifier(secretkey))) {
            throw new RuntimeException("Invalid refresh token");
        }

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Optional<RedisToken> byId = redisTokenRepository.findById(jwtId);
        if (byId.isEmpty()) {
            throw new RuntimeException("Refresh token not found or revoked");
        }

        return signedJWT.getJWTClaimsSet().getSubject();
    }

    public JwtInfo parseToken(String token) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
        Date expiredTime= signedJWT.getJWTClaimsSet().getExpirationTime();

        return JwtInfo.builder()
                .jwtId(jwtId)
                .issueTime(issueTime)
                .expiredTime(expiredTime)
                .build();
    }

}

