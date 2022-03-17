package com.yaroslavm87.minesweeper.ui.game

import android.content.Context
import android.util.Log
import com.yaroslavm87.minesweeper.R

class GridParams(
    context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int,
    cellsAmountForX: Int,
    cellsAmountForY: Int
) {

    private val className: String = "GridParams"
    private val loggingEnabled = true

    internal var incrementCellLength = 20
        set(value) {
            if (value >= 0) field = value
            recalcCoords()
        }

    internal var incrementInnerSpacing = 5
        set(value) {
            if (value >= 0) field = value
            recalcCoords()
        }

    internal val cellLength =
        (context.resources.getDimension(R.dimen.grid_base_cell_length) /
        context.resources.displayMetrics.density).toInt() +
        incrementCellLength

    private val innerSpacing =
        (context.resources.getDimension(R.dimen.grid_base_padding) /
        context.resources.displayMetrics.density).toInt() +
        incrementInnerSpacing

    private val borderSpacing = innerSpacing * 2

    internal val fieldWidth =
        borderSpacing * 2 +
        cellLength * cellsAmountForX +
        innerSpacing * (cellsAmountForX - 1)

    internal val fieldHeight =
        borderSpacing * 2 +
        cellLength * cellsAmountForY +
        innerSpacing * (cellsAmountForY - 1)

    internal var coordsForX = calcCoords(cellsAmountForX, screenWidth, fieldWidth)

    internal var coordsForY = calcCoords(cellsAmountForY, screenHeight, fieldHeight)

    internal val drawableFieldRangeForX = -cellLength..(screenWidth + cellLength)

    internal val drawableFieldRangeForY = -cellLength..(screenHeight + cellLength)

    private fun calcCoords(cellsAmount: Int, screenLength: Int, fieldLength: Int): IntArray {
        var increment = 0
        if (screenLength > fieldLength) increment = (screenLength - fieldLength) / 2

        return IntArray(cellsAmount) { index ->
            if (index == 0) borderSpacing + increment
            else borderSpacing + ((cellLength + innerSpacing) * index) + increment
        }
    }

    private fun recalcCoords() {
        coordsForX = calcCoords(coordsForX.size, screenWidth, fieldHeight)
        coordsForY = calcCoords(coordsForY.size, screenHeight, fieldHeight)
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(com.yaroslavm87.minesweeper.model.ModelConst.LOG_TAG, "$className.$message")
    }
}