package com.example.scorpapp.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scorpapp.source.DataSource
import com.example.scorpapp.source.Person
import com.example.scorpapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(private val dataSource: DataSource) : ViewModel() {
    var people: MutableLiveData<Resource<List<Person>>> = MutableLiveData()
    var nextKey: String? = null

    init {
        getPeople()
    }

    fun getPeople() {
        viewModelScope.launch {
            people.postValue(Resource.Loading())
            Log.i("_PersonViewModel", "fetching... $nextKey")
            dataSource.fetch(nextKey) { fetchResponse, fetchError ->
                run {
                    if (fetchResponse != null) {
                        nextKey = fetchResponse.next
                        val data = fetchResponse.people
                        people.postValue(Resource.Success(data, nextKey))
                    } else if (fetchError != null) {
                        people.postValue(Resource.Error(fetchError.errorDescription))
                    }
                }
            }
        }
    }
}