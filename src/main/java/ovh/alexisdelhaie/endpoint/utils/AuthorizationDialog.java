package ovh.alexisdelhaie.endpoint.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.Optional;

public class AuthorizationDialog extends JDialog {

    public static final int WIDTH = 325;
    public static final int HEIGHT = 220;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> typeBox;
    private JPanel bearerPanel;
    private JTextField bearerTokenField;
    private JTextField usernameBasicField;
    private JTextField passwordBasicField;
    private JPanel basicPanel;

    private boolean accepted = false;
    private String finalValue = "";

    public AuthorizationDialog() {
        setTitle("Set authorization");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        typeBox.addItemListener((e) -> {
            String type = (String) typeBox.getSelectedItem();
            switch (Objects.requireNonNull(type)) {
                case "Basic" -> {
                    bearerPanel.setVisible(false);
                    basicPanel.setVisible(true);
                }
                case "Bearer" -> {
                    basicPanel.setVisible(false);
                    bearerPanel.setVisible(true);
                }
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String type = (String) typeBox.getSelectedItem();
        switch (Objects.requireNonNull(type)) {
            case "Basic" -> finalValue = Tools.toBase64(
                    String.format(
                            "%s:%s",
                            usernameBasicField.getText(),
                            passwordBasicField.getText()
                    )
            );
            case "Bearer" -> finalValue = String.format("Bearer %s", bearerTokenField.getText());
        }
        accepted = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static Optional<KeyValuePair> showDialog() {
        AuthorizationDialog dialog = new AuthorizationDialog();
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setResizable(false);
        Tools.centerFrame(dialog);
        dialog.setVisible(true);
        if (dialog.accepted) {
            return Optional.of(new KeyValuePair("authorization", dialog.finalValue));
        }
        return Optional.empty();
    }

}
