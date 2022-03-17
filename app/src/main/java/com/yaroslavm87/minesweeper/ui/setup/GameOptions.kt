package com.yaroslavm87.minesweeper.ui.setup

import kotlinx.serialization.Serializable

@Serializable
class GameOptions (
    val columnsAmount: Int,
    val rowsAmount: Int,
    val minesAmount: Int
) {
    var cellsAmount = 0
    var minesPercentage = 0F

    init {
        cellsAmount = (columnsAmount * rowsAmount)
        minesPercentage = ((minesAmount.toFloat() / (columnsAmount.toFloat() * rowsAmount.toFloat()) * 100).format(2)).toFloat()
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}