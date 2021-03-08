package org.deb.loan.approver.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncoderService {
  private final Base64 base64 = new Base64();

  public String encode(String decrypted) {
    return base64.encodeAsString(decrypted.getBytes());
  }

  public String decode(String encrypted) {
    return new String(base64.decode(encrypted));
  }
}
