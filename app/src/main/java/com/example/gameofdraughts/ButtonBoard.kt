package com.example.gameofdraughts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.util.*


/*
* ButtonBoard.java - Handles the graphical user interface for the game cellBoard
*                  - Stores button ids for game cellBoard layout and maps them to the correct cell (x, y)
*                  - Creates array of buttons that map each square on the game cellBoard
*                  - Initializes the game piece images on the cellBoard (12 dark pieces and 12 light pieces)
*/
class ButtonBoard : AppCompatActivity() {
    private lateinit var buttons_id: IntArray
    private lateinit var buttonBoard: Array<Array<Button?>>
    private var moves: ArrayList<Cell?>? = null
    private var highlightedCells: ArrayList<Cell>? = null
    private var player1: Player? = null
    private var player2: Player? = null
    private var currentPlayer: Player? = null
    private var computerMode = false
    private var computerTurn = false
    private var srcCellFixed = false
    private var cellBoard = Board()
    private var srcCell: Cell? = null
    private var dstCell: Cell? = null
    private var delayHandler: Handler? = null

    // Game cellBoard layout of the black squares by square ID
    // 0-63  --> black button squares, used for indexing      _ -->  red button squares, are not used in indexing
    // 32-34 --> initially empty black squares with no pieces
    //
    //	      0   1  2   3  4   5  6   7
    //
    //	 0    0   _  2   _  4   _  6   _
    //	 1    _   9  _  11  _  13  _  15
    //	 2    16  _  18  _  20  _  22 _
    //	 3    _  25  _  27  _  29  _  31
    //	 4    32  _  34  _  36  _  38  _
    //	 5    _  41  _  43  _  45  _  47
    //	 6    48  _  50  _  52  _  54
    //	 7    _  57  _  59  _  61  _  63
    /*
     * Creates the activity for the game cellBoard, then sets up game piece images on game cellBoard
     * @param Bundle savedInstanceState
     */
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cellBoard.initialBoardSetup()
        setContentView(R.layout.board)

