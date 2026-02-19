package org.artisan.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandUtilityTest {

    @Test
    void runAsync_echo() throws Exception {
        String[] cmd = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? new String[] { "cmd", "/c", "echo", "hello" }
                : new String[] { "echo", "hello" };
        CommandUtility.CommandResult result = CommandUtility.runAsync(cmd).get(5, TimeUnit.SECONDS);
        assertTrue(result.exitCode() == 0);
        assertTrue(result.stdout().contains("hello"));
    }

    @Test
    void commandResult_isSuccess() {
        assertTrue(new CommandUtility.CommandResult(0, "", "").isSuccess());
        assertFalse(new CommandUtility.CommandResult(1, "", "").isSuccess());
    }
}
