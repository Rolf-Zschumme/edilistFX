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
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;

import de.vbl.im.model.Ansprechpartner;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;

public class ExportToExcel {
	private static final String PARTNER_SHEET = "Partner";
	private static final String SYSTEM_SHEET = "Systeme";
	private static final String KOMPONENTEN_SHEET = "Komponenten";
	private static final String G_OBJEKT_SHEET = "Geschäftsobjekte";
	private static final String INSZENARIO_SHEET = "Integrationsszenarios";
//	private static final String KONFIGURATION_SHEET = "Konfigurationen";
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
//		Map<Long, String> konfigurationZeilenNr = new HashMap<Long, String>();
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
		
		CellStyle styleWrapTx;
		styleWrapTx = wb.createCellStyle();
		styleWrapTx.setFont(cellFont);
		styleWrapTx.setWrapText(true);
		
		CellStyle styleNumber;
		styleNumber = wb.createCellStyle();
		styleNumber.setFont(cellFont);
		styleNumber.setAlignment(CellStyle.ALIGN_RIGHT);
		DataFormat format=wb.createDataFormat();
		styleNumber.setDataFormat(format.getFormat("###,###,##0.0"));

		CellStyle styleNumbr0;
		styleNumbr0 = wb.createCellStyle();
		styleNumbr0.setFont(cellFont);
		styleNumbr0.setAlignment(CellStyle.ALIGN_RIGHT);
		styleNumbr0.setDataFormat(format.getFormat("###,###,##0"));
		
		
		Row row; 

		Sheet p_Sheet = wb.createSheet(PARTNER_SHEET);
		++anz_zeilen;
		row = p_Sheet.createRow(0);
		row.setHeightInPoints(20);
		int s = 0;
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "Partner");
		createCell(row, ++s, styleHeader, "Ansprechpartner");
		createCell(row, ++s, styleHeader, "Beschreibung");
		
    	Sheet s_Sheet = wb.createSheet(SYSTEM_SHEET);
		++anz_zeilen;
    	row = s_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, ++s, styleHeader, "System");
		createCell(row, ++s, styleHeader, "Partner");
		createCell(row, ++s, styleHeader, "Ansprechpartner");
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
		createCell(row, ++s, styleHeader, "Ansprechpartner");
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
    		createCell(row, ++s, styleWrapTx, Ansprechpartner.alltoString(partner.getAnsprechpartner())); 
    		createCell(row, ++s, styleWrapTx, partner.getBeschreibung()); 
    		String p_zStr = Integer.toString(p_znr+1);
    		
    		for (InSystem system : partner.getInSystem()) {
        		row =  s_Sheet.createRow(++s_znr);
        		++anz_zeilen;
        		createCell(row, s=0, styleNormal, s_znr); 
        		createCell(row, ++s, styleNormal, system.getName());				
        		createCeFo(row, ++s, styleNormal , PARTNER_SHEET + "!B" + p_zStr);  
        		createCell(row, ++s, styleWrapTx, Ansprechpartner.alltoString(system.getAnsprechpartner())); 
        		createCell(row, ++s, styleWrapTx, system.getBeschreibung());	
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
            		createCell(row, ++s, styleWrapTx, Ansprechpartner.alltoString(system.getAnsprechpartner())); 
            		createCell(row, ++s, styleWrapTx, komponente.getBeschreibung());
            		komponentenZeilenNr.put(komponente.getId(), k_zStr);
        		}
    		}
    	}
    	for (s=0; s<4; ++s) p_Sheet.autoSizeColumn(s);
    	for (s=0; s<5; ++s) s_Sheet.autoSizeColumn(s);
    	for (s=0; s<8; ++s) k_Sheet.autoSizeColumn(s);

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

    	int go_MaxLen = g_Sheet.getColumnWidth(1);
    	
//    	Sheet c_Sheet = wb.createSheet(KONFIGURATION_SHEET);
//    	row = c_Sheet.createRow(0);
//		row.setHeightInPoints(20);
//		createCell(row, s=0, styleHeader, "lfd-Nr.");
//		createCell(row, ++s, styleHeader, "Konfiguration");
//		createCell(row, ++s, styleHeader, "InSzenario");
//		++anz_zeilen;

