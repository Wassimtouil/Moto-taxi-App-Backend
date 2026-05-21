package com.example.taximotoapp_backend.Admin.controller;

import com.example.taximotoapp_backend.User.dto.RegistrationStatDTO;
import com.example.taximotoapp_backend.User.dto.UserDTO;
import com.example.taximotoapp_backend.User.dto.UserDTOAdmin;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.service.UserService;
import com.example.taximotoapp_backend.model.enumClass.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // 1. Liste des utilisateurs avec pagination
    @GetMapping
    public Page<UserDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getAllUsers(page, size).map(UserDTO::new);
    }

    // 2. Recherche par nom
    @GetMapping("/search/name")
    public Page<UserDTO> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.searchByName(name, page, size).map(UserDTO::new);
    }

    // 3. Recherche par email
    @GetMapping("/search/email")
    public UserDTO searchByEmail(@RequestParam String email) {
        User user = userService.searchByEmail(email);
        return user != null ? new UserDTO(user) : null;
    }

    // 4. Filtre par rôle
    @GetMapping("/role/{role}")
    public Page<UserDTO> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Role roleEnum = Role.valueOf(role.toUpperCase());
        return userService.getUsersByRole(roleEnum, page, size).map(UserDTO::new);
    }

    // 4.1 Filtre multicritère
    @GetMapping("/filter")
    public Page<UserDTO> filterUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Role roleEnum = (role != null && !role.isEmpty()) ? Role.valueOf(role.toUpperCase()) : null;
        return userService.filterUsers(roleEnum, status, gender, verified, page, size).map(UserDTO::new);
    }


    // 5. Détails utilisateur
    @GetMapping("/{id}")
    public UserDTOAdmin getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? new UserDTOAdmin(user) : null;
    }

    // 6. Basculer la vérification
    @PatchMapping("/{id}/verify")
    public UserDTO toggleVerification(@PathVariable Long id) {
        User user = userService.getUserById(id);
        user.setIsVerified(!user.getIsVerified());
        return new UserDTO(userService.saveUser(user));
    }

    @GetMapping("/stats")
    public java.util.Map<String, Long> getUserStats() {
        return userService.getUserStats();
    }

    // 7. Suppression utilisateur
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "Utilisateur supprimé avec succès";
    }
    @GetMapping("/stats/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RegistrationStatDTO> getRegistrationStats(@RequestParam String role) {
        return userService.getRegistrationStats(Role.valueOf(role.toUpperCase()));
    }
    @GetMapping("/stats/gender")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RegistrationStatDTO> getGenderStats(@RequestParam String role) {
        return userService.getGenderStats(Role.valueOf(role.toUpperCase()));
    }
}
