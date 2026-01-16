import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Battleship extends JFrame {

    // --- GAME CONFIGURATION ---
    private static final int SIZE = 10;
    private static final int[] SHIP_LENGTHS = {5, 4, 3, 3, 2};
    private static final String[] SHIP_NAMES = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    
    // Colors
    private final Color COLOR_WATER = new Color(30, 144, 255); // Deep Sky Blue
    private final Color COLOR_SHIP = Color.GRAY;
    private final Color COLOR_HIT = Color.RED;
    private final Color COLOR_MISS = Color.WHITE;
    private final Color COLOR_DEFAULT = new Color(220, 220, 220); // Light Gray for fog

    // --- DATA STRUCTURES ---
    // '0' = Empty, 'S' = Ship, 'X' = Hit, 'M' = Miss
    private char[][] p1Data = new char[SIZE][SIZE];
    private char[][] p2Data = new char[SIZE][SIZE];
    
    // GUI Components
    private JButton[][] gridButtons = new JButton[SIZE][SIZE];
    private JLabel statusLabel;
    private JButton actionButton; // Used for "Next Turn" or "Rotate"

    // State Machine
    private boolean isPlayer1Turn = true; // True = P1, False = P2
    private boolean isSetupPhase = true;
    private int currentShipIndex = 0;
    private boolean isHorizontal = true; // For placement

    public Battleship() {
        setTitle("Battleship - 2 Player");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Initialize Data Grids
        initGridData(p1Data);
        initGridData(p2Data);

        // 2. Setup Top Status Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Player 1 Setup: Place your Carrier (Length 5)", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        actionButton = new JButton("Rotate (Horizontal)");
        actionButton.addActionListener(e -> toggleOrientation());
        
        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.add(actionButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 3. Setup Main Grid
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                JButton btn = new JButton();
                btn.setBackground(COLOR_WATER);
                btn.setOpaque(true);
                btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                
                final int row = r;
                final int col = c;
                btn.addActionListener(e -> handleGridClick(row, col));
                
                gridButtons[r][c] = btn;
                gridPanel.add(btn);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
        
        // Start Game
        setVisible(true);
    }

    // --- CORE LOGIC HANDLERS ---

    private void handleGridClick(int row, int col) {
        if (isSetupPhase) {
            handleSetupClick(row, col);
        } else {
            handleBattleClick(row, col);
        }
    }

    private void handleSetupClick(int row, int col) {
        char[][] currentBoard = isPlayer1Turn ? p1Data : p2Data;
        int size = SHIP_LENGTHS[currentShipIndex];

        // 1. Try to place ship
        if (placeShipLogic(currentBoard, row, col, size, isHorizontal)) {
            // 2. Update visual grid immediately for the player to see
            renderBoard(currentBoard, true); // True = Reveal Ships
            currentShipIndex++;

            // 3. Check if done with this player's fleet
            if (currentShipIndex >= SHIP_LENGTHS.length) {
                if (isPlayer1Turn) {
                    // Switch to Player 2 Setup
                    JOptionPane.showMessageDialog(this, "Player 1 Setup Complete!\nPass control to Player 2.");
                    isPlayer1Turn = false;
                    currentShipIndex = 0;
                    statusLabel.setText("Player 2 Setup: Place your Carrier (Length 5)");
                    renderBoard(p2Data, true); // Clear screen, show P2 empty board
                } else {
                    // Switch to Battle Phase
                    JOptionPane.showMessageDialog(this, "Setup Complete! Let the Battle Begin!");
                    isSetupPhase = false;
                    isPlayer1Turn = true;
                    actionButton.setText("End Turn");
                    // We remove the rotate listener and add the turn switch listener
                    for(ActionListener al : actionButton.getActionListeners()) actionButton.removeActionListener(al);
                    actionButton.addActionListener(e -> switchTurn());
                    actionButton.setEnabled(false); // Only enable after a shot
                    
                    startTurn();
                }
            } else {
                statusLabel.setText((isPlayer1Turn ? "Player 1" : "Player 2") + 
                    " Setup: Place your " + SHIP_NAMES[currentShipIndex] + " (Length " + SHIP_LENGTHS[currentShipIndex] + ")");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Placement! Overlap or Out of Bounds.");
        }
    }

    private void handleBattleClick(int row, int col) {
        // Identify whose board we are shooting at
        char[][] targetBoard = isPlayer1Turn ? p2Data : p1Data;

        // Prevent shooting same spot or shooting yourself
        if (targetBoard[row][col] == 'X' || targetBoard[row][col] == 'M') {
            return;
        }

        // Fire logic
        if (targetBoard[row][col] == 'S') {
            targetBoard[row][col] = 'X';
            gridButtons[row][col].setBackground(COLOR_HIT);
            statusLabel.setText("HIT!");
            checkWinCondition();
        } else {
            targetBoard[row][col] = 'M';
            gridButtons[row][col].setBackground(COLOR_MISS);
            statusLabel.setText("MISS!");
        }

        // Lock grid until they click "End Turn"
        disableGrid();
        actionButton.setEnabled(true);
    }

    private void startTurn() {
        String pName = isPlayer1Turn ? "Player 1" : "Player 2";
        statusLabel.setText(pName + "'s Turn to Fire!");
        actionButton.setEnabled(false);
        enableGrid();
        
        // Show the ENEMY board, but hide ships
        char[][] enemyBoard = isPlayer1Turn ? p2Data : p1Data;
        renderBoard(enemyBoard, false); // False = Fog of War (Don't show 'S')
    }

    private void switchTurn() {
        // Swap players
        isPlayer1Turn = !isPlayer1Turn;
        
        // Interstitial Screen to prevent cheating
        renderBlank();
        JOptionPane.showMessageDialog(this, "Pass the device to " + (isPlayer1Turn ? "Player 1" : "Player 2"));
        
        startTurn();
    }

    // --- HELPER LOGIC ---

    private boolean placeShipLogic(char[][] grid, int r, int c, int size, boolean horiz) {
        // Bounds Check
        if (horiz && c + size > SIZE) return false;
        if (!horiz && r + size > SIZE) return false;

        // Overlap Check
        if (horiz) {
            for (int i = 0; i < size; i++) if (grid[r][c + i] != '0') return false;
        } else {
            for (int i = 0; i < size; i++) if (grid[r + i][c] != '0') return false;
        }

        // Place
        if (horiz) {
            for (int i = 0; i < size; i++) grid[r][c + i] = 'S';
        } else {
            for (int i = 0; i < size; i++) grid[r + i][c] = 'S';
        }
        return true;
    }

    // Updates the GUI buttons based on the data array
    private void renderBoard(char[][] grid, boolean revealShips) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                char val = grid[r][c];
                JButton btn = gridButtons[r][c];
                
                if (val == 'S') {
                    if (revealShips) btn.setBackground(COLOR_SHIP);
                    else btn.setBackground(COLOR_WATER); // Fog of War
                } else if (val == 'X') {
                    btn.setBackground(COLOR_HIT);
                } else if (val == 'M') {
                    btn.setBackground(COLOR_MISS);
                } else {
                    btn.setBackground(COLOR_WATER);
                }
            }
        }
    }
    
    // Hides everything between turns
    private void renderBlank() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                gridButtons[r][c].setBackground(COLOR_DEFAULT);
            }
        }
    }

    private void checkWinCondition() {
        char[][] targetBoard = isPlayer1Turn ? p2Data : p1Data;
        boolean hasShipsLeft = false;
        
        for(int r=0; r<SIZE; r++) {
            for(int c=0; c<SIZE; c++) {
                if(targetBoard[r][c] == 'S') {
                    hasShipsLeft = true;
                    break;
                }
            }
        }
        
        if(!hasShipsLeft) {
            String winner = isPlayer1Turn ? "Player 1" : "Player 2";
            JOptionPane.showMessageDialog(this, "GAME OVER! " + winner + " WINS!");
            System.exit(0);
        }
    }

    private void toggleOrientation() {
        isHorizontal = !isHorizontal;
        actionButton.setText(isHorizontal ? "Rotate (Horizontal)" : "Rotate (Vertical)");
    }
    
    private void initGridData(char[][] grid) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = '0';
            }
        }
    }
    
    private void disableGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                gridButtons[r][c].setEnabled(false);
            }
        }
    }
    
    private void enableGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                gridButtons[r][c].setEnabled(true);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Battleship());
    }
}