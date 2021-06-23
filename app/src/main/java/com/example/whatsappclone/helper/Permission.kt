package com.example.whatsappclone.helper

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permission {

    companion object {

        fun validatePermission(activity: Activity, permissions: Array<String>, requestCode: Int): Boolean {

            if (Build.VERSION.SDK_INT >= 23) {

                val permissionList: ArrayList<String> = ArrayList()

                for (permission in permissions) {
                    val permitted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
                    if (!permitted) {
                        permissionList.add(permission)
                    }
                }

                if(permissionList.isEmpty()) return true

                val newPermissions: Array<String?> = arrayOfNulls(permissionList.size)
                permissionList.toArray(newPermissions)

                ActivityCompat.requestPermissions(
                    activity,
                    newPermissions,
                    requestCode
                )

            }

            return true
        }

    }

}