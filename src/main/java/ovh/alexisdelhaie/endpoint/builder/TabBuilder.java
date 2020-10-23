package ovh.alexisdelhaie.endpoint.builder;

import ovh.alexisdelhaie.endpoint.utils.InsertToTableDialog;
import ovh.alexisdelhaie.endpoint.utils.KeyValuePair;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TabBuilder {

    private static HashMap<String, Component> indexes = new HashMap<>();

    public static void create(JTabbedPane tab, String label, HashMap<Integer, String> urls) {
        Component c = tab.add("", buildMainPanel());
        int index = tab.indexOfComponent(c);
        updateIndexes(index);
        tab.setTabComponentAt(index, buildTabPanel(tab, c, label, urls));
        tab.setSelectedComponent(c);
    }

    private static void updateIndexes(int index) {
        indexes.put("main[" + index + "].responseTextArea", indexes.get("main[waiting].responseTextArea"));
        indexes.remove("main[waiting].responseTextArea");
    }

    private static JPanel buildTabPanel(JTabbedPane tab, Component c, String label, HashMap<Integer, String> urls) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        p.add(l, g);
        g.gridx++;
        g.weightx = 0;
        p.add(buildCloseButton(tab, c, urls), g);
        return p;
    }

    private static JButton buildCloseButton(JTabbedPane tab, Component c, HashMap<Integer, String> urls) {
        JButton b = new JButton("×");
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                urls.remove(c.hashCode());
                tab.remove(c);
            }
        });
        return b;
    }

    private static JSplitPane buildMainPanel() {
        JTextArea t = new JTextArea();
        t.setBackground(Color.WHITE);
        t.setEditable(false);
        JScrollPane sp = new JScrollPane(t);
        indexes.put("main[waiting].responseTextArea", t);
        return new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            buildParametersTabbedPane(),
            sp
        );
    }

    private static JTabbedPane buildParametersTabbedPane() {
        JTabbedPane p = new JTabbedPane();
        p.add("Params", buildParamsTab());
        p.add("Authorization", new JPanel());
        p.add("Headers", buildParamsTab());
        p.add("Body", new JTextArea());
        return p;
    }

    private static JTable buildTable() {
        JTable t = new JTable();
        TableColumn keyCol = new TableColumn();
        keyCol.setHeaderValue("Keys");
        TableColumn valCol = new TableColumn();
        valCol.setHeaderValue("Values");
        t.addColumn(keyCol);
        t.addColumn(valCol);
        return t;
    }

    public static JTextArea getResponseArea(int index) {
        return (JTextArea) indexes.get("main[" + index + "].responseTextArea");
    }

    private static JPanel buildParamsTab() {
        String[] headers = {"Keys", "Values"};
        Object[][] datas = {};
        DefaultTableModel model = new DefaultTableModel(datas, headers);
        JPanel p = new JPanel();
        JTable t = new JTable(model);
        JButton addButton = new JButton("Add new parameter");
        JButton delButton = new JButton("Delete parameter");
        delButton.setEnabled(false);
        t.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                delButton.setEnabled(t.getSelectedRows().length > 0);
            }
        });
        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Optional<KeyValuePair> result = InsertToTableDialog.showDialog("Enter value");
                if (result.isPresent()) {
                    DefaultTableModel m = (DefaultTableModel) t.getModel();
                    m.addRow(new Object[]{result.get().getKey(), result.get().getValue()});
                }
            }
        });
        delButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int n = t.getSelectedRows().length;
                if (n > 0) {
                    DefaultTableModel m = (DefaultTableModel) t.getModel();
                    for(int i = 0; i < n; i++) {
                        m.removeRow(t.getSelectedRow());
                    }
                }
            }
        });
        p.add(addButton);
        p.add(delButton);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        JPanel pp = new JPanel();
        pp.add(p);
        JScrollPane sp = new JScrollPane(t);
        pp.add(sp);
        pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
        return pp;
    }

}
