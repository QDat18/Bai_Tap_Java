package ui;

import dao.KhachHangDAO;
import model.KhachHang;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;

public class KhachHangCreationDialog extends JDialog {
    private JTextField txtMaKH;
    private JTextField txtTenKH;
    private JTextField txtSDT;
    private JButton btnSave;
    private JButton btnCancel;
    private KhachHangDAO khachHangDAO;
    private boolean savedSuccessfully = false;
    private String newMaKH;

    // Define colors
    private Color coffeeBrown = new Color(102, 51, 0);
    private Color lightBeige = new Color(245, 245, 220);
    private Color accentGreen = new Color(60, 179, 113);
    private Color darkGray = new Color(50, 50, 50);

    public KhachHangCreationDialog(JFrame owner) {
        super(owner, "Tạo Khách Hàng Mới", true);
        khachHangDAO = new KhachHangDAO();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(owner);

        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(lightBeige);
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // MaKH
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        contentPane.add(createLabel("Mã KH:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtMaKH = new JTextField(10);
        txtMaKH.setText(generateNextMaKH());
        txtMaKH.setEditable(false);
        contentPane.add(txtMaKH, gbc);

        // TenKH
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        contentPane.add(createLabel("Tên KH:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtTenKH = new JTextField(20);
        contentPane.add(txtTenKH, gbc);

        // SDT
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        contentPane.add(createLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        txtSDT = new JTextField(15);
        contentPane.add(txtSDT, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(lightBeige);
        btnSave = new JButton("Lưu");
        styleButton(btnSave, accentGreen, Color.WHITE);
        buttonPanel.add(btnSave);
        btnCancel = new JButton("Hủy");
        styleButton(btnCancel, darkGray, Color.WHITE);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        contentPane.add(buttonPanel, gbc);

        setContentPane(contentPane);

        // Event Listeners
        btnSave.addActionListener(e -> saveKhachHang());
        btnCancel.addActionListener(e -> dispose());

        setModal(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fgColor, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private String generateNextMaKH() {

        String lastMaKH = khachHangDAO.getLastMaKH();
        if (lastMaKH == null || lastMaKH.isEmpty()) {
            return "KH01";
        }
        try {
            int number = Integer.parseInt(lastMaKH.replace("KH", ""));
            return String.format("KH%02d", number + 1);
        } catch (NumberFormatException e) {
            return "KH01";
        }
    }

    private void saveKhachHang() {
        String maKH = txtMaKH.getText().trim();
        String tenKH = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim();

        if (tenKH.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập tên khách hàng.", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhachHang kh = new KhachHang();
        kh.setMaKH(maKH);
        kh.setTenkhach(tenKH);

        boolean success = khachHangDAO.saveKhachHang(kh);
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Thêm khách hàng thành công!", 
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            savedSuccessfully = true;
            newMaKH = maKH;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Thêm khách hàng thất bại.", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            savedSuccessfully = false;
        }
    }

    public boolean isSavedSuccessfully() {
        return savedSuccessfully;
    }

    public String getNewMaKH() {
        return newMaKH;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test");
            frame.setSize(200, 100);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            KhachHangCreationDialog dialog = new KhachHangCreationDialog(frame);
            dialog.setVisible(true);
        });
    }
}