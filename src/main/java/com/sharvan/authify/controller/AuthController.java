package com.sharvan.authify.controller;

import com.sharvan.authify.io.AuthRequest;
import com.sharvan.authify.io.AuthResponse;
import com.sharvan.authify.service.ProfileServiceImpl;
import com.sharvan.authify.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileServiceImpl profileService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticate(request.getEmail(), request.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            final String jwtToken = jwtUtil.generateToken(userDetails);
            ResponseCookie cookie = ResponseCookie.from("jwt",jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie.toString())
                    .body(new AuthResponse(request.getEmail(),jwtToken));
        } catch (BadCredentialsException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", true);
            map.put("message", "Email or password is incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        } catch (DisabledException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", true);
            map.put("message", "Account is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
        }catch (ResponseStatusException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", true);
            map.put("message", exception.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
        } catch (Exception exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("error", true);
            map.put("message", "Authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
        }
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(@CurrentSecurityContext(expression = "authentication?.name") String email){
        return ResponseEntity.ok(email!=null);
    }

    @PostMapping("/send-reset-otp")
    public ResponseEntity<Void> sendResetOtp(@RequestParam String email){
        profileService.sendResetOtp(email);
        return ResponseEntity.ok().build();
    }
}
