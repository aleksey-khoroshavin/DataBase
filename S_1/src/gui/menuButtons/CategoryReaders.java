package gui.menuButtons;

import javax.swing.*;
import java.awt.*;

public class CategoryReaders extends JPanel {
    private final JButton teacherButton;
    private final JButton researchersButton;
    private final JButton schoolchildButton;
    private final JButton studentButton;
    private final JButton pensionersButton;
    private final JButton workersButton;

    public CategoryReaders() {
        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JLabel readersCategory = new JLabel("<html>Выбрать категорию читателей:</html>");
        readersCategory.setFont(new Font(readersCategory.getFont().getName(), Font.BOLD, 16));
        layout.putConstraint(SpringLayout.NORTH, readersCategory, 10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, readersCategory, 20, SpringLayout.WEST, this);
        add(readersCategory);

        teacherButton = new JButton("Учителя");
        teacherButton.setFont(new Font(teacherButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.NORTH, teacherButton, 10, SpringLayout.SOUTH, readersCategory);
        layout.putConstraint(SpringLayout.WEST, teacherButton, 20, SpringLayout.WEST, this);
        add(teacherButton);

        researchersButton = new JButton("Научные сотрудники");
        researchersButton.setFont(new Font(researchersButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.NORTH, researchersButton, 10, SpringLayout.SOUTH, teacherButton);
        layout.putConstraint(SpringLayout.WEST, researchersButton, 20, SpringLayout.WEST, this);
        add(researchersButton);

        schoolchildButton = new JButton("Школьники");
        schoolchildButton.setFont(new Font(schoolchildButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.NORTH, schoolchildButton, 10, SpringLayout.SOUTH, researchersButton);
        layout.putConstraint(SpringLayout.WEST, schoolchildButton, 20, SpringLayout.WEST, this);
        add(schoolchildButton);

        studentButton = new JButton("Студенты");
        studentButton.setFont(new Font(studentButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.NORTH, studentButton, 10, SpringLayout.SOUTH, schoolchildButton);
        layout.putConstraint(SpringLayout.WEST, studentButton, 20, SpringLayout.WEST, this);
        add(studentButton);

        pensionersButton = new JButton("Пенсионеры");
        pensionersButton.setFont(new Font(pensionersButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.NORTH, pensionersButton, 10, SpringLayout.SOUTH, studentButton);
        layout.putConstraint(SpringLayout.WEST, pensionersButton, 20, SpringLayout.WEST, this);
        add(pensionersButton);

        workersButton = new JButton("Работники");
        workersButton.setFont(new Font(workersButton.getFont().getName(), Font.BOLD, 20));
        layout.putConstraint(SpringLayout.NORTH, workersButton, 10, SpringLayout.SOUTH, pensionersButton);
        layout.putConstraint(SpringLayout.WEST, workersButton, 20, SpringLayout.WEST, this);
        add(workersButton);

        String text = "<html>Посмотреть конкретную<br>категорию читателей<br>в зависимости<br>от статуса.<br><br>Общая информация:<br>" +
                "Институт<br>Школа<br>Место работы<br>Пенсионное свид. и т. д.</html>";
        JLabel info = new JLabel(text);
        info.setFont(new Font(info.getFont().getName(), Font.PLAIN, 16));
        layout.putConstraint(SpringLayout.NORTH, info, 10, SpringLayout.SOUTH, readersCategory);
        layout.putConstraint(SpringLayout.WEST, info, 20, SpringLayout.EAST, readersCategory);
        add(info);
    }

    public JButton getTeacherButton() {
        return teacherButton;
    }

    public JButton getResearchersButton() {
        return researchersButton;
    }

    public JButton getSchoolchildButton() {
        return schoolchildButton;
    }

    public JButton getStudentButton() {
        return studentButton;
    }

    public JButton getPensionersButton() {
        return pensionersButton;
    }

    public JButton getWorkersButton() {
        return workersButton;
    }
}
