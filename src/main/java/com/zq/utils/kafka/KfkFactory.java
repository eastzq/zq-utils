package com.zq.utils.kafka;

public class KfkFactory {

    /**
     * {@link KfkMqParam}
     * 
     * @param KfkMqParam 参数
     * @return
     */
    public static KfkConsumer getConsumer(KfkMqParam param) {
        return new KfkConsumer(param.getServerClusters(), param.getTopic(), param.getSubscriberName(),null);
    }

    /**
     * {@link KfkMqParam}
     * 
     * @param KfkMqParam 参数 partition 分区标识。
     * @return
     */
    public static KfkConsumer getConsumer(KfkMqParam param,Integer patition) {
        return new KfkConsumer(param.getServerClusters(), param.getTopic(), param.getSubscriberName(),patition);
    }
    
    /**
     * {@link KfkMqParam}
     * 
     * @param KfkMqParam 参数
     * @return
     */
    public static KfkProducer getProducer(KfkMqParam param) {
        return new KfkProducer(param.getServerClusters(), param.getTopic());
    }
    
    /**
     * {@link KfkMqParam}
     * 
     * @param KfkMqParam 参数
     * @return
     */
    public static KfkProducer2 getProducer2(KfkMqParam param) {
        return new KfkProducer2(param.getServerClusters(), param.getTopic());
    }
    /**
     * @param serverClusters kafka集群地址
     * @return 操作kafka属性客户端。
     */
    public static KfkClient getClient(String serverClusters) {
        return new KfkClient(serverClusters);
    }

}
