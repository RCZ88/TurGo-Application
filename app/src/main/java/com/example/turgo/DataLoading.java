package com.example.turgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private void startLoading(){
        tv_statusText.setText("Loading Data...");

        boolean isFragment = getIntent().getBooleanExtra(DLIntentMessage.EXTRA_IS_FRAGMENT.getIntentMessage(), false);
        String targetClassName = isFragment?
                getIntent().getStringExtra(DLIntentMessage.EXTRA_TARGET_FRAGMENT.getIntentMessage())
                : getIntent().getStringExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage());
        Bundle input = getIntent().getBundleExtra(DLIntentMessage.EXTRA_LOADING_INPUT.getIntentMessage());
        new Thread(()->{
            try{
                Class<?> targetClass = Class.forName("com.example.turgo." + targetClassName);
                if(RequiresDataLoading.class.isAssignableFrom(targetClass)){
                    throw new IllegalArgumentException(
                            targetClassName + " must implement RequiresDataLoading"
                    );
                }
                RequiresDataLoading loader = (RequiresDataLoading) targetClass.newInstance();

                Bundle preloadedData = loader.loadDataInBackground(input, tv_statusText);

                runOnUiThread(()->{
                    try{
                        if(isFragment){
                            loadFragment(preloadedData, targetClassName);
                        }else{
                            loadActivity(preloadedData, targetClassName);
                        }
                    }catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                Log.e("DataLoadingActivity", "Loading failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Failed to load: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadActivity(Bundle bundle, String targetClass) throws ClassNotFoundException {
        Intent intent = new Intent(this, Class.forName(targetClass));
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
    private void loadFragment(Bundle bundle, String targetFragment) throws ClassNotFoundException {
        String containerActivity = getIntent().getStringExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage());
        Intent intent = new Intent(this, Class.forName(containerActivity));
        intent.putExtras(bundle);
        intent.putExtra("FragmentToLoad", targetFragment);
        startActivity(intent);
        finish();
    }
    public static void loadAndNavigate(Context context, Class<? extends RequiresDataLoading>view, Bundle input, boolean isFragment, String activityContainer){
        Intent intent = new Intent(context, DataLoading.class);

        intent.putExtra(DLIntentMessage.EXTRA_LOADING_INPUT.getIntentMessage(), input);
        intent.putExtra(DLIntentMessage.EXTRA_IS_FRAGMENT.getIntentMessage(), isFragment);
        if(isFragment){
            intent.putExtra(DLIntentMessage.EXTRA_TARGET_FRAGMENT.getIntentMessage(), view.getSimpleName());
        }
        String activityName = isFragment?activityContainer : view.getSimpleName();
        intent.putExtra(DLIntentMessage.EXTRA_TARGET_ACTIVITY.getIntentMessage(), activityName);

        context.startActivity(intent);
    }
}