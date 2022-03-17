package com.yaroslavm87.minesweeper.eventBus

interface Subscriber {

    fun subscribeForEvent()

    fun notifyOfEvent(event: EventBus.Event)

    fun fetchEventLinkedValueAndProceed(event: EventBus.Event)

    fun unsubscribeFromEvent()

}