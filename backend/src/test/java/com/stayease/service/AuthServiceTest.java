package com.stayease.service;

import com.stayease.dto.AuthDtos.*;
import com.stayease.entity.User;
import com.stayease.repository.UserRepository;
import com.stayease.security.JwtService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.stayease.exception.ApiException;
import org.springframework.http.HttpStatus;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class AuthServiceTest {
    @Mock
    UserRepository users;
    JwtService jwt;
    AuthService service;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void set() {
        MockitoAnnotations.openMocks(this);
        jwt = mock(JwtService.class);
        service = new AuthService(users, encoder, jwt);
    }

    @Test
    void registerDefaultsGuestAndHashes() {
        when(users.existsByEmailIgnoreCase("a@b.com")).thenReturn(false);
        service.register(new RegisterRequest("a@b.com", "secret1", "A"));
        ArgumentCaptor<User> c = ArgumentCaptor.forClass(User.class);
        verify(users).save(c.capture());
        assertEquals("GUEST", c.getValue().getRole());
        assertTrue(encoder.matches("secret1", c.getValue().getPasswordHash()));
    }

    @Test
    void duplicateEmailRejected() {
        when(users.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.register(new RegisterRequest("a@b.com", "secret1", "A")));
    }


    @Test
    void loginRejectsUnknownEmail() {
        when(users.findByEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.login(
                        new LoginRequest("missing@example.com", "secret1")
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("INVALID_CREDENTIALS", ex.getError());
        verify(jwt, never()).generate(any(User.class));
    }

    @Test
    void loginRejectsIncorrectPassword() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setPasswordHash(encoder.encode("correct-password"));

        when(users.findByEmailIgnoreCase("a@b.com"))
                .thenReturn(Optional.of(user));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.login(
                        new LoginRequest("a@b.com", "wrong-password")
                )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("INVALID_CREDENTIALS", ex.getError());
        verify(jwt, never()).generate(any(User.class));
    }

}
