package com.editable.components.core.listeners;

import com.editable.components.core.jcr.JcrUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

/**
 * Event Listener that listens to Editable component event
 */
@Component(immediate = true)
@ServiceDescription("Event Listener for Editable components")
public class EditableComponentsListener implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(EditableComponentsListener.class);

    public static final String EDITABLE_COMPONENTS_CONTENT_PATH = "/content";

    public static final String EDITABLE_COMPONENT_RESOURCE_SUPER_TYPE = "editable-components/components/editablecomponent/v1/editablecomponent";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private SlingRepository slingRepository;

    private static final String NT_UNSTRUCTURED = "nt:unstructured";

    private Session session;

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            session = JcrUtil.getSession(slingRepository);
            final String[] types = {NT_UNSTRUCTURED};
            final ObservationManager observationManager = session.getWorkspace().getObservationManager();
            observationManager.addEventListener(this, Event.PROPERTY_CHANGED , EDITABLE_COMPONENTS_CONTENT_PATH,
                    true, null, types, false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate() throws RepositoryException {
        if(session != null) {
            ObservationManager observationManager = session.getWorkspace().getObservationManager();
            if (observationManager != null){
                observationManager.removeEventListener(this);
            }
            session.logout();
        }
    }

    @Override
    public void onEvent(EventIterator events) {
        try {
            while(events.hasNext()) {
                Event event = events.nextEvent();
                final String eventPath = event.getPath();
                // check if jcr:lastModified for only JCR modification to the component node
            //    if (StringUtils.equals(StringUtils.substringAfterLast(eventPath, "/"), NameConstants.PN_LAST_MOD)) {
                    // Current event absolute path
                    final String eventJcrContentPath = event.getIdentifier();
                    final ResourceResolver resourceResolver = JcrUtil.getResourceResolver(resolverFactory);
                    final Resource componentResource = resourceResolver.getResource(eventJcrContentPath);
                    if (componentResource != null) {
                       final String resourceType = componentResource.getResourceType();
                        // verify is Editable Component modification
                        if (StringUtils.contains(resourceType, EDITABLE_COMPONENT_RESOURCE_SUPER_TYPE)) {
                            // add jcr:content/root
                            String componentsStructureOrigin = componentResource.getValueMap().get("fragmentVariationPath", "")+"/jcr:content/root";
                            // Localize the components structure view
                            JcrUtil.copyNodes(componentsStructureOrigin,eventJcrContentPath,resolverFactory);
                        }
                    }
                }
            //}
        } catch (Exception e) {
            log.error("Exception occurred", e);
        }
    }
}
