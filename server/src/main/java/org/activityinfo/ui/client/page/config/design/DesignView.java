package org.activityinfo.ui.client.page.config.design;

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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDragSource;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.treegrid.CellTreeGridSelectionModel;
import com.extjs.gxt.ui.client.widget.treegrid.EditorTreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.grid.AbstractEditorTreeGridView;
import org.activityinfo.ui.client.page.common.grid.ImprovedCellTreeGridSelectionModel;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Bertram
 */
public class DesignView extends AbstractEditorTreeGridView<ModelData, DesignPresenter> implements DesignPresenter.View {

    private MenuItem newAttributeGroup;
    private MenuItem newAttribute;
    private MenuItem newIndicator;
    private Menu newMenu;

    private final class DragDropListener extends DNDListener {
        private final TreeStore treeStore;

        private DragDropListener(TreeStore treeStore) {
            this.treeStore = treeStore;
        }

        @Override
        public void dragMove(DNDEvent e) {
            List<TreeModel> sourceData = e.getData();
            ModelData source = sourceData.get(0).get("model");
            TreeGrid.TreeNode target = tree.findNode(e.getTarget());

            if (treeStore.getParent(target.getModel()) != treeStore.getParent(source)) {

                e.setCancelled(true);
                e.getStatus().setStatus(false);
            }
        }

        @Override
        public void dragDrop(DNDEvent e) {
            List<TreeModel> sourceData = e.getData();
            ModelData source = sourceData.get(0).get("model");
            presenter.onNodeDropped(source);
        }
    }

    private final Dispatcher service;

    private EditorTreeGrid<ModelData> tree;
    private ContentPanel formContainer;

    private UserDatabaseDTO db;

    @Inject
    public DesignView(Dispatcher service) {
        this.service = service;
    }

    @Override
    public void init(DesignPresenter presenter, UserDatabaseDTO db, TreeStore store) {

        this.db = db;

        setLayout(new BorderLayout());
        setHeadingText(I18N.CONSTANTS.design() + " - " + db.getName());
        setIcon(IconImageBundle.ICONS.design());

        super.init(presenter, store);

        createFormContainer();
    }

    @Override
    protected Grid<ModelData> createGridAndAddToContainer(Store store) {

        final TreeStore treeStore = (TreeStore) store;

        tree = new EditorTreeGrid<ModelData>(treeStore, createColumnModel());
        tree.setSelectionModel(new ImprovedCellTreeGridSelectionModel<ModelData>());
        tree.setClicksToEdit(EditorGrid.ClicksToEdit.TWO);
        tree.setAutoExpandColumn("name");
        tree.setHideHeaders(true);
        tree.setLoadMask(true);

        tree.setIconProvider(new ModelIconProvider<ModelData>() {
            @Override
            public AbstractImagePrototype getIcon(ModelData model) {
                if (model instanceof IsActivityDTO) {
                    return IconImageBundle.ICONS.activity();
                } else if (model instanceof Folder) {
                    return GXT.IMAGES.tree_folder_closed();
                } else if (model instanceof AttributeGroupDTO) {
                    return IconImageBundle.ICONS.attributeGroup();
                } else if (model instanceof AttributeDTO) {
                    return IconImageBundle.ICONS.attribute();
                } else if (model instanceof IndicatorDTO) {
                    return IconImageBundle.ICONS.indicator();
                } else if (model instanceof LocationTypeDTO) {
                    return IconImageBundle.ICONS.marker();
                } else {
                    return null;
                }
            }
        });
        tree.addListener(Events.CellClick, new Listener<GridEvent>() {
            @Override
            public void handleEvent(GridEvent ge) {
                showForm(tree.getStore().getAt(ge.getRowIndex()));
            }
        });

        add(tree, new BorderLayoutData(Style.LayoutRegion.CENTER));

        TreeGridDragSource source = new TreeGridDragSource(tree);
        source.addDNDListener(new DNDListener() {
            @Override
            public void dragStart(DNDEvent e) {

                ModelData sel = ((CellTreeGridSelectionModel) tree.getSelectionModel()).getSelectCell().model;
                if (!db.isDesignAllowed() || sel == null || sel instanceof Folder) {
                    e.setCancelled(true);
                    e.getStatus().setStatus(false);
                    return;
                }
                super.dragStart(e);
            }
        });

        TreeGridDropTarget target = new TreeGridDropTarget(tree);
        target.setAllowSelfAsSource(true);
        target.setFeedback(DND.Feedback.BOTH);
        target.setAutoExpand(false);
        target.addDNDListener(new DragDropListener(treeStore));
        return tree;
    }

