
package hr.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 비밀번호를 SHA-256 해시로 변환하는 공통 클래스.
 *
 * DB에는 평문 비밀번호를 저장하지 않고
 * SHA-256 해시 문자열을 저장한다.
 *
 * 기존 DB의 password_hash 컬럼 형식:
 * 소문자 16진수 64자리
 *
 * COM-01 로그인 및 COM-02 비밀번호 변경에서 사용한다.
 */
public class HashUtil {

    // 객체 생성을 막기 위한 private 생성자
    private HashUtil() {
    }

    /**
     * 입력한 비밀번호를 SHA-256 해시로 변환한다.
     *
     * @param plain 사용자가 입력한 비밀번호
     * @return SHA-256 해시 문자열
     */
    public static String sha256(String plain) {

        try {

            // SHA-256 알고리즘 객체 생성
            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            // 비밀번호를 UTF-8 바이트 배열로 변환하고 해시 계산
            byte[] digest = md.digest(
                    plain.getBytes(StandardCharsets.UTF_8)
            );

            // 해시 바이트 배열을 소문자 16진수 문자열로 변환
            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {

            // SHA-256 알고리즘을 사용할 수 없을 때 발생
            throw new IllegalStateException(
                    "SHA-256을 사용할 수 없습니다.", e
            );
        }
    }

    /**
     * 사용자가 입력한 비밀번호와 DB에 저장된 해시를 비교한다.
     *
     * @param plain 사용자가 입력한 비밀번호
     * @param storedHash DB의 password_hash
     * @return 비밀번호가 일치하면 true, 다르면 false
     */
    public static boolean matches(
            String plain,
            String storedHash) {

        // 비교 대상 중 하나라도 null이면 실패
        if (plain == null || storedHash == null) {
            return false;
        }

        // 입력 비밀번호를 SHA-256으로 변환
        byte[] inputHash = sha256(plain).getBytes(
                StandardCharsets.UTF_8
        );

        // DB에 저장된 해시 문자열을 바이트 배열로 변환
        byte[] dbHash = storedHash.getBytes(
                StandardCharsets.UTF_8
        );

        // 두 해시값 비교
        return MessageDigest.isEqual(inputHash, dbHash);
    }
}
