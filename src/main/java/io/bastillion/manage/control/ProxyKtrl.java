/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.common.util.BastillionOptions;
import io.bastillion.manage.db.ProxyDB;
import io.bastillion.manage.db.SystemDB;
import io.bastillion.manage.model.ServletResponseType;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.model.UserSchSessions;
import io.bastillion.manage.model.proxy.ProxyHost;
import io.bastillion.manage.model.ServletResponse;
import io.bastillion.manage.util.SSHUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import loophole.mvc.filter.SecurityFilter;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Action for user settings
 */
public class ProxyKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(ProxyKtrl.class);

    public static final String REQUIRED = "Required";

    static Map<Long, UserSchSessions> userSchSessionMap = new ConcurrentHashMap<>();
    @Model(name = "httpResponse")
    String httpResponse;

    @Model(name = "systemSelectId")
    Long systemSelectId = Long.valueOf(-1);

    @Model(name = "systemOptions")
    BastillionOptions systemOptions;
    @Model(name = "password")
    String password;
    @Model(name = "passphrase")
    String passphrase;

    @Model(name = "proxy")
    ProxyHost proxy = new ProxyHost();

    @Model(name = "proxyId")
    Long proxyId = Long.valueOf(-1);

    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();

    @Model(name = "url")
    String url = "";


    public ProxyKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
        systemOptions = AppConfig.getOptions();
    }

    @Kontrol(path = "/admin/proxy/http", method = MethodType.GET)
    public String getHttpResponse() throws ServletException, GeneralSecurityException, SQLException {
        if (systemSelectId != null && proxyId != null) {
            Long userId = AuthUtil.getUserId(getRequest());
            Long sessionId = AuthUtil.getSessionId(getRequest());
            System.out.println("if url is " + url );
            getHttpResponse(userId,sessionId);
        } else {
            httpResponse = "Could not load ";
        }
        return "/admin/secure_browser.html";
    }

    @Kontrol(path = "/admin/proxy/get", method = MethodType.GET)
    public ServletResponse getRawResponse() throws ServletException, GeneralSecurityException, SQLException {
        ServletResponse response = null;
        if (systemSelectId != null && proxyId != null) {
            Long userId = AuthUtil.getUserId(getRequest());
            Long sessionId = AuthUtil.getSessionId(getRequest());
            System.out.println("if url is " + url );
            response = getHttpResponse(userId,sessionId,false);
        } else {
            response = ServletResponse.builder().build();
        }
        System.out.println("ahh");
        return response;
    }

    @Kontrol(path = "/manage/proxy/assign", method = MethodType.GET)
    public String assignSystems() throws ServletException, GeneralSecurityException, SQLException {
        if (systemSelectId != null) {
            ProxyDB.getProxies(systemSelectId,sortedSet);
        } else {
            httpResponse = "Could not load ";
        }
        return "/manage/view_proxies.html";

    }

    @Kontrol(path = "/admin/proxy/select", method = MethodType.GET)
    public String selectSystem() throws ServletException, GeneralSecurityException, SQLException {

        Long userId = AuthUtil.getUserId(getRequest());
        System.out.println("user id is " + userId);
        List<Long> systemIds = SystemDB.getAllSystemIdsForUser(userId);

        sortedSet = ProxyDB.getProxies(systemIds,sortedSet);


        return "/admin/select_proxies.html";

    }

    @Kontrol(path = "/manage/proxy/delete", method = MethodType.GET)
    public String removeSystem() throws ServletException, GeneralSecurityException, SQLException {
        if (systemSelectId != null) {
            System.out.println("deleting " + proxyId + " ore " + proxy.getProxyId());
            if (proxy != null) {
                System.out.println("deleting " + proxyId + " ore " + proxy.getProxyId());
                ProxyHost px = ProxyDB.getProxyHost(proxyId);
                if (null != px)
                    ProxyDB.removeProxy(px, systemSelectId);
            }
        } else {
            System.out.println("NOT deleting " + proxyId + " ore " + proxy.getProxyId());
            httpResponse = "Could not load ";
        }
        ProxyDB.getProxies(systemSelectId,sortedSet);
        return "/manage/view_proxies.html";
    }



    @Kontrol(path = "/manage/proxy/saveProxy", method = MethodType.POST)
    public String addProxy() throws ServletException, GeneralSecurityException, SQLException {
        if (systemSelectId != null) {
            System.out.println("Saving proxy host " + proxyId + " " );
            if (proxyId >= 0){
                ProxyHost prevProxy = ProxyDB.getProxyHost(proxyId);
                ProxyDB.updateProxy(prevProxy,proxy, SystemDB.getSystem(systemSelectId));
            }
            else {
                ProxyDB.addProxyHost(proxy, SystemDB.getSystem(systemSelectId));
            }
       } else {
            throw new RuntimeException("Cannot view proxies");
        }

        ProxyDB.getProxies(systemSelectId,sortedSet);
        return "/manage/view_proxies.html";
    }

    private ServletResponse getHttpResponse(Long userId, Long sessionId) throws ServletException {
        return getHttpResponse(userId,sessionId,true);
    }

    private ServletResponse getHttpResponse(Long userId, Long sessionId, boolean parse) throws ServletException {
        ServletResponse proxyResponse = ServletResponse.builder().build();
        try {

                List<ProxyHost> proxies = ProxyDB.getProxies(systemSelectId);

                List<String> knownHosts = new ArrayList<>();

                proxies.stream().forEach( x -> knownHosts.add(x.getHost()));

                System.out.println("ohhh " + proxyId);

                ProxyHost host = ProxyDB.getProxyHost(proxyId);
                String path = "/";

                if (url != null && !url.isEmpty()){
                    for(ProxyHost dbHost : proxies){
                        URL aURL = null;
                        try {
                            aURL = new URL(url);
                            if (aURL.getHost().equals(dbHost.getHost())){
                                host = dbHost;
                                if (!aURL.getPath().isEmpty())
                                    path = aURL.getPath();
                                break;
                            }
                        } catch (MalformedURLException e) {
                            System.out.println("url is " + url);
                            e.printStackTrace();
                        }
                    }
                }


                //get status
                //if initial status run script

                    //set current session
                proxyResponse = SSHUtil.tunnelURL(passphrase, password, userId, sessionId, host.getHostSystem(), userSchSessionMap, host.getHost(),host.getPort(), path);
                httpResponse = proxyResponse.getUtfHttpResponse();
                //System.out.println("httprsp is " + httpResponse);
            if(parse) {
                Document doc = Jsoup.parse(httpResponse);

                Elements links = doc.select("a");

                for (Element link : links) {
                    if (link.attr("href") != null) {
                        String linkStr = link.attr("href").toLowerCase();
                        URL aURL = null;
                        try {
                            aURL = new URL(linkStr);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }

                        if (null != aURL && knownHosts.contains(aURL.getHost())) {

                            String url = link.attr("href");
                            link.attr("href", "/admin/proxy/http.ktrl?systemSelectId=" + systemSelectId + "&proxyId=" + proxyId + "&_csrf=" + getRequest().getParameter(SecurityFilter._CSRF) + "&url=" + url);

                        } else {
                            System.out.println(host.getHost() + " did not match " + link.attr("href"));
                            link.removeAttr("href");
                        }

                    } else if (link.attr("src") != null) {
                        String srcStr = link.attr("src").toLowerCase();
                        System.out.println("src str is " + srcStr);
                    }

                }

                links = doc.select("script");

                String baseUrl = FilenameUtils.getPath(url);

                if (baseUrl.isEmpty()){
                    baseUrl = "http://" + host.getHost() + ":" + host.getPort();
                }

                for (Element link : links) {
                    if (link.attr("src") != null) {
                        String srcStr = link.attr("src").toLowerCase();
                        System.out.println("src str is " + srcStr);
                        URL aURL = null;

                        if (srcStr.startsWith("./")) {


                            String myFile = FilenameUtils.getBaseName(srcStr)
                                    + "." + FilenameUtils.getExtension(srcStr);

                            System.out.println(baseUrl);
                            System.out.println(myFile);
                            srcStr = baseUrl + "/" + myFile;
                            //srcStr = host.getHost() + ":" + host.getPort() + "/" + srcStr.
                            System.out.println("src is now " + srcStr);
                        }
                        try {

                            aURL = new URL(srcStr);


                        } catch (MalformedURLException e) {
                            if (e.getMessage().contains("no protocol")){
                                String myFile = FilenameUtils.getBaseName(srcStr)
                                        + "." + FilenameUtils.getExtension(srcStr);
                                srcStr = baseUrl + "/" + myFile;
                            }
                            try {
                                aURL = new URL(srcStr);
                            } catch (MalformedURLException ex) {
                                e.printStackTrace();
                            }

                            //throw new RuntimeException(e);
                        }

                        if (null != aURL && knownHosts.contains(aURL.getHost())) {

                            System.out.println("Setting path");
                            link.attr("src", "/admin/proxy/get.ktrl?systemSelectId=" + systemSelectId + "&proxyId=" + proxyId + "&_csrf=" + getRequest().getParameter(SecurityFilter._CSRF) + "&url=" + srcStr);

                        } else {
                            System.out.println(host.getHost() + " did not match " + link.attr("src"));
                            link.removeAttr("src");
                        }
                    }

                }

                links = doc.select("link");

                for (Element link : links) {
                    if (link.attr("href") != null) {
                        String srcStr = link.attr("href").toLowerCase();
                        System.out.println("src str is " + srcStr);
                        URL aURL = null;
                        String myFile = FilenameUtils.getBaseName(srcStr)
                                + "." + FilenameUtils.getExtension(srcStr);

                        if (srcStr.startsWith("./") ||
                                myFile.equals(srcStr)) {




                            System.out.println(baseUrl);
                            System.out.println(myFile);
                            srcStr = baseUrl + "/" + myFile;
                            //srcStr = host.getHost() + ":" + host.getPort() + "/" + srcStr.
                            System.out.println("src is now " + srcStr);
                        }
                        try {

                            aURL = new URL(srcStr);


                        } catch (MalformedURLException e) {
                            if (e.getMessage().contains("no protocol")){
                                System.out.println("adjusting from " + url);
                                srcStr = baseUrl + "/" + myFile;

                                try {
                                    aURL = new URL(srcStr);
                                } catch (MalformedURLException ex) {
                                    ex.printStackTrace();
                                }

                            }
                            //throw new RuntimeException(e);
                        }

                        if (null != aURL && knownHosts.contains(aURL.getHost())) {

                            System.out.println("Setting path");
                            link.attr("href", "/admin/proxy/get.ktrl?systemSelectId=" + systemSelectId + "&proxyId=" + proxyId + "&_csrf=" + getRequest().getParameter(SecurityFilter._CSRF) + "&url=" + srcStr);

                        } else {
                            System.out.println(host.getHost() + " did not match " + link.attr("href"));
                            link.removeAttr("href");
                        }
                    }

                }
                httpResponse = doc.toString();

            }




        } catch (SQLException | GeneralSecurityException ex) {
            ex.printStackTrace();;
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return ServletResponse.builder()
                .type(ServletResponseType.RAW).contentType(proxyResponse.getContentType()).utfHttpResponse(httpResponse).build();
    }

}
