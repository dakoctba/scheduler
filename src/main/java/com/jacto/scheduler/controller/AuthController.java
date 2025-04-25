package com.jacto.scheduler.controller;

import com.jacto.scheduler.enumerations.Role;
import com.jacto.scheduler.model.User;
import com.jacto.scheduler.payload.request.LoginRequest;
import com.jacto.scheduler.payload.request.SignupRequest;
import com.jacto.scheduler.payload.response.JwtResponse;
import com.jacto.scheduler.payload.response.MessageResponse;
import com.jacto.scheduler.repository.UserRepository;
import com.jacto.scheduler.security.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    @Operation(
        summary = "Autenticar usuário",
        description = "Autentica um usuário e retorna um token JWT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Autenticado com sucesso",
                content = @Content(schema = @Schema(implementation = JwtResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Credenciais inválidas"
            )
        }
    )
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                                 userDetails.getId(),
                                 userDetails.getUsername(),
                                 userDetails.getFullName(),
                                 userDetails.getEmail(),
                                 roles));
    }

    @PostMapping("/signup")
    @Operation(
        summary = "Registrar usuário",
        description = "Registra um novo usuário no sistema",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Usuário registrado com sucesso",
                content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Usuário já existe ou dados inválidos"
            )
        }
    )
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Erro: Nome de usuário já está em uso!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setFullName(signUpRequest.getFullName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        String strRole = signUpRequest.getRole();
        Role role;

        if (strRole == null) {
            role = Role.TECNICO;
        } else {
            try {
                role = Role.valueOf(strRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                role = Role.TECNICO;
            }
        }

        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuário registrado com sucesso!"));
    }
}
