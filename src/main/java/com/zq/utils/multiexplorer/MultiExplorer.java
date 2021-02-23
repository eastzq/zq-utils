/**  
 * @Title: multiexplorer.java
 * @Package com.shine.tech.epdce.csveditor
 * @Description 
 * @author zq
 * @date 2021年1月20日 下午1:47:04
 * @Copyright 
 */

package com.zq.utils.multiexplorer;

import com.alibaba.fastjson.JSONObject;
import com.zq.utils.multiexplorer.utils.CsvUtil;

import java.io.File;
import java.util.*;

/**
 * @Description qc36266
 * @author zq
 * @date 2021年1月20日 下午1:47:04
 * @see
 * @since 2021年1月20日 下午1:47:04
 */
public class MultiExplorer {

    public MultiExplorer(List<Map<String, Object>> todoList, List<Rule> rules) {
        this.rules = rules;
        this.todoList = todoList;

    }

    private String svnBasePath = "C:\\Users\\zq\\Desktop\\多浏览器\\ECTMS_FILE\\jsp";

    List<Map<String, Object>> todoList;
    List<Rule> rules;

    List<String> paths = new ArrayList<>();

    List<Map<String, Object>> analyseList = new ArrayList<>();

    public void process() {
        File file = new File(svnBasePath);
        this.listFiles(file, paths);
        for (String path : paths) {
            Map map = new HashMap();
            map.put("filePath", path);
            this.analyseList.add(map);
        }

//        for (Map<String, Object> map : todoList) {
//            analyse(map);
//        }

        this.analyseList.stream().parallel().forEach(map -> {
            analyse(map);
        });

    }

    public void analyse(Map<String, Object> map) {
        try {
            JSONObject jo = new JSONObject(map);
            String filePath = jo.getString("filePath");
//          filePath = svnBasePath + filePath;
            File file = new File(filePath);
            RuleCheck ruleCheck = new RuleCheck(rules, file, "GBK");
            ruleCheck.init();
            map.put("checkResult", ruleCheck.check());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        List<Rule> rules = CsvUtil.loadConfig("C:\\Users\\zq\\Desktop\\多浏览器\\multiExplore.csv", Rule.class);
//        List<Rule> rules = CsvUtil.loadConfig(Rule.class.getClass().getResourceAsStream("/multiExplore.csv"), Rule.class);
//        File file = new File("C:\\Users\\zq\\Desktop\\多浏览器\\多浏览器改造工作进展.xlsx");
        List<Map<String, Object>> todoList = null;
        Map<String, String> m = new LinkedHashMap<String, String>();
        m.put("文件路径", "filePath");
        m.put("涉及页面", "pageDesc");
        m.put("自测情况", "isSelfTest");
        m.put("负责人", "responsible");
        m.put("完成日期", "completionDate");
        m.put("检查结果", "checkResult");
//        try (InputStream fis = new FileInputStream(file)) {
//            todoList = ExcelReader.parseExcel(fis, file.getName(), m, 3);
//        }
        MultiExplorer ex = new MultiExplorer(todoList, rules);
        ex.process();
        List<String> columns = new ArrayList<String>();
        columns.addAll(m.values());
//        ExcelWriter.genExcel(null, columns, ex.getAnalyseList(), "C:\\Users\\zq\\Desktop\\多浏览器\\test.xlsx");
        MdWriter.genMarkDown(ex.getAnalyseList(), "C:\\Users\\zq\\Desktop\\多浏览器\\test.md");
        System.out.println("处理成功");
    }

    private List<String> listFiles(File file, List<String> resultFileName) {
        File[] files = file.listFiles();
        if (files == null)
            return resultFileName;// 判断目录下是不是空的
        for (File f : files) {
            if (f.isDirectory()) {// 判断是否文件夹
//                resultFileName.add(f.getPath());
                listFiles(f, resultFileName);// 调用自身,查找子目录
            } else
                resultFileName.add(f.getPath());
        }
        return resultFileName;
    }

    public List<Map<String, Object>> getTodoList() {
        return todoList;
    }

    public void setTodoList(List<Map<String, Object>> todoList) {
        this.todoList = todoList;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Map<String, Object>> getAnalyseList() {
        return analyseList;
    }

    public void setAnalyseList(List<Map<String, Object>> analyseList) {
        this.analyseList = analyseList;
    }

}
