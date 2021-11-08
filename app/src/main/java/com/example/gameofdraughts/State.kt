package com.example.gameofdraughts

import java.io.Serializable


/**
 * Created by bregmi1 on 3/31/2017.
 */
class State(
    val board: Board,
    val player1: Player,
    val player2: Player,
    val currentPlayer: Player,
    val isSinglePlayerMode: Boolean,
    val srcCell: Cell,
    val dstCell: Cell,
    val isSrcCellFixed: Boolean
) :
    Serializable
