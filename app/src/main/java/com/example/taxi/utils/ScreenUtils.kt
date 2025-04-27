package com.example.taxi.utils

import android.content.res.Resources

object ScreenUtils {

        val width: Int
            get() = Resources.getSystem().displayMetrics.widthPixels

        val height: Int
            get() = Resources.getSystem().displayMetrics.heightPixels

}