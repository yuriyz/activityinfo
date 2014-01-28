package org.activityinfo.ui.full.client.widget.form;
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

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import org.activityinfo.api2.shared.form.FormField;
import org.activityinfo.api2.shared.form.FormFieldType;
import org.activityinfo.ui.full.client.widget.coord.CoordinateField;
import org.activityinfo.ui.full.client.widget.coord.GwtCoordinateField;

/**
 * @author yuriyz on 1/28/14.
 */
public class WidgetInfoUtil {

    private WidgetInfoUtil() {
    }

    public static WidgetInfo create(FormField field) {
        final FormFieldType fieldType = field.getType();
        if (fieldType != null) {
            switch (fieldType) {
                case QUANTITY:
                    return new SimpleWidgetInfo(new DoubleBox(), field);
                case FREE_TEXT:
                    return new SimpleWidgetInfo(new TextBox(), field);
                case LOCAL_DATE:
                    return new SimpleWidgetInfo(new DatePicker(), field);
                case GEOGRAPHIC_POINT:
                    final GwtCoordinateField latitude = new GwtCoordinateField(CoordinateField.Axis.LATITUDE);
                    final GwtCoordinateField longitude = new GwtCoordinateField(CoordinateField.Axis.LONGITUDE);
                    return new GeographicWidgetInfo(latitude, longitude, field);
                case REFERENCE:
                    return new SimpleWidgetInfo(null, field);
            }
        }
        return null;
    }
}