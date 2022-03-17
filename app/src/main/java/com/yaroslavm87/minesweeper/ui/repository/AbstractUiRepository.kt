package com.yaroslavm87.minesweeper.ui.repository

import android.content.Context
import android.util.Log
import com.yaroslavm87.minesweeper.model.ModelConst
import java.io.File

abstract class AbstractUiRepository(protected val context: Context) {

    protected var className: String = "AbstractUiRepository"
    protected var loggingEnabled = true

    protected fun restoreFile(fileName: String): File? {
        return context.cacheDir.list()
            ?.filter { name ->
                name.contains(fileName)
            }
            ?.let { filteredList ->
                if (filteredList.isNotEmpty()) {
                    val name = filteredList[0]
                    log("--- restored file name = '$name'")
                    File(context.cacheDir, name)
                } else {
                    log("--- file with name '${fileName}' does not exist")
                    null
                }
            }
    }

    protected fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }

}