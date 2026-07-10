package com.naviapp.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/**
 * Обёртка над Android TextToSpeech для озвучивания пошаговых инструкций навигации.
 * Используется системный TTS-движок — бесплатно, работает офлайн после установки
 * голосовых пакетов, не требует сторонних сервисов.
 */
public class TtsHelper {

    private TextToSpeech tts;
    private boolean ready = false;

    public TtsHelper(Context context, String languageTag) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = Locale.forLanguageTag(languageTag);
                int result = tts.setLanguage(locale);
                ready = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
    }

    public void speak(String text) {
        if (ready && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
