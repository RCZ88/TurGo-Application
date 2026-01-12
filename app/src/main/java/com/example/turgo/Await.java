package com.example.turgo;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.database.DatabaseError;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Await {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 🔥 Universal await for ANY void callback method
    public static <T> T get(Consumer<ObjectCallBack<T>> asyncMethod, Object... args) {
        CompletableFuture<T> future = new CompletableFuture<>();

        asyncMethod.accept(new ObjectCallBack<T>() {
            @Override
            public void onObjectRetrieved(T result) {
                future.complete(result);  // Returns YOUR data!
            }
            @Override
            public void onError(DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        return future.join();  // Blocks + returns result
    }
    public static void awaitVoid(Consumer<ObjectCallBack<Void>> asyncMethod) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        asyncMethod.accept(new ObjectCallBack<Void>() {
            @Override
            public void onObjectRetrieved(Void result) {
                future.complete(null);  // "Done!"
            }

            @Override
            public void onError(DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        future.join();  // BLOCKS until complete
    }
}
