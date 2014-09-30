package org.activityinfo.test;

import com.sun.jersey.api.client.UniformInterfaceException;
import org.activityinfo.client.ActivityInfoClient;
import org.activityinfo.model.record.Records;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceNode;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.system.FolderClass;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import java.security.SecureRandom;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.activityinfo.model.resource.Resources.ROOT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AuthorizationTest {
    final private static ResourceId workspaceId = Resources.generateId();
    final private static ResourceId resourceId = Resources.generateId();

    @Test
    public void test() {
        final SecureRandom secureRandom = new SecureRandom();
        final String email = Long.toHexString(secureRandom.nextLong()) + "@example.com";
        final String password = Long.toHexString(secureRandom.nextLong());
        final ActivityInfoClient client = new ActivityInfoClient(TestConfig.getRootURI(), email, password);

        if (!client.createUser()) throw new AssumptionViolatedException("Server is not configured to run in test mode");

        try {
            client.get(resourceId);
            fail("An ID locally generated at random should not already be present inside the server's resource store!");
        } catch (UniformInterfaceException uniformInterfaceException) {
            assertEquals(NOT_FOUND.getStatusCode(), uniformInterfaceException.getResponse().getStatus());
        }

        assertTrue(client.getOwnedOrSharedWorkspaces().isEmpty());

        Resource workspace = Resources.createResource();
        workspace.setId(workspaceId);
        workspace.setOwnerId(ROOT_ID);
        workspace.setVersion(1);
        workspace.setValue(Records.builder(FolderClass.CLASS_ID)
                .set(FolderClass.LABEL_FIELD_ID.asString(), "Workspace")
                .build());
        client.create(workspace);

        Resource folder = Resources.createResource();
        folder.setId(resourceId);
        folder.setOwnerId(workspaceId);
        folder.setVersion(3);
        folder.setValue(Records.builder(FolderClass.CLASS_ID)
                .set(FolderClass.LABEL_FIELD_ID.asString(), "Folder")
                .build());
        client.create(folder);

        for (ResourceNode resourceNode : client.getOwnedOrSharedWorkspaces()) {
            Resource resource = client.get(resourceNode.getId());
            assertEquals(workspace, resource);
            assertNotEquals(folder, resource);

            resource = client.get(resourceId);
            assertEquals(folder, resource);
            assertNotEquals(workspace, resource);
        }

        List<Resource> resources = client.getUpdates(workspaceId, 0);
        assertEquals(3, resources.size());
        assertEquals(workspaceId, resources.get(0).getId());
        assertNotEquals(workspaceId, resources.get(1).getId());
        assertNotEquals(resourceId, resources.get(1).getId());
        assertEquals(resourceId, resources.get(2).getId());
    }
}
