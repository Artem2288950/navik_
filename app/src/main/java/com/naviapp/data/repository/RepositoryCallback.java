package com.naviapp.data.repository;

/**
 * Простой колбэк результата асинхронной операции репозитория.
 * Используется вместо LiveData там, где нужен разовый результат сетевого запроса
 * (например, построение маршрута) — LiveData лучше подходит для потоков данных из Room.
 */
public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
