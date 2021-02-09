/**  
 * @Title: Rule.java
 * @Package com.shine.tech.epdce.csveditor.multiexplorer
 * @Description 
 * @author zq
 * @date 2021年1月20日 下午1:52:19
 * @Copyright 
 */

package com.zq.utils.multiexplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description
 * @author zq
 * @date 2021年1月20日 下午1:52:19
 * @see
 * @since 2021年1月20日 下午1:52:19
 */
public class Rule {

    private String id;
    private String keyRule;
    private String excludeRule;
    private String advice;
    private String fixRule;
    private String fileType;
    private int isUse;
    // file,article
    private String ruleScope;

    private List<Pattern> includePatterns = new ArrayList<>();
    private List<Pattern> excludePatterns = new ArrayList<>();

    public Rule() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyRule() {
        return keyRule;
    }

    public void setKeyRule(String keyRule) {
        this.keyRule = keyRule;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getFixRule() {
        return fixRule;
    }

    public void setFixRule(String fixRule) {
        this.fixRule = fixRule;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getRuleScope() {
        return ruleScope;
    }

    public void setRuleScope(String ruleScope) {
        this.ruleScope = ruleScope;
    }

    public String getExcludeRule() {
        return excludeRule;
    }

    public void setExcludeRule(String excludeRule) {
        this.excludeRule = excludeRule;
    }

    public List<Pattern> getIncludePatterns() {
        return includePatterns;
    }

    public void setIncludePatterns(List<Pattern> includePatterns) {
        this.includePatterns = includePatterns;
    }

    public List<Pattern> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<Pattern> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public int getIsUse() {
        return isUse;
    }

    public void setIsUse(int isUse) {
        this.isUse = isUse;
    }

    public synchronized void initPatterns() {
        if (this.getIncludePatterns().isEmpty() && StringUtils.isNotBlank(keyRule)) {
            for (String s : keyRule.split("&&&")) {
                this.getIncludePatterns().add(Pattern.compile(s));
            }
        }
        if (this.getExcludePatterns().isEmpty() && StringUtils.isNotBlank(excludeRule)) {
            for (String s : excludeRule.split("\\|\\|\\|")) {
                this.getExcludePatterns().add(Pattern.compile(s));
            }
        }
    }

}
