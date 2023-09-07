/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.Rule;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.DBUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO used to manage systems
 */
public class AuditingRulesDB {

    public static final String AUTHORIZED_KEYS = "authorized_keys";
    public static final String FILTER_BY_PROFILE_ID = "profile_id";

    public static final String DISPLAY_NM = "ruleName";
    public static final String SORT_BY_NAME = DISPLAY_NM;
    public static final String SORT_BY_CLASS = "ruleClass";
    public static final String SORT_BY_CONFIG = "ruleConfig";
    public static final String STATUS_CD = "status_cd";
    public static final String PROFILE_ID = "profile_id";
    public static final String SORT_BY_STATUS = STATUS_CD;

    private AuditingRulesDB() {
    }


    /**
     * method to do order by based on the sorted set object for systems for user
     *
     * @param sortedSet sorted set object
     * @return sortedSet with list of host systems
     */
    public static SortedSet getRules(SortedSet sortedSet) throws SQLException, GeneralSecurityException {
        List<Rule> ruleList = new ArrayList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from rules";

        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        //filter by profile id if exists

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Rule dbRule = new Rule();
            dbRule.setId(rs.getLong("id"));
            dbRule.setDisplayNm(rs.getString("ruleName"));
            dbRule.setRuleClass(rs.getString("ruleClass"));
            dbRule.setRuleConfig(rs.getString("ruleConfig"));
            ruleList.add(dbRule);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        sortedSet.setItemList(ruleList);
        return sortedSet;
    }


    /**
     * method to do order by based on the sorted set object for systems for user
     *
     * @param ruleId rule ID
     * @return rule if ruleId is valid
     */
    public static Rule getRule(Long ruleId) throws SQLException, GeneralSecurityException {

        String orderBy = "";
        String sql = "select * from rules where id=?";

        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, ruleId);

        ResultSet rs = stmt.executeQuery();
        Rule rule = new Rule();
        while (rs.next()) {
            Rule dbRule = new Rule();
            dbRule.setId(rs.getLong("id"));
            dbRule.setDisplayNm(rs.getString("ruleName"));
            dbRule.setRuleClass(rs.getString("ruleClass"));
            dbRule.setRuleConfig(rs.getString("ruleConfig"));
            rule = dbRule;
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return rule;
    }

    /**
     * method to do order by based on the sorted set object for systems for user
     *
     * @param systemId system Id
     * @return rule if ruleId is valid
     */
    public static List<Rule> getSystemRules(Long systemId) throws SQLException, GeneralSecurityException {

        String orderBy = "";
        String sql = "select r.* from rules r join system_rules s on r.id=s.rule_id where s.system_id=?";
//String sql = "select s.*, m.profile_id from  system s left join system_map  m on m.system_id = s.id and m.profile_id = ? " + orderBy;
        //get user for auth token
        List<Rule> rules = new ArrayList<>();
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, systemId);

        ResultSet rs = stmt.executeQuery();
        Rule rule = new Rule();
        while (rs.next()) {
            Rule dbRule = new Rule();
            dbRule.setId(rs.getLong("id"));
            dbRule.setDisplayNm(rs.getString("ruleName"));
            dbRule.setRuleClass(rs.getString("ruleClass"));
            dbRule.setRuleConfig(rs.getString("ruleConfig"));
            rules.add(dbRule);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return rules;
    }




    /**
     * method to do order by based on the sorted set object for systems
     *
     * @param sortedSet sorted set object
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet) throws SQLException, GeneralSecurityException {
        List<HostSystem> hostSystemList = new ArrayList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  system s ";
        //if profile id exists add to statement
        sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? ",system_map m where s.id=m.system_id and m.profile_id=? " : "";
        sql += orderBy;

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))) {
            stmt.setLong(1, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
        }
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            HostSystem hostSystem = new HostSystem();
            hostSystem.setId(rs.getLong("id"));
            hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
            hostSystem.setUser(rs.getString("username"));
            hostSystem.setHost(rs.getString("host"));
            hostSystem.setPort(rs.getInt("port"));
            hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
            hostSystem.setStatusCd(rs.getString(STATUS_CD));
            hostSystemList.add(hostSystem);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        sortedSet.setItemList(hostSystemList);
        return sortedSet;
    }

    public static void updateRule(Rule rule) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("update rules set ruleName=?, ruleClass=?, ruleConfig=? where id=?");
        stmt.setString(1, rule.getDisplayNm());
        stmt.setString(2, rule.getRuleClass());
        stmt.setString(3, rule.getRuleConfig());
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    public static void deleteRule(Rule rule) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("delete from system_rules where rule_id=?");
        stmt.setLong(1, rule.getId());
        stmt.execute();
        DBUtils.closeStmt(stmt);

        stmt = con.prepareStatement("delete from rules where id=?");
        stmt.setLong(1, rule.getId());
        stmt.execute();
        DBUtils.closeStmt(stmt);

        DBUtils.closeConn(con);
    }

    /**
     * inserts rule into DB
     *
     * @param rule system rule
     * @return user id
     */
    public static Long insertRule(Rule rule) throws SQLException, GeneralSecurityException {

        Long userId = null;
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("insert into rules (ruleName, ruleClass, ruleConfig) values (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, rule.getDisplayNm());
        stmt.setString(2, rule.getRuleClass());
        stmt.setString(3, rule.getRuleConfig());
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            userId = rs.getLong(1);
        }
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return userId;
    }

    /**
     * Sets the list of profiles assigned to a rule.
     *
     * @param ruleId  rule id
     * @param systemIds list of system Ids
     */
    public static void setRuleSystems(Long ruleId, List<Long> systemIds) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("delete from system_rules where rule_id=?");
        stmt.setLong(1, ruleId);
        stmt.execute();
        DBUtils.closeStmt(stmt);

        for (Long systemId : systemIds) {
            stmt = con.prepareStatement("insert into system_rules (rule_id, system_id) values (?,?)");
            stmt.setLong(1, ruleId);
            stmt.setLong(2, systemId);
            stmt.execute();
            DBUtils.closeStmt(stmt);
        }

        DBUtils.closeConn(con);
    }

}
