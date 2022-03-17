package com.yaroslavm87.minesweeper.model

object ModelConst {

    const val columnsMaxAmount = 50
    const val rowsMaxAmount = 50
    const val columnsMinAmount = 3
    const val rowsMinAmount = 3
    const val minesMaxProportion = 0.25F
    const val minesMaxAmount = (columnsMaxAmount * rowsMaxAmount * minesMaxProportion).toInt()
    const val minesMinAmount = 1

    const val LOG_TAG = "MSTag"
    const val filenameGameProgress = "GameProgress"
    const val filenameGameOptions = "GameOptions"

}