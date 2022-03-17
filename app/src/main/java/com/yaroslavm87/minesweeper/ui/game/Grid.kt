package com.yaroslavm87.minesweeper.ui.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.yaroslavm87.minesweeper.R
import com.yaroslavm87.minesweeper.model.CellStateType
import kotlin.IllegalArgumentException

class Grid(
    context: Context,
    screenWidth: Int,
    screenHeight: Int,
    private val cellsAmountForX: Int,
    cellsAmountForY: Int
    ) {

    private val className: String = "Grid"
    private val loggingEnabled = true

    private val params = GridParams(context, screenWidth, screenHeight, cellsAmountForX, cellsAmountForY)

    private val hiddenCell = AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_unexplored)
    private val hiddenMarkedCell = AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_unexplored_marked)
    private val revealedCellValues: Array<Drawable?> = arrayOf(
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_empty),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_one),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_two),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_three),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_four),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_five),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_six),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_seven),
        AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_eight)
    )
    private val revealedCellWithMineExploded = AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_mine_v2_exploded)
    private val revealedCellWithMineOther = AppCompatResources.getDrawable(context, R.drawable.ic_mine_field_item_explored_mine_v2_other)

    private val backgroundColor = context.resources.getColor(R.color.grey_80)

    private var lastMineFieldValues: Array<CellStateType>? = null

    internal fun drawMineField(
        canvas: Canvas?,
        updatedMineFieldValues: Array<CellStateType>?,
        negativeOffsetX: Int,
        negativeOffsetY: Int
    ) {

        fun getX(index: Int): Int {
            return if (index < cellsAmountForX) params.coordsForX[index]
            else params.coordsForX[index % cellsAmountForX]
        }

        fun getY(index: Int): Int {
            return if (index < cellsAmountForX) params.coordsForY[0]
            else params.coordsForY[(index - (index % cellsAmountForX)) / cellsAmountForX]
        }

        fun draw(valuesToDraw: Array<CellStateType>) {
            if (canvas == null) return
            var x: Int
            var y: Int
            canvas.drawColor(backgroundColor)

            valuesToDraw.indices.forEach { cellIndex ->

                x = getX(cellIndex)
                y = getY(cellIndex)

                val xIsVisible = (x + negativeOffsetX) in params.drawableFieldRangeForX
                val yIsVisible = (y + negativeOffsetY) in params.drawableFieldRangeForY

                if (xIsVisible && yIsVisible) {
                    val drawable = pickAppropriateDrawable(valuesToDraw[cellIndex])
                    if (drawable != null) {
                        drawable.setBounds(
                            x + negativeOffsetX,
                            y + negativeOffsetY,
                            x + params.cellLength + negativeOffsetX,
                            y + params.cellLength + negativeOffsetY
                        )
                        drawable.draw(canvas)
                    }
                }
            }
        }

        if (updatedMineFieldValues != null) {
            draw(updatedMineFieldValues)
            lastMineFieldValues = updatedMineFieldValues

        } else {
            val copy = lastMineFieldValues?.clone()
            if (copy != null) {
                draw(copy)
            } else {
                throw IllegalArgumentException("Grid.drawMineField(): cannot draw mine field as mine field values are null")
            }
        }
    }

    // is used for touch event in SV
    internal fun getIndexForGivenCoords(x: Int, y: Int): Int? {
        var indexForXCoord = -1
        var indexForYCoord = -1

        params.coordsForX.indices.forEach { indexOfX ->
            if (x in params.coordsForX[indexOfX] until (params.coordsForX[indexOfX] + params.cellLength)) indexForXCoord = indexOfX
        }
        params.coordsForY.indices.forEach { indexOfY ->
            if (y in params.coordsForY[indexOfY] until (params.coordsForY[indexOfY] + params.cellLength)) indexForYCoord = indexOfY
        }

        return if (indexForXCoord >= 0 && indexForYCoord >= 0)
            indexForXCoord + (indexForYCoord * params.coordsForX.size)
        else null
    }

    internal fun getFieldWidth(): Int {
        return params.fieldWidth
    }

    internal fun getFieldHeight(): Int {
        return params.fieldHeight
    }

    private fun pickAppropriateDrawable(cellStateType: CellStateType): Drawable? {
        return when (cellStateType) {

            CellStateType.HIDDEN ->
                hiddenCell

            CellStateType.HIDDEN_MARKED ->
                hiddenMarkedCell

            CellStateType.REVEALED_0,
            CellStateType.REVEALED_0_MARKED ->
                revealedCellValues[0]

            CellStateType.REVEALED_1,
            CellStateType.REVEALED_1_MARKED ->
                revealedCellValues[1]

            CellStateType.REVEALED_2,
            CellStateType.REVEALED_2_MARKED ->
                revealedCellValues[2]

            CellStateType.REVEALED_3,
            CellStateType.REVEALED_3_MARKED ->
                revealedCellValues[3]

            CellStateType.REVEALED_4,
            CellStateType.REVEALED_4_MARKED ->
                revealedCellValues[4]

            CellStateType.REVEALED_5,
            CellStateType.REVEALED_5_MARKED ->
                revealedCellValues[5]

            CellStateType.REVEALED_6,
            CellStateType.REVEALED_6_MARKED ->
                revealedCellValues[6]

            CellStateType.REVEALED_7,
            CellStateType.REVEALED_7_MARKED ->
                revealedCellValues[7]

            CellStateType.REVEALED_8,
            CellStateType.REVEALED_8_MARKED ->
                revealedCellValues[8]

            CellStateType.REVEALED_MINE_NOT_EXPLODED,
            CellStateType.REVEALED_MINE_NOT_EXPLODED_MARKED ->
                revealedCellWithMineOther

            CellStateType.REVEALED_MINE_EXPLODED ->
                revealedCellWithMineExploded
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(com.yaroslavm87.minesweeper.model.ModelConst.LOG_TAG, "$className.$message")
    }

}