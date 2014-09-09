package org.activityinfo.ui.app.client.chrome;

import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.FolderProjection;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceNode;
import org.activityinfo.model.table.TableData;
import org.activityinfo.model.table.TableModel;
import org.activityinfo.promise.Promise;
import org.activityinfo.service.store.ResourceStore;
import org.activityinfo.service.store.UpdateResult;
import org.activityinfo.service.store.RemoteStoreService;

import java.util.List;

public class TestRemoteStoreService implements RemoteStoreService {

    private ResourceStore store;

    public TestRemoteStoreService(ResourceStore store) {
        this.store = store;
    }

    @Override
    public Promise<Resource> get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<UpdateResult> put(Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<UpdateResult> create(Resource resource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<List<ResourceNode>> getWorkspaces() {
        return Promise.resolved(store.getOwnedOrSharedWorkspaces(AuthenticatedUser.getAnonymous()));
    }

    @Override
    public Promise<TableData> queryTable(TableModel tableModel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<FolderProjection> getFolder(ResourceId rootId) {
        throw new UnsupportedOperationException();
    }
}