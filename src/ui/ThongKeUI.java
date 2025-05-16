package ui;

import dao.ReportDAO;
import model.SanPham;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.DefaultCategoryDataset;

import com.toedter.calendar.JDateChooser;

/**
 * Giao diện người dùng cho chức năng Thống kê và Báo cáo với thiết kế hiện đại.
 * Hiển thị các loại báo cáo (Doanh thu, Tồn kho, Sản phẩm bán chạy, Khách hàng mua nhiều nhất)
 * dưới dạng bảng và biểu đồ. Sử dụng JDateChooser để chọn thời gian.
 */
public class ThongKeUI extends JPanel {

    // Original earthy color palette
    private static final Color COFFEE_BROWN = new Color(111, 78, 55); // #6F4E37
    private static final Color LIGHT_BEIGE = new Color(245, 232, 199); // #F5E8C7
    private static final Color DARK_BEIGE = new Color(210, 180, 140); // #D2B48C
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color TABLE_ALTERNATE = new Color(237, 228, 208); // #EDE4D0

    // Font constants
    private static final Font HEADER_FONT = new Font("Open Sans", Font.BOLD, 16); // Giảm font size
    private static final Font LABEL_FONT = new Font("Open Sans", Font.PLAIN, 12); // Giảm font size
    private static final Font BUTTON_FONT = new Font("Open Sans", Font.BOLD, 12); // Giảm font size
    private static final Font TABLE_FONT = new Font("Open Sans", Font.PLAIN, 11); // Giảm font size

    // UI Components
    private JComboBox<String> cbReportType;
    private JPanel dateFilterPanel;
    private JDateChooser dateChooserStart;
    private JDateChooser dateChooserEnd;
    private JComboBox<String> cbPeriodType;
    private JButton btnViewReport;
    private JSplitPane splitPane;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JPanel chartPanelContainer;
    private JButton btnExportReport;
    private JButton btnPrintReport;
    private JPanel controlPanel;

    // Data Access Object
    private final ReportDAO reportDAO;

    // Current chart panel for export/print
    private ChartPanel currentChartPanel;

    // Current report data for export/print
    private ReportData currentReportData;

    /**
     * Constructor for ThongKeUI.
     */
    public ThongKeUI() {
        reportDAO = new ReportDAO();
        setLayout(new BorderLayout(15, 15)); // Giảm khoảng cách giữa các thành phần
        setBackground(LIGHT_BEIGE);
        setBorder(new EmptyBorder(15, 15, 15, 15)); // Giảm padding

        initializeComponents();
        initializeDefaultDates();
    }

