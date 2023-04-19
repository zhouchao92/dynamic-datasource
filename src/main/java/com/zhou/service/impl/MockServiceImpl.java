package com.zhou.service.impl;

import com.zhou.repository.mapper.UserMapper;
import com.zhou.service.MockService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * MockServiceImpl
 *
 * @author 周超
 * @since 2023/4/18 12:53
 */
@Service
public class MockServiceImpl implements MockService {

    @Resource
    private UserMapper userMapper;

    @Override
    public String listUsers() {
        return userMapper.listUsers().toString();
    }
}
