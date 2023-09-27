package loophole.mvc.config;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.util.Validate;
import org.thymeleaf.web.IWebApplication;

import java.util.Map;

public class FileOrWebAppResolver extends AbstractConfigurableTemplateResolver {



    private final IWebApplication webApplication;



    public FileOrWebAppResolver(final IWebApplication webApplication) {
        super();
        Validate.notNull(webApplication, "Web Application object cannot be null");
        this.webApplication = webApplication;
    }


    @Override
    protected ITemplateResource computeTemplateResource(
            final IEngineConfiguration configuration, final String ownerTemplate, final String template, final String resourceName, final String characterEncoding, final Map<String, Object> templateResolutionAttributes) {
        return new FileOrWebTemplateResource(this.webApplication, resourceName, characterEncoding);
    }
}
