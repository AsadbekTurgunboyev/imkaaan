package com.example.taxi.ui.home.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.ConnectivityManager
import android.os.*
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.example.soundmodule.SoundManager
import com.example.taxi.R
import com.example.taxi.databinding.FragmentDriverBinding
import com.example.taxi.dbModels.TimerViewModel
import com.example.taxi.domain.drive.currentDrive.CurrentDriveStatus.*
import com.example.taxi.domain.location.LocationTracker
import com.example.taxi.domain.model.DashboardData
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.domain.preference.UserPreferenceManager.Companion.START_COST
import com.example.taxi.network.NO_CONNECT
import com.example.taxi.network.NetworkMonitor
import com.example.taxi.network.NetworkReceiver
import com.example.taxi.network.NetworkViewModel
import com.example.taxi.socket.SocketConfig
import com.example.taxi.ui.home.DriveAction
import com.example.taxi.ui.home.HomeViewModel
import com.example.taxi.ui.home.order.OrderViewModel
import com.example.taxi.ui.permission.PermissionCheckActivity
import com.example.taxi.utils.*
import com.example.taxi.utils.ConstantsUtils.locationDestination2
import com.example.taxi.utils.map.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.tapadoo.alerter.Alerter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit


class DriverFragment : Fragment(), LocationTracker.LocationUpdateListener {


    private lateinit var networkMonitor: NetworkMonitor
    private var soundPlayed = false
    lateinit var soundManager: SoundManager
    private lateinit var locationTracker: LocationTracker
    private val networkViewModel: NetworkViewModel by viewModel()
    private lateinit var networkReceiver: NetworkReceiver
    private var wakeLock: PowerManager.WakeLock? = null
    private var timer: ForwardsTimerUtil? = null
    private var isOrderStarted: Boolean = false
    private val preferenceManager: UserPreferenceManager by inject()
    private val orderViewModel: OrderViewModel by sharedViewModel()
    private val homeViewModel: HomeViewModel by sharedViewModel()
    private val driverViewModel: DriverViewModel by sharedViewModel()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableLocationComponent()
            }
        }
    private var seconds: Int = 0
    private lateinit var pauseViewModel: TimerViewModel
    private val TIMER_MESSAGE_CODE = 1
    private val handlerTimer = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TIMER_MESSAGE_CODE) {
                try {
                    seconds++
                    viewBinding.bottomDialog.progressTimeTextView.text =
                        convertSecondsToMinutes(seconds)
                    // Re-post the message with a delay of 1 second
                    sendEmptyMessageDelayed(TIMER_MESSAGE_CODE, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private var moneySeconds: Int = 0

    private var minWaitTime: Int = preferenceManager.getMinWaitTime()
    private var timerManager: TimerManager? = null




    private var lastStatus = -1
    private var currentBottomStatus = DriveAction.ACCEPT

    private var startCost = 0

    private val permissionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (LocationPermissionUtils.isLocationEnabled(requireContext())
                && LocationPermissionUtils.isBasicPermissionGranted(requireContext())
            ) {
                startDrive()
            }
        }


    private val distanceFormatter: DistanceFormatterOptions by lazy {
        DistanceFormatterOptions.Builder(requireContext())
            .unitType(UnitType.METRIC)
            .build()
    }

    private val mapboxReplayer = MapboxReplayer()

    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    //    private lateinit var mapboxNavigation: MapboxNavigation
//    private lateinit var navigationCamera: NavigationCamera
//    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var prefs: SharedPreferences
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == START_COST) {
                // Do something when START_COST changes
                val newStartCost = sharedPreferences.getInt(START_COST, 0)
                val a =
                    PhoneNumberUtil.formatMoneyNumberPlate(newStartCost.toString())
                viewBinding.bottomDialog.priceTextViewDialog.text = a

            }
        }
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    var mapView: MapView? = null
    lateinit var viewBinding: FragmentDriverBinding

    private lateinit var maneuverApi: MapboxManeuverApi

    //    private lateinit var tripProgressApi: MapboxTripProgressApi
//    private lateinit var routeLineApi: MapboxRouteLineApi
//    private lateinit var routeLineView: MapboxRouteLineView
//    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()
//    private lateinit var routeArrowView: MapboxRouteArrowView
//    private val navigationLocationProvider = NavigationLocationProvider()
//    private val locationObserver = object : LocationObserver {
//        var firstLocationUpdateReceived = false
//
//        override fun onNewRawLocation(rawLocation: Location) {
//            // not handled
//        }
//
//
//        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
//            val enhancedLocation = locationMatcherResult.enhancedLocation
//            // update location puck's position on the map
//            navigationLocationProvider.changePosition(
//                location = enhancedLocation,
//                keyPoints = locationMatcherResult.keyPoints,
//            )
//
//            // update camera position to account for new location
//            viewportDataSource.onLocationChanged(enhancedLocation)
//            viewportDataSource.evaluate()
//
//            // if this is the first location update the activity has received,
//            // it's best to immediately move the camera to the current user location
//            if (!firstLocationUpdateReceived) {
//                firstLocationUpdateReceived = true
//                navigationCamera.requestNavigationCameraToOverview(
//                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
//                        .maxDuration(0) // instant transition
//                        .build()
//                )
//            }
//        }
//    }
    private lateinit var mapboxMap: MapboxMap

