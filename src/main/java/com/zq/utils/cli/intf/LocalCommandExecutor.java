package com.zq.utils.cli.intf;

import java.io.File;

import com.zq.utils.cli.ExecuteResult;

public interface LocalCommandExecutor {
    ExecuteResult executeCommand(String command, long timeout);
    ExecuteResult executeCommand(String command, long timeout, String[] envp, File dir);
}
