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

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.GetSiteHistory;
import org.activityinfo.legacy.shared.command.GetSiteHistory.GetSiteHistoryResult;
import org.activityinfo.legacy.shared.model.SiteHistoryDTO;
import org.activityinfo.server.database.OnDataSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class GetSiteHistoryTest extends CommandTestCase {
    @Test
    public void testGetSiteHistory() {
        setUser(1);

        int siteId = 1;

        GetSiteHistoryResult result = execute(new GetSiteHistory(siteId));
        assertNotNull(result);
        assertEquals(2, result.getSiteHistories().size());

        SiteHistoryDTO dto1 = result.getSiteHistories().get(0);
        assertTrue(dto1.isInitial());
        Map<String, Object> map = dto1.getJsonMap();
        assertEquals(new Integer(1), map.get("id"));
        assertEquals("1", String.valueOf(map.get("id")));
        assertEquals("54.0", String.valueOf(map.get("I4925")));
        assertEquals("site 1 my first comment", map.get("comments"));

        SiteHistoryDTO dto2 = result.getSiteHistories().get(1);
        assertFalse(dto2.isInitial());
        map = dto2.getJsonMap();
        assertNull(map.get("id"));
        assertNull(map.get("I4925"));
        assertEquals("site 1 changed comment", map.get("comments"));

    }
}