package org.activityinfo.ui.component.formdesigner.palette;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;

import java.util.List;

public class CheckboxTemplate implements FieldTemplate {
    @Override
    public String getLabel() {
        return "Checkboxes";
    }

    @Override
    public FormField createField() {
        List<EnumValue> values = Lists.newArrayList();
        values.add(new EnumValue(Resources.generateId(), "Option 1"));
        values.add(new EnumValue(Resources.generateId(), "Option 2"));
        FormField field = new FormField(Resources.generateId());
        field.setLabel("Which options apply?");
        field.setType(new EnumType(Cardinality.MULTIPLE, values));

        return field;
    }
}