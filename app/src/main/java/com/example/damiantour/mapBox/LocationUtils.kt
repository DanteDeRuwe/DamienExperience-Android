package com.example.damiantour.mapBox

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * @author Simon Bettens
 */
object LocationUtils{

    // a list of locations
    private var _tempLocations = MutableLiveData<MutableList<Tuple>>()
    val tempLocations: LiveData<MutableList<Tuple>>
        get() = _tempLocations

    /**
     * mehod that inits the list
     */
    fun start(){
        _tempLocations.value = ArrayList()
    }

    fun getTempLocationList() :  LiveData<MutableList<Tuple>> {
        return _tempLocations
    }

    fun postNewLocation(loc : Location?){
        val list = this._tempLocations.value!!
        if (loc != null) {
            list.add(Tuple(longitude = loc.longitude,latitude = loc.latitude))
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