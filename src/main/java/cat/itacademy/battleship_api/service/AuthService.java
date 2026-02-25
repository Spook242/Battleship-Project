package cat.itacademy.battleship_api.service;

import cat.itacademy.battleship_api.dto.AuthRequest;
import cat.itacademy.battleship_api.dto.AuthResponse;
import cat.itacademy.battleship_api.model.Player;
import cat.itacademy.battleship_api.repository.PlayerRepository;
import cat.itacademy.battleship_api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtService jwtService;

    public AuthResponse register(AuthRequest request) {
        if (playerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("The username is already in use");
        }

        Player newPlayer = Player.builder().username(request.getUsername()).password(passwordEncoder.encode(request.getPassword())).build();

        playerRepository.save(newPlayer);


        String jwtToken = jwtService.generateToken(newPlayer.getUsername());

        return new AuthResponse(jwtToken, "User successfully registered");
    }

    public AuthResponse login(AuthRequest request) {

        Player player = playerRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));


        if (passwordEncoder.matches(request.getPassword(), player.getPassword())) {


            String jwtToken = jwtService.generateToken(player.getUsername());
            return new AuthResponse(jwtToken, "Successful login");

        } else {
            throw new RuntimeException("Incorrect credentials");
        }
    }
}