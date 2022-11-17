package ru.avdeev.marketsimpleapi.routers.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.config.JwtUtil;
import ru.avdeev.marketsimpleapi.dto.AuthRequest;
import ru.avdeev.marketsimpleapi.entities.User;
import ru.avdeev.marketsimpleapi.mappers.UserMapper;
import ru.avdeev.marketsimpleapi.services.UserService;

@Component
public class AuthHandler {

    UserService service;
    UserMapper userMapper;
    JwtUtil jwtUtil;

    public Mono<ServerResponse> auth(ServerRequest request) {

        return request.bodyToMono(AuthRequest.class)
                .flatMap(credentials -> service.findByUsername(credentials.getUsername())
                        .cast(User.class)
                        .zipWith(Mono.just(credentials.getPassword()))
                        .flatMap(t -> service.checkPassword(t.getT2(), t.getT1().getPassword())
                                .flatMap(isAuth -> isAuth ?
                                        ServerResponse.ok().bodyValue(userMapper.toAuthResponse(t.getT1(), jwtUtil.generateToken(t.getT1())))
                                        :
                                        ServerResponse.status(HttpStatus.UNAUTHORIZED).build())
                        )
                ).switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Autowired
    public void init(UserService service, JwtUtil util, UserMapper um) {
        this.service = service;
        jwtUtil = util;
        userMapper = um;
    }
}
