package termproject;

import javax.swing.*;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Map;

public final class Main {
    private static final String LOCAL_URL = "jdbc:oracle:thin:@127.0.0.1:1521:XE";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) {
                // Swing can use the default look and feel.
            }

            MissionRepository repository = connectionSettings();
            if (repository != null) {
                new MissionFrame(repository).setVisible(true);
            }
        });
    }

    private static MissionRepository connectionSettings() {
        Map<String, String> env = System.getenv();
        String url = env.getOrDefault("AIL_DB_URL", LOCAL_URL);
        String user = env.getOrDefault("AIL_DB_USER", "");
        if (!user.isBlank() && env.containsKey("AIL_DB_PASSWORD")) {
            return new MissionRepository(url, user, env.get("AIL_DB_PASSWORD"));
        }

        JTextField urlField = new JTextField(url, 28);
        JTextField userField = new JTextField(user);
        JPasswordField passwordField = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("JDBC 주소"));
        form.add(urlField);
        form.add(new JLabel("Oracle 사용자"));
        form.add(userField);
        form.add(new JLabel("비밀번호"));
        form.add(passwordField);
        int choice = JOptionPane.showConfirmDialog(null, form, "Oracle 연결", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) {
            return null;
        }

        char[] password = passwordField.getPassword();
        try {
            return new MissionRepository(urlField.getText().strip(), userField.getText().strip(), new String(password));
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
