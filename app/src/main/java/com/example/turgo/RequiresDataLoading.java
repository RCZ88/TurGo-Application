package com.example.turgo;

import android.os.Bundle;

public interface RequiresDataLoading {
    Bundle loadDataInBackground(Bundle input, DataLoading.ProgressCallback log);
    void onDataLoaded(Bundle preloadedData);
    void onLoadingError(Exception error);
}
