
package hr.login;

import hr.common.HashUtil;
import hr.common.NoSpaceFilter;
import hr.common.Session;
import hr.dao.AccountDAO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * COM-02 비밀번호 변경 화면.
 *
 * 현재 로그인한 사용자의 비밀번호를 변경한다.
 *
 * 1. 현재 비밀번호 확인
 * 2. 새 비밀번호 유효성 검사
 * 3. 새 비밀번호와 확인 비밀번호 일치 검사
 * 4. 새 비밀번호 SHA-256 해시 생성
 * 5. accounts 테이블의 password_hash UPDATE
 *
 * JDialog를 사용하여 메인 화면 위에 모달 창으로 표시한다.
 */
public class PasswordChangeDialog extends JDialog {

    // 비밀번호 변경 DAO
    private final AccountDAO accountDAO = new AccountDAO();

    // 로그인한 아이디 표시
    private final JTextField txtUsername = new JTextField(16);

    // 현재 비밀번호
    private final JPasswordField txtCurrent =
            new JPasswordField(16);

    // 새 비밀번호
    private final JPasswordField txtNew =
            new JPasswordField(16);

    // 새 비밀번호 확인
    private final JPasswordField txtConfirm =
            new JPasswordField(16);

    // 비밀번호 규칙 안내
    private final JLabel lblRule = new JLabel(" ");

    // 비밀번호 일치 여부 안내
    private final JLabel lblMatch = new JLabel(" ");

    // 변경 버튼
    private final JButton btnChange = new JButton("변경");

    // 취소 버튼
    private final JButton btnCancel = new JButton("취소");

    /**
     * 비밀번호 변경 창 생성자.
     *
     * @param owner 비밀번호 변경 창을 호출한 부모 창
     */
    public PasswordChangeDialog(Window owner) {

        // APPLICATION_MODAL : 변경 창이 열려 있는 동안
        // 다른 프로그램 창을 조작하지 못하도록 한다.
        super(owner, "비밀번호 변경",
                ModalityType.APPLICATION_MODAL);

        // 화면 생성
        buildUI();

        // 이벤트 연결
        bindEvents();

        pack();

        setResizable(false);

        // 부모 창 중앙에 표시
        setLocationRelativeTo(owner);

        // 닫기 버튼 클릭 시 이 다이얼로그만 종료
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Enter 키로 변경 버튼 실행
        getRootPane().setDefaultButton(btnChange);
    }

