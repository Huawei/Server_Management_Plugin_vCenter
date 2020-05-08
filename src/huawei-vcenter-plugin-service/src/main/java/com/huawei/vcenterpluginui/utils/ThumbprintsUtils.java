package com.huawei.vcenterpluginui.utils;

import com.huawei.esight.utils.HttpRequestUtil;
import com.huawei.esight.utils.ThumbprintTrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbprintsUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThumbprintsUtils.class);

  /**
   * read jks from input stream
   * @param jksInputStream
   * @param password
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   * @throws KeyStoreException
   */
  public static String[] getThumbprintsFromJKS(InputStream jksInputStream, String password)
      throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
    try {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(jksInputStream, password.toCharArray());
      Enumeration<String> aliases = keyStore.aliases();
      Set<String> thumbprints = new HashSet<>();
      while (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        Certificate[] certificateChain = keyStore.getCertificateChain(alias);
        for (Certificate certificate : certificateChain) {
          if (certificate instanceof X509Certificate) {
            thumbprints.add(ThumbprintTrustManager.getThumbprint((X509Certificate) certificate));
          }
        }
      }
      return thumbprints.toArray(new String[thumbprints.size()]);
    } catch (Exception e) {
      LOGGER.error("Failed to get thumbprints from JKS", e);
      throw e;
    }
  }

  public static void updateContextTrustThumbprints(String[] thumbprints) {
    HttpRequestUtil.updateContextTrustThumbprints(thumbprints);
  }

  public static Set<String> getRuntimeThumbprints() {
    return ThumbprintTrustManager.getThumbprints();
  }

}
