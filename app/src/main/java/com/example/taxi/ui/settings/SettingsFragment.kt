package com.example.taxi.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentSettingsBinding
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.utils.ConstantsUtils
import org.koin.android.ext.android.inject

class SettingsFragment : Fragment() {


    lateinit var viewBinding: FragmentSettingsBinding
    private val userPreferenceManager: UserPreferenceManager by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.languageTv.text = getLanguageText()
        viewBinding.selectThemeTv.text = getThemeText()
        viewBinding.selectMapNameTv.text = getMapText()
        viewBinding.selectAppearanceTv.text = getAppearanceText()

        setupButton(viewBinding.buttonChooseNavigation, ConstantsUtils.NAVIGATION)
        setupButton(viewBinding.buttonChooseTheme, ConstantsUtils.THEME)

        viewBinding.fbnBackHome.setOnClickListener {
            findNavController().navigateUp()
        }

        viewBinding.buttonLanguage.setOnClickListener {
            findNavController().navigate(R.id.choosingLanguageFragment)
        }
    }

    private fun setupButton(button: View, value: Int) {
        button.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("value", value)
            }
            findNavController().navigate(R.id.chooseSettingsFragment, bundle)
        }
    }

    private fun getAppearanceText(): String {
        return when(userPreferenceManager.getOrderAppearance()){
            UserPreferenceManager.OrderAppearance.SMALL -> getString(R.string.small)
            UserPreferenceManager.OrderAppearance.LARGE -> getString(R.string.large)
        }
    }

    private fun getLanguageText(): String {
        return when (userPreferenceManager.getLanguage()) {
            UserPreferenceManager.Language.RUSSIAN -> "Русский язык"
            UserPreferenceManager.Language.UZBEK -> "O'zbek tili"
            UserPreferenceManager.Language.KRILL -> "Узбек тили"
        }
    }

    private fun getMapText(): String{
        return userPreferenceManager.getMapName()
    }

    private fun getThemeText(): String {
        return when(userPreferenceManager.getTheme()){
            UserPreferenceManager.ThemeStyle.AUTO -> getString(R.string.auto)
            UserPreferenceManager.ThemeStyle.DARK -> getString(R.string.night)
            UserPreferenceManager.ThemeStyle.LIGHT -> getString(R.string.day)
        }
    }

}




