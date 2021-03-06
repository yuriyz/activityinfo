package org.activityinfo.ui.client.component.formdesigner.header;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author yuriyz on 7/11/14.
 */
public class HeaderPanel extends Composite {

    private static OurUiBinder uiBinder = GWT
            .create(OurUiBinder.class);
    @UiField
    HTML label;
    @UiField
    FocusPanel focusPanel;
    @UiField
    HTML description;

    interface OurUiBinder extends UiBinder<Widget, HeaderPanel> {
    }

    public HeaderPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public HTML getLabel() {
        return label;
    }

    public HTML getDescription() {
        return description;
    }

    public FocusPanel getFocusPanel() {
        return focusPanel;
    }
}

