package org.activityinfo.ui.app.client.chrome.tree;
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

import org.activityinfo.ui.style.tree.TreeComponent;
import org.activityinfo.ui.style.tree.TreeModel;
import org.activityinfo.ui.vdom.shared.Stylesheet;
import org.activityinfo.ui.vdom.shared.tree.VComponent;
import org.activityinfo.ui.vdom.shared.tree.VTree;

/**
 * @author yuriyz on 9/11/14.
 */
@Stylesheet("NavigationTree.less")
public class NavigationTree<T> extends VComponent {

    private final TreeComponent<T> tree;

    public NavigationTree(TreeModel<T> model) {
        this.tree = new TreeComponent<>(model);
        this.tree.setNodeRenderer(new NavigationNodeRenderer<T>());
    }

    @Override
    protected VTree render() {
        return tree;
    }
}
