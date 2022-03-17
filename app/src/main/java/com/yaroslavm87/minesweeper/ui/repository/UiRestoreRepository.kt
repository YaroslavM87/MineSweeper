package com.yaroslavm87.minesweeper.ui.repository

interface UiRestoreRepository<T> {

    fun restore(): List<T>
}