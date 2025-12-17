package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private static final String SERVER_NAME = "localhost";
    private static final String DB_NAME = "StarGuardianDB";
    private static final String PORT = "1433";
    private static final String SQL_USER = "sa";
    private static final String SQL_PASSWORD = "sapassword";

    private static final String URL =
            String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;user=%s;password=%s;",
                    SERVER_NAME, PORT, DB_NAME, SQL_USER, SQL_PASSWORD);

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối CSDL: " + e.getMessage(), e);
        }
    }
}