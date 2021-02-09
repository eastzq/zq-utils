/**  
 * @Title: IgniteTestCallable.java
 * @Package com.shine.ignite.test
 * @Description BLX_TODO
 * @author bailixiang
 * @date 2020年2月6日 上午11:38:24
 * @Copyright
 */

package com.zq.utils.ignite;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.lang.IgniteCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.zq.utils.cli.ExecuteResult;
import com.zq.utils.cli.ProcessBuilderCommandExecutor;
import com.zq.utils.cli.intf.CommandExecutor;

public class IgnitePartitionExpCallable implements IgniteCallable<Void> {

    private static Logger logger = LoggerFactory.getLogger(IgnitePartitionExpCallable.class);

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 2000;

    private static final long CMD_WAITING_MILLIS = 500000;

    private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int partition;

    private Ignite ignite;

    private String sqlScript;

    private String cacheName;

    private String tableName;

    private String writeColumnStrs;
    private List<String> writeColumns;

    private String targetDir;
    // 固定路径/缓存名称/表名_分区.bcp
    private String filePath;

    private JSONObject readerJson;
    private JSONObject writerJson;

    public IgnitePartitionExpCallable(Ignite ignite, Integer partition, JSONObject json) {

        this.partition = partition;
        this.ignite = ignite;

        this.readerJson = json.getJSONObject("reader");
        this.writerJson = json.getJSONObject("writer");

        this.cacheName = this.readerJson.getString("cacheName");
        this.tableName = this.readerJson.getString("tableName");
        this.targetDir = this.readerJson.getString("targetDir");
        this.writeColumnStrs = this.writerJson.getString("columnStrs");
        this.writeColumns = Arrays.asList(this.writeColumnStrs.split(","));

        this.filePath = this.genWritePath();
        this.buildSqlScript();
    }

    private void buildSqlScript() {
        this.sqlScript = String.format("select %s from %s", this.readerJson.getString("columnStrs"), this.tableName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Void call() throws Exception {
        try {
            startRead();
            startWrite();
        } catch (Exception e) {
            logger.error("数据处理失败，原因：{}", e);
            throw new Exception("任务执行失败！原因：" + e.getMessage(), e);
        } finally {
        }
        return null;
    }

    private void startRead() {
        logger.info("export ignite data...");
        SqlFieldsQuery querySql = new SqlFieldsQuery(this.sqlScript);
        querySql.setPartitions(partition);
        querySql.setPageSize(DEFAULT_PAGE_SIZE);
        querySql.setSchema(this.cacheName);
        querySql.setLazy(true);
        // Iterate over the result set.
        FieldsQueryCursor<List<?>> cursor = null;
        IgniteCache cache = null;
        List<Map<String, Object>> recordBatch = new ArrayList<>();
        int i = 0;
        try {
            cache = ignite.cache(this.cacheName);
            cursor = cache.query(querySql);
            for (List<?> row : cursor) {
                Map rowData = this.buildRecord(row);
                recordBatch.add(rowData);
                i++;
                if (i % DEFAULT_PAGE_SIZE == 0) {
                    WriterUtil.save2BcpFile(recordBatch, this.writeColumns, this.filePath);
                    recordBatch.clear();
                }
            }
            if (!recordBatch.isEmpty()) {
                WriterUtil.save2BcpFile(recordBatch, this.writeColumns, this.filePath);
                recordBatch.clear();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        logger.info("end export ignite data!");
    }

    /**
     * 导入bcp文件
     */
    private void startWrite() {
        File file = new File(this.filePath);
        if (file.exists()) {
            loadByCli();
        }
    }

    private String genWritePath() {
        this.checkPathDir();
        String fileName = this.tableName + "_" + this.partition + ".bcp";
        String t = StringUtils.join(new String[] { this.targetDir, this.cacheName, fileName }, File.separator);
        File file = new File(t);
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("删除目标文件失败，文件路径：" + t);
            }
        }
        return t;
    }

    private void checkPathDir() {
        String p = StringUtils.join(new String[] { this.targetDir, this.cacheName }, File.separator);
        File file = new File(p);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("创建文件夹失败，路径：" + p);
        }
    }

    private Map<String, Object> buildRecord(List list) {
        if (list.size() != this.writeColumns.size()) {
            throw new RuntimeException("配置项错误，查询字段个数与写入的字段个数不相等！");
        }
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (Object o : list) {
            String s = "";
            if (o instanceof Date) {
                s = sf.format((Date) o);
            } else if (o != null) {
                s = String.valueOf(o);
            }
            String column = this.writeColumns.get(i);
            map.put(column, s);
            i++;
        }
        return map;
    }

    private void loadByCli() {
        String table = writerJson.getString("tableName");
        StringBuilder sb = new StringBuilder();
        sb.append("LOAD DATA local INFILE ").append(" '").append(this.filePath.replaceAll("\\\\", "/")).append("' ");
        sb.append(" IGNORE INTO TABLE ").append(table);
        sb.append(" FIELDS TERMINATED BY ").append(" '").append(SplitSign.FIELD_DELIMITER).append("' ");
        sb.append(" ENCLOSED BY ").append(" '").append(SplitSign.FIELD_ENCLOSED).append("' ");
        sb.append(" LINES TERMINATED BY ").append(" '").append(SplitSign.NEW_LINE).append("' ");
        sb.append(" IGNORE ").append(SplitSign.SKIP_ROW).append(" LINES ");
        sb.append("(").append(this.writeColumnStrs).append(")");

        StringBuilder csb = new StringBuilder();
        csb.append("mysql");
        csb.append(" -h ").append(writerJson.getString("ip"));
        csb.append(" -P ").append(writerJson.getString("port"));
        csb.append(" -u").append(writerJson.getString("username"));
        csb.append(" -p").append(writerJson.getString("password"));
        csb.append(" ").append(writerJson.getString("instance"));
        csb.append(" -e ").append("statement");

        CommandExecutor ce = new ProcessBuilderCommandExecutor();
        String[] cmdarray = csb.toString().split(" ");
        cmdarray[cmdarray.length - 1] = sb.toString();
        ExecuteResult er = ce.executeCommand(cmdarray, CMD_WAITING_MILLIS);
        if (er.getExitCode() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("命令执行结果：{}", er.getExecuteOut());
            }
        } else {
            logger.error(er.getExecuteOut());
            throw new RuntimeException(
                    String.format("执行载入文件到mysql命令出现异常！原因：%s，文件名称：%s", er.getExecuteOut(), this.filePath));
        }
    }

}
