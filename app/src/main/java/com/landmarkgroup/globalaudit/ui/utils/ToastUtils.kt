package com.landmarkgroup.globalaudit.ui.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast

object ToastUtils {
    /**
     * Show a colored toast.
     * - Green background for success
     * - Red background for failure
     * - White text
     * - Bottom position with slight offset
     */
    fun show(context: Context, message: String, isSuccess: Boolean, long: Boolean = false) {
        // Use a custom view for consistent styling on Android 12+ where default toasts ignore background changes
        val density = context.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        
        val bgColor = if (isSuccess) Color.parseColor("#388E3C") else Color.parseColor("#D32F2F")
        
        val textView = TextView(context).apply {
            text = message
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(bgColor)
            }
        }
        
        val toast = Toast(context.applicationContext).apply {
            duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            view = textView
            setGravity(Gravity.BOTTOM, 0, dp(120))
        }
        toast.show()
    }
}
