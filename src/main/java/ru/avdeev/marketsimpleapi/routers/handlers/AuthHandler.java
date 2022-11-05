package ru.avdeev.marketsimpleapi.routers.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.config.JwtUtil;
import ru.avdeev.marketsimpleapi.dto.AuthRequest;
import ru.avdeev.marketsimpleapi.dto.AuthResponse;
import ru.avdeev.marketsimpleapi.entities.User;
import ru.avdeev.marketsimpleapi.services.UserService;

@Component
public class AuthHandler {

    UserService service;
    JwtUtil jwtUtil;
    public Mono<ServerResponse> auth(ServerRequest request) {

         return request.bodyToMono(AuthRequest.class)
                .flatMap(credentials -> service.findByUsername(credentials.getUsername())
                        .cast(User.class)
                        .zipWith(Mono.just(credentials.getPassword()))
                        .flatMap(t -> service.checkPassword(t.getT2(), t.getT1().getPassword())
                                .flatMap(isAuth -> isAuth ?
                                        ServerResponse.ok().bodyValue(new AuthResponse(jwtUtil.generateToken(t.getT1())))
                                        :
                                        ServerResponse.status(HttpStatus.UNAUTHORIZED).build())
                        )
                ).switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Autowired
    public void init(UserService service, JwtUtil util) {
        this.service = service;
        jwtUtil = util;
    }
}
