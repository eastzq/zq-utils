package com.zq.utils.kafka.monitor;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.zq.utils.kafka.KfkClient;
import com.zq.utils.kafka.KfkMqParam;

public class KfkMessageMonitor {

    private KfkMqParam mqParam;

    private KfkClient client;

    private boolean isRunning = false;

    private Map<String, KfkMessageOffset> offsets;

    private Future<Void> future;

    private static final Logger logger = LoggerFactory.getLogger(KfkMessageMonitor.class);

    private boolean isDebugEnable = false;

    public KfkMessageMonitor(KfkMqParam mqParam) {
        this.mqParam = mqParam;
        this.client = new KfkClient(this.mqParam.getServerClusters());
    }

    public KfkMessageMonitor(KfkMqParam mqParam, boolean isDebugEnable) {
        this(mqParam);
        this.isDebugEnable = isDebugEnable;
    }


    
    public void startWatch() {
        if (this.isRunning) {
            return;
        }
        ExecutorService executor = null;
        try {
            this.client.subscribe(mqParam.getSubscriberName(), mqParam.getTopic());
            executor = Executors.newSingleThreadExecutor(new CustomThreadFactory("消息进度监控"+mqParam.getTopic()+"_"+mqParam.getSubscriberName()));
            this.future = executor.submit(new MonitorRunner()); 
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
        this.isRunning = true;
    }
    
    public void close() {
        if (this.isRunning) {
            future.cancel(true);
            this.client.close();
            this.isRunning = false;
        }
    }

    public boolean isFinishAllLog() {
        if (!this.isRunning || offsets == null) {
            return false;
        }

        boolean isFinished = true;
        for (Entry<String, KfkMessageOffset> entry : offsets.entrySet()) {
            KfkMessageOffset value = entry.getValue();
            if (value.getOffset() != value.getTotal()) {
                isFinished = false;
                break;
            }
        }

        // 判断监控线程是否出现异常
        if (future.isDone()) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException("监控线程执行出现异常！获取消息消费进展失败，原因：" + e.getMessage(), e.getCause());
            }
        }
        // 判断其他线程是否出现异常，如果出现异常则跳出
        if (future.isDone() && !isFinished) {
            throw new RuntimeException("监控线程被中断，消息未处理完毕！可能原因：原因：存在处理线程出现异常！");
        }
        return isFinished;
    }

    /**
     * 定时获取消息消费情况。
     * 
     * @author zq
     *
     */
    private class MonitorRunner implements Callable<Void> {

        private static final long SLEEP_TIME = 8000;

        @Override
        public Void call() throws Exception {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    logger.debug("kafka消费监控线程，响应中断！准备退出...");
                    return null;
                }
                KfkMessageMonitor.this.offsets = KfkMessageMonitor.this.client
                        .getLatestOffsetStatus(mqParam.getSubscriberName(), mqParam.getTopic());
                if (isDebugEnable) {
                    logger.debug("监控线程数据打印！topic：{}，subscriber：{}。消费进度：{}", mqParam.getTopic(),
                            mqParam.getSubscriberName(), JSON.toJSONString(offsets));
                }
                Thread.sleep(SLEEP_TIME);
            }
        }

    }
}
