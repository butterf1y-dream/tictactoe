import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class tictac extends JFrame {
    private static final int SIZE = 3;
    private static final char EMPTY = ' ';
    private static final char PLAYER = 'X';
    private static final char AI     = 'O';

    private final JButton[][] buttons = new JButton[SIZE][SIZE];
    private final char[][] board = new char[SIZE][SIZE];
    private boolean playerTurn = true;   // true → 플레이어(X) 차례
    private boolean gameActive = true;

    public tictac() {
        // 창 기본 설정
        setTitle("틱택토 - 당신(X) vs AI(O)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        initializeBoard();           // 보드 초기화
        add(createBoardPanel(), BorderLayout.CENTER);   // 3×3 버튼 격자
        add(createControlPanel(), BorderLayout.SOUTH);  // 새 게임 버튼

        setVisible(true);
    }

    // 보드 배열 초기화
    private void initializeBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
        gameActive = true;
        playerTurn = true;
    }

    // 3×3 버튼 격자 패널 생성
    private JPanel createBoardPanel() {
        JPanel panel = new JPanel(new GridLayout(SIZE, SIZE, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                final int row = i;
                final int col = j;

                buttons[i][j] = new JButton(" ");
                buttons[i][j].setFont(new Font("맑은 고딕", Font.BOLD, 70));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setOpaque(true);

                buttons[i][j].addActionListener(e -> onCellClicked(row, col));

                panel.add(buttons[i][j]);
            }
        }
        return panel;
    }

    // 하단 컨트롤 패널 (새 게임 버튼)
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton resetButton = new JButton("새 게임 시작");
        resetButton.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        resetButton.addActionListener(e -> resetGame());

        panel.add(resetButton);
        return panel;
    }

    // 셀 클릭 시 동작
    private void onCellClicked(int row, int col) {
        if (!gameActive || !playerTurn || board[row][col] != EMPTY) {
            return;
        }

        makeMove(row, col, PLAYER);
        updateButton(row, col);

        if (checkGameOver()) {
            return;
        }

        // AI 차례
        playerTurn = false;
        int[] best = findBestMove();
        makeMove(best[0], best[1], AI);
        updateButton(best[0], best[1]);

        checkGameOver();
        playerTurn = true;
    }

    // 실제 이동 처리
    private void makeMove(int row, int col, char mark) {
        board[row][col] = mark;
    }

    // 버튼 UI 갱신
    private void updateButton(int row, int col) {
        buttons[row][col].setText(String.valueOf(board[row][col]));
        buttons[row][col].setEnabled(false);

        // 색상으로 구분 (선택사항)
        if (board[row][col] == PLAYER) {
            buttons[row][col].setForeground(new Color(0, 120, 215));
        } else {
            buttons[row][col].setForeground(new Color(220, 53, 69));
        }
    }

    // 게임 종료 여부 확인 & 결과 표시
    private boolean checkGameOver() {
        if (checkWin(PLAYER)) {
            showMessage("플레이어(X)가 이겼습니다!");
            gameActive = false;
            return true;
        }
        if (checkWin(AI)) {
            showMessage("AI(O)가 이겼습니다!");
            gameActive = false;
            return true;
        }
        if (isBoardFull()) {
            showMessage("무승부입니다.");
            gameActive = false;
            return true;
        }
        return false;
    }

    // 결과 메시지창
    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "게임 결과", JOptionPane.INFORMATION_MESSAGE);
        disableAllButtons();
    }

    // 모든 버튼 비활성화
    private void disableAllButtons() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setEnabled(false);
            }
        }
    }

    // 새 게임으로 초기화
    private void resetGame() {
        initializeBoard();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setText(" ");
                buttons[i][j].setEnabled(true);
                buttons[i][j].setForeground(Color.BLACK);
            }
        }
    }

    // ─────────────────────────────────────
    //          승리 / 무승부 체크
    // ─────────────────────────────────────

    private boolean checkWin(char p) {
        // 행 체크
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == p && board[i][1] == p && board[i][2] == p) return true;
        }
        // 열 체크
        for (int i = 0; i < SIZE; i++) {
            if (board[0][i] == p && board[1][i] == p && board[2][i] == p) return true;
        }
        // 대각선
        if (board[0][0] == p && board[1][1] == p && board[2][2] == p) return true;
        if (board[0][2] == p && board[1][1] == p && board[2][0] == p) return true;
        return false;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == EMPTY) return false;
            }
        }
        return true;
    }

    // ─────────────────────────────────────
    //             Minimax (AI)
    // ─────────────────────────────────────

    private int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    board[i][j] = AI;
                    int score = minimax(0, false);
                    board[i][j] = EMPTY;

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMaximizing) {
        if (checkWin(AI))     return 10 - depth;
        if (checkWin(PLAYER)) return depth - 10;
        if (isBoardFull())    return 0;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = AI;
                        int eval = minimax(depth + 1, false);
                        board[i][j] = EMPTY;
                        maxEval = Math.max(maxEval, eval);
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = PLAYER;
                        int eval = minimax(depth + 1, true);
                        board[i][j] = EMPTY;
                        minEval = Math.min(minEval, eval);
                    }
                }
            }
            return minEval;
        }
    }

    // 프로그램 시작
    public static void main(String[] args) {
        SwingUtilities.invokeLater(tictac::new);
    }
}