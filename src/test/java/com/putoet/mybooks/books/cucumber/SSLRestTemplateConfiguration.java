package com.putoet.mybooks.books.cucumber;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class SSLRestTemplateConfiguration {

    @Value("${server.ssl.key-store}")
    private Resource trustStore;

    @Value("${server.ssl.key-store-password}")
    private String trustStorePassword;

    @Bean
    public RestTemplate sslRestTemplate() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
                .build();

        final SSLConnectionSocketFactory sslConFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslConFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        final PoolingHttpClientConnectionManager poolingConnectionManager =
                new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        final CloseableHttpClient httpClient =
                HttpClients.custom()
                        .setConnectionManager(poolingConnectionManager)
                        .build();

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }
}