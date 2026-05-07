package com.example.taximotoapp_backend.User.service;

import com.example.taximotoapp_backend.User.dto.RegistrationStatDTO;
import com.example.taximotoapp_backend.User.model.Admin;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.AdminRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        // 1. Chercher dans User (Client/Chauffeur)
        var userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority(user.getRole().toString()))
            );
        }

        // 2. Chercher dans Admin
        var adminOpt = adminRepository.findByUsername(identifier);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        throw new UsernameNotFoundException("Utilisateur non trouvé : " + identifier);
    }


    public Page<User> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
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

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public void deleteUser(Integer id) {
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

        long total = userRepository.count();
        long clients = userRepository.findByRole(com.example.taximotoapp_backend.model.enumClass.Role.ROLE_CLIENT).size();
        long chauffeurs = userRepository.findByRole(com.example.taximotoapp_backend.model.enumClass.Role.ROLE_CHAUFFEUR).size();

        // On pourrait optimiser avec des requêtes personnalisées dans le repo
        long online = userRepository.findAll().stream()
                .filter(u -> "ONLINE".equals(u.getActivityStatus() != null ? u.getActivityStatus().name() : ""))
                .count();

        long unverified = userRepository.findAll().stream()
                .filter(u -> !u.getIsVerified())
                .count();

        return java.util.Map.of(
                "total", total,
                "clients", clients,
                "chauffeurs", chauffeurs,
                "online", online,
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
}
