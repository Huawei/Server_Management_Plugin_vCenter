package com.huawei.vcenter.utils;

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

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HttpRequestUtil {

    private static RestTemplate restTemplate;

    private static final Logger LOGGER = Logger.getLogger(HttpRequestUtil.class.getSimpleName());

    private static final int REQUEST_TIMEOUT = 30000; // millis

    static {
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,TLSv1");

        try {
            SSLUtil.turnOffSslChecking();
        } catch (KeyManagementException e) {
            LOGGER.info(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info(e.getMessage());
        }

        final HostnameVerifier PROMISCUOUS_VERIFIER = new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };

        restTemplate = new RestTemplate();

        SimpleClientHttpRequestFactory simpleFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                    throws IOException {

                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setHostnameVerifier(PROMISCUOUS_VERIFIER);
                }
                super.prepareConnection(connection, httpMethod);
            }
        };
        simpleFactory.setConnectTimeout(REQUEST_TIMEOUT);
        simpleFactory.setReadTimeout(REQUEST_TIMEOUT);

        restTemplate.setRequestFactory(simpleFactory);
        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
        list.add(new FormHttpMessageConverter());
        list.add(new SourceHttpMessageConverter());
        list.add(new StringHttpMessageConverter());
        restTemplate.setMessageConverters(list);
    }

    /**
     * @param url
     * @param method
     * @param headers
     * @param body
     * @param responseType
     * @param <T>
     * @return
     */
    public static <T> ResponseEntity<T> requestWithBody(String url, HttpMethod method, MultiValueMap<String, String> headers,
                                                        String body, Class<T> responseType) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);

        ResponseEntity<T> responseEntity = restTemplate.exchange(url, method, requestEntity, responseType);
        return responseEntity;
    }

    /**
     * Return key=value param concat by &, value is encoded
     */
    public static String concatParamAndEncode(Map<String, String> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) return "";
        StringBuilder buff = new StringBuilder();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (buff.length() > 0) buff.append("&");
            buff.append(entry.getKey()).append("=").append(encode(entry.getValue()));
        }
        return buff.toString();
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL Encode error");
        }
    }

    static class SSLUtil {

        private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        public static void turnOffSslChecking()
                throws NoSuchAlgorithmException, KeyManagementException {
            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, UNQUESTIONING_TRUST_MANAGER, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }

        public static void turnOnSslChecking() throws KeyManagementException, NoSuchAlgorithmException {
            // Return it to the initial state (discovered by reflection, now hardcoded)
            SSLContext.getInstance("SSL").init(null, null, null);
        }

        private SSLUtil() {
            throw new UnsupportedOperationException("Do not instantiate libraries.");
        }
    }

}

