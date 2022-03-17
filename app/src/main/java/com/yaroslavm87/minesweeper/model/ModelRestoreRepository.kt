package com.yaroslavm87.minesweeper.model

interface ModelRestoreRepository<T> {

    fun restore(): T
}