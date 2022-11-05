package ru.avdeev.marketsimpleapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        String authToken = authentication.getCredentials().toString();
        String username = null;
        try {
            username = jwtUtil.extractUsername(authToken);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (username != null && jwtUtil.validateToken(authToken)) {


            List<HashMap<String, String>> roles = jwtUtil.getClaims(authToken).get("roles", List.class);

            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.get("role")))
                    .collect(Collectors.toList());

            return Mono.just(new UsernamePasswordAuthenticationToken(username, null, authorities));
        }

        return Mono.empty();
    }

    @Autowired
    public void init(JwtUtil util) {
        jwtUtil = util;
    }
}
