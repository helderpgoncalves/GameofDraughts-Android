package com.example.gameofdraughts

import java.io.Serializable


/**
 * Class representing a piece in Checker. A piece can have one of the two colors: Light (given by Piece.LIGHT) and Dark (given by Piece.DARK).
 * A piece can become king if it reaches the opposite end.
 */
class Piece(color: String) : Serializable {
    /**
     * @return Returns the color of this Piece (either Piece.LIGHT or Piece.DARK).
     */
    val color: String

    /**
     * @return Returns if this Piece is a King. Returns true if this Piece is King, false otherwise.
     */
    var isKing: Boolean
        private set
    /**
     * @return Returns the cell on which this Piece is placed.
     */
    /**
     * Sets the placed piece of the given Cell to be this Piece.
     * @param givenCell The Cell in which this Piece is to be placed.
     */
    var cell: Cell?

    /**
     * Makes this piece a King.
     */
    fun makeKing() {
        isKing = true
    }

    /**
     * Checks if the given Object is equal to this Piece.
     * @param obj Object to be compared
     * @return Returns true if the given object is equal to this Piece, false otherwise.
     * The given object is equal to this Piece if the given object is an instance of Piece, has the same color as this Piece and is located in the same Cell location as this Piece.
     */
    override fun equals(obj: Any?): Boolean {
        if (obj !is Piece) {
            return false
        }
        val givenPiece = obj
        return if (givenPiece.color == color && givenPiece.isKing == isKing && givenPiece.cell!!.x == cell!!.x && givenPiece.cell!!.y == cell!!.y
        ) {
            true
        } else false
    }

    companion object {
        const val DARK = "Dark"
        const val LIGHT = "Light"

        /**
         * Returns the color of the opponent player i.e. returns the color opposite of this Piece
         * @param: Color of the player
         * @return: opponent's color
         */
        fun getOpponentColor(givenColor: String): String? {
            return if (givenColor == DARK) {
                LIGHT
            } else if (givenColor == LIGHT) {
                DARK
            } else {
                println("Given color is not valid. Given color: $givenColor")
                null
            }
        }
    }

    /**
     * Creates an instance of Piece with given color.
     * @param color The color of the piece (has to be Piece.LIGHT or Piece.DARK).
     * @throws IllegalArgumentException Throws IllegalArgumentException if the given color is not equal to Piece.LIGHT or Piece.DARK
     */
    init {
        require(color == DARK || color == LIGHT) { "The provided color for piece is not valid. Provided color: $color" }
        this.color = color
        isKing = false
        cell = null
    }
} //End of class
