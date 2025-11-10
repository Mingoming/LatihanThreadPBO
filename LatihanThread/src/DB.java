import java.sql.*;

public class DB {
    // ====== KONFIGURASI MYSQL ======
    private static final String HOST = "localhost";
    private static final int    PORT = 3306;
    private static final String DBNAME = "kafekeren";
    private static final String USER = "root";
    private static final String PASS = ""; 

    private static final String TZ = "Asia/Makassar";

    private static Connection conn;

    public static synchronized Connection get() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                ensureDatabaseExists();

                String url = String.format(
                        "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=%s",
                        HOST, PORT, DBNAME, TZ
                );
                conn = DriverManager.getConnection(url, USER, PASS);
                conn.setAutoCommit(false);

                initSchema();
            }
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ensureDatabaseExists() {
        String urlNoDb = String.format(
                "jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=%s",
                HOST, PORT, TZ
        );
        try (Connection c = DriverManager.getConnection(urlNoDb, USER, PASS);
            Statement st = c.createStatement()) {

            st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS " + DBNAME +
                " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci"
            );

        } catch (SQLException e) {
            throw new RuntimeException("Gagal memastikan database ada: " + e.getMessage(), e);
        }
    }


    private static void initSchema() {
        String createTable =
            "CREATE TABLE IF NOT EXISTS orders (" +
            "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "  customer_name VARCHAR(100) NOT NULL," +
            "  item VARCHAR(100) NOT NULL," +
            "  quantity INT NOT NULL," +
            "  status VARCHAR(20) NOT NULL," +
            "  created_at_epoch BIGINT NOT NULL," +
            "  KEY idx_orders_status (status)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Statement st = get().createStatement()) {
            st.executeUpdate(createTable);
            get().commit();
        } catch (SQLException e) {
            try { get().rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Gagal inisialisasi schema: " + e.getMessage(), e);
        }
    }
}
