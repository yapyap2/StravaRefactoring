package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserJDBCRepository {

    private final JdbcTemplate jdbcTemplate;


    public void updateUserWithLocation(User user, Set<String> location){
        try {
            List<String> list = location.stream().toList();

            if (user.isLocationComplete()) {
                String sql = "UPDATE USER SET location_complete = true where id = ?";
                jdbcTemplate.update(sql, user.getId());
            }

            String sql2 = "INSERT INTO LOCATION VALUES(?,?)";

            if (location.size() == 0) {
                return;
            }
            jdbcTemplate.batchUpdate(sql2, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    String loc = list.get(i);
                    ps.setInt(1, user.getId());
                    ps.setString(2, loc);
                }

                @Override
                public int getBatchSize() {
                    return location.size();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
    }
    }

}
