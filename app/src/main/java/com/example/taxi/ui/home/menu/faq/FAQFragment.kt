package com.example.taxi.ui.home.menu.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.databinding.FragmentFAQBinding
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.about.ResponseAbout
import com.example.taxi.ui.home.menu.about.AboutViewModel
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import org.koin.androidx.viewmodel.ext.android.viewModel

class FAQFragment : Fragment() {
    lateinit var viewBinding: FragmentFAQBinding
    val aboutViewModel: AboutViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentFAQBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.webview.webViewClient = WebViewClient()
        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }

        aboutViewModel.getFAQ()
        aboutViewModel.faq.observe(viewLifecycleOwner) {
            getAboutData(it)
        }
    }

    private fun getAboutData(resource: Resource<MainResponse<ResponseAbout>>) {
        when (resource.state) {
            ResourceState.ERROR -> {}
            ResourceState.SUCCESS -> {
                resource.data?.data?.content?.let {
                    viewBinding.webview.loadDataWithBaseURL(null, it, "text/html", "UTF-8", null)
                }
//                resource.data?.data?.content?.let { viewBinding.webview.loadData(it,"text/html","UTF-8") }
            }
            ResourceState.LOADING -> {}
        }
    }

}