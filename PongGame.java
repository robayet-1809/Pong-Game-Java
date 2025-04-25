
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

abstract class GameObject {
    protected int x, y, width, height;
    protected Color color;

    public GameObject(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public abstract void draw(Graphics2D g2d);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class Paddle extends GameObject {
    public Paddle(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
    }

    public void move(int dy, int screenHeight) {
        y += dy;
        if (y < 10) y = 10;
        if (y > screenHeight - height - 10) y = screenHeight - height - 10;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, width, height, 20, 20);
    }
}

class Ball extends GameObject {
    public double dx, dy;
    private long startTime;

    public Ball(int x, int y, int size, Color color) {
        super(x, y, size, size, color);
        start();
    }

    public void move() {
        long elapsed = System.currentTimeMillis() - startTime;
        double speedMultiplier = Math.min(1.0, elapsed / 1000.0);
        x += dx * speedMultiplier;
        y += dy * speedMultiplier;
    }

    public void bounceX() {
        dx = -dx;
    }

    public void bounceY() {
        dy = -dy;
    }

    public void reset(int newX, int newY) {
        x = newX;
        y = newY;
        start();
    }

    private void start() {
        dx = (Math.random() > 0.5 ? 3 : -3);
        dy = (Math.random() - 0.5) * 6;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 0));
        g2d.fillOval(x, y, width + 4, height + 4);
    }
}

public class PongGlowGame extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 400, HEIGHT = 600;
    private final int PADDLE_WIDTH = 15, PADDLE_HEIGHT = 100, BALL_SIZE = 20;

    private Paddle player, ai;
    private Ball ball;
    private javax.swing.Timer timer;

    private boolean upPressed = false, downPressed = false;

    private int playerScore = 0;
    private int aiScore = 0;
    private boolean gameOver = false;

    public PongGlowGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        player = new Paddle(10, HEIGHT / 2 - PADDLE_HEIGHT / 2, PADDLE_WIDTH, PADDLE_HEIGHT, new Color(0, 255, 255));
        ai = new Paddle(WIDTH - 25, HEIGHT / 2 - PADDLE_HEIGHT / 2, PADDLE_WIDTH, PADDLE_HEIGHT, new Color(255, 0, 100));
        ball = new Ball(WIDTH / 2 - BALL_SIZE / 2, HEIGHT / 2 - BALL_SIZE / 2, BALL_SIZE, Color.YELLOW);

        timer = new javax.swing.Timer(10, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        drawField(g2d);
        drawScores(g2d);
        player.draw(g2d);
        ai.draw(g2d);
        ball.draw(g2d);

        if (gameOver) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("GAME OVER", WIDTH / 2 - 90, HEIGHT / 2);
        }
    }

    private void drawField(Graphics2D g2d) {
        g2d.setColor(new Color(255, 0, 0));
        g2d.fillRect(0, 0, 50, 10);
        g2d.setColor(new Color(255, 255, 0));
        g2d.fillRect(WIDTH - 50, 0, 50, 10);

        g2d.setColor(new Color(0, 255, 0));
        g2d.fillRect(WIDTH - 50, HEIGHT - 10, 50, 10);
        g2d.setColor(new Color(0, 0, 255));
        g2d.fillRect(0, HEIGHT - 10, 50, 10);

        g2d.setColor(Color.GRAY);
        g2d.drawLine(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
        g2d.drawOval(WIDTH / 2 - 40, HEIGHT / 2 - 40, 80, 80);
    }

    private void drawScores(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255));
        g2d.setFont(new Font("Consolas", Font.BOLD, 20));
        g2d.drawString("AI: " + aiScore, 10, 25);
        g2d.drawString("Player: " + playerScore, WIDTH - 140, 25);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        if (upPressed) player.move(-4, HEIGHT);
        if (downPressed) player.move(4, HEIGHT);

        if (ball.y + BALL_SIZE / 2 > ai.y + PADDLE_HEIGHT / 2) ai.move(2, HEIGHT);
        else ai.move(-2, HEIGHT);

        ball.move();

        if (ball.y <= 0 || ball.y >= HEIGHT - BALL_SIZE) ball.bounceY();

        if (ball.getBounds().intersects(player.getBounds()) && ball.dx < 0) ball.bounceX();
        if (ball.getBounds().intersects(ai.getBounds()) && ball.dx > 0) ball.bounceX();

        if (ball.x < 0) {
            aiScore++;
            if (aiScore >= 7) gameOver = true;
            ball.reset(WIDTH / 2 - BALL_SIZE / 2, HEIGHT / 2 - BALL_SIZE / 2);
        }

        if (ball.x > WIDTH) {
            playerScore++;
            if (playerScore >= 7) gameOver = true;
            ball.reset(WIDTH / 2 - BALL_SIZE / 2, HEIGHT / 2 - BALL_SIZE / 2);
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) upPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) downPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Neon Glow Pong Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new PongGlowGame());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}