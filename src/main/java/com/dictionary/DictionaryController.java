package com.dictionary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DictionaryController {
    @FXML
    private TextField searchBox;

    @FXML
    private TextArea resultBox;

    private final String noDefinitionFoundResponse =
            "{\"title\":\"No Definitions Found\",\"message\":\"Sorry pal, we couldn't find definitions for the word you were looking for.\",\"resolution\":\"You can try the search again at later time or head to the web instead.\"}";

    @FXML
    private void onGoClick() {
        String text = searchBox.getText();

        if (text.isEmpty()) {
            return;
        }

        String response = getJSON(text);

        if (response.equals(noDefinitionFoundResponse)) {
            resultBox.appendText("No definition found for your word: " + text);
            return;
        }

        Word word = MapJson(response);

        // Display result

        resultBox.setText("");
        for (Meaning m : word.getMeanings()) {
            //resultBox.appendText(m.getDefinitions().getFirst().getDefinition());
            System.out.println(m.toString());
            for (Definition d : m.getDefinitions()) {
                //System.out.println(d.definition);
                resultBox.appendText("\n"+d.getDefinition());
            }
        }
        resultBox.appendText("\n ");
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