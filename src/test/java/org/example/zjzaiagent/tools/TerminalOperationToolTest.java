package org.example.zjzaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        String executeTerminalCommand = terminalOperationTool.executeTerminalCommand("ls");
        assertNotNull(executeTerminalCommand);
    }
}