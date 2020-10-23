package ovh.alexisdelhaie.endpoint.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class InsertToTableDialog extends JDialog {

    public static final int WIDTH = 325;
    public static final int HEIGHT = 195;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField keyField;
    private JTextField valueField;
    private JLabel message;

    private boolean accepted = false;

    private InsertToTableDialog(String message) {
        setTitle("Insert");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.message.setText(message);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
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
        accepted = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static Optional<KeyValuePair> showDialog(String message) {
        InsertToTableDialog dialog = new InsertToTableDialog(message);
        dialog.setModal(true);
        dialog.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        dialog.setResizable(false);
        Tools.centerFrame(dialog);
        dialog.setVisible(true);
        if (dialog.accepted && !dialog.keyField.getText().isBlank()) {
            return Optional.of(new KeyValuePair(dialog.keyField.getText(), dialog.valueField.getText()));
        }
        return Optional.empty();
    }
}
