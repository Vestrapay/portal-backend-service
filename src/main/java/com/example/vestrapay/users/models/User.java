package com.example.vestrapay.users.models;

import com.example.vestrapay.users.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table("app_user")
@Builder
public class User implements UserDetails {
    @Id
    @Column("id")
    private Long id;
    private String uuid;
    private String country;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    private String email;
    @Column("phone_number")
    private String phoneNumber;
    @Column("business_name")
    private String businessName;
    @Column("merchant_id")
    private String merchantId;
    @Column("referral_code")
    private String referralCode;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    @Column("user_type")
    private UserType userType;
    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private boolean enabled;
    @JsonIgnore
    private String otp;
    @Column("kyc_completed")
    private boolean kycCompleted;
    @Column("required_documents")
    private String requiredDocuments;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userType.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