    @Override
    protected void initToolBar() {

        toolBar.addSaveSplitButton();

        SelectionListener<MenuEvent> listener = new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {

                presenter.onNew(ce.getItem().getItemId());
            }
        };

        newMenu = new Menu();
        initNewMenu(newMenu, listener);

        Button newButtonMenu = new Button(I18N.CONSTANTS.newText(), IconImageBundle.ICONS.add());
        newButtonMenu.setMenu(newMenu);
        newButtonMenu.setEnabled(db.isDesignAllowed());
        toolBar.add(newButtonMenu);

        toolBar.add(new SeparatorMenuItem());

        toolBar.addButton(UIActions.EDIT, I18N.CONSTANTS.openFormDesigner(), IconImageBundle.ICONS.edit());
        toolBar.addButton(UIActions.OPEN_TABLE, I18N.CONSTANTS.openTable(), IconImageBundle.ICONS.table());
        toolBar.addDeleteButton();

        toolBar.add(new SeparatorToolItem());

        toolBar.addImportButton();
        toolBar.addExcelExportButton();
    }

    protected void initNewMenu(Menu menu, SelectionListener<MenuEvent> listener) {

        MenuItem newActivity = new MenuItem(I18N.CONSTANTS.newActivity(), IconImageBundle.ICONS.activity(), listener);
        newActivity.setItemId("Activity");
        menu.add(newActivity);

        MenuItem newLocationType = new MenuItem(
                I18N.CONSTANTS.newLocationType(),
                IconImageBundle.ICONS.marker(), listener);
        newLocationType.setItemId("LocationType");
        menu.add(newLocationType);

        newAttributeGroup = newMenuItem("AttributeGroup",
                I18N.CONSTANTS.newAttributeGroup(),
                IconImageBundle.ICONS.attribute(),
                listener);
        menu.add(newAttributeGroup);

        newAttribute = newMenuItem("Attribute",
                I18N.CONSTANTS.newAttribute(),
                IconImageBundle.ICONS.attribute(),
                listener);
        menu.add(newAttribute);

        newIndicator = new MenuItem(I18N.CONSTANTS.newIndicator(),
                IconImageBundle.ICONS.indicator(),
                listener);
        newIndicator.setItemId("Indicator");
        menu.add(newIndicator);

    }

    public Menu getNewMenu() {
        return newMenu;
    }

    public MenuItem getNewAttributeGroup() {
        return newAttributeGroup;
    }

    public MenuItem getNewAttribute() {
        return newAttribute;
    }

    public MenuItem getNewIndicator() {
        return newIndicator;
    }

    private MenuItem newMenuItem(String itemId,
                                 String label,
                                 AbstractImagePrototype icon,
                                 SelectionListener<MenuEvent> listener) {
        final MenuItem newAttribute = new MenuItem(label, icon, listener);
        newAttribute.setItemId(itemId);
        return newAttribute;
    }

    private void createFormContainer() {
        formContainer = new ContentPanel();
        formContainer.setHeaderVisible(false);
        formContainer.setBorders(false);
        formContainer.setFrame(false);

        BorderLayoutData layout = new BorderLayoutData(Style.LayoutRegion.EAST);
        layout.setSplit(true);
        layout.setCollapsible(true);
        layout.setSize(375);
        layout.setMargins(new Margins(0, 0, 0, 5));

        add(formContainer, layout);
    }

    private ColumnModel createColumnModel() {

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        TextField<String> nameField = new TextField<String>();
        nameField.setAllowBlank(false);

        ColumnConfig nameColumn = new ColumnConfig("name", I18N.CONSTANTS.name(), 150);
        nameColumn.setEditor(new CellEditor(nameField));
        nameColumn.setRenderer(new TreeGridCellRenderer());

        columns.add(nameColumn);

        return new ColumnModel(columns);
    }

    protected Class formClassForSelection(ModelData sel) {

        if (sel instanceof IsActivityDTO) {
            return ActivityForm.class;
        } else if (sel instanceof AttributeGroupDTO) {
            return AttributeGroupForm.class;
        } else if (sel instanceof IndicatorDTO) {
            return IndicatorForm.class;
        } else if (sel instanceof AttributeDTO) {
            return AttributeForm.class;
        } else if (sel instanceof LocationTypeDTO) {
            return LocationTypeForm.class;
        }

        return null;

    }

    protected AbstractDesignForm createForm(ModelData sel) {
        if (sel instanceof IsActivityDTO) {
            return new ActivityForm(service, db);
        } else if (sel instanceof AttributeGroupDTO) {
            return new AttributeGroupForm();
        } else if (sel instanceof AttributeDTO) {
            return new AttributeForm();
        } else if (sel instanceof IndicatorDTO) {
            return new IndicatorForm();
        } else if (sel instanceof LocationTypeDTO) {
            return new LocationTypeForm();
        }

        return null;
    }

    public void showForm(ModelData model) {

        // do we have the right form?
        Class formClass = formClassForSelection(model);

        AbstractDesignForm currentForm = null;
        if (formContainer.getItemCount() != 0) {
            currentForm = (AbstractDesignForm) formContainer.getItem(0);
        }

        if (formClass == null) {
            if (currentForm != null) {
                currentForm.getBinding().unbind();
                formContainer.removeAll();
            }
            return;
        } else {

            if (currentForm == null || (!formClass.equals(currentForm.getClass()))) {

                if (currentForm != null) {
                    formContainer.removeAll();
                    currentForm.getBinding().unbind();
                }

                currentForm = createForm(model);
                currentForm.setReadOnly(!db.isDesignAllowed());
                currentForm.setHeaderVisible(false);
                currentForm.setBorders(false);
                currentForm.setFrame(false);
                currentForm.getBinding().setStore(tree.getStore());
                formContainer.add(currentForm);
                formContainer.layout();
            }
        }
        currentForm.getBinding().bind(model);
    }

    @Override
    public FormDialogTether showNewForm(EntityDTO entity, FormDialogCallback callback) {

        AbstractDesignForm form = createForm(entity);
        form.getBinding().bind(entity);
        form.getBinding().setStore(tree.getStore());

        for (FieldBinding field : form.getBinding().getBindings()) {
            field.getField().clearInvalid();
        }

        FormDialogImpl dlg = new FormDialogImpl(form);
        dlg.setWidth(form.getPreferredDialogWidth());
        dlg.setHeight(form.getPreferredDialogHeight());
        dlg.setScrollMode(Style.Scroll.AUTOY);

        if (entity instanceof IsActivityDTO) {
            dlg.setHeadingText(I18N.CONSTANTS.newActivity());
        } else if (entity instanceof AttributeGroupDTO) {
            dlg.setHeadingText(I18N.CONSTANTS.newAttributeGroup());
        } else if (entity instanceof AttributeDTO) {
            dlg.setHeadingText(I18N.CONSTANTS.newAttribute());
        } else if (entity instanceof IndicatorDTO) {
            dlg.setHeadingText(I18N.CONSTANTS.newIndicator());
        } else if (entity instanceof LocationTypeDTO) {
            dlg.setHeadingText(I18N.CONSTANTS.newLocationType());
        }

        dlg.show(callback);

        return dlg;
    }
}
