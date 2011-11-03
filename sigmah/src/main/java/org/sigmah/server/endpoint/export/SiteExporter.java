/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.server.endpoint.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.sigmah.server.command.DispatcherSync;
import org.sigmah.shared.command.Filter;
import org.sigmah.shared.command.GetSites;
import org.sigmah.shared.dto.ActivityDTO;
import org.sigmah.shared.dto.AdminEntityDTO;
import org.sigmah.shared.dto.AdminLevelDTO;
import org.sigmah.shared.dto.AttributeDTO;
import org.sigmah.shared.dto.AttributeGroupDTO;
import org.sigmah.shared.dto.IndicatorDTO;
import org.sigmah.shared.dto.IndicatorGroup;
import org.sigmah.shared.dto.SiteDTO;
import org.sigmah.shared.report.model.DimensionType;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.SortInfo;

/**
 * @author Alex Bertram
 */
public class SiteExporter {

    public final DispatcherSync dispatcher;

    public final HSSFWorkbook book;
    public final CreationHelper creationHelper;

    public HashMap<String, Integer> sheetNames;

    private CellStyle dateStyle;
    private CellStyle coordStyle;
    private CellStyle indicatorValueStyle;

    private CellStyle headerStyle;
    private CellStyle headerStyleCenter;
    private CellStyle headerStyleRight;

    private CellStyle attribHeaderStyle;
    private CellStyle indicatorHeaderStyle;

    private CellStyle attribValueStyle;

//    private HSSFConditionalFormattingRule attribFalseRule;
//    private HSSFConditionalFormattingRule attribTrueRule;


    private List<Integer> indicators;
    private List<Integer> attributes;
    private List<Integer> levels;

    public SiteExporter(DispatcherSync dispatcher) {
        this.dispatcher = dispatcher;

        book = new HSSFWorkbook();
        creationHelper = book.getCreationHelper();

        sheetNames = new HashMap<String, Integer>();

        declareStyles();
    }

    private void declareStyles() {
        dateStyle = book.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("m/d/yy"));

        coordStyle = book.createCellStyle();
        coordStyle.setDataFormat(creationHelper.createDataFormat().getFormat("0.000000"));

        indicatorValueStyle = book.createCellStyle();
        indicatorValueStyle.setDataFormat(creationHelper.createDataFormat().getFormat("#,##0"));

        Font headerFont = book.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

        Font smallFont = book.createFont();
        smallFont.setFontHeightInPoints((short) 8);

        headerStyle = book.createCellStyle();
        headerStyle.setFont(headerFont);

        headerStyleCenter = book.createCellStyle();
        headerStyleCenter.setFont(headerFont);
        headerStyleCenter.setAlignment(CellStyle.ALIGN_CENTER);

        headerStyleRight = book.createCellStyle();
        headerStyleRight.setFont(headerFont);
        headerStyleRight.setAlignment(CellStyle.ALIGN_RIGHT);

        attribHeaderStyle = book.createCellStyle();
        attribHeaderStyle.setFont(smallFont);
        attribHeaderStyle.setRotation((short) 45);
        attribHeaderStyle.setWrapText(true);

        indicatorHeaderStyle = book.createCellStyle();
        indicatorHeaderStyle.setFont(smallFont);
        indicatorHeaderStyle.setWrapText(true);
        indicatorHeaderStyle.setAlignment(CellStyle.ALIGN_RIGHT);

        attribValueStyle = book.createCellStyle();
        attribValueStyle.setFont(smallFont);

    }

    public void export(ActivityDTO activity) {

        HSSFSheet sheet = book.createSheet(composeUniqueSheetName(activity));
        sheet.createFreezePane(4, 2);

        // initConditionalFormatting(sheet);
        createHeaders(activity, sheet);
        createDataRows(activity, sheet);

    }
