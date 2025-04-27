package com.example.taxi.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.taxi.domain.preference.UserPreferenceManager
import org.koin.core.context.GlobalContext
import java.util.*

object ViewUtils {

    private val userPreferenceManager by lazy {
        GlobalContext.get().get<UserPreferenceManager>()
    }
    fun setTheme(themeStyle: UserPreferenceManager.ThemeStyle) {
        when (themeStyle) {
            UserPreferenceManager.ThemeStyle.AUTO -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
            UserPreferenceManager.ThemeStyle.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            UserPreferenceManager.ThemeStyle.DARK -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    fun setLanguageForService(baseContext: Context) {
        val language = userPreferenceManager.getLanguage()
        // Create a new locale object with the user's selected language
        val locale = Locale(language.code)

        // Set the locale for the app
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

    }

    fun setLanguage(context: Context, language: UserPreferenceManager.Language): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}