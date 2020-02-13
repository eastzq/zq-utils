package com.zq.utils.kafka.monitor;

/**
 * 
 * @author zq
 * kafka 消息消费进度监控！
 */
public class KfkMessageOffset {
    
    /**
     * 分区代称
     */
    private String partitionName;
    
    /**
     * 最新消费的位置
     */
    private long offset;
    
    /**
     * 消息总数目
     */
    private long total;
    
    public KfkMessageOffset(String partitionName,long offset,long total) {
        this.partitionName = partitionName;
        this.offset = offset;
        this.total = total;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
