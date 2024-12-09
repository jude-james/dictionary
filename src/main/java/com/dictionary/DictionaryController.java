package com.dictionary;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
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

    @FXML
    private void onGoClick() {
        String json = getJSON(searchBox.getText());

        System.out.println(json);

        //serialize json string to Word obj...

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        // mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            resultBox.setText("");

            Word[] word = mapper.readValue(json, Word[].class);

            System.out.println(word[0]);

            for (Word w : word) {
                for (Meaning m : w.getMeanings()) {
                    resultBox.appendText(m.getDefinitions().getFirst().getDefinition());
                    /*
                    for (Definition d : m.getDefinitions()) {
                        //System.out.println(d.definition);
                        resultBox.appendText("\n"+d.definition);
                    }

                     */
                }
                resultBox.appendText("\n ");
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getJSON(String word) {
        String uri = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response.body();
    }
}

