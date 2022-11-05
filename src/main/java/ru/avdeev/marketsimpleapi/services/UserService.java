package ru.avdeev.marketsimpleapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.avdeev.marketsimpleapi.entities.User;
import ru.avdeev.marketsimpleapi.repository.UserRepository;
import ru.avdeev.marketsimpleapi.repository.RoleRepository;

@Service
public class UserService implements ReactiveUserDetailsService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder encoder;

    @Override
    @Transactional
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> roleRepository.findByUserId(user.getId())
                        .collectList()
                        .flatMap(roles -> {
                            user.setRoles(roles);
                            return Mono.just(user);
                        }))
                .cast(UserDetails.class);
    }

    public Mono<User> register(User user) {
        return userRepository.save(user);
    }

    public Mono<Boolean> checkPassword(String passwordForCheck, String password) {
        return Mono.just(encoder.matches(passwordForCheck, password));
    }

    @Autowired
    public void init(UserRepository repository, RoleRepository roleRepository, PasswordEncoder pe) {
        this.userRepository = repository;
        this.roleRepository = roleRepository;
        this.encoder = pe;
    }
}
