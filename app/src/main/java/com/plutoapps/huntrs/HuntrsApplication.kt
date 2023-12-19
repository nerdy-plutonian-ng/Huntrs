package com.plutoapps.huntrs

import android.app.Application

import com.plutoapps.huntrs.data.room.HuntsDatabase

class HuntrsApplication : Application() {

    companion object  {
        var db : HuntsDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        db = HuntsDatabase.getDatabase(this)
    }

}