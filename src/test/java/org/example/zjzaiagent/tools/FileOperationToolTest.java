package org.example.zjzaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test.txt";
        String readFile = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull(readFile);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test.txt";
        String content = "hello world";
        String writeFile = fileOperationTool.writeFile(fileName, content);
        Assertions.assertNotNull(writeFile);
    }
}