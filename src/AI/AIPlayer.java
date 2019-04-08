package AI;

import java.util.ArrayList;

public class AIPlayer {
    private int size;
    private String[][] board;
    private ArrayList<int[]> emptyCells;

    private String mySymbol;
    private String opponentSymbol;

    private int requiredSymbols;
    private int[][] heuristicArray;

    private int lastRowIndex;
    private int lastColIndex;
    private String lastSymbol;

    public AIPlayer(int size, int requiredSymbols, String mySymbol, String opponentSymbol) {
        this.size = size;
        board = new String[size][size];
        emptyCells = new ArrayList<>();

        this.requiredSymbols = requiredSymbols;
        heuristicArray = new int[requiredSymbols + 1][requiredSymbols + 1];

        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                board[i][j] = "";
                emptyCells.add(new int[]{i, j});
            }
        }

        for (int i = 1; i < requiredSymbols + 1; ++i) {
            int aux = (int) Math.pow(10d, i - 1);
            heuristicArray[i][0] = aux;
            heuristicArray[0][i] = -aux;
        }

        this.mySymbol = mySymbol;
        this.opponentSymbol = opponentSymbol;

        lastRowIndex = -1;
        lastColIndex = -1;
        lastSymbol = "";
    }

    public void updateBoard(int row, int col, String symbol) {
        board[row][col] = symbol;

        for (int[] cell : emptyCells) {
            if (cell[0] == row && cell[1] == col) {
                emptyCells.remove(cell);
                break;
            }
        }

        lastRowIndex = row;
        lastColIndex = col;
        lastSymbol = symbol;
    }

    public int[] next(int depth) {
        int[] result;

        if (depth == -1) {
            result = minimax(mySymbol, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            result = minimax(depth, mySymbol, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        return new int[]{result[1], result[2]};
    }

    // full depth minimax WITH alpha–beta pruning
    private int[] minimax(String symbol, int alpha, int beta) {
        int score;
        int bestRow = -1;
        int bestCol = -1;

        if (hasWon(lastRowIndex, lastColIndex, lastSymbol)) {
            if (lastSymbol.equals(mySymbol)) {
                return new int[]{1, bestRow, bestCol};
            } else {
                return new int[]{-1, bestRow, bestCol};
            }
        } else if (emptyCells.isEmpty()) {
            return new int[]{0, bestRow, bestCol};
        } else {
            for (int i = 0; i < emptyCells.size(); ++i) {
                int currRow = emptyCells.get(i)[0];
                int currCol = emptyCells.get(i)[1];

                board[currRow][currCol] = symbol;
                emptyCells.remove(i);

                lastRowIndex = currRow;
                lastColIndex = currCol;
                lastSymbol = symbol;

                if (symbol.equals(mySymbol)) {
                    score = minimax(opponentSymbol, alpha, beta)[0];

                    if (score > alpha) {
                        alpha = score;
                        bestRow = currRow;
                        bestCol = currCol;
                    }
                } else {
                    score = minimax(mySymbol, alpha, beta)[0];

                    if (score < beta) {
                        beta = score;
                        bestRow = currRow;
                        bestCol = currCol;
                    }
                }

                board[currRow][currCol] = "";
                emptyCells.add(i, new int[]{currRow, currCol});

                if (alpha >= beta) {
                    break;
                }
            }

            if (symbol.equals(mySymbol)) {
                return new int[]{alpha, bestRow, bestCol};
            } else {
                return new int[]{beta, bestRow, bestCol};
            }
        }
    }

    // depth limited minimax WITH alpha–beta pruning
    private int[] minimax(int depth, String symbol, int alpha, int beta) {
        int score;
        int bestRow = -1;
        int bestCol = -1;

        if (hasWon(lastRowIndex, lastColIndex, lastSymbol)) {
            if (lastSymbol.equals(mySymbol)) {
                return new int[]{heuristicArray[requiredSymbols][0], bestRow, bestCol};
            } else {
                return new int[]{heuristicArray[0][requiredSymbols], bestRow, bestCol};
            }
        } else if (emptyCells.isEmpty()) {
            return new int[]{0, bestRow, bestCol};
        } else if (depth == 0) {
            return new int[]{evaluate(), bestRow, bestCol};
        } else {
            for (int i = 0; i < emptyCells.size(); ++i) {
                int currRow = emptyCells.get(i)[0];
                int currCol = emptyCells.get(i)[1];

                board[currRow][currCol] = symbol;
                emptyCells.remove(i);

                lastRowIndex = currRow;
                lastColIndex = currCol;
                lastSymbol = symbol;

                if (symbol.equals(mySymbol)) {
                    score = minimax(depth - 1, opponentSymbol, alpha, beta)[0];

                    if (score > alpha) {
                        alpha = score;
                        bestRow = currRow;
                        bestCol = currCol;
                    }
                } else {
                    score = minimax(depth - 1, mySymbol, alpha, beta)[0];

                    if (score < beta) {
                        beta = score;
                        bestRow = currRow;
                        bestCol = currCol;
                    }
                }

                board[currRow][currCol] = "";
                emptyCells.add(i, new int[]{currRow, currCol});

                if (alpha >= beta) {
                    break;
                }
            }

            if (symbol.equals(mySymbol)) {
                return new int[]{alpha, bestRow, bestCol};
            } else {
                return new int[]{beta, bestRow, bestCol};
            }
        }
    }

    private boolean hasWon(int row, int col, String symbol) {
        // if computer starts and the board is empty
        if (row == -1) {
            return false;
        }

        // check row and column for the current position
        if (checkRow(row, symbol) || checkColumn(col, symbol)) {
            return true;
        }

        // check the two diagonals for the current position
        int counter = 1;

        for (int i = 1; (row - i) >= 0 && (col - i) >= 0; ++i) {
            if (board[row - i][col - i].equals(symbol)) {
                ++counter;
            } else {
                break;
            }
        }

        for (int i = 1; (row + i) < size && (col + i) < size; ++i) {
            if (board[row + i][col + i].equals(symbol)) {
                ++counter;
            } else {
                break;
            }
        }

        if (counter >= requiredSymbols) {
            return true;
        }

        counter = 1;

        for (int i = 1; (row - i) >= 0 && (col + i) < size; ++i) {
            if (board[row - i][col + i].equals(symbol)) {
                ++counter;
            } else {
                break;
            }
        }

        for (int i = 1; (row + i) < size && (col - i) >= 0; ++i) {
            if (board[row + i][col - i].equals(symbol)) {
                ++counter;
            } else {
                break;
            }
        }

        return counter >= requiredSymbols;
    }

    private int evaluate() {
        int score = 0;

        for (int i = 0; i < size; ++i) {
            score += evaluateRow(i) + evaluateColumn(i);
        }

        score += evaluateDiagonal(1) + evaluateDiagonal(-1);

        return score;
    }

    private int evaluateRow(int row) {
        int score = 0;

        for (int i = 0; i < size - requiredSymbols + 1; ++i) {
            int myCounter = 0;
            int opponentCounter = 0;

            for (int j = 0; j < requiredSymbols; ++j) {
                if (board[row][i + j].equals(mySymbol)) {
                    ++myCounter;
                } else if (board[row][i + j].equals(opponentSymbol)) {
                    ++opponentCounter;
                }
            }

            score += heuristicArray[myCounter][opponentCounter];
        }

        return score;
    }

    private int evaluateColumn(int col) {
        int score = 0;

        for (int i = 0; i < size - requiredSymbols + 1; ++i) {
            int myCounter = 0;
            int opponentCounter = 0;

            for (int j = 0; j < requiredSymbols; ++j) {
                if (board[i + j][col].equals(mySymbol)) {
                    ++myCounter;
                } else if (board[i + j][col].equals(opponentSymbol)) {
                    ++opponentCounter;
                }
            }

            score += heuristicArray[myCounter][opponentCounter];
        }

        return score;
    }

    private int evaluateDiagonal(int direction) {
        int score = 0;

        if (direction == 1) {
            for (int diagonal = 0; diagonal < size - requiredSymbols + 1; ++diagonal) {
                for (int row = diagonal; row < size - requiredSymbols + 1; ++row) {
                    int myBelowCounter = 0;
                    int opponentBelowCounter = 0;

                    int myAboveCounter = 0;
                    int opponentAboveCounter = 0;

                    for (int col = 0; col < requiredSymbols; ++col) {
                        String belowSymbol = board[row + col][row + col - diagonal];
                        String aboveSymbol = board[row + col - diagonal][row + col];

                        if (belowSymbol.equals(mySymbol)) {
                            ++myBelowCounter;
                        } else if (belowSymbol.equals(opponentSymbol)) {
                            ++opponentBelowCounter;
                        }

                        if (aboveSymbol.equals(mySymbol)) {
                            ++myAboveCounter;
                        } else if (aboveSymbol.equals(opponentSymbol)) {
                            ++opponentAboveCounter;
                        }
                    }

                    score += heuristicArray[myBelowCounter][opponentBelowCounter] +
                            heuristicArray[myAboveCounter][opponentAboveCounter];
                }
            }
        } else {
            for (int diagonal = 0; diagonal < size - requiredSymbols + 1; ++diagonal) {
                for (int row = diagonal; row < size - requiredSymbols + 1; ++row) {
                    int myBelowCounter = 0;
                    int opponentBelowCounter = 0;

                    int myAboveCounter = 0;
                    int opponentAboveCounter = 0;

                    for (int col = 0; col < requiredSymbols; ++col) {
                        String belowSymbol = board[row + col][size - (row + 1) - col + diagonal];
                        String aboveSymbol = board[row + col - diagonal][size - (row + 1) - col];

                        if (belowSymbol.equals(mySymbol)) {
                            ++myBelowCounter;
                        } else if (belowSymbol.equals(opponentSymbol)) {
                            ++opponentBelowCounter;
                        }

                        if (aboveSymbol.equals(mySymbol)) {
                            ++myAboveCounter;
                        } else if (aboveSymbol.equals(opponentSymbol)) {
                            ++opponentAboveCounter;
                        }
                    }

                    score += heuristicArray[myBelowCounter][opponentBelowCounter] +
                            heuristicArray[myAboveCounter][opponentAboveCounter];
                }
            }
        }

        return score;
    }

    private boolean checkRow(int row, String symbol) {
        int counter = 0;

        for (int j = 0; j < size; ++j) {
            if (board[row][j].equals(symbol)) {
                ++counter;

                if (counter == requiredSymbols) {
                    return true;
                }
            } else {
                counter = 0;
            }
        }

        return false;
    }

    private boolean checkColumn(int col, String symbol) {
        int counter = 0;

        for (int i = 0; i < size; ++i) {
            if (board[i][col].equals(symbol)) {
                ++counter;

                if (counter == requiredSymbols) {
                    return true;
                }
            } else {
                counter = 0;
            }
        }

        return false;
    }

    // returns 1 if X won, -1 if O won, 0 if it's a draw, 42 if it's not game over
    public int gameOver() {
        if (hasWon(lastRowIndex, lastColIndex, lastSymbol)) {
            if (lastSymbol.equals("X")) {
                return 1;
            } else {
                return -1;
            }
        }

        if (emptyCells.isEmpty()) {
            return 0;
        }

        return 42;
    }
}
