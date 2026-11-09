package hr.db;

import java.sql.Connection;
import java.sql.SQLException;

public class DBTest {

    public static void main(String[] args) {

        try {
            Connection conn = DBConnection.getConnection();

            System.out.println("Aiven MySQL 연결 성공!");

            conn.close();

        } catch (SQLException e) {
            System.out.println("DB 연결 실패!");
            e.printStackTrace();
        }
    }
}