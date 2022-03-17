package com.yaroslavm87.minesweeper.ui.repository

interface UiSaveRepository<T> {

    fun save(value: List<T>)
}