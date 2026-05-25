package com.example.taximotoapp_backend.User.service;

import com.example.taximotoapp_backend.User.dto.RegistrationStatDTO;
import com.example.taximotoapp_backend.Admin.model.Admin;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.Admin.repository.AdminRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.ActivityStatus;
import com.example.taximotoapp_backend.model.enumClass.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        // 1. Chercher dans UserRepository (qui contient Clients, Chauffeurs et maintenant Admins)
        var userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    true, // enabled (allow login for unverified accounts so they land on pending screen)
                    true,
                    true,
                    true,
                    List.of(new SimpleGrantedAuthority(user.getRole().name()))
            );
        }

        // 2. Fallback sur AdminRepository (au cas où ils ne seraient pas dans UserRepository)
        var adminOpt = adminRepository.findByEmail(identifier);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        throw new UsernameNotFoundException("Utilisateur non trouvé : " + identifier);
    }


    public Page<User> getAllUsers(int page, int size) {
        List<User> filtered = userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ROLE_ADMIN)
                .toList();

        PageRequest pageRequest = PageRequest.of(page, size);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filtered.size());
        List<User> subList = start <= end ? filtered.subList(start, end) : List.of();

        return new org.springframework.data.domain.PageImpl<>(subList, pageRequest, filtered.size());
    }

    public Page<User> searchByName(String name, int page, int size) {
        return userRepository.findByFullNameContainingIgnoreCase(
                name,
                PageRequest.of(page, size)
        );
    }

    public User searchByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public Page<User> getUsersByRole(com.example.taximotoapp_backend.model.enumClass.Role role, int page, int size) {
        return userRepository.findByRole(
                role,
                PageRequest.of(page, size)
        );
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Page<User> filterUsers(Role role, String status, String gender, Boolean verified, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // On récupère tout et on filtre en mémoire pour l'instant (plus simple sans Specifications)
        // Note: En production, il faudrait utiliser JPA Specification pour la performance
        List<User> allUsers = userRepository.findAll();

        List<User> filtered = allUsers.stream()
                .filter(u -> u.getRole() != Role.ROLE_ADMIN)
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> status == null || (u.getActivityStatus() != null && u.getActivityStatus().name().equals(status)))
                .filter(u -> gender == null || (u.getGender() != null && u.getGender().name().equals(gender)))
                .filter(u -> verified == null || u.getIsVerified().equals(verified))
                .toList();

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filtered.size());

        List<User> subList = start <= end ? filtered.subList(start, end) : List.of();

        return new org.springframework.data.domain.PageImpl<>(subList, pageRequest, filtered.size());
    }

    public java.util.Map<String, Long> getUserStats() {
        List<User> allUsers = userRepository.findAll();
        long total = allUsers.size();
        long clients = allUsers.stream().filter(u -> u.getRole() == Role.ROLE_CLIENT).count();
        long chauffeurs = allUsers.stream().filter(u -> u.getRole() == Role.ROLE_CHAUFFEUR).count();

        long online = allUsers.stream()
                .filter(u -> u.getActivityStatus() != null && u.getActivityStatus().name().equals("ONLINE"))
                .count();

        long clientsOnline = allUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_CLIENT && u.getActivityStatus() != null && u.getActivityStatus().name().equals("ONLINE"))
                .count();

        long clientsUnverified = allUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_CLIENT && !u.getIsVerified())
                .count();

        long chauffeursOnline = allUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_CHAUFFEUR && u.getActivityStatus() != null && u.getActivityStatus().name().equals("ONLINE"))
                .count();

        long chauffeursUnverified = allUsers.stream()
                .filter(u -> u.getRole() == Role.ROLE_CHAUFFEUR && !u.getIsVerified())
                .count();

        long unverified = allUsers.stream()
                .filter(u -> !u.getIsVerified())
                .count();

        return java.util.Map.of(
                "total", total,
                "clients", clients,
                "chauffeurs", chauffeurs,
                "online", online,
                "clientsOnline", clientsOnline,
                "clientsUnverified", clientsUnverified,
                "chauffeursOnline", chauffeursOnline,
                "chauffeursUnverified", chauffeursUnverified,
                "unverified", unverified
        );
    }
    public List<RegistrationStatDTO> getRegistrationStats(Role role) {
        List<Object[]> results = userRepository.countRegistrationsByDate(role);
        return results.stream()
                .map(result -> new RegistrationStatDTO(result[0].toString(), (Long) result[1]))
                .toList();
    }
    public List<RegistrationStatDTO> getGenderStats(Role role) {
        List<Object[]> results = userRepository.countByRoleAndGender(role);
        return results.stream()
                .map(result -> new RegistrationStatDTO(result[0] != null ? result[0].toString() : "NON_SPECIFIE", (Long) result[1]))
                .toList();
    }
    public User updateUserAdmin(Long id, String fullName, String email, String encodedPassword) {
        User user = getUserById(id);
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }
        if (encodedPassword != null && !encodedPassword.isEmpty()) {
            user.setPassword(encodedPassword);
        }
        return userRepository.save(user);
    }

    public Map<String, String> updateActivityStatus(Map<String, String> payload){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String statusStr = payload.get("status");
        if (statusStr != null) {
            try {
                ActivityStatus status = ActivityStatus.valueOf(statusStr.toUpperCase());
                user.setActivityStatus(status);
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                return Map.of("error", "Invalid status. Must be ONLINE or OFFLINE");
            }
        }
        return Map.of("status", user.getActivityStatus().name());
    }
    public Map<String, Object> getProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id", user.getId());
        resp.put("fullName", user.getFullName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole() != null ? user.getRole().name() : null);
        resp.put("gender", user.getGender() != null ? user.getGender().name() : null);
        resp.put("isVerified", user.getIsVerified());
        resp.put("photoUrl", (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur)
                ? ((com.example.taximotoapp_backend.User.model.Chauffeur) user).getPhotoUrl()
                : user.getPhotoBase64());
        return resp;
    }
    public Map<String, Object> updateProfile(Map<String, String> payload){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String photoBase64 = payload.get("photoBase64");
        if (photoBase64 != null) {
            user.setPhotoBase64(photoBase64);
            if (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur) {
                ((com.example.taximotoapp_backend.User.model.Chauffeur) user).setPhotoUrl(null);
            }
        }
        userRepository.save(user);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id", user.getId());
        resp.put("fullName", user.getFullName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole() != null ? user.getRole().name() : null);
        resp.put("gender", user.getGender() != null ? user.getGender().name() : null);
        resp.put("isVerified", user.getIsVerified());
        resp.put("photoUrl", (user instanceof com.example.taximotoapp_backend.User.model.Chauffeur)
                ? ((com.example.taximotoapp_backend.User.model.Chauffeur) user).getPhotoUrl()
                : user.getPhotoBase64());
        return resp;
    }
}
