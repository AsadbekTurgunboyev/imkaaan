package com.example.taxi.ui.home.history

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.taxi.databinding.FragmentHistoryBinding
import com.example.taxi.domain.model.history.HistoryDataResponse
import com.example.taxi.domain.model.history.Meta
import com.example.taxi.ui.home.transfer.transferhistory.CountPageAdapter
import com.example.taxi.ui.home.transfer.transferhistory.SelectPageInterface
import com.example.taxi.utils.DialogUtils
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Optional

class HistoryFragment : Fragment(), SelectPageInterface {

    lateinit var viewBinding: FragmentHistoryBinding

    private val historyViewModel: HistoryViewModel by viewModel()
    var page = 1
    var oldStatus = 0
    var oldType = 0
    var from: String? = null
    var to: String? = null
    var s: Int? = null
    var t: Int? = null
    private var dateRange = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyViewModel.getHistory(page = page)

        historyViewModel.isActiveFilter.observe(viewLifecycleOwner) { isActive ->
            viewBinding.isActiveFilter.visibility = if (isActive) View.VISIBLE else View.GONE
        }

        viewBinding.fbnFilter.setOnClickListener {
            DialogUtils.filterHistoryDialog(requireContext(),
                dataRange = dateRange,
                oldStatus = oldStatus,
                oldType = oldType,
                range = { range, type, status ->
                    dateRange = range as MutableList<String>
                    oldStatus = status
                    oldType = type
                    val typeValue = Optional.ofNullable(if (type != 0) type else null)
                    val statusValue = Optional.ofNullable(if (status != 0) status else null)

                    updateHistory(range, typeValue, statusValue)
                },
                removeFilter = {
                    historyViewModel.getHistory(page)
                    dateRange.clear()
                    oldStatus = 0
                    oldType = 0
                    historyViewModel.isNotActivatedFilter()
                })
                .show()
        }

        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }

        historyViewModel.historyResponse.observe(viewLifecycleOwner) {
            updateDataHistory(it)
        }

    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateHistory(range: MutableList<String>, typeValue: Optional<Int>, statusValue: Optional<Int>) {
        from = if(range.isNullOrEmpty()) null else range[0]
        to = if (range.isNullOrEmpty()) null else range[1]
        t = typeValue.orElse(null)
        s = statusValue.orElse(null)
        historyViewModel.getHistory(1, from = from, to = to, type = t, status = s)

        if (range.isNullOrEmpty() && !typeValue.isPresent && !statusValue.isPresent) {
            historyViewModel.isNotActivatedFilter()
        } else {
            historyViewModel.activatedFilter()
        }
    }

    private fun updateDataHistory(resource: Resource<HistoryDataResponse<Meta>>?) {
        resource?.let {
            when (it.state) {
                ResourceState.ERROR -> {
                    viewBinding.historyRecyclerView.visibility = View.GONE

                    viewBinding.shimmerOrder.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                }
                ResourceState.SUCCESS -> {
                    viewBinding.historyRecyclerView.visibility = View.VISIBLE

                    Log.d("tekshirish", "updateDataHistory: ${it.data?.data}")
                    viewBinding.shimmerOrder.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    page = it.data?._meta?.currentPage ?: 0
                    val dataList = it.data?.data.orEmpty()

                    if (dataList.isEmpty()){
                        viewBinding.noOrder.visibility = View.VISIBLE
                    }
                    it.data?._meta?.pageCount?.let { pageCount ->
                        viewBinding.recylerPageCount.adapter = CountPageAdapter(pageCount, page, this)
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewBinding.historyRecyclerView.adapter = withContext(Dispatchers.Default) {
                            HistoryAdapter(dataList)
                        }
                    }
                }
                ResourceState.LOADING -> {
                    viewBinding.historyRecyclerView.visibility = View.GONE
                    viewBinding.shimmerOrder.apply {
                        startShimmer()
                        visibility = View.VISIBLE
                    }
                    viewBinding.noOrder.visibility = View.INVISIBLE
                }
            }
        }

    }



    override fun setPageCount(page: Int) {
        historyViewModel.getHistory(page = page, from = from, to = to, type = t, status = s)

    }

}