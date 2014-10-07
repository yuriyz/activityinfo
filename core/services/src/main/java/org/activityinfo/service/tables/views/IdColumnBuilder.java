package org.activityinfo.service.tables.views;

import com.google.common.collect.Lists;
import org.activityinfo.model.record.Record;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.table.ColumnView;
import org.activityinfo.model.table.views.StringArrayColumnView;

import java.util.List;

public class IdColumnBuilder implements ColumnScanner {

    private List<String> ids = Lists.newArrayList();

    @Override
    public ColumnView get() {
        return new StringArrayColumnView(ids);
    }

    @Override
    public void accept(ResourceId resourceId, Record value) {
        ids.add(resourceId.asString());
    }

    @Override
    public void finalizeView() {

    }
}
