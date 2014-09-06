package org.activityinfo.migrator.tables;

import org.activityinfo.migrator.filter.MigrationContext;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.activityinfo.model.legacy.CuidAdapter.*;

public class AdminEntityTable extends SimpleTableMigrator {

    public AdminEntityTable(MigrationContext context) {
        super(context);
    }

    @Override
    protected String query() {
        return "select AdminEntityId, name, code, adminEntityParentId, adminlevelid from adminentity WHERE " +
                filter.adminEntityFilter();
    }

    @Override
    protected Resource toResource(ResultSet rs) throws SQLException {

        ResourceId id = context.resourceId(ADMIN_ENTITY_DOMAIN, rs.getInt("AdminEntityId"));
        ResourceId classId = context.resourceId(ADMIN_LEVEL_DOMAIN, rs.getInt("AdminLevelId"));

        FormInstance instance = new FormInstance(id, classId)
            .set(field(classId, NAME_FIELD), rs.getString("name"))
            .set(field(classId, CODE_FIELD), rs.getString("code"));


        int parentId = rs.getInt("AdminEntityParentId");
        if(!rs.wasNull()) {
            instance.set(field(classId, ADMIN_PARENT_FIELD),
                new ReferenceValue(context.resourceId(ADMIN_ENTITY_DOMAIN, parentId)));
        }

        return instance.asResource();
    }
}
