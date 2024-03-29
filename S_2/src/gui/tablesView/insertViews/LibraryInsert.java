package gui.tablesView.insertViews;

import controllers.TableController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class LibraryInsert extends JDialog implements InsertFrame {
    private final TableController tableController;
    private ArrayList<String> currValues;
    private final DefaultTableModel tableModel;

    public LibraryInsert(TableController tableController, DefaultTableModel tableModel){
        this.tableController = tableController;
        this.tableModel = tableModel;
    }

    @Override
    public void openInsertWindow() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        this.setBounds(dimension.width/2 - 250, dimension.height/2 - 200, 500, 400);
        this.setTitle("Добавление новой бибилиотеки");

        JPanel jPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        jPanel.setLayout(layout);
        this.add(jPanel);

        JLabel info = new JLabel("Введите данные для добавления новой библиотеки");
        info.setFont(new Font(info.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, info, 20, SpringLayout.NORTH, jPanel);
        layout.putConstraint(SpringLayout.WEST, info, 20, SpringLayout.WEST, jPanel);
        jPanel.add(info);

//        JLabel idLabel = new JLabel("Идентификатор бибилиотеки:");
//        idLabel.setFont(new Font(idLabel.getFont().getName(), Font.PLAIN, 16));
//        layout.putConstraint(SpringLayout.NORTH, idLabel, 50, SpringLayout.SOUTH, info);
//        layout.putConstraint(SpringLayout.WEST, idLabel, 20, SpringLayout.WEST, jPanel);
//        jPanel.add(idLabel);
//
//        JTextField idLibTextField = new JTextField(10);
//        idLibTextField.setFont(new Font(idLibTextField.getFont().getName(), Font.PLAIN, 16));
//        layout.putConstraint(SpringLayout.NORTH, idLibTextField, 10, SpringLayout.SOUTH, idLabel);
//        layout.putConstraint(SpringLayout.WEST, idLibTextField, 20, SpringLayout.WEST, jPanel);
//        jPanel.add(idLibTextField);


        JLabel quantityBooksLabel = new JLabel("Количество книг в фонде бибилиотеки:");
        quantityBooksLabel.setFont(new Font(quantityBooksLabel.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, quantityBooksLabel, 20, SpringLayout.SOUTH, info);
        layout.putConstraint(SpringLayout.WEST, quantityBooksLabel, 20, SpringLayout.WEST, jPanel);
        jPanel.add(quantityBooksLabel);

        JTextField quantityBooksTextField = new JTextField(10);
        quantityBooksTextField.setFont(new Font(quantityBooksTextField.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, quantityBooksTextField, 10, SpringLayout.SOUTH, quantityBooksLabel);
        layout.putConstraint(SpringLayout.WEST, quantityBooksTextField, 20, SpringLayout.WEST, jPanel);
        jPanel.add(quantityBooksTextField);


        JLabel nameLabel = new JLabel("Название библиотеки");
        nameLabel.setFont(new Font(nameLabel.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, nameLabel, 20, SpringLayout.SOUTH, quantityBooksTextField);
        layout.putConstraint(SpringLayout.WEST, nameLabel, 20, SpringLayout.WEST, jPanel);
        jPanel.add(nameLabel);

        JTextField nameTextField = new JTextField(30);
        nameTextField.setFont(new Font(nameTextField.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, nameTextField, 10, SpringLayout.SOUTH, nameLabel);
        layout.putConstraint(SpringLayout.WEST, nameTextField, 20, SpringLayout.WEST, jPanel);
        jPanel.add(nameTextField);


        JButton confirmInsert = new JButton("Добавить запись");
        confirmInsert.setFont(new Font(confirmInsert.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, confirmInsert, 50, SpringLayout.SOUTH, nameTextField);
        layout.putConstraint(SpringLayout.EAST, confirmInsert, -20, SpringLayout.EAST, jPanel);
        confirmInsert.addActionListener(e -> {
            currValues = new ArrayList<>();
            //currValues.add(idLibTextField.getText());
            currValues.add(quantityBooksTextField.getText());
            currValues.add(nameTextField.getText());
            String sql = "insert into LIBRARIES(QUANTITY_BOOKS, NAME) values (" + currValues.get(0) + ",'" + currValues.get(1) + "')";
            try {
                performInsertOperation(sql);
                quantityBooksTextField.setText("");
                nameTextField.setText("");
                Object[] values = new Object[]{tableController.getTableSet().getValueAt(
                        tableController.getTableSet().getRowCount() - 1, 0),
                        tableController.getTableSet().getValueAt(
                                tableController.getTableSet().getRowCount() - 1, 1),
                tableController.getTableSet().getValueAt(tableController.getTableSet().getRowCount()-1, 2)};
                tableModel.addRow(values);
                JLabel success = new JLabel("Запись добавлена успешно!");
                tableController.getConnection().getConn().createStatement().executeUpdate("COMMIT ");
                success.setFont(new Font(success.getFont().getName(), Font.BOLD, 16));
                JOptionPane.showMessageDialog(null, success, "INSERT", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException exception) {
                JLabel error = new JLabel();
                switch (exception.getErrorCode()){
                    case 936:{
                        error.setText("Ошибка добавленя записи! Незаполненные поля!");
                        break;
                    }
                    case 2290:{
                        error.setText("Ошибка добавления записи! Количество книг в библиотеке не может быть отрицательным!");
                        break;
                    }
                    default:{
                        error.setText(exception.getMessage());
                        break;
                    }
                }
                error.setFont(new Font(error.getFont().getName(), Font.BOLD, 16));
                JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
                try {
                    tableController.getConnection().getConn().createStatement().executeUpdate("ROLLBACK ");
                } catch (SQLException sqlException) {
                    error.setText(sqlException.getMessage());
                    JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        jPanel.add(confirmInsert);


        JButton cleanValues = new JButton("Очистить поля");
        cleanValues.setFont(new Font(cleanValues.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, cleanValues, 50, SpringLayout.SOUTH, nameTextField);
        layout.putConstraint(SpringLayout.WEST, cleanValues, 20, SpringLayout.WEST, jPanel);
        cleanValues.addActionListener(e -> {
            //idLibTextField.setText("");
            quantityBooksTextField.setText("");
            nameTextField.setText("");
        });
        jPanel.add(cleanValues);

        this.setModal(true);
        this.setResizable(false);
        this.setVisible(true);
    }

    @Override
    public void performInsertOperation(String sql) throws SQLException {
        tableController.insertNewRecord(sql);
    }
}
