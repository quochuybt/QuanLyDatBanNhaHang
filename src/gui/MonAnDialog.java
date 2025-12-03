package gui;

import dao.DanhMucMonDAO;
import entity.DanhMucMon;
import entity.MonAn;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class MonAnDialog extends JDialog {

    private JTextField txtTenMon, txtDonGia, txtDonViTinh;
    private JTextArea txtMoTa;
    private JComboBox<String> cboTrangThai;
    private JComboBox<DanhMucMonItem> cboDanhMuc;
    private JLabel lblHinhAnhPreview;
    private String selectedImagePath = "";
    private boolean succeeded = false;
    private MonAn monAn;
    private DanhMucMonDAO danhMucMonDAO = new DanhMucMonDAO();

    // Màu sắc
    private static final Color PRIMARY_COLOR = new Color(56, 118, 243);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);

    public MonAnDialog(Frame parent) {
        super(parent, "Thêm Món Ăn Mới", true);
        this.monAn = new MonAn();
        initUI();
    }

    public MonAnDialog(Frame parent, MonAn existingMonAn) {
        super(parent, "Chỉnh Sửa Món Ăn - " + existingMonAn.getMaMonAn(), true);
        this.monAn = existingMonAn;
        this.selectedImagePath = existingMonAn.getHinhAnh();
        initUI();
        fillData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0)); // Bỏ gap mặc định của layout chính
        setSize(700, 520); // Tăng kích thước chút cho thoáng
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(Color.WHITE); // Nền trắng toàn bộ

        // --- TITLE HEADER ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(245, 248, 255)); // Nền tiêu đề nhạt
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230))); // Viền dưới
        JLabel lblHeader = new JLabel(monAn.getMaMonAn() != null && monAn.getMaMonAn().length() > 3 ? "Thông tin món ăn" : "Thêm món ăn mới");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblHeader);
        add(headerPanel, BorderLayout.NORTH);

        // --- Panel Nhập liệu (Form) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Tăng khoảng cách giữa các dòng
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mã món
        JTextField txtMaMon = createStyledTextField();
        txtMaMon.setText(monAn.getMaMonAn());
        txtMaMon.setEditable(false);
        txtMaMon.setBackground(new Color(245, 245, 245)); // Xám nhẹ vì readonly
        addLabelAndField(formPanel, gbc, 0, "Mã món:", txtMaMon);

        // Tên món
        txtTenMon = createStyledTextField();
        addLabelAndField(formPanel, gbc, 1, "Tên món (*):", txtTenMon);

        // Danh mục
        cboDanhMuc = new JComboBox<>();
        cboDanhMuc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboDanhMuc.setBackground(Color.WHITE);
        loadCategories();
        addLabelAndField(formPanel, gbc, 2, "Danh mục:", cboDanhMuc);

        // Đơn giá
        txtDonGia = createStyledTextField();
        addLabelAndField(formPanel, gbc, 3, "Đơn giá (VNĐ):", txtDonGia);

        // Đơn vị tính
        txtDonViTinh = createStyledTextField();
        addLabelAndField(formPanel, gbc, 4, "Đơn vị tính:", txtDonViTinh);

        // Trạng thái
        cboTrangThai = new JComboBox<>(new String[]{"Còn", "Hết món"});
        cboTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboTrangThai.setBackground(Color.WHITE);
        addLabelAndField(formPanel, gbc, 5, "Trạng thái:", cboTrangThai);

        // Mô tả
        txtMoTa = new JTextArea(3, 20);
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMoTa.setLineWrap(true);
        txtMoTa.setBorder(new EmptyBorder(5, 5, 5, 5)); // Padding trong
        JScrollPane scrollMoTa = new JScrollPane(txtMoTa);
        scrollMoTa.setBorder(new LineBorder(new Color(200, 200, 200))); // Viền mỏng

        gbc.gridx = 0; gbc.gridy = 6; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblMota = new JLabel("Mô tả:");
        lblMota.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMota.setForeground(TEXT_COLOR);
        formPanel.add(lblMota, gbc);

        gbc.gridx = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollMoTa, gbc);

        // --- Panel Hình ảnh ---
        JPanel imagePanel = new JPanel(new BorderLayout(0, 10));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(new EmptyBorder(20, 0, 20, 20));
        imagePanel.setPreferredSize(new Dimension(200, 0));

        lblHinhAnhPreview = new JLabel("Chọn ảnh", SwingConstants.CENTER);
        lblHinhAnhPreview.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHinhAnhPreview.setForeground(Color.GRAY);
        // Viền nét đứt cho ảnh
        lblHinhAnhPreview.setBorder(BorderFactory.createDashedBorder(new Color(200, 200, 200), 2, 5));
        lblHinhAnhPreview.setOpaque(true);
        lblHinhAnhPreview.setBackground(new Color(250, 250, 250));

        JButton btnChonAnh = new JButton("Tải ảnh lên");
        styleSecondaryButton(btnChonAnh); // Style nút phụ
        btnChonAnh.addActionListener(e -> chooseImage());

        imagePanel.add(lblHinhAnhPreview, BorderLayout.CENTER);
        imagePanel.add(btnChonAnh, BorderLayout.SOUTH);

        // --- Panel Nút bấm (Lưu / Hủy) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230))); // Viền trên

        JButton btnSave = new JButton("Lưu thông tin");
        stylePrimaryButton(btnSave); // Nút chính

        JButton btnCancel = new JButton("Hủy bỏ");
        styleSecondaryButton(btnCancel); // Nút phụ

        btnSave.addActionListener(this::onSave);
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        // Add to Dialog
        add(formPanel, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- Helpers để Style ---

    private void addLabelAndField(JPanel p, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Chữ đậm
        lbl.setForeground(TEXT_COLOR);
        p.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(field, gbc);
    }

    private JTextField createStyledTextField() {
        JTextField txt = new JTextField(20);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(200, 35)); // Cao hơn để dễ nhìn
        // Tạo viền: Viền mỏng màu xám + Padding bên trong
        txt.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true), // Viền bo nhẹ
                new EmptyBorder(5, 10, 5, 10) // Padding text
        ));
        return txt;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20)); // Button to hơn

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_COLOR);
            }
        });
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(80, 80, 80));
        btn.setBackground(new Color(240, 240, 240)); // Nền xám nhạt
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(225, 225, 225));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(240, 240, 240));
            }
        });
    }

    // ... (Giữ nguyên logic loadCategories, fillData, chooseImage, onSave, helper class) ...

    private void loadCategories() {
        List<DanhMucMon> listDM = danhMucMonDAO.getAllDanhMuc();
        for (DanhMucMon dm : listDM) {
            cboDanhMuc.addItem(new DanhMucMonItem(dm));
        }
    }

    private void fillData() {
        txtTenMon.setText(monAn.getTenMon());
        txtDonGia.setText(String.format("%.0f", monAn.getDonGia()));
        txtDonViTinh.setText(monAn.getDonViTinh());
        txtMoTa.setText(monAn.getMota());
        cboTrangThai.setSelectedItem(monAn.getTrangThai());

        for (int i = 0; i < cboDanhMuc.getItemCount(); i++) {
            if (cboDanhMuc.getItemAt(i).getMaDM().equals(monAn.getMaDM())) {
                cboDanhMuc.setSelectedIndex(i);
                break;
            }
        }

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/img/MonAn/" + selectedImagePath));
                Image img = icon.getImage().getScaledInstance(160, 140, Image.SCALE_SMOOTH);
                lblHinhAnhPreview.setIcon(new ImageIcon(img));
                lblHinhAnhPreview.setText("");
                lblHinhAnhPreview.setBorder(null); // Bỏ viền nét đứt khi có ảnh
            } catch (Exception e) {
                lblHinhAnhPreview.setText("Lỗi ảnh");
            }
        }
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            this.selectedImagePath = file.getName();
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(160, 140, Image.SCALE_SMOOTH);
            lblHinhAnhPreview.setIcon(new ImageIcon(img));
            lblHinhAnhPreview.setText("");
            lblHinhAnhPreview.setBorder(null);
        }
    }

    private void onSave(ActionEvent e) {
        try {
            if(txtTenMon.getText().trim().isEmpty()) throw new Exception("Tên món không được rỗng");
            if(txtDonGia.getText().trim().isEmpty()) throw new Exception("Đơn giá không được rỗng");
            float donGia = Float.parseFloat(txtDonGia.getText());
            monAn.setTenMon(txtTenMon.getText().trim());
            monAn.setDonGia(donGia);
            monAn.setDonViTinh(txtDonViTinh.getText().trim());
            monAn.setTrangThai(cboTrangThai.getSelectedItem().toString());
            monAn.setMota(txtMoTa.getText().trim());
            monAn.setHinhAnh(selectedImagePath);
            DanhMucMonItem selectedCat = (DanhMucMonItem) cboDanhMuc.getSelectedItem();
            if(selectedCat != null) monAn.setMaDM(selectedCat.getMaDM());
            succeeded = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá phải là số!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public MonAn getMonAn() { return monAn; }

    class DanhMucMonItem {
        private DanhMucMon dm;
        public DanhMucMonItem(DanhMucMon dm) { this.dm = dm; }
        public String getMaDM() { return dm.getMadm(); }
        @Override public String toString() { return dm.getTendm(); }
    }
}