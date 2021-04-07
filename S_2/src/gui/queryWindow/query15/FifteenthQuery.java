package gui.queryWindow.query15;

import controllers.QueryController;
import gui.queryWindow.QueryFrame;
import gui.queryWindow.ResultQueryView;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class FifteenthQuery extends QueryFrame {

    public FifteenthQuery(QueryController queryController) {
        super(queryController);
    }

    @Override
    public void openQueryConfig() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        this.setBounds(dimension.width/2 - 300, dimension.height/2 - 150, 600, 300);
        this.setTitle("Поиск литературы");

        JPanel jPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        jPanel.setLayout(layout);
        this.add(jPanel);

        JLabel info = new JLabel("<html>Выберите автора из списка<br>зарегистрированных в фонде</html>");
        info.setFont(new Font(info.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, info, 20, SpringLayout.NORTH, jPanel);
        layout.putConstraint(SpringLayout.WEST, info, 20, SpringLayout.WEST, jPanel);
        jPanel.add(info);

        try{
            String sql = "select distinct author from compositions";
            getQueryController().performSQLQuery(sql);
            ArrayList<String> authorsNames = new ArrayList<>();
            while (getQueryController().getCurrResultSet().next()){
                authorsNames.add(getQueryController().getCurrResultSet().getString("author"));
            }
            getQueryController().closeSQLSet();

            JComboBox<String> authorChoose = new JComboBox(authorsNames.toArray());
            authorChoose.setFont(new Font(authorChoose.getFont().getName(), Font.PLAIN, 16));
            layout.putConstraint(SpringLayout.NORTH, authorChoose, 10, SpringLayout.SOUTH, info);
            layout.putConstraint(SpringLayout.WEST, authorChoose, 20, SpringLayout.WEST, jPanel);
            jPanel.add(authorChoose);

            JButton confirm = new JButton("Найти литературу");
            confirm.setFont(new Font(confirm.getFont().getName(), Font.BOLD, 16));
            layout.putConstraint(SpringLayout.SOUTH, confirm, -10, SpringLayout.SOUTH, jPanel);
            layout.putConstraint(SpringLayout.EAST, confirm, -10, SpringLayout.EAST, jPanel);
            confirm.addActionListener(e -> {
                String chooseGenre = Objects.requireNonNull(authorChoose.getSelectedItem()).toString();
                String queryStr = "with t1 as (\n" +
                        "    --Выбор изданий, которые относятся к произведению конкретного автора\n" +
                        "    select ID_EDITION, TITLE\n" +
                        "    from COMPOSITIONS\n" +
                        "    where AUTHOR = '"+ chooseGenre +"'\n" +
                        "),\n" +
                        "     t2 as (\n" +
                        "         --Получить инвертарные номера (номера полок, где лежит книга)\n" +
                        "         select SHELF_NUM as \"Инвентарный номер\", TITLE as \"Название\" from EDITIONS\n" +
                        "                inner join t1 on t1.ID_EDITION = EDITIONS.ID_EDITION\n" +
                        "     )\n" +
                        "select *\n" +
                        "from t2";

                try {
                    getQueryController().performSQLQuery(queryStr);
                    ResultSet resultSet = getQueryController().getCurrResultSet();
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    int columnsCount = resultSetMetaData.getColumnCount();
                    ArrayList<String> list = new ArrayList<>();
                    for(int i = 1; i <= columnsCount; i++){
                        list.add(resultSetMetaData.getColumnLabel(i));
                    }
                    String[] columnsHeaders = list.toArray(new String[0]);
                    ResultQueryView queryView = new ResultQueryView(resultSet, columnsCount, columnsHeaders);
                    getQueryController().closeSQLSet();
                }
                catch (SQLException exception){
                    JLabel error = new JLabel();
                    error.setText(exception.getMessage());
                    error.setFont(new Font(error.getFont().getName(), Font.BOLD, 16));
                    JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            });
            jPanel.add(confirm);
        }
        catch (SQLException exception){
            JLabel error = new JLabel();
            error.setText(exception.getMessage());
            error.setFont(new Font(error.getFont().getName(), Font.BOLD, 16));
            JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
        }

        this.setResizable(false);
        this.setModal(true);
        this.setVisible(true);
    }
}
