package com.example.desktopapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;

public class IntentRecognizer {

    public final ActivityResultLauncher<Intent> startForResult;
    public final Intent intent;

    public IntentRecognizer(Context context, SpeechResultCallback callback) {
        if (context instanceof ComponentActivity) {
            ActivityResultRegistry registry = ((ComponentActivity) context).getActivityResultRegistry();
            this.startForResult = registry.register("key", new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ArrayList<String> results = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            callback.onSpeechResult(results);
                        }
                    });
            this.intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            this.intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            this.intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        } else {
            throw new IllegalArgumentException("Context must be an instance of ComponentActivity");
        }
    }

    public void startListening() {
        startForResult.launch(intent);
    }

    public interface SpeechResultCallback {
        void onSpeechResult(ArrayList<String> results);
    }
}
