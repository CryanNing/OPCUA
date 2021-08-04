package com.sanquankeji.opcua.serviceimpl;

import com.google.common.collect.ImmutableList;
import com.sanquankeji.opcua.service.OpcUAClientService;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
/**
 * Created by Cryan on 2021/8/4.
 * TODO.
 */
@Service("OpcUAClientService")
public class OpcUAClientServiceImpl implements OpcUAClientService {

    /**
     * 覆盖接口的方法，建立和OPC UA的服务
     */
    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // 同步建立连接
        client.connect().get();

        // 异步读取数据
        readTagData(client).thenAccept(values -> {
            DataValue nodeId_Tag1 = values.get(0);
            DataValue nodeId_Tag2 = values.get(1);
            System.out.println("#########Tag1=" + nodeId_Tag1.getValue().getValue());
            System.out.println("#########Tag2=" + nodeId_Tag2.getValue().getValue());
            future.complete(client);
        });
    }

    /**
     * 读取标签点的数据
     */
    private CompletableFuture<List<DataValue>> readTagData(OpcUaClient client) {
       // NodeId nodeId_Tag1 = new NodeId(2, "Channel1.Device1.Tag1");
      //  NodeId nodeId_Tag2 = new NodeId(2, "Channel1.Device1.Tag2");
       // NodeId nodeId_Tag1 = new NodeId(2, "通道2.设备1.tag1");
       // NodeId nodeId_Tag2 = new NodeId(2, "通道1.设备1.标记2");
        NodeId nodeId_Tag1 = new NodeId(2, "通道 1.设备 1.标记 1");
        NodeId nodeId_Tag2 = new NodeId(2, "通道 1.设备 1.标记 2");

        List<NodeId> nodeIds = ImmutableList.of(nodeId_Tag1, nodeId_Tag2);
        return client.readValues(0.0, TimestampsToReturn.Both, nodeIds);
    }
}

