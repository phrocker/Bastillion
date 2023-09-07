/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */

package io.bastillion.common.util;

import io.bastillion.manage.util.EncryptionUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Utility to obtain the authentication token from the http session and the user id from the auth token
 */
public class AuthUtil {

    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String AUTH_TOKEN = "authToken";
    public static final String TIMEOUT = "timeout";

    public static final String USER_TYPE = "userType";
    public static final String AUTH_TYPE = "authType";

    private AuthUtil() {
    }

    private static String getCookie(EndpointConfig request, String cookieName){
        if (null != request.getUserProperties()) {
            Object obj = request.getUserProperties().get(cookieName);
            if (null != obj){
                return obj.toString();
            }
        }
        return null;
    }

    private static String getCookie(HttpServletRequest request, String cookieName){
        if (null != request.getCookies()) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) return cookie.getValue();
            }
        }
        return null;
    }

    private static void expireCookies(HttpServletRequest request, HttpServletResponse response, List<String> cookies){
        if (null != request.getCookies()) {
            for (Cookie cookie : request.getCookies()) {
                if (cookies.contains( cookie.getName())){
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    cookie.setSecure(true);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

    }


    /**
     * query session for OTP shared secret
     *
     * @param request http request
     * @return shared secret
     */
    public static String getOTPSecret(HttpServletRequest request) throws GeneralSecurityException {
        return EncryptionUtil.decrypt(getCookie(request,AUTH_TYPE));
    }

    /**
     * set authentication type
     *
     * @param request  http session
     * @param authType authentication type
     */
    public static void setAuthType(HttpServletRequest request,HttpServletResponse response, String authType) {
        if (authType != null) {
            Cookie cookie = new Cookie("authType",authType);
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    /**
     * query authentication type
     *
     * @param request http request
     * @return authentication type
     */
    public static String getAuthType(HttpServletRequest request) {
        return getCookie(request,AUTH_TYPE);
    }

    /**
     * set user type
     *
     * @param request  http session
     * @param userType user type
     */
    public static void setUserType(HttpServletRequest request, HttpServletResponse response, String userType) {
        if (userType != null) {
            Cookie cookie = new Cookie("userType", userType);
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            request.getSession().setAttribute("userType",userType);
        }
    }

    /**
     * query user type
     *
     * @param request http request
     * @return user type
     */
    public static String getUserType(HttpServletRequest request) {
        return getCookie(request,"userType");
    }

    /**
     * set session id
     *
     * @param request   http session
     * @param sessionId session id
     */
    public static void setSessionId(HttpServletRequest request,HttpServletResponse response, Long sessionId) throws GeneralSecurityException {
        if (sessionId != null) {
            Cookie cookie = new Cookie(SESSION_ID, EncryptionUtil.encrypt(sessionId.toString()));
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    /**
     * query session id
     *
     * @param request http request
     * @return session id
     */
    public static Long getSessionId(HttpServletRequest request) throws GeneralSecurityException {
        Long sessionId = null;
        String sessionIdStr = EncryptionUtil.decrypt( getCookie(request,SESSION_ID));
        if (sessionIdStr != null && !sessionIdStr.trim().equals("")) {
            sessionId = Long.parseLong(sessionIdStr);
        }
        return sessionId;
    }

    public static Long getSessionId(EndpointConfig config) throws GeneralSecurityException {
        Long sessionId = null;
        String sessionIdStr = EncryptionUtil.decrypt( getCookie(config,SESSION_ID));
        if (sessionIdStr != null && !sessionIdStr.trim().equals("")) {
            sessionId = Long.parseLong(sessionIdStr);
        }
        return sessionId;
    }

    /**
     * query session for user id
     *
     * @param request http request
     * @return user id
     */
    public static Long getUserId(HttpServletRequest request) throws GeneralSecurityException {
        Long userId = null;
        String userIdStr = EncryptionUtil.decrypt(getCookie(request,USER_ID));
        if (userIdStr != null && !userIdStr.trim().equals("")) {
            userId = Long.parseLong(userIdStr);
        }
        return userId;
    }

    public static Long getUserId(EndpointConfig request) throws GeneralSecurityException {
        Long userId = null;
        String userIdStr = EncryptionUtil.decrypt(getCookie(request,USER_ID));
        if (userIdStr != null && !userIdStr.trim().equals("")) {
            userId = Long.parseLong(userIdStr);
        }
        return userId;
    }

    /**
     * query session for the username
     *
     * @param request http request
     * @return username
     */
    public static String getUsername(HttpServletRequest request) {
        return getCookie(request,USERNAME);
    }

    /**
     * query session for authentication token
     *
     * @param request http request
     * @return authentication token
     */
    public static String getAuthToken(HttpServletRequest request) throws GeneralSecurityException {

        String authToken = getCookie(request,AUTH_TOKEN);
        authToken = EncryptionUtil.decrypt(authToken);
        return authToken;
    }

    /**
     * query session for timeout
     *
     * @param request http request
     * @return timeout string
     */
    public static String getTimeout(HttpServletRequest request) {
        return getCookie(request,TIMEOUT);
    }

    /**
     * set session OTP shared secret
     *
     * @param request http request
     * @param secret  shared secret
     */
    public static void setOTPSecret(HttpServletRequest request, HttpServletResponse response, String secret) throws GeneralSecurityException {
        if (secret != null && !secret.trim().equals("")) {
            Cookie cookie = new Cookie("otp_secret",EncryptionUtil.encrypt(secret));
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }


    /**
     * set session user id
     *
     * @param request http request
     * @param userId  user id
     */
    public static void setUserId(HttpServletRequest request,HttpServletResponse response, Long userId) throws GeneralSecurityException {
        if (userId != null) {
            Cookie cookie = new Cookie(USER_ID, EncryptionUtil.encrypt(userId.toString()));
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }


    /**
     * set session username
     *
     * @param response  http session
     * @param username username
     */
    public static void setUsername(HttpServletResponse response, String username) {
        if (username != null) {
            Cookie cookie = new Cookie(USERNAME, username);
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);

        }
    }


    /**
     * set session authentication token
     *
     * @param request   http session
     * @param authToken authentication token
     */
    public static void setAuthToken(HttpServletResponse response, String authToken) throws GeneralSecurityException {
        if (authToken != null && !authToken.trim().equals("")) {
            Cookie cookie = new Cookie(AUTH_TOKEN, EncryptionUtil.encrypt(authToken));
            cookie.setSecure(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    /**
     * set session timeout
     *
     * @param response http request
     */
    public static void setTimeout(HttpServletResponse response) {
        //set session timeout
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
        Calendar timeout = Calendar.getInstance();
        timeout.add(Calendar.MINUTE, Integer.parseInt(AppConfig.getProperty("sessionTimeout", "15")));
        Cookie cookie = new Cookie(TIMEOUT, sdf.format(timeout.getTime()));
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    /**
     * delete all session information
     *
     * @param request
     */
    public static void deleteAllSession(HttpServletRequest request, HttpServletResponse response) {

        List<String> cookies = new ArrayList<>();
        cookies.add(TIMEOUT);
        cookies.add(AUTH_TOKEN);
        cookies.add(USER_ID);
        cookies.add(SESSION_ID);
        cookies.add(USERNAME);
        cookies.add(USER_TYPE);
        expireCookies(request,response,cookies);
        request.getSession().invalidate();
    }

    /**
     * return client ip from servlet request
     *
     * @param servletRequest http servlet request
     * @return client ip
     */
    public static String getClientIPAddress(HttpServletRequest servletRequest) {
        String clientIP = null;
        if (StringUtils.isNotEmpty(AppConfig.getProperty("clientIPHeader"))) {
            clientIP = servletRequest.getHeader(AppConfig.getProperty("clientIPHeader"));
        }
        if (StringUtils.isEmpty(clientIP)) {
            clientIP = servletRequest.getRemoteAddr();
        }
        return clientIP;
    }


}
