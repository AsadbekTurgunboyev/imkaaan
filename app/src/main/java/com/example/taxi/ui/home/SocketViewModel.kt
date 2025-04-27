package com.example.taxi.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

class SocketViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val _isConnected = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val _isReadyForWork: MutableLiveData<Boolean> = savedStateHandle.getLiveData("isReadyForWork")
    val isReadyForWork: LiveData<Boolean> get() = _isReadyForWork

    fun setReadyForWork(isOn: Boolean) {
        _isReadyForWork.value = isOn
    }



    private val _exit = MutableLiveData<Boolean>()

    val exit : LiveData<Boolean> get() = _exit
    val isConnected: LiveData<Boolean>
        get() = _isConnected


    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }

    fun setExit(exit: Boolean){
        _exit.postValue(true)
    }

    // Additional functions for socket-related operations can be added here

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}