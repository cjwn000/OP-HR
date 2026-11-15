
package hr.dao;

import hr.db.DBConnection;
import hr.dto.AccountDTO;
import hr.dto.EmployeeDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * accounts 테이블 DAO.
 *
 * COM-01 로그인(R), COM-02 비밀번호 변경(R/U)을 담당한다.
 * 이후 ADM-03/04에서 INSERT 등의 메서드를 추가할 수 있다.
 *
 * 화면(Panel/Frame)에 SQL을 직접 작성하지 않고
 * 데이터베이스 관련 처리는 DAO에서 담당한다.
 *
 * 모든 SQL은 PreparedStatement를 사용하고
 * DB 자원은 try-with-resources로 관리한다.
 */
public class AccountDAO {

    /**
     * 로그인 조회 결과를 하나로 묶는 record.
     *
     * AccountDTO : 로그인 계정 정보
     * EmployeeDTO : 계정에 연결된 직원 정보
     */
    public record LoginInfo(
            AccountDTO account,
            EmployeeDTO employee) {
    }

    /**
     * COM-01 ③ 로그인 정보 조회.
     *
     * username을 이용해 accounts 테이블을 조회하고
     * employee_id를 기준으로 employees 테이블을 JOIN한다.
     *
     * @param username 로그인 아이디
     * @return 계정+직원 정보, 조회 결과가 없으면 null
     * @throws SQLException DB 연결 또는 조회 오류
     */
    public LoginInfo findLoginInfo(String username)
            throws SQLException {

        // accounts와 employees 테이블을 JOIN하여 조회
        // a는 accounts의 별칭, e는 employees의 별칭
        String sql =
                "SELECT a.account_id, a.employee_id, " +
                        "a.username, a.password_hash, a.role, " +
                        "a.is_active, a.created_at, " +
                        "e.employee_no, e.name, e.status, " +
                        "e.dept_id, e.position_id " +
                        "FROM accounts a " +
                        "JOIN employees e " +
                        "ON a.employee_id = e.employee_id " +
                        "WHERE a.username = ?";

        // DB 연결 및 SQL 실행 객체 생성
        // try-with-resources를 사용하므로 자동으로 닫힌다.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // SQL의 첫 번째 ?에 사용자 아이디를 넣는다.
            ps.setString(1, username);

            // SELECT SQL 실행
            try (ResultSet rs = ps.executeQuery()) {

                // 조회 결과가 없다면 null 반환
                if (!rs.next()) {
                    return null;
                }

                // accounts 조회 결과를 AccountDTO에 저장
                AccountDTO a = new AccountDTO();

                a.setAccountId(
                        rs.getInt("account_id"));

                a.setEmployeeId(
                        rs.getInt("employee_id"));

                a.setUsername(
                        rs.getString("username"));

                a.setPasswordHash(
                        rs.getString("password_hash"));

                a.setRole(
                        rs.getString("role"));

                a.setActive(
                        rs.getBoolean("is_active"));

                a.setCreatedAt(
                        rs.getObject(
                                "created_at",
                                LocalDateTime.class));

                // employees 조회 결과를 EmployeeDTO에 저장
                EmployeeDTO e = new EmployeeDTO();

                // AccountDTO의 직원 ID와 동일한 값
                e.setEmployeeId(a.getEmployeeId());

                e.setEmployeeNo(
                        rs.getString("employee_no"));

                e.setName(
                        rs.getString("name"));

                e.setStatus(
                        rs.getString("status"));

                e.setDeptId(
                        rs.getInt("dept_id"));

                e.setPositionId(
                        rs.getInt("position_id"));

                // 계정 정보와 직원 정보를 함께 반환
                return new LoginInfo(a, e);
            }
        }
    }

    /**
     * COM-02 ② 현재 비밀번호 해시 조회.
     *
     * 비밀번호 변경 시 현재 비밀번호가 올바른지
     * 확인하기 위해 사용한다.
     *
     * @param accountId 현재 로그인한 계정 ID
     * @return 저장된 비밀번호 해시, 없으면 null
     */
    public String findPasswordHash(int accountId)
            throws SQLException {

        // 계정 ID로 비밀번호 해시 조회
        String sql =
                "SELECT password_hash " +
                        "FROM accounts WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // 첫 번째 ?에 계정 ID 설정
            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {

                // 계정이 있으면 해시 반환, 없으면 null
                return rs.next()
                        ? rs.getString("password_hash")
                        : null;
            }
        }
    }

    /**
     * COM-02 ② 비밀번호 해시 변경.
     *
     * 현재 DB에 저장된 해시가 oldHash와 일치할 때만
     * 새 비밀번호 해시로 변경한다.
     *
     * @param accountId 변경할 계정 ID
     * @param oldHash 기존 비밀번호 해시
     * @param newHash 새 비밀번호 해시
     * @return 변경된 행의 개수 (정상적으로 변경되면 1)
     */
    public int updatePasswordHash(
            int accountId,
            String oldHash,
            String newHash) throws SQLException {

        // 해당 계정이 활성 상태이며
        // 기존 해시가 그대로인 경우에만 UPDATE
        String sql =
                "UPDATE accounts " +
                        "SET password_hash = ? " +
                        "WHERE account_id = ? " +
                        "AND password_hash = ? " +
                        "AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // 첫 번째 ? : 새 비밀번호 해시
            ps.setString(1, newHash);

            // 두 번째 ? : 계정 ID
            ps.setInt(2, accountId);

            // 세 번째 ? : 기존 비밀번호 해시
            ps.setString(3, oldHash);

            // UPDATE 실행 후 수정된 행 수 반환
            return ps.executeUpdate();
        }
    }
}
