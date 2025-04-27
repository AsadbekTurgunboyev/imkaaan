package com.example.taxi.ui.home.menu.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentChoosingLanguageBinding
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.utils.ButtonInfo
import com.example.taxi.utils.setButtonState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject


class ChoosingLanguageFragment : Fragment() {
    private lateinit var viewBinding: FragmentChoosingLanguageBinding
    private val userPrefManager: UserPreferenceManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentChoosingLanguageBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        with(viewBinding) {


            fbnBackHome.setOnClickListener {
                val navController = findNavController()
                navController.navigateUp()
            }


            val buttonInfos = listOf(
                ButtonInfo(
                    button = uzbekButton,
                    cardView = bgUzb,
                    checkView = checkUzb,
                    activeColor = R.color.yellow,
                    inactiveColor = R.color.in_active
                ),
                ButtonInfo(
                    button = rusButton,
                    cardView = bgRus,
                    checkView = checkRus,
                    activeColor = R.color.yellow,
                    inactiveColor = R.color.in_active
                ),
                ButtonInfo(
                    button = krillButton,
                    cardView = bgKrill,
                    checkView = checkKrill,
                    activeColor = R.color.yellow,
                    inactiveColor = R.color.in_active
                )
            )

            when (userPrefManager.getLanguage()) {
                UserPreferenceManager.Language.RUSSIAN -> {
                    setButtonState(buttonInfo = buttonInfos[2], false)
                    setButtonState(buttonInfo = buttonInfos[1], true)
                    setButtonState(buttonInfo = buttonInfos[0], false)

                }
                UserPreferenceManager.Language.UZBEK -> {
                    setButtonState(buttonInfo = buttonInfos[0], true)
                    setButtonState(buttonInfo = buttonInfos[1], false)
                    setButtonState(buttonInfo = buttonInfos[2], false)
                }
                UserPreferenceManager.Language.KRILL -> {
                    setButtonState(buttonInfo = buttonInfos[1], false)
                    setButtonState(buttonInfo = buttonInfos[0], false)
                    setButtonState(buttonInfo = buttonInfos[2], true)
                }
            }
            fun setButtonsClickListener() {
                buttonInfos.forEachIndexed { index, buttonInfo ->
                    buttonInfo.button.setOnClickListener {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                buttonInfos.forEachIndexed { innerIndex, innerButtonInfo ->
                                    setButtonState(innerButtonInfo, innerIndex == index)
                                }
                            }
                        }
                        when (index) {
                            0 -> updateLanguage(UserPreferenceManager.Language.UZBEK)
                            1 -> updateLanguage(UserPreferenceManager.Language.RUSSIAN)
                            2 -> updateLanguage(UserPreferenceManager.Language.KRILL)
                        }
                        activity?.recreate()
                    }

                }
            }
            setButtonsClickListener()
        }

    }

//    private fun navigateToDashboardFragment() {
//        activity?.let { currentActivity ->
//            currentActivity.recreate()
//        }
//
//    }

    private fun updateLanguage(language: UserPreferenceManager.Language) {
        userPrefManager.setLanguage(language)
    }
}