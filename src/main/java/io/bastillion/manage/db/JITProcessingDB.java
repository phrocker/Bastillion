/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.jit.JITReason;
import io.bastillion.manage.model.jit.JITRequest;
import io.bastillion.manage.model.jit.JITRequestLink;
import io.bastillion.manage.model.jit.JITStatus;
import io.bastillion.manage.model.jit.JITTracker;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.JITUtils;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO used to manage systems
 */
public class JITProcessingDB {





    private JITProcessingDB() {
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
    public static JITReason  addJITReason(JITReason reason) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("insert into jit_reasons (command_need, reason_identifier, url) values (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setString(1, reason.getCommandNeed());
        stmt.setString(2, reason.getRequestLink().getIdentifier());
        stmt.setString(3, reason.getRequestLink().getUri());
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        Long id = Long.valueOf(0);
        if (rs.next()) {
             id = rs.getLong(1);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return JITReason.builder().id(id)
                .requestLink(reason.getRequestLink())
                .commandNeed(reason.getCommandNeed()).build();
    }

    public static JITRequest addJITRequest(JITRequest request) throws SQLException, GeneralSecurityException {

        JITReason reason = request.getReason();
        if (request.getReason().getId() == -1){
            reason = addJITReason(reason);
        }
        Connection con = DBUtils.getConn();
        final String commandHash = JITUtils.getCommandHash(request.getCommand());
        System.out.println("hash is " + commandHash);
        PreparedStatement stmt = con.prepareStatement("insert into jit_requests (user_id, system_id, command, jit_reason_id, command_hash) values (?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setLong(1, request.getUserId());
        stmt.setLong(2, request.getSystemId());
        stmt.setString(3, request.getCommand());
        stmt.setLong(4, reason.getId());
        stmt.setString(5, commandHash);
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        Long id = Long.valueOf(0);
        if (rs.next()) {
            id = rs.getLong(1);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return JITRequest.builder().id(id)
                .reason(reason).systemId(request.getSystemId())
                .userId(request.getUserId())
                .build();
    }


    public static boolean hasJITRequest(String command, Long userId, Long systemId) throws SQLException, GeneralSecurityException {

        boolean hasSubmittedOpen = false;
        final String commandHash = JITUtils.getCommandHash(command);
        String sql = "select j.id,j.command,j.user_id,j.system_id,j.jit_reason_id,r.command_need,r.reason_identifier,r.url,j.last_updated from jit_requests j left join jit_reasons r on r.id = j.jit_reason_id where j.command_hash=? and j.user_id=? and j.system_id=? and NOT EXISTS (SELECT * from jit_approvals a where a.jit_request_id=j.id) order by j.last_updated";
        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1,commandHash);
        stmt.setLong(2, userId);
        stmt.setLong(3, systemId);

        ResultSet rs = stmt.executeQuery();

        while(rs.next()){
            System.out.println("GOt " + rs.getLong(0) + " " + rs.getTimestamp(9));
            hasSubmittedOpen = true;
        }

        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return hasSubmittedOpen;
    }

    public static List<JITRequest> getJITRequests(String command, Long userId, Long systemId) throws SQLException, GeneralSecurityException {

        final String commandHash = JITUtils.getCommandHash(command);
        System.out.println("hash is " + commandHash);
        String sql = "select j.id,j.command,j.user_id,j.system_id,j.jit_reason_id,r.command_need,r.reason_identifier,r.url,j.last_updated from jit_requests j left join jit_reasons r on r.id = j.jit_reason_id where j.command_hash=? and j.user_id=? and j.system_id=? order by j.last_updated desc";
        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1,commandHash);
        stmt.setLong(2, userId);
        stmt.setLong(3, systemId);

        ResultSet rs = stmt.executeQuery();
        List<JITRequest> requests = new ArrayList<>();
        while (rs.next()) {

            JITRequestLink jrl = JITRequestLink.builder().identifier(rs.getString(7)).uri(rs.getString(8)).build();
            JITReason reason = JITReason.builder().id(rs.getLong(5)).commandNeed(rs.getString(6)).requestLink(jrl).build();

            JITRequest request = JITRequest.builder().id( rs.getLong(1))
                    .reason(reason).systemId(rs.getLong(4))
                    .userId(rs.getLong(3)).command(rs.getString(2))
                    .build();


            Timestamp ts = rs.getTimestamp(9);
            System.out.println("Last updated for "  + rs.getLong(1) + " is " + ts.getTime());

            requests.add( request );
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return requests;
    }


    public static List<JITTracker> getOpenJITRequests(SortedSet sortedSet) throws SQLException, GeneralSecurityException {

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }

        String sql = "select j.id,j.command,j.user_id,j.system_id,j.jit_reason_id,r.command_need,r.reason_identifier,r.url, j.last_updated from jit_requests j left join jit_reasons r on r.id = j.jit_reason_id where NOT EXISTS (SELECT * from jit_approvals a where a.jit_request_id=j.id)\n" + orderBy;
        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);

        ResultSet rs = stmt.executeQuery();
        List<JITTracker> requests = new ArrayList<>();
        while (rs.next()) {
            JITRequestLink jrl = JITRequestLink.builder().identifier(rs.getString(7)).uri(rs.getString(8)).build();
            JITReason reason = JITReason.builder().id(rs.getLong(5)).commandNeed(rs.getString(6)).requestLink(jrl).build();

            JITTracker request = JITTracker.builder().id( rs.getLong(1))
                    .reason(reason).systemId(rs.getLong(4))
                    .userId(rs.getLong(3)).command(rs.getString(2))
                    .user(UserDB.getUser(rs.getLong(3)))
                    .hostSystem(SystemDB.getSystem(rs.getLong(4)))
                    .build();
            Timestamp ts = rs.getTimestamp(9);
            System.out.println("2 Last updated for "  + rs.getLong(1) + " is " + ts.getTime());
            requests.add( request );
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return requests;
    }

    public static List<JITTracker> getDeniedJITs(SortedSet sortedSet) throws SQLException, GeneralSecurityException {

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        else{
            orderBy = "order by last_updated desc";
        }

        String sql = "select j.id,j.command,j.user_id,j.system_id,j.jit_reason_id,r.command_need,r.reason_identifier,r.url, j.last_updated from jit_requests j left join jit_reasons r on r.id = j.jit_reason_id left join jit_approvals a on a.jit_request_id=j.id where a.approved=false " + orderBy;
        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);

        ResultSet rs = stmt.executeQuery();
        List<JITTracker> requests = new ArrayList<>();
        while (rs.next()) {
            JITRequestLink jrl = JITRequestLink.builder().identifier(rs.getString(7)).uri(rs.getString(8)).build();
            JITReason reason = JITReason.builder().id(rs.getLong(5)).commandNeed(rs.getString(6)).requestLink(jrl).build();

            JITTracker request = JITTracker.builder().id( rs.getLong(1))
                    .reason(reason).systemId(rs.getLong(4))
                    .userId(rs.getLong(3)).command(rs.getString(2))
                    .user(UserDB.getUser(rs.getLong(3)))
                    .hostSystem(SystemDB.getSystem(rs.getLong(4)))
                    .build();
            Timestamp ts = rs.getTimestamp(9);
            System.out.println("3 Last updated for "  + rs.getLong(1) + " is " + ts.getTime());
            requests.add( request );
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return requests;
    }


    public static List<JITTracker> getApprovedJITs(SortedSet sortedSet) throws SQLException, GeneralSecurityException {

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        else{
            orderBy = "order by last_updated desc";
        }

        String sql = "select j.id,j.command,j.user_id,j.system_id,j.jit_reason_id,r.command_need,r.reason_identifier,r.url, j.last_updated from jit_requests j left join jit_reasons r on r.id = j.jit_reason_id left join jit_approvals a on a.jit_request_id=j.id where a.approved=true " + orderBy;
        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);

        ResultSet rs = stmt.executeQuery();
        List<JITTracker> requests = new ArrayList<>();
        while (rs.next()) {
            JITRequestLink jrl = JITRequestLink.builder().identifier(rs.getString(7)).uri(rs.getString(8)).build();
            JITReason reason = JITReason.builder().id(rs.getLong(5)).commandNeed(rs.getString(6)).requestLink(jrl).build();

            JITTracker request = JITTracker.builder().id( rs.getLong(1))
                    .reason(reason).systemId(rs.getLong(4))
                    .userId(rs.getLong(3)).command(rs.getString(2))
                    .user(UserDB.getUser(rs.getLong(3)))
                    .hostSystem(SystemDB.getSystem(rs.getLong(4)))
                    .build();
            Timestamp ts = rs.getTimestamp(9);
            System.out.println("3 Last updated for "  + rs.getLong(1) + " is " + ts.getTime());
            requests.add( request );
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return requests;
    }


    public static JITStatus getJITStatus(JITRequest request) throws SQLException, GeneralSecurityException {

        String orderBy = "";
        String sql = "select id,approver_id,approved,last_updated from jit_approvals where jit_request_id=?";

        //get user for auth token
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, request.getId());

        ResultSet rs = stmt.executeQuery();
        JITStatus status = new JITStatus();
        while (rs.next()) {
            JITStatus jitStatus = new JITStatus();
            jitStatus.setId(rs.getLong("id"));
            jitStatus.setApproved(rs.getBoolean("approved"));
            jitStatus.setRequest(request);
            jitStatus.setApproverId(rs.getLong("approver_id"));
            status = jitStatus;
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return status;
    }

    public static Long setJITStatus(Long jitId, Long userId, boolean approval) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("delete from jit_approvals where jit_request_id=?");
        stmt.setLong(1, jitId);
        stmt.execute();
        DBUtils.closeStmt(stmt);
        stmt = con.prepareStatement("insert into jit_approvals (approver_id, approved, jit_request_id) values (?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setLong(1, userId);
        stmt.setBoolean(2, approval);
        stmt.setLong(3, jitId);
        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
        Long id = Long.valueOf(0);
        if (rs.next()) {
            id = rs.getLong(1);
            System.out.println("set " + jitId + " id is now " + id );
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return id;
    }

    //statement.executeUpdate("create table if not exists jit_approvals (id BIGINT PRIMARY KEY AUTO_INCREMENT, approver_id BIGINT, jit_request_id BIGINT, approved boolean not null default false, last_updated timestamp default CURRENT_TIMESTAMP,  foreign key (approver_id) references users(id), foreign key (jit_reason_id) references jit_reasons(id), foreign key (system_id) references system(id))");


}
