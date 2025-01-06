package com.w2sv.common.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class GlobalScope(val appDispatcher: AppDispatcher)

enum class AppDispatcher {
    Default,
    IO
}