    private void initializeComponents() {
        // Control Panel (Top)
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // Giảm khoảng cách
        controlPanel.setBackground(WHITE);
        controlPanel.setBorder(new TitledBorder(
                new LineBorder(COFFEE_BROWN, 2, true),
                "Tùy chọn Báo cáo",
                TitledBorder.LEFT, TitledBorder.TOP, HEADER_FONT, COFFEE_BROWN
        ));

        // Report Type
        controlPanel.add(createLabel("Loại Báo cáo:"));
        String[] reportTypes = {"Doanh thu", "Tồn kho", "Sản phẩm bán chạy", "Khách hàng mua nhiều nhất"};
        cbReportType = new JComboBox<>(reportTypes);
        cbReportType.setFont(LABEL_FONT);
        cbReportType.setBackground(WHITE);
        cbReportType.setForeground(COFFEE_BROWN);
        cbReportType.setBorder(BorderFactory.createLineBorder(DARK_BEIGE, 1));
        cbReportType.setPreferredSize(new Dimension(130, 25)); // Giảm kích thước
        controlPanel.add(cbReportType);

        // Date Filter Panel
        dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Giảm khoảng cách
        dateFilterPanel.setBackground(WHITE);

        dateFilterPanel.add(createLabel("Từ ngày:"));
        dateChooserStart = new JDateChooser();
        dateChooserStart.setDateFormatString("yyyy-MM-dd");
        dateChooserStart.setFont(LABEL_FONT);
        dateChooserStart.setBackground(WHITE);
        dateChooserStart.setForeground(COFFEE_BROWN);
        dateChooserStart.setPreferredSize(new Dimension(110, 25)); // Giảm kích thước
        styleDateChooser(dateChooserStart);
        dateFilterPanel.add(dateChooserStart);

        dateFilterPanel.add(createLabel("Đến ngày:"));
        dateChooserEnd = new JDateChooser();
        dateChooserEnd.setDateFormatString("yyyy-MM-dd");
        dateChooserEnd.setFont(LABEL_FONT);
        dateChooserEnd.setBackground(WHITE);
        dateChooserEnd.setForeground(COFFEE_BROWN);
        dateChooserEnd.setPreferredSize(new Dimension(110, 25)); // Giảm kích thước
        styleDateChooser(dateChooserEnd);
        dateFilterPanel.add(dateChooserEnd);

        dateFilterPanel.add(createLabel("Theo:"));
        String[] periodTypes = {"Ngày", "Tháng", "Năm"};
        cbPeriodType = new JComboBox<>(periodTypes);
        cbPeriodType.setFont(LABEL_FONT);
        cbPeriodType.setBackground(WHITE);
        cbPeriodType.setForeground(COFFEE_BROWN);
        cbPeriodType.setBorder(BorderFactory.createLineBorder(DARK_BEIGE, 1));
        cbPeriodType.setPreferredSize(new Dimension(80, 25)); // Giảm kích thước
        dateFilterPanel.add(cbPeriodType);

        controlPanel.add(dateFilterPanel);

        // Buttons
        btnViewReport = createModernButton("Xem Báo cáo", DARK_BEIGE, WHITE);
        controlPanel.add(btnViewReport);

        btnExportReport = createModernButton("Xuất Báo cáo", new Color(139, 69, 19), WHITE); // SaddleBrown for export
        btnExportReport.setEnabled(false);
        controlPanel.add(btnExportReport);

        btnPrintReport = createModernButton("In Báo cáo", DARK_BEIGE, WHITE);
        btnPrintReport.setEnabled(false);
        controlPanel.add(btnPrintReport);

        add(controlPanel, BorderLayout.NORTH);

        // Report Display Area (Center)
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500); // Giảm vị trí chia
        splitPane.setResizeWeight(0.5);
        splitPane.setBackground(LIGHT_BEIGE);
        splitPane.setDividerSize(8); // Giảm kích thước divider
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(new TitledBorder(
                new LineBorder(COFFEE_BROWN, 2, true),
                "Dữ liệu Báo cáo",
                TitledBorder.LEFT, TitledBorder.TOP, HEADER_FONT, COFFEE_BROWN
        ));

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        reportTable.setFont(TABLE_FONT);
        reportTable.setRowHeight(25); // Giảm chiều cao dòng
        reportTable.setShowGrid(true);
        reportTable.setGridColor(new Color(210, 180, 140));
        reportTable.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = reportTable.getTableHeader();
        tableHeader.setFont(HEADER_FONT);
        tableHeader.setBackground(COFFEE_BROWN);
        tableHeader.setForeground(WHITE);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 30)); // Giảm chiều cao header

        reportTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final DecimalFormat currencyFormat = new DecimalFormat("#,##0.##");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                comp.setFont(TABLE_FONT);
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? WHITE : TABLE_ALTERNATE);
                    comp.setForeground(COFFEE_BROWN);
                } else {
                    comp.setBackground(DARK_BEIGE);
                    comp.setForeground(WHITE);
                }

                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    if (table.getColumnName(column).contains("(VNĐ)")) {
                        setText(currencyFormat.format(value));
                    } else {
                        setText(value.toString());
                    }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setText(value != null ? value.toString() : "");
                }
                return comp;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tableScrollPane.setBackground(WHITE);
        tableScrollPane.getViewport().setBackground(WHITE);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Chart Panel
        chartPanelContainer = new JPanel(new BorderLayout());
        chartPanelContainer.setBackground(WHITE);
        chartPanelContainer.setBorder(new TitledBorder(
                new LineBorder(COFFEE_BROWN, 2, true),
                "Biểu đồ Báo cáo",
                TitledBorder.LEFT, TitledBorder.TOP, HEADER_FONT, COFFEE_BROWN
        ));
        JLabel placeholderLabel = new JLabel("Chọn loại báo cáo và nhấn 'Xem Báo cáo' để hiển thị biểu đồ.", SwingConstants.CENTER);
        placeholderLabel.setFont(LABEL_FONT);
        placeholderLabel.setForeground(COFFEE_BROWN);
        chartPanelContainer.add(placeholderLabel, BorderLayout.CENTER);

        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(chartPanelContainer);

        add(splitPane, BorderLayout.CENTER);

        // Event Listeners
        btnViewReport.addActionListener(e -> viewReport());
        btnExportReport.addActionListener(e -> exportReport());
        btnPrintReport.addActionListener(e -> printReport());
        cbReportType.addActionListener(e -> toggleDateFilterPanel());
        toggleDateFilterPanel();
    }

    private void initializeDefaultDates() {
        Calendar cal = Calendar.getInstance();
        dateChooserEnd.setDate(normalizeEndDate(new Date()));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dateChooserStart.setDate(normalizeStartDate(cal.getTime()));
    }

    private Date normalizeStartDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date normalizeEndDate(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(COFFEE_BROWN);
        return label;
    }

    private void styleDateChooser(JDateChooser dateChooser) {
        dateChooser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DARK_BEIGE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        dateChooser.setBackground(WHITE);
        Component editorComponent = dateChooser.getDateEditor().getUiComponent();
        if (editorComponent instanceof JTextField) {
            JTextField textField = (JTextField) editorComponent;
            textField.setFont(LABEL_FONT);
            textField.setBackground(WHITE);
            textField.setForeground(COFFEE_BROWN);
            textField.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
    }

    private JButton createModernButton(String text, Color baseColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, baseColor, 0, h, baseColor.brighter(), true);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 10, 10);
                g2d.setColor(getForeground());
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(BUTTON_FONT);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(100, 30)); // Giảm kích thước nút
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(baseColor.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(baseColor);
                }
            }
        });

        return button;
    }

    private void toggleDateFilterPanel() {
        String selectedReportType = (String) cbReportType.getSelectedItem();
        boolean needsDateFilter = "Doanh thu".equals(selectedReportType) || "Sản phẩm bán chạy".equals(selectedReportType) || "Khách hàng mua nhiều nhất".equals(selectedReportType);
        dateFilterPanel.setVisible(needsDateFilter);

        boolean isRevenueReport = "Doanh thu".equals(selectedReportType);
        cbPeriodType.setVisible(isRevenueReport);

        for (Component comp : dateFilterPanel.getComponents()) {
            if (comp instanceof JLabel && "Theo:".equals(((JLabel) comp).getText())) {
                comp.setVisible(isRevenueReport);
                break;
            }
        }

        controlPanel.revalidate();
        controlPanel.repaint();
        clearReportDisplay();
    }

    private void clearReportDisplay() {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        chartPanelContainer.removeAll();
        JLabel placeholderLabel = new JLabel("Chọn loại báo cáo và nhấn 'Xem Báo cáo' để hiển thị biểu đồ.", SwingConstants.CENTER);
        placeholderLabel.setFont(LABEL_FONT);
        placeholderLabel.setForeground(COFFEE_BROWN);
        chartPanelContainer.add(placeholderLabel, BorderLayout.CENTER);

        chartPanelContainer.revalidate();
        chartPanelContainer.repaint();
        currentChartPanel = null;
        currentReportData = null;
        btnExportReport.setEnabled(false);
        btnPrintReport.setEnabled(false);
    }

    private void viewReport() {
        String selectedReportType = (String) cbReportType.getSelectedItem();
        Date startDate = null;
        Date endDate = null;
        String periodType = null;

        if (dateFilterPanel.isVisible()) {
            startDate = dateChooserStart.getDate();
            endDate = dateChooserEnd.getDate();

            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.", "Lỗi Nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.", "Lỗi Nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            startDate = normalizeStartDate(startDate);
            endDate = normalizeEndDate(endDate);
        }

        if (cbPeriodType.isVisible()) {
            periodType = (String) cbPeriodType.getSelectedItem();
        }

        List<?> reportDataList = null;
        String chartTitle = "Biểu đồ Báo cáo";

        try {
            switch (selectedReportType) {
                case "Doanh thu":
                    reportDataList = reportDAO.getRevenueReport(startDate, endDate, periodType);
                    chartTitle = "Biểu đồ Doanh thu theo " + (periodType != null ? periodType.toLowerCase() : "thời gian");
                    break;
                case "Tồn kho":
                    reportDataList = reportDAO.getInventoryReport();
                    chartTitle = "Biểu đồ Tồn kho Sản phẩm";
                    break;
                case "Sản phẩm bán chạy":
                    reportDataList = reportDAO.getBestSellingProductsReport(startDate, endDate, 10);
                    chartTitle = "Biểu đồ Top Sản phẩm Bán chạy";
                    break;
                case "Khách hàng mua nhiều nhất":
                    reportDataList = reportDAO.getTopSpendingCustomersReport(startDate, endDate, 10);
                    chartTitle = "Biểu đồ Khách hàng Mua nhiều nhất";
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Loại báo cáo không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            if (reportDataList != null && !reportDataList.isEmpty()) {
                populateTable(reportDataList, selectedReportType);
                createAndDisplayChart(reportDataList, chartTitle, selectedReportType);

                btnExportReport.setEnabled(true);
                btnPrintReport.setEnabled(true);
                currentReportData = new ReportData(selectedReportType, reportDataList, chartTitle);
            } else {
                clearReportDisplay();
                JOptionPane.showMessageDialog(this, "Không có dữ liệu cho báo cáo này.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Error viewing report: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lấy dữ liệu: " + e.getMessage(), "Lỗi Báo cáo", JOptionPane.ERROR_MESSAGE);
            clearReportDisplay();
        }
    }

    private void populateTable(List<?> dataList, String reportType) {
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        String[] columns;
        switch (reportType) {
            case "Doanh thu":
                columns = new String[]{"Kỳ báo cáo", "Doanh thu (VNĐ)"};
                break;
            case "Tồn kho":
                columns = new String[]{"Mã SP", "Tên SP", "Mã loại", "Giá nhập (VNĐ)", "Số lượng tồn"};
                break;
            case "Sản phẩm bán chạy":
                columns = new String[]{"Mã SP", "Tên SP", "Số lượng bán", "Tổng tiền (VNĐ)"};
                break;
            case "Khách hàng mua nhiều nhất":
                columns = new String[]{"Mã KH", "Tên KH", "Tổng số lượng mua"};
                break;
            default:
                columns = new String[]{"Dữ liệu"};
                break;
        }
        tableModel.setColumnIdentifiers(columns);

        if (dataList == null) return;

        for (Object item : dataList) {
            if ("Doanh thu".equals(reportType) && item instanceof Object[] && ((Object[]) item).length >= 2) {
                tableModel.addRow((Object[]) item);
            } else if ("Tồn kho".equals(reportType) && item instanceof SanPham) {
                SanPham sp = (SanPham) item;
                double giaNhap = sp.getGianhap();
                tableModel.addRow(new Object[]{sp.getMaSP(), sp.getTenSP(), sp.getMaloai(), giaNhap, sp.getSoluong()});
            } else if ("Sản phẩm bán chạy".equals(reportType) && item instanceof Object[] && ((Object[]) item).length >= 4) {
                tableModel.addRow((Object[]) item);
            } else if ("Khách hàng mua nhiều nhất".equals(reportType) && item instanceof Object[] && ((Object[]) item).length >= 3) {
                tableModel.addRow((Object[]) item);
            }
        }
    }

    private void createAndDisplayChart(List<?> dataList, String chartTitle, String reportType) {
        chartPanelContainer.removeAll();
        JFreeChart chart = null;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String categoryAxisLabel = "Danh mục";
        String valueAxisLabel = "Giá trị";

        // Log data for debugging
        System.out.println("Creating chart for report: " + reportType);
        System.out.println("Data list size: " + (dataList != null ? dataList.size() : "null"));

        if (dataList != null && !dataList.isEmpty()) {
            if ("Doanh thu".equals(reportType)) {
                categoryAxisLabel = "Kỳ báo cáo (" + cbPeriodType.getSelectedItem().toString().toLowerCase() + ")";
                valueAxisLabel = "Doanh thu (VNĐ)";
                for (Object item : dataList) {
                    if (item instanceof Object[] && ((Object[]) item).length >= 2) {
                        Object[] row = (Object[]) item;
                        String period = row[0] != null ? row[0].toString() : "N/A";
                        Number revenue = row[1] instanceof Number ? (Number) row[1] : 0;
                        System.out.println("Adding revenue data: Period=" + period + ", Revenue=" + revenue);
                        dataset.addValue(revenue, "Doanh thu", period);
                    }
                }
                chart = ChartFactory.createLineChart(
                        chartTitle, categoryAxisLabel, valueAxisLabel, dataset,
                        PlotOrientation.VERTICAL, true, true, false
                );
                configureLineChart(chart);
            } else if ("Tồn kho".equals(reportType)) {
                categoryAxisLabel = "Sản phẩm";
                valueAxisLabel = "Số lượng tồn";
                for (Object item : dataList) {
                    if (item instanceof SanPham) {
                        SanPham sp = (SanPham) item;
                        String productName = sp.getTenSP() != null ? sp.getTenSP() : "Unknown";
                        int quantity = sp.getSoluong();
                        System.out.println("Adding inventory data: Product=" + productName + ", Quantity=" + quantity);
                        dataset.addValue(quantity, "Số lượng tồn", productName);
                    }
                }
                chart = ChartFactory.createBarChart(
                        chartTitle, categoryAxisLabel, valueAxisLabel, dataset,
                        PlotOrientation.VERTICAL, true, true, false
                );
                configureBarChart(chart);
            } else if ("Sản phẩm bán chạy".equals(reportType)) {
                categoryAxisLabel = "Sản phẩm";
                valueAxisLabel = "Số lượng bán";
                for (Object item : dataList) {
                    if (item instanceof Object[] && ((Object[]) item).length >= 3) {
                        Object[] row = (Object[]) item;
                        String tenSP = row[1] != null ? row[1].toString() : "Unknown";
                        Number quantitySold = row[2] instanceof Number ? (Number) row[2] : 0;
                        System.out.println("Adding best-selling data: Product=" + tenSP + ", Quantity Sold=" + quantitySold);
                        dataset.addValue(quantitySold, "Số lượng bán", tenSP);
                    }
                }
                chart = ChartFactory.createBarChart(
                        chartTitle, categoryAxisLabel, valueAxisLabel, dataset,
                        PlotOrientation.VERTICAL, true, true, false
                );
                configureBarChart(chart);
            } else if ("Khách hàng mua nhiều nhất".equals(reportType)) {
                categoryAxisLabel = "Khách hàng";
                valueAxisLabel = "Tổng số lượng mua";
                for (Object item : dataList) {
                    if (item instanceof Object[] && ((Object[]) item).length >= 3) {
                        Object[] row = (Object[]) item;
                        String tenKH = row[1] != null ? row[1].toString() : "Unknown";
                        Number quantityPurchased = row[2] instanceof Number ? (Number) row[2] : 0;
                        System.out.println("Adding customer data: Customer=" + tenKH + ", Quantity=" + quantityPurchased);
                        dataset.addValue(quantityPurchased, "Số lượng mua", tenKH);
                    }
                }
                chart = ChartFactory.createBarChart(
                        chartTitle, categoryAxisLabel, valueAxisLabel, dataset,
                        PlotOrientation.VERTICAL, true, true, false
                );
                configureBarChart(chart);
            }

            if (chart != null) {
                configureCommonChart(chart);
                currentChartPanel = new ChartPanel(chart);
                currentChartPanel.setPreferredSize(new Dimension(300, 200)); // Giảm kích thước biểu đồ
                currentChartPanel.setBackground(WHITE);
                currentChartPanel.setMouseWheelEnabled(true);
                currentChartPanel.setDomainZoomable(true);
                currentChartPanel.setRangeZoomable(true);
                chartPanelContainer.add(currentChartPanel, BorderLayout.CENTER);
                System.out.println("Chart created and added to panel.");
            }
        }

        if (chart == null) {
            JLabel noDataLabel = new JLabel("Không có dữ liệu để tạo biểu đồ.", SwingConstants.CENTER);
            noDataLabel.setFont(LABEL_FONT);
            noDataLabel.setForeground(COFFEE_BROWN);
            chartPanelContainer.add(noDataLabel, BorderLayout.CENTER);
            System.out.println("No chart created due to insufficient data.");
        }

        chartPanelContainer.revalidate();
        chartPanelContainer.repaint();
    }

    private void configureCommonChart(JFreeChart chart) {
        chart.setBackgroundPaint(WHITE);
        chart.getTitle().setFont(HEADER_FONT);
        chart.getTitle().setPaint(COFFEE_BROWN);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(LIGHT_BEIGE);
        plot.setRangeGridlinePaint(new Color(210, 180, 140));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(new Color(210, 180, 140));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLabelFont(LABEL_FONT);
        domainAxis.setTickLabelFont(TABLE_FONT);
        domainAxis.setLabelPaint(COFFEE_BROWN);
        domainAxis.setTickLabelPaint(COFFEE_BROWN);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(LABEL_FONT);
        rangeAxis.setTickLabelFont(TABLE_FONT);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLabelPaint(COFFEE_BROWN);
        rangeAxis.setTickLabelPaint(COFFEE_BROWN);
    }

    private void configureLineChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, DARK_BEIGE);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setBaseShapesVisible(true);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelFont(TABLE_FONT);
        renderer.setBaseItemLabelPaint(COFFEE_BROWN);
    }

    private void configureBarChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, DARK_BEIGE);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.02);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelFont(TABLE_FONT);
        renderer.setBaseItemLabelPaint(COFFEE_BROWN);
    }

    private void exportReport() {
        if (currentReportData == null || currentChartPanel == null || currentChartPanel.getChart() == null) {
            JOptionPane.showMessageDialog(this, "Không có báo cáo để xuất.", "Lỗi Xuất", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Xuất Báo cáo");
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG image (*.png)", "png");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.setFileFilter(pngFilter);

        String defaultFileName = "BaoCao_" + currentReportData.getReportType().replace(" ", "_") + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileChooser.setSelectedFile(new File(defaultFileName + ".png"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                filePath += ".png";
                fileToSave = new File(filePath);
            }

            try {
                org.jfree.chart.ChartUtilities.saveChartAsPNG(fileToSave, currentChartPanel.getChart(), 600, 400); // Giảm kích thước xuất
                JOptionPane.showMessageDialog(this, "Xuất thành công: " + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                System.err.println("Error exporting chart: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất: " + ex.getMessage(), "Lỗi Xuất", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printReport() {
        if (currentChartPanel == null || currentChartPanel.getChart() == null) {
            JOptionPane.showMessageDialog(this, "Không có biểu đồ để in.", "Lỗi In", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            currentChartPanel.createChartPrintJob();
            JOptionPane.showMessageDialog(this, "Yêu cầu in đã được gửi.", "In Báo cáo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            System.err.println("Error printing chart: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + ex.getMessage(), "Lỗi In", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Internal class to hold report data and type for export/print.
     */
    private static class ReportData {
        private final String reportType;
        private final List<?> dataList;
        private final String chartTitle;

        public ReportData(String reportType, List<?> dataList, String chartTitle) {
            this.reportType = reportType;
            this.dataList = dataList;
            this.chartTitle = chartTitle;
        }

        public String getReportType() { return reportType; }
        public List<?> getDataList() { return dataList; }
        public String getChartTitle() { return chartTitle; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Thống kê và Báo cáo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600); // Giảm kích thước tổng thể
            frame.setLocationRelativeTo(null);
            frame.add(new ThongKeUI());
            frame.setVisible(true);
        });
    }
}