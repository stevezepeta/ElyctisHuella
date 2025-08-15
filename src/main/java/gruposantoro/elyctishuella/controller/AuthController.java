package gruposantoro.elyctishuella.controller;

import gruposantoro.elyctishuella.model.User;
import gruposantoro.elyctishuella.model.dto.ApiResponse;
import gruposantoro.elyctishuella.model.dto.LoginRequest;
import gruposantoro.elyctishuella.model.dto.RegisterRequest;
import gruposantoro.elyctishuella.model.dto.UserResponse;
import gruposantoro.elyctishuella.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Metodo para Registrarse
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterRequest request) {

        try {
            User u = authService.register(request);
            UserResponse data = UserResponse.from(u);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Email registrado correctamente", data));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("Email ya esta registrado"));
        }
    }

    // Metodo para loguearse
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request) {
        return authService.Login(request)
                .map(u -> ResponseEntity.ok(
                        ApiResponse.ok("Login exitoso", UserResponse.from(u))
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("Credenciales invalidas")));
    }

}
