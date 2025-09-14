import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.sql.PreparedStatement;
import java.sql.*;





class SpaceVoyageGame extends JFrame implements ActionListener {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int GROUND_HEIGHT = 100;
    private static final double GRAVITY = 9.8;
    private static final int TARGET_SIZE = 60; // Increased from 40 to 60 for larger targets
    private static final int ROCKET_SIZE = 8;
    // Game mode settings
    private String currentGameMode = "Medium"; // Default mode
    private String currentUsername; // To store the username for saving player data
    private int currentLevel = 1; // To store the current game level
    private double currentGravity; // Will be adjusted based on game mode
    // Loading screen components
    private JWindow loadingScreen;
    private JLabel loadingImageLabel;
    private JProgressBar loadingProgressBar;
    private javax.swing.Timer loadingTimer;
    private int loadingProgress = 0;
    private Font customFont;
    private Image rocketImage; // Rocket image for loading screen
    // Start screen components
    private JPanel startPanel;
    private JButton startButton;
    private JButton rulesButton;
    private JCheckBox musicCheckBox;
    private JComboBox<String> gameModeSelector;
    private JLabel gameModeLabel;
    private Clip introMusic;
    private boolean musicEnabled = true;
    private Image startBackgroundImage;
    private Font startCustomFont;
    // Game components
    private GamePanel gamePanel;
    private JTextField velocityField;
    private JTextField angleField;
    private JButton launchButton;
    private JButton resetButton;
    private JButton newGameButton;
    private JButton viewPlayersButton;
    private JTextField usernameField;
    private JLabel scoreLabel;
    private JLabel instructionsLabel;
    private Image gameBackgroundImage; // Background image for game screen
    private javax.swing.Timer gameTimer;
    private Rocket rocket;
    private List<Target> targets;
    private List<Explosion> explosions;
    private List<Star> stars;
    private int score = 0;
    private boolean gameRunning = false;
    private Clip collisionSound;
    private Clip gameMusic; // New Clip for game background music
    private Image targetImage;
    private Image targetImage2;
    private Image targetImage3;
    // ... (iba pang fields)
    private Image windowIconImage; // <-- Para sa window icon
    // *** UPDATED DATABASE URL
    // Update these constants in your code
    private static final String DB_URL ="jdbc:mysql://localhost:3306/spacevoyage";;
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "142007"; // Replace with your actual password


    // Enhanced connection method with detailed error reporting
    private Connection getConnection() throws SQLException {
        try {
            // Explicitly load the MySQL driver (good practice, though often implicit in newer versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Attempting to connect to database...");
            System.out.println("URL: " + DB_URL);
            System.out.println("User: " + DB_USER);
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection successful!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            System.err.println("Please download mysql-connector-java and add it to your classpath");
            throw new SQLException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            throw e; // Re-throw to be handled by the caller
        }
    }

    public SpaceVoyageGame() {
        targets = new ArrayList<>();
        explosions = new ArrayList<>();
        stars = new ArrayList<>();
        // Mag-load ng window icon image sa simula
        try {
            // PALITAN ito ng path sa iyong aktwal na icon file
            windowIconImage = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\icon for space voyage.jpeg"));
            System.out.println("Window icon loaded successfully.");
        } catch (Exception e) {
            System.err.println("Could not load window icon image: " + e.getMessage());
            windowIconImage = null; // Kung hindi makapag-load, i-set sa null
        }
        showLoadingScreen();
    }

    private Font loadCustomFont(String fontPath, float size) {
        try {
            InputStream is = new FileInputStream(fontPath);
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            is.close();
            return customFont.deriveFont(size);
        } catch (Exception e) {
            System.err.println("Could not load custom font, using default: " + e.getMessage());
            return new Font("Dialog", Font.BOLD, (int)size);
        }
    }

