package de.vbl.ediliste.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.model.GeschaeftsObjekt;

public class ExportToExcel {
	private static final String PARTNER_SHEET = "Partner";
	private static final String SYSTEM_SHEET = "Systeme";
	private static final String KOMPONENTEN_SHEET = "Komponenten";
	private static final String G_OBJEKT_SHEET = "Geschäftsobjekt";

	private EntityManager em;
	
	XSSFWorkbook wb;
	
	public ExportToExcel(EntityManager em) {
		this.em = em;
		wb = new XSSFWorkbook(); 
    	XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
	}

	public int write(File file) throws IOException {
	
		int anz_zeilen = 0; 
		CellStyle styleHeader;
		Font titleFont = wb.createFont();
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		styleHeader = wb.createCellStyle();
		styleHeader.setFont(titleFont);
		
		Font cellFont = wb.createFont();
		cellFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		CellStyle styleNormal;
		styleNormal = wb.createCellStyle();
		styleNormal.setFont(cellFont);
		
		Row row; 

		Sheet p_Sheet = wb.createSheet(PARTNER_SHEET);
		++anz_zeilen;
		row = p_Sheet.createRow(0);
		row.setHeightInPoints(20);
		int s = 0;
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "Partner");
		createCell(row, ++s, styleHeader, "Beschreibung");
		
    	Sheet s_Sheet = wb.createSheet(SYSTEM_SHEET);
		++anz_zeilen;
    	row = s_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "System");
		createCell(row, ++s, styleHeader, "Partner");
		createCell(row, ++s, styleHeader, "Beschreibung");
    	
    	Sheet k_Sheet = wb.createSheet(KOMPONENTEN_SHEET);
		++anz_zeilen;
    	row = k_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "Komponente");
		createCell(row, ++s, styleHeader, "System");
		createCell(row, ++s, styleHeader, "Partner");
		createCell(row, ++s, styleHeader, "Partner --- System --- Komponente");
		createCell(row, ++s, styleHeader, "Beschreibung");
		
    	Query query = em.createQuery( "SELECT p FROM EdiPartner p ORDER BY p.name",EdiPartner.class);
    	int p_znr = 0;
    	int s_znr = 0;
    	int k_znr = 0;
    	for (Object p : query.getResultList()) {
    		EdiPartner partner = (EdiPartner) p;
    		row =  p_Sheet.createRow(++p_znr);
    		++anz_zeilen;
    		createCell(row, s=0, styleNormal, p_znr); 
    		createCell(row, ++s, styleNormal, partner.getName());         
    		createCell(row, ++s, styleNormal, partner.getBeschreibung()); 
    		String p_zStr = Integer.toString(p_znr+1);
    		
    		for (EdiSystem system : partner.getEdiSystem()) {
        		row =  s_Sheet.createRow(++s_znr);
        		++anz_zeilen;
        		createCell(row, s=0, styleNormal, s_znr); 
        		createCell(row, ++s, styleNormal, system.getName());				
        		createCeFo(row, ++s, styleNormal , PARTNER_SHEET + "!B" + p_zStr);  
        		createCell(row, ++s, styleNormal, system.getBeschreibung());	
        		String s_zStr = Integer.toString(s_znr+1);
        		
        		for (EdiKomponente komponente : system.getEdiKomponente()) {
            		row =  k_Sheet.createRow(++k_znr);
            		++anz_zeilen;
            		String k_zStr = Integer.toString(k_znr+1);
            		createCell(row, s=0, styleNormal, k_znr); 
            		createCell(row, ++s, styleNormal, komponente.getName());
            		createCeFo(row, ++s, styleNormal , SYSTEM_SHEET + "!B" + s_zStr);
            		createCeFo(row, ++s, styleNormal , PARTNER_SHEET + "!B" + p_zStr);
            		createCeFo(row, ++s, styleNormal , "D" + k_zStr + " & \" --- \" & C" + k_zStr +
            														  " & \" --- \" & B" + k_zStr);
            		createCell(row, ++s, styleNormal, komponente.getBeschreibung());
        		}
    		}
    	}
    	for (s=0; s<3; ++s) p_Sheet.autoSizeColumn(s);
    	for (s=0; s<4; ++s) s_Sheet.autoSizeColumn(s);
    	for (s=0; s<6; ++s) k_Sheet.autoSizeColumn(s);

    	int p_MaxLen = p_Sheet.getColumnWidth(1);
    	int s_MaxLen = s_Sheet.getColumnWidth(1);
    	int k_MaxLen = k_Sheet.getColumnWidth(1);
    	s_Sheet.setColumnWidth(2, p_MaxLen);
    	k_Sheet.setColumnWidth(2, s_MaxLen);
    	k_Sheet.setColumnWidth(3, p_MaxLen);
    	k_Sheet.setColumnWidth(4, p_MaxLen + s_MaxLen + k_MaxLen);

    	
    	Sheet g_Sheet = wb.createSheet(G_OBJEKT_SHEET);
    	row = g_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "Geschäftsobjet");
		++anz_zeilen;
		
    	query = em.createQuery( "SELECT g FROM GeschaeftsObjekt g ORDER BY g.name",GeschaeftsObjekt.class);
    	int g_znr = 0;
    	for (Object g : query.getResultList()) {
    		GeschaeftsObjekt gObjekt = (GeschaeftsObjekt) g;
    		row =  g_Sheet.createRow(++g_znr);
    		++anz_zeilen;
    		createCell(row, s=0, styleNormal, g_znr); 
    		createCell(row, ++s, styleNormal, gObjekt.getName());
    	}
    	for (s=0; s<2; ++s) g_Sheet.autoSizeColumn(s);
    	
		FileOutputStream out = new FileOutputStream(file);
		wb.write(out);
		out.close();
		return anz_zeilen;
	}

	private void createCell(Row row, int i, CellStyle style, String text) {
		Cell cell = row.createCell(i);
		cell.setCellValue(text); 
		cell.setCellStyle(style);
	}
	
	private void createCell(Row row, int i, CellStyle style, int value) {
		Cell cell = row.createCell(i);
		cell.setCellValue((double) value); 
		cell.setCellStyle(style);
	}

	private void createCeFo(Row row, int i, CellStyle style, String formel) {
		Cell cell = row.createCell(i);
		cell.setCellFormula(formel);
		cell.setCellStyle(style);
	}
}