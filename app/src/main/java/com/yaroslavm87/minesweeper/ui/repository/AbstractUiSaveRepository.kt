package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import java.io.File

abstract class AbstractUiSaveRepository<T>(context: Context) : AbstractUiRepository(context) {

    //abstract fun save(value: List<T>)

    protected fun saveValueToFile(file: File, value: String) {
        if (file.canWrite())
            context.openFileOutput(file.name, Context.MODE_PRIVATE).use { fos ->
                fos.write(value.toByteArray())
                log("--- saving operation to file name = '${file.name}' performed")
            }
        else
            log("--- cannot perform write operation with file name = '${file.name}'")
    }

}