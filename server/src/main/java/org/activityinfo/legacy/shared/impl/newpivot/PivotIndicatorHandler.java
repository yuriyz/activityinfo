package org.activityinfo.legacy.shared.impl.newpivot;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.impl.newpivot.source.SourceRowFetcher;
import org.activityinfo.legacy.shared.impl.pivot.PivotQueryContext;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Model based pivot sites handler.
 *
 * @author yuriyz on 6/26/14.
 */
public class PivotIndicatorHandler {

    private final PivotQueryContext queryContext;
    private final AsyncCallback<PivotSites.PivotResult> callback;
    private final AsyncCallback<List<Bucket>> forwardCallback;

    public PivotIndicatorHandler(PivotQueryContext queryContext, AsyncCallback<PivotSites.PivotResult> callback) {
        this.queryContext = queryContext;
        this.callback = callback;
        this.forwardCallback = new AsyncCallback<List<Bucket>>() {
            @Override
            public void onFailure(Throwable caught) {
                PivotIndicatorHandler.this.callback.onFailure(caught);
            }

            @Override
            public void onSuccess(List<Bucket> result) {
                // todo : take into account remaining callbacks !
                PivotIndicatorHandler.this.callback.onSuccess(new PivotSites.PivotResult(PivotIndicatorHandler.this.queryContext.getBuckets()));
            }
        };
    }

    public void execute() {
        if (queryContext.getCommand().getValueType() == PivotSites.ValueType.INDICATOR && queryContext.getCommand().getFilter().getRestrictedDimensions().contains(DimensionType.Indicator)) {
            new IndicatorAnalyzer(queryContext).apply(null).then(new Function<IndicatorAnalyzer.Indicators, Object>() {
                @Nullable
                @Override
                public Object apply(@Nullable IndicatorAnalyzer.Indicators input) {
                    new SourceRowFetcher(queryContext, true).apply(input).then(new PureIndicatorsFunction(queryContext)).then(forwardCallback);
                    new SourceRowFetcher(queryContext, false).apply(input).then(new CalculatedIndicatorsFunction(queryContext)).then(forwardCallback);
                    return null;
                }
            });
        } else {
            throw new UnsupportedOperationException();
        }
    }
}