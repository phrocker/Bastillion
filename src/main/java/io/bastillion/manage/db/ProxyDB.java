/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.User;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.model.proxy.ProxyAssignment;
import io.bastillion.manage.model.proxy.ProxyHost;
import io.bastillion.manage.util.DBUtils;

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
public class ProxyDB {

    public static final String SORT_BY_NAME = "host";
    public static final String SORT_BY_PORT = "port";




    private ProxyDB() {
    }

/****
\
 *                 //log_tm timestamp default CURRENT_TIMESTAMP, approved boolean not null default false
 *                 statement.executeUpdate("create table if not exists jit_approvals (id BIGINT PRIMARY KEY AUTO_INCREMENT, approver_id BIGINT, jit_request_id BIGINT, approved boolean not null default false, last_updated timestamp default CURRENT_TIMESTAMP,  foreign key (approver_id) references users(id), foreign key (jit_reason_id) references jit_reasons(id), foreign key (system_id) references system(id))");
 */
    /**
     * method to do order by based on the sorted set object for systems for user
     *
     * @param reason sorted set object
     * @return sortedSet with list of host systems
     */

    /**
     * statement.executeUpdate("create table if not exists host_proxies (id BIGINT PRIMARY KEY AUTO_INCREMENT, system_id BIGINT, base_url varchar not null, last_updated timestamp default CURRENT_TIMESTAMP, foreign key (system_id) references system(id))");
     *
     *                 statement.executeUpdate("create table if not exists host_proxy_assignments (id BIGINT PRIMARY KEY AUTO_INCREMENT, proxy_id BIGINT, user_id BIGINT,  foreign key (user_id) references users(id), foreign key (proxy_id) references host_proxies(id))");
     * @param host
     * @return
     * @throws SQLException
     * @throws GeneralSecurityException
     */
    public static ProxyHost  addProxyHost(ProxyHost host, HostSystem hostSystem) throws SQLException, GeneralSecurityException {
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("insert into host_proxies (system_id, base_url, port) values (?,?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        System.out.println("Setting proxies for " + hostSystem.getId());
        stmt.setLong(1, hostSystem.getId());
        stmt.setString(2, host.getHost());
        stmt.setInt(3, host.getPort());
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        Long id = Long.valueOf(0);
        if (rs.next()) {
             id = rs.getLong(1);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return ProxyHost.builder().proxyId(id).host(host.getHost()).port(host.getPort()).hostSystem(host.getHostSystem()).build();
    }

    public static ProxyHost  updateProxy(ProxyHost host, ProxyHost proxy, HostSystem hostSystem) throws SQLException, GeneralSecurityException {
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("update host_proxies set base_url=?,  port = ? where system_id = ? and id = ?");
        System.out.println("Setting proxies for " + hostSystem.getId());
        stmt.setString(1, proxy.getHost());
        stmt.setLong(2, proxy.getPort());
        stmt.setLong(3, hostSystem.getId());
        stmt.setLong(4, host.getProxyId());
        stmt.execute();

        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return host;
    }

    public static void removeProxy(ProxyHost host, long system_id) throws SQLException, GeneralSecurityException {
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("delete from host_proxies where id=? and system_id=?");
        stmt.setLong(1, host.getProxyId());
        stmt.setLong(2, system_id);
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    public static ProxyHost  addProxyHost(ProxyHost host) throws SQLException, GeneralSecurityException {
        return addProxyHost(host, host.getHostSystem());
    }

    public static ProxyAssignment  associateUser(ProxyHost host, User user) throws SQLException, GeneralSecurityException {


        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("insert into host_proxy_assignments (proxy_id, user_id) values (?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setLong(1, host.getHostSystem().getId());
        stmt.setLong(2, user.getId());
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        Long id = Long.valueOf(0);
        if (rs.next()) {
            id = rs.getLong(1);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return ProxyAssignment.builder().id(id).proxyHost(host).user(user).build();
    }


    public static ProxyHost  getProxyHost(long proxy_id) throws SQLException, GeneralSecurityException {
        ProxyHost host = null;
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select h.id,h.system_id,h.base_url,h.port from host_proxies h where id=?");
        stmt.setLong(1, proxy_id);
        stmt.execute();

        ResultSet rs = stmt.getResultSet();
        if (rs.next()) {
            HostSystem system = SystemDB.getSystem(con,rs.getLong("system_id"));
            host = ProxyHost.builder().proxyId(rs.getLong("id")).host(rs.getString("base_url")).port(rs.getInt("port")).hostSystem(system).build();
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return host;
    }


    public static SortedSet  getProxies(long system_id, SortedSet sortedSet) throws SQLException, GeneralSecurityException {
        List<ProxyHost> proxyHosts = new ArrayList<>();
        ProxyHost host = null;
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select h.id,h.system_id,h.base_url,h.port from host_proxies h where h.system_id=?");
        stmt.setLong(1, system_id);
        stmt.execute();

        ResultSet rs = stmt.getResultSet();
        System.out.println("Getting proxies for " + system_id);
        while (rs.next()) {
            HostSystem system = SystemDB.getSystem(con,rs.getLong("system_id"));
            host = ProxyHost.builder().proxyId(rs.getLong("id")).host(rs.getString("base_url")).port(rs.getInt("port")).hostSystem(system).build();
            System.out.println("got " + host.getHost());
            proxyHosts.add(host);
        }
        sortedSet.setItemList(proxyHosts);
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return sortedSet;
    }

    public static List<ProxyHost>  getProxies(long system_id) throws SQLException, GeneralSecurityException {
        List<ProxyHost> proxyHosts = new ArrayList<>();
        ProxyHost host = null;
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select h.id,h.system_id,h.base_url,h.port from host_proxies h where h.system_id=?");
        stmt.setLong(1, system_id);
        stmt.execute();

        ResultSet rs = stmt.getResultSet();
        System.out.println("Getting proxies for " + system_id);
        while (rs.next()) {
            HostSystem system = SystemDB.getSystem(con,rs.getLong("system_id"));
            host = ProxyHost.builder().proxyId(rs.getLong("id")).host(rs.getString("base_url")).port(rs.getInt("port")).hostSystem(system).build();
            System.out.println("got " + host.getHost());
            proxyHosts.add(host);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return proxyHosts;
    }

    public static SortedSet  getProxies(List<Long> systemIds, SortedSet sortedSet) throws SQLException, GeneralSecurityException {

        List<ProxyHost> hosts = new ArrayList<>();
        System.out.println("getting proxies for " + systemIds.size());
        for(Long systemId: systemIds){
            System.out.println("getting proxies for " + systemId);
            hosts.addAll ( getProxies(systemId));
        }
        sortedSet.setItemList(hosts);

        return sortedSet;
    }
}
