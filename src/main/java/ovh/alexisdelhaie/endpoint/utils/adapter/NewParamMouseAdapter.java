package ovh.alexisdelhaie.endpoint.utils.adapter;

import ovh.alexisdelhaie.endpoint.url.URLGenerator;
import ovh.alexisdelhaie.endpoint.utils.Tools;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class NewParamMouseAdapter extends CustomNewMouseAdapter {

    private final JTextField urlField;

    public NewParamMouseAdapter(JTable table, JTextField urlField) {
        super(table);
        this.urlField = urlField;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (super.valid) {
            urlField.setText(URLGenerator.processNewUrl(Tools.tableToHashMap(super.table), urlField.getText()));
        }
    }

}
