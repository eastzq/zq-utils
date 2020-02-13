package com.zq.utils.ignite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据到bcp文件。
 * 
 * @author zq
 *
 */

public class WriterUtil {

    private static final Logger logger = LoggerFactory.getLogger(WriterUtil.class);

    public static void save2BcpFile(List<Map<String, Object>> resultList, List<String> columns, String filepath) {
        File file = new File(filepath);
        try {
            if (file.exists()) {
                appendBcpFile(resultList, columns, filepath);
            } else {
                createBcpFile(resultList, columns, filepath);
            }
        } catch (IOException e) {
            throw new RuntimeException("写入bcp文件出现异常，原因：" + e.getMessage(), e);
        }
    }

    private static void createBcpFile(List<Map<String, Object>> resultList, List<String> columns, String filepath)
            throws IOException {
        File file = new File(filepath);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!file.createNewFile()) {
                throw new IOException("文件创建失败，文件名称：" + filepath);
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(getColumnStrings(columns, SplitSign.FIELD_DELIMITER, SplitSign.FIELD_ENCLOSED));
            bw.write(SplitSign.NEW_LINE);
            for (int i = 0; i < resultList.size(); i++) {
                Map<String, Object> rowData = resultList.get(i);
                String recordStr = getRecordStrings(columns, rowData, SplitSign.FIELD_DELIMITER,
                        SplitSign.FIELD_ENCLOSED);
                bw.write(recordStr);
                bw.write(SplitSign.NEW_LINE);
            }
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                logger.error("关闭io异常", e);
            }
        }

    }

    private static void appendBcpFile(List<Map<String, Object>> resultList, List<String> columns, String filepath)
            throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        File file = new File(filepath);
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            for (int i = 0; i < resultList.size(); i++) {
                Map<String, Object> rowData = resultList.get(i);
                String recordStr = getRecordStrings(columns, rowData, SplitSign.FIELD_DELIMITER,
                        SplitSign.FIELD_ENCLOSED);
                bw.write(recordStr);
                bw.write(SplitSign.NEW_LINE);
            }
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                logger.error("关闭io异常", e);
            }
        }

    }

    private static String getRecordStrings(List<String> columns, Map<String, Object> rowData, String fieldDelimeter,
            String fieldEnclosed) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            String fieldValue = (String) rowData.get(column);
            fieldValue = fieldValue == null ? "" : fieldValue;
            fieldValue = fieldValue.trim();
            builder.append(fieldEnclosed).append(fieldValue).append(fieldEnclosed);
            if (i < columns.size() - 1) {
                builder.append(fieldDelimeter);
            }
        }
        return builder.toString();
    }

    private static String getColumnStrings(List<String> columns, String fieldDelimeter, String fieldEnclosed) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            builder.append(fieldEnclosed).append(column).append(fieldEnclosed);
            if (i < columns.size() - 1) {
                builder.append(fieldDelimeter);
            }
        }
        return builder.toString();
    }

}
