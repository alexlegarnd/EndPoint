package ovh.alexisdelhaie.endpoint.builder;

import javax.swing.table.DefaultTableModel;

public class ReadOnlyTableModel extends DefaultTableModel {

    public ReadOnlyTableModel(Object[][] objects, String[] headers) {
        super(objects, headers);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
