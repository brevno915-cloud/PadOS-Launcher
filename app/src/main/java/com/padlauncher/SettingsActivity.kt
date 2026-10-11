package com.padlauncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var wallpaperButton: Button
    private lateinit var themeDark: RadioButton
    private lateinit var themeGlass: RadioButton
    private lateinit var themeLight: RadioButton
    private lateinit var brightnessSlider: SeekBar
    private lateinit var brightnessValue: TextView
    private lateinit var systemSettingsButton: Button
    private lateinit var displaySettingsButton: Button
    private lateinit var errorText: TextView

    private val prefs by lazy { LauncherPreferences.getSharedPreferences(this) }

    private val wallpaperPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            showError(getString(R.string.missing_permission_text))
            return@registerForActivityResult
        }
        LauncherPreferences.setWallpaperUri(prefs, uri.toString())
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        wallpaperButton = findViewById(R.id.selectWallpaperButton)
        themeDark = findViewById(R.id.themeDark)
        themeGlass = findViewById(R.id.themeGlass)
        themeLight = findViewById(R.id.themeLight)
        brightnessSlider = findViewById(R.id.brightnessSlider)
        brightnessValue = findViewById(R.id.brightnessValue)
        systemSettingsButton = findViewById(R.id.systemSettingsButton)
        displaySettingsButton = findViewById(R.id.displaySettingsButton)
        errorText = findViewById(R.id.errorText)

        val theme = LauncherPreferences.getTheme(prefs)
        when (theme) {
            "dark" -> themeDark.isChecked = true
            "glass" -> themeGlass.isChecked = true
            else -> themeLight.isChecked = true
        }

        brightnessSlider.progress = LauncherPreferences.getBrightness(prefs)
        updateBrightnessLabel(brightnessSlider.progress)

        wallpaperButton.setOnClickListener {
            try {
                wallpaperPicker.launch("image/*")
            } catch (e: Exception) {
                showError(getString(R.string.missing_permission_text))
            }
        }

        themeDark.setOnClickListener { LauncherPreferences.setTheme(prefs, "dark") }
        themeGlass.setOnClickListener { LauncherPreferences.setTheme(prefs, "glass") }
        themeLight.setOnClickListener { LauncherPreferences.setTheme(prefs, "light") }

        brightnessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateBrightnessLabel(progress)
                LauncherPreferences.setBrightness(prefs, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        systemSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }

        displaySettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        }
    }

    private fun updateBrightnessLabel(value: Int) {
        brightnessValue.text = "$value%"
    }

    private fun showError(message: String) {
        errorText.visibility = android.view.View.VISIBLE
        errorText.text = message
    }
}
