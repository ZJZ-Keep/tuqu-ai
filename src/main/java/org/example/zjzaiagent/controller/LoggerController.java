package org.example.zjzaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.example.zjzaiagent.app.PlanApp;
import org.example.zjzaiagent.chatmemoy.MySQLChatMemory;
import org.example.zjzaiagent.domain.Logger;
import org.example.zjzaiagent.mapper.LoggerMapper;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/logger")
public class LoggerController {

    @Resource
    private LoggerMapper loggerMapper;
    @Resource
    PlanApp planApp;
    @Resource
    MySQLChatMemory mySQLChatMemory;
    @GetMapping("/selectAll")
    public String selectAll() {
        //取出数据库的message
        List<Logger> loggerList = loggerMapper.selectList(null);
        String message = loggerList.get(0).getMessage();
        //将 Base64 字符串解析为原始文字
         //1. 解码Base64字符串
        byte[] decodedBytes = Base64.getDecoder().decode(message);
         //2. 将字节数组转换为字符串
        String decodedString = new String(decodedBytes);
        //返回
        return decodedString;
    }
}
