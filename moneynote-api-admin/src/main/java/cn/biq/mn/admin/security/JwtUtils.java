package cn.biq.mn.admin.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

// https://stackoverflow.com/questions/7270681/utility-class-in-spring-application-should-i-use-static-methods-or-not
// https://stackoverflow.com/questions/13746080/spring-or-not-spring-should-we-create-a-component-on-a-class-with-static-meth
// https://www.jianshu.com/p/6d8c8fae1918
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final String secretKey = "I am the king";
    private final long validityInMs = 3600000*24*7;

    public String createToken(Authentication authentication) {
        var userDetails = (MyUserDetails) authentication.getPrincipal();
        return JWT.create().withSubject(authentication.getName())
                .withClaim("userId", userDetails.getUser().getId())
                .withExpiresAt(new Date((new Date()).getTime() + validityInMs))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public Authentication getAuthentication(String jwtToken) {
        return new UsernamePasswordAuthenticationToken(getSubject(jwtToken), null, null);
    }

    public String getSubject(String jwtToken) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        return verifier.verify(jwtToken).getSubject();
    }

    public Integer getUserId(String jwtToken) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        return verifier.verify(jwtToken).getClaim("userId").asInt();
    }

    public boolean verify(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        verifier.verify(token);
        return true;
    }
}
