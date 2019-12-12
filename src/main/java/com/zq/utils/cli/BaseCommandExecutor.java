package com.zq.utils.cli;

import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.zq.utils.cli.intf.CommandExecutor;

public abstract class BaseCommandExecutor implements CommandExecutor {

    public static ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public static final String cmdPrefix = "cmd";

    protected String encoding = "UTF-8";

    public BaseCommandExecutor() {
    }

    public BaseCommandExecutor(String encoding) {
        if (StringUtils.isNoneBlank(encoding)) {
            this.encoding = encoding;
        }
    }

    public boolean isWindows() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            return true;
        }
        return false;
    }

    public String[] addWindowsCommandPrefix(String[] cmdarray) {
        String command = StringUtils.join(cmdarray, " ");
        if (isWindows() && !command.contains(cmdPrefix)) {
            command = cmdPrefix + " /c " + command;
            String[] f = new String[] { cmdPrefix, "/c", };
            cmdarray = concat(f, cmdarray);
        }
        return cmdarray;
    }

    public String[] concat(String[] a, String[] b) {
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public String[] splitCommand(String command) {
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++)
            cmdarray[i] = st.nextToken();
        return cmdarray;
    }

}
