package hu.krisz768.bettertuke

import android.app.Application
import com.google.android.material.color.DynamicColors

class BetterTukeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this) //Hello World
    }
}