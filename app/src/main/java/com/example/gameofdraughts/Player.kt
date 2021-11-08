package com.example.gameofdraughts

import java.io.Serializable

class Player(givenColor: String) : Serializable {
    var color: String? = null

    fun hasMoves(board: Board): Boolean {
        val pieces = board.getPieces(color!!)
        if (pieces.size > 0) {
            for (piece in pieces) {
                if (board.possibleMoves(piece).size > 0) {
                    return true
                }
            }
        }
        return false
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is Player) {
            return false
        }
        return if (obj.color == color) {
            true
        } else false
    }

    /**
     * @param givenColor of the piece that the player is associated to
     * @throws IllegalArgumentException if the given color does not matches with one of the two colors of the pieces in the board
     */
    init {
        if (givenColor === Piece.LIGHT || givenColor === Piece.DARK) {
            color = givenColor
        } else {
            throw IllegalArgumentException("Given color for the player is not valid. Given color: $givenColor")
        }
    }
}