package com.zq.utils.kafka;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelGenerator {

	private static Logger logger = LoggerFactory.getLogger(ExcelGenerator.class);
	private Workbook workbook;
	private Sheet sheet;
	private CellStyle cellStyle;
	private int rowNo = 0;
	private int pageRowNo = 0;
	private List<String> columns;

	public ExcelGenerator() {
		this.workbook = new XSSFWorkbook();
		this.sheet = this.workbook.createSheet("第1页");
		this.sheet = this.workbook.getSheetAt(0);
	}

	public void createTitles(List<String> titles) {
		for (int i = 0; i < titles.size(); i++) {
			this.sheet.addMergedRegion(new CellRangeAddress(i,i,0,10));
			Row head = this.sheet.createRow(this.pageRowNo++);
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 12);
			font.setFontName("黑体");
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			
			CellStyle style = workbook.createCellStyle();
			style.setFont(font);
			Cell cell = head.createCell(0, Cell.CELL_TYPE_STRING);
			cell.setCellStyle(style);
			// 标题名称
			String title = titles.get(i);
			cell.setCellValue(title);
		}
	}
	
	public void createColumns(List<String> columns) {
		this.columns = columns;
		createTitleHead();
	}

	
	private void createTitleHead() {
		Row head = this.sheet.createRow(this.pageRowNo++);
		for (int i = 0; i < this.columns.size(); i++) {
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 30);
			font.setFontName("黑体");
			font.setBoldweight(Font.BOLDWEIGHT_BOLD);
			font.setFontHeightInPoints((short) 10);

			CellStyle style = workbook.createCellStyle();
			style.setFont(font);
			Cell cell = head.createCell(i, Cell.CELL_TYPE_STRING);
			cell.setCellStyle(style);

			// 标题名称
			String title = this.columns.get(i);
			cell.setCellValue(title);
			this.sheet.autoSizeColumn(i, true);// 设置列宽自适应
		}
		this.rowNo++;
	}

	public void appendDataSet(List<Map<String, Object>> data) throws IOException {
		// 处理map方式
		for (int i = 0; i < data.size(); i++) {
			Row row = sheet.createRow(this.pageRowNo++);
			fillFieldMapRow(row, data.get(i));
			this.rowNo = this.rowNo + 1;
		}
	}

	/**
	 * 
	 * 填充单元格值
	 * 
	 * @Description
	 * @param row
	 * @param cellStyle
	 * @param data
	 * @param fields    void
	 * @throws @author ChenYaQiang
	 * @date 2017年10月25日 上午10:48:16
	 * @see
	 */
	private void fillFieldMapRow(Row row, Map<String, Object> dataMap) {

		/**
		 * 拆成两个循环，如果在同一个循环里处理翻译后的字段更复杂而且不够灵活，以后不好改
		 */
		try {
			for (int i = 0; i < this.columns.size(); i++) {
				String column = this.columns.get(i);
				Cell cell = row.createCell(i);
				fillCell(cell, dataMap.get(column));
			}
		} catch (Exception e) {
			logger.error("设置cell的值时出现异常！", e);
			throw new RuntimeException("设置cell的值时出现异常！原因：" + e.getMessage(), e);
		}
	}

	/**
	 * 填充单元格
	 * <p>
	 * title: fillCell
	 * </p>
	 * <p>
	 * Company: shine
	 * </p>
	 * 
	 * @param cell
	 * @param value
	 * @param cellStyle
	 * 
	 * @author liupengfei
	 * @date 2017年10月17日 下午4:59:41
	 */
	private void fillCell(Cell cell, Object value) {

		if (cell == null || value == null) {
			return;
		}
        cellStyle = this.workbook.createCellStyle();
		cell.setCellType(Cell.CELL_TYPE_STRING);
		if (value instanceof Integer) {
			cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
			cell.setCellValue((Integer) value);
			cell.setCellStyle(cellStyle);
		} else if (value instanceof Double) {
			cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
			cell.setCellValue((Double) value);
			cell.setCellStyle(cellStyle);
		} else if (value instanceof Date) {
			SimpleDateFormat sf  = new SimpleDateFormat("yyyy-mm-dd");
			cell.setCellValue(sf.format(value));
			cell.setCellStyle(cellStyle);
		} else {
			cell.setCellValue(value.toString().equals("null") ? "" : value.toString());
		}

	}

	public void write(String filePath) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			workbook.write(fos);
		} catch (IOException e) {
			logger.error("关闭IO流失败", e);
		} finally {
			fos.close();
		}
	}

}
