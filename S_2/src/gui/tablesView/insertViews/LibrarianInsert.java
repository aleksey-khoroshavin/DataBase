package gui.tablesView.insertViews;

import controllers.TableController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LibrarianInsert extends JDialog implements InsertFrame {
    private final TableController tableController;
    private ArrayList<String> currValues;
    private final DefaultTableModel tableModel;

    public LibrarianInsert(TableController tableController, DefaultTableModel tableModel) {
        this.tableController = tableController;
        this.tableModel = tableModel;
    }

    @Override
    public void openInsertWindow() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        this.setBounds(dimension.width/2 - 250, dimension.height/2 - 300, 500, 600);
        this.setTitle("Добавление нового сотрудника бибилиотеки");

        JPanel jPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        jPanel.setLayout(layout);
        this.add(jPanel);

        JLabel info = new JLabel("Введите данные для регистрации библиотекаря");
        info.setFont(new Font(info.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, info, 20, SpringLayout.NORTH, jPanel);
        layout.putConstraint(SpringLayout.WEST, info, 20, SpringLayout.WEST, jPanel);
        jPanel.add(info);

        JLabel pwdLabel = new JLabel("Пароль пользователя");
        pwdLabel.setFont(new Font(pwdLabel.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, pwdLabel, 50, SpringLayout.SOUTH, info);
        layout.putConstraint(SpringLayout.WEST, pwdLabel, 20, SpringLayout.WEST, jPanel);
        jPanel.add(pwdLabel);

        JTextField pwdTextField = new JTextField(10);
        pwdTextField.setFont(new Font(pwdTextField.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, pwdTextField, 10, SpringLayout.SOUTH, pwdLabel);
        layout.putConstraint(SpringLayout.WEST, pwdTextField, 20, SpringLayout.WEST, jPanel);
        jPanel.add(pwdTextField);

        JLabel idLibLabel = new JLabel("Идентификатор библиотеки");
        idLibLabel.setFont(new Font(idLibLabel.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, idLibLabel, 20, SpringLayout.SOUTH, pwdTextField);
        layout.putConstraint(SpringLayout.WEST, idLibLabel, 20, SpringLayout.WEST, jPanel);
        jPanel.add(idLibLabel);

        JTextField idLibTexField = new JTextField(10);
        idLibTexField.setFont(new Font(idLibTexField.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, idLibTexField, 10, SpringLayout.SOUTH, idLibLabel);
        layout.putConstraint(SpringLayout.WEST, idLibTexField, 20, SpringLayout.WEST, jPanel);
        jPanel.add(idLibTexField);

        JLabel hallNumLabel = new JLabel("Номер зала сотрудника");
        hallNumLabel.setFont(new Font(hallNumLabel.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, hallNumLabel, 20, SpringLayout.SOUTH, idLibTexField);
        layout.putConstraint(SpringLayout.WEST, hallNumLabel, 20, SpringLayout.WEST, jPanel);
        jPanel.add(hallNumLabel);

        JTextField hallNumTexFiled = new JTextField(10);
        hallNumTexFiled.setFont(new Font(hallNumTexFiled.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, hallNumTexFiled, 10, SpringLayout.SOUTH, hallNumLabel);
        layout.putConstraint(SpringLayout.WEST, hallNumTexFiled, 20, SpringLayout.WEST, jPanel);
        jPanel.add(hallNumTexFiled);

        JButton confirm = new JButton("Добавить сотрудника");
        confirm.setFont(new Font(confirm.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.SOUTH, confirm, -10, SpringLayout.SOUTH, jPanel);
        layout.putConstraint(SpringLayout.EAST, confirm, -20, SpringLayout.EAST, jPanel);
        confirm.addActionListener(e->{
            currValues = new ArrayList<>();

            //for USERS
            currValues.add(pwdTextField.getText());

            //for LIBRARIANS
            currValues.add(idLibTexField.getText());
            currValues.add(hallNumTexFiled.getText());

            String sql1 = "insert into USERS(password, user_mod) values ('"+currValues.get(0)+"', 'Библиотекарь')";

            try {
                performInsertOperation(sql1);

                PreparedStatement preStatement = tableController.getConnection().getConn().prepareStatement(
                        "select user_id from users where password = '" + currValues.get(0) + "'");
                ResultSet resultSet = preStatement.executeQuery();

                int newUserId = 0;
                if(resultSet.next()){
                    newUserId = resultSet.getInt(1);
                }

                String sql2 = "insert into LIBRARIANS values ("+ newUserId + "," + currValues.get(1) + "," + currValues.get(2) + ")";
                performInsertOperation(sql2);

                idLibTexField.setText("");
                hallNumTexFiled.setText("");

                Object[] values = new Object[]{tableController.getTableSet().getValueAt(
                        tableController.getTableSet().getRowCount() - 1, 0), tableController.getTableSet().getValueAt(
                        tableController.getTableSet().getRowCount() - 1, 1), tableController.getTableSet().getValueAt(
                        tableController.getTableSet().getRowCount() - 1, 2)};
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
                        error.setText("Ошибка добавления записи! Номер зала есть положительное число!");
                        break;
                    }
                    case 2291:{
                        error.setText("Ошибка добваления записи! Нет библиотеки с таким ID!");
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
        jPanel.add(confirm);

        JButton clear = new JButton("Очистить поля");
        clear.setFont(new Font(clear.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.SOUTH, clear, -20, SpringLayout.NORTH, confirm);
        layout.putConstraint(SpringLayout.EAST, clear, -20, SpringLayout.EAST, jPanel);
        clear.addActionListener(e -> {
            //idTextField.setText("");
            idLibTexField.setText("");
            hallNumTexFiled.setText("");
        });
        jPanel.add(clear);


        this.setResizable(false);
        this.setModal(true);
        this.setVisible(true);
    }

    @Override
    public void performInsertOperation(String sql) throws SQLException {
        tableController.insertNewRecord(sql);
    }
}
