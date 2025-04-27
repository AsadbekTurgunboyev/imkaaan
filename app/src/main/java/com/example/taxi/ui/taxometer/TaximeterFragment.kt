package com.example.taxi.ui.taxometer

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.soundmodule.SoundManager
import com.example.taxi.R
import com.example.taxi.databinding.FragmentDriverBinding
import com.example.taxi.domain.drive.currentDrive.CurrentDriveStatus
import com.example.taxi.domain.location.LocationTracker
import com.example.taxi.domain.model.DashboardData
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.DriveAction
import com.example.taxi.ui.home.HomeViewModel
import com.example.taxi.ui.home.driver.DriverViewModel
import com.example.taxi.utils.ButtonUtils
import com.example.taxi.utils.ConversionUtil.convertSecondsToMinutes
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Event
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.example.taxi.utils.TaxiCalculator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.tapadoo.alerter.Alerter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TaximeterFragment : Fragment(), LocationTracker.LocationUpdateListener {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentBottomStatus = DriveAction.TAX_STARTED
    private lateinit var locationTracker: LocationTracker
    private val driverViewModel: DriverViewModel by sharedViewModel()

    private val preferenceManager: UserPreferenceManager by inject()
    private val homeViewModel: HomeViewModel by sharedViewModel()
    private var lastStatus = -1
    private var seconds: Int = 0
    private val TIMER_MESSAGE_CODE = 1

    private val handlerTimer = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TIMER_MESSAGE_CODE) {
                try {
                    seconds++
                    viewBinding.bottomDialogTaxometer.progressTimeTextView.text =
                        convertSecondsToMinutes(seconds)
                    // Re-post the message with a delay of 1 second
                    sendEmptyMessageDelayed(TIMER_MESSAGE_CODE, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableLocationComponent()
            }
        }

    lateinit var viewBinding: FragmentDriverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTax = arguments?.getBoolean("is_taxo", false)
        if (isTax == true) {
            if(homeViewModel.dashboardLiveData.value?.isRunning() == true){
                if (homeViewModel.dashboardLiveData.value?.isPaused() == true) {
                    seconds = homeViewModel.dashboardLiveData.value?.getPauseTime()?: 0
                    homeViewModel.startDrive()
                    homeViewModel.pauseDrive()
                    handlerTimer.sendEmptyMessageDelayed(TIMER_MESSAGE_CODE, 1000)
                } else {

                }
            }else{
                homeViewModel.startDrive()
            }

            driverViewModel.completeTaximeter()
            preferenceManager.saveStatusIsTaximeter(true)
            preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.STARTED)
            preferenceManager.saveLastRaceId(-1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentDriverBinding.inflate(inflater, container, false)
        setUpSomeUiConfig()

        return viewBinding.root
    }

    private fun setUpSomeUiConfig() {
        viewBinding.bottomDialog.bttomcola.visibility = View.GONE
        viewBinding.bottomDialogTaxometer.bttomcola.visibility = View.VISIBLE
        viewBinding.buttonNavigator.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationTracker = LocationTracker.getInstance(requireContext())

        setUpUi()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val style =
            if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
//                NavigationStyles.NAVIGATION_NIGHT_STYLE
                Style.TRAFFIC_NIGHT
            } else {
                Style.TRAFFIC_DAY

//                NavigationStyles.NAVIGATION_DAY_STYLE
            }


        viewBinding.mapView.getMapboxMap().loadStyleUri(style) {
            // Style loaded
            // Check permission
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    enableLocationComponent()
                }

                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

        val llBottomSheet = view.findViewById<FrameLayout>(R.id.bottom_sheet)

        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(llBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetBehavior.isHideable = false

        viewBinding.bottomDialogTaxometer.bttomcola.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

        }
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        viewBinding.bottomDialogTaxometer.headerPeek.doOnLayout {
            bottomSheetBehavior.peekHeight = it.measuredHeight
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                val lastLocation: Location = locationResult.lastLocation!!

                viewBinding.mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(lastLocation.longitude, lastLocation.latitude))
                        .zoom(15.0).build()
                )
            }
        }

        viewBinding.bottomDialogTaxometer.swipeButton.onSwipedOnListener = {
            when (currentBottomStatus) {

                DriveAction.TAX_STARTED -> {
                    preferenceManager.clearPath()
                    viewBinding.bottomDialogTaxometer.swipeButton.checkedText =
                        getString(R.string.downloading)
                    viewBinding.bottomDialogTaxometer.swipeButton.uncheckedText =
                        getString(R.string.finish)
                    driverViewModel.acceptWithTaximeter()

                }

                DriveAction.TAX_COMPLETED -> {
                    viewBinding.bottomDialogTaxometer.swipeButton.checkedText =
                        getString(R.string.downloading)
                    viewBinding.bottomDialogTaxometer.swipeButton.uncheckedText =
                        getString(R.string.finish)
                    setFinishDialog()

                }
            }
        }

        viewBinding.bottomDialogTaxometer.apply {
//            calButtonDialog.setOnClickListener { ButtonUtils.callToPassenger(requireActivity()) }
            callBtn.setOnClickListener { ButtonUtils.callToDispatcher(requireActivity()) }
        }

        viewBinding.bottomDialogTaxometer.icPauseStart.setOnClickListener {
            if (homeViewModel.dashboardLiveData.value?.isPaused() == true) {
                handlerTimer.removeMessages(TIMER_MESSAGE_CODE)
                homeViewModel.startDrive()

            } else {
                handlerTimer.sendEmptyMessageDelayed(TIMER_MESSAGE_CODE, 1000)
                locationTracker.resumeTracking(this)
                locationTracker.pauseTracking()
                homeViewModel.pauseDrive()
            }
        }
    }

    private fun setFinishDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_ask_finish)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )

        val finishButton: MaterialButton = dialog.findViewById(R.id.button_finish_order)
        val cancelButton: MaterialButton = dialog.findViewById(R.id.button_cancel_order)

        finishButton.setOnClickListener {

            homeViewModel.startDrive()

            preferenceManager.saveStatusIsTaximeter(false)

            preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.COMPLETED)
            SoundManager(requireContext()).playSoundFinish()
            homeViewModel.stopDrive()

            viewBinding.parentContainer.keepScreenOn = false
            driverViewModel.acceptTaximeter()
            dialog.dismiss()

        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
            viewBinding.bottomDialogTaxometer.swipeButton.isChecked = false

        }

        dialog.setOnDismissListener {
            dialog.dismiss()
            viewBinding.bottomDialogTaxometer.swipeButton.isChecked = false
        }

        dialog.show()
    }

    private fun setUpUi() {
        viewBinding.callDispetcher.setOnClickListener {
            ButtonUtils.callToDispatcher(requireActivity())
        }

        homeViewModel.dashboardLiveData.observe(viewLifecycleOwner) {
            setDashboardStatus(it)
        }

        driverViewModel.orderSCTaximeter.observe(viewLifecycleOwner) {
            setBottomSheetSetting(it)
        }

        driverViewModel.acceptWithTaximeter.observe(viewLifecycleOwner) {
            acceptOrderUi(it)
        }
    }

    private fun acceptOrderUi(resourceEvent: Event<Resource<MainResponse<OrderAccept<UserModel, MileageData>>>>?) {
        resourceEvent?.getContentIfNotHandled()?.let { resource ->

            when (resource.state) {
                ResourceState.LOADING -> {}

                ResourceState.ERROR -> {
                    activity?.let { it1 ->
                        resource.message?.let { it2 ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                DialogUtils.createChangeDialog(
                                    activity = requireActivity(),
                                    title = "Xatolik!",
                                    message = it2,
                                    color = R.color.tred
                                )
                            }
                        }
                    }
                    driverViewModel.acceptTaximeter()

                    viewBinding.bottomDialogTaxometer.swipeButton.isChecked = false
//                    orderViewModel.clearAcceptOrderData()
                }

                ResourceState.SUCCESS -> {
                    viewBinding.bottomDialogTaxometer.swipeButton.isChecked = false
                    SoundManager(requireContext()).playSoundStartTaximeter()
                    val orderSettings = resource.data?.data
                    orderSettings?.let { it1 -> preferenceManager.savePriceSettings(it1) }
                    driverViewModel.completeTaximeter()
                    homeViewModel.startDrive()
                    preferenceManager.saveStatusIsTaximeter(true)
                    preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.STARTED)

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        viewBinding.mapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_navigation_puck_icon
                )
            )
            enabled = true

        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                val lastLocation = locationResult.lastLocation
                viewBinding.mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder().center(lastLocation?.longitude?.let {
                            Point.fromLngLat(
                                it, lastLocation.latitude
                            )
                        }).zoom(15.0).build()
                )
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, null /* Looper */
        )
    }


    @SuppressLint("StringFormatInvalid")
    private fun setDashboardStatus(dashboardData: DashboardData?) {

        if (lastStatus != dashboardData?.getStatus()) {
            lastStatus = dashboardData?.getStatus()!!
            when (dashboardData.getStatus()) {

                CurrentDriveStatus.STARTING -> {
                    viewBinding.bottomDialogTaxometer.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(), R.drawable.ic_pause
                        )
                    )

                    viewBinding.bottomDialogTaxometer.currentDetailTime.text =
                        getString(R.string.buyurtmada)

                    Alerter.create(activity = requireActivity())
                        .setTitle(title = getString(R.string.recconnect)).enableProgress(true)
                        .setDismissable(false)
                        .setText("${getString(R.string.recconnect)}:  ${dashboardData.getGPSSignalStrength()}")
                        .setBackgroundColorRes(colorResId = R.color.blue).show()

                    viewBinding.parentContainer.keepScreenOn = true
                }

                CurrentDriveStatus.STARTED -> {


                    Alerter.hide()
                    viewBinding.bottomDialogTaxometer.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(), R.drawable.ic_pause
                        )
                    )

                    viewBinding.bottomDialogTaxometer.currentDetailTime.text =
                        getString(R.string.buyurtmada)

                    viewBinding.bottomDialogTaxometer.timeWorkTextView.visibility = View.VISIBLE
                    viewBinding.bottomDialogTaxometer.progressTimeTextView.visibility = View.GONE
                    viewBinding.parentContainer.keepScreenOn = true
                }

                CurrentDriveStatus.PAUSED -> {
                    Alerter.hide()
                    viewBinding.bottomDialogTaxometer.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(), R.drawable.ic_play
                        )
                    )

                    viewBinding.bottomDialogTaxometer.currentDetailTime.text =
                        getString(R.string.pulli_kutish)

                    seconds = dashboardData.getPauseTime()
