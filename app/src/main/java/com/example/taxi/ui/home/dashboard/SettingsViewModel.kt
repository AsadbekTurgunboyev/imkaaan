package com.example.taxi.ui.home.dashboard

import androidx.lifecycle.ViewModel
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase

class SettingsViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel()