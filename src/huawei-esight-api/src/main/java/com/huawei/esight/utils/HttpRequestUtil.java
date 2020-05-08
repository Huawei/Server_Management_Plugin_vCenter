package com.huawei.esight.utils;

import com.huawei.esight.exception.EsightException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpRequestUtil {

  private static RestTemplate restTemplate;

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtil.class);

  private static final String CODE_ESIGHT_CONNECT_EXCEPTION = "-80011";

  private static final int REQUEST_TIMEOUT = 30000; // millis

  private static final ThumbprintTrustManager[] TRUST_MANAGERS = new ThumbprintTrustManager[]{
      new ThumbprintTrustManager()};

  static {
    final HostnameVerifier PROMISCUOUS_VERIFIER = new HostnameVerifier() {
      public boolean verify(String s, SSLSession sslSession) {
        return true;
      }
    };

    restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {

      @Override
      protected void prepareConnection(HttpURLConnection connection, String httpMethod)
          throws IOException {

        if (connection instanceof HttpsURLConnection) {
          ((HttpsURLConnection) connection).setHostnameVerifier(PROMISCUOUS_VERIFIER);
        }
        super.prepareConnection(connection, httpMethod);
      }

      @Override
      protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,TLSv1");
        HttpURLConnection httpURLConnection = super.openConnection(url, proxy);
        if (httpURLConnection instanceof HttpsURLConnection) {
          try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, TRUST_MANAGERS, new java.security.SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(PROMISCUOUS_VERIFIER);
            ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(ssf);
          } catch (Exception e) {
            LOGGER.error("Cannot set SSL context", e);
          }
        }
        return httpURLConnection;
      }
    };
    requestFactory.setConnectTimeout(REQUEST_TIMEOUT);
    requestFactory.setReadTimeout(REQUEST_TIMEOUT);

    restTemplate.setRequestFactory(requestFactory);
    List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
    list.add(new GsonHttpMessageConverter());
    list.add(new FormHttpMessageConverter());
    list.add(new SourceHttpMessageConverter());
    list.add(new StringHttpMessageConverter());
    restTemplate.setMessageConverters(list);
  }

  /**
   *
   * @param url
   * @param method
   * @param headers
   * @param body
   * @param responseType
   * @param <T>
   * @return
   */
  public static <T> ResponseEntity<T> requestWithBody(String url, HttpMethod method,
      MultiValueMap<String, String> headers, String body, Class<T> responseType) {
    HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
    ResponseEntity<T> responseEntity = restTemplate
        .exchange(url, method, requestEntity, responseType);
    if (responseEntity == null) {
      throw new EsightException(CODE_ESIGHT_CONNECT_EXCEPTION, "Esight not found error");
    }

    if (responseEntity.getStatusCode().value() >= 400
        && responseEntity.getStatusCode().value() <= 600) {
      throw new EsightException(responseEntity.getStatusCode().name(), "Esight error");
    }
    return responseEntity;
  }

  /**
   * Return key=value param concat by &, value is encoded
   */
  public static String concatParamAndEncode(Map<String, String> paramMap) {
    if (paramMap == null || paramMap.isEmpty()) {
      return "";
    }
    StringBuilder buff = new StringBuilder();
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      if (buff.length() > 0) {
        buff.append("&");
      }
      buff.append(entry.getKey()).append("=").append(encode(entry.getValue()));
    }
    return buff.toString();
  }

  /**
   * Return key=value param concat by &
   */
  public static String concatParam(Map<String, String> paramMap) {
    if (paramMap == null || paramMap.isEmpty()) {
      return "";
    }
    StringBuilder buff = new StringBuilder();
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      if (buff.length() > 0) {
        buff.append("&");
      }
      buff.append(entry.getKey()).append("=").append(entry.getValue());
    }
    return buff.toString();
  }

  public static void updateContextTrustThumbprints(String[] thumbprints) {
    for (String thumbprint : thumbprints) {
      TRUST_MANAGERS[0].addThumbprint(thumbprint);
    }
  }

  private static String encode(String str) {
    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new EsightException("Esight URL Encode error");
    }
  }

}

