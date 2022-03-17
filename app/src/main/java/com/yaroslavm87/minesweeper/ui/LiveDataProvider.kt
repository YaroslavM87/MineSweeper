package com.yaroslavm87.minesweeper.ui

import androidx.lifecycle.MutableLiveData
import com.yaroslavm87.minesweeper.ui.game.ViewModelMineFieldCommands

object LiveDataProvider {
    val viewModelMineFieldCommands = MutableLiveData<ViewModelMineFieldCommands>()
    val mineFieldRowsAmount = MutableLiveData<Int>()
    val mineFieldColumnsAmount = MutableLiveData<Int>()
    val screenWidth = MutableLiveData<Int>()
    val screenHeight = MutableLiveData<Int>()
}