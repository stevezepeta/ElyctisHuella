package gruposantoro.elyctishuella.service;

import gruposantoro.elyctishuella.model.User;
import gruposantoro.elyctishuella.model.dto.LoginRequest;
import gruposantoro.elyctishuella.model.dto.RegisterRequest;
import gruposantoro.elyctishuella.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User register(RegisterRequest req) {
        // No tiene validaciones, la unica validacion que tiene es que el email sea duplicado
        if(userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email ya esta registrado");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        return userRepository.save(user);
    }

    public Optional<User> Login(LoginRequest req) {
        return userRepository.findByEmail(req.getEmail())
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPassword()));
    }

}
