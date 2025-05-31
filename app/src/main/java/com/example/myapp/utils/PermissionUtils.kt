package com.example.myapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.app.Activity
import androidx.core.app.ActivityCompat

object PermissionUtils {
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Placeholder for requesting permissions if needed in the future
    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

    // Example of how to request multiple permissions
    // fun requestMultiplePermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
    //     ActivityCompat.requestPermissions(activity, permissions, requestCode)
    // }

    // Constants for permission strings (good practice)
    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    const val WRITE_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    // Add more as needed, e.g., Manifest.permission.RECORD_AUDIO
}
