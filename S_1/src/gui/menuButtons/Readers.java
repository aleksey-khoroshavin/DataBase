package gui.menuButtons;

import javax.swing.*;
import java.awt.*;

public class Readers extends JPanel {
    private final JButton openButton;

    public Readers() {
        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        openButton = new JButton("Открыть");
        openButton.setFont(new Font(openButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.EAST, openButton, -10, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.SOUTH, openButton, -5, SpringLayout.SOUTH, this);

        JLabel infoText = new JLabel("<html><b>Читатели</b>.Общая информация:<br>ФИО<br>Номер библиотеки<br>Статус</html>");
        infoText.setFont(new Font(infoText.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, infoText, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, infoText, 20, SpringLayout.WEST, this);

        add(infoText);
        add(openButton);
    }

    public JButton getOpenButton() {
        return openButton;
    }
}
