/**
 *    Copyright (C) 2018 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.base;

import io.bastillion.manage.model.ServletResponseType;
import io.bastillion.manage.model.ServletResponse;
import loophole.mvc.filter.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("*" + DispatcherServlet.CTR_EXT)
public class DispatcherServlet extends HttpServlet {


    private static Logger log = LoggerFactory.getLogger(DispatcherServlet.class);
    public static final String CTR_EXT = ".ktrl";
    private static final long serialVersionUID = 412L;

    public DispatcherServlet() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    /**
     * Execute through base controller
     *
     * @param request  HTTP servlet request
     * @param response HTTP servlet response
     */
    private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        loophole.mvc.base.BaseKontroller bc = new BaseKontroller(request, response);

        ServletResponse resp = bc.execute();
        String forward = resp.getUtfHttpResponse();
        if (resp != null) {
            switch (resp.getType()) {
                case REDIRECT:
                    forward = forward.contains("?") ? forward + "&" : forward + "?";
                    forward = forward + SecurityFilter._CSRF + "=" + request.getSession().getAttribute(SecurityFilter._CSRF);
                    forward = request.getContextPath() + forward.replaceAll("redirect:", "");
                    log.debug("redirect : " + forward);
                    response.sendRedirect(forward);
                    break;
                case FORWARD:
                    forward = forward.replaceAll("forward:", "");
                    log.debug("forward: " + forward);
                    request.getRequestDispatcher(forward)
                            .forward(request, response);
                    break;
                case UNKNOWN:
                    if (null != forward) {
                        if (forward.contains("redirect:")) {
                            //add csrf to redirect
                            forward = forward.contains("?") ? forward + "&" : forward + "?";
                            forward = forward + SecurityFilter._CSRF + "=" + request.getSession().getAttribute(SecurityFilter._CSRF);
                            forward = request.getContextPath() + forward.replaceAll("redirect:", "");
                            log.debug("redirect : " + forward);
                            response.sendRedirect(forward);
                        } else {
                            forward = forward.replaceAll("forward:", "");
                            log.debug("forward: " + forward);
                            request.getRequestDispatcher(forward)
                                    .forward(request, response);
                        }
                    }
                case RAW:
                    response.setContentType(resp.getContentType());
                    response.getWriter().write(forward);
                    break;

            }
        }
            /*
            if (forward.contains("redirect:")) {
                //add csrf to redirect
                forward = forward.contains("?") ? forward + "&" : forward + "?";
                forward = forward + SecurityFilter._CSRF + "=" + request.getSession().getAttribute(SecurityFilter._CSRF);
                forward = request.getContextPath() + forward.replaceAll("redirect:", "");
                log.debug("redirect : " + forward);
                response.sendRedirect(forward);
            }else if (forward.startsWith("raw:")){
                forward = forward.substring(4);
                response.setContentType("text/css");
                response.getWriter().write(forward);
            } else {
                forward = forward.replaceAll("forward:", "");
                log.debug("forward: " + forward);
                request.getRequestDispatcher(forward)
                        .forward(request, response);
            }
        }*/

    }
}