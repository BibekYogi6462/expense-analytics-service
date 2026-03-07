package com.bibek.financeTracker.controller;

import com.bibek.financeTracker.dto.AuthDto;
import com.bibek.financeTracker.dto.ProfileDto;
import com.bibek.financeTracker.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDto> registerProfile(@RequestBody ProfileDto profileDto) {
        ProfileDto registeredProfile = profileService.registerProfile(profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }


    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token){
        boolean isActivated = profileService.activateProfile(token);
        if(isActivated){
            return  ResponseEntity.status(HttpStatus.OK).body("Activated");
        }else{
            return   ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation token");
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody AuthDto authDto){
        try {
            if (!profileService.isAccountActive(authDto.getEmail())) {  // ← Only change is adding '!' here
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("error", "Account is not activated. Please check your email for the activation link.")
                );
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDto);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", e.getMessage())
            );
        }
    }





}
