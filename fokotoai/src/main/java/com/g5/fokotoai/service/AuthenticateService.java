package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.AuthenticateRequest;
import com.g5.fokotoai.dto.request.IntrospectRequest;
import com.g5.fokotoai.dto.request.LogoutRequest;
import com.g5.fokotoai.dto.response.AuthenticateResponse;
import com.g5.fokotoai.dto.response.IntrospectResponse;
import com.g5.fokotoai.dto.response.LogoutResponse;
import com.g5.fokotoai.entity.InvalidatedToken;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.repository.InvalidatedTokenRepository;
import com.g5.fokotoai.repository.StudentRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthenticateService {

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    StudentRepository studentRepository ;
    InvalidatedTokenRepository invalidatedTokenRepository ;

    public AuthenticateResponse isAuthenticatedService(AuthenticateRequest authenticateRequest){
        var student = studentRepository.findByEmail(authenticateRequest.getEmail()).orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND)) ;

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10) ;

        boolean isExactly = passwordEncoder.matches(authenticateRequest.getPasswordHash() , student.getPasswordHash());

        if(!isExactly) throw new AppException(ErrorCode.UNAUTHENTICATED) ;

        return AuthenticateResponse.builder()
                .isAuthenticate(isExactly)
                .token(generateToken(student))
                .build();
    }

    public String generateToken(Student student){

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512) ;

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(student.getEmail())
                .issuer("chuvvdepzai")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(2  , ChronoUnit.DAYS).toEpochMilli()))
                .claim("studentID" , student.getStudentId()) // we can read ID by Long studentId = claims.getLongClaim("studentId");
                .build() ;

        Payload payload =new Payload(jwtClaimsSet.toJSONObject()) ;

        JWSObject jwsObject =new JWSObject(jwsHeader , payload) ;

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        }catch(JOSEException e){
            log.warn("Can not generate token", e);
            throw new RuntimeException(e);
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {

        String token = request.getToken();

        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID() ;

        boolean verified = signedJWT.verify(verifier);

        boolean tokenExists = invalidatedTokenRepository.existsById(jwtId) ;

        if(tokenExists){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return IntrospectResponse.builder()
                .isTokenValid(verified && !tokenExists)
                .build();
    }

    public LogoutResponse logOutService(LogoutRequest request) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(request.getToken()) ;

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID() ;

        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime() ;

        Long ttl = (expiredTime.getTime() - System.currentTimeMillis())/1000 ;

        InvalidatedToken invalidatedToken = new InvalidatedToken(jwtId ,ttl) ;

        invalidatedTokenRepository.save(invalidatedToken) ;

        return LogoutResponse.builder()
                .message("Logout Successfuly")
                .build();
    }


}
