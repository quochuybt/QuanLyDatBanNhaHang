package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    // --- ( các hằng số SERVER_NAME, DB_NAME, PORT, USER, PASS, URL...) ---
    private static final String SERVER_NAME = "localhost";
    private static final String DB_NAME = "StarGuardianDB";
    private static final String PORT = "1433";
    private static final String SQL_USER = "sa";
    private static final String SQL_PASSWORD = "sapassword";
    private static final String URL_SQL_AUTH =
            String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;",
                    SERVER_NAME, PORT, DB_NAME, SQL_USER, SQL_PASSWORD);

    // Thêm một biến static để lưu kết nối ---
    private static Connection connection = null;

    /**
     * Lấy kết nối đến CSDL SQL Server (Singleton Pattern).
     * Chỉ tạo kết nối mới nếu nó chưa tồn tại hoặc đã bị đóng.
     * @return một đối tượng Connection
     */
    public static Connection getConnection() {
        try {
            // ---: Kiểm tra xem kết nối ---
            if (connection == null || connection.isClosed()) {
                // 1. Nạp driver JDBC của SQL Server
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                // 2. Tạo kết nối mới VÀ LƯU LẠI
                connection = DriverManager.getConnection(URL_SQL_AUTH);
            }

            // 3. Trả về kết nối đã có
            return connection;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Lỗi: Không tìm thấy driver JDBC của SQL Server!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối CSDL: " + e.getMessage(), e);
        }
    }

    /**
     * (Tùy chọn) Thêm hàm này để gọi khi tắt ứng dụng
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối CSDL.");
            } catch (SQLException e) {
                // Bỏ qua lỗi
            }
        }
    }

    /**
     * Hàm main để kiểm tra kết nối CSDL
     */
//    public static void main(String[] args) {
//        try {
//            Connection conn = SQLConnection.getConnection();
//            if (conn != null && !conn.isClosed()) {
//                System.out.println(" Kết nối CSDL SQL Server THÀNH CÔNG!");
//                System.out.println("Đã kết nối tới database: " + conn.getCatalog());
//
//                // Thử kết nối lần 2
//                Connection conn2 = SQLConnection.getConnection();
//                System.out.println("Kết nối lần 2...");
//                // So sánh 2 đối tượng connection
//                if (conn == conn2) {
//                    System.out.println("Tốt! Đã tái sử dụng kết nối thành công.");
//                } else {
//                    System.err.println("Lỗi! Đã tạo kết nối mới.");
//                }
//
//            }
//        } catch (Exception e) {
//            System.err.println(" Kết nối CSDL THẤT BẠI!");
//            e.printStackTrace();
//        } finally {
//            SQLConnection.closeConnection(); // Đóng kết nối khi test xong
//        }
//    }
}