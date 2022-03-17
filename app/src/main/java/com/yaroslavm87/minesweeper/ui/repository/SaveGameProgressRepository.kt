package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import com.yaroslavm87.minesweeper.model.ModelConst
import com.yaroslavm87.minesweeper.model.GameProgress
import com.yaroslavm87.minesweeper.model.ModelSaveRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SaveGameProgressRepository(context: Context) :
    AbstractUiSaveRepository<GameProgress>(context),
    ModelSaveRepository<GameProgress>
{

    private val fileName = ModelConst.filenameGameProgress

    init {
        className = "SaveGameProgressRepository"
        loggingEnabled = true
    }

    override fun save(value: GameProgress) {
        log("save()")
        restoreFile(fileName)?.let { file ->
            saveValueToFile(file, Json.encodeToString(value))
        }
    }
}