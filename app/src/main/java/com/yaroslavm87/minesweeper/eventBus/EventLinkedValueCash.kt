package com.yaroslavm87.minesweeper.eventBus

import android.util.Log
import com.yaroslavm87.minesweeper.model.ModelConst
import java.util.*
import kotlin.collections.HashMap

object EventLinkedValueCash {

    private val globalValueCash = HashMap<Int, Deque<Any>>()
    private val lastValueCash = HashMap<EventBus.Event, Any>()
    private const val className: String = "EventLinkedValueCash"
    private const val loggingEnabled = false

    internal fun putToSubscribersCashValue(subscriber: Subscriber, event: EventBus.Event, value: Any) {
        log("putToSubscribersCashValue(): for $event")
        val key = computeHashKey(subscriber, event)
        var subscriberValueCash = globalValueCash[key]
        if (subscriberValueCash == null) {
            log("--- create new subscriberValueCash for $event")
            subscriberValueCash = LinkedList()
            globalValueCash[key] = subscriberValueCash
        }
        subscriberValueCash.addLast(value)
    }

    internal fun putToLastCashValue(event: EventBus.Event, value: Any) {
        log("putToLastCashValue(): for $event")
        lastValueCash[event] = value
    }

    internal fun getFromSubscribersCashValue(subscriber: Subscriber, event: EventBus.Event) : Any? {
        log("getFromSubscribersCashValue(): for $event")
        return globalValueCash[computeHashKey(subscriber, event)]?.pollFirst(). also {
            log("getFromSubscribersCashValue(): value from cash = $it")
        }
    }

    internal fun getFromLastCashValue(event: EventBus.Event) : Any? {
        log("getFromLastCashValue(): for $event")
        return lastValueCash[event].also {
            log("--- value from cash = $it")
        }
    }

    internal fun onSubscriptionCancelled(
        subscriber: Subscriber,
        event: EventBus.Event,
        isLastSubscriberForEvent: Boolean
    ) {
        log("onSubscriptionCancelled(): remove valueCash for $subscriber and $event")
        globalValueCash.remove(computeHashKey(subscriber, event))
        if (isLastSubscriberForEvent) lastValueCash.remove(event)
    }

    private fun computeHashKey(subscriber: Subscriber, event: EventBus.Event): Int {
        log("computeHashKey(): for $subscriber and $event")
        return subscriber.hashCode() + event.hashCode().also {
            log("--- hashCode = $it")
        }
    }

    internal fun hasLastValueInCash(event: EventBus.Event) : Boolean {
        return lastValueCash.contains(event).also {
            log("hasLastValueInCash(): $it for $event")
        }
    }

    internal fun hasSubscribersValueInCash(subscriber: Subscriber, event: EventBus.Event) : Boolean {
        return globalValueCash[computeHashKey(subscriber, event)]?.isEmpty()?.also {
            log("hasSubscribersValueInCash(): $it for $event")
        } ?: false.also {
            log("hasSubscribersValueInCash(): globalValueCash does not contain value cash for $subscriber and $event")
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }

}