package com.zhou.controller;

import com.zhou.service.MockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * MockController
 * 测试：http://localhost:8080/mock/request?source=1
 *
 * @author 周超
 * @since 2023/4/19 10:19
 */
@RestController
@RequestMapping("/mock")
public class MockController {

    @Resource
    private MockService mockService;

    @GetMapping("/request")
    public String listUsers() {
        return mockService.listUsers();
    }


}
