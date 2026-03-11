package cat.itacademy.battleship_api.controller;

import cat.itacademy.battleship_api.dto.AuthRequest;
import cat.itacademy.battleship_api.dto.AuthResponse;
import cat.itacademy.battleship_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, exception.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException exception) {
            return ResponseEntity.status(401).body(new AuthResponse(null, exception.getMessage()));
        }
    }
}