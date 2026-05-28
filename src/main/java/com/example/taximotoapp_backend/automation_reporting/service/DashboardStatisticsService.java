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
}