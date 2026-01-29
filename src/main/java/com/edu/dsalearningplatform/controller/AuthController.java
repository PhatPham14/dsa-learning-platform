package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.dto.request.LoginRequest;
import com.edu.dsalearningplatform.dto.request.RefreshRequest;
import com.edu.dsalearningplatform.dto.request.RegisterRequest;
import com.edu.dsalearningplatform.dto.response.LoginSuccessResponse;
import com.edu.dsalearningplatform.enums.UserRole;
import com.edu.dsalearningplatform.security.jwt.JwtUtils;
import com.edu.dsalearningplatform.security.services.UserDetailsImpl;
import com.edu.dsalearningplatform.security.services.UserDetailsServiceImpl;
import com.edu.dsalearningplatform.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "AuthController", description = "Quản lí tài khoản")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

//    @PostMapping("/register-staff")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> registerStaff(@RequestBody @Valid RegisterStaffRequest request) {
//        int number = request.getNumbers();
//        while(number>0){
//            UserRole role = UserRole.valueOf(request.getRole());
//            userService.registerUser(
//                    request.getFullName(),
//                    String.valueOf(number)+ request.getEmail(),
//                    request.getPhone(),
//                    request.getPassword(),
//                    role
//            );
//        }
//
//        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công!"));
//    }
    @GetMapping("/check-auth")
    public String checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Quyền thực tế: " + authentication.getAuthorities());
        return "Quyền thực tế: " + authentication.getAuthorities();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        UserRole role = UserRole.valueOf("CUSTOMER");
        userService.registerUser(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),

                role
        );
        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmailOrPhone(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String accessToken = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);

            return ResponseEntity.ok(new LoginSuccessResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getPhone(),
                    userDetails.getFullName(),
                    userDetails.getAuthorities().iterator().next().getAuthority()
            ));
        } catch (BadCredentialsException ex) {
            // Trả về JSON lỗi
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Thông tin đăng nhập không đúng!"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        if (jwtUtils.validateJwtToken(refreshToken)) {
            Integer userId = jwtUtils.getUserIdFromJwtToken(refreshToken);
            // Lấy UserDetails từ username
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsServiceImpl.loadUserById(userId);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            String newAccessToken = jwtUtils.generateJwtToken(authentication);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "type", "Bearer"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }
    }
}

