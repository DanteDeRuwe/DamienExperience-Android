package com.example.damiantour.mapBox

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.damiantour.mapBox.model.LocationData
import com.example.damiantour.mapBox.model.Tuple

/**
 * @author Simon Bettens
 */
object LocationUtils{

    // a list of locations
    private var _tempLocations = MutableLiveData<MutableList<LocationData>>()
    val tempLocations: LiveData<MutableList<LocationData>>
        get() = _tempLocations

    /**
     * mehod that inits the list
     */
    fun start(){
        _tempLocations.value = ArrayList()
    }

    fun getTempLocationList() :  LiveData<MutableList<LocationData>> {
        return _tempLocations
    }

    fun postNewLocation(loc : Location?){
        val list = this._tempLocations.value!!
        if (loc != null) {
            list.add(LocationData(longitude = loc.longitude,latitude = loc.latitude))
        }
        _tempLocations.postValue( list)
    }

    /**
     * @author Simon
     * clears the temporay list of locations
     */
    fun resetCurrentTempLocations() {
        println("reset list")
        val list = this._tempLocations.value
        if(list!=null) {
            this._tempLocations.value!!.clear()
        }
    }

    fun tempLocationsSize(): Int {
        return this._tempLocations.value!!.size
    }

}