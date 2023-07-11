package ru.otus.flow

import android.app.Application
import ru.otus.flow.di.Injector
import ru.otus.flow.di.InjectorProvider

class FlowApplication : Application(), InjectorProvider {

    private lateinit var _injector: Injector

    override val injector: Injector
        get() = _injector

    override fun onCreate() {
        super.onCreate()

        _injector = Injector(this)
    }
}
