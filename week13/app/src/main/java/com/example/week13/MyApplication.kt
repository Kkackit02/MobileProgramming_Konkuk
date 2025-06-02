package com.example.week13

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    var isForeGround = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(
        activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        isForeGround = true
    }

    override fun onActivityPaused(activity: Activity) {
        isForeGround = false
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(
        activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
    }

}