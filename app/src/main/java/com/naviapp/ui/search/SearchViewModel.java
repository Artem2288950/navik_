package com.naviapp.ui.search;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.naviapp.data.model.Place;
import com.naviapp.data.repository.RepositoryCallback;
import com.naviapp.data.repository.SearchRepository;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private final SearchRepository repository = new SearchRepository();

    private final MutableLiveData<List<Place>> results = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<Place>> getResults() {
        return results;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void search(@NonNull String query, Double biasLat, Double biasLon, String languageCode) {
        if (query.trim().isEmpty()) {
            return;
        }
        loading.setValue(true);
        repository.search(query, biasLat, biasLon, languageCode, new RepositoryCallback<List<Place>>() {
            @Override
            public void onSuccess(List<Place> result) {
                loading.postValue(false);
                results.postValue(result);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}
