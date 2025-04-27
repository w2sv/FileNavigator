package com.w2sv.test

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

/**
 * May be used to receive [Timber] logs during unit testing.
 */
class TimberTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                Timber.plant(TestDebugTree)
                try {
                    base.evaluate()
                } finally {
                    Timber.uprootAll()
                }
            }
        }
}

private object TestDebugTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        println(
            buildString {
                tag?.let { append("$tag: ") }
                append(message)
            }
        )
        t?.printStackTrace(System.out)
    }
}
