package com.plutoapps.huntrs

import android.app.Application
import com.plutoapps.huntrs.data.room.HuntsDatabase

class HuntrsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        db = HuntsDatabase.getDatabase(this)
    }

    companion object  {
        var db : HuntsDatabase? = null
    }
}