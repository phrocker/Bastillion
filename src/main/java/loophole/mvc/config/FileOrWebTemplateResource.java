package loophole.mvc.config;

import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.WebApplicationTemplateResource;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;
import org.thymeleaf.web.IWebApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class FileOrWebTemplateResource implements ITemplateResource {


    private final IWebApplication webApplication;
    private final String path;
    private final String characterEncoding;
    private final boolean isContent;


    public FileOrWebTemplateResource(final IWebApplication webApplication, final String path, final String characterEncoding) {

        super();

        Validate.notNull(webApplication, "Web Application object cannot be null");
        Validate.notEmpty(path, "Resource Path cannot be null or empty");
        // Character encoding CAN be null (system default will be used)

        this.webApplication = webApplication;
        int htmlIndex = path.indexOf("<html");
        System.out.println("html idnex is " + htmlIndex);
        if (htmlIndex < 0) {
            System.out.println("content is " + path);
            final String cleanPath = TemplateResourceUtils.cleanPath(path);
            this.path = (cleanPath.charAt(0) != '/' ? ("/" + cleanPath) : cleanPath);
            this.isContent = false;
        } else {
            if (path.endsWith(".html"))
            {
                path.substring(0,path.length()-5);
            }
            System.out.println(path);
            this.path = path;
            this.isContent = true;
        }
        this.characterEncoding = characterEncoding;

    }




    public String getDescription() {
        return this.path;
    }




    public String getBaseName() {
        return TemplateResourceUtils.computeBaseName(this.path);
    }




    public Reader reader() throws IOException {

        if (isContent){
            return new StringReader(this.path);
        }
        final InputStream inputStream = this.webApplication.getResourceAsStream(this.path);
        if (inputStream == null) {
            throw new FileNotFoundException(String.format("Web Application resource \"%s\" does not exist", this.path));
        }

        if (!StringUtils.isEmptyOrWhitespace(this.characterEncoding)) {
            return new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream), this.characterEncoding));
        }

        return new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));

    }




    public ITemplateResource relative(final String relativeLocation) {

        Validate.notEmpty(relativeLocation, "Relative Path cannot be null or empty");

        final String fullRelativeLocation = TemplateResourceUtils.computeRelativeLocation(this.path, relativeLocation);
        return new WebApplicationTemplateResource(this.webApplication, fullRelativeLocation, this.characterEncoding);

    }




    public boolean exists() {
        return this.webApplication.resourceExists(this.path);
    }


}
