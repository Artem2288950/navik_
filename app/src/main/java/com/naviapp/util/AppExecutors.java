package com.naviapp.util;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Лёгкая замена RxJava/Coroutines для фоновых задач (запросы к Room и сети).
 * Один ограниченный пул потоков достаточен для навигационного приложения
 * и не создаёт лишней нагрузки на слабых устройствах вроде Galaxy A02.
 */
public final class AppExecutors {

    private static volatile AppExecutors instance;

    private final ExecutorService diskAndNetworkExecutor;
    private final Handler mainThreadHandler;

    private AppExecutors() {
        // 2 потока: один под БД, один под сеть — достаточно для приложения такого масштаба
        diskAndNetworkExecutor = Executors.newFixedThreadPool(2);
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    public void runInBackground(Runnable runnable) {
        diskAndNetworkExecutor.execute(runnable);
    }

    public void runOnMainThread(Runnable runnable) {
        mainThreadHandler.post(runnable);
    }
}
