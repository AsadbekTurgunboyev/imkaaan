package com.example.taxi.ui.home.menu.about

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.databinding.FragmentAboutBinding
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.about.ResponseAbout
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import org.koin.androidx.viewmodel.ext.android.viewModel


class AboutFragment : Fragment() {

    lateinit var viewBinding: FragmentAboutBinding
    private val aboutViewModel : AboutViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentAboutBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.webview.loadUrl("https://bekjaantaxi.uz/policy")

        viewBinding.webview.webViewClient = WebViewClient()
        val webSettings = viewBinding.webview.settings
        webSettings.javaScriptEnabled = true
        viewBinding.fbnBackHome.setOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }

        aboutViewModel.getAbout()

//        aboutViewModel.about.observe(viewLifecycleOwner){
//            getAboutData(it)
//        }
    }

    private fun getAboutData(resource: Resource<MainResponse<ResponseAbout>>) {
       when(resource.state){
           ResourceState.ERROR ->{
               Log.d("olish", "getAboutData: xato")
           }
           ResourceState.SUCCESS ->{
               val manufacturer = Build.MANUFACTURER

               val content = resource.data?.data?.content
               if (manufacturer != null && manufacturer.equals("Huawei", ignoreCase = true)) {
                   // This is a Huawei device
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                       viewBinding.contentTxt.text = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY)
                   } else {
                       viewBinding.contentTxt.text = Html.fromHtml(content)
                   }

                   viewBinding.contentTxt.visibility = View.VISIBLE
                   viewBinding.webview.visibility = View.GONE
               } else {
                   // This is not a Huawei device
                   content?.let {
                       viewBinding.webview.loadDataWithBaseURL(null,
                           it,"text/html", "UTF-8", null)
                   }
                  viewBinding.contentTxt.visibility = View.GONE
                   viewBinding.webview.visibility = View.VISIBLE
               }



//               resource.data?.data?.content?.let { viewBinding.webview.loadData(it,"text/html","UTF-8") }
           }
           ResourceState.LOADING ->{}
       }
    }
}