package com.xtremepixel.memorygame.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun isPersissionGranted(context:Context,permission:String):Boolean{

    return ContextCompat.checkSelfPermission(context,permission)==PackageManager.PERMISSION_GRANTED
}

fun requestPermision(activity:Activity?,permission: String,requestCode:Int){
    ActivityCompat.requestPermissions(activity!!, arrayOf(permission),requestCode)
}