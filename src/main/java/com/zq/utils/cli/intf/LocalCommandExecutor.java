package com.zq.utils.cli.intf;

import com.zq.utils.cli.ExecuteResult;

public interface LocalCommandExecutor {
    ExecuteResult executeCommand(String command, long timeout);
}
