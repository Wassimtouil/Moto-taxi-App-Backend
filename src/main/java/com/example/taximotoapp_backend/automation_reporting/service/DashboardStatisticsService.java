package com.example.taximotoapp_backend.automation_reporting.service;

import com.example.taximotoapp_backend.automation_reporting.dto.PaymentStatsDTO;
import com.example.taximotoapp_backend.automation_reporting.dto.TopDriverDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    public Double getTodayRevenue() {

        String sql = """
                SELECT COALESCE(SUM(price),0)
                FROM trajet
                WHERE status='Completed'
                AND DATE(completed_at)=CURDATE()
                """;

        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Long getTodayTrips() {
        String sql = """
                SELECT COUNT(*)
                FROM trajet
                WHERE DATE(requested_at)=CURDATE()
                """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getInactiveDrivers() {
        String sql = """
                SELECT COUNT(*)
                FROM chauffeur c
                LEFT JOIN trajet t
                    ON c.user_id=t.chauffeur_id
                    AND DATE(t.requested_at)=CURDATE()
                WHERE t.id IS NULL
                """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getNewClients() {

        String sql = """
                SELECT COUNT(*)
                FROM user
                WHERE role='ROLE_CLIENT'
                AND DATE(created_at)=CURDATE()
                """;

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Double getCancellationRate() {

        String sql = """
                SELECT
                (SUM(CASE WHEN status='Canceled' THEN 1 ELSE 0 END)*100.0)
                / COUNT(*)
                FROM trajet
                WHERE DATE(requested_at)=CURDATE()
                """;

        Double value = jdbcTemplate.queryForObject(sql, Double.class);

        return value == null ? 0.0 : value;
    }

    public PaymentStatsDTO getPaymentStats() {

        String cashSql = """
                SELECT COUNT(*)
                FROM trajet
                WHERE payment_method='CASH'
                AND DATE(requested_at)=CURDATE()
                """;

        String onlineSql = """
                SELECT COUNT(*)
                FROM trajet
                WHERE payment_method='ONLINE'
                AND DATE(requested_at)=CURDATE()
                """;

        Long cash = jdbcTemplate.queryForObject(cashSql, Long.class);
        Long online = jdbcTemplate.queryForObject(onlineSql, Long.class);

        return new PaymentStatsDTO(cash, online);
    }

    public List<TopDriverDTO> getTopDrivers() {

        String sql = """
                SELECT
                    u.full_name,
                    SUM(t.price) as revenue,
                    COUNT(t.id) as trips
                FROM trajet t
                JOIN chauffeur c ON t.chauffeur_id=c.user_id
                JOIN user u ON c.user_id=u.id
                WHERE t.status='Completed'
                AND DATE(t.completed_at)=CURDATE()
                GROUP BY u.full_name
                ORDER BY revenue DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new TopDriverDTO(
                        rs.getString("full_name"),
                        rs.getDouble("revenue"),
                        rs.getLong("trips")
                )
        );
    }

    public List<Map<String, Object>> getTodayReclamations() {
        String sql = """
                SELECT
                    objet,
                    message,
                    reclamation_status
                FROM reclamation
                WHERE date_reclamation=CURDATE()
                LIMIT 20
                """;
        return jdbcTemplate.queryForList(sql);
    }

    // Méthodes présentes dans le HTML mais absentes du service

    public Long getCompletedTrips() {
        String sql = """
            SELECT COUNT(*) FROM trajet
            WHERE status='Completed'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getCanceledTrips() {
        String sql = """
            SELECT COUNT(*) FROM trajet
            WHERE status='Canceled'
            AND DATE(requested_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getActiveDrivers() {
        String sql = """
            SELECT COUNT(DISTINCT chauffeur_id) FROM trajet
            WHERE DATE(requested_at)=CURDATE()
            AND chauffeur_id IS NOT NULL
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getActiveClients() {
        String sql = """
            SELECT COUNT(DISTINCT client_id) FROM trajet
            WHERE DATE(requested_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Double getAvgTripPrice() {
        String sql = """
            SELECT COALESCE(AVG(price), 0) FROM trajet
            WHERE status='Completed'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Double getAvgDistance() {
        String sql = """
            SELECT COALESCE(AVG(distance_km), 0) FROM trajet
            WHERE status='Completed'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Double getTotalDistance() {
        String sql = """
            SELECT COALESCE(SUM(distance_km), 0) FROM trajet
            WHERE status='Completed'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Double getAvgDuration() {
        String sql = """
            SELECT COALESCE(AVG(duration_minutes), 0) FROM trajet
            WHERE status='Completed'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Double getCashRevenue() {
        String sql = """
            SELECT COALESCE(SUM(price), 0) FROM trajet
            WHERE status='Completed'
            AND payment_method='CASH'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Double getOnlineRevenue() {
        String sql = """
            SELECT COALESCE(SUM(price), 0) FROM trajet
            WHERE status='Completed'
            AND payment_method='ONLINE'
            AND DATE(completed_at)=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public List<Map<String, Object>> getPeakHours() {
        String sql = """
            SELECT HOUR(requested_at) as hour, COUNT(*) as trips
            FROM trajet
            WHERE DATE(requested_at)=CURDATE()
            GROUP BY HOUR(requested_at)
            ORDER BY trips DESC
            LIMIT 5
            """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTopZones() {
        String sql = """
            SELECT tl.pickup_address, COUNT(*) as total
            FROM trajet t
            JOIN trajet_location tl ON t.id=tl.trajet_id
            WHERE DATE(t.requested_at)=CURDATE()
            AND tl.pickup_address IS NOT NULL
            GROUP BY tl.pickup_address
            ORDER BY total DESC
            LIMIT 5
            """;
        return jdbcTemplate.queryForList(sql);
    }

    public Long getBlockedUsers() {
        // Basé sur is_verified=false ou une logique métier spécifique
        String sql = """
            SELECT COUNT(*) FROM user
            WHERE is_verified=0
            AND role != 'ROLE_ADMIN'
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getPendingDrivers() {
        String sql = """
            SELECT COUNT(*) FROM chauffeur c
            JOIN user u ON c.user_id=u.id
            WHERE u.is_verified=0
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getReclamationCount() {
        String sql = """
            SELECT COUNT(*) FROM reclamation
            WHERE date_reclamation=CURDATE()
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Double getYesterdayRevenue() {
        String sql = """
            SELECT COALESCE(SUM(price), 0) FROM trajet
            WHERE status='Completed'
            AND DATE(completed_at)=DATE_SUB(CURDATE(),INTERVAL 1 DAY)
            """;
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public Long getYesterdayTrips() {
        String sql = """
            SELECT COUNT(*) FROM trajet
            WHERE DATE(requested_at)=DATE_SUB(CURDATE(),INTERVAL 1 DAY)
            """;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}