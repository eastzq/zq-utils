package com.zq.runnablejar.intf;

import com.zq.runnablejar.ExecuteResult;

public interface LocalCommandExecutor {
    ExecuteResult executeCommand(String command, long timeout);
}
