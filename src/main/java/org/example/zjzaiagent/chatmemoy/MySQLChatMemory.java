package org.example.zjzaiagent.chatmemoy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.agent.tool.P;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.domain.Logger;
import org.example.zjzaiagent.mapper.LoggerMapper;
import org.example.zjzaiagent.utils.MessageSerializer;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 自定义数据库持久化
 */
@Service
@Slf4j
public class MySQLChatMemory implements ChatMemory {

    @Resource
    private LoggerMapper loggerMapper;

    private final int maxMessage;

    public MySQLChatMemory() {
        this.maxMessage = 10;
    }

    public MySQLChatMemory(int maxMessage) {
        this.maxMessage = maxMessage;
    }
    /**
     * 添加一个数据到数据库中
     * @param conversationId
     * @param message
     */
    @Override
    public void add(String conversationId, Message message) {
        Logger logger = new Logger();
        logger.setId(conversationId);
        logger.setTime(new Date());
        logger.setMessage(MessageSerializer.serialize(message));
        loggerMapper.insert(logger);


    }

    //todo 优化防止重复插入
    /**
     * 添加多条数据到数据库中
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Logger> loggerList = new ArrayList<>();
        for (Message message : messages) {
            Logger logger = new Logger();
            logger.setId(conversationId);
            logger.setTime(new Date());
            logger.setMessage(MessageSerializer.serialize(message));
            loggerList.add(logger);
        }
        loggerMapper.insert(loggerList);
    }
    /**
     * 从数据库中获取数据
     * @param conversationId
     * @return
     */
    @Override
    public List<Message> get(String conversationId) {
        Page<Logger> page = new Page<>(1, maxMessage);
        QueryWrapper<Logger> wrapper = new QueryWrapper<>();
        wrapper.eq("id", conversationId)
                .orderByDesc("time");

        List<Logger> loggerList = loggerMapper.selectPage(page, wrapper).getRecords();
        if (loggerList == null || loggerList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Message> messages = new ArrayList<>();
        for (Logger logger : loggerList) {
            messages.add(MessageSerializer.deserialize(logger.getMessage()));
        }
        return messages;
    }


    /**
     * 从数据库中获取数据
     * 从数据库中获取倒数lastN条数据
     * @param conversationId
     * @param lastN
     * @return
     */
    public List<Message> get(String conversationId, int lastN) {
        Page<Logger> page = new Page<>(1, lastN);
        QueryWrapper<Logger> wrapper = new QueryWrapper<>();
        wrapper.eq("id", conversationId)
                .orderByDesc("time"); // 按时间倒序

        // 使用 selectPage 而非 selectList
        List<Logger> loggerList = loggerMapper.selectPage(page, wrapper).getRecords();

        List<Message> messages = new ArrayList<>();
        for (Logger logger : loggerList) {
            messages.add(MessageSerializer.deserialize(logger.getMessage()));
        }
        return messages;
    }

    /**
     * 从数据库中获取数据
     * 从数据库中获取倒数lastN条数据
     * @param conversationId
     * @param lastN
     * @return
     */
    public List<Message> getList(String conversationId, int lastN) {
        return get(conversationId, lastN);
    }

    /**
     * 清空数据
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
        QueryWrapper<Logger> loggerQueryWrapper = new QueryWrapper<>();
        loggerQueryWrapper.eq("id",conversationId);
        loggerMapper.delete(loggerQueryWrapper);

    }





}
