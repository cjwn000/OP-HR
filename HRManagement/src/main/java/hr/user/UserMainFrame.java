package hr.user;

import hr.common.HeaderPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 사용자 메인 창 임시
 */
public class UserMainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public UserMainFrame() {
        super("HRMS - 사용자");
        setLayout(new BorderLayout());
        add(new HeaderPanel(this), BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);

        // ▼ 팀원별로 자기 화면 한 줄만 추가
        cards.add(new JLabel("USR-01 홈 (구현 예정)", SwingConstants.CENTER), "USR-01");

        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void showScreen(String id) {
        cardLayout.show(cards, id);
    }
}