    /**
     * 비밀번호 변경 화면 구성.
     */
    private void buildUI() {

        JPanel form = new JPanel(new GridBagLayout());

        form.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 28, 16, 28));

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;

        // 현재 로그인한 아이디 표시
        txtUsername.setText(Session.username);

        // 아이디 수정 방지
        txtUsername.setEditable(false);

        int row = 0;

        // 아이디 입력칸
        row = addField(
                form, c, row, "아이디", txtUsername);

        // 현재 비밀번호
        row = addField(
                form, c, row, "현재 비밀번호", txtCurrent);

        // 새 비밀번호
        row = addField(
                form, c, row, "새 비밀번호", txtNew);

        // 새 비밀번호 확인
        row = addField(
                form, c, row, "새 비밀번호 확인", txtConfirm);

        // 비밀번호 규칙 안내
        lblRule.setText(
                "8자 이상, 영문+숫자를 포함해야 합니다.");

        lblRule.setForeground(Color.GRAY);

        c.insets = new Insets(4, 0, 0, 0);
        c.gridy = row++;

        form.add(lblRule, c);

        // 비밀번호 일치 여부
        c.gridy = row++;

        form.add(lblMatch, c);

        // 버튼 영역
        JPanel buttons =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttons.add(btnChange);
        buttons.add(btnCancel);

        c.insets = new Insets(14, 0, 0, 0);
        c.gridy = row;

        form.add(buttons, c);

        setContentPane(form);
    }

    /**
     * 라벨과 입력칸을 화면에 추가하는 공통 메서드.
     *
     * @return 다음에 사용할 행 번호
     */
    private int addField(
            JPanel form,
            GridBagConstraints c,
            int row,
            String label,
            JComponent field) {

        c.insets = new Insets(6, 0, 2, 0);
        c.gridy = row++;

        // 항목 이름 표시
        form.add(new JLabel(label), c);

        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = row++;

        // 입력창 표시
        form.add(field, c);

        return row;
    }

    /**
     * 버튼과 입력 이벤트 연결.
     */
    private void bindEvents() {

        // 비밀번호 입력칸 3개에 공백(스페이스) 입력 차단
        NoSpaceFilter.apply(txtCurrent);
        NoSpaceFilter.apply(txtNew);
        NoSpaceFilter.apply(txtConfirm);

        // DocumentListener:
        // 입력칸의 내용이 변경될 때마다 검사
        DocumentListener live = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshHints();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshHints();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshHints();
            }
        };

        // 새 비밀번호 입력 시 실시간 검사
        txtNew.getDocument().addDocumentListener(live);

        // 확인 비밀번호 입력 시 실시간 검사
        txtConfirm.getDocument().addDocumentListener(live);

        // 변경 버튼
        btnChange.addActionListener(e -> doChange());

        // 취소 버튼
        btnCancel.addActionListener(e -> dispose());
    }

    /**
     * 새 비밀번호의 규칙과 일치 여부를 화면에 표시한다.
     */
    private void refreshHints() {

        char[] newArr = txtNew.getPassword();
        char[] confirmArr = txtConfirm.getPassword();

        try {

            String newPw = new String(newArr);
            String confirm = new String(confirmArr);

            // 새 비밀번호 규칙 검사
            if (newPw.isEmpty()) {

                lblRule.setText(
                        "8자 이상, 영문+숫자를 포함해야 합니다.");

                lblRule.setForeground(Color.GRAY);

            } else if (isValidRule(newPw)) {

                lblRule.setText(
                        "사용할 수 있는 비밀번호입니다.");

                lblRule.setForeground(new Color(0, 130, 0));

            } else {

                lblRule.setText(
                        "8자 이상, 영문+숫자를 포함해야 합니다.");

                lblRule.setForeground(Color.RED);
            }

            // 새 비밀번호와 확인 비밀번호 비교
            if (confirm.isEmpty()) {

                lblMatch.setText(" ");

            } else if (newPw.equals(confirm)) {

                lblMatch.setText("비밀번호가 일치합니다.");

                lblMatch.setForeground(
                        new Color(0, 130, 0));

            } else {

                lblMatch.setText(
                        "비밀번호가 일치하지 않습니다.");

                lblMatch.setForeground(Color.RED);
            }

        } finally {

            // 사용한 문자 배열 초기화
            Arrays.fill(newArr, '\0');
            Arrays.fill(confirmArr, '\0');
        }
    }

    /**
     * 새 비밀번호 규칙 검사.
     *
     * 조건:
     * 1. 8자 이상
     * 2. 영문자가 1개 이상
     * 3. 숫자가 1개 이상
     */
    private boolean isValidRule(String password) {

        if (password.length() < 8) {
            return false;
        }

        boolean hasEnglish = false;
        boolean hasNumber = false;

        for (char ch : password.toCharArray()) {

            // 영어 대문자 또는 소문자
            if ((ch >= 'A' && ch <= 'Z')
                    || (ch >= 'a' && ch <= 'z')) {

                hasEnglish = true;
            }

            // 숫자
            if (ch >= '0' && ch <= '9') {
                hasNumber = true;
            }
        }

        return hasEnglish && hasNumber;
    }

    /**
     * 비밀번호 변경 처리.
     *
     * 현재 비밀번호 확인 후
     * 새 비밀번호 해시를 DB에 저장한다.
     */
    private void doChange() {

        // 비밀번호 입력값 가져오기
        char[] curArr = txtCurrent.getPassword();
        char[] newArr = txtNew.getPassword();
        char[] confirmArr = txtConfirm.getPassword();

        String current = new String(curArr);
        String newPw = new String(newArr);
        String confirm = new String(confirmArr);

        // 문자 배열 초기화
        Arrays.fill(curArr, '\0');
        Arrays.fill(newArr, '\0');
        Arrays.fill(confirmArr, '\0');

        // 현재 비밀번호 공백 검사
        if (current.isEmpty()) {

            warn("현재 비밀번호를 입력하세요.");
            return;
        }

        // 새 비밀번호 규칙 검사
        if (!isValidRule(newPw)) {

            warn("새 비밀번호는 8자 이상이며 "
                    + "영문과 숫자를 포함해야 합니다.");
            return;
        }

        // 새 비밀번호 확인
        if (!newPw.equals(confirm)) {

            warn("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return;
        }

        // 중복 실행 방지
        btnChange.setEnabled(false);
        btnChange.setText("변경 중...");

        // 변경 대상 계정은 현재 로그인한 계정
        final int accountId = Session.accountId;

        // DB 작업을 별도 스레드에서 실행
        SwingWorker<Integer, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Integer doInBackground()
                            throws Exception {

                        // 현재 DB 비밀번호 해시 조회
                        String storedHash =
                                accountDAO.findPasswordHash(accountId);

                        // 현재 비밀번호가 틀린 경우
                        if (!HashUtil.matches(current, storedHash)) {
                            return -1;
                        }

                        // 새 비밀번호가 기존 비밀번호와 같은 경우
                        if (HashUtil.matches(newPw, storedHash)) {
                            return -2;
                        }

                        // 새 비밀번호를 SHA-256 해시로 변환
                        String newHash = HashUtil.sha256(newPw);

                        // 현재 해시가 그대로인 경우에만 변경
                        return accountDAO.updatePasswordHash(
                                accountId,
                                storedHash,
                                newHash
                        );
                    }

                    @Override
                    protected void done() {

                        // 작업 완료 후 버튼 복구
                        btnChange.setEnabled(true);
                        btnChange.setText("변경");

                        try {

                            // DB 처리 결과 확인
                            int result = get();

                            if (result == -1) {

                                warn("현재 비밀번호가 올바르지 않습니다.");

                            } else if (result == -2) {

                                warn("새 비밀번호는 현재 비밀번호와 달라야 합니다.");

                            } else if (result == 1) {

                                // 비밀번호 변경 성공
                                JOptionPane.showMessageDialog(
                                        PasswordChangeDialog.this,
                                        "비밀번호가 변경되었습니다.",
                                        "완료",
                                        JOptionPane.INFORMATION_MESSAGE
                                );

                                // 비밀번호 변경 창 닫기
                                dispose();

                            } else {

                                // DB에서 변경된 행이 없는 경우
                                warn("비밀번호를 변경하지 못했습니다. "
                                        + "다시 시도하세요.");
                            }

                        } catch (InterruptedException ex) {

                            Thread.currentThread().interrupt();

                            warn("작업이 중단되었습니다.");

                        } catch (ExecutionException ex) {

                            // DB 작업 중 예외 발생
                            Throwable cause = ex.getCause();

                            if (cause instanceof SQLException) {

                                warn("DB 오류: " + cause.getMessage());

                            } else {

                                warn("비밀번호 변경 중 오류가 발생했습니다.");
                            }
                        }
                    }
                };

        // 비밀번호 변경 작업 시작
        worker.execute();
    }

    /**
     * 사용자에게 경고 메시지를 표시한다.
     */
    private void warn(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "확인",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
