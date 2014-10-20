package org.activityinfo.service.tables;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.activityinfo.model.table.ColumnType;
import org.activityinfo.model.table.ColumnView;
import org.activityinfo.model.table.views.DoubleArrayColumnView;
import org.activityinfo.model.table.views.StringArrayColumnView;

import java.util.List;

/**
 * Supplies a Column that is combined from several source columns.
 *
 */
class ColumnCombiner implements Supplier<ColumnView> {

    private List<Supplier<ColumnView>> sources;

    private ColumnView result;

    ColumnCombiner(List<Supplier<ColumnView>> sources) {
        Preconditions.checkArgument(sources.size() > 1, "source.size() > 1");
        this.sources = sources;
    }

    @Override
    public ColumnView get() {
        if(result == null) {
            result = combine();
        }
        return result;
    }

    private ColumnView combine() {
        ColumnView[] cols = new ColumnView[sources.size()];
        for(int j=0;j<cols.length;++j) {
            cols[j] = sources.get(j).get();
        }
        ColumnType columnType = sources.get(0).get().getType();

        switch(columnType) {

            case STRING:
                return combineString(cols);
            case NUMBER:
                return combineDouble(cols);
            case BOOLEAN:
                break;
            case DATE:
                break;
        }
        throw new UnsupportedOperationException();
    }

    private ColumnView combineString(ColumnView[] cols) {
        int numRows = cols[0].numRows();
        int numCols = cols.length;

        String[] values = new String[numRows];

        for(int i=0;i!=numRows;++i) {
            for(int j=0;j!=numCols;++j) {
                String value = cols[j].getString(j);
                if(value != null) {
                    values[i] = value;
                    break;
                }
            }
        }

        return new StringArrayColumnView(values);
    }

    private ColumnView combineDouble(ColumnView[] cols) {
        int numRows = cols[0].numRows();
        int numCols = cols.length;

        double[] values = new double[numRows];

        for(int i=0;i!=numRows;++i) {
            values[i] = Double.NaN;
            for(int j=0;j!=numCols;++j) {
                double value = cols[j].getDouble(j);
                if(!Double.isNaN(value)) {
                    values[i] = value;
                    break;
                }
            }
        }
        return new DoubleArrayColumnView(values);
    }


}