package com.editable.components.core.models;

import com.adobe.cq.wcm.core.components.models.ExperienceFragment;
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.via.ResourceSuperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Iterator;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    resourceType = EditableComponent.RESOURCE_TYPE
)
@Exporter(
        name = "jackson",
        extensions = {"json"}
)
public class EditableComponent {

    private static final Logger log = LoggerFactory.getLogger(EditableComponent.class);

    public static final String RESOURCE_TYPE = "editable-components/components/editablecomponent/v1/editablecomponent";

    @Self
    @Via(type = ResourceSuperType.class)
    ExperienceFragment experienceFragment;

    @Inject
    @Named("fragmentVariationPath")
    @Via("resource")
    @Optional
    private String fragmentVariationPath;

    @Inject
    private Resource resource;

    @Inject
    private SlingHttpServletRequest request;

    public String getLocalizedFragmentVariationPath() {
        return fragmentVariationPath + "/jcr:content/root";
    }

    @PostConstruct
    public void init() {
        String originPath = getLocalizedFragmentVariationPath();
        String destination = resource.getPath();
        copyNodes(originPath,destination);
    }


    private boolean resourcePathContains(Resource resource, String value) {
        String resourceName = StringUtils.substringBefore(getSubPath(resource.getPath()),"_");
        return StringUtils.containsAny(value,resource.getPath());
    }

    /**
     * Given uri string, returns last string after the last slash('/')
     * @param pagePath
     * @return
     */
    private String getSubPath(final String pagePath) {
        return StringUtils.substringAfterLast(pagePath, "/");
    }


    /**
     * Given origin path and destination path, copies the node without deleting the origin
     * @param originPath
     * @param destinationPath
     */
    private void copyNodes(String originPath, String destinationPath) {
        try {
            ResourceResolver resourceResolver = request.getResourceResolver();
            Resource originResource = resourceResolver.getResource(originPath);
            Resource destinationResource = resourceResolver.getResource(destinationPath);
            if (originResource != null && destinationResource != null) {
                final Iterator<Resource> resourceIterator = originResource.listChildren();

                Iterable<Resource> iterable = () -> resourceIterator;

                iterable.forEach(currentResource -> {
                            try {
                                Node srcNode = currentResource.adaptTo(Node.class);
                                Node destNode = destinationResource.adaptTo(Node.class);
                                if (srcNode != null && destNode != null) {
                                    JcrUtil.copy(srcNode,destNode,null, true);
                                }
                            } catch ( RepositoryException e) {
                                log.error("Error copying "+ currentResource.getName() +" -> Error" + e.getMessage());
                            }
                        }
                );
            }
            resourceResolver.commit();
        } catch (PersistenceException e) {
            log.error("Error copying"+ originPath +" -> Error: " + e.toString());
        }
    }
}
