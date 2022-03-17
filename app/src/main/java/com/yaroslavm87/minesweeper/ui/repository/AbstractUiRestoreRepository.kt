package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import java.io.File

abstract class AbstractUiRestoreRepository<T>(context: Context) : AbstractUiRepository(context) {

    //abstract fun restore(): List<T>

    protected fun readFromFile(file: File): String {
        return if (file.canRead()) context.openFileInput(file.name).use {
            log("--- reading operation from file name = '${file.name}' started")
            String(it.readBytes())
        } else {
            log("--- cannot perform read operation with file name = '${file.name}'")
            ""
        }
    }

}