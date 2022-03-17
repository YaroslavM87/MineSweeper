package com.yaroslavm87.minesweeper.model

import android.util.Log

class MineField private constructor (
    private val columnsAmount: Int,
    private val rowsAmount: Int,
    private val mineIndices: IntArray,
) {
    internal val field: Array<FieldCell> = fillMineField()
    private val className: String = "FieldCell"
    private val loggingEnabled = true


    private fun fillMineField(): Array<FieldCell> {
        FieldCell
            .amountOfCellsInField(columnsAmount * rowsAmount)
            .fieldColumnsAmount(columnsAmount)
            .mineCellIndices(mineIndices)

        return Array(columnsAmount * rowsAmount) {
            FieldCell
                .cellIndex(it)
                .build()
        }
    }

    internal companion object Builder {

        private const val className: String = "FieldCell.Builder"

        internal fun build(
            columnsAmount: Int,
            rowsAmount: Int,
            mineIndices: IntArray
        ): MineField {

            var isMineIndexWithinMineField = true
            mineIndices.forEach {
                isMineIndexWithinMineField = isMineIndexWithinMineField
                        && it in 0 until rowsAmount * columnsAmount
            }

            if (
                columnsAmount < 0
                || rowsAmount < 0
                || mineIndices.size !in 1..(rowsAmount * columnsAmount)
                || !isMineIndexWithinMineField
            ) throw IllegalArgumentException("$className.build(): illegal initial parameters in MineField")

            return MineField(
                columnsAmount,
                rowsAmount,
                mineIndices
            )
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }

}