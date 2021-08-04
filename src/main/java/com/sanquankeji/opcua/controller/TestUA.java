package com.sanquankeji.opcua.controller;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import java.util.Arrays;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * Created by Cryan on 2021/8/3.
 * TODO.
 */
public class TestUA {
    public static void main(String[] args) throws Exception{
        String EndPointUrl = "opc.tcp://127.0.0.1:49320";
        EndpointDescription[] endpointDescription = UaTcpStackClient.getEndpoints(EndPointUrl).get();
        EndpointDescription endpoint = Arrays.stream(endpointDescription)
                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getSecurityPolicyUri()))
                .findFirst().orElseThrow(() -> new Exception("没有节点返回"));
        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("Ning")) // opc ua 定义的名
                .setApplicationUri(EndPointUrl)// 地址
                .setEndpoint(endpoint)// 安全策略等配置
                .setRequestTimeout(uint(50000)) //等待时间
                .build();
        OpcUaClient opcClient = new OpcUaClient(config);
        opcClient.connect().get();
        NodeId nodeId_Tag1 = new NodeId(2, "通道 1.设备 1.标记 1");  //  namespaceIndex： 是获取的的下标   identifier： 是名称
        NodeId nodeId_Tag2 = new NodeId(2, "通道 1.设备 1.标记 2");
        DataValue value = opcClient.readValue(0.0, TimestampsToReturn.Both, nodeId_Tag1).get(); // 获取值
        DataValue value2 = opcClient.readValue(0.0, TimestampsToReturn.Both, nodeId_Tag2).get();
        System.out.println(value.getValue().getValue()); // 输出值
        System.out.println(value2.getValue().getValue());
    }
}
