package com.lhf.gmall.util;

import io.jsonwebtoken.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shkstart
 * @create 2018-09-19 1:50
 */
public class JwtUtil {
    public static String encode(String key, Map<String, Object> param, String salt) {
        if (salt != null) {
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;
    }

    public static Map<String, Object> decode(String token, String key, String salt) {
        Claims claims = null;
        if (salt != null) {
            key += salt;
        }
        try {
            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            return null;
        }
        return claims;
    }

    public static void main(String[] args) {
//        Map<String,Object> map = new HashMap<>();
//        map.put("nikeName", "zxc");
//        map.put("passswd", "123456");
//        String salt = "192.168.234.128";
//        String gmall0508 = encode("gmall0508", map, salt);
//        System.out.println(gmall0508);
//        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6Inp4YyIsInVzZXJJZCI6IjIifQ.0DYv9LPjO3MI6QjdPYoZU5tRayZ5euw_DuaESG7_rAQ";
//        Map<String, Object> gmall05081 = decode(token, "gmall0508", salt);
//        System.out.println(gmall05081);
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6Inp4YyIsInVzZXJJZCI6IjIifQ.0DYv9LPjO3MI6QjdPYoZU5tRayZ5euw_DuaESG7_rAQ";
        Map<String, Object> gmall0508 = decode(token, "gmall0508", "192.168.234.18");
        System.out.println(gmall0508);
    }
}
