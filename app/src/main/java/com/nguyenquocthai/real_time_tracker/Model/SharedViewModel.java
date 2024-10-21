package com.nguyenquocthai.real_time_tracker.Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<LatLng> locationData = new MutableLiveData<>();
    //    private final MutableLiveData<YourOtherDataType> otherData = new MutableLiveData<>();

    public void setLocationData(LatLng latLng) {
        locationData.setValue(latLng);
    }

    public LiveData<LatLng> getLocationData() {
        return locationData;
    }
    //    Dành cho loại dữ liệu khác
    //    public void setOtherData(YourOtherDataType data) {
    //        otherData.setValue(data);
    //    }
    //
    //    public LiveData<YourOtherDataType> getOtherData() {
    //        return otherData;
    //    }
}
