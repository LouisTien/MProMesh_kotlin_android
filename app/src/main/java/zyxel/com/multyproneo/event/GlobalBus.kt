package zyxel.com.multyproneo.event

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by LouisTien on 2019/5/24.
 */
// Use object so we have a singleton instance
object GlobalBus
{

    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any)
    {
        publisher.onNext(event)
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}