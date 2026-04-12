package org.example.zjzaiagent.chatmemoy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件持久化的对话记忆
 */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;
    private final int maxMessages;
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录和窗口大小
    public FileBasedChatMemory(String dir, int maxMessages) {
        this.BASE_DIR = dir;
        this.maxMessages = maxMessages;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    // 构造对象时，指定文件保存目录，默认窗口大小为 10
    public FileBasedChatMemory(String dir) {
        this(dir, 10);
    }

    @Override
    public void add(String conversationId, Message message) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.add(message);
        // 如果消息数量超过窗口大小，删除最早的消息
        while (conversationMessages.size() > maxMessages) {
            conversationMessages.remove(0);
        }
        saveConversation(conversationId, conversationMessages);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        // 如果消息数量超过窗口大小，删除最早的消息
        while (conversationMessages.size() > maxMessages) {
            conversationMessages.remove(0);
        }
        saveConversation(conversationId, conversationMessages);
    }


    @Override
    public List<Message> get(String conversationId) {
        List<Message> allMessages = getOrCreateConversation(conversationId);
        int size = allMessages.size();
        int startIndex = Math.max(0, size - maxMessages);
        return allMessages.subList(startIndex, size);
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    // 从文件中加载对话
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    // 保存对话到文件中
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}