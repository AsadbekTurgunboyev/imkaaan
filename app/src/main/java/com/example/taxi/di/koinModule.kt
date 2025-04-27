package com.example.taxi.di

import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.taxi.domain.drive.drivepath.SpeedDiffCalculator
import com.example.taxi.SingleLocationProvider
import com.example.taxi.components.service.LocationProvider
import com.example.taxi.components.service.LocationProviderForHuawei
import com.example.taxi.data.repository.MainRepositoryImpl
import com.example.taxi.db.AppDatabase
import com.example.taxi.domain.drive.DriveLocalityAddService
import com.example.taxi.domain.drive.DriveService
import com.example.taxi.domain.drive.DriveStatAnalyser
import com.example.taxi.domain.drive.currentDrive.EndForgotCalculator
import com.example.taxi.domain.drive.currentDrive.PauseCalculator
import com.example.taxi.domain.drive.drivepath.DrivePathBuilder
import com.example.taxi.domain.drive.drivepath.DrivePathFilter
import com.example.taxi.domain.drive.drivepath.PathAngleDiffChecker
import com.example.taxi.domain.location.LocalityInfoCollector
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.domain.privacyPolicy.PrivacyPolicyService
import com.example.taxi.domain.repository.MainRepository
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.network.NetworkViewModel
import com.example.taxi.repositeries.DriveRepository
import com.example.taxi.socket.SocketMessageProcessor
import com.example.taxi.translate.TranslateApi
import com.example.taxi.ui.check.CheckViewModel
import com.example.taxi.ui.home.HomeViewModel
import com.example.taxi.ui.home.SocketViewModel
import com.example.taxi.ui.home.dashboard.DashboardViewModel
import com.example.taxi.ui.home.driver.DriverViewModel
import com.example.taxi.ui.home.driver.driveReport.DriveReportViewModel
import com.example.taxi.ui.home.history.HistoryViewModel
import com.example.taxi.ui.home.menu.about.AboutViewModel
import com.example.taxi.ui.home.order.OrderViewModel
import com.example.taxi.ui.home.profile.ProfileViewModel
import com.example.taxi.ui.home.service.ServiceViewModel
import com.example.taxi.ui.home.tarif.ModeViewModel
import com.example.taxi.ui.home.transfer.transferhistory.TransferHistoryViewModel
import com.example.taxi.ui.home.transfer.transfermoney.TransferMoneyViewModel
import com.example.taxi.ui.splash.SplashViewModel
import com.example.taxi.utils.ClockUtils
import com.example.taxi.utils.ConversionUtil
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@RequiresApi(Build.VERSION_CODES.N)
val koinModule = module {

    single { DriveService() }
    single { TranslateApi() }

    single { DriveLocalityAddService(get(), get()) }

//    factory { PrivacyPolicyService(get()) }

//    factory { StateViewProvider() }

    factory { LocationProvider(androidContext()) }
    factory { LocationProviderForHuawei(androidContext()) }

    factory { androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    factory { LocationServices.getFusedLocationProviderClient(androidContext()) }
    factory { ClockUtils() }

    factory { ConversionUtil }

    factory { com.example.taxi.utils.SphericalUtil() }

    factory { EndForgotCalculator() }

    factory { DriveRepository(get()) }

//    factory { MapPolyLineCreator() }

    factory { DrivePathBuilder() }

    factory { PauseCalculator() }

    factory { PathAngleDiffChecker() }

    factory { SpeedDiffCalculator() }

    factory { DriveStatAnalyser() }

    factory { LocalityInfoCollector(androidContext()) }

    factory { UserPreferenceManager(androidContext()) }

    single { AppDatabase.invoke(androidContext()) }

    factory { SingleLocationProvider(get()) }

    factory { DrivePathFilter() }

    viewModel { HomeViewModel(get()) }

    viewModel { SplashViewModel(get()) }
    viewModel { HistoryViewModel(getMainResponseUseCase = get()) }


//    viewModel { DashboardViewModel() }

//    viewModel { MyDrivesViewModel(get(), get()) }

//    viewModel { DriveReportViewModel(get(), get(), get(), get(), get(), get()) }

//    viewModel { MyAllDrivesViewModel(get()) }

//    viewModel { CompareViewModel(get(), get()) }
//
//    viewModel { RegisterViewModel(getRegisterResponseUseCase = get()) }
//    viewModel { InputPasswordViewModel(getRegisterResponseUseCase = get()) }
//    viewModel { PersonalDataViewModel(getRegisterResponseUseCase = get()) }
//    viewModel { CarDataViewModel(getRegisterResponseUseCase = get()) }
//
    viewModel { ModeViewModel(getMainResponseUseCase = get()) }
    viewModel { ServiceViewModel(getMainResponseUseCase = get()) }
    viewModel { SocketViewModel(get()) }
    viewModel { AboutViewModel(get()) }
//    viewModel { HomeViewModel(get()) }
    viewModel { OrderViewModel(getMainResponseUseCase = get()) }
    viewModel { DriverViewModel(mainResponseUseCase = get()) }
    viewModel { DashboardViewModel(getMainResponseUseCase = get(), userPreferenceManager = get(),get()) }
    viewModel { TransferMoneyViewModel(getMainResponseUseCase = get()) }
    viewModel { TransferHistoryViewModel(getMainResponseUseCase = get()) }
//    viewModel { SplashViewModel(get()) }
    viewModel { DriveReportViewModel(get(), get(), get(), get()) }
    viewModel{CheckViewModel(get())}
    viewModel{NetworkViewModel(get(),get())}
    viewModel{ProfileViewModel(get())}

//    factory { GetRegisterResponseUseCase(registerRepository = get()) }
    factory { GetMainResponseUseCase(mainRepository = get()) }
    factory { PrivacyPolicyService(get()) }
    single { SocketMessageProcessor(get(), get()) }
//
//    single<RegisterRepository> {
//        RegisterRepositoryImpl(apiService = get())
//    }
//    single { UserPreferenceManager(context = get()) }

    single<MainRepository> {
        MainRepositoryImpl(apiService = get())
    }


//    factory { LocationProvider() }


}