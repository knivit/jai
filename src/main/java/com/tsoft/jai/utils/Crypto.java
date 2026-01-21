package com.tsoft.jai.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

public final class Crypto {

    // pub fn sha256(input: &str) -> String {
    //    let mut hasher = Sha256::new();
    //    hasher.update(input);
    //    format!("{:x}", hasher.finalize())
    // }
    public static String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return null;
        }
    }

    // pub fn base64_encode<T: AsRef<[u8]>>(input: T) -> String {
    //    STANDARD.encode(input)
    // }
    public static String base64Encode(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    private Crypto() { }
}
