package hr.common;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 * 입력칸에 공백(스페이스, 탭 등)이 들어가지 않도록 막는 필터.
 *
 * 키보드로 입력할 때뿐 아니라 붙여넣기(Ctrl+V)할 때도
 * 공백 문자를 자동으로 제거한다.
 *
 * 사용 예) NoSpaceFilter.apply(txtPassword);
 */
public class NoSpaceFilter extends DocumentFilter {

    // 객체는 apply()를 통해서만 만든다
    private NoSpaceFilter() {
    }

    /**
     * 입력 컴포넌트에 공백 차단 필터를 적용한다.
     */
    public static void apply(JTextComponent field) {

        if (field.getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new NoSpaceFilter());
        }
    }

    // 글자를 새로 입력할 때
    @Override
    public void insertString(FilterBypass fb, int offset,
                             String text, AttributeSet attr)
            throws BadLocationException {

        super.insertString(fb, offset, removeSpaces(text), attr);
    }

    // 선택한 글자를 다른 글자로 바꿀 때 (붙여넣기 포함)
    @Override
    public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs)
            throws BadLocationException {

        super.replace(fb, offset, length, removeSpaces(text), attrs);
    }

    /**
     * 문자열에서 공백 문자를 모두 제거한다.
     */
    private static String removeSpaces(String text) {

        if (text == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(text.length());

        for (char ch : text.toCharArray()) {
            if (!Character.isWhitespace(ch)) {
                sb.append(ch);
            }
        }

        return sb.toString();
    }
}
