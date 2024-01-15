package cn.biq.mn.utils;

import cn.biq.mn.response.BaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
public class WebUtils {

    private final HttpServletRequest servletRequest;
    private final RestTemplate restTemplate;

    public String getRequestIP() {
        String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
            // you can add more matching headers here ...
        };
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = servletRequest.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return servletRequest.getRemoteAddr();
    }

    public static void response(HttpServletResponse response, BaseResponse data) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        try {
//            ServletOutputStream os = servletResponse.getOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
            response.getWriter().write(json);
//            os.write(json.getBytes(StandardCharsets.UTF_8));
//            os.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public HashMap<String, Object> get(String url) {
        String res = restTemplate.getForObject(url, String.class);
        HashMap<String, Object> baseMap;
        try {
            baseMap = new ObjectMapper().readValue(res, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return baseMap;
    }

}
