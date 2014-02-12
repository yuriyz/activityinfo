package org.activityinfo.ui.full.client.filter;

import java.util.ArrayList;
import java.util.List;

import org.activityinfo.api.client.Dispatcher;
import org.activityinfo.api.shared.command.Filter;
import org.activityinfo.api.shared.command.GetLocations;
import org.activityinfo.api.shared.command.GetLocations.GetLocationsResult;
import org.activityinfo.api.shared.model.LocationDTO;
import org.activityinfo.api.shared.model.LocationTypeDTO;
import org.activityinfo.reports.shared.model.DimensionType;
import org.activityinfo.ui.full.client.filter.FilterToolBar.ApplyFilterEvent;
import org.activityinfo.ui.full.client.filter.FilterToolBar.ApplyFilterHandler;
import org.activityinfo.ui.full.client.filter.FilterToolBar.RemoveFilterEvent;
import org.activityinfo.ui.full.client.filter.FilterToolBar.RemoveFilterHandler;
import org.activityinfo.ui.full.client.i18n.I18N;
import org.activityinfo.ui.full.client.icon.IconImageBundle;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class LocationFilterPanel extends ContentPanel implements FilterPanel {

    private Dispatcher service;
    private FilterToolBar filterToolBar;
    private Filter baseFilter = new Filter();
    private Filter value = new Filter();

    private ListStore<LocationDTO> store;
    private CheckBoxListView<LocationDTO> listView;
    
    @Inject
    public LocationFilterPanel(Dispatcher service) {
        this.service = service;
        initializeComponent();

        createFilterToolBar();
        createList();
    }
    
    private void initializeComponent() {
        setHeadingText(I18N.CONSTANTS.filterByLocation());
        setIcon(IconImageBundle.ICONS.filter());

        setLayout(new FitLayout());
        setScrollMode(Style.Scroll.NONE);
        setHeadingText(I18N.CONSTANTS.filterByLocation());
        setIcon(IconImageBundle.ICONS.filter());
    }
    
    private void createFilterToolBar() {
        filterToolBar = new FilterToolBar();
        filterToolBar.addApplyFilterHandler(new ApplyFilterHandler() {

            @Override
            public void onApplyFilter(ApplyFilterEvent deleteEvent) {
                applyFilter();
            }
        });
        filterToolBar.addRemoveFilterHandler(new RemoveFilterHandler() {

            @Override
            public void onRemoveFilter(RemoveFilterEvent deleteEvent) {
                clearFilter();
                ValueChangeEvent.fire(LocationFilterPanel.this, value);
            }
        });
        setTopComponent(filterToolBar);
    }
    
    protected void applyFilter() {
        value = new Filter();
        if (isRendered()) {
            List<Integer> selectedIds = getSelectedIds();
            if (selectedIds.size() > 0) {
                value.addRestriction(DimensionType.Partner, getSelectedIds());
            }
        }

        ValueChangeEvent.fire(this, value);
        filterToolBar.setApplyFilterEnabled(false);
        filterToolBar.setRemoveFilterEnabled(true);
    }
    
    private List<Integer> getSelectedIds() {
        List<Integer> list = new ArrayList<Integer>();

        for (LocationDTO model : listView.getChecked()) {
            list.add(model.getId());
        }
        return list;
    }
    
    private void createList() {
        store = new ListStore<LocationDTO>();
        listView = new CheckBoxListView<LocationDTO>();
        listView.setStore(store);
        listView.setDisplayProperty("name");
        listView.addListener(Events.Select,
                new Listener<ListViewEvent<LocationTypeDTO>>() {

                    @Override
                    public void handleEvent(ListViewEvent<LocationTypeDTO> be) {
                        filterToolBar.setApplyFilterEnabled(true);
                    }
                });
        add(listView);
    }
    
    protected void clearFilter() {
        for (LocationDTO location : listView.getStore().getModels()) {
            listView.setChecked(location, false);
        }
        value = new Filter();
        filterToolBar.setApplyFilterEnabled(false);
        filterToolBar.setRemoveFilterEnabled(false);
    }
    
    @Override
    public Filter getValue() {
        return value;
    }

    @Override
    public void setValue(Filter arg0) {
        setValue(value, false);
    }

    @Override
    public void setValue(Filter arg0, boolean fireEvents) {
        this.value = new Filter();
        this.value.addRestriction(DimensionType.Location,
                value.getRestrictions(DimensionType.Location));
        applyInternalValue();
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }        
    }
    
    private void applyInternalValue() {
        for (LocationDTO model : listView.getStore().getModels()) {
            listView.setChecked(
                    model,
                    value.getRestrictions(DimensionType.Location).contains(
                            model.getId()));
        }
        filterToolBar.setApplyFilterEnabled(false);
        filterToolBar.setRemoveFilterEnabled(value.isRestricted(DimensionType.Location));
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Filter> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void applyBaseFilter(Filter rawFilter) {
        final Filter filter = new Filter(rawFilter);
        filter.clearRestrictions(DimensionType.Location);

        if (baseFilter == null || !baseFilter.equals(filter)) {
            service.execute(new GetLocations(filter), new AsyncCallback<GetLocationsResult>() {
                
                @Override
                public void onFailure(Throwable arg0) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onSuccess(GetLocationsResult result) {
                    List<Integer> ids = getSelectedIds();
                    store.removeAll();
                    store.add(result.getLocations());
                    applyInternalValue();

                    for (LocationDTO partner : store.getModels()) {
                        if (ids.contains(partner.getId())) {
                            listView.setChecked(partner, true);
                        }
                    }

                    baseFilter = filter;                    
                }
            
            });
        }
    }
}
