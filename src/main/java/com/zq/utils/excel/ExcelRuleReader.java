package com.zq.utils.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * excel转sql脚本工具类。
 * 
 * @author zq
 *
 */
public class ExcelRuleReader {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";
    private static String DIR = "C:\\Users\\86134\\Desktop\\excel配置表2";

    private static final Logger logger = LoggerFactory.getLogger(ExcelRuleReader.class);

    /**
     * 判断Excel的版本,获取Workbook
     * 
     * @param in
     * @param filename
     * @return
     * @throws IOException
     */
    public static Workbook getWorkbok(InputStream in, File file) throws IOException {
        Workbook wb = null;
        if (file.getName().endsWith(EXCEL_XLS)) { // Excel 2003
            wb = new HSSFWorkbook(in);
        } else if (file.getName().endsWith(EXCEL_XLSX)) { // Excel 2007/2010
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }

    /**
     * 判断文件是否是excel
     * 
     * @throws Exception
     */
    public static void checkExcelVaild(File file) throws Exception {
        if (!file.exists()) {
            throw new Exception("文件不存在");
        }
        if (!(file.isFile() && (file.getName().endsWith(EXCEL_XLS) || file.getName().endsWith(EXCEL_XLSX)))) {
            throw new Exception("文件不是Excel");
        }
    }

    /**
     * 读取Excel测试，兼容 Excel 2003/2007/2010
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        File excelDir = new File(DIR); // 创建文件对象
        String resultFilePath = excelDir + File.separator + "result.sql";
        if (!excelDir.exists() || !excelDir.isDirectory()) {
            return;
        }
        File resultFile = new File(resultFilePath);
        if (resultFile.exists() && resultFile.delete()) {
        }

        File[] files = excelDir.listFiles();
        List<File> targetFiles = new ArrayList<>();
        for (File f : files) {
            if (f.getName().endsWith(EXCEL_XLS) || f.getName().endsWith(EXCEL_XLSX)) {
                targetFiles.add(f);
            }
        }
        for (File file : targetFiles) {
            FileInputStream fis = null;
            try {
                String tableName = getFileName(file);
                fis = new FileInputStream(file);
                Workbook workbook = getWorkbok(fis, file);
                String columnStr = getColumnString(workbook);
                String[] columns = StringUtils.split(columnStr, ",");
                String prefix = "INSERT INTO " + tableName + "(" + columnStr + ")\n";
                String sufix = "\n\n\n\n";
                int sheetCount = workbook.getNumberOfSheets();
                for (int c = 0; c < sheetCount; c++) {
                    List<String> all = new ArrayList<>();
                    Sheet sheet = workbook.getSheetAt(c);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row.getCell(0) == null) {
                            break;
                        }
                        Map<String, Object> m = new HashMap<>();
                        for (int j = 0; j < columns.length; j++) {
                            Object obj = null;
                            Cell cell = row.getCell(j);
                            if (cell == null) {
                                obj = "";
                            } else {
                                obj = getValue(cell);
                            }
                            m.put(columns[j], obj.toString().trim());
                        }
                        wrap(m);
                        List<String> t = new ArrayList<>();
                        for (int j = 0; j < columns.length; j++) {
                            Object obj = m.get(columns[j]);
                            t.add("'" + obj.toString() + "'");
                        }
                        all.add(" SELECT " + StringUtils.join(t, ",") + " FROM DUAL ");
                    }
                    operate(resultFilePath, prefix + StringUtils.join(all, "\n UNION ALL") + sufix);
                }
            } finally {
                fis.close();
            }
        }
    }

    public static String getFileName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf("."));
    }

    public static Map<String, Object> wrap(Map<String, Object> m) {
        String t = (String) m.get("RULE_CONTENT");
        String n = (String) m.get("RULE_RESULT_ITEM");
        String vs = null;
        if (t.startsWith("return")) {
            vs = t.substring("return".length()+1,t.length()-1);
            vs ="setValue(\""+n+"\","+vs+");";
        }else {
            vs = t;
        }
        m.put("RULE_CONTENT", vs);
        return m;
    }

    private static Object getValue(Cell cell) {
        Object obj = null;
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_BOOLEAN:
            obj = cell.getBooleanCellValue();
            break;
        case Cell.CELL_TYPE_ERROR:
            obj = cell.getErrorCellValue();
            break;
        case Cell.CELL_TYPE_NUMERIC:
            obj = cell.getNumericCellValue();
            obj = ((Double) obj).intValue();
            break;
        case Cell.CELL_TYPE_STRING:
            String t = cell.getStringCellValue();
            obj = t.replaceAll("\r", "").replaceAll("\n", ";");
            break;
        default:
            obj = cell.getStringCellValue();
            break;
        }
        if (obj == null) {
            obj = "";
        }
        return obj;
    }

    private static void operate(String filePath, String content) throws Exception {
        logger.debug("准备将处理结果写入到sql文件中");
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtils.write(file, content, true);
    }

    private static String getColumnString(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        List<String> columns = new ArrayList<>();
        Row columnRow = sheet.getRow(0);
        int columnTotalNum = columnRow.getLastCellNum();
        for (int j = 0; j < columnTotalNum; j++) {
            Object obj = null;
            Cell cell = columnRow.getCell(j);
            if (cell == null) {
                obj = "";
            } else {
                obj = getValue(cell);
            }
            columns.add(obj.toString());
        }
        return StringUtils.join(columns, ",");
    }
}