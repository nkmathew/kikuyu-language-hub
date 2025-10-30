package com.nkmathew.kikuyuflashcards.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nkmathew.kikuyuflashcards.R

/**
 * Standardized button creation utility for consistent button styling across the app
 */
object ButtonStyleHelper {

    // Standard button dimensions
    const val STANDARD_HEIGHT = 48  // 48dp
    const val LARGE_HEIGHT = 56     // 56dp
    const val SMALL_HEIGHT = 36     // 36dp

    // Standard padding values
    const val STANDARD_PADDING_HORIZONTAL = 24  // 24dp
    const val STANDARD_PADDING_VERTICAL = 12    // 12dp
    const val LARGE_PADDING_HORIZONTAL = 32     // 32dp
    const val LARGE_PADDING_VERTICAL = 16       // 16dp

    // Standard corner radius
    const val STANDARD_CORNER_RADIUS = 12f      // 12dp
    const val LARGE_CORNER_RADIUS = 16f         // 16dp
    const val SMALL_CORNER_RADIUS = 8f          // 8dp

    // Standard text sizes
    const val SMALL_TEXT_SIZE = 14f              // 14sp
    const val STANDARD_TEXT_SIZE = 16f          // 16sp
    const val LARGE_TEXT_SIZE = 18f             // 18sp

    // Standard margins
    const val STANDARD_MARGIN = 8               // 8dp
    const val LARGE_MARGIN = 12                 // 12dp

    /**
     * Create a standardized primary button with theme colors
     */
    fun createPrimaryButton(
        context: Context,
        text: String,
        isDarkTheme: Boolean,
        onClick: (View) -> Unit
    ): LinearLayout {
        return createButton(
            context = context,
            text = text,
            textSize = STANDARD_TEXT_SIZE,
            height = STANDARD_HEIGHT,
            cornerRadius = STANDARD_CORNER_RADIUS,
            backgroundColor = getThemeColor(context, "primary", isDarkTheme),
            textColor = Color.WHITE,
            paddingHorizontal = STANDARD_PADDING_HORIZONTAL,
            paddingVertical = STANDARD_PADDING_VERTICAL,
            onClick = onClick
        )
    }

    /**
     * Create a standardized secondary button with theme colors
     */
    fun createSecondaryButton(
        context: Context,
        text: String,
        isDarkTheme: Boolean,
        onClick: (View) -> Unit
    ): LinearLayout {
        return createButton(
            context = context,
            text = text,
            textSize = STANDARD_TEXT_SIZE,
            height = STANDARD_HEIGHT,
            cornerRadius = STANDARD_CORNER_RADIUS,
            backgroundColor = getThemeColor(context, "secondary", isDarkTheme),
            textColor = Color.WHITE,
            paddingHorizontal = STANDARD_PADDING_HORIZONTAL,
            paddingVertical = STANDARD_PADDING_VERTICAL,
            onClick = onClick
        )
    }

    /**
     * Create a standardized tertiary button with theme colors
     */
    fun createTertiaryButton(
        context: Context,
        text: String,
        isDarkTheme: Boolean,
        onClick: (View) -> Unit
    ): LinearLayout {
        return createButton(
            context = context,
            text = text,
            textSize = STANDARD_TEXT_SIZE,
            height = STANDARD_HEIGHT,
            cornerRadius = STANDARD_CORNER_RADIUS,
            backgroundColor = getThemeColor(context, "tertiary", isDarkTheme),
            textColor = Color.WHITE,
            paddingHorizontal = STANDARD_PADDING_HORIZONTAL,
            paddingVertical = STANDARD_PADDING_VERTICAL,
            onClick = onClick
        )
    }

    /**
     * Create a special accent button for important actions
     */
    fun createAccentButton(
        context: Context,
        text: String,
        isDarkTheme: Boolean,
        onClick: (View) -> Unit
    ): LinearLayout {
        return createButton(
            context = context,
            text = text,
            textSize = LARGE_TEXT_SIZE,
            height = LARGE_HEIGHT,
            cornerRadius = LARGE_CORNER_RADIUS,
            backgroundColor = if (isDarkTheme) Color.parseColor("#FF6F00") else Color.parseColor("#FF8F00"),
            textColor = Color.WHITE,
            paddingHorizontal = LARGE_PADDING_HORIZONTAL,
            paddingVertical = LARGE_PADDING_VERTICAL,
            elevation = 6f,
            onClick = onClick
        )
    }

