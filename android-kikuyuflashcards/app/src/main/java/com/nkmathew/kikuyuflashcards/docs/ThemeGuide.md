# Kikuyu Flash Cards Theme Guide

## Overview

This guide explains how to use the `ThemeColors` class to maintain consistent theming across all activities in the Kikuyu Flash Cards app.

## Why Use ThemeColors?

- **Consistency**: Ensures the same colors are used across all activities
- **Maintainability**: Makes it easier to update the theme in one place
- **Readability**: Makes code more self-documenting with semantic color names
- **Dark Mode Support**: Better support for theme transitions

## How to Use ThemeColors

### Basic Usage

```kotlin
// Instead of this:
setTextColor(ContextCompat.getColor(this, R.color.md_theme_dark_onSurface))

// Use this:
setTextColor(ContextCompat.getColor(this, ThemeColors.textPrimaryColor))
```

### Styling UI Components

#### Text Views

```kotlin
textView.apply {
    setTextColor(ContextCompat.getColor(context, ThemeColors.textPrimaryColor))
    // For secondary text
    setTextColor(ContextCompat.getColor(context, ThemeColors.textSecondaryColor))
}
```

#### Buttons

```kotlin
button.apply {
    setTextColor(ContextCompat.getColor(context, ThemeColors.buttonPrimaryTextColor))
    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 24f
        setColor(ContextCompat.getColor(context, ThemeColors.buttonPrimaryBgColor))
    }
}
```

#### Cards and Containers

```kotlin
cardContainer.apply {
    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 16f
        setColor(ContextCompat.getColor(context, ThemeColors.cardBgColor))
        setStroke(2, ContextCompat.getColor(context, ThemeColors.cardStrokeColor))
    }
}
```

## Color Semantics

### Primary Colors
- `primaryColor`: Main brand color
- `onPrimaryColor`: Text/icons that appear on primary color
- `primaryContainerColor`: Container elements using primary color
- `onPrimaryContainerColor`: Text/icons that appear on primary container

### Secondary Colors
- `secondaryColor`: Secondary brand color for less emphasis
- `onSecondaryColor`: Text/icons that appear on secondary color
- `secondaryContainerColor`: Container elements using secondary color
- `onSecondaryContainerColor`: Text/icons that appear on secondary container

### Status Colors
- `successColor`: Used for success states and confirmations
- `warningColor`: Used for warnings and cautions
- `infoColor`: Used for informational messages
- `errorColor`: Used for error states

## Common Use Cases

### Activity Headers

```kotlin
titleText = TextView(this).apply {
    text = "Activity Title"
    textSize = 24f
    setTypeface(null, android.graphics.Typeface.BOLD)
    setTextColor(ContextCompat.getColor(this@YourActivity, ThemeColors.primaryColor))
}
```

### Interactive Elements

```kotlin
// Standard buttons
primaryButton.background = createButtonBackground(ThemeColors.buttonPrimaryBgColor)
secondaryButton.background = createButtonBackground(ThemeColors.buttonSecondaryBgColor)

// Interactive word items
wordView.background = createWordBackground(ThemeColors.primaryContainerColor)
wordView.setTextColor(ContextCompat.getColor(context, ThemeColors.onPrimaryContainerColor))
```

### Feedback Messages

```kotlin
// Success message
successText.setTextColor(ContextCompat.getColor(this, ThemeColors.successColor))

// Error message
errorText.setTextColor(ContextCompat.getColor(this, ThemeColors.errorColor))
```

## Theme Extensions (Future Improvement)

For a future version, consider creating Kotlin extension functions:

```kotlin
fun Context.getThemeColor(@ColorRes colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}

fun TextView.setThemeTextColor(@ColorRes colorResId: Int) {
    setTextColor(context.getThemeColor(colorResId))
}
```

This would simplify usage to:

```kotlin
textView.setThemeTextColor(ThemeColors.textPrimaryColor)
```

## Conclusion

Using the ThemeColors class will ensure visual consistency across the app and make future theme updates much easier. Always refer to this class when setting colors in your UI components.