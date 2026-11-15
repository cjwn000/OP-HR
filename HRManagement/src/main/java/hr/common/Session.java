
package hr.common;

/**
 * 현재 로그인한 사용자의 정보를 저장하는 공통 클래스.
 *
 * COM-01 로그인 성공 시 정보를 저장한다.
 * 관리자 및 사용자 화면에서 로그인 정보를 공유한다.
 * 로그아웃 시 clear()로 모든 정보를 초기화한다.
 *
 * static 변수를 사용하므로 객체 생성 없이 접근할 수 있다.
 */
public class Session {

    // 현재 로그인한 계정의 고유번호
    public static int accountId;

    // 현재 로그인한 직원의 고유번호
    public static int employeeId;

    // 로그인 아이디
    public static String username;

    // ADMIN 또는 USER
    public static String role;

    // 직원 이름
    public static String name;

    // 직원 사번
    public static String employeeNo;

    /**
     * 외부에서 Session 객체를 생성하지 못하도록 제한한다.
     */
    private Session() {
    }

    /**
     * 로그아웃할 때 저장된 사용자 정보를 초기화한다.
     */
    public static void clear() {

        accountId = 0;
        employeeId = 0;

        username = null;
        role = null;
        name = null;
        employeeNo = null;
    }
}
