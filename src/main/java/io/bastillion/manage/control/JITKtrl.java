/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.common.util.BastillionOptions;
import io.bastillion.manage.db.AuditingRulesDB;
import io.bastillion.manage.db.JITProcessingDB;
import io.bastillion.manage.db.SystemDB;
import io.bastillion.manage.jit.JITRequest;
import io.bastillion.manage.jit.JITTracker;
import io.bastillion.manage.model.Rule;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.JITUtils;
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
public class JITKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(JITKtrl.class);

    public static final String REQUIRED = "Required";
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();

    @Model(name = "approvedSortedSet")
    SortedSet approvedSortedSet = new SortedSet();
    @Model(name = "jitRequest")
    JITRequest jitRequest = new JITRequest();

    @Model(name = "ruleSelectId")
    List<Long> ruleSelectId = new ArrayList<>();

    @Model(name = "systemOptions")
    BastillionOptions systemOptions;


    public JITKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
        systemOptions = AppConfig.getOptions();
    }

    @Kontrol(path = "/manage/denyJIT", method = MethodType.GET)
    public String denyJIT() throws ServletException, GeneralSecurityException, SQLException {
        Long userId = AuthUtil.getUserId(getRequest());
        JITUtils.denyJIT(jitRequest,userId);

        return viewAllJITs();
    }

    @Kontrol(path = "/manage/approveJIT", method = MethodType.GET)
    public String approveJIT() throws ServletException, GeneralSecurityException, SQLException {
        Long userId = AuthUtil.getUserId(getRequest());
            JITUtils.approveJIT(jitRequest,userId);

        return viewAllJITs();
    }

    @Kontrol(path = "/manage/viewOpenJIT", method = MethodType.GET)
    public String viewAllJITs() throws ServletException {
        try {
            List<JITTracker> jits = JITProcessingDB.getOpenJITRequests(sortedSet);
            sortedSet.setItemList(jits);

            List<JITTracker> approvedJITs = JITProcessingDB.getApprovedJITs(approvedSortedSet);
            approvedSortedSet.setItemList(approvedJITs);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }
        return "/manage/manage_jit.html";
    }

}
