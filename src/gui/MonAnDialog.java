package gui;

import dao.DanhMucMonDAO;
import dao.MonAnDAO;
import entity.DanhMucMon;
import entity.MonAn;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class MonAnDialog extends JDialog {

    private JTextField txtMaMon, txtTenMon, txtDonGia, txtDonViTinh;
    private JTextArea txtMoTa;
    private JComboBox<String> cboTrangThai;
    private JComboBox<DanhMucMon> cboDanhMuc;
    private JLabel lblHinhAnhPreview;
    private String selectedImageFileName = "";
    private boolean succeeded = false;
    private MonAn monAn;

    private DanhMucMonDAO danhMucMonDAO = new DanhMucMonDAO();
    private MonAnDAO monAnDAO = new MonAnDAO();

    public MonAnDialog(Frame parent) {
        super(parent, "Thêm Món Ăn", true);
        this.monAn = new MonAn();
        this.monAn.setMaMonAn(monAnDAO.getNextMaMonAn());
        initUI();
    }

    public MonAnDialog(Frame parent, MonAn existingMonAn) {
        super(parent, "Cập Nhật Món Ăn", true);
        this.monAn = existingMonAn;
        this.selectedImageFileName = existingMonAn.getHinhAnh();
        initUI();
        fillData();
    }

    private void initUI() {
        setSize(800, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaMon = addField(pnlForm, gbc, 0, "Mã món:", false);
        txtMaMon.setText(monAn.getMaMonAn());
        txtTenMon = addField(pnlForm, gbc, 1, "Tên món (*):", true);

        gbc.gridx=0; gbc.gridy=2; pnlForm.add(new JLabel("Danh mục:"), gbc);
        cboDanhMuc = new JComboBox<>();
        loadCategories();
        gbc.gridx=1; pnlForm.add(cboDanhMuc, gbc);

        txtDonGia = addField(pnlForm, gbc, 3, "Đơn giá:", true);
        txtDonViTinh = addField(pnlForm, gbc, 4, "Đơn vị tính:", true);

        gbc.gridx=0; gbc.gridy=5; pnlForm.add(new JLabel("Trạng thái:"), gbc);
        cboTrangThai = new JComboBox<>(new String[]{"Còn", "Hết món"});
        gbc.gridx=1; pnlForm.add(cboTrangThai, gbc);

        gbc.gridx=0; gbc.gridy=6; pnlForm.add(new JLabel("Mô tả:"), gbc);
        txtMoTa = new JTextArea(3, 20);
        txtMoTa.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx=1; pnlForm.add(new JScrollPane(txtMoTa), gbc);

        add(pnlForm, BorderLayout.CENTER);

        JPanel pnlImage = new JPanel(new BorderLayout(0, 10));
        pnlImage.setBackground(Color.WHITE);
        pnlImage.setBorder(new EmptyBorder(20, 0, 20, 20));
        pnlImage.setPreferredSize(new Dimension(250, 0));

        lblHinhAnhPreview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblHinhAnhPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton btnUpload = new JButton("Chọn ảnh");
        btnUpload.addActionListener(e -> chooseImage());

        pnlImage.add(lblHinhAnhPreview, BorderLayout.CENTER);
        pnlImage.add(btnUpload, BorderLayout.SOUTH);
        add(pnlImage, BorderLayout.EAST);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.setBackground(new Color(56, 118, 243));
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(this::onSave);
        btnCancel.addActionListener(e -> dispose());

        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        add(pnlBtn, BorderLayout.SOUTH);
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
                "Hình ảnh (JPG, PNG, GIF, BMP)",
                "jpg", "jpeg", "png", "gif", "bmp"
        ));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File srcFile = fc.getSelectedFile();

            if (srcFile == null || !srcFile.exists()) {
                JOptionPane.showMessageDialog(this, "File không tồn tại hoặc chưa chọn file!");
                return;
            }

            String projectPath = System.getProperty("user.dir");
            File destFolder = new File(projectPath + "/resources/img/MonAn");
            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }

            String newName = System.currentTimeMillis() + "_" + srcFile.getName();
            File destFile = new File(destFolder, newName);

            try {
                Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.selectedImageFileName = newName;
                displayImage(destFile.getAbsolutePath());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi copy ảnh: " + e.getMessage());
            }
        }
    }

    private void displayImage(String pathOrName) {
        if (pathOrName == null || pathOrName.isEmpty()) {
            lblHinhAnhPreview.setIcon(null);
            lblHinhAnhPreview.setText("Chưa có ảnh");
            return;
        }

        ImageIcon icon = null;
        try {
            File f = new File(pathOrName);
            if (f.exists() && f.isAbsolute()) {
                icon = new ImageIcon(f.getAbsolutePath());
            } else {
                String localPath = System.getProperty("user.dir") + "/resources/img/MonAn/" + pathOrName;
                File localFile = new File(localPath);
                if (localFile.exists()) {
                    icon = new ImageIcon(localPath);
                } else {
                    java.net.URL imgURL = getClass().getResource("/img/MonAn/" + pathOrName);
                    if (imgURL != null) icon = new ImageIcon(imgURL);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(230, 180, Image.SCALE_SMOOTH);
            lblHinhAnhPreview.setIcon(new ImageIcon(img));
            lblHinhAnhPreview.setText("");
        } else {
            lblHinhAnhPreview.setIcon(null);
            lblHinhAnhPreview.setText("Ảnh lỗi/Không tìm thấy");
        }
    }

    private void onSave(ActionEvent e) {
        try {
            if (txtTenMon.getText().trim().isEmpty()) {
                throw new Exception("Tên món không được để trống!");
            }
            if (txtDonGia.getText().trim().isEmpty()) {
                throw new Exception("Đơn giá không được để trống!");
            }

            float giaBan = Float.parseFloat(txtDonGia.getText().trim());

            if (giaBan < 0) {
                throw new Exception("Giá bán không được âm!");
            }

            monAn.setTenMon(txtTenMon.getText().trim());
            monAn.setDonGia(giaBan);
            monAn.setDonViTinh(txtDonViTinh.getText().trim());
            monAn.setTrangThai(cboTrangThai.getSelectedItem().toString());
            monAn.setMota(txtMoTa.getText().trim());
            monAn.setHinhAnh(selectedImageFileName);

            DanhMucMon dm = (DanhMucMon) cboDanhMuc.getSelectedItem();
            if (dm != null) monAn.setMaDM(dm.getMadm());

            succeeded = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá phải là số hợp lệ!", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JTextField addField(JPanel p, GridBagConstraints gbc, int row, String lbl, boolean edit) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        p.add(new JLabel(lbl), gbc);

        JTextField txt = new JTextField(15);
        txt.setEditable(edit);
        if(!edit) txt.setBackground(new Color(240,240,240));

        gbc.gridx = 1; gbc.weightx = 1;
        p.add(txt, gbc);
        return txt;
    }

    private void loadCategories() {
        for (DanhMucMon dm : danhMucMonDAO.getAllDanhMuc()) {
            cboDanhMuc.addItem(dm);
        }
    }

    private void fillData() {
        txtMaMon.setText(monAn.getMaMonAn());
        txtTenMon.setText(monAn.getTenMon());
        txtDonGia.setText(String.format("%.0f", monAn.getDonGia()));
        txtDonViTinh.setText(monAn.getDonViTinh());
        txtMoTa.setText(monAn.getMota());
        cboTrangThai.setSelectedItem(monAn.getTrangThai());

        for (int i=0; i<cboDanhMuc.getItemCount(); i++) {
            if (cboDanhMuc.getItemAt(i).getMadm().equals(monAn.getMaDM())) {
                cboDanhMuc.setSelectedIndex(i); break;
            }
        }
        displayImage(selectedImageFileName);
    }

    public boolean isSucceeded() { return succeeded; }
    public MonAn getMonAn() { return monAn; }
}