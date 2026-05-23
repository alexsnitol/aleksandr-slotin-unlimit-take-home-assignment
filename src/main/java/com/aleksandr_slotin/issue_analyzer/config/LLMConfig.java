package com.aleksandr_slotin.issue_analyzer.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class LLMConfig {

    @Bean
    public OllamaChatModel analyzeIssueChatModel(
            @Value("${spring.ai.ollama.base-url}") String baseUrl,
            @Value("${spring.ai.ollama.chat.options.model}") String model,
            @Value("${spring.ai.ollama.chat.options.temperature}") Double temperature,
            @Value("${spring.ai.ollama.request-timeout:2m}") Duration requestTimeout,
            @Value("${spring.ai.ollama.init.timeout:2m}") Duration initTimeout,
            @Value("${spring.ai.ollama.init.max-retries:3}") int initMaxRetries
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(requestTimeout);
        ReactorClientHttpRequestFactory requestFactory = new ReactorClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(requestTimeout);
        RestClient.Builder restClientBuilder = RestClient.builder()
                .requestFactory(requestFactory);
        WebClient.Builder webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        return OllamaChatModel.builder()
                .ollamaApi(
                        OllamaApi.builder()
                                .baseUrl(baseUrl)
                                .restClientBuilder(restClientBuilder)
                                .webClientBuilder(webClientBuilder)
                                .build()
                )
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model(model)
                                .temperature(temperature)
                                .enableThinking()
                                .build()
                )
                .modelManagementOptions(
                        ModelManagementOptions.builder()
                                .timeout(initTimeout)
                                .maxRetries(initMaxRetries)
                                .build()
                )
                .build();
    }

    @Bean
    public ChatClient analyzeIssueChatClient(
            OllamaChatModel analyzeIssueChatModel
    ) {
        return ChatClient.builder(analyzeIssueChatModel)
                .defaultTemplateRenderer(
                        StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()
                )
                .build();
    }

}
