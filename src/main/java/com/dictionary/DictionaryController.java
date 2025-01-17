package com.dictionary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DictionaryController {
    @FXML
    private TextField searchBox;

    @FXML
    private TextFlow resultBox;

    private final String noDefinitionFoundResponse =
            "{\"title\":\"No Definitions Found\",\"message\":\"Sorry pal, we couldn't find definitions for the word you were looking for.\",\"resolution\":\"You can try the search again at later time or head to the web instead.\"}";

    @FXML
    private void onSearchClick() {
        String text = searchBox.getText();

        if (text.isEmpty()) {
            return;
        }

        String response = getJSON(text);

        resultBox.getChildren().clear();

        if (response.equals(noDefinitionFoundResponse)) {
            Text noDefFound = new Text("No entries found for: " + text);
            noDefFound.setFont(Font.font("Serif", 18));
            resultBox.getChildren().add(noDefFound);

            return;
        }

        Word word = MapJson(response);
        DisplayResult(word);
    }

    private void DisplayResult(Word word) {
        // Word
        Text wordText = new Text(word.getWord());
        wordText.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        resultBox.getChildren().add(wordText);

        // Phonic
        if (word.getPhonetic() != null) {
            Text phonicText = new Text(" | " + word.getPhonetic().substring(1, word.getPhonetic().length() - 1) + " | ");
            phonicText.setFont(Font.font("Serif", 18));
            resultBox.getChildren().add(phonicText);
        }

        // New line before meanings
        Text newLineText = new Text("\n");
        resultBox.getChildren().add(newLineText);

        List<Meaning> meanings = word.getMeanings();
        for (int i = 0; i < meanings.size(); i++) {
            // Verb, noun, etc
            Text partOfSpeechText = new Text("\n\t" + meanings.get(i).getPartOfSpeech());
            partOfSpeechText.setFont(Font.font("Serif", FontWeight.BOLD, 18));
            resultBox.getChildren().add(partOfSpeechText);

            for (int j = 0; j < meanings.get(i).getDefinitions().size(); j++) {
                // Definition
                if (meanings.get(i).getDefinitions().size() != 1) {
                    Text numberingText = new Text("\n\t\t" + (j+1) + ".");
                    numberingText.setFont(Font.font("Serif", 18));
                    resultBox.getChildren().add(numberingText);
                }

                Text definitionText = new Text("\n\t\t\t" + meanings.get(i).getDefinitions().get(j).getDefinition());
                definitionText.setFont(Font.font("Serif", 18));
                resultBox.getChildren().add(definitionText);
                
                // Example quote
                String example = meanings.get(i).getDefinitions().get(j).getExample();
                if (example != null) {
                    Text exampleText = new Text("\n\t\t\t" + example);
                    exampleText.setFont(Font.font("Serif", FontWeight.BOLD, FontPosture.ITALIC, 16));
                    resultBox.getChildren().add(exampleText);
                }
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
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response.body();
    }

    private Word MapJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        Word[] word;

        try {
            word = mapper.readValue(json, Word[].class);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return word[0];
    }
}