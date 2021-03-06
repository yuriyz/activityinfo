package org.activityinfo.ui.client.component.filter;

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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.CheckCascade;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.legacy.client.monitor.MaskingAsyncMonitor;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UI Component that allows the user to select a list of Indicators
 *
 * @author Alex Bertram
 */
public class IndicatorTreePanel extends ContentPanel {

    private final Dispatcher dispatcher;

    private final TreeStore<ModelData> store;
    private final TreePanel<ModelData> tree;
    private ToolBar toolBar;
    private StoreFilterField filter;
    private boolean multipleSelection;

    /**
     * Keep our own copy of our selection state that is independent of the
     * loading process
     */
    private Set<Integer> selection = Sets.newHashSet();

    public IndicatorTreePanel(Dispatcher dispatcher, final boolean multipleSelection) {
        this.dispatcher = dispatcher;

        this.setHeadingText(I18N.CONSTANTS.indicators());
        this.setIcon(IconImageBundle.ICONS.indicator());
        this.setLayout(new FitLayout());
        this.setScrollMode(Style.Scroll.NONE);

        store = new TreeStore<ModelData>(new Loader());

        setStoreKeyProvider();

        tree = new TreePanel<ModelData>(store);

        tree.setCheckNodes(TreePanel.CheckNodes.BOTH);
        tree.setCheckStyle(CheckCascade.CHILDREN);

        tree.getStyle().setNodeCloseIcon(null);
        tree.getStyle().setNodeOpenIcon(null);

        tree.setLabelProvider(new NodeLabelProvider());

        tree.setCheckable(true);
        tree.expandAll();
        tree.setStateId("indicatorPanel");
        tree.setStateful(true);
        tree.setAutoSelect(true);
        tree.addListener(Events.BrowserEvent, new Listener<TreePanelEvent<ModelData>>() {

            @Override
            public void handleEvent(TreePanelEvent<ModelData> be) {
                if (be.getEventTypeInt() == Event.ONKEYPRESS) {
                    if (!toolBar.isVisible()) {
                        toolBar.setVisible(true);
                    }
                    filter.focus();
                }
            }
        });

        add(tree);
        createFilterBar();

        tree.getStore().getLoader().addLoadListener(new LoadListener() {

            @Override
            public void loaderLoad(LoadEvent le) {
                Log.info("Loaded " + le);
            }
        });

        tree.getStore().addStoreListener(new StoreListener<ModelData>() {

            @Override
            public void storeDataChanged(StoreEvent<ModelData> se) {
                // apply our internal state to the newly loaded list
                applySelection();
            }
        });

        addCheckChangedListener(new Listener<TreePanelEvent>() {

            @Override
            public void handleEvent(TreePanelEvent be) {
                if (be.getItem() instanceof IndicatorDTO) {
                    IndicatorDTO indicator = (IndicatorDTO) be.getItem();
                    if (be.isChecked()) {
                        selection.add(indicator.getId());
                    } else {
                        selection.remove(indicator.getId());
                    }
                } else if (be.isChecked()) {
                    tree.getStore().getLoader().loadChildren(be.getItem());
                }
            }
        });
    }

    private void setStoreKeyProvider() {
        store.setKeyProvider(new ModelKeyProvider<ModelData>() {

            @Override
            public String getKey(ModelData model) {
                new ArrayList<String>();
                if (model instanceof ProvidesKey) {
                    return ((ProvidesKey) model).getKey();
                } else if (model == null) {
                    throw new IllegalStateException(
                            "Did not expect model to be null: assigning keys in IndicatorTreePanel");
                }
                throw new IllegalStateException("Unknown type: expected activity, userdb, indicator or indicatorgroup");
            }
        });
    }

    @Override
    public void setHeadingText(String heading) {
        super.setHeadingText(heading);
    }

