package hr.main;

import hr.login.LoginFrame;

import javax.swing.*;

/** 프로그램 시작점 → LoginFrame */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {

            }
            new LoginFrame().setVisible(true);
        });
    }
}
