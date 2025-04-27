package com.example.taxi

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.example.taxi.domain.preference.UserPreferenceManager
import org.junit.Test
import org.mockito.Mockito.*

class PreferenceManagerTest {

    @Test
    fun testSetAndGetDriverStatus() {
        val sharedPreferences = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)

        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(sharedPreferences.getString(anyString(), anyString())).thenReturn(
            UserPreferenceManager.DriverStatus.ACCEPTED.name)
        val context = ApplicationProvider.getApplicationContext<Context>()
//        val userPreferenceManager = UserPreferenceManager(context, sharedPreferences)

//        val status = userPreferenceManager.setAndGetDriverStatus(UserPreferenceManager.DriverStatus.ACCEPTED)

        verify(editor).putString(anyString(), eq(UserPreferenceManager.DriverStatus.ACCEPTED.name))
        verify(editor).apply()
        verify(sharedPreferences).getString(anyString(), anyString())

//        assertEquals(UserPreferenceManager.DriverStatus.ACCEPTED, status)
    }
}