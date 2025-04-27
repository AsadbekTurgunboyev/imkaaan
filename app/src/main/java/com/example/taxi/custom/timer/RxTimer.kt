package com.example.taxi.custom.timer

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RxTimer private constructor(builder: Builder) {
    private val period: Long
    private val initialDelay: Long
    private val unit: TimeUnit
    private val onComplete: Action?
    private val onEmit: Consumer<Long>?
    private val onError: Consumer<Throwable>?
    private var disposable: Disposable? = null
    private var isStarted = false

    init {
        period = builder.period
        initialDelay = builder.initialDelay
        unit = builder.unit
        onComplete = builder.onComplete
        onEmit = builder.onEmit
        onError = builder.onError
    }

    fun start(): RxTimer {
        if (disposable == null || disposable!!.isDisposed) {
            disposable = Observable.interval(initialDelay, period, unit)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { count ->
                        if (onEmit != null) {
                            onEmit.accept(count + 1)
                        }
                    },
                    { throwable ->
                        if (onError != null) {
                            onError.accept(throwable)
                        }
                    },
                    {
                        if (onComplete != null) {
                            onComplete.run()
                        }
                    }
                )
            isStarted = true
        }
        return this
    }

    fun stop() {
        if (disposable != null) {
            disposable!!.dispose()
            isStarted = false
        }
    }

    fun isStarted(): Boolean {
        return isStarted
    }

    class Builder {
        var period: Long = 1
        var initialDelay: Long = 0
        var unit: TimeUnit = TimeUnit.SECONDS
        var onComplete: Action? = null
        var onEmit: Consumer<Long>? = null
        var onError: Consumer<Throwable>? = null

        fun period(period: Long): Builder {
            this.period = period
            return this
        }

        fun initialDelay(initialDelay: Long): Builder {
            this.initialDelay = initialDelay
            return this
        }

        fun unit(unit: TimeUnit): Builder {
            this.unit = unit
            return this
        }

        fun onComplete(onComplete: Action): Builder {
            this.onComplete = onComplete
            return this
        }

        fun onEmit(onEmit: Consumer<Long>): Builder {
            this.onEmit = onEmit
            return this
        }

        fun onError(onError: Consumer<Throwable>): Builder {
            this.onError = onError
            return this
        }

        fun build(): RxTimer {
            return RxTimer(this)
        }
    }
}