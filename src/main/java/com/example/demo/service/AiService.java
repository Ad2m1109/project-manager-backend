package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.example.demo.dto.GeminiRequest;
import com.example.demo.dto.GeminiResponse;
import java.util.List;

@Service
public class AiService {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public AiService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String generateContent(String prompt) {
        try {
            GeminiRequest request = new GeminiRequest(List.of(
                    new GeminiRequest.Content(List.of(
                            new GeminiRequest.Part(prompt)))));

            GeminiResponse response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).content().parts().get(0).text();
            }
            return "No response from AI.";

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            System.err.println("Rate limit exceeded: " + e.getMessage());
            return "I'm currently overwhelmed with requests (Rate Limit Exceeded). Please try again in a minute.";
        } catch (Exception e) {
            System.err.println("Error generating content: " + e.getMessage());
            e.printStackTrace();
            return "Error: Unable to generate response. " + e.getMessage();
        }
    }
}
