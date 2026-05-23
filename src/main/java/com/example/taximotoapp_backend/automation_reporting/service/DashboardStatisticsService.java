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
}