package org.activityinfo.service.store;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceNode;
import org.activityinfo.model.resource.ResourceTree;
import org.activityinfo.service.jaxrs.EntityTags;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests the Jersey setup, not the actual service itself
 */
public class ResourceStoreTest extends JerseyTest {

    public ResourceStoreTest() {
        super(new InMemoryTestContainerFactory());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new InMemoryTestContainerFactory();
    }

    @Override
    protected AppDescriptor configure() {
        return new LowLevelAppDescriptor.Builder(new TestResourceConfig()).build();
    }

    private WebResource getStoreService() {
        WebResource webResource = resource();
        return webResource.path("service").path("store");
    }

    @Test
    public void get() {
        Resource resource = getStoreService()
                .path("resource")
                .path(ResourceStoreStub.MY_RESOURCE_ID.asString())
                .accept(MediaType.APPLICATION_JSON)
                .get(Resource.class);

        Resource expected = ResourceStoreStub.getMyResource();
        assertThat(resource.getId(), equalTo(expected.getId()));
        assertThat(resource.getOwnerId(), equalTo(expected.getOwnerId()));
        assertThat(resource.getVersion(), equalTo(expected.getVersion()));
        assertThat(resource.getProperties(), equalTo(expected.getProperties()));
        assertThat(resource, equalTo(expected));
    }

    @Test
    public void create() {
        Resource newResource = ResourceStoreStub.getNewResourceThatWillCommit();
        ClientResponse response = getStoreService()
                .path("resource")
                .path(ResourceStoreStub.NEW_RESOURCE_ID_TO_COMMIT.asString())
                .entity(newResource, MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class);

        assertThat(response.getStatusInfo(), equalTo((Response.StatusType)ClientResponse.Status.CREATED));
        assertThat(response.getEntityTag(), equalTo(EntityTags.ofResource(
                newResource.getId(),
                ResourceStoreStub.NEW_VERSION)));
    }

    @Test
    public void queryTree() {

        ResourceTreeRequest request = new ResourceTreeRequest(ResourceId.ROOT_ID);

        ResourceTree tree = getStoreService()
                .path("query")
                .path("tree")
                .accept(MediaType.APPLICATION_JSON)
                .entity(request, MediaType.APPLICATION_JSON_TYPE)
                .post(ResourceTree.class);

        assertThat(tree.getRootNode().getChildren(), hasSize(2));
    }

    @Test
    public void getUserRoots() {

        List<ResourceNode> roots = getStoreService()
                .path("query")
                .path("roots")
                .get(new GenericType<List<ResourceNode>>() { });


        assertThat(roots, hasSize(3));

    }
}