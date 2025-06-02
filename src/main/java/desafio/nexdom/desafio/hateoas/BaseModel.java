package desafio.nexdom.desafio.hateoas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Classe base para modelos HATEOAS que fornece métodos utilitários
 * para adicionar links de forma consistente.
 * 
 * @param <T> O tipo do modelo que estende esta classe base
 */
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

    /**
     * Adiciona um link para o próprio recurso.
     * 
     * @param controllerClass A classe do controlador
     * @param params Os parâmetros para construir a URI
     * @return O modelo atual para encadeamento
     */
    public T addSelfLink(Class<?> controllerClass, Object... params) {
        try {
            Link link = WebMvcLinkBuilder.linkTo(controllerClass)
                    .slash(params)
                    .withSelfRel()
                    .withType(RequestMethod.GET.name());
            this.add(link);
        } catch (Exception e) {
            // Log the error but don't fail the request
            LOG.error("Error adding self link: {}", e.getMessage());
        }
        return (T) this;
    }

    /**
     * Adiciona um link para uma coleção de recursos.
     * 
     * @param controllerClass A classe do controlador
     * @param rel O nome da relação
     * @return O modelo atual para encadeamento
     */
    public T addCollectionLink(Class<?> controllerClass, String rel) {
        try {
            Link link = WebMvcLinkBuilder.linkTo(controllerClass)
                    .withRel(rel)
                    .withType(RequestMethod.GET.name());
            this.add(link);
        } catch (Exception e) {
            // Log the error but don't fail the request
            LOG.error("Error adding collection link: {}", e.getMessage());
        }
        return (T) this;
    }

    /**
     * Adiciona um link para uma ação relacionada ao recurso.
     * 
     * @param controllerClass A classe do controlador
     * @param rel O nome da relação
     * @param method O método HTTP (GET, POST, PUT, DELETE, etc.)
     * @param params Os parâmetros para construir a URI
     * @return O modelo atual para encadeamento
     */
    public T addActionLink(Class<?> controllerClass, String rel, String method, Object... params) {
        try {
            // Handle the case where the last parameter is for the path variable
            Object lastParam = params.length > 0 ? params[params.length - 1] : "";
            
            Link link = WebMvcLinkBuilder.linkTo(controllerClass)
                    .slash(lastParam)
                    .withRel(rel)
                    .withType(method);
            this.add(link);
        } catch (Exception e) {
            // Log the error but don't fail the request
            LOG.error("Error adding action link: {}", e.getMessage());
        }
        return (T) this;
    }

    /**
     * Adiciona um link personalizado ao modelo.
     * 
     * @param href A URI do link
     * @param rel O nome da relação
     * @param method O método HTTP
     * @return O modelo atual para encadeamento
     */
    public T addLink(String href, String rel, String method) {
        try {
            Link link = Link.of(href).withRel(rel).withType(method);
            this.add(link);
        } catch (Exception e) {
            // Log the error but don't fail the request
            LOG.error("Error adding custom link: {}", e.getMessage());
        }
        return (T) this;
    }

    /**
     * Obtém a URI base da aplicação.
     * 
     * @return A URI base
     */
    protected String getBaseUri() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
    }
}
