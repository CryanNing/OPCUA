package com.sanquankeji.opcua.milo;

import com.sanquankeji.opcua.service.OpcUAClientService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.springframework.stereotype.Service;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;



/**
 * Created by Cryan on 2021/8/4.
 * TODO.
 */

@Service("OpcUAClientRunner")
@Slf4j
public class OpcUAClientRunner {

    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

    private final OpcUAClientService opcUAClientService;

    public OpcUAClientRunner(OpcUAClientService opcUAClientService) {
        this.opcUAClientService = opcUAClientService;
    }

    /**
     * OPC UA的运行入口程序
     */
    public void run() {
        try {
            // 创建OPC UA客户端
            OpcUaClient opcUaClient = createClient();
            // future执行完毕后，异步判断状态
            future.whenCompleteAsync((c, ex) -> {
                if (ex != null) {
                    log.error("连接OPC UA服务错误: {}", ex.getMessage(), ex);
                }
                // 关闭OPC UA客户端
                try {
                    opcUaClient.disconnect().get();
                    Stack.releaseSharedResources();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("OPC UA服务关闭错误: {}", e.getMessage(), e);
                }
            });

            try {
                // 获取OPC UA服务器的数据
                opcUAClientService.run(opcUaClient, future);
                future.get(5, TimeUnit.SECONDS);
            } catch (Throwable t) {
                log.error("OPC UA客户端运行错误: {}", t.getMessage(), t);
                future.completeExceptionally(t);
            }
        } catch (Throwable t) {
            log.error("OPC UA客户端创建错误: {}", t.getMessage(), t);
            future.completeExceptionally(t);
        }
    }

    /**
     * 创建OPC UA的服务连接对象
     */
    private OpcUaClient createClient() throws Exception {
        Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security");
        Files.createDirectories(securityTempDir);
        if (!Files.exists(securityTempDir)) {
            throw new Exception("不能够创建安全路径: " + securityTempDir);
        }
        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);
        // 获取OPC UA的服务器端节点
        EndpointDescription[] endpoints =
                UaTcpStackClient.getEndpoints(opcUAClientService.getEndpointUrl()).get();
        EndpointDescription endpoint = Arrays.stream(endpoints)
                .filter(e -> e.getEndpointUrl().equals(opcUAClientService.getEndpointUrl()))
                .findFirst().orElseThrow(() -> new Exception("没有节点返回"));

        // 设置OPC UA的配置信息
        OpcUaClientConfig config =
                OpcUaClientConfig.builder()
                        .setApplicationName(LocalizedText.english("OPC UA SCREEN"))
                        .setApplicationUri("urn:eclipse:milo:examples:client")
                        .setCertificate(loader.getClientCertificate())
                        .setKeyPair(loader.getClientKeyPair())
                        .setEndpoint(endpoint)
                        .setIdentityProvider(new UsernameProvider("opcua", "00123"))
                        .setRequestTimeout(uint(5000))
                        .build();
        // 创建OPC UA客户端
        return new OpcUaClient(config);
    }
}

