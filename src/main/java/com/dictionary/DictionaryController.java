package com.dictionary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.media.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class DictionaryController implements Initializable {
    @FXML
    private TextField searchBox;

    @FXML
    private TextFlow resultBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button listenButton;

    private StringWrapper definitionWrapper;
    private StringWrapper exampleWrapper;

    private final String fontPath = "/fonts/JetBrainsMono/ttf/";

    private final Font regularFont = Font.loadFont(getClass().getResourceAsStream(fontPath + "JetBrainsMono-Regular.ttf"), 18);
    private final Font boldFont = Font.loadFont(getClass().getResourceAsStream(fontPath + "JetBrainsMono-Bold.ttf"), 18);
    private final Font italicFont = Font.loadFont(getClass().getResourceAsStream(fontPath + "JetBrainsMono-ThinItalic.ttf"), 18);
    private final Font wordFont = Font.loadFont(getClass().getResourceAsStream(fontPath + "JetBrainsMono-Bold.ttf"), 22);
    private final Font phoneticFont = new Font("Courier New", 18);

    private final String bulletSymbol = "• ";

    private final Color primaryColour = Color.rgb(247, 247, 247);
    private final Color highlightColour = Color.rgb(194, 78, 78);
    private final Color exampleColour = Color.rgb(168, 182, 255);

    private MediaPlayer mediaPlayer = null;

    private final String noDefinitionFoundResponse =
            "{\"title\":\"No Definitions Found\",\"message\":\"Sorry pal, we couldn't find definitions for the word you were looking for.\",\"resolution\":\"You can try the search again at later time or head to the web instead.\"}";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resultBox.setTabSize(1);
        Text initialText = new Text("> Type a word to look up");
        initialText.setFont(regularFont);
        initialText.setFill(primaryColour);
        resultBox.getChildren().add(initialText);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        definitionWrapper = new StringWrapper(65, 1, 3, true);
        exampleWrapper = new StringWrapper(65, 1, 3, true);
    }

    @FXML
    private void onSearchClick() {
        resultBox.getChildren().clear();
        mediaPlayer = null;

        String text = searchBox.getText().trim();

        if (text.isEmpty()) {
            return;
        }

        if (text.matches(".*[\"\\\\|/^%{}#?<>\\[\\] ].*")) {
            Text noDefFound = new Text("No entries found for: " + text);
            noDefFound.setFont(regularFont);
            noDefFound.setFill(primaryColour);
            resultBox.getChildren().add(noDefFound);

            return;
        }

        String response = getJSON(text);

        if (response == null) {
            Text connectionError = new Text("Connection error. Connect to the internet.");
            connectionError.setFont(regularFont);
            connectionError.setFill(primaryColour);
            resultBox.getChildren().add(connectionError);

            return;
        }

        if (response.equals(noDefinitionFoundResponse)) {
            Text noDefFound = new Text("No entries found for: " + text);
            noDefFound.setFont(regularFont);
            noDefFound.setFill(primaryColour);
            resultBox.getChildren().add(noDefFound);

            return;
        }

        Word word = MapJson(response);
        FormatResult(word);
    }

    @FXML
    private void OnListenClick() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        }
    }

    private void FormatResult(Word word) {
        // Audio
        String mp3Link = null;
        if (!word.getPhonetics().isEmpty()) {
            for (int i = 0; i < word.getPhonetics().size(); i++) {
                if (!Objects.equals(word.getPhonetics().get(i).getAudio(), "")) {
                    mp3Link = word.getPhonetics().get(i).getAudio();
                    break;
                }
            }
        }

        if (mp3Link != null && !mp3Link.isEmpty()) {
            listenButton.setDisable(false);
            Media audio = new Media(mp3Link);
            mediaPlayer = new MediaPlayer(audio);
        }
        else {
            listenButton.setDisable(true);
        }

        // Word
        Text wordText = new Text(word.getWord());
        wordText.setFont(wordFont);
        wordText.setFill(primaryColour);
        resultBox.getChildren().add(wordText);

        // Phonic
        if (word.getPhonetic() != null) {
            Text phoneticText = new Text(" | " + word.getPhonetic().substring(1, word.getPhonetic().length() - 1) + " | ");
            phoneticText.setFont(phoneticFont);
            phoneticText.setFill(primaryColour);
            resultBox.getChildren().add(phoneticText);
        }
        else {
            List<Phonetic> phonetics = word.getPhonetics();
            for (int i = 0; i < phonetics.size(); i++) {
                String phonetic = phonetics.get(i).getText();
                if (phonetic != null) {
                    Text phoneticText = new Text(" | " + phonetic.substring(1, phonetic.length() - 1) + " | ");
                    phoneticText.setFont(regularFont);
                    phoneticText.setFill(primaryColour);
                    resultBox.getChildren().add(phoneticText);
                    break;
                }
            }
        }

        List<Meaning> meanings = word.getMeanings();
        for (int i = 0; i < meanings.size(); i++) {
            // Verb, noun, etc
            Text partOfSpeechText = new Text("\n\n\t" + meanings.get(i).getPartOfSpeech());
            partOfSpeechText.setFont(boldFont);
            partOfSpeechText.setFill(highlightColour);
            resultBox.getChildren().add(partOfSpeechText);

            for (int j = 0; j < meanings.get(i).getDefinitions().size(); j++) {
                // Definition
                Text definitionText = new Text(definitionWrapper.wrapAndIndent(bulletSymbol + meanings.get(i).getDefinitions().get(j).getDefinition()));
                definitionText.setFont(regularFont);
                definitionText.setFill(primaryColour);
                resultBox.getChildren().add(definitionText);

                // Example quote
                String example = meanings.get(i).getDefinitions().get(j).getExample();
                Text exampleText = new Text("\n");

                if (example != null) {
                    exampleText = new Text(exampleWrapper.wrapAndIndent(example) + "\n");
                }

                exampleText.setFont(italicFont);
                exampleText.setFill(exampleColour);
                resultBox.getChildren().add(exampleText);
            }
        }
    }

    private String getJSON(String word) {
        String uri = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (ConnectException e) {
            return null;
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response.body();
    }

    private Word MapJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        Word[] words;

        try {
            words = mapper.readValue(json, Word[].class);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return words[0];
    }
}