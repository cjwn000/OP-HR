
package hr.dto;

import java.time.LocalDate;

/**
 * employees 테이블의 데이터를 저장하는 DTO 클래스.
 *
 * 직원의 기본정보, 입사일, 계약기간,
 * 고용형태, 부서, 직급, 재직상태를 보관한다.
 *
 * 로그인할 때는 직원 이름, 사번, 재직상태 등을 사용한다.
 */
public class EmployeeDTO {

    // 직원 고유번호 (PRIMARY KEY)
    private int employeeId;

    // 직원 사번
    private String employeeNo;

    // 직원 이름
    private String name;

    // 전화번호
    private String phone;

    // 이메일
    private String email;

    // 입사일
    private LocalDate hireDate;

    // 근로계약 시작일 (NULL 가능)
    private LocalDate contractStart;

    // 근로계약 종료일 (NULL 가능)
    private LocalDate contractEnd;

    // 고용형태 ID (employment_types 참조)
    private int employmentTypeId;

    // 부서 ID (departments 참조)
    private int deptId;

    // 직급 ID (positions 참조)
    private int positionId;

    // 재직상태: ACTIVE / ON_LEAVE / RESIGNED
    private String status;

    // 직원 ID
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    // 사번
    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    // 직원 이름
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 전화번호
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 이메일
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // 입사일
    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    // 계약 시작일
    public LocalDate getContractStart() {
        return contractStart;
    }

    public void setContractStart(LocalDate contractStart) {
        this.contractStart = contractStart;
    }

    // 계약 종료일
    public LocalDate getContractEnd() {
        return contractEnd;
    }

    public void setContractEnd(LocalDate contractEnd) {
        this.contractEnd = contractEnd;
    }

    // 고용형태 ID
    public int getEmploymentTypeId() {
        return employmentTypeId;
    }

    public void setEmploymentTypeId(int employmentTypeId) {
        this.employmentTypeId = employmentTypeId;
    }

    // 부서 ID
    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    // 직급 ID
    public int getPositionId() {
        return positionId;
    }

    public void setPositionId(int positionId) {
        this.positionId = positionId;
    }

    // 재직상태
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
