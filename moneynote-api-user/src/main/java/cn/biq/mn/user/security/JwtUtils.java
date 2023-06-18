package cn.biq.mn.user.security;

import cn.biq.mn.user.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

// https://stackoverflow.com/questions/7270681/utility-class-in-spring-application-should-i-use-static-methods-or-not
// https://stackoverflow.com/questions/13746080/spring-or-not-spring-should-we-create-a-component-on-a-class-with-static-meth
// https://www.jianshu.com/p/6d8c8fae1918
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final String secretKey = "rzxlszyykpbgqcflzxsqcysyhljt";
    private final long validityInMs = 3600000*24*30; //30天

    public String createToken(User user) {
        return JWT.create().withSubject(user.getId().toString())
                .withClaim("userId", user.getId())
                .withExpiresAt(new Date((new Date()).getTime() + validityInMs))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public Integer getUserId(String jwtToken) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        return verifier.verify(jwtToken).getClaim("userId").asInt();
    }

}
