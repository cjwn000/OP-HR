package hr.main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("HR 인사 관리 시스템");

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JLabel label = new JLabel(
                "HR 인사 관리 시스템",
                SwingConstants.CENTER
        );

        frame.add(label);
        frame.setVisible(true);
    }
}