        // If device is in portrait mode
        if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_PORTRAIT) {
            resizeBoardToScreenSizePortrait()
        } else if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_LANDSCAPE) {
            resizeBoardToScreenSizeLandscape()
        }
        srcCell = null
        dstCell = null
        srcCellFixed = false
        delayHandler = Handler()
        highlightedCells = ArrayList()
        buttons_id = buttonArray
        buttonBoard = Array(8) { arrayOfNulls(8) }
        fillButtonBoard(listener)
        updateBoard(buttonBoard, cellBoard)
        moves = ArrayList() // init moves arraylist

        // If the load message was loaded, we load the game, otherwise a new game is created
        if (getIntent().getBooleanExtra("LOAD", false)) {
            loadGame()
            updateBoard(buttonBoard, cellBoard)
            updateTurnTracker()
        } else {
            chooseColorDialog()
            choosePlayerDialog()
        }
    }

    /*
     * Creates dialog menu to let a user pick which player to be
     */
    fun choosePlayerDialog() {
        val gameMode = arrayOf<CharSequence>("1 Player ", "2 Player")
        val gameModeBuilder = AlertDialog.Builder(this@ButtonBoard)
        gameModeBuilder.setCancelable(false)
        gameModeBuilder.setTitle("Select Game Mode:")
        gameModeBuilder.setItems(
            gameMode
        ) { dialog, clickValue -> // Computer mode
            if (clickValue == 0) {
                computerMode = true
            } else if (clickValue == 1) {
                computerMode = false
            }
            updateTurnTracker()
        }
        gameModeBuilder.show()
    }

    /*
     * Creates dialog menu to let player 1 pick their color
     */
    fun chooseColorDialog() {
        val choices = arrayOf<CharSequence>("Light", "Dark")
        val builder = AlertDialog.Builder(this@ButtonBoard)
        builder.setCancelable(false)
        builder.setTitle("Please select color for Player 1")
        builder.setItems(
            choices
        ) { dialog, clickValue -> // Light player starts first
            if (clickValue == 0) {
                player1 = Player(Piece.LIGHT)
                player2 = Player(Piece.DARK)
                currentPlayer = player2
                if (computerMode) {
                    computerTurn = true
                    delayHandler!!.postDelayed({
                        updateTurnTracker()
                        computersTurn()
                    }, 1000)
                }
            } else if (clickValue == 1) {
                player1 = Player(Piece.DARK)
                player2 = Player(Piece.LIGHT)
                currentPlayer = player1
            }
            updateTurnTracker()
        }
        builder.show()
    }

    /*
      * Creates listener to perform action when player clicks a game piece
      * Handles when player wants to move a piece
      */
    private val listener = View.OnClickListener { v ->
        val tag = v.tag as Int
        val xCord = tag / 10
        val yCord = tag % 10
        if (!computerTurn) {
            playerTurn(xCord, yCord)
        }
    }

    /*
     * Used for letting player click a move
     * @param int xCord - X-Coordinate of cell
     * @param int yCord - Y-Coordinate of cell
     */
    fun playerTurn(xCord: Int, yCord: Int) {

        // If both players have pieces, game IS RUNNING
        if (player1!!.hasMoves(cellBoard) && player1!!.hasMoves(cellBoard)) {

            // If piece exists AND color of piece matches players piece AND counter == 0, let the player take a turn
            if (cellBoard.getCell(xCord, yCord)!!.containsPiece() && cellBoard.getCell(xCord, yCord)
                    !!.piece!!.color == currentPlayer!!.color && srcCell == null
            ) {
                unHighlightPieces() // unhighlight other pieces if user clicks a source cell
                srcCell = cellBoard.getCell(xCord, yCord)
                moves = cellBoard.possibleMoves(srcCell)

                //If the user taps the cell with no moves then show the message stating that
                if (moves!!.isEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        "No possible moves!",
                        Toast.LENGTH_SHORT
                    ).show()
                    srcCell = null
                    updateTurnTracker()
                } else {
                    showPossibleMoves(moves!!)
                    srcCell = cellBoard.getCell(xCord, yCord)
                    updatePiecePressed(srcCell)
                }
            } else if (srcCell != null && srcCell == cellBoard.getCell(
                    xCord,
                    yCord
                ) && !srcCellFixed
            ) {
                srcCell = null
                updatePieces(xCord, yCord) // updates the graphical pieces
                updateTurnTracker()
            } else if (!cellBoard.getCell(xCord, yCord)!!.containsPiece() && moves!!.contains(
                    cellBoard.getCell(xCord, yCord)
                ) && srcCell != null
            ) {
                dstCell = cellBoard.getCell(xCord, yCord)
                onSecondClick(srcCell, dstCell)
            }
        }

        // If player who is light runs out of pieces, they lose
        if (!player1!!.hasMoves(cellBoard) && player2!!.hasMoves(cellBoard) ||
            player1!!.hasMoves(cellBoard) && !player2!!.hasMoves(cellBoard)
        ) {
            gameOverDialog()
        } else if (!player1!!.hasMoves(cellBoard) && !player2!!.hasMoves(cellBoard)) {
            Toast.makeText(applicationContext, "DRAW, NO WINNERS!", Toast.LENGTH_LONG).show()
        }
    }

    /*
    * When the player clicks an empty cell on the cellBoard to move source piece to, move the piece
    * If the players move captures a piece, we want to check if THAT piece has any more capture moves
    * Stores the new coordinates of the piece that made a capture (coordinates of the piece after capture)
    * @param int xCord - Stores x-coordinate of the destination cell the user clicks
    * @param int yCord - Stores the y-coordinate of the destination cell the user clicks
    */
    fun onSecondClick(givenSrcCell: Cell?, givenDstCell: Cell?) {
        unHighlightPieces()
        val captureMove = cellBoard.isCaptureMove(givenSrcCell, givenDstCell)
        val changedCells = cellBoard.movePiece(
            givenSrcCell!!.coords,
            givenDstCell!!.coords
        ) // moves piece, store captured piece into array list
        updatePieces(changedCells)
        if (captureMove) {
            moves =
                cellBoard.getCaptureMoves(givenDstCell) // stores the future capture moves of the cell

            // If the piece that captured opponents piece has no capture moves, end turn
            if (moves!!.isEmpty()) {
                srcCell = null
                dstCell = null
                srcCellFixed = false
                changeTurn()
            } else {
                srcCell = dstCell
                srcCellFixed = true
                updatePiecePressed(srcCell)
                showPossibleMoves(moves!!)

                //If current player is computer
                if (currentPlayer === player2 && computerMode) {
                    computerCaptureTurn(moves!!)
                }
            }
        } else {
            srcCell = null
            dstCell = null
            srcCellFixed = false
            changeTurn()
        }
    }

    /*
     * Deals with handling the computers turn
     * Simulates a real-life player making moves
     */
    fun computersTurn() {
        val cellsWithMoves = ArrayList<Cell>()
        val cellsWithCaptureMoves = ArrayList<Cell>()
        var captureMoves: ArrayList<Cell?>
        for (cell in highlightedCells!!) {
            captureMoves = cellBoard.getCaptureMoves(cell)
            if (!captureMoves.isEmpty()) {
                cellsWithCaptureMoves.add(cell)
            } else {
                cellsWithMoves.add(cell)
            }
        }
        val random = Random()
        if (cellsWithCaptureMoves.isNotEmpty()) {
            srcCell = cellsWithCaptureMoves[random.nextInt(cellsWithCaptureMoves.size)]
            val possibleMoves = cellBoard.getCaptureMoves(srcCell)
            dstCell = possibleMoves[random.nextInt(possibleMoves.size)]
        } else {
            srcCell = cellsWithMoves[random.nextInt(cellsWithMoves.size)]
            val possibleMoves = cellBoard.possibleMoves(srcCell)
            dstCell = possibleMoves[random.nextInt(possibleMoves.size)]
        }
        updatePiecePressed(srcCell)
        buttonBoard[dstCell!!.x][dstCell!!.y]!!.setBackgroundResource(R.drawable.possible_moves_image)
        delayHandler!!.postDelayed({ onSecondClick(srcCell, dstCell) }, 1000)
    }

    /*
     * When a computer has a capture turn, this method allows him to make that move
     * Uses timers to perform non-instant moves
     * @param ArrayList<Cell> captureMoves - The moves that a computer can use for a capture
     */
    fun computerCaptureTurn(captureMoves: ArrayList<Cell?>) {
        dstCell = captureMoves[Random().nextInt(captureMoves.size)]
        delayHandler!!.postDelayed({ onSecondClick(srcCell, dstCell) }, 1000)
    }

    /*
    * Method that gets the button ID's for mapping buttons to an arraylist
    * @ret int[] - Returns the array of button ID's
    */
    val buttonArray: IntArray
        get() = intArrayOf(
            R.id.button0, R.id.button2, R.id.button4, R.id.button6,
            R.id.button9, R.id.button11, R.id.button13, R.id.button15,
            R.id.button16, R.id.button18, R.id.button20, R.id.button22,
            R.id.button25, R.id.button27, R.id.button29, R.id.button31,
            R.id.button32, R.id.button34, R.id.button36, R.id.button38,
            R.id.button41, R.id.button43, R.id.button45, R.id.button47,
            R.id.button48, R.id.button50, R.id.button52, R.id.button54,
            R.id.button57, R.id.button59, R.id.button61, R.id.button63
        )

    /*
    * Fills the Button indexes array with each button object and asigns index using button tag
    * @param View.OnClickListener listener
    */
    fun fillButtonBoard(listener: View.OnClickListener?) {
        var index = 0
        for (i in 0..7) {
            for (j in 0..7) {
                if ((i + j) % 2 == 0) {
                    buttonBoard[i][j] = findViewById<Button>(buttons_id[index])
                    index++
                    buttonBoard[i][j]!!.tag = i * 10 + j
                    buttonBoard[i][j]!!.setOnClickListener(listener)
                }
            }
        }
    }

    /*
     * Updates the game pieces on the UI Board according to Game.java Cell[][] array
     * @param Button[][] buttonBoard, Board cellBoard
     */
    fun updateBoard(buttonIndexes: Array<Array<Button?>>, board: Board) {
        for (i in 0..7) {
            for (j in 0..7) {
                if ((i + j) % 2 == 0) {
                    if (!board.getCell(i, j)!!.containsPiece()) {
                        buttonIndexes[i][j]!!.setBackgroundResource(R.drawable.blank_square)
                    } else if (board.getCell(i, j)!!.piece!!.color == Piece.LIGHT) {
                        //King light piece
                        if (board.getCell(i, j)!!.piece!!.isKing) {
                            buttonIndexes[i][j]!!.setBackgroundResource(R.drawable.light_king_piece)
                        } else {
                            buttonIndexes[i][j]!!.setBackgroundResource(R.drawable.light_piece)
                        }
                    } else if (board.getCell(i, j)!!.piece!!.color == Piece.DARK) {
                        // King dark piece
                        if (board.getCell(i, j)!!.piece!!.isKing) {
                            buttonIndexes[i][j]!!.setBackgroundResource(R.drawable.dark_king_piece)
                        } else {
                            buttonIndexes[i][j]!!.setBackgroundResource(R.drawable.dark_piece)
                        }
                    }
                }
            }
        }
    }

    /*
     * When a piece moves to an empty cell, we want to update the pieces affected
     * @param int xCord - The x-coordinate of a piece after it has moved to an empty cell
     * @param int yCord - The y-coordinate of a piece after it has moved to an empty cell
     */
    fun updatePieces(xCord: Int, yCord: Int) {

        // For all of the possible moves colored in on the cellBoard, after a piece moves we want to remove them
        var possMoves: Cell?
        for (i in moves!!.indices) {
            possMoves = moves!![i]
            buttonBoard[possMoves!!.x][possMoves!!.y]!!.setBackgroundResource(R.drawable.blank_square) // color possible moves blank
        }

        // If the piece is light
        if (cellBoard.getCell(xCord, yCord)?.piece
                !!.color == Piece.LIGHT && cellBoard.getCell(xCord, yCord)!!.containsPiece()
        ) {
            // If piece is light AND is king
            if (cellBoard.getCell(xCord, yCord)?.piece?.isKing == true) {
                buttonBoard[xCord][yCord]!!.setBackgroundResource(R.drawable.light_king_piece)
            } else {
                buttonBoard[xCord][yCord]!!.setBackgroundResource(R.drawable.light_piece)
            }
        } else {
            // // If piece is dark AND is king
            if (cellBoard.getCell(xCord, yCord)!!.piece!!.isKing) {
                buttonBoard[xCord][yCord]!!.setBackgroundResource(R.drawable.dark_king_piece)
            } else {
                buttonBoard[xCord][yCord]!!.setBackgroundResource(R.drawable.dark_piece)
            }
        }
    }

    /*
     * When a piece jumps an opponent piece, we want to remove the piece jumped and update new piece graphic at its destination
     * @param int xCordSrc - The x-coordinate of a piece that will jump opponent piece
     * @param int yCordSrc - The y-coordinate of a piece that will jump opponent piece
     * @param int xCordDst - The new x-coordinate of a piece after it jumped an opponent piece
     * @param int yCordDst - The new y-coordinate of a piece after it jumped an opponent piece
     * @param Cell pieceCaptured - The piece that was captured
     */
    fun updatePieces(changedCells: ArrayList<Cell?>) {

        // For all of the possible moves colored in on the cellBoard, after a piece jumps we want to remove them
        var possMoves: Cell?
        for (i in moves!!.indices) {
            possMoves = moves!![i]
            buttonBoard[possMoves!!.x][possMoves.y]!!.setBackgroundResource(R.drawable.blank_square) // color possible moves blank
        }
        for (cell in changedCells) {
            if (!cell!!.containsPiece()) {
                buttonBoard[cell!!.x][cell.y]!!.setBackgroundResource(R.drawable.blank_square)
            } else if (cell!!.piece!!.color == Piece.LIGHT) {
                if (cell!!.piece!!.isKing) {
                    buttonBoard[cell!!.x][cell.y]!!.setBackgroundResource(R.drawable.light_king_piece)
                } else {
                    buttonBoard[cell!!.x][cell.y]!!.setBackgroundResource(R.drawable.light_piece)
                }
            } else if (cell.piece!!.color == Piece.DARK) {
                if (cell.piece!!.isKing) {
                    buttonBoard[cell.x][cell.y]!!.setBackgroundResource(R.drawable.dark_king_piece)
                } else {
                    buttonBoard[cell.x][cell.y]!!.setBackgroundResource(R.drawable.dark_piece)
                }
            }
        }
    }

    /*
     * When the player clicks a game piece on the cellBoard we want to color in that piece
     * Colors the piece/cell that the user presses
     * @param int xCord - The x-coordinate of the source cell that we want to change to pressed piece graphic
     * @param int yCord - The y-coordinate of the source cell that we want to change to pressed piece graphic
     */
    fun updatePiecePressed(givenCell: Cell?) {
        // If current player is light AND the piece selected is a light piece, player can ONLY move light pieces and can jump ONLY dark pieces
        if (currentPlayer!!.color == Piece.LIGHT && givenCell!!.piece
                !!.color == Piece.LIGHT
        ) {

            // If light AND king
            if (givenCell.piece!!.isKing) {
                buttonBoard[givenCell.x][givenCell.y]!!.setBackgroundResource(R.drawable.light_king_piece_pressed)
            } else {
                buttonBoard[givenCell.x][givenCell.y]!!.setBackgroundResource(R.drawable.light_piece_pressed) // fill selected light piece as pressed piece image
            }
        }
        // If current player is dark AND the piece selected is a dark piece, player can ONLY move dark pieces and can jump ONLY light pieces
        if (currentPlayer!!.color == Piece.DARK && givenCell!!.piece!!.color == Piece.DARK) {

            // If dark AND king
            if (cellBoard.getCell(givenCell.x, givenCell.y)!!.piece!!.isKing) {
                buttonBoard[givenCell.x][givenCell.y]!!.setBackgroundResource(R.drawable.dark_king_piece_pressed)
            } else {
                buttonBoard[givenCell.x][givenCell.y]!!.setBackgroundResource(R.drawable.dark_piece_pressed) // fill selected dark piece as pressed piece image
            }
        }
    }

    /*
    * Switches currentPlayer to the other player, updates the turn tracker
    */
    fun changeTurn() {
        // If both players have moves, we can switch turns
        if (player1!!.hasMoves(cellBoard) && player2!!.hasMoves(cellBoard)) {
            if (currentPlayer == player1) {
                currentPlayer = player2
                if (computerMode) {
                    computerTurn = true
                    delayHandler!!.postDelayed({ computersTurn() }, 1000)
                }
                updateTurnTracker()
            } else {
                currentPlayer = player1
                if (computerMode) {
                    computerTurn = false
                }
                updateTurnTracker()
            }
        } else {
            gameOverDialog()
        }
    }

    /*
     * Unhighlights the game pieces when a player performs a move
     */
    fun unHighlightPieces() {
        var highlightedCell: Cell
        while (highlightedCells!!.isNotEmpty()) {
            highlightedCell = highlightedCells!!.removeAt(0)
            if (highlightedCell.piece!!.color == Piece.LIGHT) {
                if (highlightedCell.piece!!.isKing) {
                    buttonBoard[highlightedCell.x][highlightedCell.y]!!.setBackgroundResource(
                        R.drawable.light_king_piece
                    )
                } else {
                    buttonBoard[highlightedCell.x][highlightedCell.y]!!.setBackgroundResource(
                        R.drawable.light_piece
                    )
                }
            } else {
                if (highlightedCell.piece!!.isKing) {
                    buttonBoard[highlightedCell.x][highlightedCell.y]!!.setBackgroundResource(
                        R.drawable.dark_king_piece
                    )
                } else {
                    buttonBoard[highlightedCell.x][highlightedCell.y]!!.setBackgroundResource(
                        R.drawable.dark_piece
                    )
                }
            }
        }
    }

    /*
     * Updates the player turn tracker
     */
    fun updateTurnTracker() {
        if (currentPlayer != null) {
            // Get all the pieces of the current player that can move & highlight them
            val currentPlayerPieces = currentPlayer!!.color?.let { cellBoard.getPieces(it) }
            var moves: ArrayList<Cell?>
            if (currentPlayerPieces != null) {
                for (piece in currentPlayerPieces) {
                    moves = cellBoard.possibleMoves(piece)
                    if (moves.isNotEmpty()) {
                        if (piece.color == Piece.DARK && piece.isKing) {
                            buttonBoard[piece.cell?.x!!][piece.cell!!.y]!!.setBackgroundResource(R.drawable.dark_king_highlighted)
                        } else if (piece.color == Piece.DARK) {
                            buttonBoard[piece.cell?.x!!][piece.cell!!.y]!!.setBackgroundResource(R.drawable.dark_piece_highlighted)
                        } else if (piece.color == Piece.LIGHT && piece.isKing) {
                            buttonBoard[piece.cell?.x!!][piece.cell?.y!!]!!.setBackgroundResource(R.drawable.light_king_highlighted)
                        } else if (piece.color == Piece.LIGHT) {
                            buttonBoard[piece.cell!!.x][piece.cell?.y!!]!!.setBackgroundResource(R.drawable.light_piece_highlighted)
                        }
                        piece.cell?.let { highlightedCells!!.add(it) }
                    }
                }
            }
        }
    }

    /*
     * When player clicks a piece, stores all of the possible moves and colors possible moves on cellBoard
     * @param int xCord - Gets the possible moves of a piece using this x-coordinate
     * @param int yCord - Gets the possible moves of a piece using this y-coordinate
     */
    fun showPossibleMoves(moves: ArrayList<Cell?>) {
        for (cell in moves) {
            buttonBoard[cell!!.x][cell.y]!!.setBackgroundResource(R.drawable.possible_moves_image) // color possible moves square
        }
    }

    /*
     * The dialog menu that pops up after a game has ended
     */
    fun gameOverDialog() {
        updateTurnTracker()
        val winner: String
        winner = if (!player1!!.hasMoves(cellBoard)) {
            "Player 2"
        } else {
            "Player 1"
        }
        val choices = arrayOf<CharSequence>("Play Again", "Return to Main Menu")
        val builder = AlertDialog.Builder(this@ButtonBoard)
        builder.setCancelable(false)
        builder.setTitle("$winner Wins!")
        builder.setItems(
            choices
        ) { _, clickValue -> // If user clicks New Match, create a new match
            if (clickValue == 0) {
                restartMatch()
            } else if (clickValue == 1) {
                quitMatch()
            }
        }
        builder.show()
    }

    /*
     * Loads a saved game if the user chooses to do so
     * Loads the game from a save file
     */
    fun loadGame() {
        try {
            val inputStream: InputStream = applicationContext.openFileInput("savedGame.dat")
            val objectInputStream = ObjectInputStream(inputStream)
            val savedState = objectInputStream.readObject() as State
            cellBoard = savedState.board
            player1 = savedState.player1
            player2 = savedState.player2
            currentPlayer = savedState.currentPlayer
            computerMode = savedState.isSinglePlayerMode
            srcCell = savedState.srcCell
            dstCell = savedState.dstCell
            srcCellFixed = savedState.isSrcCellFixed
            if (srcCellFixed && srcCell != null) {
                updatePiecePressed(srcCell)
                moves = cellBoard.getCaptureMoves(srcCell)
                showPossibleMoves(moves!!)
            }
        } catch (e: FileNotFoundException) {
            Toast.makeText(applicationContext, "No Game Saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Error loading the game", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /*
     * Saves the game when user chooses to do so
     * Saves the game to a save game file
     */
    fun saveGame() {
        try {
            val objectOutputStream = ObjectOutputStream(
                getApplicationContext().openFileOutput(
                    "savedGame.dat",
                    Context.MODE_PRIVATE
                )
            )
            objectOutputStream.writeObject(
                player1?.let {
                    player2?.let { it1 ->
                        srcCell?.let { it2 ->
                            dstCell?.let { it3 ->
                                currentPlayer?.let { it4 ->
                                    State(
                                        cellBoard,
                                        it,
                                        it1,
                                        it4,
                                        computerMode,
                                        it2,
                                        it3,
                                        srcCellFixed
                                    )
                                }
                            }
                        }
                    }
                }
            )
            objectOutputStream.close()
            Toast.makeText(getApplicationContext(), "Game Saved", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(
                getApplicationContext(),
                "Error in saving the game! ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /*
     * Deals with saving a game when a previous save game file is found
     */
    fun saveGameFound() {
        val choices = arrayOf<CharSequence>("Overwrite", "Cancel")
        val builder = AlertDialog.Builder(this@ButtonBoard)
        builder.setCancelable(true)
        builder.setTitle("A previously saved game was found. Overwrite?")
        builder.setItems(
            choices
        ) { dialog, clickValue ->
            if (clickValue == 0) {
                val file: File = applicationContext.getFileStreamPath("savedGame.dat")
                if (file != null || file.exists()) {
                    file.delete()
                }
                saveGame()
                Toast.makeText(applicationContext, "Match Saved!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }

    /*
     * When user chooses to restart a match, this dialog appears with confirmation menu
     */
    fun restartMatchDialog() {
        val choices = arrayOf<CharSequence>("Restart", "Cancel")
        val builder = AlertDialog.Builder(this@ButtonBoard)
        builder.setCancelable(true)
        builder.setTitle("Are you sure you want to restart?")
        builder.setItems(
            choices
        ) { dialog, clickValue ->
            if (clickValue == 0) {
                restartMatch()
            }
        }
        builder.show()
    }

    /*
     * Dialog menu when user tries to quit the match
     */
    fun quitMatchDialog() {
        val choices = arrayOf<CharSequence>("Quit", "Cancel")
        val builder = AlertDialog.Builder(this@ButtonBoard)
        builder.setCancelable(true)
        builder.setTitle("Are you sure you want to quit?")
        builder.setItems(
            choices
        ) { dialog, clickValue ->
            if (clickValue == 0) {
                quitMatch()
            }
        }
        builder.show()
    }

    /*
     * Restarts the match
     */
    fun restartMatch() {
        Toast.makeText(applicationContext, "Match Restarted!", Toast.LENGTH_SHORT).show()
        val restartMatch = Intent(this@ButtonBoard, ButtonBoard::class.java)
        startActivity(restartMatch)
    }

    /*
     * Quits the match, returns to MainMenu.java activity
     */
    fun quitMatch() {
        val exitGame = Intent(this@ButtonBoard, MainActivity::class.java)
        exitGame.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        exitGame.putExtra("EXIT", true)
        startActivity(exitGame)
    }

    /*
    * Adds Quick Menu at top-right corner with following options: Save, Load, Restart, Quit
    * @param Menu menu
    * @ret boolean
    */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.play_game_drop_down_menu, menu) //Menu Resource, Menu
        return true
    }

    /*
     * Adds the following options: Save, Load, Restart, Quit to the Quick Menu with case on clicked
     * @param MenuItem menu
     * @ret boolean
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveGame -> {
                val file: File = applicationContext.getFileStreamPath("savedGame.dat")
                if (file == null || !file.exists()) {
                    saveGame()
                } else {
                    saveGameFound()
                }
                true
            }
            R.id.restartMatch -> {
                restartMatchDialog()
                true
            }
            R.id.quitMatch -> {
                quitMatchDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
         * If device orientation changes after activity is started, we want to change the board according
         * @param Configuration newConfig
         */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig != null) {
            super.onConfigurationChanged(newConfig)
        }
        // If device is in portrait mode
        if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_PORTRAIT) {
            resizeBoardToScreenSizePortrait()
        } else if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_LANDSCAPE) {
            resizeBoardToScreenSizeLandscape()
        }
    }

    /*
     * Resizes the gameboard and pieces according to the screen size (Portrait)
     * Scales the width & height according to the required dimensions
     * Testing & working with:
     *      - Galaxy S3 1280x720 (phone)
     *      - Nexus 5  1080x1920 (phone)
     *      - Nexus 9  2048x1536 (tablet)
     *      - Pixel XL 1440x2560 (phone)
     */
    private fun resizeBoardToScreenSizePortrait() {
        // Gets the width of the current screen
        val wm = this.getApplication().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels.toDouble()

        // Sets the width & height for the game board image
        val imageView = findViewById(R.id.boardImageView) as ImageView
        val imageParams = imageView.layoutParams
        imageParams.width = (width * 1.0028).toInt()
        imageParams.height = (width * 1.0085).toInt()
        imageView.layoutParams = imageParams

        // Sets the width & height for the grid of game buttons in the layout
        val buttonLayout = findViewById(R.id.parent_layout) as LinearLayout
        val buttonLayoutParams =
            buttonLayout.layoutParams // Gets the layout params that will allow you to resize the layout
        buttonLayoutParams.width = (width * 0.967).toInt()
        buttonLayoutParams.height = (width * 0.9723).toInt()
        buttonLayout.layoutParams = buttonLayoutParams
    }

    /*
     * Resizes the gameboard and pieces according to the screen size (Landscape)
     * Scales the width & height according to the required dimensions
     * Testing & working with:
     *      - Galaxy S3 1280x720 (phone)
     *      - Nexus 5  1080x1920 (phone)
     *      - Nexus 9  2048x1536 (tablet)
     *      - Pixel XL 1440x2560 (phone)
     */
    fun resizeBoardToScreenSizeLandscape() {
        // Gets the height of the action bar, so we can prevent action bar from partially hiding the board
        val styledAttributes: TypedArray = getApplicationContext().getTheme()
            .obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val actionBarHeight = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()

        // Gets the height of the current screen
        val wm = this.getApplication().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val height =
            metrics.heightPixels - actionBarHeight * 1.75 // subtract size of top action bar so it doesn't partially hide board

        // Sets the width & height for the game board image
        val imageView = findViewById<ImageView>(R.id.boardImageView)
        val imageParams = imageView.layoutParams
        imageParams.width = (height * 1.0028).toInt()
        imageParams.height = (height * 1.0085).toInt()
        imageView.layoutParams = imageParams

        // Sets the width & height for the grid of game buttons in the layout
        val buttonLayout = findViewById<LinearLayout>(R.id.parent_layout)
        val buttonLayoutParams =
            buttonLayout.layoutParams // Gets the layout params that will allow you to resize the layout
        buttonLayoutParams.width = (height * 0.967).toInt()
        buttonLayoutParams.height = (height * 0.9723).toInt()
        buttonLayout.layoutParams = buttonLayoutParams
    }

    /*
    * When back button is pressed, do not restart activity
    */
    override fun onBackPressed() {}
}