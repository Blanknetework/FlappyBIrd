import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird class
    int birdX = 50;  // Bird starts on the left side of the screen
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }

        void reset() {
            x = birdX;
            y = birdY;
        }
    }

    // Pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4; // Move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; // Move bird up/down speed.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    // Game State
    enum GameState { MENU, PLAYING, GAME_OVER }
    GameState currentState = GameState.MENU;

    JButton startButton;
    JButton quitButton;
    JButton restartButton;

    Clip backgroundMusic;
    Clip dieSound;
    Clip scoreSound;

    public GamePanel() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getClassLoader().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getClassLoader().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getClassLoader().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getClassLoader().getResource("./bottompipe.png")).getImage();

        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Menu Buttons
        startButton = new JButton("Start");
        quitButton = new JButton("Quit");
        restartButton = new JButton("Restart");

        startButton.setBounds(120, 250, 120, 50);
        quitButton.setBounds(120, 320, 120, 50);
        restartButton.setBounds(120, 250, 120, 50);

        startButton.addActionListener(e -> startGame());
        quitButton.addActionListener(e -> System.exit(0)); // Quit the game
        restartButton.addActionListener(e -> startGame());

        setLayout(null);
        add(startButton);
        add(quitButton);

        // Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        // Game timer
        gameLoop = new Timer(1000 / 60, this); // How long it takes to start timer, milliseconds gone between frames

        // Load and play background music
        backgroundMusic = loadSound("main.wav");
        if (backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            System.out.println("Background music not loaded.");
        }

        // Load die sound
        dieSound = loadSound("die.wav");
        if (dieSound == null) {
            System.out.println("Die sound not loaded.");
        }

        // Load score sound
        scoreSound = loadSound("score.wav");
        if (scoreSound == null) {
            System.out.println("Score sound not loaded.");
        }
    }

    void startGame() {
        currentState = GameState.PLAYING;
        bird.reset();  // Reset bird to starting position
        velocityY = 0;   // Reset bird velocity
        pipes.clear();    // Clear any pipes from previous game
        score = 0;       // Reset score
        gameOver = false; // Set game over to false
        gameLoop.start(); // Start the game loop (to update and move objects continuously)
        placePipeTimer.start(); // Start pipe generation
        startButton.setVisible(false);   // Hide the start button after game starts
        quitButton.setVisible(false);    // Hide the quit button after game starts
        restartButton.setVisible(false); // Hide the restart button after game starts
        requestFocusInWindow(); // Ensure the panel has focus to receive key events
        repaint();             // Repaint the screen
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (currentState) {
            case MENU:
                drawMenu(g);
                break;
            case PLAYING:
                draw(g);
                break;
            case GAME_OVER:
                draw(g);
                drawGameOver(g);
                break;
        }
    }

    public void drawMenu(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);  // Show background in menu
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 40));
        g.drawString("Flappy Bird", 80, 100);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press Start to Play", 100, 200);
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);  // Show bird in menu
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (!gameOver) {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void drawGameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 40));
        FontMetrics fm = g.getFontMetrics();
        int gameOverWidth = fm.stringWidth("Game Over");
        g.drawString("Game Over", (boardWidth - gameOverWidth) / 2, boardHeight / 2 - 50);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        int scoreWidth = fm.stringWidth("Score: " + (int) score);
        g.drawString("Score: " + (int) score, (boardWidth - scoreWidth) / 2, boardHeight / 2);

        restartButton.setBounds((boardWidth - 120) / 2, boardHeight / 2 + 30, 120, 50);
        quitButton.setBounds((boardWidth - 120) / 2, boardHeight / 2 + 90, 120, 50);
        restartButton.setVisible(true);
        quitButton.setVisible(true);
        add(restartButton);
        add(quitButton);
    }

    public void move() {
        if (currentState == GameState.PLAYING) {
            // Apply gravity to bird
            velocityY += gravity; // Increase velocityY due to gravity
            bird.y += velocityY;  // Move the bird downwards based on velocityY

            // Prevent bird from going above the top of the screen
            bird.y = Math.max(bird.y, 0);

            // Move pipes
            for (Pipe pipe : pipes) {
                pipe.x += velocityX; // Move pipes to the left

                // Increment score when bird passes a pipe
                if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                    score += 0.5; // Increment score
                    pipe.passed = true; // Mark pipe as passed
                    playSound(scoreSound);
                }

                // Check for collisions
                if (collision(bird, pipe)) {
                    gameOver = true;
                    currentState = GameState.GAME_OVER; // End game on collision
                    playSound(dieSound);
                }
            }

            // Check if bird hits the ground (falls off the screen)
            if (bird.y > boardHeight) {
                gameOver = true;
                currentState = GameState.GAME_OVER; // End game if bird hits the ground
                playSound(dieSound);
            }

            // Increase game speed when score reaches 1000
            if (score >= 1000) {
                increaseGameSpeed();
            }
        }
    }

    void increaseGameSpeed() {
        velocityX -= 2; // Increase the speed of pipes
        gameLoop.setDelay(1000 / 75); // Increase the game loop speed
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   // a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) { // Called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (currentState == GameState.PLAYING) {
                // Only jump if game is in PLAYING state
                velocityY = -9; // Jumping speed
            } else if (currentState == GameState.MENU) {
                // Start game when space is pressed on the menu
                startGame();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private Clip loadSound(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void playSound(Clip clip) {
        if (clip != null) {
            clip.setFramePosition(0); // Rewind to the beginning
            clip.start();
        }
    }
}