    private void showLoadingScreen() {
        loadingScreen = new JWindow();
        loadingScreen.setSize(1000, 600);
        loadingScreen.setLocationRelativeTo(null);
        loadingScreen.setLayout(new BorderLayout());
        customFont = loadCustomFont("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\Game Of Squids.otf", 72f);
        // Try to load rocket image
        try {
            rocketImage = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\r2-removebg-preview (1).png"));
            // Scale rocket image
            rocketImage = rocketImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.err.println("Could not load rocket image: " + e.getMessage());
            rocketImage = null;
        }
        JPanel loadingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                try {
                    Image backgroundImage = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\prgload.png"));
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    g2d.setColor(new Color(25, 25, 112));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        loadingPanel.setLayout(new BorderLayout());
        JPanel overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlayPanel.setLayout(new GridBagLayout());
        overlayPanel.setOpaque(false);
        loadingPanel.add(overlayPanel, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        JLabel titleLabel = new JLabel("SPACE VOYAGE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(252, 219, 2),
                        0, getHeight(), new Color(23, 2, 44)
                );
                g2d.setPaint(gradient);
                g2d.setFont(customFont);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width += 20;
                size.height += 10;
                return size;
            }
        };
        titleLabel.setFont(customFont);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0; gbc.gridy = 0;
        overlayPanel.add(titleLabel, gbc);
        // Custom progress bar panel with rocket
        JPanel progressPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int barWidth = 600;
                int barHeight = 30;
                int x = (getWidth() - barWidth) / 2;
                int y = (getHeight() - barHeight) / 2;
                // Draw progress bar background
                g2d.setColor(Color.BLACK);
                g2d.fillRoundRect(x, y, barWidth, barHeight, 10, 10);
                g2d.setColor(new Color(50, 50, 50));
                g2d.drawRoundRect(x, y, barWidth, barHeight, 10, 10);
                // Draw progress fill
                int progressWidth = (int) (barWidth * loadingProgress / 100.0);
                if (progressWidth > 0) {
                    GradientPaint progressGradient = new GradientPaint(
                            x, y, new Color(252, 219, 2),
                            x + progressWidth, y, new Color(23, 2, 44)
                    );
                    g2d.setPaint(progressGradient);
                    g2d.fillRoundRect(x, y, progressWidth, barHeight, 10, 10);
                }
                // Draw rocket at the end of progress
                if (rocketImage != null && progressWidth > 0) {
                    int rocketX = x + progressWidth - 20;
                    int rocketY = y - 10;
                    g2d.drawImage(rocketImage, rocketX, rocketY, this);
                } else if (progressWidth > 0) {
                    // Fallback: draw simple rocket if image not loaded
                    int rocketX = x + progressWidth - 10;
                    int rocketY = y - 5;
                    g2d.setColor(Color.RED);
                    g2d.fillOval(rocketX, rocketY, 20, 20);
                    g2d.setColor(Color.ORANGE);
                    g2d.fillOval(rocketX + 3, rocketY + 3, 14, 14);
                }
                // Draw progress text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String progressText = "Loading... " + loadingProgress + "%";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(progressText)) / 2;
                int textY = y + barHeight + 25;
                g2d.drawString(progressText, textX, textY);
            }
        };
        progressPanel.setPreferredSize(new Dimension(700, 80));
        progressPanel.setOpaque(false);
        gbc.gridy = 1;
        overlayPanel.add(progressPanel, gbc);
        loadingScreen.add(loadingPanel, BorderLayout.CENTER);
        loadingScreen.setVisible(true);
        loadingTimer = new javax.swing.Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadingProgress += 2;
                progressPanel.repaint(); // Repaint to update rocket position
                if (loadingProgress >= 100) {
                    loadingTimer.stop();
                    loadingScreen.dispose();
                    showStartScreen();
                }
            }
        });
        loadingTimer.start();
    }

    private void showStartScreen() {
        setTitle("Space Voyage - By Luigi Balingit");
        // I-set ang window icon
        if (windowIconImage != null) {
            setIconImage(windowIconImage);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        startCustomFont = loadCustomFont("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\Game Of Squids.otf", 52f);
        startPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (startBackgroundImage != null) {
                    g2d.drawImage(startBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(25, 25, 112));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g2d.setColor(Color.WHITE);
                for (int i = 0; i < 150; i++) {
                    int x = (int)(Math.random() * getWidth());
                    int y = (int)(Math.random() * getHeight());
                    int size = (int)(Math.random() * 3) + 1;
                    g2d.fillOval(x, y, size, size);
                }
            }
        };
        startPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 10, 10);
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("SPACE VOYAGE", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(252, 219, 2),
                        0, getHeight(), new Color(23, 2, 44)
                );
                g2d.setPaint(gradient);
                g2d.setFont(startCustomFont);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width += 20;
                size.height += 10;
                return size;
            }
        };
        titleLabel.setFont(startCustomFont);
        titleLabel.setForeground(new Color(146, 120, 200));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        gbc.gridx = 0; gbc.gridy = 0;
        startPanel.add(titlePanel, gbc);
        startButton = new JButton("START GAME") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(106, 13, 173),
                        0, getHeight(), new Color(25, 25, 112)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(148, 0, 255, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 70);
            }
        };
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 1;
        startPanel.add(startButton, gbc);
        // Username Input
        JPanel usernamePanel = new JPanel();
        usernamePanel.setOpaque(false);
        usernamePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        usernameLabel.setForeground(Color.WHITE); // Kulay puti para makita sa dark background
        usernamePanel.add(usernameLabel);

        // Replace the username field creation section (around line 320-340) with this fixed version:

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(new Color(50, 50, 70)); // Dark blue-gray background
        usernameField.setForeground(Color.WHITE); // White text
        usernameField.setCaretColor(Color.WHITE); // White cursor
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 2),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        usernamePanel.add(usernameField);
        gbc.gridy = 1;
        startPanel.add(usernamePanel, gbc);
        gbc.gridy = 2; // Ilipat ang Start Button sa y = 2
        startPanel.add(startButton, gbc);
        rulesButton = new JButton("GAME RULES") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(138, 43, 226),
                        0, getHeight(), new Color(75, 0, 130)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(148, 0, 255, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 70);
            }
        };
        rulesButton.setFont(new Font("Arial", Font.BOLD, 24));
        rulesButton.setContentAreaFilled(false);
        rulesButton.setFocusPainted(false);
        rulesButton.setBorderPainted(false);
        rulesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 3;
        startPanel.add(rulesButton, gbc);
        musicCheckBox = new JCheckBox("Enable Intro Music", true);
        musicCheckBox.setFont(new Font("Arial", Font.PLAIN, 20));
        musicCheckBox.setForeground(Color.WHITE);
        musicCheckBox.setOpaque(false);
        musicCheckBox.setIconTextGap(10);
        gbc.gridy = 6;
        startPanel.add(musicCheckBox, gbc);
        // Game mode selector with futuristic styling
        JPanel gameModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gameModePanel.setOpaque(false);
        JLabel modeLabel = new JLabel("Game Mode: ");
        modeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        modeLabel.setForeground(Color.WHITE);
        gameModePanel.add(modeLabel);
        gameModeSelector = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        gameModeSelector.setFont(new Font("Arial", Font.BOLD, 20));
        gameModeSelector.setSelectedItem("Medium"); // Default selection
        gameModeSelector.setBackground(new Color(75, 0, 130)); // Deep violet background
        gameModeSelector.setForeground(new Color(75, 0, 130)); // Deep violet background
        gameModeSelector.setBorder(BorderFactory.createLineBorder(new Color(75, 0, 130), 2));// Deep violet background
        gameModeSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(new Color(147, 112, 219)); // Medium purple for selection
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(75, 0, 130)); // Deep violet background
                    c.setForeground(new Color(224, 187, 228)); // Light lavender text
                }
                return c;
            }
        });
        gameModePanel.add(gameModeSelector);
        gbc.gridy = 5;
        startPanel.add(gameModePanel, gbc);
        // View Players button with futuristic styling (matching start screen buttons)
        viewPlayersButton = new JButton("LEADERBOARD") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background (matching start screen buttons)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(138, 43, 226),
                        0, getHeight(), new Color(75, 0, 130)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                // Glow effect
                g2d.setColor(new Color(100, 0, 150, 50)); // Purple glow
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);
                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 70); // Pareho lang size ng START GAME at GAME RULES
            }
        };
        viewPlayersButton.setFont(new Font("Arial", Font.BOLD, 24));
        viewPlayersButton.setContentAreaFilled(false);
        viewPlayersButton.setFocusPainted(false);
        viewPlayersButton.setBorderPainted(false);
        viewPlayersButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 4;
        startPanel.add(viewPlayersButton, gbc);
        startButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username to start the game.", "Username Required", JOptionPane.WARNING_MESSAGE);
            } else {
                startGame(username);
            }
        });
        rulesButton.addActionListener(e -> showRules());
        musicCheckBox.addActionListener(e -> toggleMusic());
        gameModeSelector.addActionListener(e -> currentGameMode = (String) gameModeSelector.getSelectedItem());
        viewPlayersButton.addActionListener(e -> showPlayerList());
        add(startPanel, BorderLayout.CENTER);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        loadIntroMusic();
        loadCollisionSound();
        loadStartBackgroundImage();
        if (musicCheckBox.isSelected()) {
            playIntroMusic();
        }
    }

    private void loadStartBackgroundImage() {
        try {
            startBackgroundImage = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\startbg.png"));
        } catch (Exception e) {
            System.err.println("Could not load start background image: " + e.getMessage());
            startBackgroundImage = null;
        }
    }
    // Load game background image
    private void loadGameBackgroundImage() {
        try {
            // REPLACE THIS PATH with your game background image path
            gameBackgroundImage = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\bg game.jpg"));
        } catch (Exception e) {
            System.err.println("Could not load game background image: " + e.getMessage());
            gameBackgroundImage = null;
        }
    }

    private void loadIntroMusic() {
        try {
            File musicFile = new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\Dxrk ダーク - RAVE (Official Audio).wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile);
            introMusic = AudioSystem.getClip();
            introMusic.open(audioInputStream);
        } catch (Exception e) {
            System.err.println("Could not load intro music: " + e.getMessage());
        }
    }

    private void playIntroMusic() {
        if (introMusic != null && !introMusic.isRunning()) {
            introMusic.setFramePosition(0);
            introMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void resumeIntroMusic() {
        if (introMusic != null) {
            if (!introMusic.isRunning()) {
                introMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    private void pauseIntroMusic() {
        if (introMusic != null && introMusic.isRunning()) {
            introMusic.stop();
        }
    }

    private void stopIntroMusic() {
        if (introMusic != null) {
            if (introMusic.isRunning()) {
                introMusic.stop();
            }
            introMusic.setFramePosition(0);
        }
    }

    private void toggleMusic() {
        musicEnabled = musicCheckBox.isSelected();
        if (musicEnabled) {
            resumeIntroMusic();
        } else {
            pauseIntroMusic();
        }
    }

    private void startGame(String username) {
        this.currentUsername = username; // Store the username
        // Set the game mode based on selection
        currentGameMode = (String) gameModeSelector.getSelectedItem();
        stopIntroMusic();
        remove(startPanel);
        initializeGameComponents();
        playGameMusic(); // Start game music
        setVisible(true);
    }

    private void showRules() {
        // Create custom panel for futuristic look (matching the message box theme)
        JPanel rulesPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Black violet gradient background (matching message box)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(25, 0, 50), // Dark violet
                        0, getHeight(), new Color(0, 0, 0) // Black
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                // Add subtle grid pattern (matching message box)
                g2d.setColor(new Color(100, 0, 150, 50));
                for (int i = 0; i < getWidth(); i += 20) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 20) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        rulesPanel.setLayout(new BorderLayout());
        rulesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rulesPanel.setPreferredSize(new Dimension(500, 400));
        // Header panel (matching message box)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel headerLabel = new JLabel("GAME RULES");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        headerLabel.setForeground(new Color(138, 43, 226)); // Blue violet
        headerPanel.add(headerLabel);
        rulesPanel.add(headerPanel, BorderLayout.NORTH);
        // Content panel (matching message box)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] rules = {
                "1. Enter username, select mode (Easy=30, Med=70, Hard=100 pts/target).",
                "2. Input launch Velocity (1-200 m/s) and Angle (0-90 degrees).",
                "3. Click 'Launch' to send your rocket on its trajectory.",
                "4. Try to hit and destroy ALL targets to win the round!",
                "5. Use 'Reset' for another attempt, 'New Game' for a fresh start."
        };
        for (String rule : rules) {
            JPanel rulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            rulePanel.setOpaque(false);
            rulePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel bullet = new JLabel("▶");
            bullet.setFont(new Font("Arial", Font.BOLD, 16));
            bullet.setForeground(new Color(255, 215, 0)); // Gold bullet
            bullet.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            JLabel ruleLabel = new JLabel(rule);
            ruleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            ruleLabel.setForeground(new Color(220, 220, 255)); // Light violet text
            rulePanel.add(bullet);
            rulePanel.add(ruleLabel);
            contentPanel.add(rulePanel);
        }
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        rulesPanel.add(scrollPane, BorderLayout.CENTER);
        // Create custom dialog (matching message box)
        JDialog dialog = new JDialog(this, "Space Voyage Rules", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.add(rulesPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        // Add futuristic border (matching message box)
        rulesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        dialog.setVisible(true);
    }

    private void initializeGameComponents() {
        initializeComponents();
        generateStars();
        loadTargetImage();
        loadCollisionSound();
        loadGameMusic(); // Load game music
        loadGameBackgroundImage(); // Load game background
        // Create game mode label
        gameModeLabel = new JLabel("Mode: " + currentGameMode);
        gameModeLabel.setForeground(Color.WHITE);
        gameModeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gameModeLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        gameModeLabel.setOpaque(false);
        initializeNewGame();
        gameTimer = new javax.swing.Timer(16, this);
        gameTimer.start();
        setSize(WIDTH, HEIGHT + 150);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void loadTargetImage() {
        try {
            targetImage = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\target_3-removebg-preview.png"));
            // Load second target image if available
            try {
                targetImage2 = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\target_2.png"));
            } catch (Exception e2) {
                System.err.println("Could not load second target image: " + e2.getMessage());
                targetImage2 = null;
            }
            // Load third target image if available
            try {
                targetImage3 = ImageIO.read(new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\target 3 (2).png"));
            } catch (Exception e3) {
                System.err.println("Could not load third target image: " + e3.getMessage());
                targetImage3 = null;
            }
        } catch (Exception e) {
            System.err.println("Could not load target image: " + e.getMessage());
            targetImage = null;
        }
    }

    private void loadCollisionSound() {
        try {
            File soundFile = new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\Explosion sound effect (mp3cut.net).wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            collisionSound = AudioSystem.getClip();
            collisionSound.open(audioInputStream);
        } catch (Exception e) {
            System.err.println("Could not load sound file, using default sound: " + e.getMessage());
            createDefaultSound();
        }
    }

    private void loadGameMusic() {
        try {
            File musicFile = new File("C:\\Users\\Luigi Balingit\\OneDrive\\Desktop\\Java Program\\SpaceVoyage.java\\out\\production\\spacevoyage.java\\Dxrk ダーク - RAVE (Official Audio).wav"); // Assuming you have a game_music.wav in resources
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicFile);
            gameMusic = AudioSystem.getClip();
            gameMusic.open(audioIn);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultSound() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            byte[] audioData = new byte[44100 * 2];
            for (int i = 0; i < audioData.length/2; i++) {
                double frequency = 200 + (i < 11025 ? 300 * (1.0 - (double)i/11025) : 0);
                double amplitude = (1.0 - (double)i/(audioData.length/2)) * 0.7;
                double sample = Math.sin(2 * Math.PI * frequency * i / 44100) * amplitude;
                short sampleValue = (short)(sample * Short.MAX_VALUE);
                audioData[2*i] = (byte)sampleValue;
                audioData[2*i+1] = (byte)(sampleValue >> 8);
            }
            AudioInputStream audioInputStream = new AudioInputStream(
                    new ByteArrayInputStream(audioData), format, audioData.length/2);
            collisionSound = AudioSystem.getClip();
            collisionSound.open(audioInputStream);
        } catch (Exception e) {
            System.err.println("Could not create default sound: " + e.getMessage());
        }
    }

    private void playCollisionSound() {
        if (collisionSound != null) {
            collisionSound.setFramePosition(0);
            collisionSound.start();
        }
    }

    private void playGameMusic() {
        if (gameMusic != null && !gameMusic.isRunning()) {
            gameMusic.setFramePosition(0);
            gameMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void stopGameMusic() {
        if (gameMusic != null && gameMusic.isRunning()) {
            gameMusic.stop();
        }
    }

    private void pauseGameMusic() {
        if (gameMusic != null && gameMusic.isRunning()) {
            gameMusic.stop();
        }
    }

    private void initializeComponents() {
        // Control panel with futuristic design matching message box theme
        JPanel controlPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Black violet gradient background (matching message box)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(25, 0, 50), // Dark violet
                        0, getHeight(), new Color(0, 0, 0) // Black
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                // Subtle grid pattern (matching message box)
                g2d.setColor(new Color(100, 0, 150, 50));
                for (int i = 0; i < getWidth(); i += 15) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i < getHeight(); i += 15) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(138, 43, 226)), "Controls",
                0, 0, new Font("Arial", Font.BOLD, 14), new Color(138, 43, 226)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        // Velocity input with futuristic styling
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel velLabel = new JLabel("Velocity (m/s):");
        velLabel.setForeground(new Color(220, 220, 255)); // Light violet text
        velLabel.setFont(new Font("Arial", Font.BOLD, 13));
        controlPanel.add(velLabel, gbc);
        gbc.gridx = 1;
        velocityField = new JTextField("50", 10) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Custom border matching message box theme
                g2d.setColor(new Color(138, 43, 226)); // Blue violet border
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
            }
            @Override
            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 100)); // Semi-transparent black
                setForeground(Color.WHITE);
                setCaretColor(new Color(255, 215, 0)); // Gold caret
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        };
        controlPanel.add(velocityField, gbc);
        // Angle input with futuristic styling
        gbc.gridx = 2; gbc.gridy = 0;
        JLabel angleLabel = new JLabel("Angle (degrees):");
        angleLabel.setForeground(new Color(220, 220, 255)); // Light violet text
        angleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        controlPanel.add(angleLabel, gbc);
        gbc.gridx = 3;
        angleField = new JTextField("45", 10) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Custom border matching message box theme
                g2d.setColor(new Color(138, 43, 226)); // Blue violet border
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
            }
            @Override
            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 100)); // Semi-transparent black
                setForeground(Color.WHITE);
                setCaretColor(new Color(255, 215, 0)); // Gold caret
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                setFont(new Font("Arial", Font.PLAIN, 12));
            }
        };
        controlPanel.add(angleField, gbc);
        // Launch button with futuristic styling (matching start screen buttons)
        gbc.gridx = 4;
        launchButton = new JButton("Launch") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background (matching start screen buttons)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(106, 13, 173), // Dark violet
                        0, getHeight(), new Color(25, 25, 112) // Navy blue
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Glow effect
                g2d.setColor(new Color(148, 0, 255, 100)); // Purple glow
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(90, 35);
            }
        };
        launchButton.setContentAreaFilled(false);
        launchButton.setFocusPainted(false);
        launchButton.setBorderPainted(false);
        launchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        launchButton.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(launchButton, gbc);
        // Reset button with futuristic styling (matching start screen buttons)
        gbc.gridx = 5;
        resetButton = new JButton("Reset") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background (matching start screen buttons)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(138, 43, 226), // Blue violet
                        0, getHeight(), new Color(75, 0, 130) // Indigo
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Glow effect
                g2d.setColor(new Color(148, 0, 255, 100)); // Purple glow
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(90, 35);
            }
        };
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(resetButton, gbc);
        // New Game button with futuristic styling (matching start screen buttons)
        gbc.gridx = 6;
        newGameButton = new JButton("Relocate") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background (matching start screen buttons)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(25, 25, 112), // Navy blue
                        0, getHeight(), new Color(0, 0, 0) // Black
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Glow effect
                g2d.setColor(new Color(148, 0, 255, 100)); // Purple glow
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(110, 35);
            }
        };
        newGameButton.setContentAreaFilled(false);
        newGameButton.setFocusPainted(false);
        newGameButton.setBorderPainted(false);
        newGameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newGameButton.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(newGameButton, gbc);
        // Back to Menu button with futuristic styling
        gbc.gridx = 7;
        JButton backButton = new JButton("Back to Menu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background (matching start screen buttons)
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0, 0, 139), // Dark blue
                        0, getHeight(), new Color(0, 0, 50) // Very dark blue
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Glow effect
                g2d.setColor(new Color(148, 0, 255, 100)); // Purple glow
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 35);
            }
        };
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        controlPanel.add(backButton, gbc);
        // Add action listener for back button
        backButton.addActionListener(e -> {
            // Tanggalin ang game components
            remove(gamePanel);
            stopGameMusic(); // Stop game music when returning to start screen
            for (Component comp : getContentPane().getComponents()) {
                if (comp instanceof JPanel && comp != gamePanel) {
                    remove(comp);
                }
            }
            // I-reset ang score at rocket state
            initializeNewGame();
            // Ibalik ang start screen
            add(startPanel, BorderLayout.CENTER);
            // I-reset ang window size
            setSize(900, 650);
            setLocationRelativeTo(null);
            // I-play muli ang intro music kung naka-enable
            if (musicEnabled) {
                playIntroMusic();
            }
            revalidate();
            repaint();
        });
        // Score display with futuristic styling (matching message box theme)
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        scoreLabel = new JLabel("Score: 0") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background with transparency (matching message box)
                g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Border (matching message box)
                g2d.setColor(new Color(138, 43, 226)); // Blue violet border
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            }
        };
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setOpaque(false);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        controlPanel.add(scoreLabel, gbc);
        // Game mode indicator with futuristic styling
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3;
        gameModeLabel = new JLabel("Mode: " + currentGameMode) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background with transparency
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Border
                g2d.setColor(new Color(138, 43, 226));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            }
        };
        gameModeLabel.setForeground(Color.CYAN);
        gameModeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gameModeLabel.setOpaque(false);
        gameModeLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        controlPanel.add(gameModeLabel, gbc);
        // Instructions with futuristic styling (matching message box theme)
        gbc.gridx = 3; gbc.gridwidth = 4;
        instructionsLabel = new JLabel("<html>Enter velocity and angle, then click Launch. Hit targets for 50 points each!</html>") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background with transparency (matching message box)
                g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Border (matching message box)
                g2d.setColor(new Color(138, 43, 226)); // Blue violet border
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            }
        };
        instructionsLabel.setForeground(new Color(186, 85, 211)); // Medium orchid text
        instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionsLabel.setOpaque(false);
        instructionsLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        controlPanel.add(instructionsLabel, gbc);
        add(controlPanel, BorderLayout.NORTH);
        // Game panel with custom background
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);
        // Add action listeners
        launchButton.addActionListener(e -> launchRocket());
        resetButton.addActionListener(e -> resetRocket());
        newGameButton.addActionListener(e -> newGame());
    }

    private void generateStars() {
        stars.clear();
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            stars.add(new Star(rand.nextInt(WIDTH), rand.nextInt(HEIGHT - GROUND_HEIGHT)));
        }
    }

    private void launchRocket() {
        if (gameRunning) return;
        try {
            double velocity = Double.parseDouble(velocityField.getText());
            double angle = Math.toRadians(Double.parseDouble(angleField.getText()));
            if (velocity <= 0 || velocity > 200) {
                // Create custom futuristic dialog
                JDialog dialog = new JDialog(this, "Warning", true);
                dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                dialog.setLayout(new BorderLayout());
                // Create futuristic panel
                JPanel panel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        // Dark violet to black gradient
                        GradientPaint gradient = new GradientPaint(
                                0, 0, new Color(25, 0, 50),
                                0, getHeight(), new Color(0, 0, 0)
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                        // Grid pattern
                        g2d.setColor(new Color(100, 0, 150, 50));
                        for (int i = 0; i < getWidth(); i += 15) {
                            g2d.drawLine(i, 0, i, getHeight());
                        }
                        for (int i = 0; i < getHeight(); i += 15) {
                            g2d.drawLine(0, i, getWidth(), i);
                        }
                    }
                };
                panel.setLayout(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
                panel.setPreferredSize(new Dimension(400, 150));
                // Header
                JLabel header = new JLabel("WARNING!", JLabel.CENTER);
                header.setFont(new Font("Arial", Font.BOLD, 24));
                header.setForeground(new Color(255, 50, 50)); // Red
                header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
                panel.add(header, BorderLayout.NORTH);
                // Message content
                JLabel message = new JLabel(
                        "<html><div style='text-align: center;'>" +
                                "Velocity must be between<br>1 and 200 m/s" +
                                "</div></html>",
                        JLabel.CENTER
                );
                message.setFont(new Font("Arial", Font.PLAIN, 16));
                message.setForeground(new Color(255, 150, 150)); // Light red
                panel.add(message, BorderLayout.CENTER);
                // OK button with futuristic styling
                JButton okButton = new JButton("ACKNOWLEDGED") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        GradientPaint gradient = new GradientPaint(
                                0, 0, new Color(106, 13, 173),
                                0, getHeight(), new Color(25, 25, 112)
                        );
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                        g2d.setColor(new Color(148, 0, 255, 100));
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(getFont());
                        FontMetrics fm = g2d.getFontMetrics();
                        String text = getText();
                        int x = (getWidth() - fm.stringWidth(text)) / 2;
                        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                        g2d.drawString(text, x, y);
                        g2d.dispose();
                    }
                };
                okButton.setContentAreaFilled(false);
                okButton.setFocusPainted(false);
                okButton.setBorderPainted(false);
                okButton.setFont(new Font("Arial", Font.BOLD, 14));
                okButton.setPreferredSize(new Dimension(180, 40));
                okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                JPanel buttonPanel = new JPanel();
                buttonPanel.setOpaque(false);
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                buttonPanel.add(okButton);
                panel.add(buttonPanel, BorderLayout.SOUTH);
                dialog.add(panel, BorderLayout.CENTER);
                // Add futuristic border
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(138, 43, 226), 2),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
                okButton.addActionListener(e -> dialog.dispose());
                dialog.add(panel, BorderLayout.CENTER);
                dialog.pack();
                dialog.setLocationRelativeTo(this);
                dialog.setResizable(false);
                dialog.setVisible(true);
                return;
            }
            rocket = new Rocket(50, HEIGHT - GROUND_HEIGHT - 50, velocity, angle);
            gameRunning = true;
            launchButton.setEnabled(false);
        } catch (NumberFormatException e) {
            // Create custom futuristic dialog
            JDialog dialog = new JDialog(this, "Error", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            // Create futuristic panel
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Dark violet to black gradient
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(25, 0, 50),
                            0, getHeight(), new Color(0, 0, 0)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    // Grid pattern
                    g2d.setColor(new Color(100, 0, 150, 50));
                    for (int i = 0; i < getWidth(); i += 15) {
                        g2d.drawLine(i, 0, i, getHeight());
                    }
                    for (int i = 0; i < getHeight(); i += 15) {
                        g2d.drawLine(0, i, getWidth(), i);
                    }
                }
            };
            panel.setLayout(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            panel.setPreferredSize(new Dimension(400, 150));
            // Header
            JLabel header = new JLabel("ERROR!", JLabel.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 24));
            header.setForeground(new Color(255, 50, 50)); // Red
            header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            panel.add(header, BorderLayout.NORTH);
            // Message content
            JLabel message = new JLabel(
                    "<html><div style='text-align: center;'>" +
                            "Please enter valid numbers<br>for velocity and angle" +
                            "</div></html>",
                    JLabel.CENTER
            );
            message.setFont(new Font("Arial", Font.PLAIN, 16));
            message.setForeground(new Color(255, 150, 150)); // Light red
            panel.add(message, BorderLayout.CENTER);
            // OK button with futuristic styling
            JButton okButton = new JButton("UNDERSTOOD") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(106, 13, 173),
                            0, getHeight(), new Color(25, 25, 112)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2d.setColor(new Color(148, 0, 255, 100));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(getFont());
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = getText();
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2d.drawString(text, x, y);
                    g2d.dispose();
                }
            };
            okButton.setContentAreaFilled(false);
            okButton.setFocusPainted(false);
            okButton.setBorderPainted(false);
            okButton.setFont(new Font("Arial", Font.BOLD, 14));
            okButton.setPreferredSize(new Dimension(180, 40));
            okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            buttonPanel.add(okButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            dialog.add(panel, BorderLayout.CENTER);
            // Add futuristic border
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(138, 43, 226), 2),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            okButton.addActionListener(e2 -> dialog.dispose());
            dialog.add(panel, BorderLayout.CENTER);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);
            dialog.setVisible(true);
        }
    }

    private void resetRocket() {
        rocket = null;
        gameRunning = false;
        launchButton.setEnabled(true);
        explosions.clear();
    }

    private void initializeNewGame() {
        rocket = null;
        gameRunning = false;
        score = 0;
        updateScore();
        applyGameModeSettings();
        explosions.clear();
    }

    // Enhanced save player data method
    private void savePlayerData(String username, String gameMode, int level, int score) {
        // First test connection
        if (!testDatabaseConnection()) {
            System.err.println("Cannot save data - no database connection");
            JOptionPane.showMessageDialog(this,
                    "Could not save score. Database connection failed.",
                    "Save Failed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO players (username, game_mode, level, score) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, gameMode);
            pstmt.setInt(3, level); // currentLevel (hardcoded as 1 in your game)
            pstmt.setInt(4, score);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Player data saved successfully!");
                System.out.println("  Username: " + username);
                System.out.println("  Game Mode: " + gameMode);
                System.out.println("  Level: " + level);
                System.out.println("  Score: " + score);
                // Show success message to user
                JOptionPane.showMessageDialog(this,
                        "Score saved successfully!\nPlayer: " + username + "\nScore: " + score,
                        "Score Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("✗ Failed to save player data: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to save score to database.\nError: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Updated showPlayerList method with better error handling
    private void showPlayerList() {
        JDialog playerListDialog = new JDialog(this, "Player Scores", true);
        playerListDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        playerListDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Top Players", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(138, 43, 226));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table setup
        JTable playerTable = new JTable();
        playerTable.setFont(new Font("Arial", Font.PLAIN, 14));
        playerTable.setBackground(new Color(75, 0, 130));
        playerTable.setForeground(Color.WHITE);
        playerTable.setGridColor(new Color(75, 0, 130));
        playerTable.getTableHeader().setBackground(new Color(75, 0, 130));
        playerTable.getTableHeader().setForeground(Color.BLACK);
        playerTable.setRowHeight(25);

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Rank");
        tableModel.addColumn("Username");
        tableModel.addColumn("Game Mode");
        tableModel.addColumn("Level");
        tableModel.addColumn("Score");
        tableModel.addColumn("Date");

        String sql = "SELECT username, game_mode, level, score, DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') as play_date " +
                "FROM players ORDER BY score DESC LIMIT 20";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int rank = 1;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                tableModel.addRow(new Object[]{
                        rank++,
                        rs.getString("username"),
                        rs.getString("game_mode"),
                        rs.getInt("level"),
                        rs.getInt("score"),
                        rs.getString("play_date")
                });
            }

            if (!hasData) {
                tableModel.addRow(new Object[]{"", "No scores yet", "", "", "", ""});
            }

        } catch (SQLException e) {
            System.err.println("Error loading player data: " + e.getMessage());
            e.printStackTrace();

            // Show error in table
            tableModel.addRow(new Object[]{"", "Error loading data", e.getMessage(), "", "", ""});

            JOptionPane.showMessageDialog(playerListDialog,
                    "Error connecting to database:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        playerTable.setModel(tableModel);

        JScrollPane scrollPane = new JScrollPane(playerTable);
        scrollPane.getViewport().setBackground(Color.DARK_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Close button with futuristic styling (matching other game buttons)
        JButton closeButton = new JButton("CLOSE") { // Binago ang text sa "DISMISS" para mas pormal
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background (matching other buttons - indigo to dark blue)
                GradientPaint gradient = new GradientPaint(0, 0, new Color(106, 13, 173), // Indigo
                        0, getHeight(), new Color(25, 25, 112)); // Dark Blue
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Parehong radius na 10

                // Glow effect (purple glow)
                g2d.setColor(new Color(148, 0, 255, 100)); // Purple glow
                g2d.setStroke(new BasicStroke(2)); // Parehong stroke na 2
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10); // Parehong radius

                // Button text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12)); // Parehong font size na 12
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);
                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                // Mas maliit na size na tumutugma sa ibang control buttons
                return new Dimension(100, 35); // Width 100, Height 35
            }
        };
        closeButton.setContentAreaFilled(false); // Mahalaga para gumana ang custom paint
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Parehong cursor
        closeButton.addActionListener(e -> playerListDialog.dispose());

// ... (code pagkatapos ng closeButton) ...
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        playerListDialog.add(panel);
        playerListDialog.setSize(700, 500);
        playerListDialog.setLocationRelativeTo(this);
        playerListDialog.setVisible(true);
    }

    // Enhanced test connection method
    private boolean testDatabaseConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Test if the players table exists
                String testQuery = "SELECT COUNT(*) FROM players";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(testQuery)) {
                    System.out.println("Players table exists and is accessible");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            // Show detailed error message to user
            String errorMessage = "<html><body width='300'>" +
                    "<h2>Database Connection Failed!</h2>" +
                    "<p><b>Error:</b> " + e.getMessage() + "</p>" +
                    "<p><b>Error Code:</b> " + e.getErrorCode() + "</p>" +
                    "<p><b>SQL State:</b> " + e.getSQLState() + "</p>";

            if (e.getMessage().contains("Unknown database")) {
                errorMessage += "<p><b>Solution:</b> Create the 'suptodo' database in MySQL Workbench.</p>";
            } else if (e.getMessage().contains("Access denied")) {
                errorMessage += "<p><b>Solution:</b> Check your username and password.</p>";
            } else if (e.getMessage().contains("Communications link failure")) {
                errorMessage += "<p><b>Solution:</b> Make sure MySQL server is running.</p>";
            } else {
                errorMessage += "<p>Please check your MySQL server and database configuration.</p>";
            }
            errorMessage += "</body></html>";
            JOptionPane.showMessageDialog(null, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void applyGameModeSettings() {
        // Apply settings based on game mode
        switch (currentGameMode) {
            case "Easy":
                // Easy mode settings
                currentGravity = GRAVITY * 0.7; // Lower gravity
                generateTargets(5); // More targets
                break;
            case "Medium":
                // Medium mode settings (more challenging than Easy)
                currentGravity = GRAVITY * 0.9; // Slightly higher than Easy but lower than default
                generateTargets(4); // More targets than Hard, fewer than Easy
                // Add some moving targets for medium difficulty
                for (Target target : targets) {
                    if (Math.random() < 0.5) { // 50% chance for each target to move
                        target.setMoving(true);
                        target.setSpeed(1); // Slower than Hard mode
                    }
                }
                break;
            case "Hard":
                // Hard mode settings
                currentGravity = GRAVITY * 1.3; // Higher gravity
                generateTargets(3); // Fewer targets
                break;
            default:
                // Default to medium if something goes wrong
                currentGravity = GRAVITY;
                generateTargets(3);
                break;
        }
        // Update game mode label if it exists
        if (gameModeLabel != null) {
            gameModeLabel.setText("Mode: " + currentGameMode);
        }
    }

    private void newGame() {
        resetRocket();
        score = 0;
        updateScore();
        applyGameModeSettings();
    }

    private void generateTargets(int targetCount) {
        targets.clear();
        Random rand = new Random();
        for (int i = 0; i < targetCount; i++) {
            boolean validPosition = false;
            int attempts = 0;
            int x = 0, y = 0;
            while (!validPosition && attempts < 100) {
                x = rand.nextInt(WIDTH - TARGET_SIZE - 200) + 200;
                y = rand.nextInt(HEIGHT - GROUND_HEIGHT - TARGET_SIZE - 100) + 50;
                Rectangle newTargetBounds = new Rectangle(x, y, TARGET_SIZE, TARGET_SIZE);
                validPosition = true;
                for (Target target : targets) {
                    if (newTargetBounds.intersects(target.getBounds())) {
                        validPosition = false;
                        break;
                    }
                }
                attempts++;
            }
            if (attempts < 100) {
                Target newTarget = new Target(x, y);
                // Apply difficulty-specific target behavior
                if (currentGameMode.equals("Hard")) {
                    newTarget.setMoving(true);
                    newTarget.setSpeed(rand.nextInt(3) + 3); // Random speed 1-3
                } else if (currentGameMode.equals("Medium")) {
                    // Medium mode has a chance for moving targets (handled in applyGameModeSettings)
                }
                // Easy mode has no moving targets
                targets.add(newTarget);
            }
        }
    }

    // For backward compatibility
    private void generateTargets() {
        generateTargets(3); // Default to 3 targets
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
        // Repaint to update the custom background
        scoreLabel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // --- UPDATE TARGETS FIRST (kahit hindi pa na-launch) ---
        for (Target target : targets) {
            target.update();
        }
        // --- UPDATE ROCKET ONLY IF GAME IS RUNNING ---
        if (gameRunning && rocket != null) {
            rocket.update();
            if (rocket.x > WIDTH + 100 || rocket.y > HEIGHT) {
                resetRocket();
                return;
            }
            Iterator<Target> targetIter = targets.iterator();
            while (targetIter.hasNext()) {
                Target target = targetIter.next();
                if (rocket.getBounds().intersects(target.getBounds())) {
                    // Create enhanced explosion effects
                    explosions.add(new Explosion(target.x + TARGET_SIZE/2, target.y + TARGET_SIZE/2));
                    // Add secondary explosions for chain reaction effect
                    explosions.add(new Explosion(target.x + TARGET_SIZE/4, target.y + TARGET_SIZE/4));
                    explosions.add(new Explosion(target.x + TARGET_SIZE*3/4, target.y + TARGET_SIZE*3/4));
                    targetIter.remove();
                    // Score based on game mode
                    int scoreValue;
                    switch (currentGameMode) {
                        case "Easy":
                            scoreValue = 30; // Lower score for easy mode
                            break;
                        case "Hard":
                            scoreValue = 100; // Higher score for hard mode
                            break;
                        default: // Medium
                            scoreValue = 70; // Increased score for medium mode to reflect moving targets
                            break;
                    }
                    score += scoreValue;
                    updateScore();
                    playCollisionSound();
                    resetRocket();
                    if (targets.isEmpty()) {
                        // Show futuristic congratulatory message box
                        JDialog dialog = new JDialog(this, "Mission Complete", true);
                        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                        dialog.setLayout(new BorderLayout());
                        stopGameMusic(); // Stop game music when game is complete
                        // Save player data to database
                        savePlayerData(this.currentUsername, currentGameMode, currentLevel, score);
                        // Create futuristic panel
                        JPanel panel = new JPanel() {
                            @Override
                            protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2d = (Graphics2D) g;
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                // Dark violet to black gradient
                                GradientPaint gradient = new GradientPaint(
                                        0, 0, new Color(25, 0, 50),
                                        0, getHeight(), new Color(0, 0, 0)
                                );
                                g2d.setPaint(gradient);
                                g2d.fillRect(0, 0, getWidth(), getHeight());
                                // Grid pattern
                                g2d.setColor(new Color(100, 0, 150, 50));
                                for (int i = 0; i < getWidth(); i += 15) {
                                    g2d.drawLine(i, 0, i, getHeight());
                                }
                                for (int i = 0; i < getHeight(); i += 15) {
                                    g2d.drawLine(0, i, getWidth(), i);
                                }
                            }
                        };
                        panel.setLayout(new BorderLayout());
                        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
                        panel.setPreferredSize(new Dimension(400, 200));
                        // Header
                        JLabel header = new JLabel("MISSION ACCOMPLISHED!", JLabel.CENTER);
                        header.setFont(new Font("Arial", Font.BOLD, 24));
                        header.setForeground(new Color(255, 215, 0)); // Gold
                        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
                        panel.add(header, BorderLayout.NORTH);
                        // Message content
                        JLabel message = new JLabel(
                                "<html><div style='text-align: center;'>" +
                                        "<span style='font-size: 18px; color: cyan;'>Congratulations!</span><br><br>" +
                                        "All targets have been destroyed!<br><br>" +
                                        "<span style='font-size: 20px; color: yellow;'>Final Score: " + score + "</span>" +
                                        "</div></html>",
                                JLabel.CENTER
                        );
                        message.setFont(new Font("Arial", Font.PLAIN, 14));
                        message.setForeground(new Color(220, 220, 255));
                        panel.add(message, BorderLayout.CENTER);
                        // OK button with futuristic styling
                        JButton okButton = new JButton("CONTINUE MISSION") {
                            @Override
                            protected void paintComponent(Graphics g) {
                                Graphics2D g2d = (Graphics2D) g.create();
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                GradientPaint gradient = new GradientPaint(
                                        0, 0, new Color(106, 13, 173),
                                        0, getHeight(), new Color(25, 25, 112)
                                );
                                g2d.setPaint(gradient);
                                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                                g2d.setColor(new Color(148, 0, 255, 100));
                                g2d.setStroke(new BasicStroke(2));
                                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                                g2d.setColor(Color.WHITE);
                                g2d.setFont(getFont());
                                FontMetrics fm = g2d.getFontMetrics();
                                String text = getText();
                                int x = (getWidth() - fm.stringWidth(text)) / 2;
                                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                                g2d.drawString(text, x, y);
                                g2d.dispose();
                            }
                        };
                        okButton.setContentAreaFilled(false);
                        okButton.setFocusPainted(false);
                        okButton.setBorderPainted(false);
                        okButton.setFont(new Font("Arial", Font.BOLD, 14));
                        okButton.setPreferredSize(new Dimension(180, 40));
                        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        JPanel buttonPanel = new JPanel();
                        buttonPanel.setOpaque(false);
                        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                        buttonPanel.add(okButton);
                        panel.add(buttonPanel, BorderLayout.SOUTH);
                        dialog.add(panel, BorderLayout.CENTER);
                        // Add futuristic border
                        panel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(138, 43, 226), 2),
                                BorderFactory.createEmptyBorder(20, 20, 20, 20)
                        ));
                        okButton.addActionListener(e2 -> {
                            dialog.dispose();
                            // --- START ng Bagong Code ---
                            // Tanggalin ang game components
                            remove(gamePanel);
                            for (Component comp : getContentPane().getComponents()) {
                                if (comp instanceof JPanel && comp != gamePanel) {
                                    remove(comp);
                                }
                            }
                            // I-reset ang score at rocket state
                            initializeNewGame();
                            // Ibalik ang start screen
                            add(startPanel, BorderLayout.CENTER);
                            // I-reset ang window size
                            setSize(900, 650);
                            setLocationRelativeTo(null);
                            // I-play muli ang intro music kung naka-enable
                            if (musicEnabled) {
                                playIntroMusic();
                            }
                            revalidate();
                            repaint();
                            // --- END ng Bagong Code ---
                        });
                        dialog.add(panel, BorderLayout.CENTER);
                        dialog.pack();
                        dialog.setLocationRelativeTo(this);
                        dialog.setResizable(false);
                        dialog.setVisible(true);
                        // Alisin ang `newGame();` dito dahil ginagawa na sa action ng button
                    }
                    break;
                }
            }
        }
        explosions.removeIf(explosion -> {
            explosion.update();
            return explosion.isFinished();
        });
        gamePanel.repaint();
    }

    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Draw custom background image
            if (gameBackgroundImage != null) {
                g2d.drawImage(gameBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback background with futuristic theme
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(25, 0, 50), // Dark violet
                        0, getHeight(), new Color(0, 0, 0) // Black
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            // Draw stars overlay
            g2d.setColor(Color.WHITE);
            for (Star star : stars) {
                g2d.fillOval(star.x, star.y, 2, 2);
            }
            // Draw ground
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
            // Draw launcher
            g2d.setColor(Color.GRAY);
            g2d.fillRect(30, HEIGHT - GROUND_HEIGHT - 30, 40, 30);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(45, HEIGHT - GROUND_HEIGHT - 50, 10, 20);
            // Draw targets
            for (Target target : targets) {
                target.draw(g2d);
            }
            // Draw rocket
            if (rocket != null) {
                rocket.draw(g2d);
            }
            // Draw explosions
            for (Explosion explosion : explosions) {
                explosion.draw(g2d);
            }
            // Draw trajectory path
            if (rocket != null) {
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.setStroke(new BasicStroke(1));
                int prevX = (int) rocket.startX;
                int prevY = (int) rocket.startY;
                for (double t = 0; t <= rocket.time; t += 0.1) {
                    int x = (int) (rocket.startX + rocket.vx * t);
                    int y = (int) (rocket.startY - rocket.vy * t + 0.5 * GRAVITY * t * t);
                    g2d.drawLine(prevX, prevY, x, y);
                    prevX = x;
                    prevY = y;
                }
            }
        }
    }

    private class Rocket {
        double x, y, startX, startY;
        double vx, vy;
        double time = 0;
        public Rocket(double startX, double startY, double velocity, double angle) {
            this.startX = startX;
            this.startY = startY;
            this.x = startX;
            this.y = startY;
            this.vx = velocity * Math.cos(angle);
            this.vy = velocity * Math.sin(angle);
        }
        public void update() {
            time += 0.016;
            x = startX + vx * time;
            y = startY - vy * time + 0.5 * currentGravity * time * time; // Use currentGravity instead of GRAVITY
        }
        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.RED);
            g2d.fillOval((int)x - ROCKET_SIZE/2, (int)y - ROCKET_SIZE/2, ROCKET_SIZE, ROCKET_SIZE);
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int)x - ROCKET_SIZE/3, (int)y - ROCKET_SIZE/3, ROCKET_SIZE*2/3, ROCKET_SIZE*2/3);
        }
        public Rectangle getBounds() {
            return new Rectangle((int)x - ROCKET_SIZE/2, (int)y - ROCKET_SIZE/2, ROCKET_SIZE, ROCKET_SIZE);
        }
    }

    private class Target {
        int x, y;
        Color color;
        int targetType; // 0, 1, or 2 for different target images
        boolean isMoving = false;
        int speed = 1;
        int direction = 1; // 1 for right, -1 for left
        public Target(int x, int y) {
            this.x = x;
            this.y = y;
            Random rand = new Random();
            Color[] colors = {Color.GREEN, Color.MAGENTA, Color.CYAN, new Color(255, 165, 0)};
            this.color = colors[rand.nextInt(colors.length)];
            this.targetType = rand.nextInt(3); // Randomly assign one of three target types
        }
        public void setMoving(boolean moving) {
            this.isMoving = moving;
        }
        public void setSpeed(int speed) {
            this.speed = speed;
        }
        public void update() {
            if (isMoving) {
                x += speed * direction;
                // Reverse direction if hitting screen edges with proper boundary checking
                if (x <= 0) {
                    // Hit left edge - bounce right
                    x = 0;
                    direction = 1;
                } else if (x >= WIDTH - TARGET_SIZE) {
                    // Hit right edge - bounce left
                    x = WIDTH - TARGET_SIZE;
                    direction = -1;
                }
            }
        }
        public void draw(Graphics2D g2d) {
            // Set rendering hints for better image quality
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // Choose which image to use based on targetType
            if (targetType == 0 && targetImage != null) {
                g2d.drawImage(targetImage, x, y, TARGET_SIZE, TARGET_SIZE, null);
            } else if (targetType == 1 && targetImage2 != null) {
                g2d.drawImage(targetImage2, x, y, TARGET_SIZE, TARGET_SIZE, null);
            } else if (targetType == 2 && targetImage3 != null) {
                g2d.drawImage(targetImage3, x, y, TARGET_SIZE, TARGET_SIZE, null);
            } else {
                // Fallback to colored shapes if images aren't available
                g2d.setColor(color);
                g2d.fillRect(x, y + TARGET_SIZE/3, TARGET_SIZE, TARGET_SIZE/3);
                g2d.fillOval(x + TARGET_SIZE/4, y, TARGET_SIZE/2, TARGET_SIZE/2);
                g2d.setColor(color.brighter());
                g2d.fillOval(x + TARGET_SIZE/3, y + TARGET_SIZE/6, TARGET_SIZE/3, TARGET_SIZE/6);
            }
        }
        public Rectangle getBounds() {
            return new Rectangle(x, y, TARGET_SIZE, TARGET_SIZE);
        }
    }

    private class Explosion {
        int x, y;
        int frame = 0;
        int maxFrames = 40; // Extended duration
        List<Particle> particles; // Add particle system
        public Explosion(int x, int y) {
            this.x = x;
            this.y = y;
            this.particles = new ArrayList<>();
            // Create particles for more dramatic effect
            Random rand = new Random();
            for (int i = 0; i < 50; i++) {
                double angle = rand.nextDouble() * 2 * Math.PI;
                double speed = rand.nextDouble() * 8 + 2;
                Color color = new Color[]{
                        Color.YELLOW, Color.ORANGE, Color.RED, Color.MAGENTA
                }[rand.nextInt(4)];
                particles.add(new Particle(x, y, angle, speed, color));
            }
        }
        public void update() {
            frame++;
            // Update particles
            for (Particle p : particles) {
                p.update();
            }
        }
        public void draw(Graphics2D g2d) {
            if (frame > maxFrames) return;
            float progress = (float) frame / maxFrames;
            int size = (int) (100 * progress); // Larger explosion
            int alpha = (int) (255 * (1 - progress));
            // Draw multiple concentric circles for shockwave effect
            Color[] colors = {
                    new Color(255, 255, 0, Math.min(alpha, 200)), // Yellow core
                    new Color(255, 165, 0, Math.min(alpha, 150)), // Orange ring
                    new Color(255, 0, 0, Math.min(alpha, 100)),   // Red outer ring
                    new Color(139, 0, 0, Math.min(alpha, 50))     // Dark red fade
            };
            for (int i = 0; i < colors.length; i++) {
                g2d.setColor(colors[i]);
                int circleSize = size - i * 15;
                if (circleSize > 0) {
                    g2d.fillOval(x - circleSize/2, y - circleSize/2, circleSize, circleSize);
                }
            }
            // Draw particles
            for (Particle p : particles) {
                p.draw(g2d);
            }
            // Add star burst effect
            if (frame < 20) {
                g2d.setColor(new Color(255, 255, 200, 200 - frame * 10));
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int length = (int)(20 * (1 - progress));
                    int endX = (int)(x + Math.cos(angle) * length);
                    int endY = (int)(y + Math.sin(angle) * length);
                    g2d.drawLine(x, y, endX, endY);
                }
            }
        }
        public boolean isFinished() {
            return frame > maxFrames;
        }
        // Particle class for explosion debris
        private class Particle {
            double x, y, vx, vy;
            Color color;
            int life;
            public Particle(double x, double y, double angle, double speed, Color color) {
                this.x = x;
                this.y = y;
                this.vx = Math.cos(angle) * speed;
                this.vy = Math.sin(angle) * speed;
                this.color = color;
                this.life = 30 + (int)(Math.random() * 20);
            }
            public void update() {
                x += vx;
                y += vy;
                vy += 0.2; // Gravity effect
                life--;
            }
            public void draw(Graphics2D g2d) {
                if (life > 0) {
                    int alpha = Math.min(255, life * 8);
                    Color fadedColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            alpha
                    );
                    g2d.setColor(fadedColor);
                    g2d.fillOval((int)x-2, (int)y-2, 4, 4);
                }
            }
        }
    }

    private class Star {
        int x, y;
        public Star(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SpaceVoyageGame();
        });
    }
}
