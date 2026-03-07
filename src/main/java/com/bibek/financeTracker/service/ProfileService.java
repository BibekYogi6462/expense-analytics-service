package com.bibek.financeTracker.service;

import com.bibek.financeTracker.dto.AuthDto;
import com.bibek.financeTracker.dto.ProfileDto;
import com.bibek.financeTracker.entity.ProfileEntity;
import com.bibek.financeTracker.repository.ProfileRepository;
import com.bibek.financeTracker.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationUrl;


    public ProfileDto registerProfile(ProfileDto profileDto) {

        ProfileEntity newProfile = toEntity(profileDto);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        String activationLink = "activationUrl/api/v1.0/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate Your Finance Tracker Account";
        String body = "Dear " + newProfile.getFullName() + ",\n\n" +
                "Thank you for registering with Finance Tracker! Please click the link below to activate your account:\n\n" +
                activationLink + "\n\n" +
                "If you did not register for this account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Finance Tracker Team";
        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);


    }


    public ProfileEntity toEntity(ProfileDto profileDto) {
        return ProfileEntity.builder()
                .id(profileDto.getId())
                .fullName(profileDto.getFullName())
                .email(profileDto.getEmail())
                .password(passwordEncoder.encode(profileDto.getPassword()))
                .profileImageUrl(profileDto.getProfileImageUrl())
                .createdAt(profileDto.getCreatedAt())
                .updatedAt(profileDto.getUpdatedAt())
                .build();
    }


    public ProfileDto toDTO(ProfileEntity profileEntity) {
        return ProfileDto.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }


    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "Profile not found with email " + authentication.getName()
                        )
                );
    }

    public ProfileDto getPublicProfile(String email){

        ProfileEntity currentUser = null;

        if(email == null){
            currentUser = getCurrentProfile();
        }else{
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(
                            () -> new UsernameNotFoundException(
                                    "Profile not found with email " + email
                            )
                    );
        }

        return ProfileDto.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }


    public Map<String, Object> authenticateAndGenerateToken(AuthDto authDto) {
        try{
            System.out.println("=== Login Attempt ===");
            System.out.println("Email: " + authDto.getEmail());
            System.out.println("Password provided: " + (authDto.getPassword() != null ? "YES" : "NO"));

            if(!isAccountActive(authDto.getEmail())){
                System.out.println("Account not active!");
                throw new RuntimeException("Account not activated. Please check your email.");
            }

            System.out.println("Account is active, attempting authentication...");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDto.getEmail(),
                            authDto.getPassword()
                    )
            );

            System.out.println("Authentication successful: " + authentication.isAuthenticated());

            String token = jwtUtil.generateToken(authDto.getEmail());
            System.out.println("Token generated successfully");

            return Map.of(
                    "token", token,  // Return actual token, not "JWT Token"
                    "user", getPublicProfile(authDto.getEmail())
            );

        }catch (Exception e){
            System.out.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Invalid email or password");
        }
    }

}
