package com.example.taxi.domain.model.map

import android.os.Parcel
import android.os.Parcelable

data class MapLocation(var placeLatitude:Double, var placeLongitude:Double): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(placeLatitude)
        parcel.writeDouble(placeLongitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MapLocation> {
        override fun createFromParcel(parcel: Parcel): MapLocation {
            return MapLocation(parcel)
        }

        override fun newArray(size: Int): Array<MapLocation?> {
            return arrayOfNulls(size)
        }
    }
}
