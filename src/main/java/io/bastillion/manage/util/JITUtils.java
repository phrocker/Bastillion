package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.JITProcessingDB;
import io.bastillion.manage.model.jit.JITReason;
import io.bastillion.manage.model.jit.JITRequest;
import io.bastillion.manage.model.jit.JITRequestLink;
import io.bastillion.manage.model.jit.JITStatus;
import io.bastillion.manage.model.SchSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public class JITUtils {


    static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static JITReason createReason(String commandNeed, String ticketId, String ticketURI){
        JITReason.JITReasonBuilder jitReasonBuilder = JITReason.builder().commandNeed(commandNeed);
        JITRequestLink.JITRequestLinkBuilder jriBuilder = JITRequestLink.builder();
        if ( AppConfig.getOptions().jitRequiresTicket){
            if (StringUtils.isNotEmpty(ticketId) && StringUtils.isNotEmpty(ticketURI)){
                jriBuilder=jriBuilder.identifier(ticketId).uri(ticketURI);
            }
        }
        jitReasonBuilder.requestLink(jriBuilder.build());
        return jitReasonBuilder.build();
    }

    public static JITRequest createRequest(String command, JITReason reason, SchSession session) throws SQLException, GeneralSecurityException {
        return createRequest(command,reason,session.getUserId(),session.getHostSystem().getId());
    }

    public static JITRequest createRequest(String command, JITReason reason, Long userId, Long systemId) throws SQLException, GeneralSecurityException {

        JITRequest request = JITRequest.builder().command(command).reason(reason).userId(userId).systemId(systemId).build();
        return request;
    }

    public static String getCommandHash(String command){
        String originalString = command.trim();
        return DigestUtils.sha256Hex(originalString);
    }

    public static boolean isApproved(String command, Long userId, Long systemId) throws SQLException, GeneralSecurityException {
        List<JITRequest> requests = JITProcessingDB.getJITRequests(command,userId,systemId);
        boolean approved = false;

        if (requests.size() > 0){
            JITRequest request = requests.get(0);
            JITStatus status = JITProcessingDB.getJITStatus(request);
            approved =  status.isApproved();
        }

        return approved;
    }

    public static void approveJIT(JITRequest request, Long userId) throws SQLException, GeneralSecurityException {
        JITProcessingDB.setJITStatus(request.getId(),userId,true);
    }

    public static void denyJIT(JITRequest request, Long userId) throws SQLException, GeneralSecurityException {
        JITProcessingDB.setJITStatus(request.getId(),userId,false);
    }
}
