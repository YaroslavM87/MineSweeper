package com.yaroslavm87.minesweeper.model

interface ModelSaveRepository<T> {

    fun save(value: T)
}