package com.example.taxi.ui.login.cardata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.model.register.car_data.CarData

class CarViewModel : ViewModel() {
    private val _carId = MutableLiveData<Int?>()
    val carId: MutableLiveData<Int?> = _carId
    private val _carModel = MutableLiveData<CarData?>()
    val carModel : MutableLiveData<CarData?> = _carModel

    private val _colorId = MutableLiveData<Int?>()
    val colorId: MutableLiveData<Int?> = _colorId

    fun setCarId(id: Int?) {
        _carId.value = id
    }
    fun setCarModel(model : CarData?){
        _carModel.value = model
    }

    fun setColorId(id: Int?) {
        _colorId.value = id
    }
}
