package com.example.taxi.ui.login.selfie

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.imagepickerlibrary.ImagePicker
import com.app.imagepickerlibrary.ImagePicker.Companion.registerImagePicker
import com.app.imagepickerlibrary.listener.ImagePickerResultListener
import com.app.imagepickerlibrary.model.PickExtension
import com.app.imagepickerlibrary.model.PickerType
import com.example.taxi.R
import com.example.taxi.databinding.FragmentFillSelfieBinding
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.HomeActivity
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.tapadoo.alerter.Alerter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FillSelfieFragment : Fragment() {
    lateinit var viewBinding: FragmentFillSelfieBinding
    private val selfieViewModel: SelfieViewModel by viewModel()

    private lateinit var mProfileUri: Uri
    private lateinit var mLicenseUri: Uri
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    mProfileUri = fileUri
                    viewBinding.uploadImgSelfie.setImageURI(fileUri)
                }
                com.github.dhaval2404.imagepicker.ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireContext(),
                        com.github.dhaval2404.imagepicker.ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(requireContext(), "Bekor qilindi!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    private val starForLicenseImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data!!

                mLicenseUri = fileUri
                viewBinding.uploadImgLicense.setImageURI(fileUri)
            }
            com.github.dhaval2404.imagepicker.ImagePicker.RESULT_ERROR -> {
                Toast.makeText(
                    requireContext(),
                    com.github.dhaval2404.imagepicker.ImagePicker.getError(data),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Bekor qilindi!", Toast.LENGTH_SHORT).show()
            }
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selfieViewModel.selfieResponse.observe(this) { handleData(it) }

    }

    private fun handleData(resource: Resource<MainResponse<SelfieAllData<IsCompletedModel,StatusModel>>>?) {
        resource?.let {
            when (resource.state) {
                ResourceState.LOADING -> {
                    activity?.let { it1 ->
                        Alerter.create(it1).setTitle("Yuklanmoqda...")
                            .setDuration(60000)
                            .setText("Siz belgilangan rasmlar yuklanmoqda, bu biroz vaqtni olishi mumkin")
                            .enableProgress(true).setProgressColorRes(R.color.primaryColor).show()
                    }
                }
                ResourceState.SUCCESS -> {
                    selfieViewModel.clearFile()
                    Alerter.clearCurrent(activity)
                    resource.data?.let { complete ->
                        if (complete.data.is_completed.string) {
                            UserPreferenceManager(requireContext()).setRegisterComplete()
                            Toast.makeText(
                                requireContext(),
                                "Ro'yhatdan muvafaqiyatli o'tdingiz.",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity?.let { it1 -> HomeActivity.open(activity = it1) }
                        }
                    }
                }
                ResourceState.ERROR -> {
                    selfieViewModel.clearFile()
                    Alerter.clearCurrent(activity)
                    DialogUtils.createChangeDialog(
                        activity = requireActivity(),
                        title = "Xatolik",
                        message = "${resource.message}",
                        color =  R.color.tred
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewBinding = FragmentFillSelfieBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVar()

        with(viewBinding) {
            backPersonDataFbn.setOnClickListener {
                val navController = findNavController()
                navController.navigateUp()
            }

            uploadLicense.setOnClickListener {
                com.github.dhaval2404.imagepicker.ImagePicker.with(requireActivity()).cameraOnly()
                    .cropSquare().compress(1024)
                    .maxResultSize(1080, 1080) //com.example.taxi.domain.model.history.com.example.taxi.domain.model.history.com.example.taxi.domain.model.history.User can only capture image using Camera
                    .createIntent { intent ->
                        starForLicenseImageResult.launch(intent)
                    }
            }

            uploadSelfie.setOnClickListener {
                com.github.dhaval2404.imagepicker.ImagePicker.with(requireActivity()).cameraOnly()
                    .cropSquare().compress(1024)
                    .maxResultSize(1080, 1080) //com.example.taxi.domain.model.history.com.example.taxi.domain.model.history.com.example.taxi.domain.model.history.User can only capture image using Camera
                    .createIntent { intent ->
                        startForProfileImageResult.launch(intent)
                    }
            }


            nextDataCarFbn.setOnClickListener {
                selfieViewModel.fillSelfie(
                    selfieUri = mProfileUri,
                    licensePhotoUri = mLicenseUri,
                    contentResolver = requireContext().contentResolver
                )
            }
        }
    }

    private fun initVar() {

    }





}

