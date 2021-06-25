package com.ht117.sandsara

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Reporter
 * Setup reporter with crashlytic
 */
class Reporter: Timber.Tree() {

    private val logger = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logger.log("$message\n${t?.message}")
    }
}