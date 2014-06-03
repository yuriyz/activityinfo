package org.activityinfo.core.shared.expr;
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

import org.activityinfo.core.shared.Cuid;

/**
 * @author yuriyz on 6/3/14.
 */
public class Placeholder {

    private final String placeholder;
    private final Cuid fieldId;
    private final boolean isRowLevel;

    public Placeholder(String placeholder) {
        this.placeholder = placeholder;
        this.isRowLevel = !placeholder.contains("_");
        if (isRowLevel) {
            fieldId = new Cuid(placeholder);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public boolean isRowLevel() {
        return isRowLevel;
    }

    public Cuid getFieldId() {
        return fieldId;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
