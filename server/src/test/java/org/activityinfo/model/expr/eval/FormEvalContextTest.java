package org.activityinfo.model.expr.eval;

import org.activityinfo.model.expr.diagnostic.CircularReferenceException;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.junit.Test;

public class FormEvalContextTest {

    @Test(expected = CircularReferenceException.class)
    public void circularRefs() {

        FormField a = new FormField(Resources.generateId());
        a.setCode("A");
        a.setType(new CalculatedFieldType("B+1"));
        a.setLabel("A");

        FormField b = new FormField(Resources.generateId());
        b.setCode("B");
        b.setType(new CalculatedFieldType("A/50"));
        b.setLabel("B");

        FormClass formClass = new FormClass(Resources.generateId());
        formClass.addElement(a);
        formClass.addElement(b);

        FormEvalContext context = new FormEvalContext(formClass);
        context.setInstance(new FormInstance(Resources.generateId(), formClass.getId()));

        System.out.println(context.getFieldValue(a.getId()));

    }

}