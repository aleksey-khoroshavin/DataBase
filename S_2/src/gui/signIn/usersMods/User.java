package gui.signIn.usersMods;

import connection.DBConnection;
import gui.MainWindow;
import gui.UserMods;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class User extends UserMod{

    public User(String nameServer, Properties properties, String url) {
        super(nameServer, properties, url);
    }

    @Override
    public void openSecurityCheckWindow() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        this.setBounds(dimension.width/2 - 150, dimension.height/2 - 150, 300, 300);
        this.setTitle("Вход как читатель");

        JPanel panel = new JPanel();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        this.add(panel);

        JLabel info = new JLabel("<html>Введите идентификатор и пароль пользователя.</html>");
        Font labelFont = info.getFont();
        info.setFont(new Font(labelFont.getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.WEST, info, 20, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, info, -20, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, info, 20, SpringLayout.NORTH, panel);
        panel.add(info);

        JLabel loginLabel = new JLabel("ID");
        loginLabel.setFont(new Font(loginLabel.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, loginLabel, 10, SpringLayout.SOUTH, info);
        layout.putConstraint(SpringLayout.WEST, loginLabel, 20, SpringLayout.WEST, panel);
        panel.add(loginLabel);

        JTextField login = new JTextField(15);
        login.setFont(new Font(login.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, login, 10, SpringLayout.SOUTH, loginLabel);
        layout.putConstraint(SpringLayout.WEST, login, 20, SpringLayout.WEST, panel);
        panel.add(login);

        JLabel passwordLabel = new JLabel("Пароль");
        passwordLabel.setFont(new Font(passwordLabel.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, passwordLabel, 10, SpringLayout.SOUTH, login);
        layout.putConstraint(SpringLayout.WEST, passwordLabel, 20, SpringLayout.WEST, panel);
        panel.add(passwordLabel);

        JPasswordField passwordValue = new JPasswordField(15);
        passwordValue.setFont(new Font(passwordValue.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, passwordValue, 10, SpringLayout.SOUTH, passwordLabel);
        layout.putConstraint(SpringLayout.WEST, passwordValue, 20, SpringLayout.WEST, panel);
        panel.add(passwordValue);

        JButton confirm = new JButton("Подтвердить");
        confirm.setFont(new Font(confirm.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, confirm, 20, SpringLayout.SOUTH, passwordValue);
        layout.putConstraint(SpringLayout.WEST, confirm, 30, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, confirm, -30, SpringLayout.EAST, panel);
        confirm.addActionListener(e -> {
            String loginValue = login.getText();

            StringBuilder pwd = new StringBuilder();
            for(int i = 0; i < passwordValue.getPassword().length; i++){
                pwd.append(passwordValue.getPassword()[i]);
            }

            try {
                DBConnection connection = new DBConnection(getUrl(), getProperties());
                PreparedStatement preStatement = connection.getConn().prepareStatement("select USER_ID, PASSWORD," +
                        "USER_MOD" +
                        " from USERS where USER_ID = " + loginValue + " and PASSWORD = '"+ pwd +"'");
                ResultSet resultSet = preStatement.executeQuery();

                int count = 0;
                String userModStr = "";
                UserMods userMod = UserMods.READER;
                while (resultSet.next()){
                    count++;
                    userModStr = resultSet.getString("user_mod");
                }

                resultSet.close();

                if(count > 0){
                    System.out.println("Success!");
                    this.setVisible(false);

                    System.out.println("Set role...");

                    switch (userModStr){
                        case "Администратор":{
                            connection.setRole(UserMods.ADMINISTRATOR);
                            userMod = UserMods.ADMINISTRATOR;
                            break;
                        }
                        case "Библиотекарь":{
                            connection.setRole(UserMods.LIBRARIAN);
                            userMod = UserMods.LIBRARIAN;
                            break;
                        }
                        case "Читатель":{
                            connection.setRole(UserMods.READER);
                            userMod = UserMods.READER;
                            break;
                        }
                    }

                    MainWindow mainWindow = new MainWindow(connection, getNameServer(), userMod, false);
                    mainWindow.setUserId(loginValue);
                    mainWindow.run();
                }
                else {
                    connection.getConn().close();
                    throw new SQLException("Неверный идентификатор пользователя или пароль!");
                }
            } catch (SQLException exception) {
                JLabel error = new JLabel("Ошибка подключения! " + exception.getMessage());
                error.setFont(new Font(error.getFont().getName(), Font.BOLD, 16));
                JOptionPane.showMessageDialog(null, error, "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(confirm);

        this.setResizable(false);
        this.setModal(true);
        this.setVisible(true);
    }
}
