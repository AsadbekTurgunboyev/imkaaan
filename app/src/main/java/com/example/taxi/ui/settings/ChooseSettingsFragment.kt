package com.example.taxi.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentChooseSettingsBinding
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.utils.ConstantsUtils
import com.example.taxi.utils.ViewUtils
import org.koin.android.ext.android.inject

class ChooseSettingsFragment : Fragment() {

    private val preferenceManager: UserPreferenceManager by inject()
    private val viewBinding by lazy { FragmentChooseSettingsBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.fbnBackHome.setOnClickListener {
            findNavController().navigateUp()
        }

        val choose = arguments?.getInt("value")
        when(choose){
            ConstantsUtils.NAVIGATION -> {
                viewBinding.tvDetail.text = getString(R.string.navigation)
            }
            ConstantsUtils.THEME ->{
                viewBinding.tvDetail.text = getString(R.string.theme)

            }
        }
        val (list, oldOptions) = when (choose) {
            ConstantsUtils.NAVIGATION -> {
                ConstantsUtils.mapOptions to preferenceManager.getMapSettings()

            }


            ConstantsUtils.THEME ->{
                ConstantsUtils.getThemeOptions(requireContext()) to preferenceManager.getThemeOptions()
            }

            else -> {
                ConstantsUtils.mapOptions to preferenceManager.getMapSettings()
            }
        }

        val adapter = oldOptions?.let {
            SettingsAdapter(list, it) { packageName, valueName ->
                viewBinding.buttonConfrim.visibility =
                    if (packageName != oldOptions) View.VISIBLE else View.INVISIBLE
                viewBinding.buttonConfrim.setOnClickListener {
                    when (choose) {
                        ConstantsUtils.NAVIGATION -> preferenceManager.saveMapSettings(
                            packageName,
                            valueName
                        )
                        ConstantsUtils.THEME -> {
                            preferenceManager.setTheme(packageName)
                            ViewUtils.setTheme(preferenceManager.getTheme())
                        }
                    }

                    findNavController().navigateUp()
                }
            }
        }
        viewBinding.settingsRecyclerview.adapter = adapter

    }
}