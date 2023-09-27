import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Board extends JTable {
    private static final int BOARD_SIZE = 50;
    private boolean running = true;
    private int a = 0;
    private int b = 0;
    private double speed=1;
    private String movement = "";
    private int points = 0;
    private int pointsExtra=1;
    private Map<Point, Shape> dotShapes;
    private Map<Point, Shape> ghostShapes;
    private Map<Point, Shape> extras;
    private Map<Point, Shape> walls;
    private Point pacman;
    private String userName;
    private JPanel entryPanel;
    private int lifesLeft=3;
    private ExitThread thread;
    private extrasThread extraThread;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Board() {
        thread = new ExitThread();
        thread.start();
        addKeyListener(thread);
        initializeBoard();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void initializeBoard() {
        DefaultTableModel model = new DefaultTableModel(BOARD_SIZE, BOARD_SIZE) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

       // remove(entryPanel);
        createGhostsPoints();
        createPoints();
        setIntercellSpacing(new java.awt.Dimension(0, 0));
        setTableHeader(null);
        pacman = new Point(0, 0);
        running = true;
        createWalls();
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "moveLeft");
        getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                movement = "left";
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "moveRight");
        getActionMap().put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                movement = "right";
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "dying");
        getActionMap().put("dying", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dying();
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "moveUp");
        getActionMap().put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                movement = "up";
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "moveDown");
        getActionMap().put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                movement = "down";
            }
        });
        setModel(model);
        setDefaultRenderer(Object.class, new CircleCellRenderer());
        MovementThread movementThread = new MovementThread();
        movementThread.start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void openTextListWindow() {
        TextListWindow textListWindow = new TextListWindow();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void createWalls() {
        walls = new HashMap<>();
        Shape circle = new Rectangle(0, 0, 40, 30);
        Random random = new Random();

        float propability = 0.50f;
        for (int layer = 0; layer < BOARD_SIZE / 2; layer += 2) {
            int count = 0;
            int max_count = (BOARD_SIZE - layer) * 4 - 4;
            int min_enters = 2;
            for (int i = layer; i < BOARD_SIZE - layer; i++) {
                if (random.nextDouble() < propability && count < max_count - min_enters) {
                    walls.put(new Point(i, layer), circle);
                    count++;
                }
                if (random.nextDouble() < propability && count < max_count - min_enters) {
                    walls.put(new Point(i, BOARD_SIZE - layer - 1), circle);
                    count++;
                }
            }

            for (int i = layer; i < BOARD_SIZE - layer; i++) {
                if (random.nextDouble() < propability && count < max_count - min_enters) {
                    walls.put(new Point(layer, i), circle);
                    count++;
                }
                if (random.nextDouble() < propability && count < max_count - min_enters) {

                    walls.put(new Point(BOARD_SIZE - layer - 1, i), circle);
                    count++;
                }
            }
        }
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                walls.remove(new Point(i, j));

        for (int i = BOARD_SIZE/2; i < BOARD_SIZE/2+4; i++)
            for (int j = BOARD_SIZE/2; j < BOARD_SIZE/2+4; j++)
                walls.remove(new Point(i, j));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void movement() {
        if (dotShapes != null && !dotShapes.isEmpty() && ghostShapes != null && !ghostShapes.isEmpty()) {
            if (movement.equals("left")) {
                if (pacman.x > 0 && !walls.containsKey(new Point(b - 1, a))) {
                    pacman.x--;
                    if (dotShapes.containsKey(new Point(b, a))) {
                        dotShapes.remove(new Point(b, a));
                        points += pointsExtra;
                    }
                    b--;
                }
            } else if (movement.equals("right")) {
                if (pacman.x < 100 && !walls.containsKey(new Point(b + 1, a))) {
                    pacman.x++;
                    if (dotShapes.containsKey(new Point(b, a))) {
                        dotShapes.remove(new Point(b, a));
                        points += pointsExtra;
                    }
                    b++;
                }
            } else if (movement.equals("up")) {
                if (pacman.y > 0 && !walls.containsKey(new Point(b, a - 1))) {
                    pacman.y--;
                    if (dotShapes.containsKey(new Point(b, a))) {
                        dotShapes.remove(new Point(b, a));
                        points += pointsExtra;
                    }
                    a--;
                }
            } else if (movement.equals("down")) {
                if (pacman.y < 100 && !walls.containsKey(new Point(b, a + 1))) {
                    if (dotShapes.containsKey(new Point(b, a))) {
                        dotShapes.remove(new Point(b, a));
                        points += pointsExtra;
                    }
                    pacman.y++;
                    a++;
                }
            }
            SwingUtilities.invokeLater(this::repaint);
            for (Map.Entry<Point, Shape> entry : ghostShapes.entrySet()) {
                if (pacman.x == entry.getKey().x && pacman.y == entry.getKey().y) {
                    dying();
                }
            }
            if(dotShapes.isEmpty())
            {
                JOptionPane.showMessageDialog(null, "You won! You got " + points + " points!");
                points = 0;
                running = false;
                a = 0;
                b = 0;
                pacman = new Point(0, 0);
                lifesLeft=3;
                TextInputWindow window = new TextInputWindow();
                window.setMinimumSize(new Dimension(300, 200));
            }
            if (extras != null && !extras.isEmpty())
                for (Map.Entry<Point, Shape> extra : extras.entrySet()) {
                    if (pacman.x == extra.getKey().x && pacman.y == extra.getKey().y) {
                        extraThread = new extrasThread();
                        extras.remove(extra.getKey(), extra.getValue());
                        extraThread.start();
                    }
                }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void dying() {
        lifesLeft--;
        a = 0;
        b = 0;
        pacman = new Point(0, 0);
        if (lifesLeft == 0) {
            JOptionPane.showMessageDialog(null, "You died! You got " + points + " points!");
            points = 0;
            running = false;
            TextInputWindow window = new TextInputWindow();
            window.setMinimumSize(new Dimension(300, 200));
            //dispose();
        }
       // startBoard();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void createPoints() {
        dotShapes = new HashMap<>();
        Shape circle = new Ellipse2D.Double(8, 8, 2, 2);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                dotShapes.put(new Point(i, j), circle);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void createGhostsPoints() {
        ghostShapes = new HashMap<>();
        Shape circle = new Ellipse2D.Double(5, 5, 15, 15);
        for (int i = 0; i < 5; i++) {
            Random random = new Random();
            Point cell = new Point(random.nextInt(3) + BOARD_SIZE/2, random.nextInt(3) + BOARD_SIZE/2);
            if (ghostShapes.get(cell)==null)
                ghostShapes.put(cell, circle);
            else
                i--;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void createExtra(Map.Entry<Point, Shape> entry) {
        extras = new HashMap<>();
        Shape circle = new Ellipse2D.Double(5, 5, 8, 8);
        extras.put(new Point(entry.getKey().x, entry.getKey().y), circle);
        //extras.put(new Point(2,2), circle);
        timeThread timeThread = new timeThread(entry.getKey(), circle);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Point ghostsMovements(Map.Entry<Point, Shape> entry) {
        Random random = new Random();
        int direction = random.nextInt(4);
        Random randomExtra = new Random();
        int x = randomExtra.nextInt(100);
        Point ghostPosition = entry.getKey();

        Set<Point> occupiedCells = new HashSet<>();
        for (Map.Entry<Point, Shape> ghostEntry : ghostShapes.entrySet()) {
            occupiedCells.add(ghostEntry.getKey());
        }

        switch (direction) {
            case 0:
                if (ghostPosition.x > 0 && !walls.containsKey(new Point(ghostPosition.x - 1, ghostPosition.y))) {
                    Point cell = new Point(ghostPosition.x - 1, ghostPosition.y);
                    if (!occupiedCells.contains(cell)) {
                        ghostPosition.setLocation(cell);
                    }
                    if(x==0)
                        createExtra(entry);
                }
                break;
            case 1:
                if (ghostPosition.x < 100 && !walls.containsKey(new Point(ghostPosition.x + 1, ghostPosition.y))) {
                    Point cell = new Point(ghostPosition.x + 1, ghostPosition.y);
                    if (!occupiedCells.contains(cell)) {
                        ghostPosition.setLocation(cell);
                    }
                    if(x==1)
                        createExtra(entry);
                }
                break;
            case 2:
                if (ghostPosition.y > 0 && !walls.containsKey(new Point(ghostPosition.x, ghostPosition.y - 1))) {
                    Point cell = new Point(ghostPosition.x, ghostPosition.y - 1);
                    if (!occupiedCells.contains(cell)) {
                        ghostPosition.setLocation(cell);
                    }
                    if(x==2)
                        createExtra(entry);
                }
                break;
            case 3:
                if (ghostPosition.y < 100 && !walls.containsKey(new Point(ghostPosition.x, ghostPosition.y + 1))) {
                    Point cell = new Point(ghostPosition.x, ghostPosition.y + 1);
                    if (!occupiedCells.contains(cell)) {
                        ghostPosition.setLocation(cell);
                    }
                    if(x==3)
                        createExtra(entry);
                }
                break;
        }

        return ghostPosition;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class CircleCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component cellRenderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            cellRenderer.setBackground(Color.BLACK);
            return cellRenderer;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Rectangle cellRect = getCellRect(0, 0, false);
            int cellWidth = cellRect.width;
            int cellHeight = cellRect.height;

            int row = getTableCellRendererComponent(Board.this, null, false, false, 0, 0).getBounds().y / cellHeight;
            int column = getTableCellRendererComponent(Board.this, null, false, false, 0, 0).getBounds().x / cellWidth;

            Point cell = new Point(column, row);
            Shape circle = dotShapes.get(cell);
            Shape ghosts = ghostShapes.get(cell);
            Shape wall = walls.get(cell);

            if (circle != null) {
                g2d.setColor(Color.YELLOW);
                g2d.fill(circle);
            }
            if (ghosts != null) {
                g2d.setColor(Color.BLUE);
                g2d.fill(ghosts);
            }
            if (pacman.x == cell.x && pacman.y == cell.y) {
                g2d.setColor(Color.RED);
                g2d.fill(new Ellipse2D.Double(5, 5, 10, 10));
            }

            if (wall != null) {
                g2d.setColor(Color.GREEN);
                g2d.fill(wall);
            }
            if(extras!=null) {
                Shape extra = extras.get(cell);
                if (extra != null) {
                    g2d.setColor(Color.WHITE);
                    g2d.fill(extra);
                }
            }
        }

    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class timeThread extends Thread {
        private Point point;
        private Shape circle;
        timeThread(Point point, Shape circle) {
            this.point = point;
            this.circle = circle;
            start();
        }
        @Override
        public void run() {
            try {
                Thread.sleep(5000);

                extras.remove(new Point(point.x, point.y), circle);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MovementThread extends Thread {
        @Override
        public void run() {
            while (running) {
                movement();
                if (dotShapes != null && !dotShapes.isEmpty() && ghostShapes != null && !ghostShapes.isEmpty()) {

                    Map<Point, Shape> updatedGhostShapes = new HashMap<>();

                    Shape circle = new Ellipse2D.Double(5, 5, 15, 15);
                    for (Map.Entry<Point, Shape> entry : ghostShapes.entrySet()) {
                        updatedGhostShapes.put(ghostsMovements(entry), circle);
                    }


                    ghostShapes = updatedGhostShapes;
                }
                try {
                    Thread.sleep(200*(long)speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class ExitThread extends Thread implements KeyListener {
        private boolean ctrlPressed;
        private boolean wPressed;

        @Override
        public void run() {

            while (true) {
                if (ctrlPressed && wPressed) {
                    System.exit(0);
                }  try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_W) {
                wPressed = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                ctrlPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_W) {
                wPressed = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

    }
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class extrasThread extends Thread {
          @Override
          public void run() {
              Random random = new Random();
              int x = random.nextInt(4);

              try {
                  switch (x % 20) {
                      case 1:
                          lifesLeft++;
                          break;
                      case 2:
                          pointsExtra = 2;
                          Thread.sleep(3000);
                          pointsExtra = 1;
                          break;
                      case 3:
                          speed = 2;
                          Thread.sleep(3000);
                          speed = 1;
                          break;
                      default:
                          break;

                  }
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }

          }

      }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class TextInputWindow extends JFrame {
        private JTextField textField;

        public TextInputWindow() {
            setTitle("Text Input Window");
            setSize(300, 200);

            textField = new JTextField();
            textField.setBounds(50, 50, 200, 30);
            textField.setPreferredSize(new Dimension(200, 30));

            JButton button = new JButton("Ok");
            button.addActionListener(e -> {
                userName = textField.getText();
                KeyValueLogger logger = new KeyValueLogger("points.txt");

                logger.log(userName, points);

                logger.writeSortedToFile();
                System.out.println("Data written to file successfully.");

                dispose(); // Close the window
                points = 0;

            });
            add(new JLabel("Podaj imie:"));
            add(button);
            add(textField);

            setLayout(new FlowLayout()); // Use a layout manager
            setVisible(true);
        }
    }
    }