//                    viewBinding.timeCard.visibility = View.VISIBLE
                    viewBinding.bottomDialogTaxometer.progressTimeTextView.visibility = View.VISIBLE
                    viewBinding.bottomDialogTaxometer.timeWorkTextView.visibility = View.GONE
                    viewBinding.parentContainer.keepScreenOn = true
                }

                CurrentDriveStatus.STOPPED -> {
                    Alerter.hide()
                    viewBinding.bottomDialogTaxometer.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(), R.drawable.ic_pause
                        )
                    )
//                    viewBinding.timeCard.visibility = View.GONE

                    viewBinding.parentContainer.keepScreenOn = false
                }
            }
        }

        var secondsElapsed = 0
        viewBinding.bottomDialogTaxometer.timeWorkTextView.text = dashboardData.timeText()


        Log.d("tekshirish1", "setDashboardStatus: ${preferenceManager.getStartCost()}")
        viewBinding.bottomDialogTaxometer.inDriveCostPrice.text =
            if (preferenceManager.isHasDestinationSecond()) {
                preferenceManager.getStartCost().toString()
            } else {
                TaxiCalculator.getCurrentDriveCost(
                    dashboardData, preferenceManager, secondsElapsed.toLong()
                )
            }
    }

    private fun setBottomSheetSetting(state: Int?) {
        if (currentBottomStatus != state) {
            currentBottomStatus = state!!
        }
        when (state) {

            DriveAction.TAX_STARTED -> {
                viewBinding.bottomDialogTaxometer.timeWorkTextView.visibility = View.GONE
                viewBinding.bottomDialogTaxometer.progressTimeTextView.visibility = View.GONE
                viewBinding.bottomDialogTaxometer.linearLayoutWitPause.visibility = View.GONE
                viewBinding.bottomDialogTaxometer.currentDetailTime.text =
                    getString(R.string.taximeter)
                viewBinding.bottomDialogTaxometer.swipeButton.uncheckedText =
                    getString(R.string.lets_go)
                viewBinding.parentContainer.keepScreenOn = false

            }

            DriveAction.TAX_COMPLETED -> {
                viewBinding.bottomDialogTaxometer.timeWorkTextView.visibility = View.VISIBLE
                viewBinding.bottomDialogTaxometer.progressTimeTextView.visibility = View.GONE
                viewBinding.bottomDialogTaxometer.linearLayoutWitPause.visibility = View.VISIBLE
                viewBinding.bottomDialogTaxometer.swipeButton.uncheckedText =
                    getString(R.string.finish)
                viewBinding.parentContainer.keepScreenOn = true
            }

        }
    }

    override fun onLocationChanged(location: Location?) {

    }


    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        val activityManager =
            requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.appTasks
        if (tasks.isNotEmpty()) {
            tasks[0].setExcludeFromRecents(true)
        }
    }

    override fun onDistanceChanged(distance: Float) {
        handlerTimer.removeMessages(TIMER_MESSAGE_CODE)
        homeViewModel.startDrive()
        locationTracker.stopLocationUpdates()
    }

}