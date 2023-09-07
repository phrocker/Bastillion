/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.socket;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Configure web sockets and set the http session
 */
public class GetHttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);


        Map<String, List<String>> headers = request.getHeaders();

        List<String> cookies = headers.get("Cookie");
        if (null != cookies) {
            for(String cookie : cookies){
                Scanner scan = new Scanner(cookie).useDelimiter(";");
                while(scan.hasNext()){
                    String cook = scan.next();

                    String[] rawCookieNameAndValue = cook.split("=");
                    if (rawCookieNameAndValue.length != 2) {
                        throw new RuntimeException("Invalid cookie: missing name and value.");
                    }
                    System.out.println(rawCookieNameAndValue[0]);
                    config.getUserProperties().put(rawCookieNameAndValue[0].trim(), rawCookieNameAndValue[1].trim());
                }
            }
        }

    }

}
