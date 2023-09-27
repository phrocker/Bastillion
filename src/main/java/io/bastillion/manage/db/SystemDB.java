/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.common.util.ConcurrentLRUCache;
import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.HostSystemRule;
import io.bastillion.manage.model.Profile;
import io.bastillion.manage.model.ProfileRule;
import io.bastillion.manage.model.Rule;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.DBUtils;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * DAO used to manage systems
 */
public class SystemDB {

    public static final String AUTHORIZED_KEYS = "authorized_keys";
    public static final String FILTER_BY_PROFILE_ID = "profile_id";

    public static final String SYSTEM_ID = "display_nm";

    public static final String DISPLAY_NM = "display_nm";
    public static final String SORT_BY_NAME = DISPLAY_NM;
    public static final String SORT_BY_USER = "username";
    public static final String SORT_BY_HOST = "host";
    public static final String STATUS_CD = "status_cd";
    public static final String PROFILE_ID = "profile_id";
    public static final String SORT_BY_STATUS = STATUS_CD;


    private static final ConcurrentLRUCache<Long, HostSystem> systemMap = new ConcurrentLRUCache(100);

    private SystemDB() {
    }


    /**
     * method to do order by based on the sorted set object for systems for user
     *
     * @param sortedSet sorted set object
     * @param userId    user id
     * @return sortedSet with list of host systems
     */
    public static SortedSet getUserSystemSet(SortedSet sortedSet, Long userId) throws SQLException, GeneralSecurityException {
        List<HostSystem> hostSystemList = new ArrayList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from system where id in (select distinct system_id from  system_map m, user_map um where m.profile_id=um.profile_id and um.user_id=? ";
        //if profile id exists add to statement
        sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? " and um.profile_id=? " : "";
        sql += ") " + orderBy;

        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, userId);
        //filter by profile id if exists
        if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))) {
            stmt.setLong(2, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
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


    /**
     * method to do order by based on the sorted set object for systems
     *
     * @param sortedSet sorted set object
     * @return sortedSet with list of host systems
     * @profileId check if system is apart of given profile
     */
    public static SortedSet getSystemSet(SortedSet sortedSet, Long profileId) throws SQLException, GeneralSecurityException {
        List<HostSystem> hostSystemList = new ArrayList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select s.*, m.profile_id from  system s left join system_map  m on m.system_id = s.id and m.profile_id = ? " + orderBy;

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, profileId);
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
            hostSystem.setChecked(profileId != null && profileId.equals(rs.getLong(PROFILE_ID)));
            hostSystemList.add(hostSystem);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        sortedSet.setItemList(hostSystemList);
        return sortedSet;
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


    /**
     * returns system by id
     *
     * @param id system id
     * @return system
     */
    public static HostSystem getSystem(Long id) throws SQLException, GeneralSecurityException {

        HostSystem hostSystem = systemMap.get(id);
        if (null == hostSystem) {
            Connection con = DBUtils.getConn();
            hostSystem = getSystem(con, id);
            DBUtils.closeConn(con);
            systemMap.put(id,hostSystem);
        }

        return hostSystem;
    }


    /**
     * returns system by id
     *
     * @param con DB connection
     * @param id  system id
     * @return system
     */
    public static HostSystem getSystem(Connection con, Long id) throws SQLException {

        HostSystem hostSystem = null;

        PreparedStatement stmt = con.prepareStatement("select * from  system where id=?");
        stmt.setLong(1, id);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            hostSystem = new HostSystem();
            hostSystem.setId(rs.getLong("id"));
            hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
            hostSystem.setUser(rs.getString("username"));
            hostSystem.setHost(rs.getString("host"));
            hostSystem.setPort(rs.getInt("port"));
            hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
            hostSystem.setStatusCd(rs.getString(STATUS_CD));
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return hostSystem;
    }


    /**
     * inserts host system into DB
     *
     * @param hostSystem host system object
     * @return user id
     */
    public static Long insertSystem(HostSystem hostSystem) throws SQLException, GeneralSecurityException {

        Long userId = null;
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, username, host, port, authorized_keys, status_cd) values (?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, hostSystem.getDisplayNm());
        stmt.setString(2, hostSystem.getUser());
        stmt.setString(3, hostSystem.getHost());
        stmt.setInt(4, hostSystem.getPort());
        stmt.setString(5, hostSystem.getAuthorizedKeys());
        stmt.setString(6, hostSystem.getStatusCd());
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
     * updates host system record
     *
     * @param hostSystem host system object
     */
    public static void updateSystem(HostSystem hostSystem) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, username=?, host=?, port=?, authorized_keys=?, status_cd=?  where id=?");
        stmt.setString(1, hostSystem.getDisplayNm());
        stmt.setString(2, hostSystem.getUser());
        stmt.setString(3, hostSystem.getHost());
        stmt.setInt(4, hostSystem.getPort());
        stmt.setString(5, hostSystem.getAuthorizedKeys());
        stmt.setString(6, hostSystem.getStatusCd());
        stmt.setLong(7, hostSystem.getId());
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * deletes host system
     *
     * @param hostSystemId host system id
     */
    public static void deleteSystem(Long hostSystemId) throws SQLException, GeneralSecurityException {


        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("delete from system where id=?");
        stmt.setLong(1, hostSystemId);
        stmt.execute();
        DBUtils.closeStmt(stmt);

        DBUtils.closeConn(con);
    }

    /**
     * returns the host systems
     *
     * @param systemIdList list of host system ids
     * @return host system with array of public keys
     */
    public static List<HostSystem> getSystems(List<Long> systemIdList) throws SQLException, GeneralSecurityException {

        List<HostSystem> hostSystemListReturn = new ArrayList<>();

        Connection con = DBUtils.getConn();
        for (Long systemId : systemIdList) {
            HostSystem hostSystem = getSystem(con, systemId);
            hostSystemListReturn.add(hostSystem);
        }

        DBUtils.closeConn(con);

        return hostSystemListReturn;

    }


    /**
     * returns all systems
     *
     * @return system list
     */
    public static List<HostSystem> getAllSystems() throws SQLException, GeneralSecurityException {

        List<HostSystem> hostSystemList = new ArrayList<>();


        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select * from system");
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

        return hostSystemList;
    }


    /**
     * returns all system ids
     *
     * @param con DB connection
     * @return system
     */
    public static List<Long> getAllSystemIds(Connection con) throws SQLException {

        List<Long> systemIdList = new ArrayList<>();

        PreparedStatement stmt = con.prepareStatement("select * from system");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            systemIdList.add(rs.getLong("id"));
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);


        return systemIdList;

    }

    /**
     * returns all system ids for user
     *
     * @param con    DB connection
     * @param userId user id
     * @return system
     */
    public static List<Long> getAllSystemIdsForUser(Connection con, Long userId) throws SQLException {

        List<Long> systemIdList = new ArrayList<>();


        PreparedStatement stmt = con.prepareStatement("select distinct system_id from system_map m, user_map um, system s where m.profile_id=um.profile_id and um.user_id=?");
        stmt.setLong(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            systemIdList.add(rs.getLong("system_id"));
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);


        return systemIdList;

    }

    /**
     * returns all system ids for user
     *
     * @param userId user id
     * @return system
     */
    public static List<Long> getAllSystemIdsForUser(Long userId) throws SQLException, GeneralSecurityException {
        List<Long> systemIdList = new ArrayList<>();
        Connection con = DBUtils.getConn();
        systemIdList = getAllSystemIdsForUser(con, userId);

        DBUtils.closeConn(con);
        return systemIdList;
    }

    /**
     * returns all system ids
     *
     * @return system
     */
    public static List<Long> getAllSystemIds() throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        List<Long> systemIdList = getAllSystemIds(con);
        DBUtils.closeConn(con);

        return systemIdList;
    }

    /**
     * method to check system permissions for user
     *
     * @param con                DB connection
     * @param systemSelectIdList list of system ids to check
     * @param userId             user id
     * @return only system ids that user has perms for
     */
    public static List<Long> checkSystemPerms(Connection con, List<Long> systemSelectIdList, Long userId) throws SQLException {

        List<Long> systemIdList = new ArrayList<>();
        List<Long> userSystemIdList = getAllSystemIdsForUser(con, userId);

        for (Long systemId : userSystemIdList) {
            if (systemSelectIdList.contains(systemId)) {
                systemIdList.add(systemId);
            }
        }

        return systemIdList;
    }

    public static SortedSet getSystemsForRule(SortedSet sortedSet, Long ruleId) throws SQLException, GeneralSecurityException {

        List<HostSystemRule> hostRules = new ArrayList<>();
        // get the list of all profiles
        List<HostSystem> profiles = getAllSystems();

        Rule rule = AuditingRulesDB.getRule(ruleId);

        Connection con = DBUtils.getConn();

        for(HostSystem hostSystem : profiles){

            /***
             * SELECT *
             * FROM Movies
             * LEFT JOIN Movie_Links
             * ON Movies.ID = Movie_Links.movie_id;
             */
            String sql = "select id from system_rules pr where pr.system_id = ? and pr.rule_id = ?";

            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, hostSystem.getId());
            stmt.setLong(2, ruleId);
            ResultSet rs = stmt.executeQuery();
            HostSystemRule pr = new HostSystemRule();
            while (rs.next()) {

                pr.setChecked(true);

                break;

            }
            List<Rule> ruleList = new ArrayList<>();
            ruleList.add(rule);
            pr.setRuleList(ruleList);
            pr.setHost(hostSystem.getHost());
            pr.setId(hostSystem.getId());
            pr.setDisplayNm(hostSystem.getDisplayNm());
            pr.setUser(hostSystem.getUser());
            hostRules.add(pr);
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        }



        DBUtils.closeConn(con);

        sortedSet.setItemList(hostRules);
        return sortedSet;
    }
}
