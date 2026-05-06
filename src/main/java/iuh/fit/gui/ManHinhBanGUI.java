package iuh.fit.gui;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.service.BanService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManHinhBanGUI extends JPanel {

    private final BanService banService = new BanService();
    private JTable table;
    private DefaultTableModel model;

    public ManHinhBanGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(createHeader(), BorderLayout.NORTH);
        add(createMainTabs(), BorderLayout.CENTER);
        loadData();
    }

    private JTabbedPane createMainTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel danhSachPanel = new JPanel(new BorderLayout());
        danhSachPanel.setOpaque(false);
        danhSachPanel.add(createTable(), BorderLayout.CENTER);

        tabs.addTab("Danh sách bàn", danhSachPanel);
        tabs.addTab("Đặt bàn", new ManHinhDatBanGUI());
        tabs.addTab("Gọi món", new ManHinhGoiMonGUI());
        return tabs;
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("Danh sách bàn");
        lbl.setFont(new Font("Arial", Font.BOLD, 22));
        JButton refresh = new JButton("Làm mới");
        refresh.addActionListener(e -> loadData());
        p.add(lbl, BorderLayout.WEST);
        p.add(refresh, BorderLayout.EAST);
        return p;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(new String[]{"Mã", "Tên bàn", "Khu vực", "Số ghế", "Trạng thái", "Giờ mở"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        return new JScrollPane(table);
    }

    private void loadData() {
        List<BanDTO> ds = banService.getAllBan();
        model.setRowCount(0);
        for (BanDTO b : ds) {
            model.addRow(new Object[]{
                    b.getMaBan(),
                    b.getTenBan(),
                    b.getKhuVuc(),
                    b.getSoGhe(),
                    b.getTrangThai(),
                    b.getGioMoBan()
            });
        }
    }
}
