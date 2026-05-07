package iuh.fit.gui;

import iuh.fit.core.dto.BanDTO;
import iuh.fit.core.dto.ChiTietHoaDonDTO;
import iuh.fit.core.dto.HoaDonDTO;
import iuh.fit.core.entity.Ban;
import iuh.fit.core.entity.MonAn;
import iuh.fit.core.mapper.JsonMapper;
import iuh.fit.core.repository.ChiTietHoaDonRepository.ChiTietHoaDonItem;
import iuh.fit.core.service.BanService;
import iuh.fit.core.service.ChiTietHoaDonService;
import iuh.fit.core.service.HoaDonService;
import iuh.fit.core.service.MonAnService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManHinhGoiMonGUI extends JPanel {

    private final MonAnService monAnService = new MonAnService();
    private final BanService banService = new BanService();
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonService();
    private final HoaDonService hoaDonService = new HoaDonService();

    private Ban banHienTai;
    private HoaDonDTO activeHoaDon;

    private DefaultTableModel modelMon;
    private DefaultTableModel modelOrder;
    private JComboBox<String> cbBan;
    private JTextField txtMaDon;
    private JLabel lblTongTien;
    private JTable tblMon;
    private JTable tblOrder;

    public ManHinhGoiMonGUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadBan();
        loadMonAn();
    }

    public ManHinhGoiMonGUI(DanhSachBanGUI parent, String maNVDangNhap) {
        this();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Gọi món");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        p.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cbBan = new JComboBox<>();
        cbBan.setPreferredSize(new Dimension(220, 30));
        txtMaDon = new JTextField(16);
        txtMaDon.setToolTipText("Nhập mã đơn (maDon) cần lưu chi tiết");
        JButton btnLoad = new JButton("Nạp đơn");
        btnLoad.addActionListener(e -> napChiTietDon());
        JButton btnMoBan = new JButton("Mở bàn/Tạo đơn");
        btnMoBan.addActionListener(e -> moBanVaTaoDonTuBan());

        right.add(new JLabel("Bàn:"));
        right.add(cbBan);
        right.add(new JLabel("Mã đơn:"));
        right.add(txtMaDon);
        right.add(btnMoBan);
        right.add(btnLoad);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    private JSplitPane createBody() {
        modelMon = new DefaultTableModel(new String[]{"Mã món", "Tên món", "Đơn giá", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMon = new JTable();
        tblMon.setModel(modelMon);
        tblMon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) themMonVaoDon();
            }
        });
        JScrollPane left = new JScrollPane(tblMon);

        modelOrder = new DefaultTableModel(new String[]{"Mã món", "Tên món", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblOrder = new JTable(modelOrder);
        JScrollPane right = new JScrollPane(tblOrder);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.6);
        return split;
    }

    private JPanel createFooter() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnThem = new JButton("Thêm món");
        JButton btnXoa = new JButton("Xóa dòng");
        left.add(btnThem);
        left.add(btnXoa);
        btnThem.addActionListener(e -> themMonVaoDon());
        btnXoa.addActionListener(e -> xoaDongDon());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTongTien = new JLabel("Tổng: 0 VND");
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 15));
        JButton btnLuu = new JButton("Lưu chi tiết đơn");
        btnLuu.addActionListener(e -> luuChiTietDon());
        right.add(lblTongTien);
        right.add(btnLuu);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void loadBan() {
        cbBan.removeAllItems();
        for (BanDTO b : banService.getAllBan()) {
            cbBan.addItem(b.getMaBan() + " - " + b.getTenBan());
        }
    }

    private void loadMonAn() {
        List<MonAn> list = monAnService.findAll();
        modelMon.setRowCount(0);
        for (MonAn m : list) {
            modelMon.addRow(new Object[]{m.getMaMonAn(), m.getTenMon(), m.getDonGia(), m.getTrangThai()});
        }
    }

    private void themMonVaoDon() {
        int row = tblMon.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn món cần thêm");
            return;
        }

        String maMon = modelMon.getValueAt(row, 0).toString();
        String tenMon = modelMon.getValueAt(row, 1).toString();
        float donGia = ((Number) modelMon.getValueAt(row, 2)).floatValue();

        for (int i = 0; i < modelOrder.getRowCount(); i++) {
            if (maMon.equals(modelOrder.getValueAt(i, 0).toString())) {
                int sl = ((Number) modelOrder.getValueAt(i, 2)).intValue() + 1;
                modelOrder.setValueAt(sl, i, 2);
                modelOrder.setValueAt(sl * donGia, i, 4);
                capNhatTongTien();
                return;
            }
        }
        modelOrder.addRow(new Object[]{maMon, tenMon, 1, donGia, donGia});
        capNhatTongTien();
    }

    private void xoaDongDon() {
        int row = tblOrder.getSelectedRow();
        if (row >= 0) {
            modelOrder.removeRow(row);
            capNhatTongTien();
        }
    }

    private void capNhatTongTien() {
        float tong = 0;
        for (int i = 0; i < modelOrder.getRowCount(); i++) {
            tong += ((Number) modelOrder.getValueAt(i, 4)).floatValue();
        }
        lblTongTien.setText("Tổng: " + String.format("%,.0f", tong) + " VND");
    }

    public void updateBillPanelTotals() {
        capNhatTongTien();
    }

    private void napChiTietDon() {
        String maDon = txtMaDon.getText().trim();
        if (maDon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã đơn trước khi nạp");
            return;
        }

        // 1. Gọi đúng tên hàm và bọc maDon thành DTO
        ChiTietHoaDonDTO filterDTO = ChiTietHoaDonDTO.builder().maDon(maDon).build();
        List<ChiTietHoaDonDTO> ds = chiTietHoaDonService.getChiTietTheoMaDon(filterDTO);

        modelOrder.setRowCount(0);
        for (ChiTietHoaDonDTO ct : ds) {
            modelOrder.addRow(new Object[]{
                    // 2. Sử dụng các hàm Getter chuẩn của DTO
                    ct.getMaMonAn(),
                    ct.getTenMon(),
                    ct.getSoLuong(),
                    ct.getDonGia(),
                    ct.getThanhTien()
            });
        }
        capNhatTongTien();
    }

    private void luuChiTietDon() {
        String maDon = txtMaDon.getText().trim();
        if (maDon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã đơn (maDon)");
            return;
        }
        if (modelOrder.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Đơn chưa có món để lưu");
            return;
        }

        List<ChiTietHoaDonDTO> items = new ArrayList<>();
        for (int i = 0; i < modelOrder.getRowCount(); i++) {
            String maMon = modelOrder.getValueAt(i, 0).toString();
            int sl = ((Number) modelOrder.getValueAt(i, 2)).intValue();
            float dg = ((Number) modelOrder.getValueAt(i, 3)).floatValue();

            // 2. Dùng Builder của DTO để tạo object
            ChiTietHoaDonDTO dtoItem = ChiTietHoaDonDTO.builder()
                    .maMonAn(maMon)
                    .soLuong(sl)
                    .donGia(dg)
                    .build();

            items.add(dtoItem);
        }

        try {
            ChiTietHoaDonDTO donDTO = ChiTietHoaDonDTO.builder().maDon(maDon).build();

            // 3. Truyền vào hoàn toàn hợp lệ
            chiTietHoaDonService.replaceByMaDon(donDTO, items);

            JOptionPane.showMessageDialog(this, "Lưu chi tiết gọi món thành công");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lưu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moBanVaTaoDonTuBan() {
        String selected = (String) cbBan.getSelectedItem();
        if (selected == null || selected.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn");
            return;
        }

        String maBan = selected.split(" - ")[0].trim();
        String maNV = "NV01001";
        String maKH = null;

        try {
            this.banHienTai = JsonMapper.convert(banService.getBanByMa(maBan), Ban.class);
            var hd = hoaDonService.moBanVaTaoHoaDon(maBan, maNV, maKH, java.time.LocalDateTime.now(), "Tạo từ màn hình gọi món");
            if (hd != null && hd.getMaDon() != null) {
                this.activeHoaDon = hd;
                txtMaDon.setText(hd.getMaDon());
                napChiTietDon();
                JOptionPane.showMessageDialog(this, "Đã mở bàn/tạo đơn: " + hd.getMaDon());
            } else {
                txtMaDon.setText("DON" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                JOptionPane.showMessageDialog(this, "Không lấy được mã đơn từ hóa đơn, vui lòng kiểm tra dữ liệu.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Mở bàn thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean loadDuLieuBan(Ban banDuocChon) {
        if (banDuocChon == null) return false;
        this.banHienTai = banDuocChon;
        txtMaDon.setText("");
        return true;
    }

    public Ban getBanHienTai() {
        return banHienTai;
    }

    public HoaDonDTO getActiveHoaDon() {
        return activeHoaDon;
    }

    public DefaultTableModel getModelChiTietHoaDon() {
        return modelOrder;
    }

    public void xoaThongTinGoiMon() {
        if (modelOrder != null) {
            modelOrder.setRowCount(0);
        }
        if (txtMaDon != null) {
            txtMaDon.setText("");
        }
        banHienTai = null;
        activeHoaDon = null;
        updateBillPanelTotals();
    }
}
