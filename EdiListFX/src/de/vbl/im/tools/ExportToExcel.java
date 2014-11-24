package de.vbl.im.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;
import de.vbl.im.model.Konfiguration;

public class ExportToExcel {
	private static final String PARTNER_SHEET = "Partner";
	private static final String SYSTEM_SHEET = "Systeme";
	private static final String KOMPONENTEN_SHEET = "Komponenten";
	private static final String G_OBJEKT_SHEET = "Geschäftsobjekte";
	private static final String INSZENARIO_SHEET = "Integrationsszenarios";
	private static final String KONFIGURATION_SHEET = "Konfigurationen";
	private static final String INT_SHEET = "Integrationen";
	private static final String EMPFAENGER_SHEET = "Integrationen mit Empfängern";

	private EntityManager em;
	
	XSSFWorkbook wb;
	
	public ExportToExcel(EntityManager em) {
		this.em = em;
		wb = new XSSFWorkbook(); 
    	XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
	}

	public int write(File file) throws IOException {
	
		Map<Long, String> komponentenZeilenNr   = new HashMap<Long, String>();
		Map<Long, String> inSzenarioZeilenNr   = new HashMap<Long, String>();
		Map<Long, String> konfigurationZeilenNr = new HashMap<Long, String>();
		Map<Long, String> geschaeftsObZeilenNr  = new HashMap<Long, String>();
		
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
    	
		String TRZ = IMconstant.KOMPO_TRENNUNG;
		
    	Sheet k_Sheet = wb.createSheet(KOMPONENTEN_SHEET);
		++anz_zeilen;
    	row = k_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "ID");
		createCell(row, ++s, styleHeader, "Komponente");
		createCell(row, ++s, styleHeader, "System");
		createCell(row, ++s, styleHeader, "Partner");
		createCell(row, ++s, styleHeader, "Partner" + TRZ + "System" + TRZ + "Komponente");
		createCell(row, ++s, styleHeader, "Beschreibung");
		
    	TypedQuery<InPartner> partnerQuery = em.createQuery(
    			"SELECT p FROM InPartner p ORDER BY p.name",InPartner.class);
    	int p_znr = 0;
    	int s_znr = 0;
    	int k_znr = 0;
    	for (InPartner partner : partnerQuery.getResultList()) {
    		row =  p_Sheet.createRow(++p_znr);
    		++anz_zeilen;
    		createCell(row, s=0, styleNormal, p_znr); 
    		createCell(row, ++s, styleNormal, partner.getName());         
    		createCell(row, ++s, styleNormal, partner.getBeschreibung()); 
    		String p_zStr = Integer.toString(p_znr+1);
    		
    		for (InSystem system : partner.getInSystem()) {
        		row =  s_Sheet.createRow(++s_znr);
        		++anz_zeilen;
        		createCell(row, s=0, styleNormal, s_znr); 
        		createCell(row, ++s, styleNormal, system.getName());				
        		createCeFo(row, ++s, styleNormal , PARTNER_SHEET + "!B" + p_zStr);  
        		createCell(row, ++s, styleNormal, system.getBeschreibung());	
        		String s_zStr = Integer.toString(s_znr+1);
        		
        		for (InKomponente komponente : system.getInKomponente()) {
            		row =  k_Sheet.createRow(++k_znr);
            		++anz_zeilen;
            		String k_zStr = Integer.toString(k_znr+1);
            		createCell(row, s=0, styleNormal, k_znr); 
            		createCell(row, ++s, styleNormal, komponente.getId());
            		createCell(row, ++s, styleNormal, komponente.getName());
            		createCeFo(row, ++s, styleNormal , SYSTEM_SHEET + "!B" + s_zStr);
            		createCeFo(row, ++s, styleNormal , PARTNER_SHEET + "!B" + p_zStr);
            		createCeFo(row, ++s, styleNormal , "E" + k_zStr + " & \"" + TRZ + "\" & D" + k_zStr +
            														  " & \"" + TRZ + "\" & C" + k_zStr);
//																	  " & \" --- \" & C" + k_zStr);
            		createCell(row, ++s, styleNormal, komponente.getBeschreibung());
            		komponentenZeilenNr.put(komponente.getId(), k_zStr);
        		}
    		}
    	}
    	for (s=0; s<3; ++s) p_Sheet.autoSizeColumn(s);
    	for (s=0; s<4; ++s) s_Sheet.autoSizeColumn(s);
    	for (s=0; s<7; ++s) k_Sheet.autoSizeColumn(s);

