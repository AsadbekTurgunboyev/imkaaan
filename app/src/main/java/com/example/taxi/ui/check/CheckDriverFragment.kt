package com.example.taxi.ui.check

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentChechkDriverBinding
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.github.dhaval2404.imagepicker.ImagePicker
import com.tapadoo.alerter.Alerter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable


class CheckDriverFragment : Fragment() {

    lateinit var viewBinding: FragmentChechkDriverBinding
    private val checkViewModel: CheckViewModel by viewModel()
    private val imageUris = mutableMapOf<String, Uri>()
    lateinit var dialog: Dialog
    var exportNumber = 0
    private var currentImageKey: String? = null
    private val startForUploadImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleImageResult(result)
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentChechkDriverBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            imageUris.putAll(savedInstanceState.getSerializable("imageUris") as MutableMap<String, Uri>)
            currentImageKey = savedInstanceState.getString("currentImageKey")
            // Restore the image views
            imageUris.forEach { (key, uri) ->
                updateImageView(key, uri)
            }
        }
        val exportNumber = arguments?.getInt("export_photo", -1) ?: -1
        if (exportNumber == 1) viewBinding.buttonBack.visibility =
            View.VISIBLE else viewBinding.buttonBack.visibility = View.INVISIBLE



        dialog = DialogUtils.checkSuccessDialog(requireContext()) {
            val navController = findNavController()

//            navController.popBackStack(navController.graph.startDestinationId, false)
            navController.navigate(
                R.id.dashboardFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(navController.graph.startDestinationId, false)
                    .setLaunchSingleTop(true)
                    .build()
            )
        }
        setupUI()

        checkViewModel.selfieResponse.observe(viewLifecycleOwner) {
            updateUi(it)
        }
        viewBinding.buttonConfirm.setOnClickListener {
            checkViewModel.fillChecks(imageUris)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("imageUris", imageUris as Serializable)
        outState.putString("currentImageKey", currentImageKey)
    }

    private fun updateUi(resource: Resource<MainResponse<Any>>?) {
        when (resource?.state) {
            ResourceState.ERROR -> {
                Alerter.clearCurrent(activity)
                DialogUtils.createChangeDialog(
                    activity = requireActivity(),
                    title = "Xatolik",
                    message = "${resource.message}",
                    color = R.color.tred
                )
            }

            ResourceState.SUCCESS -> {
                Alerter.clearCurrent(activity)
                dialog.show()
            }

            ResourceState.LOADING -> {
                activity?.let { it1 ->
                    Alerter.create(it1).setTitle("Yuklanmoqda...")
                        .setDuration(60000)
                        .setText("Siz belgilagan rasmlar yuklanmoqda, bu biroz vaqtni olishi mumkin")
                        .enableProgress(true).setProgressColorRes(R.color.primaryColor).show()
                }
            }

            else -> {}
        }
    }

    private fun setupUI() {
        with(viewBinding) {
            buttonBack.setOnClickListener { findNavController().navigateUp() }
            uploadFront.setOnClickListener { launchImagePicker("img_front") }
            uploadBack.setOnClickListener { launchImagePicker("img_back") }
            uploadLeft.setOnClickListener { launchImagePicker("img_left") }
            uploadRight.setOnClickListener { launchImagePicker("img_right") }
            uploadFrontChair.setOnClickListener { launchImagePicker("img_front_chair") }
            uploadBackChair.setOnClickListener { launchImagePicker("img_back_chair") }
            uploadNumber.setOnClickListener { launchImagePicker("img_number") }
            uploadLicense.setOnClickListener { launchImagePicker("img_license") }
        }
    }

    private fun launchImagePicker(imageKey: String) {
        currentImageKey = imageKey
        ImagePicker.with(this)
            .cameraOnly()
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                startForUploadImage.launch(intent)
            }
    }

    private fun handleImageResult(result: ActivityResult) {
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.data?.let { uri ->
                    currentImageKey?.let {
                        imageUris[it] = uri
                        saveImageUrisToSharedPreferences(it,uri)
                        updateImageView(it, uri)
                    }
                }
            }

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(
                    requireContext(),
                    ImagePicker.getError(result.data),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Toast.makeText(requireContext(), "Bekor qilindi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateImageView(imageKey: String, uri: Uri) {
        when (imageKey) {
            "img_front" -> viewBinding.uploadImgFront.setImageURI(uri)
            "img_back" -> viewBinding.uploadImgBack.setImageURI(uri)
            "img_left" -> viewBinding.uploadImgLeft.setImageURI(uri)
            "img_right" -> viewBinding.uploadImgRight.setImageURI(uri)
            "img_front_chair" -> viewBinding.uploadImgFrontChair.setImageURI(uri)
            "img_back_chair" -> viewBinding.uploadImgBackChair.setImageURI(uri)
            "img_number" -> viewBinding.uploadImgNumber.setImageURI(uri)
            "img_license" -> viewBinding.uploadImgLicense.setImageURI(uri)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (findNavController().currentDestination?.id == R.id.checkDriverFragment) {
                        if (exportNumber == 1) {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    private fun loadAllImagesFromSharedPreferences(): Map<String, Uri> {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val imageUris = mutableMapOf<String, Uri>()
        val currentTime = System.currentTimeMillis()

        // Load all image URIs saved within the last 10 minutes
        val keys = listOf(
            "img_front", "img_back", "img_left", "img_right",
            "img_front_chair", "img_back_chair", "img_number", "img_license"
        )
        keys.forEach { key ->
            val savedUriString = sharedPreferences.getString(key, null)
            val timestamp = sharedPreferences.getLong("${key}_timestamp", 0)

            if (savedUriString != null && currentTime - timestamp < 8 * 60 * 1000) {
                imageUris[key] = Uri.parse(savedUriString)
            }
        }

        return imageUris
    }

    private fun saveImageUrisToSharedPreferences(imageKey: String, uri: Uri) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val currentTime = System.currentTimeMillis()
        editor.putString(imageKey, uri.toString())
        editor.putLong("${imageKey}_timestamp", currentTime)

        editor.apply()
    }

    override fun onStart() {
        super.onStart()
        val savedImageUris = loadAllImagesFromSharedPreferences()
        savedImageUris.forEach { (key, uri) ->
            imageUris[key] = uri
            updateImageView(key, uri)
        }
    }
}