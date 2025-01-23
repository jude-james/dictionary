package com.dictionary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ResourceBundle;

public class DictionaryController implements Initializable {
    @FXML
    private TextField searchBox;

    @FXML
    private TextFlow resultBox;

    @FXML
    private ScrollPane scrollPane;

    private StringWrapper definitionWrapper;
    private StringWrapper exampleWrapper;

    private String fontName = "Courier New";
    private String bulletSymbol = "• ";
    Font font = Font.loadFont("file:src/Serif?.ttf", 22);

    private final String noDefinitionFoundResponse =
            "{\"title\":\"No Definitions Found\",\"message\":\"Sorry pal, we couldn't find definitions for the word you were looking for.\",\"resolution\":\"You can try the search again at later time or head to the web instead.\"}";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resultBox.setTabSize(1);
        Text initialText = new Text("Type a word to look up...");
        initialText.setFont(Font.font(fontName, FontWeight.NORMAL, 20));
        initialText.setFill(Color.WHITE);
        resultBox.getChildren().add(initialText);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        definitionWrapper = new StringWrapper(65, 1, 3, true);
        exampleWrapper = new StringWrapper(65, 1, 3, true);
    }

    @FXML
    private void onSearchClick() {
        String text = searchBox.getText().trim();

        resultBox.getChildren().clear();

        if (text.isEmpty()) {
            return;
        }

        if (text.matches(".*[\"\\\\|/^%{}#?<>\\[\\] ].*")) {
            Text noDefFound = new Text("No entries found for: " + text);
            noDefFound.setFont(Font.font(fontName, 18));
            noDefFound.setFill(Color.WHITE);
            resultBox.getChildren().add(noDefFound);

            return;
        }

        String response = getJSON(text);

        if (response.equals(noDefinitionFoundResponse)) {
            Text noDefFound = new Text("No entries found for: " + text);
            noDefFound.setFont(Font.font(fontName, 18));
            noDefFound.setFill(Color.WHITE);
            resultBox.getChildren().add(noDefFound);

            return;
        }

        Word word = MapJson(response);
        FormatResult(word);
    }

    private void FormatResult(Word word) {
        // Word
        Text wordText = new Text(word.getWord());
        wordText.setFont(Font.font(fontName, FontWeight.BOLD, 22));
        wordText.setFill(Color.WHITE);
        resultBox.getChildren().add(wordText);

        // Phonic
        if (word.getPhonetic() != null) {
            Text phonicText = new Text(" | " + word.getPhonetic().substring(1, word.getPhonetic().length() - 1) + " | ");
            phonicText.setFont(Font.font(fontName, 20));
            phonicText.setFill(Color.WHITE);
            resultBox.getChildren().add(phonicText);
        }
        else {
            //TODO loop through phonics phonic if word.getPhonic is null
            // Text phonicText = new Text(" | " + word.getPhonetics().get(1).getText().substring(1, word.getPhonetics().get(1).getText().length() - 1) + " | ");
        }

        List<Meaning> meanings = word.getMeanings();
        for (int i = 0; i < meanings.size(); i++) {
            // Verb, noun, etc
            Text partOfSpeechText = new Text("\n\n\t" + meanings.get(i).getPartOfSpeech());
            partOfSpeechText.setFont(Font.font(fontName, FontWeight.BOLD, 20));
            partOfSpeechText.setFill(Color.SIENNA);
            resultBox.getChildren().add(partOfSpeechText);

            for (int j = 0; j < meanings.get(i).getDefinitions().size(); j++) {
                // Definition
                Text definitionText = new Text(definitionWrapper.wrapAndIndent(bulletSymbol + meanings.get(i).getDefinitions().get(j).getDefinition()));
                definitionText.setFont(Font.font(fontName, FontWeight.BOLD,18));
                definitionText.setFill(Color.WHITE);
                resultBox.getChildren().add(definitionText);

                // Example quote
                String example = meanings.get(i).getDefinitions().get(j).getExample();
                Text exampleText = new Text("\n");

                if (example != null) {
                    exampleText = new Text(exampleWrapper.wrapAndIndent(example) + "\n");
                }

                exampleText.setFont(Font.font(fontName, FontPosture.ITALIC, 18));
                exampleText.setFill(Color.WHITE);
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