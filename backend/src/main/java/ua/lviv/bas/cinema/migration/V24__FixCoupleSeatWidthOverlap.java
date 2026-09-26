package ua.lviv.bas.cinema.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class V24__FixCoupleSeatWidthOverlap extends BaseJavaMigration {

    private static final int CELL_WIDTH = 60;

    private record ExistingSeat(long id, String seatType) {
    }

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        Map<Long, Map<Integer, List<ExistingSeat>>> seatsByHallAndRow = loadSeats(connection);

        try (PreparedStatement update = connection.prepareStatement("UPDATE seats SET x = ? WHERE id = ?")) {
            for (var hallRows : seatsByHallAndRow.values()) {
                for (var rowSeats : hallRows.values()) {
                    layoutRowWithoutOverlap(rowSeats, update);
                }
            }
            update.executeBatch();
        }
    }

    private Map<Long, Map<Integer, List<ExistingSeat>>> loadSeats(Connection connection) throws Exception {
        Map<Long, Map<Integer, List<ExistingSeat>>> result = new LinkedHashMap<>();
        String sql = "SELECT id, hall_id, seat_row, seat_type FROM seats ORDER BY hall_id, seat_row, number";
        try (PreparedStatement select = connection.prepareStatement(sql);
             ResultSet rs = select.executeQuery()) {
            while (rs.next()) {
                result.computeIfAbsent(rs.getLong("hall_id"), id -> new LinkedHashMap<>())
                        .computeIfAbsent(rs.getInt("seat_row"), row -> new ArrayList<>())
                        .add(new ExistingSeat(rs.getLong("id"), rs.getString("seat_type")));
            }
        }
        return result;
    }

    private void layoutRowWithoutOverlap(List<ExistingSeat> seats, PreparedStatement update) throws Exception {
        int x = 0;
        for (ExistingSeat seat : seats) {
            update.setInt(1, x);
            update.setLong(2, seat.id());
            update.addBatch();
            x += unitsFor(seat) * CELL_WIDTH;
        }
    }

    private int unitsFor(ExistingSeat seat) {
        return "COUPLE".equals(seat.seatType()) ? 2 : 1;
    }
}
