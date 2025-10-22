package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    // --- CẤU HÌNH KẾT NỐI VỚI SQL SERVER ---
    
    // Tên máy chủ (thường là 'localhost' nếu chạy trên máy của bạn)
    private static final String SERVER_NAME = "localhost"; 
    
    // Tên CSDL bạn đã tạo
    private static final String DB_NAME = "StarGuardianDB"; 

    // Cổng (default của SQL Server là 1433)
    private static final String PORT = "1433";


    //  Dùng SQL Server Authentication (Dùng user/pass của SQL Server) ---
    // Thường dùng user 'sa' (System Administrator)
    private static final String SQL_USER = "sa";
    private static final String SQL_PASSWORD = "123456789"; // ⚠️ THAY MẬT KHẨU CỦA BẠN VÀO ĐÂY
    private static final String URL_SQL_AUTH = 
        String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;",
                      SERVER_NAME, PORT, DB_NAME, SQL_USER, SQL_PASSWORD);

    /**
     * Lấy kết nối đến CSDL SQL Server.
     * @return một đối tượng Connection
     */
    public static Connection getConnection() {
        try {
            // 1. Nạp driver JDBC của SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // 2. Tạo kết nối
       
            // Dùng cách 2:
            Connection conn = DriverManager.getConnection(URL_SQL_AUTH);

            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Lỗi: Không tìm thấy driver JDBC của SQL Server!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối CSDL: " + e.getMessage(), e);
        }
    }

    /**
     * Hàm main để kiểm tra kết nối CSDL
     */
    public static void main(String[] args) {
        try {
            Connection conn = SQLConnection.getConnection();
            if (conn != null) {
                System.out.println("✅ Kết nối CSDL SQL Server THÀNH CÔNG!");
                System.out.println("Đã kết nối tới database: " + conn.getCatalog());
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("❌ Kết nối CSDL THẤT BẠI!");
            e.printStackTrace();
        }
    }
}