package org.activityinfo.ui.full.client.local;

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

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.api.shared.command.*;
import org.activityinfo.api.shared.impl.*;
import org.activityinfo.ui.full.client.local.command.HandlerRegistry;

public class HandlerRegistryProvider implements Provider<HandlerRegistry> {

    private final HandlerRegistry registry;

    @Inject
    public HandlerRegistryProvider(
            GetSchemaHandler schemaHandler,
            GetSitesHandler sitesHandler,
            GetAdminEntitiesHandler adminHandler,
            GetPartnersDimensionHandler partnersDimensionHandler,
            GetAttributeGroupsDimensionHandler attributeGroupsDimensionHandler,
            CreateSiteHandler createSiteHandler,
            UpdateSiteHandler updateSiteHandler,
            CreateLocationHandler createLocationHandler,
            SearchLocationsHandler searchLocationsHandler,
            // SearchHandler searchHandler,
            PivotSitesHandler pivotSitesHandler,
            GeneratePivotTableHandler generatePivotTableHandler,
            GetLocationsHandler getLocationsHandler,
            DeleteSiteHandler deleteSiteHandler,
            GetSiteAttachmentsHandler getSiteAttachmentsHandler) {

        registry = new HandlerRegistry();
        registry.registerHandler(GetSchema.class, schemaHandler);
        registry.registerHandler(GetSites.class, sitesHandler);
        registry.registerHandler(GetAdminEntities.class, adminHandler);
        registry.registerHandler(GetPartnersDimension.class, partnersDimensionHandler);
        registry.registerHandler(GetAttributeGroupsDimension.class, attributeGroupsDimensionHandler);
        registry.registerHandler(CreateSite.class, createSiteHandler);
        registry.registerHandler(UpdateSite.class, updateSiteHandler);
        registry.registerHandler(CreateLocation.class, createLocationHandler);
        // registry.registerHandler(Search.class, searchHandler);
        registry.registerHandler(SearchLocations.class, searchLocationsHandler);
        registry.registerHandler(GeneratePivotTable.class, generatePivotTableHandler);
        registry.registerHandler(PivotSites.class, pivotSitesHandler);
        registry.registerHandler(GetLocations.class, getLocationsHandler);
        registry.registerHandler(DeleteSite.class, deleteSiteHandler);
        registry.registerHandler(GetSiteAttachments.class, getSiteAttachmentsHandler);
    }

    @Override
    public HandlerRegistry get() {
        return registry;
    }
}
