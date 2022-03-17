package com.yaroslavm87.minesweeper.ui.setup

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.yaroslavm87.minesweeper.model.Model
import com.yaroslavm87.minesweeper.ui.LiveDataProvider
import com.yaroslavm87.minesweeper.ui.UiConst
import com.yaroslavm87.minesweeper.ui.repository.RestoreGameOptionsRepository
import com.yaroslavm87.minesweeper.ui.repository.RestoreGameProgressRepository
import com.yaroslavm87.minesweeper.ui.repository.SaveGameOptionsRepository

class ViewModelSetup : ViewModel(), FragmentSetup.OnFragmentSetupEventListener {

    internal var chosenGameOptionsIndex: Int? = null
    internal var gameOptionsList = mutableListOf<GameOptions>()
    private val className: String = "ViewModelSetup"
    private val loggingEnabled: Boolean = true

    internal fun setScreenDimensions(x: Int, y: Int) {
        LiveDataProvider.screenWidth.value = x
        LiveDataProvider.screenHeight.value = y
    }

    internal fun restoreGameOptionsList(context: Context) {
        gameOptionsList.clear()
        gameOptionsList.addAll(RestoreGameOptionsRepository(context).restore())
    }

    internal fun saveGameOptionsList(context: Context) {
        SaveGameOptionsRepository(context).save(gameOptionsList)
    }

    internal fun restoreGameState(context: Context) {
        Model.restoreGameStateFrom(RestoreGameProgressRepository(context))
    }

    override fun onSetupFragmentEvent(event: FragmentSetup.SetupFragmentEvents) {
        when(event) {
            FragmentSetup.SetupFragmentEvents.START_GAME_CALL -> {
                log("onSetupFragmentEvent(): incoming event is $event")
                if (chosenGameOptionsIndex != null) {
                    gameOptionsList[chosenGameOptionsIndex!!].apply {
                        setModelWithParams(columnsAmount, rowsAmount, minesAmount)
                        LiveDataProvider.apply {
                            mineFieldColumnsAmount.value = columnsAmount
                            mineFieldRowsAmount.value = rowsAmount
                        }
                    }
                }
            }
        }
    }

    private fun setModelWithParams(columns: Int, rows: Int, minesAmount: Int) {
        Model.setColumnsAmount(columns)
        Model.setRowsAmount(rows)
        Model.setMinesAmount(minesAmount)
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(UiConst.LOG_TAG, "$className.$message")
    }

    override fun onCleared() {
        super.onCleared()
        log("onCleared")
    }
}