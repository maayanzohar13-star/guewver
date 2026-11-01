package com.example.ideaformaayanzohar.voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class VoiceSpeaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean isReady = false;
    private boolean isSpeechEnabled = true;

    public VoiceSpeaker(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("he", "IL"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale if Hebrew voice data is missing
                tts.setLanguage(Locale.getDefault());
            }
            isReady = true;
        } else {
            Log.e("VoiceSpeaker", "TextToSpeech init failed");
        }
    }

    public void speak(String text) {
        if (!isSpeechEnabled || !isReady || text == null || text.trim().isEmpty()) {
            return;
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AiSpeechUtteranceId");
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public boolean isSpeechEnabled() {
        return isSpeechEnabled;
    }

    public void setSpeechEnabled(boolean speechEnabled) {
        isSpeechEnabled = speechEnabled;
        if (!isSpeechEnabled) {
            stop();
        }
    }
}
