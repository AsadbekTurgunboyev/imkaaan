package com.example.taxi.ui.home.qrcode

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentShareQRBinding
import com.example.taxi.domain.preference.UserPreferenceManager
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.Color
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogo
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import org.koin.android.ext.android.inject

class ShareQRFragment : Fragment() {


    lateinit var viewBinding: FragmentShareQRBinding
    private val userPreferenceManager: UserPreferenceManager by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentShareQRBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.fbnBackHome.setOnClickListener {
            val nav = findNavController()
            nav.navigateUp()
        }


        val options = QrVectorOptions.Builder()
            .setPadding(.3f)
            .setLogo(
                QrVectorLogo(
                    drawable = ContextCompat
                        .getDrawable(requireContext(), R.drawable.ic_avatar),
                    size = .25f,
                    padding = QrVectorLogoPadding.Natural(.2f),
                    shape = QrVectorLogoShape
                        .Circle
                )
            )

            .setColors(
                QrVectorColors(
                    dark = QrVectorColor
                        .Solid(Color(0xff8CCEA1)),
                    ball = QrVectorColor.Solid(
                        ContextCompat.getColor(requireContext(), R.color.soft_black)
                    ),
                    frame = QrVectorColor.LinearGradient(
                        colors = listOf(
                            0f to Color.YELLOW,
                            1f to ContextCompat.getColor(requireContext(), R.color.tblue),
                        ),
                        orientation = QrVectorColor.LinearGradient
                            .Orientation.LeftDiagonal
                    )
                )
            )
            .setShapes(
                QrVectorShapes(
                    darkPixel = QrVectorPixelShape
                        .RoundCorners(.5f),
                    ball = QrVectorBallShape
                        .RoundCorners(.25f),
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f),
                )
            )
            .build()
        val driverId = userPreferenceManager.getDriverID()
        val data = QrData.Url("https://play.google.com/store/apps/details?id=uz.insoft.win_taxi&referrer=$driverId")
//        val data = QrData.Url("$driverId")
        val drawable : Drawable = QrCodeDrawable(data, options)
        viewBinding.imageQR.setImageDrawable(drawable)
    }


}