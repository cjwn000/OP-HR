
package hr.login;

import hr.admin.AdminMainFrame;
import hr.common.HashUtil;
import hr.common.Session;
import hr.dao.AccountDAO;
import hr.user.UserMainFrame;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

/**
 * COM-01 로그인 화면.
 *
 * 사용자로부터 아이디와 비밀번호를 입력받는다.
 * AccountDAO를 이용해 계정과 직원 정보를 조회한다.
 *
 * 로그인 성공 시 Session에 정보를 저장하고
 * ADMIN이면 관리자 화면,
 * USER이면 일반 사용자 화면으로 이동한다.
 *
 * 로그인 실패가 연속 3회 발생하면
 * 5초 동안 로그인 버튼을 비활성화한다.
 */
public class LoginFrame extends JFrame {

    // 아이디 저장에 사용하는 키
    private static final String PREF_USERNAME = "savedUsername";

    // 비밀번호를 가릴 때 사용할 문자
    private static final char MASK_CHAR = '●';

    // 로그인 최대 실패 횟수
    private static final int MAX_FAIL = 3;

    // 로그인 잠금 시간
    private static final int LOCK_SECONDS = 5;

    // DB 계정 조회 객체
    private final AccountDAO accountDAO = new AccountDAO();

    // 아이디 저장을 위한 Java 기본 Preferences
    private final Preferences prefs =
            Preferences.userNodeForPackage(LoginFrame.class);

    // 아이디 입력 필드
    private final JTextField txtUsername = new JTextField(16);

    // 비밀번호 입력 필드
    private final JPasswordField txtPassword =
            new JPasswordField(16);

    // 아이디 저장 체크박스
    private final JCheckBox chkSaveId =
            new JCheckBox("아이디 저장");

    // 비밀번호 표시 체크박스
    private final JCheckBox chkShowPw =
            new JCheckBox("비밀번호 표시");

    // 로그인 버튼
    private final JButton btnLogin = new JButton("로그인");

    // 결과 메시지 출력
    private final JLabel lblMessage =
            new JLabel(" ", SwingConstants.CENTER);

    // 연속 실패 횟수
    private int failCount = 0;

    // 로그인 잠금 타이머
    private Timer lockTimer;

    // 잠금 남은 시간
    private int remainSeconds;

    // DB 조회 중인지 확인
    private boolean loggingIn = false;

    /**
     * 로그인 창 생성자.
     */
    public LoginFrame() {

        super("HRMS - 로그인");

        // 화면 구성
        buildUI();

        // 이벤트 연결
        bindEvents();

        // 저장된 아이디 불러오기
        loadSavedUsername();

        pack();
        setSize(440, getHeight() + 30);

        // 창 크기 변경 제한
        setResizable(false);

        // 화면 중앙에 배치
        setLocationRelativeTo(null);

        // X 버튼 클릭 시 프로그램 종료
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Enter 키를 누르면 로그인 버튼 실행
        getRootPane().setDefaultButton(btnLogin);
    }