    	int p_MaxLen = p_Sheet.getColumnWidth(1);
    	int s_MaxLen = s_Sheet.getColumnWidth(1);
    	int k_MaxLen = k_Sheet.getColumnWidth(1);
    	s_Sheet.setColumnWidth(2, p_MaxLen);
    	k_Sheet.setColumnWidth(3, s_MaxLen);
    	k_Sheet.setColumnWidth(4, p_MaxLen);
    	k_Sheet.setColumnWidth(5, p_MaxLen + s_MaxLen + k_MaxLen);
    	k_Sheet.setColumnHidden(1, true);
    	
    	Sheet g_Sheet = wb.createSheet(G_OBJEKT_SHEET);
    	row = g_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "Geschäftsobjet");
		++anz_zeilen;
		
    	TypedQuery<GeschaeftsObjekt> geschaeftsObjektQuery = em.createQuery(
    			"SELECT g FROM GeschaeftsObjekt g ORDER BY g.name",GeschaeftsObjekt.class);
    	int g_znr = 0;
    	for (GeschaeftsObjekt gObjekt : geschaeftsObjektQuery.getResultList()) {
    		row =  g_Sheet.createRow(++g_znr);
    		++anz_zeilen;
    		createCell(row, s=0, styleNormal, g_znr); 
    		createCell(row, ++s, styleNormal, gObjekt.getName());
    		geschaeftsObZeilenNr.put(gObjekt.getId(), Integer.toString(g_znr+1));
    	}
    	for (s=0; s<2; ++s) g_Sheet.autoSizeColumn(s);

    	
    	Sheet i_Sheet = wb.createSheet(INSZENARIO_SHEET);
    	row = i_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "InSzenario");
		++anz_zeilen;
		
    	Sheet c_Sheet = wb.createSheet(KONFIGURATION_SHEET);
    	row = c_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "Konfiguration");
		createCell(row, ++s, styleHeader, "InSzenario");
		++anz_zeilen;

    	Sheet e_Sheet = wb.createSheet(INT_SHEET);
    	row = e_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "I-Nr.");
		createCell(row, ++s, styleHeader, "InSzenario");
		createCell(row, ++s, styleHeader, "Konfiguration");
		createCell(row, ++s, styleHeader, "Bezeichnung");
		createCell(row, ++s, styleHeader, "Sender");
		createCell(row, ++s, styleHeader, "Intervall");
		createCell(row, ++s, styleHeader, "ab Datum");
		createCell(row, ++s, styleHeader, "bis Datum");
		++anz_zeilen;
		
    	TypedQuery<InSzenario> inSzenarioQuery = em.createQuery(
    			"SELECT i FROM InSzenario i ORDER BY i.name",InSzenario.class);
    	int i_znr = 0;
    	int c_znr = 0;
    	int e_znr = 0;
    	for (InSzenario inSzenario : inSzenarioQuery.getResultList()) {
    		row =  i_Sheet.createRow(++i_znr);
    		++anz_zeilen;
    		createCell(row, s=0, styleNormal, i_znr); 
    		createCell(row, ++s, styleNormal, inSzenario.getName());
    		String i_zStr = Integer.toString(i_znr+1);
    		
    		for (Konfiguration configuration : inSzenario.getKonfiguration()) {
        		row =  c_Sheet.createRow(++c_znr);
        		++anz_zeilen;
        		createCell(row, s=0, styleNormal, c_znr); 
        		createCell(row, ++s, styleNormal, configuration.getName());				
        		createCeFo(row, ++s, styleNormal , INSZENARIO_SHEET + "!B" + i_zStr);  
        		String c_zStr = Integer.toString(c_znr+1);
        		
        		for(Integration integration : configuration.getIntegration()) {
        			row = e_Sheet.createRow(++e_znr);
            		++anz_zeilen;
            		createCell(row, s=0, styleNormal, e_znr); 
            		createCell(row, ++s, styleNormal, integration.getInNr());				
            		createCeFo(row, ++s, styleNormal, INSZENARIO_SHEET + "!B" + i_zStr);				
            		createCeFo(row, ++s, styleNormal, KONFIGURATION_SHEET + "!B" + c_zStr);				
            		createCell(row, ++s, styleNormal, integration.getBezeichnung());				
            		createCeFo(row, ++s, styleNormal, KOMPONENTEN_SHEET + "!F" + 
            				komponentenZeilenNr.get(integration.getInKomponente().getId()));
            		String intervall = integration.getIntervall() == null ? "" : integration.getIntervall().getName();
            		createCell(row, ++s, styleNormal, intervall);
            		createCell(row, ++s, styleNormal, integration.getSeitDatum());
            		createCell(row, ++s, styleNormal, integration.getBisDatum());
        		}
        		konfigurationZeilenNr.put(configuration.getId(), c_zStr);
    		}
    		inSzenarioZeilenNr.put(inSzenario.getId(), i_zStr);
    	}
    	for (s=0; s<=2; ++s) i_Sheet.autoSizeColumn(s);
    	for (s=0; s<=3; ++s) c_Sheet.autoSizeColumn(s);
    	for (s=0; s<=12; ++s) e_Sheet.autoSizeColumn(s);
    	
    	int i_MaxLen = i_Sheet.getColumnWidth(1);
    	int c_MaxLen = c_Sheet.getColumnWidth(1);
    	c_Sheet.setColumnWidth(2, i_MaxLen);
    	e_Sheet.setColumnWidth(2, i_MaxLen);
    	e_Sheet.setColumnWidth(3, c_MaxLen);
    	e_Sheet.setColumnWidth(5, p_MaxLen + s_MaxLen + k_MaxLen);
    	
    	Sheet ee_Sheet = wb.createSheet(EMPFAENGER_SHEET);
    	row = ee_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "I-Nr.");
		createCell(row, ++s, styleHeader, "InSzenario");
		createCell(row, ++s, styleHeader, "Konfiguration");
		createCell(row, ++s, styleHeader, "Bezeichnung");
		createCell(row, ++s, styleHeader, "Sender");
		createCell(row, ++s, styleHeader, "Intervall");
		createCell(row, ++s, styleHeader, "ab Datum");
		createCell(row, ++s, styleHeader, "bis Datum");
		createCell(row, ++s, styleHeader, "Empfänger");
		++anz_zeilen;
		
    	TypedQuery<Integration> integrationQuery = em.createQuery(
    			"SELECT e FROM Integration e ORDER BY e.inNr", Integration.class);
    	e_znr = 0;
    	for(Integration integration : integrationQuery.getResultList()) {
			
			for(InEmpfaenger inEmpfaenger : integration.getInEmpfaenger()) {
				if (integration.getInKomponente().getId() <= 0) {
					System.out.println("Integration ohne Sender " + integration.inNrStrExp().get());
					continue; 
				}
				row = ee_Sheet.createRow(++e_znr);
				++anz_zeilen;
				createCell(row, s=0, styleNormal, e_znr); 
				createCell(row, ++s, styleNormal, integration.inNrStrExp().get());			
				createCeFo(row, ++s, styleNormal, INSZENARIO_SHEET + "!B" + 
						inSzenarioZeilenNr.get(integration.getKonfiguration().getInSzenario().getId()));				
				createCeFo(row, ++s, styleNormal, KONFIGURATION_SHEET + "!B" +
						konfigurationZeilenNr.get(integration.getKonfiguration().getId()));				
				createCell(row, ++s, styleNormal, integration.getBezeichnung());				
				createCeFo(row, ++s, styleNormal, KOMPONENTEN_SHEET + "!F" + 
						komponentenZeilenNr.get(integration.getInKomponente().getId()));
				String intervall = integration.getIntervall() == null ? "" : integration.getIntervall().getName();
				createCell(row, ++s, styleNormal, intervall);
				createCell(row, ++s, styleNormal, integration.getSeitDatum());
				createCell(row, ++s, styleNormal, integration.getBisDatum());
				createCeFo(row, ++s, styleNormal, KOMPONENTEN_SHEET + "!F" +
						komponentenZeilenNr.get(inEmpfaenger.getKomponente().getId()));;
				createCeFo(row, ++s, styleNormal, G_OBJEKT_SHEET + "!B" +
						geschaeftsObZeilenNr.get(inEmpfaenger.getGeschaeftsObjekt().getId()));;
			}	
		}
    	for (s=0; s<=14; ++s) ee_Sheet.autoSizeColumn(s);
    	
    	ee_Sheet.setColumnWidth(2, i_MaxLen);
    	ee_Sheet.setColumnWidth(3, c_MaxLen);
    	ee_Sheet.setColumnWidth(5, p_MaxLen + s_MaxLen + k_MaxLen);
    	

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

	private void createCell(Row row, int i, CellStyle style, long value) {
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