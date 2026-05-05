package iuh.fit.gui;

import iuh.fit.core.entity.PhanCong;
import iuh.fit.core.entity.VaiTro;
import iuh.fit.core.service.PhanCongService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LichLamViecGUI extends JPanel {

    private final PhanCongService phanCongService = new PhanCongService();
    private final VaiTro currentUserRole;

    private LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
    private JLabel lblWeek;
    private DefaultTableModel model;

    public LichLamViecGUI(VaiTro role) {
        this.currentUserRole = role;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        reloadData();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("CA LÀM");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        p.add(title, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        lblWeek = new JLabel();
        lblWeek.setFont(new Font("Arial", Font.BOLD, 16));
        center.add(prev);
        center.add(lblWeek);
        center.add(next);
        prev.addActionListener(e -> { weekStart = weekStart.minusWeeks(1); reloadData(); });
        next.addActionListener(e -> { weekStart = weekStart.plusWeeks(1); reloadData(); });
        p.add(center, BorderLayout.CENTER);

        if (currentUserRole == VaiTro.QUANLY) {
            JButton assign = new JButton("Phân ca");
            assign.addActionListener(e -> {
                AssignShiftDialog d = new AssignShiftDialog((Frame) SwingUtilities.getWindowAncestor(this));
                d.setVisible(true);
                reloadData();
            });
            p.add(assign, BorderLayout.EAST);
        }

        return p;
    }

    private JScrollPane createTable() {
        model = new DefaultTableModel(new String[]{"Ngày", "Ca", "Nhân viên", "Mã NV"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        return new JScrollPane(table);
    }

    private void reloadData() {
        LocalDate end = weekStart.plusDays(6);
        lblWeek.setText(String.format("Tuần: %s - %s",
                weekStart.format(DateTimeFormatter.ofPattern("dd/MM")),
                end.format(DateTimeFormatter.ofPattern("dd/MM"))));

        List<PhanCong> data = phanCongService.findAll().stream()
                .filter(pc -> pc.getNgayLam() != null && !pc.getNgayLam().isBefore(weekStart) && !pc.getNgayLam().isAfter(end))
                .sorted((a, b) -> {
                    int cmpDate = a.getNgayLam().compareTo(b.getNgayLam());
                    if (cmpDate != 0) return cmpDate;
                    return a.getCaLam().getGioBatDau().compareTo(b.getCaLam().getGioBatDau());
                })
                .collect(Collectors.toList());

        model.setRowCount(0);
        Locale vi = new Locale("vi", "VN");
        for (PhanCong pc : data) {
            String ngay = pc.getNgayLam().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, vi)
                    + " - " + pc.getNgayLam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String ca = pc.getCaLam().getTenCa() + " (" + pc.getCaLam().getGioBatDau() + " - " + pc.getCaLam().getGioKetThuc() + ")";
            model.addRow(new Object[]{ngay, ca, pc.getNhanVien().getHoten(), pc.getNhanVien().getManv()});
        }
    }
}
