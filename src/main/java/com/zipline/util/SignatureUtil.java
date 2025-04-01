package com.zipline.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;


public class SignatureUtil {

  private final String secretKey;
  private final String authMethod;
  private final String now;
  private final String salt;
  private final String authMessage;

  public SignatureUtil(@Value("${sms.secret-key}") String secretKey,
      @Value("${sms.auth-method}") String authMethod) {
    this.secretKey = secretKey;
    this.authMethod = authMethod;
    this.now = Instant.now().toString();
    this.salt = generateSalt();
    this.authMessage = now + salt;
  }

  private static String generateSalt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    return HexUtils.toHexString(salt);
  }

  public Mono<Map<String, String>> generateSignature() {
    return Mono.fromCallable(() -> {
      Map<String, String> result = new HashMap<>();

      Mac mac = Mac.getInstance(authMethod);
      SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
          authMethod);
      mac.init(keySpec);

      byte[] hmacBytes = mac.doFinal(authMessage.getBytes(StandardCharsets.UTF_8));

      String hash = HexUtils.toHexString(hmacBytes);

      result.put("time", now);
      result.put("salt", salt);
      result.put("hash", hash);

      return result;

    }).onErrorMap(e -> new RuntimeException("signature를 생성 중에 오류가 발생했습니다.", e));
  }
}