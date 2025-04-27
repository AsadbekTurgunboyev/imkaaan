package com.example.taxi.ui.home.tarif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.databinding.FragmentTarifBinding
import com.example.taxi.domain.model.tarif.ModeRequest
import com.example.taxi.domain.model.tarif.ModeResponse
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TarifFragment : Fragment(), ModeToggleInterface {

    lateinit var viewBinding: FragmentTarifBinding
    private val modeViewModel: ModeViewModel by viewModel()

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
                        ModeAdapter(
                            list = list,
                            modeToggleInterface = this,
                            lifecycleOwner = viewLifecycleOwner
                        )
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        stopLoading()
                        viewBinding.recyclerViewTarif.adapter = adapter
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
        viewBinding = FragmentTarifBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        modeViewModel.modeResponse.observe(viewLifecycleOwner) { resource ->
            updateUi(resource)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            modeViewModel.getModes()
        }


        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }


    }

    override fun toggle(id: String, title: String, message: String, color: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            DialogUtils.createChangeDialog(
                activity = requireActivity(),
                title = title,
                message = message,
                color = if (color) R.color.tgreen else R.color.tred
            )
            modeViewModel.setModes(ModeRequest(id = id))

        }
    }

    private fun stopLoading() {
        viewBinding.recyclerViewTarif.visibility = View.VISIBLE
        viewBinding.shimmerTarif.stopShimmer()
        viewBinding.shimmerTarif.visibility = View.INVISIBLE
    }


    private fun setLoading() {
        viewBinding.recyclerViewTarif.visibility = View.INVISIBLE
        viewBinding.shimmerTarif.visibility = View.VISIBLE
        viewBinding.shimmerTarif.startShimmer()

    }


}