    /**
     * 로그인 화면에 표시할 컴포넌트를 생성한다.
     */
    private void buildUI() {

        // GridBagLayout으로 입력 폼 배치
        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 32, 24, 32));

        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(6, 4, 6, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // HRMS 제목
        JLabel lblLogo =
                new JLabel("HRMS", SwingConstants.CENTER);

        lblLogo.setFont(
                lblLogo.getFont().deriveFont(Font.BOLD, 28f));

        // 부제목
        JLabel lblSub =
                new JLabel("인사관리 시스템",
                        SwingConstants.CENTER);

        lblSub.setForeground(Color.GRAY);

        // 제목은 두 열을 차지하도록 설정
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;

        form.add(lblLogo, c);

        c.gridy = 1;
        form.add(lblSub, c);

        // 아이디 라벨
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridx = 0;
        c.gridy = 2;

        form.add(new JLabel("아이디"), c);

        // 아이디 입력칸
        c.gridx = 1;
        c.weightx = 1;

        form.add(txtUsername, c);

        // 비밀번호 라벨
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;

        form.add(new JLabel("비밀번호"), c);

        // 비밀번호 입력칸
        c.gridx = 1;
        c.weightx = 1;

        form.add(txtPassword, c);

        // 기본적으로 비밀번호 가리기
        txtPassword.setEchoChar(MASK_CHAR);

        // 체크박스 배치
        JPanel chkPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        chkPanel.add(chkSaveId);
        chkPanel.add(chkShowPw);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.weightx = 1;

        form.add(chkPanel, c);

        // 로그인 버튼
        btnLogin.setPreferredSize(new Dimension(350, 40));
        btnLogin.setBackground(new Color(65, 125, 190));
        btnLogin.setForeground(Color.WHITE);

        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        c.gridy = 5;
        form.add(btnLogin, c);

        // 로그인 오류 메시지
        lblMessage.setForeground(Color.RED);

        c.gridy = 6;
        form.add(lblMessage, c);

        // 화면 하단 안내
        JLabel lblInfo = new JLabel(
                "계정 발급은 관리자에게 문의하세요.",
                SwingConstants.CENTER);

        lblInfo.setForeground(Color.GRAY);

        c.gridy = 7;
        form.add(lblInfo, c);

        // JFrame에 화면 등록
        setContentPane(form);
    }

    /**
     * 버튼과 체크박스 이벤트를 연결한다.
     */
    private void bindEvents() {

        // 아이디 저장 체크 해제 시 기존 저장값 삭제
        chkSaveId.addActionListener(e -> {

            if (!chkSaveId.isSelected()) {
                prefs.remove(PREF_USERNAME);
            }
        });

        // 비밀번호 표시 체크박스
        chkShowPw.addItemListener(e -> {

            if (chkShowPw.isSelected()) {

                // 비밀번호 보이기
                txtPassword.setEchoChar((char) 0);

            } else {

                // 비밀번호 숨기기
                txtPassword.setEchoChar(MASK_CHAR);
            }
        });

        // 로그인 버튼 클릭 시 로그인 검사
        btnLogin.addActionListener(e -> doLogin());
    }

    /**
     * 이전에 저장된 아이디를 입력 필드에 표시한다.
     */
    private void loadSavedUsername() {

        String saved = prefs.get(PREF_USERNAME, "");

        if (!saved.isEmpty()) {

            txtUsername.setText(saved);
            chkSaveId.setSelected(true);

            // 비밀번호 입력칸에 커서 이동
            SwingUtilities.invokeLater(
                    txtPassword::requestFocusInWindow);
        }
    }

    /**
     * 로그인 버튼 클릭 시 실행되는 메서드.
     *
     * 입력값 검사 후 별도 스레드에서 DB 조회를 수행한다.
     */
    private void doLogin() {

        // 이미 로그인 조회 중이면 중복 요청 방지
        if (loggingIn) {
            return;
        }

        // 아이디 가져오기
        String username = txtUsername.getText().trim();

        // 비밀번호 가져오기
        char[] pw = txtPassword.getPassword();

        try {

            // 입력값 공백 검사
            if (username.isEmpty() || pw.length == 0) {

                showError("아이디와 비밀번호를 입력하세요.");
                return;
            }

            // HashUtil이 String을 사용하므로 문자열로 변환
            String password = new String(pw);

            // DB 작업 시작
            loggingIn = true;

            btnLogin.setEnabled(false);
            btnLogin.setText("로그인 중...");

            lblMessage.setText(" ");

            /**
             * SwingWorker:
             *
             * doInBackground()는 별도 스레드에서 실행된다.
             * done()은 Swing 이벤트 스레드에서 실행된다.
             */
            SwingWorker<AccountDAO.LoginInfo, Void> worker =
                    new SwingWorker<>() {

                        @Override
                        protected AccountDAO.LoginInfo doInBackground()
                                throws Exception {

                            // 아이디로 계정과 직원 정보 조회
                            AccountDAO.LoginInfo info =
                                    accountDAO.findLoginInfo(username);

                            // 아이디가 존재하지 않음
                            if (info == null) {
                                return null;
                            }

                            // 입력 비밀번호를 해시로 변환하여 비교
                            if (!HashUtil.matches(
                                    password,
                                    info.account().getPasswordHash())) {

                                return null;
                            }

                            // 비밀번호가 맞으면 계정+직원 정보 반환
                            return info;
                        }

                        @Override
                        protected void done() {

                            try {

                                // DB 조회 결과 가져오기
                                AccountDAO.LoginInfo info = get();

                                // 계정이 없거나 비밀번호가 틀림
                                if (info == null) {
                                    onLoginFail();
                                    return;
                                }

                                // 비활성화 계정 로그인 차단
                                if (!info.account().isActive()) {

                                    showError("비활성화된 계정입니다.");
                                    return;
                                }

                                // 퇴사 처리된 직원 로그인 차단
                                if ("RESIGNED".equals(
                                        info.employee().getStatus())) {

                                    showError("퇴사 처리된 계정입니다.");
                                    return;
                                }

                                // 계정 권한 확인
                                String role = info.account().getRole();

                                if (!"ADMIN".equals(role)
                                        && !"USER".equals(role)) {

                                    showError("알 수 없는 계정 권한입니다.");
                                    return;
                                }

                                // 로그인 성공 시 실패 횟수 초기화
                                failCount = 0;

                                // 아이디 저장 체크 여부 확인
                                if (chkSaveId.isSelected()) {

                                    prefs.put(PREF_USERNAME, username);

                                } else {

                                    prefs.remove(PREF_USERNAME);
                                }

                                // 로그인 성공 정보를 Session에 저장
                                Session.accountId =
                                        info.account().getAccountId();

                                Session.employeeId =
                                        info.account().getEmployeeId();

                                Session.username =
                                        info.account().getUsername();

                                Session.role = role;

                                Session.name =
                                        info.employee().getName();

                                Session.employeeNo =
                                        info.employee().getEmployeeNo();

                                // 관리자 또는 사용자 화면 열기
                                openMainFrame();

                            } catch (InterruptedException ex) {

                                Thread.currentThread().interrupt();

                                showError("로그인 작업이 중단되었습니다.");

                            } catch (ExecutionException ex) {

                                // DB 작업 중 발생한 예외 확인
                                Throwable cause = ex.getCause();

                                JOptionPane.showMessageDialog(
                                        LoginFrame.this,
                                        "데이터베이스 오류가 발생했습니다.\n"
                                                + cause.getMessage(),
                                        "오류",
                                        JOptionPane.ERROR_MESSAGE
                                );

                            } finally {

                                // DB 조회 완료
                                loggingIn = false;

                                // 잠금 중이 아니라면 로그인 버튼 활성화
                                if (lockTimer == null
                                        || !lockTimer.isRunning()) {

                                    btnLogin.setEnabled(true);
                                    btnLogin.setText("로그인");
                                }
                            }
                        }
                    };

            // 백그라운드 DB 조회 시작
            worker.execute();

        } finally {

            // 입력받은 char 배열 초기화
            Arrays.fill(pw, '\0');
        }
    }

    /**
     * 아이디 또는 비밀번호 오류 시 실행한다.
     */
    private void onLoginFail() {

        // 실패 횟수 증가
        failCount++;

        // 입력한 비밀번호 삭제
        txtPassword.setText("");

        // 오류 메시지
        showError("아이디 또는 비밀번호가 올바르지 않습니다.");

        // 3회 실패하면 버튼 잠금
        if (failCount >= MAX_FAIL) {
            lockLoginButton();
        }
    }

    /**
     * 로그인 버튼을 5초간 비활성화한다.
     */
    private void lockLoginButton() {

        failCount = 0;
        remainSeconds = LOCK_SECONDS;

        btnLogin.setEnabled(false);
        btnLogin.setText(
                "로그인 (" + remainSeconds + ")");

        // 1초마다 실행되는 Swing Timer
        lockTimer = new Timer(1000, e -> {

            remainSeconds--;

            if (remainSeconds <= 0) {

                // 시간이 끝나면 타이머 정지
                lockTimer.stop();

                // 로그인 버튼 활성화
                btnLogin.setEnabled(true);
                btnLogin.setText("로그인");

                lblMessage.setText(" ");

            } else {

                // 남은 시간 표시
                btnLogin.setText(
                        "로그인 (" + remainSeconds + ")");
            }
        });

        // 타이머 시작
        lockTimer.start();
    }

    /**
     * 로그인한 계정의 role에 따라 메인 화면을 연다.
     */
    private void openMainFrame() {

        if ("ADMIN".equals(Session.role)) {

            // 관리자 메인 화면 생성
            new AdminMainFrame().setVisible(true);

        } else if ("USER".equals(Session.role)) {

            // 일반 사용자 메인 화면 생성
            new UserMainFrame().setVisible(true);

        } else {

            // 잘못된 권한이면 세션 초기화
            Session.clear();

            showError("알 수 없는 계정 권한입니다.");
            return;
        }

        // 로그인 창 종료
        dispose();
    }

    /**
     * 로그인 화면에 오류 메시지를 표시한다.
     */
    private void showError(String message) {

        lblMessage.setText(message);
    }
}
