import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {
    private Board board;
    private JPanel entryPanel;


    public GameWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Game Window");
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window on the screen

//        add(board); // Add the board to the window
//        board = new Board();

//        openBoardWindow();
        initializeGame();
        setVisible(true);
    }

    private void initializeGame()
    {
        entryPanel = new JPanel(new GridLayout(3, 1));

        JButton startButton = new JButton("Start");
        startButton.addActionListener((e) -> openBoardWindow());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener((e) -> System.exit(0));

        JButton pointsButton = new JButton("Points");
        pointsButton.addActionListener((e) -> openTextListWindow());

        entryPanel.add(new JLabel("Welcome to the Game!"));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(startButton);
        buttonPanel.add(pointsButton);
        buttonPanel.add(exitButton);

        setLayout(new BorderLayout());
        add(entryPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

    }
    private void openBoardWindow() {
        JFrame boardWindow = new JFrame("Board Window");
        boardWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        boardWindow.setSize(800, 600);
        boardWindow.setLocationRelativeTo(this); // Position the board window relative to the game window
        board = new Board();
        boardWindow.add(board); // Add the board to the board window

        boardWindow.setVisible(true);
    }
    private void openTextListWindow() {
        TextListWindow textListWindow = new TextListWindow();
    }

}