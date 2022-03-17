package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import com.yaroslavm87.minesweeper.model.ModelConst
import com.yaroslavm87.minesweeper.ui.setup.GameOptions
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SaveGameOptionsRepository(context: Context) :
    AbstractUiSaveRepository<GameOptions>(context),
    UiSaveRepository<GameOptions>
{

    init {
        className = "SaveGameOptionsRepository"
        loggingEnabled = true
    }

    override fun save(value: List<GameOptions>) {
        log("save()")
        restoreFile(ModelConst.filenameGameOptions)?.let { file ->
            saveValueToFile(file, Json.encodeToString(value))
        }
    }
}