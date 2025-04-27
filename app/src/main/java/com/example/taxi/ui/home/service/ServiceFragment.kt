package com.example.taxi.ui.home.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentServiceBinding
import com.example.taxi.domain.model.tarif.ModeRequest
import com.example.taxi.domain.model.tarif.ModeResponse
import com.example.taxi.ui.home.tarif.ModeToggleInterface
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServiceFragment : Fragment(), ModeToggleInterface {
    private lateinit var viewBinding: FragmentServiceBinding
    private val serviceViewModel: ServiceViewModel by viewModel()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentServiceBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serviceViewModel.serviceResponse.observe(viewLifecycleOwner) { resource ->
            updateUi(resource)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            serviceViewModel.getService()
        }

        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }
    }

    private fun updateUi(resource: Resource<ModeResponse>?) {
        resource?.let {
            when (it.state) {
                ResourceState.LOADING -> {
                    setLoading()
                }

                ResourceState.ERROR -> {

                }
                ResourceState.SUCCESS -> {

                    val adapter = it.data?.data?.let { list ->
                        ServiceAdapter(
                            list = list,
                            modeToggleInterface = this,
                            lifecycleOwner = viewLifecycleOwner
                        )
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        stopLoading()
                        viewBinding.serviceRecyclerView.adapter = adapter
                    }
                }
            }
        }
    }

    private fun stopLoading() {
        viewBinding.serviceRecyclerView.visibility = View.VISIBLE
        viewBinding.shimmerService.stopShimmer()
        viewBinding.shimmerService.visibility = View.INVISIBLE
    }


    private fun setLoading() {
        viewBinding.serviceRecyclerView.visibility = View.INVISIBLE
        viewBinding.shimmerService.visibility = View.VISIBLE
        viewBinding.shimmerService.startShimmer()

    }

    override fun toggle(id: String, title: String, message: String, color: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            DialogUtils.createChangeDialog(
                activity = requireActivity(),
                title = title,
                message = message,
                color = if (color) R.color.tgreen else R.color.tred
            )
            serviceViewModel.setService(ModeRequest(id = id))

        }
    }


}