package com.example.taximotoapp_backend.Admin.service;

import com.example.taximotoapp_backend.Admin.dto.AdminDashboardSummaryDto;
import com.example.taximotoapp_backend.Admin.dto.UserLocationDto;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.Role;
import com.example.taximotoapp_backend.paiement.repository.PaiementRepository;
import com.example.taximotoapp_backend.trajet.mapper.TrajetMapper;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final UserRepository userRepository;
    private final TrajetRepository trajetRepository;
    private final TrajetMapper trajetMapper;

    public AdminDashboardSummaryDto getDashboardSummary() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long totalUsers = userRepository.count();
        long totalChauffeurs = userRepository.findByRole(Role.ROLE_CHAUFFEUR).size();
        long totalClients = userRepository.findByRole(Role.ROLE_CLIENT).size();

        long totalTrips = trajetRepository.count();
        long tripsToday = trajetRepository.countByRequestedAtAfter(startOfDay);

        Double totalRevenue = trajetRepository.sumTotalRevenue();
        Double revenueToday = trajetRepository.sumRevenueSince(startOfDay);
        // Fetch recent trips (last 10)
        List<com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse> recentTrajets =
                trajetRepository.findRecentTrajets(PageRequest.of(0, 10))
                        .stream()
                        .map(trajetMapper::toDTO)
                        .collect(Collectors.toList());

        // Trips activity for the last 7 days
        List<Map<String, Object>> tripsActivity = new ArrayList<>();
        List<Object[]> tripCounts = trajetRepository.countTripsByDate(LocalDateTime.now().minusDays(7));
        for (Object[] obj : tripCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", obj[0].toString());
            map.put("count", obj[1]);
            tripsActivity.add(map);
        }

        // Registration activity (all roles combined)
        List<Map<String, Object>> registrationActivity = new ArrayList<>();
        // We'll just use a simplified version here for the dashboard
        List<Object[]> regCounts = userRepository.countRegistrationsByDate(Role.ROLE_CLIENT); // Just as example
        for (Object[] obj : regCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", obj[0].toString());
            map.put("count", obj[1]);
            registrationActivity.add(map);
        }

        // Trips by Month (Last 12 months)
        List<Map<String, Object>> tripsByMonth = new ArrayList<>();
        List<Object[]> monthCounts = trajetRepository.countTripsByMonth(LocalDateTime.now().minusMonths(12));
        for (Object[] obj : monthCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("month", obj[0]);
            map.put("count", obj[1]);
            tripsByMonth.add(map);
        }

        // Peak Hours
        List<Map<String, Object>> peakHours = new ArrayList<>();
        List<Object[]> hourCounts = trajetRepository.countTripsByHour();
        for (Object[] obj : hourCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("hour", obj[0] + "h");
            map.put("count", obj[1]);
            peakHours.add(map);
        }

        // Peak Days
        List<Map<String, Object>> peakDays = new ArrayList<>();
        List<Object[]> dayCounts = trajetRepository.countTripsByDayOfWeek();
        for (Object[] obj : dayCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("day", obj[0]);
            map.put("count", obj[1]);
            peakDays.add(map);
        }

        // Top Pickup Zones (for heatmap)
        List<Map<String, Object>> topZones = new ArrayList<>();
        List<Object[]> zoneCounts = trajetRepository.findTopPickupZones();
        for (Object[] obj : zoneCounts) {
            Map<String, Object> map = new HashMap<>();
            map.put("zone", obj[0]);
            map.put("lat", ((Number) obj[1]).doubleValue());
            map.put("lng", ((Number) obj[2]).doubleValue());
            map.put("count", ((Number) obj[3]).longValue());
            topZones.add(map);
        }
        // Age group stats
        List<Map<String, Object>> ageGroupStats = new ArrayList<>();
        List<Object[]> ageCounts = userRepository.countUsersByAgeGroups();
        if (!ageCounts.isEmpty() && ageCounts.get(0) != null) {
            Object[] counts = ageCounts.get(0);
            String[] labels = {"Jeunes(16-25)", "Jeunes adultes(26-35)", "Adultes(36-45)", "Seniors actifs(46-60)"};
            String[] ranges = {"16-25 ans", "26-35 ans", "36-45 ans", "46-60 ans"};
            for (int i = 0; i < labels.length; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("label", labels[i]);
                map.put("range", ranges[i]);
                map.put("count", counts[i] != null ? counts[i] : 0);
                ageGroupStats.add(map);
            }
        }

        return AdminDashboardSummaryDto.builder()
                .totalUsers(totalUsers)
                .totalChauffeurs(totalChauffeurs)
                .totalClients(totalClients)
                .totalTrips(totalTrips)
                .tripsToday(tripsToday)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .revenueToday(revenueToday != null ? revenueToday : 0.0)
                .recentTrajets(recentTrajets)
                .tripsActivity(tripsActivity)
                .registrationActivity(registrationActivity)
                .tripsByMonth(tripsByMonth)
                .peakHours(peakHours)
                .peakDays(peakDays)
                .topZones(topZones)
                .ageGroupStats(ageGroupStats)
                .build();
    }

    public List<UserLocationDto> getUserLocations() {
        return userRepository.findOnlineUsersWithLocation().stream()
                .map(user -> UserLocationDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .latitude(user.getLocation().getLatitude())
                        .longitude(user.getLocation().getLongitude())
                        .build())
                .collect(Collectors.toList());
    }
}
