/**  
 * @Title: ExcelReader.java
 * @Package com.shine.tech.epdce.csveditor.multiexplorer
 * @Description 
 * @author zq
 * @date 2021年1月20日 下午2:27:56
 * @Copyright 
 */

package com.zq.utils.multiexplorer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

/**
 * @Description
 * @author zq
 * @date 2021年1月20日 下午2:27:56
 * @see
 * @since 2021年1月20日 下午2:27:56
 */
public class ExcelReader {
    private static Logger logger = LoggerFactory.getLogger(ExcelWriter.class);

    /**
     * Excel文件流 --> List <Map<String,Object>> 对象 想直接转成java bean的朋友可以使用fastjson将
     * List<Map<String,Object>>转成bean对象
     *
     */

    private final static String excel2003L = ".xls"; // 2003- 版本的excel
    private final static String excel2007U = ".xlsx"; // 2007+ 版本的excel

    /**
     * 将流中的Excel数据转成List<Map>
     * 
     * @param in       输入流
     * @param fileName 文件名（判断Excel版本）
     * @param mapping  字段名称映射
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> parseExcel(InputStream in, String fileName, Map<String, String> mapping)
            throws Exception {
        // 根据文件名来创建Excel工作薄
        Workbook work = getWorkbook(in, fileName);
        if (null == work) {
            throw new Exception("创建Excel工作薄为空！");
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        // 返回数据
        List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();

        // 遍历Excel中所有的sheet
        for (int i = 0; i < work.getNumberOfSheets(); i++) {
            sheet = work.getSheetAt(i);
            if (sheet == null)
                continue;
            // 取第一行标题
            row = sheet.getRow(0);
            String title[] = null;
            if (row != null) {
                title = new String[row.getLastCellNum()];
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    title[y] = (String) getCellValue(cell);
                }
            } else
                continue;
            logger.info(JSON.toJSONString(title));

            // 遍历当前sheet中的所有行
            for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
                row = sheet.getRow(j);
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                // 遍历所有的列
                for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                    cell = row.getCell(y);
                    String key = title[y];
                    String fieldName = "";
                    if (mapping.get(key) == null) {
                        fieldName = key;
                    } else {
                        fieldName = mapping.get(key);
                    }
                    m.put(fieldName, getCellValue(cell));
                }
                ls.add(m);
            }
        }
        return ls;
    }

    /**
     * 将流中的Excel数据转成List<Map>
     * 
     * @param in       输入流
     * @param fileName 文件名（判断Excel版本）
     * @param mapping  字段名称映射
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> parseExcel(InputStream in, String fileName, Map<String, String> mapping,
            int sheetIndex) throws Exception {
        // 根据文件名来创建Excel工作薄
        Workbook work = getWorkbook(in, fileName);
        if (null == work) {
            throw new Exception("创建Excel工作薄为空！");
        }
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        // 返回数据
        List<Map<String, Object>> ls = new ArrayList<Map<String, Object>>();
        sheet = work.getSheetAt(sheetIndex);
        if (sheet == null) {
            return null;
        }
        // 取第一行标题
        row = sheet.getRow(0);
        String title[] = null;
        if (row != null) {
            title = new String[row.getLastCellNum()];
            for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                cell = row.getCell(y);
                title[y] = (String) getCellValue(cell);
            }
        }

        // 遍历当前sheet中的所有行
        for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
            row = sheet.getRow(j);
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            // 遍历所有的列
            for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                cell = row.getCell(y);
                String key = title[y];
                String fieldName = "";
                if (mapping.get(key) == null) {
                    fieldName = key;
                } else {
                    fieldName = mapping.get(key);
                }
                m.put(fieldName, getCellValue(cell));
            }
            ls.add(m);
        }
        return ls;
    }

    /**
     * 描述：根据文件后缀，自适应上传文件的版本
     * 
     * @param inStr ,fileName
     * @return
     * @throws Exception
     */
    public static Workbook getWorkbook(InputStream inStr, String fileName) throws Exception {
        Workbook wb = null;
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (excel2003L.equals(fileType)) {
            wb = new HSSFWorkbook(inStr); // 2003-
        } else if (excel2007U.equals(fileType)) {
            wb = new XSSFWorkbook(inStr); // 2007+
        } else {
            throw new Exception("解析的文件格式有误！");
        }
        return wb;
    }

    /**
     * 描述：对表格中数值进行格式化
     * 
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        Object value = null;
        DecimalFormat df = new DecimalFormat("0"); // 格式化number String字符
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd"); // 日期格式化
        DecimalFormat df2 = new DecimalFormat("0"); // 格式化数字

        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            value = cell.getRichStringCellValue().getString();
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                value = df.format(cell.getNumericCellValue());
            } else if ("m/d/yy".equals(cell.getCellStyle().getDataFormatString())) {
                value = sdf.format(cell.getDateCellValue());
            } else {
                value = df2.format(cell.getNumericCellValue());
            }
            break;
        case Cell.CELL_TYPE_BOOLEAN:
            value = cell.getBooleanCellValue();
            break;
        case Cell.CELL_TYPE_BLANK:
            value = "";
            break;
        default:
            break;
        }
        return value;
    }

    public static void main(String[] args) throws Exception {
        File file = new File("C:\\Users\\zq\\Desktop\\多浏览器\\多浏览器改造工作进展.xlsx");
        List<Map<String, Object>> ls = null;

        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("文件路径", "filePath");
        m.put("涉及页面", "pageDesc");
        m.put("自测情况", "isSelfTest");
        m.put("负责人", "responsible");
        m.put("完成日期", "completionDate");
        try (InputStream fis = new FileInputStream(file)) {
            ls = parseExcel(fis, file.getName(), m, 3);
        }
        List<String> columns = new ArrayList<String>();
        columns.addAll(m.values());
        ExcelWriter.genExcel(null, columns, ls,"D:/test.xlsx");
        System.out.println(JSON.toJSONString(ls));
    }
}
