package com.example.taxi.di

import com.example.taxi.data.repository.MainRepositoryImpl
import com.example.taxi.data.repository.RegisterRepositoryImpl
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.domain.repository.MainRepository
import com.example.taxi.domain.repository.RegisterRepository
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.domain.usecase.register.GetRegisterResponseUseCase
import com.example.taxi.network.NetworkViewModel
import com.example.taxi.ui.home.HomeViewModel
import com.example.taxi.ui.home.SocketViewModel
import com.example.taxi.ui.home.dashboard.DashboardViewModel
import com.example.taxi.ui.home.driver.DriverViewModel
import com.example.taxi.ui.home.history.HistoryViewModel
import com.example.taxi.ui.home.order.OrderViewModel
import com.example.taxi.ui.home.service.ServiceViewModel
import com.example.taxi.ui.home.tarif.ModeViewModel
import com.example.taxi.ui.login.cardata.CarDataViewModel
import com.example.taxi.ui.login.inputpassword.InputPasswordViewModel
import com.example.taxi.ui.login.inputphone.RegisterViewModel
import com.example.taxi.ui.login.personaldata.PersonalDataViewModel
import com.example.taxi.ui.login.selfie.SelfieViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

// Declare a lazy-initialized variable that will hold the loaded Koin modules.
fun injectFeature() = loadFeature
private val loadFeature by lazy {
    // Load the modules for the view models, use cases, and repositories.

    loadKoinModules(
        listOf(
            viewModelModule,
            useCaseModule,
            repositoryModule
        )
    )

}

// Declare a module for the view models.
val viewModelModule: Module = module {

    viewModel { RegisterViewModel(getRegisterResponseUseCase = get()) }
    viewModel { InputPasswordViewModel(getRegisterResponseUseCase = get()) }
    viewModel { PersonalDataViewModel(getRegisterResponseUseCase = get()) }
    viewModel { CarDataViewModel(getRegisterResponseUseCase = get()) }
    viewModel { SelfieViewModel(getRegisterResponseUseCase = get()) }
    viewModel { ModeViewModel(getMainResponseUseCase = get()) }
    viewModel { ServiceViewModel(getMainResponseUseCase = get()) }
    viewModel { SocketViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { OrderViewModel(getMainResponseUseCase = get()) }
    viewModel { DriverViewModel(mainResponseUseCase = get()) }
    viewModel { DashboardViewModel(getMainResponseUseCase = get(), userPreferenceManager = get(),get()) }
    viewModel { HistoryViewModel(getMainResponseUseCase = get()) }
    viewModel { NetworkViewModel(getMainResponseUseCase = get(),get()) }
}

// Declare a module for the use cases.
val useCaseModule: Module = module {
    // Declare a factory for the register response use case that depends on a repository.
    factory { GetRegisterResponseUseCase(registerRepository = get()) }
    factory { GetMainResponseUseCase(mainRepository = get()) }

    factory { UserPreferenceManager(androidContext()) }

}

// Declare a module for the repositories.
val repositoryModule: Module = module {
    // Declare a single instance of the register repository that depends on an API service.
    single<RegisterRepository> {
        RegisterRepositoryImpl(apiService = get())
    }

    single<MainRepository> {
        MainRepositoryImpl(apiService = get())
    }


}
