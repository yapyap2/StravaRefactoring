package com.example.stravarefactoring.Repository;

import com.example.stravarefactoring.domain.Ride;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RideBatchRepository {

    @PostConstruct
    private void init(){
        Long l = jdbcTemplate.queryForObject("SELECT MAX(id) FROM RIDE", Long.class);
        if(l == null) id = 0L;
        else id = l;
    }


    private final JdbcTemplate jdbcTemplate;

    Long id;

    @Transactional
    public List<Ride> saveAll(List<Ride> rideList) {
        String sql = "insert into" +
                " ride" +
                "(average_speed, average_watts, comment_count, distance, kudos_count, moving_time, name, ride_id, ride_type, start_date_local, summary_polyline, total_watts, total_elevation_gain, total_photo_count, mapped,user_id) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Ride ride = rideList.get(i);
                        id+=1;
                        ride.setId(id);
                        ps.setDouble(1, ride.getAverage_speed());
                        ps.setDouble(2, ride.getAverage_watts());
                        ps.setInt(3, ride.getComment_count());
                        ps.setDouble(4, ride.getDistance());
                        ps.setInt(5, ride.getKudos_count());
                        ps.setInt(6, ride.getMoving_time());
                        ps.setString(7, ride.getName());
                        ps.setInt(8, ride.getRideId());
                        ps.setString(9, ride.getRideType());
                        ps.setTimestamp(10, Timestamp.valueOf(ride.getStart_date_local()));
                        ps.setString(11, ride.getSummary_polyline());
                        ps.setDouble(12, ride.getTotalWatts());
                        ps.setDouble(13, ride.getTotal_elevation_gain());
                        ps.setInt(14, ride.getTotal_photo_count());
                        ps.setBoolean(15, ride.isMapped());
                        ps.setInt(16, ride.getUser().getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return rideList.size();
                    }
                });

        return rideList;
    }


    @Transactional
    public void batchUpdateRides(List<Ride> rides) {
        String sql = "UPDATE ride SET mapped = ? WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                Ride ride = rides.get(i);
                preparedStatement.setBoolean(1, true);
                preparedStatement.setLong(2, ride.getId());
            }

            @Override
            public int getBatchSize() {
                return rides.size();
            }
        });
    }


}