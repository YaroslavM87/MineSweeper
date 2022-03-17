package com.yaroslavm87.minesweeper.model

import com.yaroslavm87.minesweeper.eventBus.SubscriberKeeper
import com.yaroslavm87.minesweeper.eventBus.Subscriber
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PublisherTest {

    private enum class TestEvent : SubscriberKeeper.Event {
        EVENT1,
        EVENT2,
    }

    class Subscriber1 : Subscriber {
        var event: SubscriberKeeper.Event? = null
        var notificationCounter = 0
        override fun receiveUpdate(event: SubscriberKeeper.Event, observableValue: Any) {
            notificationCounter++
        }

        override fun receiveUpdate(event: SubscriberKeeper.Event) {
            notificationCounter++
        }
    }

    private val publisher = SubscriberKeeper
    private lateinit var subscriber1: Subscriber1


    @Before
    fun initTestData() {
        subscriber1 = Subscriber1()

    }

    @After
    fun clearTestData() {
        subscriber1.notificationCounter = 0
        subscriber1.event = null
        publisher.cancelSubscription(subscriber1, TestEvent.EVENT1)
        publisher.cancelSubscription(subscriber1, TestEvent.EVENT2)
    }

    @Test
    fun subscribeForEvent_ifSubscribesForEvent1_shouldBeInSetOfSubscribersForEvent1() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1)
        Assert.assertTrue(publisher.isSubscribedFor(subscriber1, TestEvent.EVENT1))
    }

    @Test
    fun subscribeForEvent_ifSubscribesForEvent1_shouldNotBeInSetOfSubscribersForEvent2() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1)
        Assert.assertFalse(publisher.isSubscribedFor(subscriber1, TestEvent.EVENT2))
    }

    @Test
    fun subscribeForEvent_ifSubscribesForEvent1_publisherShouldHaveSetOfSubscribersForEvent1() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1)
        Assert.assertTrue(publisher.hasSubscribersFor(TestEvent.EVENT1))
    }

    @Test
    fun cancelSubscription_ifUnsubscribesFromEvent1_shouldNotBeInSetOfSubscribersForEvent1() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1)
        publisher.cancelSubscription(subscriber1, TestEvent.EVENT1)
        Assert.assertFalse(publisher.isSubscribedFor(subscriber1, TestEvent.EVENT1))
    }

    @Test
    fun cancelSubscription_ifUnsubscribesFromEvent1_shouldBeInSetOfSubscribersForEvent2() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1, TestEvent.EVENT2)
        publisher.cancelSubscription(subscriber1, TestEvent.EVENT1)
        Assert.assertTrue(publisher.isSubscribedFor(subscriber1, TestEvent.EVENT2))
    }

    @Test
    fun notifyEventHappened_ifSubscribedForEvent1AndEvent2_shouldBeNotified2Times() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1, TestEvent.EVENT2)
        publisher.notifyEventHappened(TestEvent.EVENT1)
        publisher.notifyEventHappened(TestEvent.EVENT2)
        Assert.assertEquals(2, subscriber1.notificationCounter)
    }

    @Test
    fun notifyEventHappened_ifUnsubscribedFromEvent1_shouldBeNotified1Time() {
        publisher.subscribeForEvent(subscriber1, TestEvent.EVENT1, TestEvent.EVENT2)
        publisher.cancelSubscription(subscriber1, TestEvent.EVENT1)
        publisher.notifyEventHappened(TestEvent.EVENT1)
        publisher.notifyEventHappened(TestEvent.EVENT2)
        Assert.assertEquals(1, subscriber1.notificationCounter)
    }
}