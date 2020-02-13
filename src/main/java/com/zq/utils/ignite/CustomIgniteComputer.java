package com.zq.utils.ignite;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * ignite异步处理任务
 * 
 * @author zq
 *
 */
public class CustomIgniteComputer {

    private static final int PARTITION_NUM = 1024;

    private static Logger logger = LoggerFactory.getLogger(CustomIgniteComputer.class);

    private Ignite ignite;

    private JSONObject json;

    public CustomIgniteComputer(Ignite ignite, JSONObject json) {
        this.ignite = ignite;
        this.json = json;
    }

    public long execute() {
        logger.debug("准备server执行任务...");
        String cacheName = this.json.getJSONObject("reader").getString("cacheName");
        long beginTime = System.currentTimeMillis();
        IgniteCompute computer = ignite.compute();
        List<IgniteFuture<Void>> igniteFutures = new ArrayList<>();
        for (int partition = 0; partition < PARTITION_NUM; partition++) {
            IgniteFuture<Void> future = computer.affinityCallAsync(Arrays.asList(cacheName), partition,
                    new IgnitePartitionExpCallable(ignite, partition, this.json));
            igniteFutures.add(future);
        }
        // 等待获取完毕！
        logger.debug("等待server执行结果...");
        for (IgniteFuture<Void> future : igniteFutures) {
            future.get();
        }
        long endTime = System.currentTimeMillis() - beginTime;
        logger.debug("任务处理完毕，处理时长：{}", endTime);
        return endTime;
    }

    public long execute1() throws Exception {
        logger.debug("准备执行任务...");
        long beginTime = System.currentTimeMillis();
        new IgnitePartitionExpCallable(this.ignite, 1, this.json).call();
        long useTime = System.currentTimeMillis() - beginTime;
        logger.debug("任务处理完毕，处理时长：{}", useTime);
        return useTime;
    }

    public static Ignite getIgniteClient() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(Arrays.asList("10.168.0.221:47501"));
        spi.setIpFinder(ipFinder);
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setPeerClassLoadingEnabled(true);
        cfg.setDiscoverySpi(spi);
        // 显式配置client模式启动该节点.
        cfg.setClientMode(true);
        return Ignition.start(cfg);
    }

}
