package com.yaroslavm87.minesweeper.eventBus

import android.util.Log
import com.yaroslavm87.minesweeper.model.ModelConst

object EventBus {

    interface Event

    private const val className: String = "EventBus"
    private const val loggingEnabled = false

    fun subscribe(subscriber: Subscriber, vararg events: Event) {
        for (event in events) {
            log("subscribe(): $subscriber for $event")
            SubscribersReferenceKeeper.registerSubscriber(subscriber, event)
            if (EventLinkedValueCash.hasLastValueInCash(event)) {
                log("subscribe(): notify $subscriber for $event")
                subscriber.notifyOfEvent(event)
            }
        }
    }

    fun notifySubscribersWithValue(event: Event, linkedValue: Any) {
        val set = SubscribersReferenceKeeper.getSubscribers(event)
        if(set != null && set.isNotEmpty()) {
            for(subscriber in set) {
                log("notifySubscribersWithValue(): notify $subscriber for $event")
                EventLinkedValueCash.putToSubscribersCashValue(subscriber, event, linkedValue)
                EventLinkedValueCash.putToLastCashValue(event, linkedValue)
                subscriber.notifyOfEvent(event)
            }
        } else {
            if (set == null) log("notifySubscribersWithValue(): set of subscribers is null")
            else log("notifySubscribersWithValue(): set of subscribers is empty")
            EventLinkedValueCash.putToLastCashValue(event, linkedValue)
        }
    }

    fun notifySubscribers(event: Event) {
        val set = SubscribersReferenceKeeper.getSubscribers(event)
        if(set != null && set.isNotEmpty()) {
            for(subscriber in set) {
                log("notifySubscribers(): notify $subscriber for $event")
                subscriber.notifyOfEvent(event)
            }
        } else {
            log("notifySubscribers(): set of subscribers is null")
        }
    }

    fun getEventLinkedValue(subscriber: Subscriber, event: Event) : Any? {
        log("getEventLinkedValue()")
        return if (EventLinkedValueCash.hasSubscribersValueInCash(subscriber, event))
            EventLinkedValueCash.getFromSubscribersCashValue(subscriber, event)
        else EventLinkedValueCash.getFromLastCashValue(event)
    }

    fun unsubscribe(subscriber: Subscriber, vararg events: Event) {
        log("unsubscribe()")
        for (event in events)  {
            SubscribersReferenceKeeper.unregisterSubscriber(subscriber, event)
            EventLinkedValueCash.onSubscriptionCancelled(
                subscriber, event,
                !SubscribersReferenceKeeper.hasSubscribersFor(event)
            )
        }
    }

    private fun log(message: String) {
        if (loggingEnabled) Log.d(ModelConst.LOG_TAG, "$className.$message")
    }
}