package org.activityinfo.server.command;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.CloneDatabase;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetFormClass;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElement;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author yuriyz on 11/26/2014.
 */
@RunWith(InjectionSupport.class)
public class CloneDatabaseTest extends CommandTestCase2 {

    public static final int PEAR_DATABASE_ID = 1;

    @Test
    @OnDataSet("/dbunit/sites-simple1.db.xml")
    public void fullClone() throws CommandException {

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO pearDb = schema.getDatabaseById(PEAR_DATABASE_ID);

        CloneDatabase cloneDatabase = new CloneDatabase()
                .setSourceDatabaseId(pearDb.getId())
                .setCopyData(true)
                .setCopyPartners(true)
                .setCopyUsers(true)
                .setCountryId(pearDb.getCountry().getId())
                .setName("PearClone")
                .setDescription("PearClone Description");

        CreateResult cloneResult = execute(cloneDatabase);
        assertNotEquals(cloneResult.getNewId(), pearDb.getId());

        assertLegacy(cloneResult.getNewId(), pearDb.getId(), true);

    }

    private void assertLegacy(int newDbId, int sourceDbId, boolean assertDesignData) {
        assertNotEquals(newDbId, sourceDbId);

        SchemaDTO schema = execute(new GetSchema());

        UserDatabaseDTO sourceDb = schema.getDatabaseById(sourceDbId);
        UserDatabaseDTO targetDb = schema.getDatabaseById(newDbId);

        assertEquals(targetDb.getName(), "PearClone");
        assertEquals(targetDb.getFullName(), "PearClone Description");

        if (assertDesignData) {

            assertEquals(sourceDb.getActivities().size(), targetDb.getActivities().size());
            for (ActivityDTO activityDTO : sourceDb.getActivities()) {
                ActivityFormDTO sourceActivity = execute(new GetActivityForm(activityDTO.getId()));
                ActivityFormDTO targetActivity = execute(new GetActivityForm(entityByName(targetDb.getActivities(), sourceActivity.getName()).getId()));

                // legacy level
                assertActivityClone(sourceActivity, targetActivity);

                // form class level
                FormClass sourceFormClass = execute(new GetFormClass(sourceActivity.getResourceId())).getFormClass();
                FormClass targetFormClass = execute(new GetFormClass(targetActivity.getResourceId())).getFormClass();
                assertFormClass(sourceFormClass, targetFormClass);
            }
        }
    }

    private void assertActivityClone(ActivityFormDTO sourceActivity, ActivityFormDTO targetActivity) {
        assertProperties(sourceActivity, sourceActivity,
                "name", "category", "classicView");

        // indicators
        assertEquals(sourceActivity.getIndicators().size(), targetActivity.getIndicators().size());
        for (IndicatorDTO sourceIndicator : sourceActivity.getIndicators()) {
            IndicatorDTO targetIndicator = entityByName(targetActivity.getIndicators(), sourceIndicator.getName());

            assertProperties(sourceIndicator, targetIndicator,
                    "name", "units", "expression", "listHeader", "skipExpression", "nameInExpression", "calculatedAutomatically");
        }

        // attributes groups
        for (AttributeGroupDTO sourceAttributeGroup : sourceActivity.getAttributeGroups()) {
            AttributeGroupDTO targetGroup = entityByName(targetActivity.getAttributeGroups(), sourceAttributeGroup.getName());

            assertProperties(sourceAttributeGroup, targetGroup,
                    "name", "mandatory", "defaultValue", "workflow", "multipleAllowed", "sortOrder");

            // attributes
            for (AttributeDTO sourceAttribute : sourceAttributeGroup.getAttributes()) {
                AttributeDTO targetAttribute = entityByName(targetGroup.getAttributes(), sourceAttribute.getName());

                assertProperties(sourceAttribute, targetAttribute, "name");
            }
        }


    }

    private void assertFormClass(FormClass sourceFormClass, FormClass targetFormClass) {
        assertNotEquals(sourceFormClass.getId(), targetFormClass.getId());

        assertEquals(sourceFormClass.getLabel(), targetFormClass.getLabel());
        assertEquals(sourceFormClass.getDescription(), targetFormClass.getDescription());

        // TODO !!!

        // fields
//        for (FormField sourceField : sourceFormClass.getFields()) {
//            FormField targetField = (FormField) elementByName(targetFormClass.getElements(), sourceField.getLabel());
//
//            assertEquals(sourceField.getDescription(), targetField.getDescription());
//            assertEquals(sourceField.getCode(), targetField.getCode());
//            assertEquals(sourceField.getRelevanceConditionExpression(), targetField.getRelevanceConditionExpression());
//
//            if (sourceField.getType() instanceof ReferenceType) {
//                // need something more sophisticated to check equality of ReferenceType
//            } else {
//                assertEquals(sourceField.getType(), targetField.getType());
//            }
//        }
//
//        // sections
//        for (FormSection sourceSection : sourceFormClass.getSections()) {
//            FormSection targetSection = (FormSection) elementByName(targetFormClass.getElements(), sourceSection.getLabel());
//
//            assertEquals(sourceSection.getLabel(), targetSection.getLabel());
//        }
    }

    private static <T extends BaseModelData> void assertProperties(T entity1, T entity2, String... propertyNames) {
        if (propertyNames == null || propertyNames.length == 0) {
            throw new RuntimeException("No property names specified.");
        }

        for (String property : propertyNames) {
            assertEquals(entity1.get(property), entity2.get(property));
        }
    }

    private static <T extends BaseModelData> T entityByName(List<T> entities, String name) {
        for (T entity : entities) {
            if (name.equals(entity.get("name"))) {
                return entity;
            }
        }
        throw new RuntimeException("No entity with name:" + name);
    }

    private static <T extends FormElement> T elementByName(List<T> list, String label) {
        for (T entity : list) {
            if (label.equals(entity.getLabel())) {
                return entity;
            }
        }
        throw new RuntimeException("No FormElement with label:" + label);

    }
}
