package ovh.alexisdelhaie.endpoint.utils.adapter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomDeleteMouseAdapter extends MouseAdapter {

    protected final JTable table;
    protected boolean valid = false;

    public CustomDeleteMouseAdapter(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        updateTable();
    }

    private void updateTable() {
        int n = table.getSelectedRows().length;
        if (n > 0) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            for(int i = 0; i < n; i++) {
                m.removeRow(table.getSelectedRow());
            }
            valid = true;
        }
    }

}
