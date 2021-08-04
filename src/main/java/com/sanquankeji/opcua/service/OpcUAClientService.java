package com.sanquankeji.opcua.service;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
/**
 * Created by Cryan on 2021/8/4.
 * TODO.
 */

/**
 * @author yaohj
 * @date 2020/7/30
 * OPC UA协议对象接口
 */
public interface OpcUAClientService {

    /**
     * OPC UA服务器地址和接口
     */
    default String getEndpointUrl() {
        return "opc.tcp://127.0.0.1:49320";
    }

    /**
     * 过滤返回的server endpoint
     */
    default Predicate<EndpointDescription> endpointFilter() {
        return e -> true;
    }

    /**
     * 连接服务器的安全策略
     * None、Basic128Rsa15、Basic256、Basic256Sha256、Aes128_Sha256_RsaOaep、Aes256_Sha256_RsaPss
     */
    default SecurityPolicy getSecurityPolicy() {
        return SecurityPolicy.None;
    }

    /**
     * 提供身份验证
     */
    default IdentityProvider getIdentityProvider() {
        return new AnonymousProvider();
    }

    /**
     * 实际操作服务、由实现类重写实现
     */
    void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception;
}

