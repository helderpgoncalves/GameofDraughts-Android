package com.example.gameofdraughts

import java.io.Serializable

/**
 * A class representing a Board in Checker.
 */
class Board : Serializable {
    private val board: Array<Array<Cell?>>
    private val lightPieces: ArrayList<Piece>
    private val darkPieces: ArrayList<Piece>

    /**
     * Sets up the board in initial configuration i.e puts the pieces in places where they should be when starting the game
     */
    // The configuration is as follows:
    //L -> Light Colored Piece  	D-> Dark Colored Piece  	 _-> a blank cell
    //	    0  1  2  3  4  5  6  7
    //	 0  L  _  L  _  L  _  L  _
    //	 1  _  L  _  L  _  L  _  L
    //	 2  L  _  L  _  L  _  L  _
    //	 3  _  _  _  _  _  _  _  _
    //	 4  _  _  _  _  _  _  _  _
    //	 5  _  D  _  D  _  D  _  D
    //	 6  D  _  D  _  D  _  D  _
    //	 7  _  D  _  D  _  D  _  D
    fun initialBoardSetup() {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                board[i][j] = Cell(i, j)
            }
        }
        run {
            var column = 0
            while (column < BOARD_SIZE) {
                this.board[0][column]!!.placePiece(Piece(Piece.LIGHT))
                this.board[2][column]!!.placePiece(Piece(Piece.LIGHT))
                this.board[6][column]!!.placePiece(Piece(Piece.DARK))
                this.board[0][column]!!.piece?.let { lightPieces.add(it) }
                this.board[2][column]!!.piece?.let { lightPieces.add(it) }
                this.board[6][column]!!.piece?.let { darkPieces.add(it) }
                column += 2
            }
        }
        var column = 1
        while (column < BOARD_SIZE) {
            board[1][column]!!.placePiece(Piece(Piece.LIGHT))
            board[5][column]!!.placePiece(Piece(Piece.DARK))
            board[7][column]!!.placePiece(Piece(Piece.DARK))
            board[1][column]!!.piece?.let { lightPieces.add(it) }
            board[5][column]!!.piece?.let { darkPieces.add(it) }
            board[7][column]!!.piece?.let { darkPieces.add(it) }
            column += 2
        }
    } // end of initialBoardSetup

    /**
     * Gets the Cell in the specified position
     * @param x x-coordinate of the cell
     * @param y y-coordinate of the cell
     * @return the instance of Cell in the position specified by given x-coordinate and y-coordinate
     * @throws IllegalArgumentException if the given x-coordinate and y-coordinate is out of bound i.e not in range 0 <= x, y <= 7
     */
    @Throws(IllegalArgumentException::class)
    fun getCell(x: Int, y: Int): Cell? {
        require(!(x < 0 || x > 7 || y < 0 || y > 7)) { "The coordinates provided are outside of the board" }
        return board[x][y]
    }

    /**
     * Returns an ArrayList of pieces of the specified color
     * @return ArrayList of the pieces of the specified color that are in the board currently
     * @param givenColor The color of the pieces that is to be retrieved.
     * @throws IllegalArgumentException if the specified color is not a valid color
     * i.e if the specified color is not one of Piece.LIGHT or Piece.DARK
     */
    @Throws(IllegalArgumentException::class)
    fun getPieces(givenColor: String): ArrayList<Piece> {
        if (givenColor == Piece.LIGHT) {
            return lightPieces
        } else if (givenColor == Piece.DARK) {
            return darkPieces
        }
        throw IllegalArgumentException("Given color is not the color of the pieces in board. Given color: $givenColor")
    }

    /**
     * Moves the piece from one cell to another cell in the board. This method does not checks if the given move is valid or not.
     *
     * @param fromX The x-coordinate of the source cell i.e the cell from where the piece is to be moved.
     * @param fromY The y-coordinate of the source cell.
     * @param toX The x-coordinate of the destination cell i.e. the cell where the piece is to be placed.
     * @param toY The y-coordinate of the destination cell.
     * @return Returns an ArrayList of Cell that were changed by the move. If the move did not involve any capture then the
     * returned ArrayList will contain the source and destination Cells only. However, if the move comprised of
     * a capture, then the returned ArrayList will contain the source Cell, destination Cell and the Cell in which
     * the captured Piece was located.
     * @throws NullPointerException if the source Cell does not contains any Piece.
     * @throws IllegalArgumentException if the given x and y coordinates are out of bound (i.e 0 <= x, y <= 7) or if the destination cell contains a Piece
     */
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun movePiece(fromX: Int, fromY: Int, toX: Int, toY: Int): ArrayList<Cell?> {
        val srcCell = getCell(fromX, fromY)
        val dstCell = getCell(toX, toY)
        val changedCells = java.util.ArrayList<Cell?>()
        if (srcCell!!.piece == null) {
            throw NullPointerException("The source cell does not contains piece to move.")
        }
        require(dstCell!!.piece == null) { "The destination cell already contains a piece. Cannot move to occupied cell." }
        if (isCaptureMove(srcCell, dstCell)) {
            val capturedCellX = (fromX + toX) / 2
            val capturedCellY = (fromY + toY) / 2
            val capturedPiece = board[capturedCellX][capturedCellY]!!.piece
            if (capturedPiece != null) {
                removePiece(capturedPiece)
            }
            if (capturedPiece != null) {
                changedCells.add(capturedPiece.cell)
            } // here capturedPiece might cause null pointer exception. Not sure yet.
        }
        srcCell.movePiece(dstCell)
        changedCells.add(srcCell)
        changedCells.add(dstCell)
        return changedCells
    } // End of move

    /**
     * Moves the piece from one cell to another cell in the board. This method does not checks if the given move is valid or not.
     * This method calls movePiece(int fromX, int fromY, int toX, int toY) with arguments src[0], src[1], dst[0] and dst[1] respectively.
     * @param src An integer array of length two representing the coordinate of source Cell i.e. src[0] = x-coordinate of source Cell and src[1] = y-coordiante of source Cell
     * @param dst An integer array of length two representing the coordinate of destination Cell i.e. dst[0] = x-coordinate of destination Cell and dst[1] = y-coordiante of destination Cell
     * @return Returns an ArrayList of Cell that were changed by the move. If the move did not involve any capture then the
     * returned ArrayList will contain the source and destination Cells only. However, if the move comprised of
     * a capture, then the returned ArrayList will contain the source Cell, destination Cell and the Cell in which
     * the captured Piece was located.
     * @throws IllegalArgumentException if the length of the parameters is not equal to two i.e. if src.length != 2 || dst.length != 2;
     */
    @Throws(IllegalArgumentException::class)
    fun movePiece(src: IntArray, dst: IntArray): ArrayList<Cell?> {
        require(!(src.size != 2 || dst.size != 2)) { "The given dimension of the points does not match." }
        return movePiece(src[0], src[1], dst[0], dst[1])
    }

    /**
     * Moves the piece from one cell to another cell in the board. This method does not checks if the given move is valid or not.
     * This method calls movePiece(int fromX, int fromY, int toX, int toY) with arguments move[0], move[1], move[2] and move[3] respectively.
     * @param move An integer array of length four representing the move i.e.
     * <br></br>move[0] = x-coordinate of the source Cell,
     * <br></br>move[1] = y-coordiante of the source Cell,
     * <br></br>move[2] = x-coordinate of the destination Cell,
     * <br></br>move[3] = y-coordinate of the destination Cell
     * @return Returns an ArrayList of Cell that were changed by the move. If the move did not involve any capture then the
     * returned ArrayList will contain the source and destination Cells only. However, if the move comprised of
     * a capture, then the returned ArrayList will contain the source Cell, destination Cell and the Cell in which
     * the captured Piece was located.
     * @throws IllegalArgumentException if the length of the parameters is not equal to two i.e. if src.length != 2 || dst.length != 2;
     */
    @Throws(IllegalArgumentException::class)
    fun movePiece(move: IntArray): ArrayList<Cell?> {
        require(move.size == 4) { "The given dimension of the points does not match." }
        return movePiece(move[0], move[1], move[2], move[3])
    }

    /**
     * Removes the given Piece from the board.
     * @param capturedPiece The Piece to be removed
     * @throws IllegalStateException if the piece was not successfully removed
     */
    @Throws(IllegalStateException::class, IllegalArgumentException::class)
    fun removePiece(capturedPiece: Piece) {
        if (capturedPiece.color == Piece.LIGHT) {
            check(lightPieces.remove(capturedPiece)) { "Error removing the piece" }
            capturedPiece.cell?.placePiece(null)
        } else if (capturedPiece.color == Piece.DARK) {
            check(darkPieces.remove(capturedPiece)) { "Error removing the piece" }
            capturedPiece.cell?.placePiece(null)
        }
    }

    /**
     * Returns the possible moves that the Piece located in the given coordinate can have.
     * This method calls possibleMoves(Cell givenCell) with the Cell returned by the method getCell(x, y) of the Board class as the parameter.
     * @param x The x-coordinate of the Cell in which the required Piece is located.
     * @param y The x-coordinate of the Cell in which the required Piece is located.
     * @return An ArrayList of Cell where the Piece in the given location can move. If there is no Piece in the given location then the returned ArrayList is empty.
     * @throws IllegalArgumentException if the coordinates are out of bound i.e. not in range 0<= x, y <=7
     */
    @Throws(IllegalArgumentException::class)
    fun possibleMoves(x: Int, y: Int): ArrayList<Cell?> {
        require(!(x < 0 || x > 7 || y < 0 || y > 7)) { "Invalid value of x or y provided. (x, y) = ($x, )" }
        return possibleMoves(board[x][y])
    }

    /**
     * Returns the possible moves that the Piece in given Cell can have.
     * @param givenCell The Cell associated with the Piece whose possible moves is to be determined
     * @return An ArrayList of Cell where the Piece in the given Cell can move. If there is no Piece in the given Cell then the returned ArrayList is empty.
     * @throws NullPointerException if the given Cell is null i.e. givenCell == null.
     */
    @Throws(NullPointerException::class)
    fun possibleMoves(givenCell: Cell?): ArrayList<Cell?> {
        if (givenCell == null) {
            throw NullPointerException("Given Cell is null. Cannot find the possible moves of null Cell")
        }
        val nextMoves = java.util.ArrayList<Cell?>()
        val givenPiece = givenCell.piece ?: return nextMoves
        val playerColor = givenPiece.color
        val opponentColor = Piece.getOpponentColor(playerColor)


        // if the piece is light-colored
        if (playerColor == Piece.LIGHT) {
            //the next move will be one row ahead i.e in row number X+1
            var nextX = givenCell.x + 1
            if (nextX < 8) {
                //next move = (currentRow +1, currentColumn +1)
                var nextY = givenCell.y + 1
                //if the cell is not out of bound further checking is required
                if (nextY < 8) {
                    //if the cell is empty then add the cell to next move
                    if (!board[nextX][nextY]!!.containsPiece()) {
                        nextMoves.add(board[nextX][nextY])
                    } else if (board[nextX][nextY]!!.piece?.color == opponentColor) {
                        val xCoordAfterHoping = nextX + 1
                        val yCoordAfterHoping = nextY + 1
                        if (xCoordAfterHoping < 8 && yCoordAfterHoping < 8 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                                .containsPiece()
                        ) {
                            nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                        }
                    }
                }


                //next move = (currentRow+1, currentColumn -1)
                nextY = givenCell.y - 1
                // if the cell is within bound and does not contains a piece then add it to nextMoves
                if (nextY >= 0) {
                    if (!board[nextX][nextY]!!.containsPiece()) {
                        nextMoves.add(board[nextX][nextY])
                    } else if (board[nextX][nextY]!!.piece?.color == opponentColor) {
                        val xCoordAfterHoping = nextX + 1
                        val yCoordAfterHoping = nextY - 1
                        if (xCoordAfterHoping < 8 && yCoordAfterHoping >= 0 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                                .containsPiece()
                        ) {
                            nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                        }
                    }
                }
            }

            //if the given piece is king then have to look to the row behind
            if (givenPiece.isKing) {
                nextX = givenCell.x - 1
                if (nextX >= 0) {
                    //nextMove = (currentRow -1, currentColumn+1)
                    //add this cell if it is within bound and doesnot contain piece
                    var nextY = givenCell.y + 1
                    if (nextY < 8 && !board[nextX][nextY]!!.containsPiece()) {
                        nextMoves.add(board[nextX][nextY])
                    } else if (nextY < 8 && board[nextX][nextY]!!.piece?.color == opponentColor) {
                        val xCoordAfterHoping = nextX - 1
                        val yCoordAfterHoping = nextY + 1
                        if (xCoordAfterHoping >= 0 && yCoordAfterHoping < 8 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                                .containsPiece()
                        ) {
                            nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                        }
                    }
                    //nextMove = (currentRow-1, currentColumn-1)
                    //add this cell if it is within bound and does not contains piece
                    nextY = givenCell.y - 1
                    if (nextY >= 0 && !board[nextX][nextY]!!.containsPiece()) {
                        nextMoves.add(board[nextX][nextY])
                    } else if (nextY >= 0 && board[nextX][nextY]!!.piece?.color == opponentColor) {
                        val xCoordAfterHoping = nextX - 1
                        val yCoordAfterHoping = nextY - 1
                        if (xCoordAfterHoping >= 0 && yCoordAfterHoping >= 0 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                                .containsPiece()
                        ) {
                            nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                        }
                    }
                }
            }
        } else if (givenPiece.color == Piece.DARK) {
            //dark pieces are on the higher rows and to move it forward we have to move them to rows with lower row number.
            //So by assigning currentRow = currentRow -1, we are actually advancing the pieces

            //next move will be on the next row of current row. Rember that currentRow -= 1 will advance the row for darker pieces
            var nextX = givenCell.x - 1
            if (nextX >= 0) {
                //next move = (currentRow -1, currentColumn +1) which is a row ahead and a column to right
                var nextY = givenCell.y + 1
                if (nextY < 8 && !board[nextX][nextY]!!.containsPiece()) {
                    nextMoves.add(board[nextX][nextY])
                } else if (nextY < 8 && board[nextX][nextY]!!.piece?.color == opponentColor) {
                    val xCoordAfterHoping = nextX - 1
                    val yCoordAfterHoping = nextY + 1
                    if (xCoordAfterHoping >= 0 && yCoordAfterHoping < 8 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                            .containsPiece()
                    ) {
                        nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                    }
                }
                //next move = (currentRow -1, currentColumn+1) which is a row ahead and a column to left
                nextY = givenCell.y - 1
                if (nextY >= 0 && !board[nextX][nextY]!!.containsPiece()) {
                    nextMoves.add(board[nextX][nextY])
                } else if (nextY >= 0 && board[nextX][nextY]!!.piece?.color == opponentColor) {
                    val xCoordAfterHoping = nextX - 1
                    val yCoordAfterHoping = nextY - 1
                    if (xCoordAfterHoping >= 0 && yCoordAfterHoping >= 0 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                            .containsPiece()
                    ) {
                        nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                    }
                }
            }

            //if the piece is king we have to look back; Remember in Dark pieces back row = currentRow +1
            if (givenPiece.isKing) {
                //getting to row behind current row
                nextX = givenCell.x + 1
                if (nextX < 8) {
                    //next move = (currentRow +1, currentColumn+1) which is a row behind and a column right
                    var nextY = givenCell.y + 1
                    if (nextY < 8 && !board[nextX][nextY]!!.containsPiece()) {
                        nextMoves.add(board[nextX][nextY])
                    } else if (nextY < 8 && board[nextX][nextY]!!.piece?.color == opponentColor) {
                        val xCoordAfterHoping = nextX + 1
                        val yCoordAfterHoping = nextY + 1
                        if (xCoordAfterHoping < 8 && yCoordAfterHoping < 8 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                                .containsPiece()
                        ) {
                            nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                        }
                    }

                    //next move = (currentRow +1, currentColumn-1) which is a row behind and a column left
                    nextY = givenCell.y - 1
                    if (nextY >= 0 && !board[nextX][nextY]!!.containsPiece()) {
                        nextMoves.add(board[nextX][nextY])
                    } else if (nextY >= 0 && board[nextX][nextY]!!.piece?.color == opponentColor) {
                        val xCoordAfterHoping = nextX + 1
                        val yCoordAfterHoping = nextY - 1
                        if (xCoordAfterHoping < 8 && yCoordAfterHoping >= 0 && !board[xCoordAfterHoping][yCoordAfterHoping]!!
                                .containsPiece()
                        ) {
                            nextMoves.add(board[xCoordAfterHoping][yCoordAfterHoping])
                        }
                    }
                }
            }
        } // end of else if dark piece
        return nextMoves
    } // end of possibleMoves method

    /**
     * Returns the possible moves that the given Piece can have. This method calls possibleMoves(Cell givenCell) with the Cell object returned by givenPiece.getCell() as a parameter.
     * @param givenPiece The Piece whose possible move is to be determined
     * @return An ArrayList of Cell where the given Piece can move.
     * @throws NullPointerException if the given Piece is null i.e. givenPiece == null.
     */
    @Throws(NullPointerException::class)
    fun possibleMoves(givenPiece: Piece?): ArrayList<Cell?> {
        if (givenPiece == null) {
            throw NullPointerException("The Piece provided is null. Cannot find possible moves of a null Piece")
        }
        return possibleMoves(givenPiece.cell)
    }

    /**
     * Returns the capturing moves of the Piece located in the provided Cell.
     * @param givenCell The Cell where the Piece, whose capturing moves is to be determined, is located.
     * @return An ArrayList of Cell where the Piece in given Cell can move and also captures the opponent's piece when performing those moves.
     * If there is no Piece in the given Cell or if the Piece in given Cell does not have any capturing moves then the returned ArrayList is empty.
     * @throws NullPointerException if the given Cell is null.
     */
    @Throws(NullPointerException::class)
    fun getCaptureMoves(givenCell: Cell?): ArrayList<Cell?> {
        if (givenCell == null) {
            throw NullPointerException("The Cell provided is null.")
        }
        val possibleMovesOfCell = possibleMoves(givenCell)
        val capturingMoves = ArrayList<Cell?>()
        for (dstCell in possibleMovesOfCell) {
            if (isCaptureMove(givenCell, dstCell)) {
                capturingMoves.add(dstCell)
            }
        }
        return capturingMoves
    }

    /**
     * Returns the capture moves of the Piece located in the Cell with given x and y coordinates.
     * This method calls the method getCaptureMoves(Cell givenCell) with the Cell object returned by getCell(x, y) (method of the Board class) as argument.
     * @param x The x-coordinate of the Cell where the Piece, whose capture moves is to be determined, is located.
     * @param y The y-coordinate of the Cell where the Piece, whose capture moves is to be determined, is located.
     * @return An ArrayList of Cell where the Piece located in given coordinates can move and also captures the opponent's piece when performing those moves.
     * If there is no Piece in the Cell with given coordinates or if the Piece in the Cell with given coordinates does not have any capturing moves, then the returned ArrayList is empty.
     * @throws IllegalArgumentException if the coordinates are out of bound i.e. not in range 0<= x, y <=7
     */
    @Throws(IllegalArgumentException::class)
    fun getCaptureMoves(x: Int, y: Int): ArrayList<Cell?> {
        require(!(x < 0 || x > 7 || y < 0 || y > 7)) { "Invalid value of x or y provided. (x, y) = ($x, )" }
        return getCaptureMoves(board[x][y])
    }

    /**
     * Returns whether the given pair of Cell can form a capture move. This method does not check if the destination Cell is a valid move for Piece in source Cell.
     * Therefore, make sure that the destination Cell is from the list given by possibleMoves(...) method applied to source Cell.
     * @param srcCell: The source Cell of the move.
     * @param dstCell: The destination Cell of the move.
     * @retun Returns true if the given pair of Cell can form the capture moves. Returns false otherwise.
     * @throws NullPointerException if source Cell and/or destination Cell is null.
     * @throws IllegalArgumentException if source Cell does not contain any piece.
     */
    @Throws(NullPointerException::class, IllegalArgumentException::class)
    fun isCaptureMove(srcCell: Cell?, dstCell: Cell?): Boolean {
        if (srcCell == null) {
            throw NullPointerException("The source cell is null. Cannot tell if the move is capture move or not if source cell is null.")
        }
        if (dstCell == null) {
            throw NullPointerException("The destination cell is null. Cannot tell if the move is capture move or not if destination cell is null.")
        }
        requireNotNull(srcCell.piece) { "The source cell does not contain a piece. Cannot be capture move if source cell does not have a piece. SrcCell: (" + srcCell.x + ", " + srcCell.y + ")" }
        return if (Math.abs(srcCell.x - dstCell.x) == 2 && Math.abs(srcCell.y - dstCell.y) == 2) {
            true
        } else false
    }

    /**
     * Returns if the given coordinates form a capture move.
     * This method does not check if the given move is a valid move or not.
     * This method calls isCaptureMove(Cell srcCell, Cell dstCell) as isCaptureMove(getCell(givenMove[0], givenMove[1]) , getCell(givenMove[2], givenMove[3]))
     * @param givenMove An integer array of size 4 such that:
     * <br></br> givenMove[0] = x-coordinate of the source Cell
     * <br></br> givenMove[1] = y-coordinate of the source Cell
     * <br></br> givenMove[2] = x-coordinate of the destination Cell
     * <br></br> givenMove[3] = y-coordinate of the destination Cell
     * @return Returns if the given Cell is the capturing move of the given Piece.
     * @throws IllegalArgumentException if the coordinates are out of bound i.e. not in range [0, 7] inclusive
     */
    @Throws(IllegalArgumentException::class)
    fun isCaptureMove(givenMove: IntArray): Boolean {
        require(givenMove.size == 4) { "The dimension of the array that represents the move does not matches" }
        return isCaptureMove(
            board[givenMove[0]][givenMove[1]],
            board[givenMove[2]][givenMove[3]]
        )
    }

    companion object {
        private const val BOARD_SIZE = 8
    }

    /**
     * Creates an instance of Board. Sets up the pieces as in freshly started game.
     */
    init {
        lightPieces = java.util.ArrayList()
        darkPieces = java.util.ArrayList()
        board = Array(BOARD_SIZE) {
            arrayOfNulls(
                BOARD_SIZE
            )
        }
    }
} // End of class
