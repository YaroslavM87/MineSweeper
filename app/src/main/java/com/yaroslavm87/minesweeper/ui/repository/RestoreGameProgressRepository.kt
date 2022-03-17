package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import com.yaroslavm87.minesweeper.model.ModelConst
import com.yaroslavm87.minesweeper.model.GameProgress
import com.yaroslavm87.minesweeper.model.ModelRestoreRepository
import com.yaroslavm87.minesweeper.ui.setup.GameOptions
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RestoreGameProgressRepository(context: Context) :
    ModelRestoreRepository<GameProgress?>,
    AbstractUiRestoreRepository<GameOptions>(context)
{

    private val fileName = ModelConst.filenameGameProgress

    init {
        className = "RestoreGameProgressRepository"
        loggingEnabled = true
    }

    override fun restore(): GameProgress? {
        log("restore()")
        return restoreFile(fileName)?.let { file ->
            Json.decodeFromString(readFromFile(file))
        }
    }

//    override fun restore(): GameProgress? {
//        log("restore()")
//
//        val restoredFile = context.cacheDir.list()
//
//            ?.filter { fileName ->
//                fileName.contains(fileName)
//
//            }?.let { filteredList ->
//
//                if (filteredList.isNotEmpty()) {
//                    val fileName = filteredList[0]
//                    log("--- restored file name = '$fileName'")
//                    File(context.cacheDir, fileName)
//
//                } else {
//                    log("--- file with name '${ModelConst.filenameGameProgress}' does not exist")
//                    null
//                }
//            }
//
//        return restoredFile?.let { file ->
//            context.openFileInput(file.name)
//                .use { fis ->
//                    Json.decodeFromString<GameProgress>(String(fis.readBytes()))
//                }
//        }
//    }

}