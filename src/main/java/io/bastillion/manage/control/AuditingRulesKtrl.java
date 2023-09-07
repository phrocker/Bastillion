/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.AuditingRulesDB;
import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.db.SystemDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.Rule;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.RefreshAuthKeyUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for user settings
 */
public class AuditingRulesKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(AuditingRulesKtrl.class);

    public static final String REQUIRED = "Required";
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "rule")
    Rule rule = new Rule();

    @Model(name = "ruleSelectId")
    List<Long> ruleSelectId = new ArrayList<>();


    public AuditingRulesKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/admin/ruleSettings", method = MethodType.GET)
    public String viewAdminSystems() throws ServletException, GeneralSecurityException, SQLException {

            Long userId = AuthUtil.getUserId(getRequest());
            sortedSet = AuditingRulesDB.getRules(sortedSet);

        return "/admin/view_rules.html";
    }


    @Kontrol(path = "/manage/viewRules", method = MethodType.GET)
    public String viewMangeRules() throws ServletException {
        try {
            sortedSet = AuditingRulesDB.getRules(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }
        return "/manage/view_rules.html";
    }

    @Kontrol(path = "/manage/saveRule", method = MethodType.POST)
    public String saveSystem() throws ServletException {
        String retVal = "redirect:/manage/viewRules.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();

        try {
            if (rule.getId() != null) {
                AuditingRulesDB.updateRule(rule);
            } else {
                rule.setId(AuditingRulesDB.insertRule(rule));
            }
            sortedSet = AuditingRulesDB.getRules(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }


        retVal = "/manage/view_rules.html";

        return retVal;
    }

    @Kontrol(path = "/manage/deleteRule", method = MethodType.GET)
    public String deleteRule() throws ServletException {
        String retVal = "redirect:/manage/viewRules.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();

        try {
            if (rule.getId() != null) {
                AuditingRulesDB.deleteRule(rule);
            }
            sortedSet = AuditingRulesDB.getRules(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }


        retVal = "/manage/view_rules.html";

        return retVal;
    }

    @Kontrol(path = "/manage/viewSystemRules", method = MethodType.GET)
    public String viewProfileUsers() throws ServletException {
        if (rule != null && rule.getId() != null) {
            try {
                rule = AuditingRulesDB.getRule(rule.getId());
                sortedSet = SystemDB.getSystemsForRule(sortedSet, rule.getId());
            } catch (SQLException | GeneralSecurityException ex) {
                log.error(ex.toString(), ex);
                throw new ServletException(ex.toString(), ex);
            }

        }
        return "/manage/view_system_rules.html";
    }

    @Kontrol(path = "/manage/assignRulesToSystem", method = MethodType.POST)
    public String assignRulesToSystem() throws ServletException {

        if (ruleSelectId != null) {
            try {
                AuditingRulesDB.setRuleSystems(rule.getId(), ruleSelectId);
            } catch (SQLException | GeneralSecurityException ex) {
                log.error(ex.toString(), ex);
                throw new ServletException(ex.toString(), ex);
            }
        }
        return "redirect:/manage/viewSystemRules.ktrl?rule.id=" + rule.getId();
    }
}
