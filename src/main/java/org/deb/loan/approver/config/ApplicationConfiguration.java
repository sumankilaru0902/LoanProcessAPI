package org.deb.loan.approver.config;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import javax.net.ssl.SSLContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
@Setter
public class ApplicationConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
    sslContext.init(null, null, null);
    CloseableHttpClient httpClient =
        HttpClients.custom()
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .setSSLContext(sslContext)
            .build();
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);
    requestFactory.setConnectTimeout(3000);
    return new RestTemplate(requestFactory);
  }
}