    /**
     * Create a standardized button with custom parameters
     */
    private fun createButton(
        context: Context,
        text: String,
        textSize: Float,
        height: Int,
        cornerRadius: Float,
        backgroundColor: Int,
        textColor: Int,
        paddingHorizontal: Int,
        paddingVertical: Int,
        elevation: Float = 4f,
        onClick: (View) -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

            // Set layout parameters
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    height.toFloat(),
                    context.resources.displayMetrics
                ).toInt()
            ).apply {
                setMargins(0, STANDARD_MARGIN, 0, STANDARD_MARGIN)
            }

            // Create background
            background = createButtonBackground(backgroundColor, cornerRadius)
            this.elevation = elevation

            // Make clickable
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick(it) }

            // Add text
            val textView = TextView(context).apply {
                this.text = text
                this.textSize = textSize
                setTextColor(textColor)
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            addView(textView)
        }
    }

    /**
     * Create a button background with specified color and corner radius
     */
    private fun createButtonBackground(color: Int, cornerRadius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            this.cornerRadius = cornerRadius
        }
    }

    /**
     * Get theme color based on theme name and dark/light mode
     */
    private fun getThemeColor(context: Context, colorType: String, isDarkTheme: Boolean): Int {
        val colorRes = when (colorType) {
            "primary" -> if (isDarkTheme) R.color.md_theme_dark_primary else R.color.md_theme_light_primary
            "secondary" -> if (isDarkTheme) R.color.md_theme_dark_secondary else R.color.md_theme_light_secondary
            "tertiary" -> if (isDarkTheme) R.color.md_theme_dark_tertiary else R.color.md_theme_light_tertiary
            "surface" -> if (isDarkTheme) R.color.md_theme_dark_surface else R.color.md_theme_light_surface
            "surfaceContainer" -> if (isDarkTheme) R.color.md_theme_dark_surfaceContainer else R.color.md_theme_light_surfaceContainer
            "error" -> if (isDarkTheme) R.color.md_theme_dark_error else R.color.md_theme_light_error
            else -> R.color.md_theme_light_primary
        }

        return ContextCompat.getColor(context, colorRes)
    }

    /**
     * Get color resource ID for theme-based coloring
     */
    fun getThemeColorId(colorType: String, isDarkTheme: Boolean): Int {
        return when (colorType) {
            "primary" -> if (isDarkTheme) R.color.md_theme_dark_primary else R.color.md_theme_light_primary
            "secondary" -> if (isDarkTheme) R.color.md_theme_dark_secondary else R.color.md_theme_light_secondary
            "tertiary" -> if (isDarkTheme) R.color.md_theme_dark_tertiary else R.color.md_theme_light_tertiary
            "surface" -> if (isDarkTheme) R.color.md_theme_dark_surface else R.color.md_theme_light_surface
            "error" -> if (isDarkTheme) R.color.md_theme_dark_error else R.color.md_theme_light_error
            "success" -> R.color.success_green
            "warning" -> R.color.warning_orange
            "info" -> R.color.info_blue
            "quiz" -> R.color.quiz_purple
            else -> R.color.md_theme_light_primary
        }
    }

    /**
     * Create a progress indicator background for buttons
     */
    fun createProgressButtonBackground(
        context: Context,
        progress: Float,
        baseColorResId: Int,
        isDarkTheme: Boolean
    ): GradientDrawable {
        val baseColor = ContextCompat.getColor(context, baseColorResId)
        val endColor = ContextCompat.getColor(
            context,
            if (isDarkTheme) R.color.md_theme_dark_surfaceContainerHigh else R.color.md_theme_light_surfaceContainerHigh
        )

        return GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(baseColor, endColor)).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = STANDARD_CORNER_RADIUS
            gradientType = GradientDrawable.LINEAR_GRADIENT
            setGradientCenter(progress, 0.5f)
        }
    }
}