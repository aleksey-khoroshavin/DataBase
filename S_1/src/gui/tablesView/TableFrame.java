package gui.tablesView;

import connection.DBConnection;
import controllers.TableController;
import gui.tablesView.insertViews.LibraryInsert;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class TableFrame extends JFrame {
    private final String tableName;
    DBConnection connection;
    private final TableController tableController;

    public TableFrame(TableController tableController){
        this.tableName = tableController.getTableName();
        this.connection = tableController.getConnection();
        this.tableController = tableController;
    }

    private String translateTableName(){
        switch (tableName){
            case "Libraries":
                return "Библиотеки";
            default:
                return null;
        }
    }

    public void openTable() throws SQLException {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        this.setBounds(dimension.width/2 - 400, dimension.height/2 - 300, 800, 600);
        this.setTitle(translateTableName());

        JPanel jPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        jPanel.setLayout(layout);
        this.add(jPanel);

        DefaultTableModel tableModel = tableController.getTableSet();

        JLabel tableTitle = new JLabel("Таблица \""+ translateTableName() + "\"");
        tableTitle.setFont(new Font(tableTitle.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, tableTitle, 20, SpringLayout.NORTH, jPanel);
        layout.putConstraint(SpringLayout.WEST, tableTitle, 20, SpringLayout.WEST, jPanel);
        jPanel.add(tableTitle);

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        layout.putConstraint(SpringLayout.NORTH, scrollPane, 20, SpringLayout.NORTH, tableTitle);
        layout.putConstraint(SpringLayout.WEST, scrollPane, 20, SpringLayout.WEST, jPanel);
        layout.putConstraint(SpringLayout.SOUTH, scrollPane, -20, SpringLayout.SOUTH, jPanel);
        jPanel.add(scrollPane);

        JLabel infoTable = new JLabel("<html>Модификация<br>таблицы данных:</html>");
        infoTable.setFont(new Font(infoTable.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, infoTable, 50, SpringLayout.NORTH, jPanel);
        layout.putConstraint(SpringLayout.WEST, infoTable, 20, SpringLayout.EAST, scrollPane);
        jPanel.add(infoTable);

        //ввод данных в таблицу
        JButton insert = new JButton("Добавить запись");
        insert.setFont(new Font(insert.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.WEST, insert, 20, SpringLayout.EAST, scrollPane);
        layout.putConstraint(SpringLayout.NORTH, insert, 30, SpringLayout.SOUTH, infoTable);

        insert.addActionListener(e -> {
            LibraryInsert libraryInsert = new LibraryInsert(tableController);
            libraryInsert.openInsertWindow();
        });

        jPanel.add(insert);

        //Удаление данных из таблицы
        JButton delete = new JButton("Удалить запись");
        delete.setFont(new Font(delete.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.WEST, delete, 20, SpringLayout.EAST, scrollPane);
        layout.putConstraint(SpringLayout.NORTH, delete, 30, SpringLayout.SOUTH, insert);
        jPanel.add(delete);

        //Модификация данных таблицы
        JButton modify = new JButton("Изменить запись");
        modify.setFont(new Font(modify.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.WEST, modify, 20, SpringLayout.EAST, scrollPane);
        layout.putConstraint(SpringLayout.NORTH, modify, 30, SpringLayout.SOUTH, delete);
        jPanel.add(modify);




        JButton exit = new JButton("Закрыть таблицу");
        exit.setFont(new Font(exit.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.WEST, exit, 20, SpringLayout.EAST, scrollPane);
        layout.putConstraint(SpringLayout.SOUTH, exit, -20, SpringLayout.SOUTH, jPanel);
        exit.addActionListener(e -> setVisible(false));
        jPanel.add(exit);

        this.setResizable(false);
        this.setVisible(true);
    }
}
