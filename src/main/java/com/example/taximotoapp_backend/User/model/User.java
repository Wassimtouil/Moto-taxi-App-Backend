package com.example.taximotoapp_backend.User.model;

import com.example.taximotoapp_backend.model.Reclamation;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;
import com.example.taximotoapp_backend.model.enumClass.Gender;
import com.example.taximotoapp_backend.model.enumClass.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid; // UID Firebase
    @Column(name = "full_name")
    private String fullName;
    @Enumerated(EnumType.STRING)
    private Role role; // CLIENT, CHAUFFEUR

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "is_verified",nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    private ActivityStatus activityStatus;

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    private List<Reclamation> reclamations;

    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt ;
    @PrePersist
    protected void onCreate (){
        createdAt=LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public String getFullName() {
        return fullName;
    }
    public Role getRole() {
        return role;
    }
    public Gender getGender() {
        return gender;
    }

    public ActivityStatus getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setIsVerified(Boolean verified) {
        isVerified = verified;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isVerified;
    }
}

