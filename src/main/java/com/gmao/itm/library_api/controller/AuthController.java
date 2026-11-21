package com.gmao.itm.library_api.controller;

import com.gmao.itm.library_api.dto.AuthResponse;
import com.gmao.itm.library_api.dto.LoginRequest;
import com.gmao.itm.library_api.dto.RegisterRequest;
import com.gmao.itm.library_api.exception.BusinessRuleException;
import com.gmao.itm.library_api.model.Role;
import com.gmao.itm.library_api.model.User;
import com.gmao.itm.library_api.repository.UserRepository;
import com.gmao.itm.library_api.security.CustomUserDetailsService;
import com.gmao.itm.library_api.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse(Role.ROLE_USER.name());

        return ResponseEntity.ok(new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                userDetails.getUsername(),
                role
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String normalizedUsername = request.username().trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new BusinessRuleException("Username '" + normalizedUsername + "' is already taken");
        }

        Role assignedRole = request.role() != null ? request.role() : Role.ROLE_USER;

        User user = User.builder()
                .username(normalizedUsername)
                .password(passwordEncoder.encode(request.password()))
                .role(assignedRole)
                .enabled(true)
                .build();

        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(normalizedUsername);
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                userDetails.getUsername(),
                assignedRole.name()
        ));
    }
}