//		for (Konfiguration configuration : inSzenario.getKonfiguration()) {
//			row =  c_Sheet.createRow(++c_znr);
//			++anz_zeilen;
//			createCell(row, s=0, styleNormal, c_znr); 
//			createCell(row, ++s, styleNormal, configuration.getName());				
//			createCeFo(row, ++s, styleNormal , INSZENARIO_SHEET + "!B" + i_zStr);  
//			String c_zStr = Integer.toString(c_znr+1);
//			konfigurationZeilenNr.put(configuration.getId(), c_zStr);
//	}
	
    	Sheet is_Sheet = wb.createSheet(INSZENARIO_SHEET);
    	row = is_Sheet.createRow(0);
		row.setHeightInPoints(20);
//		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, s=0, styleHeader, "IS-Nr.");
		createCell(row, ++s, styleHeader, "InSzenario");
		createCell(row, ++s, styleHeader, "Ansprechpartner");
		createCell(row, ++s, styleHeader, "Beschreibung");
		
		++anz_zeilen;
		
    	Sheet in_Sheet = wb.createSheet(INT_SHEET);
    	row = in_Sheet.createRow(0);
		row.setHeightInPoints(20);
//		createCell(row, s=0, styleHeader, "lfd-Nr.");
		createCell(row, s=0, styleHeader, "I-Nr.");
		createCell(row, ++s, styleHeader, "Integrationsszenario");
		createCell(row, ++s, styleHeader, "Bezeichnung");
		createCell(row, ++s, styleHeader, "Sender");
		createCell(row, ++s, styleHeader, "Intervall");
		createCell(row, ++s, styleHeader, "Anz.Transfers");
		createCell(row, ++s, styleHeader, "⌀  Größe(KG)");
		createCell(row, ++s, styleHeader, "Max.Größe(KG)");
		createCell(row, ++s, styleHeader, "ab Datum");
		createCell(row, ++s, styleHeader, "bis Datum");
		createCell(row, ++s, styleHeader, "Konfiguration");
		++anz_zeilen;
		
    	TypedQuery<InSzenario> inSzenarioQuery = em.createQuery(
    			"SELECT i FROM InSzenario i ORDER BY i.name",InSzenario.class);
    	int i_znr = 0;
    	int e_znr = 0;
    	for (InSzenario inSzenario : inSzenarioQuery.getResultList()) {
    		row =  is_Sheet.createRow(++i_znr);
    		++anz_zeilen;
    		int spIS=0;
    		createCell(row, spIS++, styleNormal, inSzenario.getIsNrStr());
    		createCell(row, spIS++, styleNormal, inSzenario.getName());
    		createCell(row, spIS++, styleWrapTx, Ansprechpartner.alltoString(inSzenario.getAnsprechpartner())); 
    		createCell(row, spIS++, styleWrapTx, inSzenario.getBeschreibung()); 
    		String i_zStr = Integer.toString(i_znr+1);
    		
        		for(Integration integration : inSzenario.getIntegration()) {
        			row = in_Sheet.createRow(++e_znr);
            		++anz_zeilen;
            		int spI = 0;
            		createCell(row, spI++, styleNormal, integration.inNrStrExp().get());				
            		createCeFo(row, spI++, styleNormal, INSZENARIO_SHEET + "!B" + i_zStr);				
            		createCell(row, spI++, styleNormal, integration.getBezeichnung());				
            		createCeFo(row, spI++, styleNormal, KOMPONENTEN_SHEET + "!F" + 
            				komponentenZeilenNr.get(integration.getInKomponente().getId()));
            		String intervall = integration.getIntervall() == null ? "" : integration.getIntervall().getName();
            		createCell(row, spI++, styleNormal, intervall);
            		createCell(row, spI++, styleNormal, integration.getAnzahlMsg());
            		double avKB = integration.getAverageByte() / 100;
            		createCell(row, spI++, styleNumber, String.format("%.1f", avKB / 10));
            		double mxKB = integration.getMaximalByte() / 100;
            		createCell(row, spI++, styleNumbr0, String.format("%.0f", mxKB / 10));
            		createCell(row, spI++, styleNormal, integration.getSeitDatum());
            		createCell(row, spI++, styleNormal, integration.getBisDatum());
            		String konfigurationName = integration.getKonfiguration() == null ? "" : integration.getKonfiguration().getName();
            		createCell(row, spI++, styleNormal, konfigurationName);
//            		createCeFo(row, spI++, styleNormal, KONFIGURATION_SHEET + "!B" + c_zStr);				
        		}
    		inSzenarioZeilenNr.put(inSzenario.getId(), i_zStr);
    	}
    	for (s=0; s<=4; ++s)  is_Sheet.autoSizeColumn(s);
    	for (s=0; s<=11; ++s) in_Sheet.autoSizeColumn(s);
    	
    	int is_MaxLen = is_Sheet.getColumnWidth(1);
    	in_Sheet.setColumnWidth(1, is_MaxLen);
    	in_Sheet.setColumnWidth(3, p_MaxLen + s_MaxLen + k_MaxLen);

    	Sheet ie_Sheet = wb.createSheet(EMPFAENGER_SHEET);
    	row = ie_Sheet.createRow(0);
		row.setHeightInPoints(20);
		createCell(row, s=0, styleHeader, "I-Nr.");
		createCell(row, ++s, styleHeader, "Integrationszenario");
		createCell(row, ++s, styleHeader, "Sender");
		createCell(row, ++s, styleHeader, "Geschäftsobjekt");
		createCell(row, ++s, styleHeader, "Empfänger");
		++anz_zeilen;
		
    	TypedQuery<Integration> integrationQuery = em.createQuery(
    			"SELECT e FROM Integration e ORDER BY e.inNr", Integration.class);
    	e_znr = 0;
    	for(Integration integration : integrationQuery.getResultList()) {
			int empfaengerIndex = 1;
			for(InEmpfaenger inEmpfaenger : integration.getInEmpfaenger()) {
				if (integration.getInKomponente().getId() <= 0) {
					System.out.println("Integration ohne Sender " + integration.inNrStrExp().get());
					continue; 
				}
				row = ie_Sheet.createRow(++e_znr);
				++anz_zeilen;
				createCell(row, s=0, styleNormal, integration.inNrStrExp().get() + "." + empfaengerIndex++);			
				createCeFo(row, ++s, styleNormal, INSZENARIO_SHEET + "!B" + 
						inSzenarioZeilenNr.get(integration.getInSzenario().getId()));				
//				createCeFo(row, ++s, styleNormal, KONFIGURATION_SHEET + "!B" +
//						konfigurationZeilenNr.get(integration.get....getId()));				
//				createCell(row, ++s, styleNormal, integration.getBezeichnung());				
				createCeFo(row, ++s, styleNormal, KOMPONENTEN_SHEET + "!F" + 
						komponentenZeilenNr.get(integration.getInKomponente().getId()));
//				String intervall = integration.getIntervall() == null ? "" : integration.getIntervall().getName();
//				createCell(row, ++s, styleNormal, intervall);
//				createCell(row, ++s, styleNormal, integration.getSeitDatum());
//				createCell(row, ++s, styleNormal, integration.getBisDatum());
				createCeFo(row, ++s, styleNormal, G_OBJEKT_SHEET + "!B" +
						geschaeftsObZeilenNr.get(inEmpfaenger.getGeschaeftsObjekt().getId()));;
				createCeFo(row, ++s, styleNormal, KOMPONENTEN_SHEET + "!F" +
						komponentenZeilenNr.get(inEmpfaenger.getKomponente().getId()));;
			}	
		}
    	for (s=0; s<=5; ++s) ie_Sheet.autoSizeColumn(s);
    	
    	ie_Sheet.setColumnWidth(1, is_MaxLen);
    	ie_Sheet.setColumnWidth(2, p_MaxLen + s_MaxLen + k_MaxLen);
    	ie_Sheet.setColumnWidth(3, go_MaxLen);
    	ie_Sheet.setColumnWidth(4, p_MaxLen + s_MaxLen + k_MaxLen);
    	

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