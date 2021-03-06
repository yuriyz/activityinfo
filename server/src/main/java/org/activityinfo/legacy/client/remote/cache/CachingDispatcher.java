package org.activityinfo.legacy.client.remote.cache;

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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.remote.AbstractDispatcher;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;

public class CachingDispatcher extends AbstractDispatcher {

    private final CacheManager cacheManager;
    private final Dispatcher dispatcher;

    public CachingDispatcher(CacheManager proxyManager, Dispatcher dispatcher) {
        super();
        this.cacheManager = proxyManager;
        this.dispatcher = dispatcher;
    }

    @Override
    public <T extends CommandResult> void execute(final Command<T> command, final AsyncCallback<T> callback) {

        cacheManager.notifyListenersBefore(command);

        CacheResult proxyResult = cacheManager.execute(command);
        if (proxyResult.isCouldExecute()) {
            callback.onSuccess((T) proxyResult.getResult());
        } else {
            dispatcher.execute(command, new AsyncCallback<T>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(T result) {
                    cacheManager.notifyListenersOfSuccess(command, result);
                    callback.onSuccess(result);
                }
            });
        }
    }
}
