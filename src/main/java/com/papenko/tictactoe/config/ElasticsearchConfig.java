package com.papenko.tictactoe.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.papenko.tictactoe.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfig.class);

    private final String elasticUsername;
    private final String elasticPassword;
    private final String elasticHost;
    private final Integer elasticPort;
    private final String elasticProtocol;
    private final String profile;

    public ElasticsearchConfig(@Value("#{systemEnvironment['ELASTIC_USERNAME']}") String elasticUsername,
                               @Value("#{systemEnvironment['ELASTIC_PASSWORD']}") String elasticPassword,
                               @Value("#{systemEnvironment['ELASTIC_HOST']}") String elasticHost,
                               @Value("#{systemEnvironment['ELASTIC_PORT']}") Integer elasticPort,
                               @Value("#{systemEnvironment['ELASTIC_PROTOCOL']}") String elasticProtocol,
                               @Value("#{systemEnvironment['SPRING_PROFILES_ACTIVE']}") String profile) {
        this.elasticUsername = elasticUsername;
        this.elasticPassword = elasticPassword;
        this.elasticHost = elasticHost;
        this.elasticPort = elasticPort;
        this.elasticProtocol = elasticProtocol;
        this.profile = profile;
    }

    @Override
    public RestHighLevelClient elasticsearchClient() {
        if ("production".equals(profile)) {
            LOGGER.info("Using production environment");
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(elasticUsername, elasticPassword);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);

            HttpHost httpHost = new HttpHost(elasticHost, elasticPort, elasticProtocol);
            RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
            restClientBuilder.setHttpClientConfigCallback(h -> h.setDefaultCredentialsProvider(credentialsProvider));

            return new RestHighLevelClient(restClientBuilder);
        }
        LOGGER.info("Using default local environment");
        return new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }
}
