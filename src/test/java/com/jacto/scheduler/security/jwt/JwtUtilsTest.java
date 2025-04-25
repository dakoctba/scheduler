package com.jacto.scheduler.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    private Authentication authentication;
    private UserDetails userDetails;
    private String jwtSecret;
    private int jwtExpirationMs;

    @BeforeEach
    void setUp() {
        jwtSecret = "57ffd86ce5c53fccdfc26bd1153e825dcbda4f76d1587692689dd8b1e2b33285";
        jwtExpirationMs = 86400000;

        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);

        userDetails = new User("testuser", "password", java.util.Collections.emptyList());
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void generateJwtToken_ShouldGenerateValidToken() {
        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void getUserNameFromJwtToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void getUserNameFromJwtToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () ->
            jwtUtils.getUserNameFromJwtToken(invalidToken)
        );
    }
}
