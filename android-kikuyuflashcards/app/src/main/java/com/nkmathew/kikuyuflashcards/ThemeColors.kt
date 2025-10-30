package com.nkmathew.kikuyuflashcards

import androidx.annotation.ColorRes

/**
 * ThemeColors - A central repository of color resources for consistent theming across activities
 *
 * This object provides color resource IDs for both light and dark themes, making it easier to maintain
 * consistent styling across the application. By using these constants instead of hardcoded color values,
 * theme changes can be applied more consistently and with less effort.
 */
object ThemeColors {

    // Primary theme colors
    @ColorRes val primaryColor = R.color.md_theme_dark_primary
    @ColorRes val onPrimaryColor = R.color.md_theme_dark_onPrimary
    @ColorRes val primaryContainerColor = R.color.md_theme_dark_primaryContainer
    @ColorRes val onPrimaryContainerColor = R.color.md_theme_dark_onPrimaryContainer

    // Secondary theme colors
    @ColorRes val secondaryColor = R.color.md_theme_dark_secondary
    @ColorRes val onSecondaryColor = R.color.md_theme_dark_onSecondary
    @ColorRes val secondaryContainerColor = R.color.md_theme_dark_secondaryContainer
    @ColorRes val onSecondaryContainerColor = R.color.md_theme_dark_onSecondaryContainer

    // Tertiary theme colors
    @ColorRes val tertiaryColor = R.color.md_theme_dark_tertiary
    @ColorRes val onTertiaryColor = R.color.md_theme_dark_onTertiary
    @ColorRes val tertiaryContainerColor = R.color.md_theme_dark_tertiaryContainer
    @ColorRes val onTertiaryContainerColor = R.color.md_theme_dark_onTertiaryContainer

    // Error theme colors
    @ColorRes val errorColor = R.color.md_theme_dark_error
    @ColorRes val onErrorColor = R.color.md_theme_dark_onError
    @ColorRes val errorContainerColor = R.color.md_theme_dark_errorContainer
    @ColorRes val onErrorContainerColor = R.color.md_theme_dark_onErrorContainer

    // Surface theme colors
    @ColorRes val surfaceColor = R.color.md_theme_dark_surface
    @ColorRes val onSurfaceColor = R.color.md_theme_dark_onSurface
    @ColorRes val surfaceVariantColor = R.color.md_theme_dark_surfaceVariant
    @ColorRes val onSurfaceVariantColor = R.color.md_theme_dark_onSurfaceVariant
    @ColorRes val surfaceContainerLowColor = R.color.md_theme_dark_surfaceContainerLow
    @ColorRes val surfaceContainerHighColor = R.color.md_theme_dark_surfaceContainerHigh

    // Background theme colors
    @ColorRes val backgroundColor = R.color.md_theme_dark_background
    @ColorRes val onBackgroundColor = R.color.md_theme_dark_onBackground

    // Outline theme colors
    @ColorRes val outlineColor = R.color.md_theme_dark_outline

    // Status and feedback colors
    @ColorRes val successColor = R.color.success_green
    @ColorRes val warningColor = R.color.warning_orange
    @ColorRes val infoColor = R.color.info_blue

    // Common app-specific colors
    @ColorRes val textPrimaryColor = R.color.md_theme_dark_onSurface
    @ColorRes val textSecondaryColor = R.color.text_secondary

    // Button styling
    @ColorRes val buttonPrimaryBgColor = R.color.md_theme_dark_primary
    @ColorRes val buttonPrimaryTextColor = R.color.md_theme_dark_onPrimary
    @ColorRes val buttonSecondaryBgColor = R.color.md_theme_dark_secondary
    @ColorRes val buttonSecondaryTextColor = R.color.md_theme_dark_onSecondary
    @ColorRes val buttonTertiaryBgColor = R.color.md_theme_dark_tertiary
    @ColorRes val buttonTertiaryTextColor = R.color.md_theme_dark_onTertiary

    // Card styling
    @ColorRes val cardBgColor = R.color.md_theme_dark_surface
    @ColorRes val cardStrokeColor = R.color.md_theme_dark_primary
    @ColorRes val cardTextColor = R.color.md_theme_dark_onSurface
}