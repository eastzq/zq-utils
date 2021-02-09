/**  
 * @Title: CsvUtil.java
 * @Package com.shine.tech.epdce.csveditor
 * @Description 
 * @author zq
 * @date 2021年1月20日 下午1:58:44
 * @Copyright 
 */

package com.zq.utils.multiexplorer.utils;

import com.alibaba.fastjson.JSON;
import com.csvreader.CsvReader;
import com.zq.utils.multiexplorer.Rule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description
 * @author zq
 * @date 2021年1月20日 下午1:58:44
 * @see
 * @since 2021年1月20日 下午1:58:44
 */
public class CsvUtil {

    public CsvUtil() {
    }

    public static <T> List<T> loadConfig(String path, Class<T> clazz) {
        try (InputStream inputStream = new FileInputStream(new File(path))) {
            CsvReader csvReader = new CsvReader(inputStream, StandardCharsets.UTF_8);
            if (!csvReader.readHeaders()) {
                throw new RuntimeException("读取配置文件" + path + "数据为空");
            }
            String[] headers = csvReader.getHeaders();
            List<T> dataList = new ArrayList<>();
            while (csvReader.readRecord()) {
                dataList.add(readLine(csvReader.getValues(), headers, clazz));
            }

            return dataList;
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("配置文件解析加载出现错误，{0}", e);
        }
    }

    public static <T> List<T> loadConfig(InputStream inputStream, Class<T> clazz) {
        try {
            CsvReader csvReader = new CsvReader(inputStream, StandardCharsets.UTF_8);
            if (!csvReader.readHeaders()) {
                throw new RuntimeException("读取配置文件数据为空");
            }
            String[] headers = csvReader.getHeaders();
            List<T> dataList = new ArrayList<>();
            while (csvReader.readRecord()) {
                dataList.add(readLine(csvReader.getValues(), headers, clazz));
            }

            return dataList;
        } catch (IOException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("配置文件解析加载出现错误，{0}", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> T readLine(String[] line, String[] headers, Class<T> clazz)
            throws InstantiationException, IllegalAccessException {
        T obj = clazz.newInstance();
        Map<String, Object> orgi = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            orgi.put(headers[i], line[i]);
        }
        BeanCopyUtil.copyMap(obj, orgi);
        return obj;
    }

    public static void main(String[] args) {
        List<Rule> rules = loadConfig("C:\\Users\\zq\\Desktop\\多浏览器\\multiExplore.csv", Rule.class);
        System.out.println(JSON.toJSONString(rules));
    }

}
