package com.example.desktopapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView responseTextView;
    private SpeechRecognizer speechRecognizer;
    private IntentRecognizer intentRecognizer;
    private TextToSpeech textToSpeech;
    private String chatStr = "";

    // List of jokes
    private List<String> jokes;
    private String apiKey = ""; // Replace with your actual API key

    // List to store chat history
    private List<String> chatHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseTextView = findViewById(R.id.responseTextView);
        Button listenButton = findViewById(R.id.listenButton);

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListenerImpl());

        // Initialize IntentRecognizer
        intentRecognizer = new IntentRecognizer(this, this::onSpeechResult);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language not supported");
                } else {
                    // Greet the user when the app is opened
                    speak("Hi Rohit, I am Blackbot. How can I help you?");
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed");
            }
        });

        // Set onClickListener for the Listen button
        listenButton.setOnClickListener(v -> startListening());

        // Adjust padding based on system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize list of jokes
        initializeJokes();
    }

    // Helper method to start voice recognition
    private void startListening() {
        intentRecognizer.startForResult.launch(intentRecognizer.intent);
    }

    // Callback for speech recognition results
    private void onSpeechResult(ArrayList<String> results) {
        if (results != null && !results.isEmpty()) {
            String query = results.get(0).toLowerCase();

            // Add user command to chat history
            addToChatHistory("Rohit: " + query);

            if (query.contains("open camera")) {
                openCamera();
            } else if (query.contains("open whatsapp")) {
                openWhatsApp();
            } else if (query.contains("tell me a joke")) {
                tellJoke();
            } else if (query.contains("open music")) {
                openMusicPlayer();
            } else if (query.contains("open LinkedIn")) {
                openLinkedInProfile();
            } else if (query.contains("open instagram")) {
                openInstagramProfile();
            } else {
                // Call OpenAIChat for other queries
                getResponseFromOpenAI(query);
            }
        }
    }

    // Function to perform chat using GPT-3
    private void getResponseFromOpenAI(String query) {
        String url = "https://api.openai.com/v1/completions";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo-instruct");
            jsonObject.put("prompt", query);
            jsonObject.put("temperature", 0.7);
            jsonObject.put("max_tokens", 150);
            jsonObject.put("top_p", 1);
            jsonObject.put("frequency_penalty", 0.0);
            jsonObject.put("presence_penalty", 0.0);

            Log.d("OpenAI Request", jsonObject.toString()); // Log the request

            // Create a JsonObjectRequest
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, jsonObject, response -> {
                        try {
                            Log.d("OpenAI Response", response.toString()); // Log the response
                            String responseMsg = response.getJSONArray("choices").getJSONObject(0).getString("text");
                            responseTextView.setText(responseMsg);
                            addToChatHistory("BlackBot: " + responseMsg); // Add response to chat history
                        } catch (JSONException e) {
                            Log.e("OpenAI Error", "Error parsing JSON response: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> {
                        Log.e("OpenAI Error", "Volley Error: " + error.toString()); // Log Volley error
                        Toast.makeText(MainActivity.this, "Volley Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + apiKey);
                    return headers;
                }
            };

            // Add the request to the RequestQueue
            Volley.newRequestQueue(this).add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e("OpenAI Error", "JSON Exception: " + e.getMessage());
            Toast.makeText(MainActivity.this, "JSON Exception", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to add chat to history
    private void addToChatHistory(String message) {
        chatHistory.add(message);
        updateChatHistoryUI();
    }

    // Function to update chat history UI
    private void updateChatHistoryUI() {
        StringBuilder chatHistoryStr = new StringBuilder();

        // Add a new line if chat history is not empty
        if (!chatHistory.isEmpty()) {
            chatHistoryStr.append("\n");
        }

        // Append each message in the chat history with appropriate formatting
        for (String message : chatHistory) {
            if (message.startsWith("Rohit: ")) {
                chatHistoryStr.append(Html.fromHtml("<b><big>Rohit:</big></b> ")).append(message.substring(7)); // Bold and bigger text for Rohit's message
            } else if (message.startsWith("BlackBot: ")) {
                chatHistoryStr.append(Html.fromHtml("<b><big>BlackBot:</big></b> ")).append(message.substring(10)); // Bold and bigger text for BlackBot's message
            } else {
                chatHistoryStr.append(message);
            }
            chatHistoryStr.append("\n\n"); // Add gap between consecutive commands and responses
        }

        responseTextView.setText(chatHistoryStr.toString());
    }

    // Function to perform other actions
    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            responseTextView.setText("Camera app not available");
            speak("Camera app not available");
            Log.d("Assistant", "Camera app not available");
        }

    }
    private void openLinkedInProfile() {
        String linkedInProfileUrl = "https://www.linkedin.com/in/rohit-kumar-giri-9b444324a";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedInProfileUrl));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Handle case when no app is available to handle the intent
            responseTextView.setText("LinkedIn app not available");
            speak("LinkedIn app not available");
            Log.d("Assistant", "LinkedIn app not available");
        }
    }

    private void openInstagramProfile() {
        String instagramAppPackage = "com.instagram.android";

        // Check if Instagram app is installed
        if (isAppInstalled(instagramAppPackage)) {
            // Instagram app is installed, open the profile
            String instagramProfileUrl = "https://www.instagram.com/badal_bnftv?igsh=MW80dDh0OXg2Z3M4";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramProfileUrl));
            startActivity(intent);
        } else {
            // Instagram app is not installed
            responseTextView.setText("Instagram app not installed");
            speak("Instagram app not installed");
            Log.d("Assistant", "Instagram app not installed");
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            // Attempt to get the application info
            getPackageManager().getApplicationInfo(packageName, 0);
            // If no exception is thrown, the app is installed
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // If an exception is thrown, the app is not installed
            return false;
        }
    }

    private void openWhatsApp() {
        // Implementation for opening WhatsApp
        String phoneNumber = "6202985338"; // Replace with the phone number you want to chat with
        String url = "https://wa.me/" + phoneNumber;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            responseTextView.setText("WhatsApp not supported or not installed");
            speak("WhatsApp not supported or not installed");
            Log.d("Assistant", "WhatsApp not supported or not installed");
        }

    }

    private void openMusicPlayer() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file:///SD Card/Music/1.opus"), "audio/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            responseTextView.setText("Assistant Response: Music app not available");
        }
    }

    private void tellJoke() {
        // Randomly select a joke from the list
        Random random = new Random();
        int index = random.nextInt(jokes.size());
        String joke = jokes.get(index);

        // Display the joke with an emoji
        String response = "Okay Rohit, Here's a joke for you: \n" + joke;
        responseTextView.setText(response);
        speak(response);
    }

    // Text-to-Speech function
    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        // Shutdown TextToSpeech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private class RecognitionListenerImpl implements RecognitionListener {
        // Implementation for RecognitionListener methods
        @Override
        public void onReadyForSpeech(Bundle params) {
            // Implementation for onReadyForSpeech
        }

        @Override
        public void onBeginningOfSpeech() {
            // Implementation for onBeginningOfSpeech
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // Implementation for onRmsChanged
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // Implementation for onBufferReceived
        }

        @Override
        public void onEndOfSpeech() {
            // Implementation for onEndOfSpeech
        }

        @Override
        public void onError(int error) {
            // Implementation for onError
        }

        @Override
        public void onResults(Bundle results) {
            // Implementation for onResults
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // Implementation for onPartialResults
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // Implementation for onEvent
        }
    }

    private void initializeJokes() {
        jokes = new ArrayList<>();
        jokes.add("Why did the scarecrow win an award? Because he was outstanding in his field!");
        jokes.add("What do you get when you cross a snowman and a vampire? Frostbite!");
        jokes.add("Why don't scientists trust atoms? Because they make up everything!");
        jokes.add("How does a penguin build its house? Igloos it together!");
        jokes.add("What's brown and sticky? A stick!");
        jokes.add("Why don't skeletons fight each other? They don't have the guts!");
        jokes.add("What did one wall say to the other wall? 'I'll meet you at the corner.'");
        jokes.add("Why did the bicycle fall over? It was two-tired!");
        jokes.add("How do you organize a space party? You planet!");
        jokes.add("Why did the tomato turn red? Because it saw the salad dressing!");
        jokes.add("What did the janitor say when he jumped out of the closet? Supplies!");
        jokes.add("Why don't oysters donate to charity? Because they are shellfish!");
        jokes.add("What's orange and sounds like a parrot? A carrot!");
        jokes.add("Why did the math book look sad? Because it had too many problems!");
        jokes.add("What did one plate say to another plate? 'Tonight, dinner's on me!'");
        jokes.add("Why don't scientists trust atoms? Because they make up everything!");
        jokes.add("What did the grape do when it got stepped on? Nothing, but it let out a little wine!");
        jokes.add("How do you make a tissue dance? You put a little boogie in it!");
        jokes.add("Why did the computer go to therapy? Because it had too many bytes of emotional baggage!");
        jokes.add("What do you call a fish wearing a crown? A kingfish!");
    }

}
