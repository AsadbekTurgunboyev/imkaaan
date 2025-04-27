package com.example.taxi.ui.home.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.databinding.FragmentMessageBinding
import com.example.taxi.domain.model.message.MessageResponse
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.dashboard.DashboardViewModel
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MessageFragment : Fragment() {
    private val dashboardViewModel: DashboardViewModel by sharedViewModel()
    lateinit var viewBinding: FragmentMessageBinding

    val userPreferenceManager: UserPreferenceManager by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentMessageBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.getMessage()

        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }
        dashboardViewModel.messageResponse.observe(viewLifecycleOwner) {
            updateMessageUi(it)
        }
    }

    private fun updateMessageUi(resource: Resource<MessageResponse>?) {
        resource?.let {
            when(it.state){
                ResourceState.LOADING ->{
                    startLoading()
                }
                ResourceState.SUCCESS ->{
                    stopLoading()
                    viewBinding.messageRecyclerView.adapter = it.data?.data?.let { it1 ->
                         MessageAdapter(
                            it1
                        )
                    }
                    userPreferenceManager.saveMessageCount(0)

                }
                ResourceState.ERROR ->{
                    stopLoading()
                }
            }
        }
    }

    private fun stopLoading(){
        viewBinding.messageRecyclerView.visibility = View.VISIBLE

        with(viewBinding.shimmerOrder){
            stopShimmer()
            visibility = View.GONE
        }
    }
    private fun startLoading(){
        viewBinding.messageRecyclerView.visibility = View.GONE
        viewBinding.shimmerOrder.apply {
            startShimmer()
            visibility = View.VISIBLE
        }
    }

}