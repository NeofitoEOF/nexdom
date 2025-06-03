package desafio.nexdom.desafio.hateoas;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BaseModel<T extends RepresentationModel<? extends T>> extends RepresentationModel<T> {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BaseModel.class);
    
    @JsonUnwrapped
    private Object content;
    
    public BaseModel() {
        super();
    }
    
    public BaseModel(Object content) {
        this();
        this.content = content;
    }

    public T addSelfLink(Class<?> controllerClass, Object... params) {
        try {
            Link link = WebMvcLinkBuilder.linkTo(controllerClass)
                    .slash(params)
                    .withSelfRel()
                    .withType(RequestMethod.GET.name());
            this.add(link);
        } catch (Exception e) {
            LOG.error("Error adding self link: {}", e.getMessage());
        }
        return (T) this;
    }

    public T addCollectionLink(Class<?> controllerClass, String rel) {
        try {
            Link link = WebMvcLinkBuilder.linkTo(controllerClass)
                    .withRel(rel)
                    .withType(RequestMethod.GET.name());
            this.add(link);
        } catch (Exception e) {
            LOG.error("Error adding collection link: {}", e.getMessage());
        }
        return (T) this;
    }

    public T addActionLink(Class<?> controllerClass, String rel, String method, Object... params) {
        try {
            Object lastParam = params.length > 0 ? params[params.length - 1] : "";
            
            Link link = WebMvcLinkBuilder.linkTo(controllerClass)
                    .slash(lastParam)
                    .withRel(rel)
                    .withType(method);
            this.add(link);
        } catch (Exception e) {
            LOG.error("Error adding action link: {}", e.getMessage());
        }
        return (T) this;
    }

    public T addLink(String href, String rel, String method) {
        try {
            Link link = Link.of(href).withRel(rel).withType(method);
            this.add(link);
        } catch (Exception e) {
            LOG.error("Error adding custom link: {}", e.getMessage());
        }
        return (T) this;
    }
    protected String getBaseUri() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
    }
}
