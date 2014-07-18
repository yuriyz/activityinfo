package org.activityinfo.ui.client.component.formdesigner.design;
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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

/**
 * @author yuriyz on 7/17/14.
 */
public class FieldDesignWidgetFactory {

    private FieldDesignWidgetFactory() {
    }

    public static FieldDesignWidget create(FieldWidgetContainer widgetContainer) {
        FieldType type = widgetContainer.getFormField().getType();
        if (type instanceof QuantityType) {
            return new QuantityFieldDesignWidget(widgetContainer);
        }

        Log.error("Unexpected field type " + type.getTypeClass());
        //throw new UnsupportedOperationException();
        return new FieldDesignWidget() {
            @Override
            public Widget asWidget() {
                return new Label();
            }
        };
    }
}