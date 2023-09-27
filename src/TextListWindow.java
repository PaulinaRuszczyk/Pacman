import javax.swing.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;

public class TextListWindow extends JFrame {
    public TextListWindow() {
        JList<String> textList = new JList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        textList.setModel(listModel);

        try (BufferedReader reader = new BufferedReader(new FileReader("points.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                listModel.addElement(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Text List Window");
        setSize(400, 300);
        setLocationRelativeTo(null); // Center the window on the screen

        JScrollPane scrollPane = new JScrollPane(textList);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

}