    private void createFilterBar() {
        toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(I18N.CONSTANTS.search()));
        filter = new FilterField();
        filter.addListener(Events.Blur, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                if (filter.getRawValue() == null || filter.getRawValue().length() == 0) {
                    toolBar.setVisible(false);
                }
            }
        });
        toolBar.add(filter);
        toolBar.setVisible(false);
        filter.bind(store);
        setTopComponent(toolBar);
    }

    private final class NodeLabelProvider implements ModelStringProvider<ModelData> {
        @Override
        public String getStringValue(ModelData model, String property) {
            String name = model.get("name");
            if (model instanceof IndicatorDTO) {
                return name;
            } else {
                if (name == null) {
                    name = "noname";
                }
                return "<b>" + name + "</b>";
            }
        }
    }

    private class Proxy implements DataProxy<List<ModelData>> {
        private SchemaDTO schema;

        @Override
        public void load(DataReader<List<ModelData>> listDataReader,
                         Object parent,
                         final AsyncCallback<List<ModelData>> callback) {

            if (parent == null) {
                dispatcher.execute(new GetSchema(),
                        new MaskingAsyncMonitor(IndicatorTreePanel.this, I18N.CONSTANTS.loading()),
                        new SuccessCallback<SchemaDTO>() {

                            @Override
                            public void onSuccess(SchemaDTO result) {
                                schema = result;
                                callback.onSuccess(new ArrayList<ModelData>(schema.getDatabases()));
                            }
                        });
            } else if (parent instanceof UserDatabaseDTO) {

                callback.onSuccess(new ArrayList<ModelData>(createDatabaseChildren((UserDatabaseDTO) parent)));

            } else if (parent instanceof ActivityCategory) {

                callback.onSuccess(new ArrayList<ModelData>(((ActivityCategory) parent).getActivities()));

            } else if (parent instanceof ActivityDTO) {

                loadActivityChildren((ActivityDTO) parent).then(callback);

            } else if (parent instanceof IndicatorGroup) {

                callback.onSuccess(createIndicatorList((IndicatorGroup) parent));
            }
        }
    }

    private List<ModelData> createDatabaseChildren(UserDatabaseDTO databaseDTO) {

        List<ModelData> children = new ArrayList<ModelData>();

        Set<ActivityCategory> categories = new HashSet<ActivityCategory>();

        for (ActivityDTO activityDTO : databaseDTO.getActivities()) {
            if (activityDTO.hasCategory()) {
                ActivityCategory activityCategory = new ActivityCategory(databaseDTO.getId(), activityDTO.getCategory());
                categories.add(activityCategory);
                if (!children.contains(activityCategory)) {
                    children.add(activityCategory);
                }
            } else {
                children.add(activityDTO);
            }
        }

        // fill category with activities
        for (ActivityCategory category : categories) {
            for (ActivityDTO activityDTO : databaseDTO.getActivities()) {
                if (category.getName().equals(activityDTO.getCategory())) {
                    category.addActivity(activityDTO);
                }
            }
        }

        return children;
    }

    private Promise<List<ModelData>> loadActivityChildren(ActivityDTO activity) {
        return dispatcher.execute(new GetActivityForm(activity.getId())).then(new Function<ActivityFormDTO, List<ModelData>>() {
            @Nullable
            @Override
            public List<ModelData> apply(@Nullable ActivityFormDTO form) {
                List<IndicatorGroup> groupIndicators = form.groupIndicators();
                List<ModelData> children = new ArrayList<ModelData>();
                for (IndicatorGroup group : groupIndicators) {
                    if (group.getName() == null) {
                        for (IndicatorDTO indicator : group.getIndicators()) {
                            children.add(indicator);
                        }
                    } else {
                        children.add(group);
                    }
                }
                return children;
            }
        });
    }

    private List<ModelData> createIndicatorList(IndicatorGroup group) {
        ArrayList<ModelData> list = new ArrayList<ModelData>();
        for (IndicatorDTO indicator : group.getIndicators()) {
            list.add(indicator);
        }

        return list;
    }

    /**
     * @return the list of selected indicators
     */
    public List<IndicatorDTO> getSelection() {
        List<IndicatorDTO> list = new ArrayList<IndicatorDTO>();
        for (ModelData model : tree.getCheckedSelection()) {
            if (model instanceof IndicatorDTO) {
                list.add((IndicatorDTO) model);
            }
        }
        return list;
    }

    public void select(int indicatorId, boolean select) {

        if (select) {
            if (!multipleSelection) {
                selection.clear();
            }
            selection.add(indicatorId);
        } else {
            selection.remove(indicatorId);
        }

        // apply directly if the indicators are loaded
        for (ModelData model : tree.getStore().getAllItems()) {
            if (model instanceof IndicatorDTO && ((IndicatorDTO) model).getId() == indicatorId) {
                setChecked((IndicatorDTO) model, select);
            }
        }
    }

    public void setSelection(Iterable<Integer> indicatorIds) {
        selection = Sets.newHashSet(indicatorIds);
        applySelection();
    }

    public void addCheckChangedListener(Listener<TreePanelEvent> listener) {
        tree.addListener(Events.CheckChange, listener);
    }

    public void addBeforeCheckedListener(Listener<TreePanelEvent> listener) {
        tree.addListener(Events.BeforeCheckChange, listener);
    }

    /**
     * @return the list of the ids of selected indicators
     */
    public List<Integer> getSelectedIds() {
        return Lists.newArrayList(selection);
    }

    public void setMultipleSelection(boolean multipleSelection) {
        this.multipleSelection = multipleSelection;
        if (multipleSelection) {
            tree.setCheckStyle(TreePanel.CheckCascade.CHILDREN);
        } else {
            tree.setCheckStyle(TreePanel.CheckCascade.NONE);
        }
    }

    private class Loader extends BaseTreeLoader<ModelData> {
        public Loader() {
            super(new Proxy());
        }

        @Override
        public boolean hasChildren(ModelData parent) {
            return !(parent instanceof IndicatorDTO);
        }
    }

    private static class FilterField extends StoreFilterField {
        @Override
        protected boolean doSelect(Store store, ModelData parent, ModelData record, String property, String filter) {

            String[] keywords = filter.toLowerCase().split("\\s+");
            String name = ((String) record.get("name")).toLowerCase();
            for (String keyword : keywords) {
                if (name.indexOf(keyword) == -1) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isMultipleSelection() {
        return multipleSelection;
    }

    public void clearSelection() {
        for (IndicatorDTO indicator : getSelection()) {
            tree.getSelectionModel().deselect(indicator);
            tree.setChecked(indicator, false);
        }
    }

    public void setChecked(IndicatorDTO indicator, boolean b) {
        tree.setChecked(indicator, b);
    }

    public void setLeafCheckableOnly() {
        tree.setCheckNodes(TreePanel.CheckNodes.LEAF);
    }

    private void applySelection() {
        for (ModelData model : tree.getStore().getAllItems()) {
            if (model instanceof IndicatorDTO) {
                IndicatorDTO indicator = (IndicatorDTO) model;
                boolean selected = selection.contains(indicator.getId());
                setChecked(indicator, selected);
            }
        }
    }
}
