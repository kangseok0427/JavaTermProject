package termproject;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class MissionRepository {
    private final String url;
    private final String username;
    private final String password;

    public MissionRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public List<Mission> findAll(String search) throws SQLException {
        String sql = """
                SELECT mission_id, title, learning_objective, description, difficulty, pass_score
                FROM mission
                WHERE ? IS NULL OR INSTR(LOWER(title || ' ' || learning_objective), LOWER(?)) > 0
                ORDER BY mission_id
                """;
        List<Mission> missions = new ArrayList<>();
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, search);
            statement.setString(2, search);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    missions.add(new Mission(result.getLong("mission_id"), result.getString("title"),
                            result.getString("learning_objective"), result.getString("description"),
                            result.getInt("difficulty"), result.getBigDecimal("pass_score")));
                }
            }
        }
        return missions;
    }

    public long insert(Mission mission) throws SQLException {
        String sql = """
                INSERT INTO mission (title, learning_objective, description, difficulty, pass_score, mission_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connect();
             PreparedStatement sequence = connection.prepareStatement("SELECT ail_mission_seq.NEXTVAL FROM dual");
             ResultSet result = sequence.executeQuery()) {
            result.next();
            long id = result.getLong(1);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindFields(statement, mission);
                statement.setLong(6, id);
                statement.executeUpdate();
            }
            return id;
        }
    }

    public boolean update(Mission mission) throws SQLException {
        String sql = """
                UPDATE mission SET title = ?, learning_objective = ?, description = ?, difficulty = ?, pass_score = ?
                WHERE mission_id = ?
                """;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFields(statement, mission);
            statement.setLong(6, mission.id());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM mission WHERE mission_id = ?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private void bindFields(PreparedStatement statement, Mission mission) throws SQLException {
        statement.setString(1, mission.title());
        statement.setString(2, mission.objective());
        statement.setClob(3, new StringReader(mission.description()));
        statement.setInt(4, mission.difficulty());
        statement.setBigDecimal(5, mission.passScore());
    }
}
