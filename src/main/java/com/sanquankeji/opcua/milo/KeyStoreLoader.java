package com.sanquankeji.opcua.milo;

/**
 * Created by Cryan on 2021/8/4.
 * TODO.OPCUA  证书生成
 */

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

class KeyStoreLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    // 证书别名
    private static final String CLIENT_ALIAS = "client-ai";
    // 获取私钥的密码
    private static final char[] PASSWORD = "password".toCharArray();
    // 证书对象
    private X509Certificate clientCertificate;
    // 密钥对对象
    private KeyPair clientKeyPair;

    KeyStoreLoader load(Path baseDir) throws Exception {
        // 创建一个使用`PKCS12`加密标准的KeyStore。KeyStore在后面将作为读取和生成证书的对象。
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        // PKCS12的加密标准的文件后缀是.pfx，其中包含了公钥和私钥。
        // 而其他如.der等的格式只包含公钥，私钥在另外的文件中。
        Path serverKeyStore = baseDir.resolve("example-client.pfx");

        logger.info("Loading KeyStore at {}", serverKeyStore);
        // 如果文件不存在则创建.pfx证书文件。
        if (!Files.exists(serverKeyStore)) {
            keyStore.load(null, PASSWORD);
            // 用2048位的RAS算法。`SelfSignedCertificateGenerator`为Milo库的对象。
            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
            // `SelfSignedCertificateBuilder`也是Milo库的对象，用来生成证书。
            // 中间所设置的证书属性可以自行修改。
            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                    .setCommonName("Eclipse Milo Example Client")
                    .setOrganization("SanQuanKeJi")
                    .setOrganizationalUnit("dev")
                    .setLocalityName("CryanNing")
                    .setStateName("CA")
                    .setCountryCode("US")
                    .setApplicationUri("urn:eclipse:milo:examples:client")
                    .addDnsName("localhost")
                    .addIpAddress("127.0.0.1");

            // Get as many hostnames and IP addresses as we can listed in the certificate.
            for (String hostname : HostnameUtil.getHostnames("0.0.0.0")) {
                if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                    builder.addIpAddress(hostname);
                } else {
                    builder.addDnsName(hostname);
                }
            }
            // 创建证书
            X509Certificate certificate = builder.build();
            // 设置对应私钥的别名，密码，证书链
            keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
            try (OutputStream out = Files.newOutputStream(serverKeyStore)) {
                // 保存证书到输出流
                keyStore.store(out, PASSWORD);
            }
        } else {
            try (InputStream in = Files.newInputStream(serverKeyStore)) {
                // 如果文件存在则读取
                keyStore.load(in, PASSWORD);
            }
        }
        // 用密码获取对应别名的私钥。
        Key serverPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
        if (serverPrivateKey instanceof PrivateKey) {
            // 获取对应别名的证书对象。
            clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);
            // 获取公钥
            PublicKey serverPublicKey = clientCertificate.getPublicKey();
            // 创建Keypair对象。
            clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
        }
        return this;
    }
    // 返回证书
    X509Certificate getClientCertificate() {
        return clientCertificate;
    }

    // 返回密钥对
    KeyPair getClientKeyPair() {
        return clientKeyPair;
    }
}

