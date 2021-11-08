package com.example.gameofdraughts

import java.io.Serializable


class Cell(x: Int, y: Int) : Serializable { // End of class
    /**
     * @return Returns the x-coordinate of this cell.
     */
    val x: Int

    /**
     * @return Returns the y-coordinate of this cell.
     */
    val y: Int

    /**
     * @return Returns the piece placed (null if no piece is placed) in this cell.
     */
    var piece: Piece?
        private set

    /**
     * @return Returns the coordinate of this cell as an integer array of length two,
     * in which the first element is the x-coordinate of the cell and the second value is the y-coordinate of the cell.
     */
    val coords: IntArray
        get() = intArrayOf(x, y)

    /**
     * @param givenPiece The piece to place in this cell. If the piece are to their opposite end then the piece is made King.
     */
    fun placePiece(givenPiece: Piece?) {
        piece = givenPiece
        if (givenPiece != null) {
            givenPiece.cell = this
            if (x == 0 && givenPiece.color == Piece.DARK) {
                piece!!.makeKing()
            } else if (x == 7 && givenPiece.color == Piece.LIGHT) {
                piece!!.makeKing()
            }
        }
    }

    /**
     * @return Returns if the cell contains any piece i.e returns true if this cell contains piece and false if the placed piece of this cell is null.
     */
    fun containsPiece(): Boolean {
        return piece != null
    }

    /**
     * @param anotherCell Cell where the piece in this cell is to be moved.
     * @throws IllegalArgumentException Throws IllegalArgumentException if the Cell provided is null.
     */
    @Throws(IllegalArgumentException::class)
    fun movePiece(anotherCell: Cell?) {
        requireNotNull(anotherCell) { "Provided cell is null. Cannot move to a null Cell." }
        anotherCell.placePiece(piece)
        piece!!.cell = anotherCell
        piece = null
    }

    /**
     * @return String representation of the Cell.
     */
    override fun toString(): String {
        var str = ""
        str += "Cell Loc: (" + x + ", " + y + ") \t Placed piece: "
        str += if (piece == null) {
            "nothing\n"
        } else {
            """${piece!!.color}  isKing: ${piece!!.isKing}
"""
        }
        return str
    }

    /**
     * Creates an instance of Cell with given x-coordinate and y-coordinate with no piece placed in the cell i.e. piece placed in the cell is null.
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     */
    init {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            println("The provided coordinates for the cell are out of range.")
        }
        this.x = x
        this.y = y
        piece = null
    }
}



