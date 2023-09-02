/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import io.bastillion.manage.auditing.rules.ForbiddenCommandsRule;


/**
 * Value object that contains configuration information around auditing rules
 */
public class Rule {
    Long id;
    String displayNm;
    String ruleClass = ForbiddenCommandsRule.class.getCanonicalName();
    String ruleConfig;

    String errorMsg;

    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayNm() {
        return displayNm;
    }

    public void setDisplayNm(String displayNm) {
        this.displayNm = displayNm;
    }

    public String getRuleClass() {
        return ruleClass;
    }

    public void setRuleClass(String ruleClass) {
        this.ruleClass = ruleClass;
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }


}
