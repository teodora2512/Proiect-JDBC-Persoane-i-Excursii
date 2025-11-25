import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexiuneBD {
    private static final String url = "jdbc:mysql://localhost:3306/lab8";
    private static final String user="root";
    private static final String password="Scotti22";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,user,password);
    }
}
