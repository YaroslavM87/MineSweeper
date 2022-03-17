package com.yaroslavm87.minesweeper.eventBus

import android.util.Log
import com.yaroslavm87.minesweeper.model.ModelConst

object SubscribersReferenceKeeper {

    private val setsOfSubscribers = mutableMapOf<EventBus.Event, MutableSet<Subscriber>>()
    private const val className: String = "SubscribersReferenceKeeper"
    private const val loggingEnabled = false


    fun hasSubscribersFor(event: EventBus.Event) : Boolean {
        return setsOfSubscribers.containsKey(event).also {
            log("hasSubscribersFor(): for $event = $it")
        }
    }

    fun isSubscribedFor(subscriber: Subscriber, event: EventBus.Event) : Boolean {
        log("isSubscribedFor(): for $event")
        return setsOfSubscribers[event]?.contains(subscriber).also {
            log("--- setsOfSubscribers[event].contains(subscriber) = $it")
        } ?: false.also {
            log("--- setsOfSubscribers[event] = null")
        }
    }

    fun registerSubscriber(subscriber: Subscriber, event: EventBus.Event) {
        log("registerSubscriber(): for $event")
        getOrCreate(event).add(subscriber).also {
            log("registerSubscriber(): add(subscriber) = $it")
        }
    }

    fun getSubscribers(event: EventBus.Event) : Set<Subscriber>? {
        log("getSubscribers()")
        val result = setsOfSubscribers[event]
        return if (result != null) {
            result.also {
                log("--- return setOfSubscribers for $event")
                removeIfEmpty(it, event)
            }
        } else {
            log("--- there are no setOfSubscribers for $event")
            null
        }
    }

    fun unregisterSubscriber(subscriber: Subscriber, event: EventBus.Event) {
        val set = setsOfSubscribers[event]
        set?.apply {
            log("unregisterSubscriber(): remove subscriber from setOfSubscribers for $event")
            remove(subscriber)
        }?.also {
            removeIfEmpty(it, event)
        }
    }

    private fun getOrCreate(event: EventBus.Event) : MutableSet<Subscriber> {
        return setsOfSubscribers[event].also {
            if (it != null) log("getOrCreate(): get setOfSubscribers for $event")
        }
            ?: mutableSetOf<Subscriber>().also {
                log("getOrCreate(): create setOfSubscribers for $event")
                setsOfSubscribers[event] = it
            }
    }

    private fun removeIfEmpty(
        set: MutableSet<Subscriber>,
        event: EventBus.Event
    ) {
        if (set.isEmpty()) {
            log("removeIfEmpty(): remove set from setsOfSubscribers for $event")
            setsOfSubscribers.remove(event)
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }
}