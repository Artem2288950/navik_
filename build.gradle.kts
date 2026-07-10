// Top-level build file. Здесь только объявление версий плагинов —
// весь код приложения написан на Java, Kotlin DSL используется исключительно
// для конфигурации сборки, как и требовалось.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
