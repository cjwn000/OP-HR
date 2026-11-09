
package hr.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {

        Properties properties = new Properties();

        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new SQLException("db.properties 파일을 찾을 수 없습니다.");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new SQLException("DB 설정 파일을 읽을 수 없습니다.", e);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}
