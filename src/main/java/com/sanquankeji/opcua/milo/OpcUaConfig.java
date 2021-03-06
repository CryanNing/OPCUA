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
 * @description:  ????????????  OPC UA  ????????????
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
     * @return opu ua ????????????
     * @throws Exception
     */
    @Bean
    public OpcUaClient createClient() throws Exception {

        // ?????????????????????
        String EndPointUrl = "opc.tcp://192.168.31.165:51210";
        //??????????????????
        EndpointDescription[] endpointDescription = UaTcpStackClient.getEndpoints(EndPointUrl).get();
        //???????????????????????????????????????????????????????????????????????????
        EndpointDescription endpoint = Arrays.stream(endpointDescription)
                .filter(e -> e.getSecurityPolicyUri().equals(None.getSecurityPolicyUri()))
                .findFirst().orElseThrow(() -> new Exception("no desired endpoints returned"));

        OpcUaClientConfig config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("test")) // opc ua ????????????
                .setApplicationUri(EndPointUrl)// ??????
                .setEndpoint(endpoint)// ?????????????????????
                .setRequestTimeout(uint(10000)) //????????????
                .build();

        OpcUaClient opcClient = new OpcUaClient(config);// ????????????\
        return opcClient;
    }

    private final AtomicLong clientHandles = new AtomicLong(1L);

    /**
     * opc ua  ??????????????????
     *
     * @throws Exception
     */
    @Bean
    public void createSubscription() throws Exception {
        // ??????OPC UA??????????????????
        OpcUaClient client = this.createClient();
        // ??????????????????
        client.connect().get();
        //?????????????????????

        //??????????????????1000ms???????????????
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(500.0).get();
        // ?????????????????????key
        List<String> key = new ArrayList<>();
        key.add("shifoukeyisaopiao");
        key.add("saopiaochenggong");

        for (int i = 0; i < key.size(); i++) {
            String node = key.get(i);
            //?????????????????????
            NodeId nodeId = new NodeId(2, node);
            ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);
            //?????????????????????
            MonitoringParameters parameters = new MonitoringParameters(
                    uint(1 + i),  // ??????????????????????????????key?????????
                    0.0,     // sampling interval
                    null,       // filter, null means use default
                    uint(10),   // queue size
                    true        // discard oldest
            );

            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);
            //?????????????????????????????????????????????????????????????????????
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
