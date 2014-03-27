package org.activityinfo.ui.client.component.table;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.ui.client.component.table.dialog.VisibleColumnsDialog;
import org.activityinfo.ui.client.widget.ButtonWithSize;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays the this classes' FormInstances in a table format
 */
public class InstanceTableView implements IsWidget, RequiresResize {

    private static final int DEFAULT_MAX_COLUMN_COUNT = 5;
    private static final Logger LOGGER = Logger.getLogger(InstanceTableView.class.getName());

    private final ResourceLocator resourceLocator;
    private final HTMLPanel panel;
    private List<FieldColumn> columns;
    private List<FieldColumn> selectedColumns;

    @UiField
    DivElement emRuler;
    @UiField
    Element columnAlert;
    @UiField(provided = true)
    InstanceTable table;
    @UiField
    ButtonWithSize addButton;
    @UiField
    ButtonWithSize removeButton;
    @UiField
    ButtonWithSize blukEditButton;
    @UiField
    ButtonWithSize editButton;

    interface InstanceTableViewUiBinder extends UiBinder<HTMLPanel, InstanceTableView> {
    }

    private static InstanceTableViewUiBinder ourUiBinder = GWT.create(InstanceTableViewUiBinder.class);

    public InstanceTableView(ResourceLocator resourceLocator) {
        InstanceTableStyle.INSTANCE.ensureInjected();
        this.resourceLocator = resourceLocator;
        this.table = new InstanceTable(resourceLocator);
        this.panel = ourUiBinder.createAndBindUi(this);

        initButtons();
    }

    private void initButtons() {
        setRemoveButtonState();
        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                setRemoveButtonState();
            }
        });
    }

    private void setRemoveButtonState() {
        removeButton.setEnabled(!table.getSelectionModel().getSelectedSet().isEmpty());
    }

    public void setCriteria(Criteria criteria) {
        table.setCriteria(criteria);
    }

    public void setColumns(final List<FieldColumn> columns) {
        this.columns = columns;
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                calculateSelectedColumns();
            }
        });
    }

    public void setSelectedColumns(final List<FieldColumn> selectedColumns) {
        this.selectedColumns = selectedColumns;
        table.setColumns(selectedColumns);
    }

    private void calculateSelectedColumns() {
        if (columns.size() <= getMaxNumberOfColumns()) {
            setSelectedColumns(Lists.newArrayList(columns));
            columnAlert.getStyle().setDisplay(Style.Display.BLOCK);
        } else {
            setSelectedColumns(Lists.newArrayList(columns.subList(0, getMaxNumberOfColumns())));
            columnAlert.getStyle().clearDisplay();
        }
    }

    public int getMaxNumberOfColumns() {
        double emSizeInPixels = ((double) emRuler.getOffsetWidth()) / 100d;

        LOGGER.log(Level.FINE, "emSizeInPixels = " + emSizeInPixels);

        double columnWidthInPixels = InstanceTable.COLUMN_WIDTH * emSizeInPixels;

        int columnLimit = (int) Math.floor(panel.getElement().getClientWidth() / columnWidthInPixels);
        LOGGER.log(Level.FINE, "columnLimit = " + columnLimit);
        if (columnLimit <= 0) { // fallback : yuriyz: check calculations above
            columnLimit = DEFAULT_MAX_COLUMN_COUNT;
        }
        return columnLimit;
    }

    public InstanceTable getTable() {
        return table;
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    public void onResize() {

    }

    @UiHandler("visibleColumns")
    public void onConfigure(ClickEvent event) {
        final VisibleColumnsDialog visibleColumnsDialog = new VisibleColumnsDialog(this);
        visibleColumnsDialog.show();
    }

    @UiHandler("addButton")
    public void onAdd(ClickEvent event) {

    }

    @UiHandler("removeButton")
    public void onRemove(ClickEvent event) {
        //todo
    }

    public List<FieldColumn> getColumns() {
        if (columns == null) {
            columns = Lists.newArrayList();
        }
        return columns;
    }

    public List<FieldColumn> getSelectedColumns() {
        if (selectedColumns == null) {
            selectedColumns = Lists.newArrayList();
        }
        return selectedColumns;
    }

    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }
}