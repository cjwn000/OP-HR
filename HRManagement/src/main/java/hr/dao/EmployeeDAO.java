
package hr.dao;

import hr.db.DBConnection;
import hr.dto.EmployeeDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * employees 테이블 DAO.
 *
 * 직원의 employee_id를 이용하여
 * 직원 상세 정보를 조회하는 역할을 담당한다.
 *
 * 추후 직원 등록, 수정, 검색 등의 메서드를 추가할 수 있다.
 *
 * SQL은 DAO에서만 작성하며
 * PreparedStatement와 try-with-resources를 사용한다.
 */
public class EmployeeDAO {

    /**
     * 직원 ID를 이용한 직원 상세 정보 조회.
     *
     * @param employeeId 조회할 직원의 ID
     * @return 직원 정보 DTO, 조회 결과가 없으면 null
     * @throws SQLException DB 연결 또는 SQL 실행 오류
     */
    public EmployeeDTO findById(int employeeId)
            throws SQLException {

        // 직원 ID가 일치하는 한 명의 직원 조회
        String sql =
                "SELECT * FROM employees WHERE employee_id = ?";

        // 데이터베이스 연결 및 SQL 객체 준비
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            // 첫 번째 ?에 직원 ID 설정
            ps.setInt(1, employeeId);

            // SELECT 실행
            try (ResultSet rs = ps.executeQuery()) {

                // 결과가 없으면 null 반환
                if (!rs.next()) {
                    return null;
                }

                // DB 결과를 담을 EmployeeDTO 생성
                EmployeeDTO e = new EmployeeDTO();

                // 직원 고유번호
                e.setEmployeeId(
                        rs.getInt("employee_id"));

                // 사번
                e.setEmployeeNo(
                        rs.getString("employee_no"));

                // 이름
                e.setName(
                        rs.getString("name"));

                // 전화번호
                e.setPhone(
                        rs.getString("phone"));

                // 이메일
                e.setEmail(
                        rs.getString("email"));

                // 입사일
                e.setHireDate(
                        rs.getObject(
                                "hire_date",
                                LocalDate.class));

                // 계약 시작일
                e.setContractStart(
                        rs.getObject(
                                "contract_start",
                                LocalDate.class));

                // 계약 종료일
                e.setContractEnd(
                        rs.getObject(
                                "contract_end",
                                LocalDate.class));

                // 고용형태
                e.setEmploymentTypeId(
                        rs.getInt("employment_type_id"));

                // 부서
                e.setDeptId(
                        rs.getInt("dept_id"));

                // 직급
                e.setPositionId(
                        rs.getInt("position_id"));

                // 재직상태
                e.setStatus(
                        rs.getString("status"));

                // 완성된 직원 DTO 반환
                return e;
            }
        }
    }
}
