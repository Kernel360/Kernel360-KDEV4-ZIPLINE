package com.zipline.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.zipline.global.exception.common.FileUploadException;
import com.zipline.global.exception.common.errorcode.CommonErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExcelReader {

	public <T> List<T> readExcel(MultipartFile inputStream, ExcelRowMapper<T> mapper) {
		try (InputStream is = inputStream.getInputStream()) {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			List<T> list = new ArrayList<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				if (isEmptyRow(sheet.getRow(i), formatter)) {
					continue;
				}
				list.add(mapper.map(sheet.getRow(i), formatter));
			}
			return list;
		} catch (IOException e) {
			throw new FileUploadException(CommonErrorCode.FILE_UPLOAD_FAILED);
		}
	}

	private boolean isEmptyRow(Row row, DataFormatter formatter) {
		if (row == null) {
			return true;
		}

		for (Cell cell : row) {
			String cellValue = formatter.formatCellValue(cell);
			if (cellValue != null && !cellValue.trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
