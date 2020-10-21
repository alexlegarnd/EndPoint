package ovh.alexisdelhaie.endpoint.builder;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class TabBuilder {

    private static HashMap<String, Component> indexes = new HashMap<>();

    public static void create(JTabbedPane tab, String label) {
        Component c = tab.add("", buildMainPanel());
        int index = tab.indexOfComponent(c);
        updateIndexes(index);
        tab.setTabComponentAt(index, buildTabPanel(tab, c, label));
        tab.setSelectedComponent(c);
    }

    private static void updateIndexes(int index) {
        indexes.put("main[" + index + "].responseTextArea", indexes.get("main[waiting].responseTextArea"));
        indexes.remove("main[waiting].responseTextArea");
    }

    private static JPanel buildTabPanel(JTabbedPane tab, Component c, String label) {
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
        p.add(buildCloseButton(tab, c), g);
        return p;
    }

    private static JButton buildCloseButton(JTabbedPane tab, Component c) {
        JButton b = new JButton("×");
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
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
        p.add("Params", buildTable());
        p.add("Authorization", new JPanel());
        p.add("Headers", buildTable());
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

}
