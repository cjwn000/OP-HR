
package hr.common;

import hr.login.LoginFrame;
import hr.login.PasswordChangeDialog;

import javax.swing.*;
import java.awt.*;

/**
 * 관리자와 일반 사용자 메인 창에서 공통으로 사용하는 상단 바.
 *
 * 기능:
 * 1. 로그인한 사용자 이름과 권한 표시
 * 2. 비밀번호 변경 창 열기
 * 3. 로그아웃 처리
 */
public class HeaderPanel extends JPanel {

    /**
     * @param owner 현재 상단 바가 속한 JFrame
     */
    public HeaderPanel(JFrame owner) {

        // 좌우로 컴포넌트를 배치
        super(new BorderLayout());

        // 상단 바 여백 설정
        setBorder(
                BorderFactory.createEmptyBorder(
                        8, 16, 8, 16));

        // Session에 저장된 이름과 권한 표시
        JLabel lblUser = new JLabel(
                Session.name + " (" + Session.role + ")");

        lblUser.setFont(
                lblUser.getFont().deriveFont(Font.BOLD));

        // 사용자 정보를 왼쪽에 배치
        add(lblUser, BorderLayout.WEST);

        // 비밀번호 변경 버튼
        JButton btnPw = new JButton("비밀번호 변경");

        // 로그아웃 버튼
        JButton btnLogout = new JButton("로그아웃");

        // 버튼 영역
        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        right.setOpaque(false);

        right.add(btnPw);
        right.add(btnLogout);

        // 오른쪽에 버튼 표시
        add(right, BorderLayout.EAST);

        // 비밀번호 변경 버튼 이벤트
        btnPw.addActionListener(e -> {

            // 현재 프레임을 부모로 하여 모달 창 생성
            PasswordChangeDialog dialog =
                    new PasswordChangeDialog(owner);

            dialog.setVisible(true);
        });

        // 로그아웃 버튼 이벤트
        btnLogout.addActionListener(e -> {

            // 로그인 정보 초기화
            Session.clear();

            // 로그인 창 다시 생성
            new LoginFrame().setVisible(true);

            // 기존 관리자/사용자 창 닫기
            owner.dispose();
        });
    }
}