//    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
//        // update the camera position to account for the progressed fragment of the route
//        viewportDataSource.onRouteProgressChanged(routeProgress)
//        viewportDataSource.evaluate()
//
//
//        // draw the upcoming maneuver arrow on the map
//        val style = mapboxMap.getStyle()
//        if (style != null) {
//            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
//            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
//        }
//
//        // update top banner with maneuver instructions
//        val maneuvers = maneuverApi.getManeuvers(routeProgress)
//        maneuvers.fold(
//            { error ->
//                Toast.makeText(
//                    context,
//                    error.errorMessage,
//                    Toast.LENGTH_SHORT
//                ).show()
//            },
//            {
////                viewBinding.maneuverView.visibility = View.VISIBLE
//                viewBinding.maneuverView.renderManeuvers(maneuvers)
//            }
//        )
//
//        // update bottom trip progress summary
////        viewBinding.tripProgressView.render(
////            tripProgressApi.getTripProgress(routeProgress)
////        )
//    }

//    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

//    private val routesObserver = RoutesObserver { routeUpdateResult ->
//        if (routeUpdateResult.routes.isNotEmpty()) {
//            // generate route geometries asynchronously and render them
//            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }
//
//            routeLineApi.setRoutes(
//                routeLines
//            ) { value ->
//                mapboxMap.getStyle()?.apply {
//                    routeLineView.renderRouteDrawData(this, value)
//                }
//            }
//
//            // update the camera position to account for the new route
//            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
//            viewportDataSource.evaluate()
//        } else {
//            // remove the route line and route arrow from the map
//            val style = mapboxMap.getStyle()
//            if (style != null) {
//                routeLineApi.clearRouteLine { value ->
//                    routeLineView.renderClearRouteLineValue(
//                        style,
//                        value
//                    )
//                }
//                routeArrowView.render(style, routeArrowApi.clearArrows())
//            }
//
//            // remove the route reference from camera position evaluations
//            viewportDataSource.clearRouteData()
//            viewportDataSource.evaluate()
//        }
//    }

    private var isRouteFetched = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = preferenceManager.getSharedPreferences()

        when (arguments?.getInt("driver_current_position", -1)) {
            ConstantsUtils.STATUS_GOING_TO_CLIENT -> {
                driverViewModel.acceptedOrder()
            }

            ConstantsUtils.STATUS_WAITING_FOR_CLIENT -> {
                val startTime = arguments?.getInt("driver_arrivedAt", 0)
                startTime?.toLong()?.let { preferenceManager.setStartedTimeAcceptOrder(it * 1000) }
                driverViewModel.arrivedOrder()
            }

            ConstantsUtils.STATUS_ON_THE_WAY -> {
                driverViewModel.startedOrder()

                if (homeViewModel.dashboardLiveData.value?.isRunning() == true) {
                    if (homeViewModel.dashboardLiveData.value?.isPaused() == true){
                        seconds = homeViewModel.dashboardLiveData.value?.getPauseTime()?: 0
                        homeViewModel.startDrive()
                        homeViewModel.pauseDrive()
                        handlerTimer.sendEmptyMessageDelayed(TIMER_MESSAGE_CODE, 1000)

                    }else{

                    }
                }else{
                    Log.d("tekshirish", "onCreate: pauza bolmadi")
                    homeViewModel.startDrive()
                }

            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewBinding = FragmentDriverBinding.inflate(inflater, container, false)
        preferenceManager.saveStatusIsTaximeter(false)


        networkReceiver = NetworkReceiver { isConnected ->
            if (isConnected) {
                networkViewModel.getOrderCurrent()
            }
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                networkReceiver, filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            requireContext().registerReceiver(networkReceiver, filter)
        }

        networkViewModel.response.observe(viewLifecycleOwner) { response ->
            when (response?.state) {
                ResourceState.SUCCESS -> {

                }

                ResourceState.ERROR -> {
                    if (response.message == NO_CONNECT) {
                        networkViewModel.clearNetworkData()
                        val intent = Intent("com.example.taxi.ORDER_DATA_ACTION")
                        intent.putExtra(SocketConfig.ORDER_CANCELLED, NO_CONNECT)
                        context?.sendBroadcast(intent)


                    }


                }

                ResourceState.LOADING -> {}
                else -> {}
            }
        }

        // Inflate the layout for this fragment
        return viewBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUiNavigation()
        networkMonitor = NetworkMonitor(requireContext())
        soundManager = SoundManager(requireContext())
//        viewModel = ViewModelProvider(this)[TimerViewModel::class.java]
//        viewModel.timeLiveData.observe(viewLifecycleOwner) { (time, message) ->
//            updateUITime(time, message)
//        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        locationTracker = LocationTracker.getInstance(requireContext())
//        handlerTimer = Handler(L ooper.getMainLooper())
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViewDialog()
        mapboxMap = viewBinding.mapView.getMapboxMap()
        lastStatus = -1
        val llBottomSheet = view.findViewById<FrameLayout>(R.id.bottom_sheet)

        val bottomSheetBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(llBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetBehavior.isHideable = false

        viewBinding.bottomDialog.bttomcola.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

        }
        // set callback for changes

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        driverViewModel.orderStartCompleteLiveData.observe(viewLifecycleOwner) {
            setBottomSheetSetting(it)
        }

        viewBinding.callDispetcher.setOnClickListener {
            ButtonUtils.callToDispatcher(requireActivity())
        }

        homeViewModel.dashboardLiveData.observe(viewLifecycleOwner) {
            setDashboardStatus(it)
        }

        orderViewModel.acceptOrder.observe(viewLifecycleOwner) {
            setOrderSettingsUi(it)
        }

        driverViewModel.arriveOrder.observe(viewLifecycleOwner) {
            val timestampMilliseconds: Long = System.currentTimeMillis()

            if (preferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.ACCEPTED) {
                preferenceManager.setStartedTimeAcceptOrder(timestampMilliseconds)
            }

            arriveOrderUi(it)

        }

        driverViewModel.startOrder.observe(viewLifecycleOwner) {
//            startWorkTimer()
            startOrderUi(it)
        }

        viewBinding.bottomDialog.icPauseStart.setOnClickListener {
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

        viewBinding.bottomDialog.apply {
            calButtonDialog.setOnClickListener { ButtonUtils.callToPassenger(requireActivity()) }
            callBtn.setOnClickListener { ButtonUtils.callToPassenger(requireActivity()) }
        }


//        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
//            MapboxNavigationProvider.retrieve()
//        } else {
//            MapboxNavigationProvider.create(
//                NavigationOptions.Builder(applicationContext = requireContext())
//                    .accessToken(getString(R.string.mapbox_access_token))
//                    // comment out the location engine setting block to disable simulation
////                    .locationEngine(replayLocationEngine)
//                    .build()
//            )
//        }
//        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
//        navigationCamera = NavigationCamera(
//            mapboxMap,
//            viewBinding.mapView.camera,
//            viewportDataSource
//        )

//        viewBinding.mapView.camera.addCameraAnimationsLifecycleListener(
//            NavigationBasicGesturesHandler(navigationCamera)
//        )

//        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
//            // shows/hide the recenter button depending on the camera state
//            when (navigationCameraState) {
//                NavigationCameraState.TRANSITION_TO_FOLLOWING,
//                NavigationCameraState.FOLLOWING -> viewBinding.recenter.visibility = View.INVISIBLE
//                NavigationCameraState.TRANSITION_TO_OVERVIEW,
//                NavigationCameraState.OVERVIEW,
//                NavigationCameraState.IDLE -> viewBinding.recenter.visibility = View.VISIBLE
//            }
//        }
//        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            viewportDataSource.overviewPadding = landscapeOverviewPadding
//        } else {
//            viewportDataSource.overviewPadding = overviewPadding
//        }
//        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            viewportDataSource.followingPadding = landscapeFollowingPadding
//        } else {
//            viewportDataSource.followingPadding = followingPadding
//        }

//        val distanceFormatterOptions = mapboxNavigation
//            .navigationOptions
//            .distanceFormatterOptions
//

        // initialize maneuver api that feeds the data to the top banner maneuver view
//        maneuverApi = MapboxManeuverApi(
//            MapboxDistanceFormatter(distanceFormatterOptions)
//        )

//        tripProgressApi = MapboxTripProgressApi(
//            TripProgressUpdateFormatter.Builder(requireContext())
//                .distanceRemainingFormatter(
//                    DistanceRemainingFormatter(distanceFormatter)
//                    /**maybe change [distanceFormatterOptions] */
//                )
//                .timeRemainingFormatter(
//                    TimeRemainingFormatter(requireContext())
//                )
//                .percentRouteTraveledFormatter(
//                    PercentDistanceTraveledFormatter()
//                )
//                .estimatedTimeToArrivalFormatter(
//                    EstimatedTimeToArrivalFormatter(requireContext(), TimeFormat.NONE_SPECIFIED)
//                )
//                .build()
//        )

//        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(requireContext())
//            .withRouteLineBelowLayerId("road-label")
//            .build()
//
//        lifecycle.coroutineScope.launch {
//            mapboxNavigation.flowRouteProgress().collect {
//                val value = tripProgressApi.getTripProgress(it)
//                viewBinding.bottomDialog.distanceTextViewDialog.text =
//                    value.formatter.getDistanceRemaining(value.distanceRemaining)
//
//                viewBinding.bottomDialog.timeRemainingTextView.text =
//                    value.formatter.getTimeRemaining(value.currentLegTimeRemaining)
////                viewBinding.remainingTimeTextView.text =
////                    "Mijozni oling: ${value.formatter.getTimeRemaining(value.currentLegTimeRemaining)}"
//            }
//        }

        viewBinding.bottomDialog.headerPeek.doOnLayout {
            bottomSheetBehavior.peekHeight = it.measuredHeight
        }
//        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
//        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
//        val routeArrowOptions = RouteArrowOptions.Builder(requireContext())
//            .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID).build()
//        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        viewBinding.bottomDialog.swipeButton.onSwipedOnListener = {
            when (currentBottomStatus) {

                DriveAction.STARTED -> {
                    viewBinding.bottomDialog.swipeButton.checkedText =
                        getString(R.string.downloading)
                    setFinishDialog()

                }

                DriveAction.ARRIVED -> {
                     viewBinding.bottomDialog.swipeButton.checkedText =
                        getString(R.string.downloading)
                    viewBinding.bottomDialog.swipeButton.uncheckedText = getString(R.string.finish)
                    driverViewModel.startOrder()
                }

                DriveAction.COMPLETED -> {

                }

                DriveAction.ACCEPT -> {
                    viewBinding.bottomDialog.swipeButton.checkedText =
                        getString(R.string.downloading)
                    viewBinding.bottomDialog.swipeButton.uncheckedText = getString(R.string.lets_go)
                    driverViewModel.arriveOrder()

                }
            }
        }

        val style =
            if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
//                NavigationStyles.NAVIGATION_NIGHT_STYLE
                Style.TRAFFIC_NIGHT
            } else {
                Style.TRAFFIC_DAY

//                NavigationStyles.NAVIGATION_DAY_STYLE
            }


//         load map style
        mapboxMap.loadStyleUri(
//            Style.MAPBOX_STREETS
            style
        ) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) -> {
                    enableLocationComponent()
                }

                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            when (preferenceManager.getDriverStatus()) {
                UserPreferenceManager.DriverStatus.ACCEPTED -> {

                    viewBinding.buttonNavigator.visibility = View.VISIBLE
//                    findRoute(
//                        Point.fromLngLat(locationStart.placeLongitude, locationStart.placeLatitude),
//                        Point.fromLngLat(
//                            locationDestination.placeLongitude,
//                            locationDestination.placeLatitude
//                        )
//                    )
                }

                UserPreferenceManager.DriverStatus.ARRIVED -> {
                    viewBinding.buttonNavigator.visibility = View.GONE

                    locationDestination2?.let {
                        changeDestination(
                            destination = Point.fromLngLat(
                                it.placeLongitude,
                                it.placeLatitude
                            )
                        )
                    }
                }

                else -> {
                    clearRouteAndStopNavigation()
                }
            }


//
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                val lastLocation: Location = locationResult.lastLocation!!

                mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(lastLocation.longitude, lastLocation.latitude))
                        .zoom(15.0)
                        .build()
                )
            }
        }

        viewBinding.mapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_navigation_puck_icon
                )
            )
//            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

//        viewBinding.recenter.setOnClickListener {
//            navigationCamera.requestNavigationCameraToFollowing()
//            viewBinding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
//
//            if (isRouteFetched) {
//                isRouteFetched = false
//                if (preferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.ACCEPTED) {
//                    viewBinding.maneuverView.visibility = View.VISIBLE
//                }
//            }
//        }
//        viewBinding.routeOverview.setOnClickListener {
//            navigationCamera.requestNavigationCameraToOverview()
//            viewBinding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
//        }
//
//        mapboxNavigation.startTripSession()


    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        Log.d("Mapbox", "Enabling location component")

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                val lastLocation = locationResult.lastLocation
                mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(lastLocation?.longitude?.let {
                            com.mapbox.geojson.Point.fromLngLat(
                                it, lastLocation.latitude
                            )
                        })
                        .zoom(15.0)
                        .build()
                )
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun setFinishDialog() {

        val dialog = Dialog(requireContext())
        homeViewModel.startDrive()

        dialog.setContentView(R.layout.dialog_ask_finish)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val finishButton: MaterialButton = dialog.findViewById(R.id.button_finish_order)
        val cancelButton: MaterialButton = dialog.findViewById(R.id.button_cancel_order)

        finishButton.setOnClickListener {


            preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.COMPLETED)
            clearRouteAndStopNavigation()
            handlerTimer.removeMessages(TIMER_MESSAGE_CODE)
            SoundManager(requireContext()).playSoundFinish()
            homeViewModel.stopDrive()
            viewBinding.parentContainer.keepScreenOn = false
            dialog.dismiss()

            driverViewModel.completedOrder()

        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
            viewBinding.bottomDialog.swipeButton.isChecked = false

        }

        dialog.setOnDismissListener {
            dialog.dismiss()
            viewBinding.bottomDialog.swipeButton.isChecked = false
        }

        dialog.show()
    }


    private fun updateUITime(time: Int, status: String) {
        activity?.runOnUiThread {
            viewBinding.bottomDialog.currentDetailTime.text = status
            viewBinding.bottomDialog.progressTimeTextView.text = convertSecondsToMinutes(time)
        }
    }


    private fun initViewDialog() {
        preferenceManager.getDestinationAddress()
            ?.let { viewBinding.addressFromTextViewNavigator.convertToCyrillic(it) }
        viewBinding.buttonNavigator.setOnClickListener {
            findRoute(
                preferenceManager.getDestination1Lat(),
                preferenceManager.getDestination1Long()
            )
        }
        with(viewBinding.bottomDialog) {
            preferenceManager.getDestinationAddress()
                ?.let { addressTextViewDialogOnMap.convertToCyrillic(it) }

            if (preferenceManager.getPassengerName()
                    .isNullOrEmpty()
            ) {
                passengerName.visibility = View.GONE
                passengerPhone.gravity = Gravity.CENTER_VERTICAL
                passengerPhone.setTextAppearance(android.R.style.TextAppearance_Material_Medium)
                passengerPhone.text = preferenceManager.getPassengerPhone()
            } else {

                passengerName.visibility = View.VISIBLE
                passengerName.text = preferenceManager.getPassengerName()
                passengerPhone.text = preferenceManager.getPassengerPhone()
            }
            passengerComment.text = preferenceManager.getPassengerComment()


            val a =
                PhoneNumberUtil.formatMoneyNumberPlate(preferenceManager.getStartCost().toString())
            priceTextViewDialog.text = a
        }
    }


    private fun convertSecondsToMinutes(seconds: Int): String {

        val minutes = seconds / 60
        val secondsRemainder = seconds % 60
        val formattedMinutes = String.format("%02d", minutes)
        val formattedSeconds = String.format("%02d", secondsRemainder)
        return "$formattedMinutes:$formattedSeconds"
    }

    @SuppressLint("MissingPermission")
    private fun setBottomSheetSetting(state: Int?) {

        if (currentBottomStatus != state) {
            currentBottomStatus = state!!
        }
        when (state) {
            DriveAction.ACCEPT -> {

                viewBinding.buttonNavigator.visibility = View.VISIBLE
                viewBinding.bottomDialog.timeWorkTextView.visibility = View.GONE
                viewBinding.bottomDialog.progressTimeTextView.visibility = View.GONE
                viewBinding.bottomDialog.timeRemainingTextView.visibility = View.VISIBLE
                viewBinding.bottomDialog.currentDetailTime.text = getString(R.string.mijozgacha)
                viewBinding.parentContainer.keepScreenOn = true
                viewBinding.bottomDialog.swipeButton.uncheckedText = getString(R.string.arrive)

//                acquireWakeLock()
//                homeViewModel.startDrive()
            }

            DriveAction.ARRIVED -> {
                Log.d(TAG1, "setBottomSheetSetting: arrived")
                startTimer()

                clearRouteAndStopNavigation()


                preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.ARRIVED)
                viewBinding.bottomDialog.timeWorkTextView.visibility = View.GONE
                viewBinding.bottomDialog.progressTimeTextView.visibility = View.VISIBLE
                viewBinding.bottomDialog.timeRemainingTextView.visibility = View.GONE
                viewBinding.bottomDialog.addressTextViewDialogOnMap.text =
                    preferenceManager.getDestination2Address()
                viewBinding.bottomDialog.currentDetailTime.text =
                    getString(R.string.bepul_kutish)
                viewBinding.parentContainer.keepScreenOn = true
                viewBinding.bottomDialog.swipeButton.uncheckedText = getString(R.string.lets_go)

//                acquireWakeLock()


            }

            DriveAction.STARTED -> {
                timerManager?.saveTransitionTime()
//                timerManager?.clearTime()

                viewBinding.bottomDialog.swipeButton.uncheckedText = getString(R.string.finish)


                preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.STARTED)
                viewBinding.bottomDialog.timeWorkTextView.visibility = View.VISIBLE
                viewBinding.bottomDialog.progressTimeTextView.visibility = View.GONE
                viewBinding.bottomDialog.timeRemainingTextView.visibility = View.GONE

                if (locationDestination2 != null) {

                    changeDestination(
                        destination = Point.fromLngLat(
                            locationDestination2!!.placeLongitude,
                            locationDestination2!!.placeLatitude
                        )
                    )
                } else {
                    clearRouteAndStopNavigation()
                }

                viewBinding.bottomDialog.linearLayoutWitPause.visibility = View.VISIBLE
                viewBinding.bottomDialog.linearLayoutNoPause.visibility = View.GONE
                viewBinding.bottomDialog.currentDetailTime.text = getString(R.string.buyurtmada)
                Log.d("jarayonn", "setBottomSheetSetting: buyurtma")
                viewBinding.parentContainer.keepScreenOn = true

            }

            DriveAction.COMPLETED -> {


            }

        }
    }

    private fun setOrderSettingsUi(event: Event<Resource<MainResponse<OrderAccept<UserModel, MileageData>>>>) {
        event.getContentIfNotHandled()?.let { response ->
            when (response.state) {
                ResourceState.SUCCESS -> {
                    minWaitTime = response.data?.data?.getMinWaitTime()!!
                    startCost = response.data.data.start_cost
                    viewBinding.bottomDialog.priceTextViewDialog.text =
                        PhoneNumberUtil.formatMoneyNumberPlate(startCost.toString())
                    viewBinding.bottomDialog.addressTextViewDialogOnMap.text =
                        response.data.data.address.from
                    val orderSettings = response.data.data
                    preferenceManager.savePriceSettings(orderSettings)

                }

                ResourceState.ERROR -> {

                }

                ResourceState.LOADING -> {}
            }
        }
    }

    private fun setUpUiNavigation() {
        viewBinding.textView9.text = preferenceManager.getMapName()

        when (preferenceManager.getMapSettings()) {
            MapUtils.MapType.WAZE.packageName -> {
                viewBinding.imageView4.setImageResource(R.drawable.icon_waze)
            }

            MapUtils.MapType.YANDEX.packageName -> {
                viewBinding.imageView4.setImageResource(R.drawable.ic_yandex_navigator)
            }

            MapUtils.MapType.GOOGLE.packageName -> {
                viewBinding.imageView4.setImageResource(R.drawable.icon_google_maps)

            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun setDashboardStatus(dashboardData: DashboardData?) {

        if (lastStatus != dashboardData?.getStatus()) {
            lastStatus = dashboardData?.getStatus()!!
            when (dashboardData.getStatus()) {

                STARTING -> {
                    viewBinding.bottomDialog.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause
                        )
                    )

                    viewBinding.bottomDialog.currentDetailTime.text =
                        getString(R.string.buyurtmada)

                    Alerter.create(activity = requireActivity())
                        .setTitle(title = getString(R.string.recconnect))
                        .enableProgress(true)
                        .setDismissable(false)
                        .setText("${getString(R.string.recconnect)}:  ${dashboardData.getGPSSignalStrength()}")
                        .setBackgroundColorRes(colorResId = R.color.blue)
                        .show()

                    viewBinding.parentContainer.keepScreenOn = true
                }

                STARTED -> {


                    Alerter.hide()
                    viewBinding.bottomDialog.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause
                        )
                    )

                    viewBinding.bottomDialog.currentDetailTime.text =
                        getString(R.string.buyurtmada)

                    viewBinding.bottomDialog.progressTimeTextView.visibility = View.GONE
                    viewBinding.bottomDialog.timeWorkTextView.visibility = View.VISIBLE
                    viewBinding.parentContainer.keepScreenOn = true
                }

                PAUSED -> {

                    Alerter.hide()
                    viewBinding.bottomDialog.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_play
                        )
                    )

                    viewBinding.bottomDialog.currentDetailTime.text =
                        getString(R.string.pulli_kutish)
                    Log.d("jarayonn", "setDashboardStatus: pulli kutish")


//                    viewBinding.timeCard.visibility = View.VISIBLE
                    viewBinding.bottomDialog.progressTimeTextView.visibility = View.VISIBLE
                    viewBinding.bottomDialog.timeWorkTextView.visibility = View.GONE
                    viewBinding.parentContainer.keepScreenOn = false
                }

                STOPPED -> {
                    Alerter.hide()
                    viewBinding.bottomDialog.icPauseStart.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause
                        )
                    )
//                    viewBinding.timeCard.visibility = View.GONE

                    viewBinding.parentContainer.keepScreenOn = false
                }
            }
        }
        var secondsElapsed = 0





        viewBinding.bottomDialog.timeWorkTextView.text = dashboardData.timeText()


        viewBinding.bottomDialog.inDriveCostPrice.text =
            if (preferenceManager.isHasDestinationSecond()) {
                preferenceManager.getStartCost().toString()
            } else {
                TaxiCalculator.getCurrentDriveCost(
                    dashboardData,
                    preferenceManager,
                    secondsElapsed.toLong()
                )
            }


    }

    private fun changeDestination(destination: Point) {
        viewBinding.buttonNavigator.visibility = View.VISIBLE
//        findRoute(
//            preferenceManager.getDestination2Lat(),
//            preferenceManager.getDestination2Long(),
//        )
    }

    private fun findRoute(lat: String?, long: String?) {

        MapUtils.findRoute(requireContext(), lat, long, preferenceManager)

//        val currentLocation = navigationLocationProvider.lastLocation
////        val originPoint = originLocation?.let {
////            Point.fromLngLat(it.longitude, it.latitude)
////        } ?: return
//        val bearing = currentLocation?.bearing?.toDouble() ?: run {
//            45.0
//        }
//
//
////        40.383054849711264, 71.78235197671702
//        val originPoint =
//            origin ?: Point.fromLngLat(locationStart.placeLongitude, locationStart.placeLatitude)
//
//
////        val originPoint =  Point.fromLngLat(71.78235197671702, 40.383054849711264)
//        // execute a route request
//        // it's recommended to use the
//        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
//        // that make sure the route request is optimized
//        // to allow for support of all of the Navigation SDK features
//        mapboxNavigation.requestRoutes(
//            RouteOptions.builder()
//                .applyDefaultNavigationOptions()
//                .applyLanguageAndVoiceUnitOptions(requireContext())
//                .coordinatesList(listOf(originPoint, destination))
//
//                // provide the bearing for the origin of the request to ensure
//                // that the returned route faces in the direction of the current user movement
//                .bearingsList(
//                    listOf(
//                        Bearing.builder()
//                            .angle(bearing)
//                            .degrees(45.0)
//                            .build(),
//                        null
//                    )
//                )
//                .layersList(listOf(mapboxNavigation.getZLevel(), null))
//                .build(),
//            object : RouterCallback {
//                override fun onRoutesReady(
//                    routes: List<DirectionsRoute>,
//                    routerOrigin: RouterOrigin
//                ) {
//                    setRouteAndStartNavigation(routes)
//                }
//
//                override fun onFailure(
//                    reasons: List<RouterFailure>,
//                    routeOptions: RouteOptions
//                ) {
//                    // no impl
//                }
//
//                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
//                    // no impl
//                }
//            }
//        )
    }

//    private fun setRouteAndStartNavigation(routes: List<DirectionsRoute>) {
//        // set routes, where the first route in the list is the primary route that
//        // will be used for active guidance
//        mapboxNavigation.setRoutes(routes)
//
//        // start location simulation along the primary route
//        startSimulation(routes.first())
//
//        // show UI elements
//        viewBinding.routeOverview.visibility = View.VISIBLE
////        viewBinding.tripProgressView.visibility = View.VISIBLE
//
//        // move the camera to overview when new route is available
//        navigationCamera.requestNavigationCameraToOverview()
//        isRouteFetched = true
//    }


    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    override fun onStart() {
        super.onStart()


        // register event listeners
//        mapboxNavigation.registerRoutesObserver(routesObserver)
//        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
//        mapboxNavigation.registerLocationObserver(locationObserver)
////        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
//        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

//        if (mapboxNavigation.getRoutes().isEmpty()) {
//            // if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
//            // but we're not simulating yet,
//            // push a single location sample to establish origin
//            mapboxReplayer.pushEvents(
//                listOf(
//                    ReplayRouteMapper.mapToUpdateLocation(
//                        eventTimestamp = 0.0,
//                        point = Point.fromLngLat(-122.39726512303575, 37.785128345296805)
//                    )
//                )
//            )
//            mapboxReplayer.playFirstLocation()
//        }
    }

    override fun onStop() {
        super.onStop()
//        handlerTimer.removeMessages(TIMER_MESSAGE_CODE)


//        if (preferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.COMPLETED) {

        // unregister event listeners to prevent leaks or unnecessary resource consumption
//        mapboxNavigation.unregisterRoutesObserver(routesObserver)
//        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
//        mapboxNavigation.unregisterLocationObserver(locationObserver)
////        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
//        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
//        }
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.register()
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        if (timerManager == null && preferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.STARTED) {
            timerManager = TimerManager(requireContext()) { time, state ->
                updateUITime(time, state)
            }
        }
//        timerManager?.updateCallback = ::updateUITime

    }

    override fun onPause() {
        super.onPause()
        networkMonitor.unregister()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.unregisterReceiver(networkReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopLocationUpdates()

//        if (preferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.COMPLETED) {
//            MapboxNavigationProvider.destroy()
//            mapboxReplayer.finish()
//            if (::maneuverApi.isInitialized) {
//                maneuverApi.cancel()
//            }
//            if (::routeLineApi.isInitialized) {
//                routeLineApi.cancel()
//            }
//            if (::routeLineApi.isInitialized) {
//                routeLineView.cancel()
//            }
//
//        }
        handlerTimer.removeMessages(TIMER_MESSAGE_CODE)


//        speechApi.cancel()
//        voiceInstructionsPlayer.shutdown()
    }

    private fun startDrive() {
        homeViewModel.startDrive()
    }

    private fun clearRouteAndStopNavigation() {
        viewBinding.buttonNavigator.visibility = View.GONE

        // clear
//        mapboxNavigation.setRoutes(listOf())
//        mapboxNavigation.setNavigationRoutes(listOf())

        // stop simulation
        mapboxReplayer.stop()
//        mapboxReplayer.run {
//            stop()
//            clearEvents()
//        }


//        // hide UI elements
//        viewBinding.maneuverView.visibility = View.INVISIBLE
//        viewBinding.routeOverview.visibility = View.INVISIBLE
    }


    private fun arriveOrderUi(response: Resource<MainResponse<Any>>?) {

        response?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    viewBinding.bottomDialog.swipeButton.checkedText =
                        getString(R.string.downloading)
                }

                ResourceState.SUCCESS -> {
                    isOrderStarted = true
//                    startTimerIfOrderStarted()
                    viewBinding.bottomDialog.swipeButton.isChecked = false
                    driverViewModel.arrivedOrder()
                }

                ResourceState.ERROR -> {
                    viewBinding.bottomDialog.swipeButton.isChecked = false
                    driverViewModel.arrivedOrder()
                }
            }
        }

    }

    private fun startOrderUi(response: Resource<MainResponse<Any>>?) {
        response?.let {
            when (response.state) {
                ResourceState.SUCCESS -> {
                    preferenceManager.setFinishedTimeOrder(System.currentTimeMillis())
                    if (!soundPlayed) {
                        soundManager.playSoundJourneyBeginWithBelt()
                        soundPlayed = true
                    }
                    viewBinding.bottomDialog.swipeButton.isChecked = false
                    driverViewModel.startedOrder()
                    if (LocationPermissionUtils.isBasicPermissionGranted(requireContext())
                        && LocationPermissionUtils.isLocationEnabled(requireContext())
                    ) {
                        startDrive()
                    } else {
                        permissionActivityResultLauncher.launch(
                            PermissionCheckActivity.getOpenIntent(
                                context = requireContext(),
                                isPromptMode = false
                            )
                        )
                    }

                }

                ResourceState.ERROR -> {
                    preferenceManager.setFinishedTimeOrder(System.currentTimeMillis())
                    viewBinding.bottomDialog.swipeButton.isChecked = false
                    driverViewModel.startedOrder()
                    if (LocationPermissionUtils.isBasicPermissionGranted(requireContext())
                        && LocationPermissionUtils.isLocationEnabled(requireContext())
                    ) {
                        startDrive()
                    } else {
                        permissionActivityResultLauncher.launch(
                            PermissionCheckActivity.getOpenIntent(
                                context = requireContext(),
                                isPromptMode = false
                            )
                        )
                    }
                }

                ResourceState.LOADING -> {
                    viewBinding.bottomDialog.swipeButton.checkedText =
                        getString(R.string.downloading)
                }
            }
        }
    }

    private fun completeOrderUi(response: Resource<MainResponse<Any>>?) {

        response?.let {
            when (response.state) {
                ResourceState.SUCCESS -> {
//                    showFinishDialog(it)
                }

                ResourceState.LOADING -> {

                }

                ResourceState.ERROR -> {


                    viewBinding.bottomDialog.swipeButton.isChecked = false
                }
            }
        }

    }


    private fun acquireWakeLock() {
        val wakeLock =
            (requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                requireActivity().packageName
            ).also {
                it.setReferenceCounted(false)
            }
        wakeLock?.acquire(TimeUnit.HOURS.toMillis(2))
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }


    private fun startTimer() {

        if (timerManager == null) {
            timerManager = TimerManager(requireContext()) { time, state ->
                updateUITime(time, state)
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        Log.d("masofa", "onLocationChanged: $location")
    }


    override fun onDistanceChanged(distance: Float) {
        Log.d("masofa", "onDistanceChanged: $distance")
        handlerTimer.removeMessages(TIMER_MESSAGE_CODE)

        homeViewModel.startDrive()
        locationTracker.stopLocationUpdates()

    }
}

const val TAG1 = "vaqtlar"