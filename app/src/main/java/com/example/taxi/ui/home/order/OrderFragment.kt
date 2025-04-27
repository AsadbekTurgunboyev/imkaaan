package com.example.taxi.ui.home.order

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.SingleLocationProvider
import com.example.taxi.databinding.FragmentOrderBinding
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.map.MapLocation
import com.example.taxi.domain.model.order.Address
import com.example.taxi.domain.model.order.MileageData
import com.example.taxi.domain.model.order.OrderAccept
import com.example.taxi.domain.model.order.OrderData
import com.example.taxi.domain.model.order.UserModel
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.driver.DriverViewModel
import com.example.taxi.utils.*
import com.example.taxi.utils.ConstantsUtils.locationDestination
import com.example.taxi.utils.ConstantsUtils.locationDestination2
import com.example.taxi.utils.ConstantsUtils.locationStart
import com.example.taxi.utils.LocationHandler.isGpsEnabled
import com.example.taxi.utils.LocationHandler.isLocationApproved
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class OrderFragment : Fragment(), BottomSheetInterface {

    lateinit var viewBinding: FragmentOrderBinding
    private val orderViewModel: OrderViewModel by sharedViewModel()
    private lateinit var orderAdapter: OrderAdapter
    private val driverViewModel: DriverViewModel by sharedViewModel()
    lateinit var singleLocationProvider: SingleLocationProvider
    private val preferenceManager: UserPreferenceManager by inject()
    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val handler = CoroutineExceptionHandler { _, exception ->
        isRouteFetched = false
    }
    var dialog: Dialog? = null

    var isNewOrder: Boolean = false

    private var destLang: Double? = 0.0
    private var destLong: Double? = 0.0

    private var isRouteFetched = false

    private var myCurrentAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var orderId: Int = -1
        var lat1: String? = null
        var long1: String? = null
        var lat2: String? = null
        var long2: String? = null

        arguments?.let {
            orderId = it.getInt("order_id", -1)
            lat1 = it.getString("lat1")
            long1 = it.getString("long1")
            lat2 = it.getString("lat2")
            long2 = it.getString("long2")
        }

        if (orderId > 0 && lat1 != null && long1 != null) {
            acceptOrder(
                id = orderId,
                latitude1 = lat1!!,
                longitude1 = long1!!,
                latitude2 = lat2,
                longitude2 = long2
            )
        }


    }

    private fun acceptedOrder(event: Event<Resource<MainResponse<OrderAccept<UserModel, MileageData>>>>) {

        event.getContentIfNotHandled()?.let { resource ->

            when (resource.state) {
                ResourceState.LOADING -> {}

                ResourceState.ERROR -> {

                    dialog?.dismiss()
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

                }

                ResourceState.SUCCESS -> {

                    dialog?.dismiss()
                    preferenceManager.clearPath()
                    val orderSettings = resource.data?.data
                    orderSettings?.let { it1 -> preferenceManager.savePriceSettings(it1) }

                    if (resource.data?.data?.latitude2 != null && resource.data.data.longitude2 != null) {
                        locationDestination2 = MapLocation(
                            resource.data.data.latitude2!!.toDouble(),
                            resource.data.data.longitude2!!.toDouble()
                        )

                    }
                    preferenceManager.setDriverStatus(UserPreferenceManager.DriverStatus.ACCEPTED)
                    driverViewModel.acceptedOrder()
                    val navController = findNavController()
                    orderViewModel.clearAcceptOrderData()
                    changeDriveStatus()
                    if (dialog?.isShowing == true) {
                        dialog?.cancel()
                        dialog?.dismiss()
                    }

                    navController.navigate(R.id.driverFragment)


                }
            }
        }
    }

    private fun changeDriveStatus() {
        // When drive status changes
        val intent = Intent("com.example.SEND_TO_SOCKET")
        intent.putExtra("data", "Your data here")
        requireContext().sendBroadcast(intent)
    }

    @SuppressLint("MissingPermission")
    private fun updateUi(resource: Resource<MainResponse<List<OrderData<Address>>>>?) {
        resource?.let {
            when (resource.state) {
                ResourceState.LOADING -> {
                    shimmerLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        if (it.data.isEmpty()) {
                            noOrderShow()
                        } else {
                            fusedLocationClient =
                                LocationServices.getFusedLocationProviderClient(
                                    requireActivity()
                                )
                            fusedLocationClient!!.lastLocation.addOnSuccessListener { location ->
                                receiveItemFromSocket(it.data, location)

                            }

                            orderShow()

                        }
                    }
                }

                ResourceState.ERROR -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        DialogUtils.createChangeDialog(
                            activity = requireActivity(),
                            title = "Xatolik!",
                            message = resource.message,
                            color = R.color.tred
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentOrderBinding.inflate(layoutInflater, container, false)

        return viewBinding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (dialog == null) {
            setDialog()
        }

        getOrderData()

        orderViewModel.isNewOrder.observe(viewLifecycleOwner) {
            isNewOrder = it
        }
        orderViewModel.orderResponse.observe(viewLifecycleOwner) {
            updateUi(it)
        }

        viewBinding.buttonSort.setOnClickListener {
            orderAdapter.sortWithKm()
        }

        val locationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        singleLocationProvider = SingleLocationProvider(locationManager)


        orderViewModel.acceptOrder.observe(viewLifecycleOwner) {
            acceptedOrder(it)
        }


        with(viewBinding) {

            fbnBackHome.setOnClickListener {
                val navController = findNavController()
                navController.navigateUp()
            }

        }
    }

    private fun getOrderData() {
        orderViewModel.getOrders()
    }


    private fun setDialog() {
        dialog = Dialog(requireContext())
        dialog?.setContentView(R.layout.dialog_loading)
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }
//        dialog?.setCancelable(false)
    }

    private fun noOrderShow() {
        with(viewBinding) {
            noOrder.visibility = View.VISIBLE
            recyclerViewOrder.visibility = View.GONE
            shimmerOrder.stopShimmer()
            shimmerOrder.visibility = View.GONE
        }
    }

    private fun orderShow() {
        with(viewBinding) {
            noOrder.visibility = View.GONE
            recyclerViewOrder.visibility = View.VISIBLE
            shimmerOrder.stopShimmer()
            shimmerOrder.visibility = View.GONE
        }
    }

    private fun shimmerLoading() {
        with(viewBinding) {
            noOrder.visibility = View.GONE
            recyclerViewOrder.visibility = View.GONE
            shimmerOrder.startShimmer()
            shimmerOrder.visibility = View.VISIBLE
        }
    }

    private fun receiveItemFromSocket(
        orderData: List<OrderData<Address>>,
        location: Location
    ) {

        val soundPlayer = SoundPlayer(SoundPlayer.SoundType.LowSound, requireActivity())
        CoroutineScope(Dispatchers.Main).launch {
            // Execute addItemToRecyclerView in parallel with playSound
            val addItemJob = async(Dispatchers.IO) {
                activity?.runOnUiThread {
                    orderAdapter = OrderAdapter(
                        orderData,
//                                    singleLocationProvider.getLocation(requireContext()),
                        location,
                        this@OrderFragment,
                        getAppearance()
                    )
                    viewBinding.recyclerViewOrder.adapter = orderAdapter

//                    orderAdapter.addItemToRecyclerView(orderItem = orderData)
                }
            }
            val playSoundJob = async(Dispatchers.IO) {
                if (isNewOrder) {
                    soundPlayer.playSound()

                }
            }

            // Await both jobs to complete
            addItemJob.await()
            playSoundJob.await()

            // Update the UI after the coroutines have completed
            // Perform any additional UI-related operations here
        }

    }

    private fun getAppearance(): Int {
        return when(preferenceManager.getOrderAppearance()){
            UserPreferenceManager.OrderAppearance.LARGE -> R.layout.item_order
            UserPreferenceManager.OrderAppearance.SMALL -> R.layout.item_order_small
        }
    }

    override fun showBottom(orderData: OrderData<Address>, distance: String) {

        val myBottomSheet = MyBottomSheet(
            requireContext(),
            R.style.BottomSheetDialogTheme,
            orderData = orderData,
            distance = distance,
            bottomSheetInterface = this
        )
        val bottomView =
            LayoutInflater.from(context).inflate(R.layout.bottom_sheet_order_accepted, null)
        myBottomSheet.setContentView(bottomView)
        myBottomSheet.show()

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient!!.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    locationStart.placeLatitude = location.latitude
                    locationStart.placeLongitude = location.longitude

                }
            }

    }


    override fun acceptOrder(
        id: Int,
        latitude1: String,
        longitude1: String,
        latitude2: String?,
        longitude2: String?
    ) {

        if (dialog == null) {
            setDialog()
        }
        dialog?.show()
        if (requireActivity().isLocationApproved()) {
            if (requireActivity().isGpsEnabled()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    getCurrentLocation()
                }, 300)
            } else {
                dialog?.dismiss()
                try {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {

                }
            }

        } else {
            dialog?.dismiss()
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LocationHandler.LOCATION_PERMISSION
            )
        }

        onclickMethod(
            id = id,
            latitude = latitude1.toDouble(),
            longitude = longitude1.toDouble(),
            latitude2 = latitude2?.toDouble(),
            longitude2 = longitude2?.toDouble()
        )

    }


    private fun stopStandardLocationUpdates() {
        fusedLocationClient?.let {
            locationCallback?.let { it1 -> it.removeLocationUpdates(it1) }
            locationCallback = null
            fusedLocationClient = null
        }
    }

    private fun stopHuaweiLocationUpdates() {
        fusedLocationClient?.let {
            locationCallback?.let { it1 -> it.removeLocationUpdates(it1) }
            locationCallback = null
            fusedLocationClient = null
        }
    }


    private fun onclickMethod(
        id: Int,
        latitude: Double,
        longitude: Double,
        latitude2: Double?,
        longitude2: Double?
    ) {

        locationDestination = MapLocation(latitude, longitude)
        if (locationDestination2 != null) {
            locationDestination2 =
                latitude2?.let { longitude2?.let { it1 -> MapLocation(it, it1) } }
        }

        if (preferenceManager.getOrderId() != id){
            orderViewModel.acceptOrder(id)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
        orderViewModel.clearOrder()

        stopStandardLocationUpdates()
        stopHuaweiLocationUpdates()
    }


}