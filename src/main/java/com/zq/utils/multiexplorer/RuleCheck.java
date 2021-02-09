/**  
 * @Title: RuleCheck.java
 * @Package com.shine.tech.epdce.csveditor.multiexplorer
 * @Description 
 * @author zq
 * @date 2021年1月21日 上午9:10:25
 * @Copyright 
 */

package com.zq.utils.multiexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @author zq
 * @date 2021年1月21日 上午9:10:25
 * @see
 * @since 2021年1月21日 上午9:10:25
 */
public class RuleCheck {

    private List<Rule> rules;

    private File checkFile;

    private List<String> lines;

    private String article;

    private String charset = "GBK";

    StringBuilder result = new StringBuilder();

    List<Rule> articleRules = new ArrayList<>();

    List<Rule> lineRules = new ArrayList<>();

    public RuleCheck(List<Rule> rules, File checkFile, String charset) {
        this.rules = rules;
        this.checkFile = checkFile;
    }

    /**
     * @Description
     * @return void
     * @author zq
     * @throws IOException
     * @date 2021年1月21日 上午9:12:51
     * @see
     */
    public void init() throws IOException {
        this.lines = FileUtils.readLines(checkFile, charset);
        // -*10
        this.article = StringUtils.join(lines, "----------");
        String fileName = this.checkFile.getName();

        for (Rule rule : rules) {
            List<String> arr = Arrays.asList(rule.getFileType().split(","));
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (arr.contains(suffix.toLowerCase()) && rule.getIsUse() == 1) {
                rule.initPatterns();
                if (rule.getRuleScope().equals("file")) {
                    articleRules.add(rule);
                } else {
                    lineRules.add(rule);
                }
            }
        }
    }

    /**
     * @Description
     * @return void
     * @author zq
     * @date 2021年1月21日 上午9:20:06
     * @see
     */
    public String check() {
        this.checkArticle();
        this.checkLines();
        return result.toString();
    }

    private void checkArticle() {
        for (Rule rule : this.articleRules) {
            boolean isExist = true;
            for (Pattern pattern : rule.getIncludePatterns()) {
                Matcher matcher = pattern.matcher(this.article);
                if (!matcher.find()) {
                    isExist = false;
                    break;
                }
            }

            boolean isFilter = false;
            for (Pattern pattern : rule.getExcludePatterns()) {
                Matcher matcher = pattern.matcher(this.article);
                if (matcher.find()) {
                    isFilter = true;
                }
            }

            if (isExist && !isFilter) {
                this.result.append("\n  file ").append(":").append(rule.getAdvice());
            }

        }

    }

    private void checkLines() {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (StringUtils.isBlank(line)) {
                continue;
            }
            for (Rule rule : lineRules) {
                boolean isExist = true;
                for (Pattern pattern : rule.getIncludePatterns()) {
                    Matcher matcher = pattern.matcher(line);
                    if (!matcher.find()) {
                        isExist = false;
                        break;
                    }
                }
                boolean isFilter = false;
                for (Pattern pattern : rule.getExcludePatterns()) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        isFilter = true;
                        break;
                    }
                }
                if (isExist && !isFilter) {
                    this.result.append("\n line ").append(i).append(":").append(rule.getAdvice());
                    this.result.append("\n```\n").append(lines.get(i - 1)).append("\n").append(line).append("\n")
                            .append(lines.get(i + 1)).append("\n").append("```");
                }
            }
        }
    }
}
