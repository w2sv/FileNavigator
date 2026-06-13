package com.w2sv.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationDefaultScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationIoScope
