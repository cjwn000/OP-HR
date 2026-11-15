
package hr.dto;

import java.time.LocalDateTime;

/**
 * accounts 테이블의 데이터를 저장하는 DTO 클래스.
 *
 * DTO(Data Transfer Object)는
 * 데이터베이스에서 조회한 정보를 Java 객체에 저장하는 역할을 한다.
 *
 * accounts 테이블의 한 행에 해당하는 정보를 보관한다.
 */
public class AccountDTO {

    // 계정의 고유번호 (PRIMARY KEY)
    private int accountId;

    // 계정과 연결된 직원의 고유번호 (FOREIGN KEY)
    private int employeeId;

    // 로그인 아이디
    private String username;

    // SHA-256 방식으로 변환된 비밀번호 해시
    private String passwordHash;

    // 계정 권한: ADMIN 또는 USER
    private String role;

    // 계정 활성 여부
    private boolean active;

    // 계정 생성 날짜와 시간
    private LocalDateTime createdAt;

    // 계정 ID 조회 및 설정
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    // 직원 ID 조회 및 설정
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    // 로그인 아이디 조회 및 설정
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // 비밀번호 해시 조회 및 설정
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // 계정 권한 조회 및 설정
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // 활성 상태 조회 및 설정
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // 계정 생성일 조회 및 설정
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
