package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import android.util.Log
import com.yaroslavm87.minesweeper.model.ModelConst
import com.yaroslavm87.minesweeper.ui.setup.GameOptions
import kotlinx.serialization.decodeFromString

import kotlinx.serialization.json.Json
import java.io.File

class RestoreGameOptionsRepository(context: Context) :
    AbstractUiRestoreRepository<GameOptions>(context),
    UiRestoreRepository<GameOptions>
{
    private val fileName = ModelConst.filenameGameOptions

    init {
        className = "RestoreGameOptionsRepository"
        loggingEnabled = true
    }

    override fun restore(): MutableList<GameOptions> {
        log("restore()")
        return restoreFile(fileName)?.let { file ->
            Json.decodeFromString(readFromFile(file))
        } ?: mutableListOf()
    }

}