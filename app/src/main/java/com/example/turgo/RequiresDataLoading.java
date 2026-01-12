package com.example.turgo;

import android.os.Bundle;
import android.widget.TextView;

public interface RequiresDataLoading {
    Bundle loadDataInBackground(Bundle input, TextView logLoading);
    void onDataLoaded(Bundle preloadedData);
    void onLoadingError(Exception error);
}
