package com.example.damiantour.stopRoute

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.damiantour.database.dao.LocationDatabaseDao
import com.example.damiantour.mapBox.model.LocationData
import org.junit.Assert
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


internal class StoppedRouteViewModelTest {

    @Rule
    @JvmField
    var mockitoRule: MockitoRule = MockitoJUnit.rule()

    private lateinit var calendar : Calendar
    private lateinit var locationDao : LocationDatabaseDao
    private lateinit var viewModel: StoppedRouteViewModel

    @BeforeEach
    fun setup(){
        locationDao = DefaultLocationDatabaseDao()
        viewModel = StoppedRouteViewModel(locationDao)
        calendar = Mockito.mock(Calendar::class.java)

        viewModel.setCalender(calendar)
    }

    @Test
    fun calculateDistance2PointsTest() {
        val loc1 = LocationData(longitude = 3.7338817119598384, latitude = 51.043714412328235)
        val loc2 = LocationData(longitude = 3.723624944686889, latitude = 51.060453747481304)
        val list = ArrayList<LocationData>()
        list.add(loc1)
        list.add(loc2)
        val distance = viewModel.calculateDistance(list)
        val distanceRounded = distance.roundToInt()
        Assert.assertEquals(2, distanceRounded)

    }

    @Test
    fun calculateDistance4PointsTest() {
        val loc1 = LocationData(longitude = 3.7283134460449214, latitude = 50.985491078590634)
        val loc2 = LocationData(longitude = 3.736445903778076, latitude = 50.994824149515914)
        val loc3 = LocationData(longitude = 3.738698959350586, latitude = 50.99294688082788)
        val loc4 = LocationData(longitude = 3.745543956756592, latitude = 50.99548590238895)
        val list = ArrayList<LocationData>()
        list.add(loc1)
        list.add(loc2)
        list.add(loc3)
        list.add(loc4)
        val distance = viewModel.calculateDistance(list)
        val distanceRounded = distance.roundToInt()
        Assert.assertEquals(2, distanceRounded)

    }

    @Test
    fun calculateSpeed60kmph() {
        //Arrange
        val startTime = 0L
        `when`(calendar.timeInMillis).thenReturn(3600000)
        viewModel.setDistance(60.00)
        //Act
        val kmph = viewModel.calculateSpeed(startTime)
        //Assert
        Assert.assertEquals(60.00,kmph,0.5)
    }

    @Test
    fun calculateSpeed120kmph() {
        //Arrange
        val startTime = 0L
        `when`(calendar.timeInMillis).thenReturn(7200000)
        viewModel.setDistance(120.00)
        //Act
        val kmph = viewModel.calculateSpeed(startTime)
        //Assert
        Assert.assertEquals(60.00,kmph,0.5)
    }
}
//dummy datasource
class DefaultLocationDatabaseDao : LocationDatabaseDao {

    val list = ArrayList<LocationData>()

    override suspend fun insert(location: LocationData) {
        list.add(location)
    }
    override suspend fun clear() {
        list.clear()
    }

    override fun getAllLocationsLiveData(): LiveData<List<LocationData>> {
        return MutableLiveData(list)
    }

    override fun getAllLocations(): List<LocationData> {
        return list
    }
}