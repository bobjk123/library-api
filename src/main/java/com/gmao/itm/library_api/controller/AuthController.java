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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT token issuance")
@SecurityRequirements
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Authenticate user and issue JWT",
            description = "Validates username and password credentials and returns a signed Bearer JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIn0.signature",
                                              "tokenType": "Bearer",
                                              "expiresInMs": 86400000,
                                              "username": "admin",
                                              "role": "ROLE_ADMIN"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "admin",
                                              "password": "LibraryPass123!"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest request
    ) {
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

    @Operation(
            summary = "Register a new API user",
            description = "Creates a new user account with a BCrypt-encoded password and returns a JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or username already taken")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "librarian_01",
                                              "password": "LibraryPass123!",
                                              "role": "ROLE_USER"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody RegisterRequest request
    ) {
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