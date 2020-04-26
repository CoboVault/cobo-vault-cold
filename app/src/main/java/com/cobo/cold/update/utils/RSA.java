/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.cold.update.utils;

import android.util.ArrayMap;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA {
    private static final String ALGORITHM = "RSA";
    private static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final int MAX_DECRYPT_BLOCK = 128;

    public static byte[] decrypt(@NonNull byte[] publicKeyBuffer, @NonNull byte[] encryptData)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBuffer));
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer;
        int read;

        for (int i = 0, len = encryptData.length; i < len; i += MAX_DECRYPT_BLOCK) {
            read = Math.min(len - i, MAX_DECRYPT_BLOCK);
            buffer = cipher.doFinal(encryptData, i, read);
            outputStream.write(buffer, 0, buffer.length);
        }

        return outputStream.toByteArray();
    }

    public static byte[] encrypt(@NonNull byte[] privateKeyBuffer, @NonNull byte[] content)
            throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBuffer);
        final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer;
        int read;

        for (int i = 0, len = content.length; i < len; i += MAX_ENCRYPT_BLOCK) {
            read = Math.min(len - i, MAX_ENCRYPT_BLOCK);
            buffer = cipher.doFinal(content, i, read);
            outputStream.write(buffer, 0, buffer.length);
        }

        return outputStream.toByteArray();
    }

    public static byte[] sign(@NonNull byte[] privateKeyBuffer, @NonNull byte[] content)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBuffer);
        final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        final Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

        signature.initSign(privateKey);
        signature.update(content);

        return signature.sign();
    }

    public static boolean check(@NonNull byte[] publicKeyBuffer,
                                @NonNull byte[] content, @NonNull byte[] signed)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBuffer));
        final Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

        signature.initVerify(publicKey);
        signature.update(content);

        return signature.verify(signed);
    }

    public static Map<String, String> newKeyPair() throws NoSuchAlgorithmException {
        final Map<String, String> keyMap = new ArrayMap<>();
        final KeyPairGenerator keygen = KeyPairGenerator.getInstance(ALGORITHM);
        final SecureRandom random = new SecureRandom();
        keygen.initialize(1024, random);

        final KeyPair kp = keygen.generateKeyPair();
        final RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();
        final RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();

        keyMap.put("privateKey", Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT));
        keyMap.put("publicKey", Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));

        return keyMap;
    }
}
