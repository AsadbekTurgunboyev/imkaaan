package com.example.taxi.domain.privacyPolicy

import com.example.taxi.domain.preference.UserPreferenceManager

class PrivacyPolicyService(private val userPreferenceManager: UserPreferenceManager) {

    fun isPolicyAccepted() = userPreferenceManager.getPrivacyPolicyReadStatus()

    fun onAcceptPolicy() = userPreferenceManager.setPrivacyPolicyReadStatus()
}