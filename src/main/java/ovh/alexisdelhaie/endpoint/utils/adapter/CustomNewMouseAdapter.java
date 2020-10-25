package ovh.alexisdelhaie.endpoint.utils.adapter;

import ovh.alexisdelhaie.endpoint.utils.InsertToTableDialog;
import ovh.alexisdelhaie.endpoint.utils.KeyValuePair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class CustomNewMouseAdapter extends MouseAdapter {

    protected final JTable table;
    protected boolean valid = false;

    public CustomNewMouseAdapter(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        updateTable();
    }

    private void updateTable() {
        Optional<KeyValuePair> result = InsertToTableDialog.showDialog("Enter value");
        if (result.isPresent()) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.addRow(new Object[]{result.get().getKey(), result.get().getValue()});
            valid = true;
        }
    }

}
