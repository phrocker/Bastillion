/**
 *    Copyright (C) 2018 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.config;

import loophole.mvc.base.TemplateServlet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TemplateConfig implements ServletContextListener {

	private static final String TEMPLATE_ENGINE = "com.thymeleafexamples.thymeleaf3.TemplateEngineInstance";
	private static final String TEMPLATE_ENGINEF = "com.thymeleafexamples.thymeleaf3.TemplateEngineInstanceFile";
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		TemplateEngine engine = templateEngine(sce.getServletContext());
		sce.getServletContext().setAttribute(TEMPLATE_ENGINE, engine);
		engine = templateEngine(sce.getServletContext(), true);
		sce.getServletContext().setAttribute(TEMPLATE_ENGINEF, engine);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

	public TemplateEngine templateEngine(ServletContext servletContext) {
		return templateEngine(servletContext,false);
	}

	public TemplateEngine templateEngine(ServletContext servletContext, boolean treatAsContent) {
		TemplateEngine engine = new TemplateEngine();
		if (treatAsContent) {
			engine.setTemplateResolver(contentHandlingResolver(servletContext));
		} else {
			engine.setTemplateResolver(templateResolver(servletContext));
		}
		return engine;
	}

	public ITemplateResolver templateResolver(ServletContext servletContext) {
		JavaxServletWebApplication webApp = JavaxServletWebApplication.buildApplication(servletContext);
		WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(webApp);
		resolver.setPrefix("/");
		resolver.setSuffix(TemplateServlet.VIEW_EXT);
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCacheable(true);
		resolver.setCacheTTLMs(60000L);
		resolver.setCharacterEncoding("UTF-8");
		return resolver;
	}

	public ITemplateResolver contentHandlingResolver(ServletContext servletContext) {
		JavaxServletWebApplication webApp = JavaxServletWebApplication.buildApplication(servletContext);
		FileOrWebAppResolver resolver = new FileOrWebAppResolver(webApp);
		resolver.setPrefix("/");
		resolver.setSuffix(TemplateServlet.VIEW_EXT);
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCacheable(true);
		resolver.setCacheTTLMs(60000L);
		resolver.setCharacterEncoding("UTF-8");
		return resolver;
	}

	
	public static TemplateEngine getTemplateEngine(ServletContext context) {
		return (TemplateEngine) context.getAttribute(TEMPLATE_ENGINE);
	}

	public static TemplateEngine getTemplateEngineForFile(ServletContext context) {
		return (TemplateEngine) context.getAttribute(TEMPLATE_ENGINEF);
	}
}