//
//    private void initConditionalFormatting(HSSFSheet sheet) {
//        HSSFSheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
//
//        attribFalseRule = cf.createConditionalFormattingRule(CFRuleRecord.ComparisonOperator.EQUAL, "FALSE", null);
//        HSSFFontFormatting grayFont = attribFalseRule.createFontFormatting();
//        grayFont.setFontColorIndex(HSSFColor.GREY_25_PERCENT.index);
//
//        attribTrueRule = cf.createConditionalFormattingRule(CFRuleRecord.ComparisonOperator.EQUAL, "TRUE", null);
//        HSSFPatternFormatting grayFill = attribTrueRule.createPatternFormatting();
//        grayFill.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
//        HSSFFontFormatting boldFont = attribTrueRule.createFontFormatting();
//        boldFont.setFontStyle(false,true);
//
//    }

    private String composeUniqueSheetName(ActivityDTO activity) {
        String sheetName = activity.getDatabase().getName() + " - " + activity.getName();
        // replace invalid chars: / \ [ ] * ?
        sheetName = sheetName.replaceAll("[\\Q/\\*?[]\\E]", " ");

        // sheet names can only be 31 characters long, plus we need about 4-6 chars for disambiguation
        String shortenedName = sheetName.substring(0, Math.min(25, sheetName.length()));

        // assure that the sheet name is unique
        if (!sheetNames.containsKey(shortenedName)) {
            sheetNames.put(shortenedName, 1);
            return sheetName;
        } else {
            int index = sheetNames.get(shortenedName);
            sheetNames.put(shortenedName, index + 1);
            return shortenedName + " (" + index + ")";
        }

    }

    private void createHeaders(ActivityDTO activity, HSSFSheet sheet) {

        /// The HEADER rows

        Row headerRow1 = sheet.createRow(0);
        Row headerRow2 = sheet.createRow(1);
        headerRow2.setHeightInPoints(75);

        int column = 0;
        createHeaderCell(headerRow2, column++, "Date1", CellStyle.ALIGN_RIGHT);
        createHeaderCell(headerRow2, column++, "Date2", CellStyle.ALIGN_RIGHT);

        createHeaderCell(headerRow2, column, "Partner");
        sheet.setColumnWidth(column, 16 * 256);
        column++;

        createHeaderCell(headerRow2, column, activity.getLocationType().getName());
        sheet.setColumnWidth(column, 20 * 256);
        column++;

        createHeaderCell(headerRow2, column++, "Axe");


        indicators = new ArrayList<Integer>(activity.getIndicators().size());
        if (activity.getReportingFrequency() == ActivityDTO.REPORT_ONCE) {
            for (IndicatorGroup group : activity.groupIndicators()) {
                if (group.getName() != null) {
                    // create a merged cell on the top row spanning all members of the group
                    createHeaderCell(headerRow1, column, group.getName());
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, column, column + group.getIndicators().size() - 1));
                }
                for (IndicatorDTO indicator : group.getIndicators()) {
                    indicators.add(indicator.getId());
                    createHeaderCell(headerRow2, column, indicator.getName(), indicatorHeaderStyle);
                    sheet.setColumnWidth(column, 16 * 256);
                    column++;
                }
            }
        }
        attributes = new ArrayList<Integer>();
        int firstAttributeColumn = column;
        for (AttributeGroupDTO group : activity.getAttributeGroups()) {
            if (group.getAttributes().size() != 0) {
                createHeaderCell(headerRow1, column, group.getName(), CellStyle.ALIGN_CENTER);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, column, column + group.getAttributes().size() - 1));

                for (AttributeDTO attrib : group.getAttributes()) {
                    attributes.add(attrib.getId());
                    createHeaderCell(headerRow2, column, attrib.getName(), attribHeaderStyle);
                    sheet.setColumnWidth(column, 5 * 256);
                    column++;
                }
            }
        }

        levels = new ArrayList<Integer>();
        for (AdminLevelDTO level : activity.getAdminLevels()) {
            createHeaderCell(headerRow2, column++, "Code " + level.getName());
            createHeaderCell(headerRow2, column++, level.getName());
            levels.add(level.getId());
        }
        createHeaderCell(headerRow2, column, "Longitude", CellStyle.ALIGN_RIGHT);
        createHeaderCell(headerRow2, column + 1, "Latitude", CellStyle.ALIGN_RIGHT);
        sheet.setColumnWidth(column, 12 * 256);
        sheet.setColumnWidth(column + 1, 12 * 256);

    }

    private List<SiteDTO> querySites(ActivityDTO activity) {

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Activity, activity.getId());

        GetSites query = new GetSites();
        query.setFilter(filter);
        query.setSortInfo(new SortInfo("date2", SortDir.DESC));
        
        return dispatcher.execute(query).getData();
    }

    private void createDataRows(ActivityDTO activity, Sheet sheet) {

        // Create the drawing patriarch. This is the top level container for all shapes including
        // cell comments.
        HSSFPatriarch patr = ((HSSFSheet) sheet).createDrawingPatriarch();

        int rowIndex = 2;
        for (SiteDTO site : querySites(activity)) {

            Row row = sheet.createRow(rowIndex++);
            int column = 0;

            createCell(row, column++, site.getDate1());
            createCell(row, column++, site.getDate2());
            createCell(row, column++, site.getPartnerName());

            Cell locationCell = createCell(row, column++, site.getLocationName());
            if (site.getComments() != null) {
                Comment comment = patr.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 8, 10));
                comment.setString(creationHelper.createRichTextString(site.getComments()));

                locationCell.setCellComment(comment);
            }

            createCell(row, column++, site.getLocationAxe());

            for (Integer indicatorId : indicators) {
                createIndicatorValueCell(row, column++, site.getIndicatorValue(indicatorId));
            }

            for (Integer attribId : attributes) {

                Boolean value = site.getAttributeValue(attribId);
                if (value != null) {
                    Cell valueCell = createCell(row, column, value);
                    valueCell.setCellStyle(attribValueStyle);
                }
                column++;
            }

            for (Integer levelId : levels) {
                AdminEntityDTO entity = site.getAdminEntity(levelId);
                if (entity != null) {
                    createCell(row, column, "");
                    createCell(row, column + 1, entity.getName());
                }
                column += 2;
            }

            if (site.hasLatLong()) {
                createCoordCell(row, column++, site.getLongitude());
                createCoordCell(row, column++, site.getLatitude());
            }
        }
    }

    private Cell createHeaderCell(Row headerRow, int columnIndex, String text, CellStyle style) {
        Cell cell = headerRow.createCell(columnIndex);
        cell.setCellValue(creationHelper.createRichTextString(text));
        cell.setCellStyle(style);

        return cell;
    }

    private Cell createHeaderCell(Row headerRow, int columnIndex, String text) {
        return createHeaderCell(headerRow, columnIndex, text, CellStyle.ALIGN_LEFT);
    }

    private Cell createHeaderCell(Row headerRow, int columnIndex, String text, int align) {
        Cell cell = headerRow.createCell(columnIndex);
        cell.setCellValue(creationHelper.createRichTextString(text));

        switch (align) {
            case CellStyle.ALIGN_LEFT:
                cell.setCellStyle(headerStyle);
                break;
            case CellStyle.ALIGN_CENTER:
                cell.setCellStyle(headerStyleCenter);
                break;
            case CellStyle.ALIGN_RIGHT:
                cell.setCellStyle(headerStyleRight);
                break;
        }

        return cell;
    }

    private Cell createCell(Row row, int columnIndex, String text) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(creationHelper.createRichTextString(text));
        return cell;
    }

    private void createCell(Row row, int columnIndex, LocalDate date) {
        Cell cell = row.createCell(columnIndex);
        if(date != null) {
        	cell.setCellValue(date.atMidnightInMyTimezone());
        }
        cell.setCellStyle(dateStyle);
    }

    private void createIndicatorValueCell(Row row, int columnIndex, Double value) {
        if (value != null) {
            Cell cell = row.createCell(columnIndex);
            cell.setCellValue(value);
            cell.setCellStyle(indicatorValueStyle);
        }
    }

    private void createCoordCell(Row row, int columnIndex, double value) {

        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(coordStyle);
    }

    private Cell createCell(Row row, int columnIndex, boolean value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);

        return cell;
    }

    public HSSFWorkbook getBook() {
        return book;
    }
}
