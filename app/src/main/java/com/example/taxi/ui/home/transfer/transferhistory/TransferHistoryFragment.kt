package com.example.taxi.ui.home.transfer.transferhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.databinding.FragmentTransferHistoryBinding
import com.example.taxi.domain.model.transfer.HistoryMeta
import com.example.taxi.domain.model.transfer.ResponseTransferHistory
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferHistoryFragment : Fragment(), SelectPageInterface {


    lateinit var viewBinding: FragmentTransferHistoryBinding
    private val transferHistoryViewModel: TransferHistoryViewModel by viewModel()
    var page = 1
    var typeValues = 0
    var dateRange = mutableListOf<String>("","")

    var t: Int? = null
    var from: String? = null
    var to: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentTransferHistoryBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transferHistoryViewModel.getHistoryFromPage(page)

        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }

        viewBinding.filterButton.setOnClickListener {
            DialogUtils.filterDialog(requireContext(),
                dataRange = dateRange,
                typeOld = typeValues,
                range = { range, type ->
                    dateRange = range as MutableList<String>
                    typeValues = type
                    t  = if (type != 0 ) type else null
                    from = if (range.isNullOrEmpty()) null else range[0]
                    to = if (range.isNullOrEmpty()) null else range[1]
                    transferHistoryViewModel.getHistoryFromPage(1, from = from, to = to, type = t)
                    transferHistoryViewModel.activatedFilter()

                }, removeFilter = {
                    transferHistoryViewModel.getHistoryFromPage(page)
                    dateRange.clear()
                    typeValues = 0
                    transferHistoryViewModel.isNotActivatedFilter()
                }).show()
        }

        transferHistoryViewModel.isActiveFilter.observe(viewLifecycleOwner) { isActive ->
            viewBinding.isActiveFilter.visibility = if (isActive) View.VISIBLE else View.GONE
        }


        transferHistoryViewModel.historyTransfer.observe(viewLifecycleOwner) {
            updateData(it)
        }


    }

    private fun updateData(resource: Resource<ResponseTransferHistory<HistoryMeta>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.ERROR -> {
                    viewBinding.historyTransferRecyclerView.visibility = View.GONE
                    viewBinding.shimmerHistory.stopShimmer()
                    viewBinding.shimmerHistory.visibility = View.GONE
                }
                ResourceState.SUCCESS -> {
                    viewBinding.historyTransferRecyclerView.visibility = View.VISIBLE
                    viewBinding.shimmerHistory.stopShimmer()
                    viewBinding.shimmerHistory.visibility = View.GONE

                    page = it.data?.meta?.currentPage ?: 0
                    val dataList = it.data?.data.orEmpty()

                    if (dataList.isEmpty()){
                        viewBinding.noOrder.visibility = View.VISIBLE
                    }

                    it.data?.meta?.pageCount?.let { pageCount ->
                        viewBinding.recylerPageCount.adapter = CountPageAdapter(pageCount, page, this)
                    }

                    viewLifecycleOwner.lifecycleScope.launch {
                        viewBinding.historyTransferRecyclerView.adapter = withContext(Dispatchers.Default) {
                            TransferHistoryAdapter(dataList)
                        }
                    }

                }
                ResourceState.LOADING -> {
                    viewBinding.historyTransferRecyclerView.visibility = View.GONE
                    viewBinding.shimmerHistory.startShimmer()
                    viewBinding.noOrder.visibility = View.GONE

                    viewBinding.shimmerHistory.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun setPageCount(page: Int) {
        transferHistoryViewModel.getHistoryFromPage(page, from = from, to = to, type = t)

    }


}