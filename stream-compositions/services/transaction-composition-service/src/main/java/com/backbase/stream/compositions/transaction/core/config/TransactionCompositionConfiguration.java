package com.backbase.stream.compositions.transaction.core.config;

import com.backbase.stream.compositions.transaction.cursor.client.TransactionCursorApi;
import com.backbase.stream.compositions.transaction.integration.ApiClient;
import com.backbase.stream.compositions.transaction.integration.client.TransactionIntegrationApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;

@Configuration
@AllArgsConstructor
@EnableWebFluxSecurity
@EnableConfigurationProperties(TransactionConfigurationProperties.class)
public class TransactionCompositionConfiguration {
    private final TransactionConfigurationProperties transactionConfigurationProperties;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.csrf().disable().build();
    }

    @Bean
    @Primary
    public TransactionIntegrationApi transactionIntegrationApi(ApiClient transactionIntegrationClient) {
        return new TransactionIntegrationApi(transactionIntegrationClient);
    }

    @Bean
    @Primary
    public TransactionCursorApi transactionCursorApi(
            com.backbase.stream.compositions.transaction.cursor.ApiClient transactionCursorClient) {
        return new TransactionCursorApi(transactionCursorClient);
    }

    @Bean
    public ApiClient transactionIntegrationClient(WebClient dbsWebClient, ObjectMapper objectMapper,
            DateFormat dateFormat) {

        ApiClient apiClient = new ApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(transactionConfigurationProperties.getIntegrationBaseUrl());

        return apiClient;
    }

    @Bean
    public com.backbase.stream.compositions.transaction.cursor.ApiClient transactionCursorClient(
            WebClient dbsWebClient, ObjectMapper objectMapper, DateFormat dateFormat) {

        com.backbase.stream.compositions.transaction.cursor.ApiClient apiClient =
                new com.backbase.stream.compositions.transaction.cursor.ApiClient(
                        dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(transactionConfigurationProperties.getCursor().getBaseUrl());

        return apiClient;
    }
}