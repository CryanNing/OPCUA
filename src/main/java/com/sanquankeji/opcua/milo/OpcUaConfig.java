package com.sanquankeji.opcua.milo;

/**
 * Created by Cryan on 2021/8/4.
 * TODO.
 */
import com.sanquankeji.opcua.util.RedisUtil;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.security.SecurityPolicy.None;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * @description:  订阅模式  OPC UA  获取数据
 * @author: hcq
 * @createDate: 2021/4/27
 * @version: 1.0
 */

@Configuration
public class OpcUaConfig {

    @Autowired
    private RedisUtil redisUtil;

    @Bean
    public RedisUtil transferService() {
        return redisUtil;
    }
    /**
     * @return opu ua 准备连接
     * @throws Exception
     */
    @Bean
    public OpcUaClient createClient() throws Exception {

        // 连接地址端口号
        String EndPointUrl = "opc.tcp://192.168.31.165:51210";
        //安全策略选择
        EndpointDescription[] endpointDescription = UaTcpStackClient.getEndpoints(EndPointUrl).get();
        //过滤掉不需要的安全策略，选择一个自己需要的安全策略
        EndpointDescription endpoint = Arrays.stream(endpointDescription)
                .filter(e -> e.getSecurityPolicyUri().equals(None.getSecurityPolicyUri()))
                .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("test")) // opc ua 定义的名
                .setApplicationUri(EndPointUrl)// 地址
                .setEndpoint(endpoint)// 安全策略等配置
                .setRequestTimeout(uint(10000)) //等待时间
                .build();

        OpcUaClient opcClient = new OpcUaClient(config);// 准备连接\
        return opcClient;
    }

    private final AtomicLong clientHandles = new AtomicLong(1L);

    /**
     * opc ua  打开连接订阅
     *
     * @throws Exception
     */
    @Bean
    public void createSubscription() throws Exception {
        // 获取OPC UA服务器的数据
        OpcUaClient client = this.createClient();
        // 同步建立连接
        client.connect().get();
        //创建监控项请求

        //创建发布间隔1000ms的订阅对象
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(500.0).get();
        // 你所需要订阅的key
        List<String> key = new ArrayList<>();
        key.add("shifoukeyisaopiao");
        key.add("saopiaochenggong");

        for (int i = 0; i < key.size(); i++) {
            String node = key.get(i);
            //创建订阅的变量
            NodeId nodeId = new NodeId(2, node);
            ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
            //创建监控的参数
            MonitoringParameters parameters = new MonitoringParameters(
                    uint(1 + i),  // 为了保证唯一性，否则key值一致
                    0.0,     // sampling interval
                    null,       // filter, null means use default
                    uint(10),   // queue size
                    true        // discard oldest
            );

            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
            //创建监控项，并且注册变量值改变时候的回调函数。
            List<UaMonitoredItem> items = subscription.createMonitoredItems(
                    TimestampsToReturn.Both,
                    newArrayList(request),
                    (item, id) -> {
                        item.setValueConsumer((is, value) -> {
                            String nodeName = item.getReadValueId().getNodeId().getIdentifier().toString();
                            String nodeValue = value.getValue().getValue().toString();
                            redisUtil.set(nodeName,nodeValue);
                        });
                    }).get();
        }

    }
}
