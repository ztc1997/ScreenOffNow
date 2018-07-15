package com.ztc1997.screenoffnow

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.ztc1997.anycall.Anycall
import org.jetbrains.anko.toast

class SONAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "AccessibilityServices"
        private const val PACKAGE_NAME_SYSTEM_UI = "com.android.systemui"
    }

    private val descNotificationShade by lazy {
        val res = packageManager.getResourcesForApplication(PACKAGE_NAME_SYSTEM_UI)
        val id = res.getIdentifier("accessibility_desc_notification_shade", "string", PACKAGE_NAME_SYSTEM_UI)
        res.getString(id)
    }

    private val anycall by lazy { Anycall(this) }
    private var lastTap = 0L

    override fun onAccessibilityEvent(ev: AccessibilityEvent) {
        if (ev.packageName != PACKAGE_NAME_SYSTEM_UI || ev.className != "android.widget.FrameLayout") return
        if (ev.text.size > 0 && ev.text[0] == descNotificationShade)
            if (ev.eventTime - lastTap < 150)
                onDoubleTap()
            else
                lastTap = ev.eventTime

        //Log.d(TAG, ev.toString())
    }

    override fun onInterrupt() {

    }

    private fun onDoubleTap() {
        anycall.startShell {
            if (it)
                anycall.callMethod("android.os.IPowerManager", Context.POWER_SERVICE,
                        "goToSleep", SystemClock.uptimeMillis(),
                        Anycall.CallMethodResultListener { resultCode, reply ->
                            if (resultCode != 0) toast(R.string.toast_root_access_failed)
                            try {
                                reply?.readException()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            true
                        })
            else
                toast(R.string.toast_root_access_failed)
        }
    }
}