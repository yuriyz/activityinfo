package org.activityinfo.core.shared.table;
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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Cuid;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.form.tree.FieldPath;
import org.activityinfo.fp.client.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author yuriyz on 5/29/14.
 */
public class TableColumnDataBuilder {

    private final ResourceLocator resourceLocator;

    public TableColumnDataBuilder(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }

    public Promise<TableColumnData> build(TableModel tableModel) {
        final List<Promise<ArrayColumnView>> promises = Lists.newArrayList();
        for (FieldColumn column : tableModel.getColumns()) {
            promises.add(fetchColumn(tableModel.getFormClassId(), column));
        }

        return Promise.waitAll(promises).then(new Supplier<TableColumnData>() {
            @Override
            public TableColumnData get() {
                TableColumnData tableData = new TableColumnData();
                for (Promise<ArrayColumnView> promise : promises) {
                    tableData.getColumnIdToViewMap().put(promise.get().getColumnId(), promise.get());
                }
                return null;
            }
        });
    }


    private Promise<ArrayColumnView> fetchColumn(Cuid formClassId, final FieldColumn column) {
        Promise<QueryResult<Projection>> queryResultPromise = resourceLocator.queryProjection(new InstanceQuery(column.getFieldPaths(),
                new ClassCriteria(formClassId)));

        return queryResultPromise.then(new Function<QueryResult<Projection>, ArrayColumnView>() {
            @Nullable
            @Override
            public ArrayColumnView apply(@Nullable QueryResult<Projection> queryResult) {
                List<Projection> rows = queryResult.getProjections();

                Object[] columnArray = new Object[queryResult.getTotalCount()];
                for (int i = 0; i != queryResult.getTotalCount(); ++i) {
                    for (FieldPath path : column.getFieldPaths()) {
                        Object value = rows.get(i).getValue(path);
                        if (value != null) {
                            columnArray[i] = value;
                        }
                    }
                }
                ArrayColumnView columnView = new ArrayColumnView(columnArray);
                columnView.setColumnId(column.getId());
                return columnView;
            }
        });
    }
}
