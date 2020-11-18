package ovh.alexisdelhaie.endpoint.builder;

import ovh.alexisdelhaie.endpoint.utils.RequestTab;
import ovh.alexisdelhaie.endpoint.utils.Tools;
import ovh.alexisdelhaie.endpoint.utils.adapter.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TabBuilder {

    public static final ConcurrentHashMap<String, Component> indexes = new ConcurrentHashMap<>();

    public static void create(JTabbedPane tab, String label, ConcurrentHashMap<Integer, RequestTab> tabs, JTextField urlField) {
        Component c = tab.add("", buildMainPanel(urlField));
        int index = tab.indexOfComponent(c);
        tab.setTabComponentAt(index, buildTabPanel(tab, c, label, tabs));
        tab.setSelectedComponent(c);
        updateIndexes(index);
    }

    private static void updateIndexes(int index) {
        indexes.put("main[" + index + "].responseTextArea", indexes.get("main[waiting].responseTextArea"));
        indexes.put("main[" + index + "].body", indexes.get("main[waiting].body"));
        indexes.put("main[" + index + "].params", indexes.get("main[waiting].params"));
        indexes.put("main[" + index + "].headers", indexes.get("main[waiting].headers"));
        indexes.put("main[" + index + "].tabTitle", indexes.get("main[waiting].tabTitle"));
        indexes.put("main[" + index + "].requestHeaderTextArea", indexes.get("main[waiting].requestHeaderTextArea"));
        indexes.put("main[" + index + "].responseHeaderTextArea", indexes.get("main[waiting].responseHeaderTextArea"));
        indexes.remove("main[waiting].responseTextArea");
        indexes.remove("main[waiting].body");
        indexes.remove("main[waiting].params");
        indexes.remove("main[waiting].headers");
        indexes.remove("main[waiting].tabTitle");
        indexes.remove("main[waiting].requestHeaderTextArea");
        indexes.remove("main[waiting].responseHeaderTextArea");
    }

    private static JPanel buildTabPanel(JTabbedPane tab, Component c, String label, ConcurrentHashMap<Integer, RequestTab> tabs) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        indexes.put("main[waiting].tabTitle", l);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        p.add(l, g);
        g.gridx++;
        g.weightx = 0;
        p.add(buildCloseButton(tab, c, tabs), g);
        return p;
    }

    private static JButton buildCloseButton(JTabbedPane tab, Component c, ConcurrentHashMap<Integer, RequestTab> tabs) {
        JButton b = new JButton("×");
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                tabs.remove(c.hashCode());
                tab.remove(c);
            }
        });
        return b;
    }

    private static JSplitPane buildMainPanel(JTextField urlField) {
        JTextArea t = new JTextArea();
        t.setBackground(Color.WHITE);
        t.setEditable(false);
        JScrollPane sp = new JScrollPane(t);
        indexes.put("main[waiting].responseTextArea", t);
        return new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            buildParametersTabbedPane(urlField),
            sp
        );
    }

    private static JTabbedPane buildParametersTabbedPane(JTextField urlField) {
        JTabbedPane p = new JTabbedPane();
        p.add("Params", buildParamsTab(true, urlField));
        p.add("Headers", buildParamsTab(false, null));
        JTextArea body = new JTextArea();
        indexes.put("main[waiting].body", body);
        p.add("Body", body);
        // Response tab
        JTextArea repHeader = new JTextArea();
        repHeader.setBackground(Color.WHITE);
        repHeader.setEditable(false);
        JScrollPane rep = new JScrollPane(repHeader);
        JTextArea reqHeader = new JTextArea();
        reqHeader.setBackground(Color.WHITE);
        reqHeader.setEditable(false);
        JScrollPane req = new JScrollPane(reqHeader);
        indexes.put("main[waiting].requestHeaderTextArea", reqHeader);
        indexes.put("main[waiting].responseHeaderTextArea", repHeader);
        p.add("Response", new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                rep,
                req
        ));
        return p;
    }

    public static JTextArea getResponseArea(int index) {
        return (JTextArea) indexes.get("main[" + index + "].responseTextArea");
    }

    public static JTextArea getBody(int index) {
        return (JTextArea) indexes.get("main[" + index + "].body");
    }

    public static JTextArea getRequestHeaderTextArea(int index) {
        return (JTextArea) indexes.get("main[" + index + "].requestHeaderTextArea");
    }

    public static JTextArea getResponseHeaderTextArea(int index) {
        return (JTextArea) indexes.get("main[" + index + "].responseHeaderTextArea");
    }

    public static HashMap<String, String> getParams(int index) {
        JTable t = (JTable) indexes.get("main[" + index + "].params");
        return Tools.tableToHashMap(t);
    }

    public static JTable getParamsTable(int index) {
        return (JTable) indexes.get("main[" + index + "].params");
    }

    public static HashMap<String, String> getHeaders(int index) {
        JTable t = (JTable) indexes.get("main[" + index + "].headers");
        return Tools.tableToHashMap(t);
    }

    public static JLabel getLabel(int index) {
        return (JLabel) indexes.get("main[" + index + "].tabTitle");
    }

    private static JPanel buildParamsTab(boolean isParam, JTextField urlField) {
        String[] headers = {"Keys", "Values"};
        DefaultTableModel model = (isParam)? new ReadOnlyTableModel(new Object[][]{}, headers) :
                new DefaultTableModel(new Object[][]{}, headers);
        JPanel p = new JPanel();
        JTable t = new JTable(model);
        indexes.put((isParam) ? "main[waiting].params" : "main[waiting].headers", t);
        JButton addButton = new JButton("New");
        JButton delButton = new JButton("Remove");
        delButton.setEnabled(false);
        t.getSelectionModel().addListSelectionListener(event -> delButton.setEnabled(t.getSelectedRows().length > 0));
        p.add(addButton);
        p.add(delButton);
        if (isParam) {
            addButton.addMouseListener(new NewParamMouseAdapter(t, urlField));
            delButton.addMouseListener(new DeleteParamMouseAdapter(t, urlField));
        } else {
            addButton.addMouseListener(new CustomNewMouseAdapter(t));
            delButton.addMouseListener(new CustomDeleteMouseAdapter(t));
            JButton authButton = new JButton("Set authorization");
            authButton.addMouseListener(new AuthorizationAdapter(t));
            p.add(authButton);
        }
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        pp.add(p);
        JScrollPane sp = new JScrollPane(t);
        pp.add(sp);
        pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
        return pp;
    }

}
