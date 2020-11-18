package ovh.alexisdelhaie.endpoint.utils.adapter;

import ovh.alexisdelhaie.endpoint.utils.AuthorizationDialog;
import ovh.alexisdelhaie.endpoint.utils.KeyValuePair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class AuthorizationAdapter extends MouseAdapter {

    protected final JTable table;

    public AuthorizationAdapter(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        Optional<KeyValuePair> result = AuthorizationDialog.showDialog();
        if (result.isPresent()) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            for (int i = m.getRowCount() - 1; i > -1; i--) {
                String key = (String) m.getValueAt(i,0);
                if (key.toLowerCase().equals(result.get().getKey().toLowerCase())) {
                    m.removeRow(i);
                }
            }
            m.addRow(new Object[]{result.get().getKey(), result.get().getValue()});
        }
    }

}
