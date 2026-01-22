package com.example.turgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DataLoading extends AppCompatActivity {
    private TextView tv_statusText;
    private ProgressBar pb_loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tv_statusText = findViewById(R.id.tv_ADL_LoadingText);
        pb_loading = findViewById(R.id.pb_ADL_LoadingCircle);
        startLoading();
    }

    public interface ProgressCallback {
        void onProgress(String status);
    }

    private void startLoading(){
        tv_statusText.setText("Loading Data...");

        boolean isFragment = getIntent().getBooleanExtra(DLIntentMessage.EXTRA_IS_FRAGMENT.getIntentMessage(), false);
        Class<? extends RequiresDataLoading>targetClass = (Class<? extends RequiresDataLoading>) (isFragment?
                getIntent().getSerializableExtra(DLIntentMessage.EXTRA_TARGET_FRAGMENT.getIntentMessage())
                : getIntent().getSerializableExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage()));
        Bundle input = getIntent().getBundleExtra(DLIntentMessage.EXTRA_LOADING_INPUT.getIntentMessage());
        User user = (User) getIntent().getSerializableExtra(DLIntentMessage.EXTRA_TARGET_USER.getIntentMessage());
        int selectedBottomNav = getIntent().getIntExtra(DLIntentMessage.EXTRA_TARGET_BOTTOM_NAV.getIntentMessage(), -1);
        Log.d("selectedBottomNav", "on DataLoading - startLoading(): " + selectedBottomNav);
        new Thread(()->{
            if(!RequiresDataLoading.class.isAssignableFrom(targetClass)){
                throw new IllegalArgumentException(
                        targetClass + " must implement RequiresDataLoading"
                );
            }
            RequiresDataLoading loader = null;
            try {
                loader = targetClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
            ProgressCallback callback = status -> runOnUiThread(() ->
                    tv_statusText.setText(status));

            Bundle preloadedData = loader.loadDataInBackground(input, callback);

            runOnUiThread(()->{
                try{
                    if(isFragment){
                        loadFragment(preloadedData, targetClass, user, selectedBottomNav);
                    }else{
                        loadActivity(preloadedData, targetClass, user);
                    }
                }catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        }).start();
    }

    private void loadActivity(Bundle bundle, Class<?> targetClass, User user) throws ClassNotFoundException {
        Intent intent = new Intent(this, targetClass);
        intent.putExtras(bundle);
        intent.putExtra("FullUserObject", user);
        startActivity(intent);
        finish();
    }
    private void loadFragment(Bundle bundle, Class<? extends RequiresDataLoading> targetFragment, User user, int bottomNavMenuId) throws ClassNotFoundException {
        Class<? extends AppCompatActivity> containerActivity = (Class<? extends AppCompatActivity>)getIntent().getSerializableExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage());
        Intent intent = new Intent(this, containerActivity);
        intent.putExtras(bundle);
        intent.putExtra("FullUserObject", user);
        intent.putExtra("FragmentToLoad", targetFragment);
        if(bottomNavMenuId!= -1){
            intent.putExtra("BottomNavToSelect", bottomNavMenuId);
        }
        startActivity(intent);
        finish();
    }
    public static void loadAndNavigate(Context context, Class<? extends RequiresDataLoading>view, Bundle input, boolean isFragment, Class<? extends AppCompatActivity> activityContainer, User user){
        Toast.makeText(context, "Loading And Navigating (DATA LOADING)", Toast.LENGTH_SHORT).show();
        Log.d("DataLoading", "Loading and Navigating to View: " + view.getSimpleName());
        Intent intent = new Intent(context, DataLoading.class);

        intent.putExtra(DLIntentMessage.EXTRA_LOADING_INPUT.getIntentMessage(), input);
        intent.putExtra(DLIntentMessage.EXTRA_IS_FRAGMENT.getIntentMessage(), isFragment);
        if(isFragment){
            intent.putExtra(DLIntentMessage.EXTRA_TARGET_FRAGMENT.getIntentMessage(), view);
        }
        Class<?> activity = isFragment? activityContainer : view;
        intent.putExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage(), activity);
        intent.putExtra(DLIntentMessage.EXTRA_TARGET_USER.getIntentMessage(), user);
        context.startActivity(intent);
    }
    public static void loadAndNavigate(Context context, Class<? extends RequiresDataLoading>view, Bundle input, boolean isFragment, Class<?> activityContainer, User user, int ofBottomNav){
        Toast.makeText(context, "Loading And Navigating (DATA LOADING)", Toast.LENGTH_SHORT).show();
        Log.d("DataLoading", "Loading and Navigating to View: " + view.getSimpleName());
        Intent intent = new Intent(context, DataLoading.class);

        intent.putExtra(DLIntentMessage.EXTRA_LOADING_INPUT.getIntentMessage(), input);
        intent.putExtra(DLIntentMessage.EXTRA_IS_FRAGMENT.getIntentMessage(), isFragment);
        if(isFragment){
            intent.putExtra(DLIntentMessage.EXTRA_TARGET_FRAGMENT.getIntentMessage(), view);
            Log.d("selectedBottomNav", "on DataLoading - loadAndNavigate(): " + ofBottomNav);
            intent.putExtra(DLIntentMessage.EXTRA_TARGET_BOTTOM_NAV.getIntentMessage(), ofBottomNav);
        }
        Class<?> activity = isFragment?activityContainer : view;
        intent.putExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage(), activity);
        intent.putExtra(DLIntentMessage.EXTRA_TARGET_USER.getIntentMessage(), user);

        context.startActivity(intent);
    }
}