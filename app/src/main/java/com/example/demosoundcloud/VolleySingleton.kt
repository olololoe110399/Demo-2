package com.example.demosoundcloud

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(private val mCtx: Context) {
    private var mRequestQueue: RequestQueue?
    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue =
                    Volley.newRequestQueue(mCtx.applicationContext)
            }
            return mRequestQueue
        }

    companion object {
        private var mInstance: VolleySingleton? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): VolleySingleton? {
            if (mInstance == null) {
                mInstance = VolleySingleton(context)
            }
            return mInstance
        }
    }

    init {
        mRequestQueue = requestQueue
    }
}