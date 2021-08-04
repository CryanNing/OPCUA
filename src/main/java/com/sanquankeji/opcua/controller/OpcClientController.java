package com.sanquankeji.opcua.controller;

import com.sanquankeji.opcua.milo.OpcUAClientRunner;
import com.sanquankeji.opcua.service.OpcUAClientService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Cryan on 2021/8/4.
 * TODO.
 */
@RestController
@Slf4j
public class OpcClientController {
    @Autowired
    private OpcUAClientService opcUAClientService;

    @RequestMapping("/")
    public void getTest(){
        log.info("开始获取数据");
        new OpcUAClientRunner(opcUAClientService).run();
    }
}
