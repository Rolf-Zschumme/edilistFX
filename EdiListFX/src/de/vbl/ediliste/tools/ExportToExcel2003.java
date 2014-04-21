package de.vbl.ediliste.tools;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javafx.scene.control.Dialogs;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;

public class ExportToExcel2003 {
	private static final String PARTNER_SHEET = "Partner";
	private static final String SYSTEM_SHEET = "Systeme";
	private static final String KOMPONENTEN_SHEET = "Komponenten";

	private Stage primaryStage;
	private String appl_title;
	private EntityManager em;
	
	private WritableCellFormat aktfont10;
	private WritableCellFormat aktfont10bold;
	
	public ExportToExcel2003(Stage parent , EntityManager em) {
		primaryStage = parent;
		appl_title = parent.getTitle();
		this.em = em;
		aktfont10 = new WritableCellFormat( new WritableFont(WritableFont.ARIAL, 10));
		aktfont10bold = new WritableCellFormat( new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD));

	}

	public void write(File file) {
		
//		File file = new File("EdiListExport.xls");
		WorkbookSettings wbSettings = new WorkbookSettings();
		
		wbSettings.setLocale(new Locale("en","EN"));
		
		WritableWorkbook workbook = null;
		try {
			workbook = Workbook.createWorkbook(file, wbSettings);
		} catch (IOException e) {
			Dialogs.showErrorDialog(primaryStage, "Fehler bei Dateianlage", "?", appl_title, e);
		}
		try {
			createExcelSheets(workbook);
		} catch (WriteException e) {
			Dialogs.showErrorDialog(primaryStage, "Fehler beim Excelsheet füllen", "?", appl_title, e);
		}
		
		try {
			workbook.write();
			workbook.close();
		} catch (IOException e) {
			Dialogs.showErrorDialog(primaryStage, "Fehler bei Dateischreiben", "?", appl_title, e);
		} catch (WriteException e) {
			Dialogs.showErrorDialog(primaryStage, "Fehler bei Datei-Schließen", "?", appl_title, e);
		}
		
		Dialogs.showInformationDialog(primaryStage, 
				"Information", 
				"Export erstellt", appl_title);
	
	}
	
	private void createExcelSheets(WritableWorkbook wb) throws RowsExceededException, WriteException {
    	wb.createSheet(PARTNER_SHEET,0);
    	WritableSheet p_Sheet = wb.getSheet(0);
    	CellView cv = new CellView();
    	cv.setFormat(aktfont10);
    	cv.setFormat(aktfont10bold);
    	cv.setAutosize(true);
    	addCaption(p_Sheet,0,0,"lfd.-Nr.");
    	addCaption(p_Sheet,0,1,"Partner");
    	addCaption(p_Sheet,0,2,"Beschreibung");

    	wb.createSheet(SYSTEM_SHEET,1);
    	WritableSheet s_Sheet = wb.getSheet(1);
    	addCaption(s_Sheet,0,0,"lfd.-Nr.");
    	addCaption(s_Sheet,0,1,"System");
    	addCaption(s_Sheet,0,2,"Partner");
    	addCaption(s_Sheet,0,3,"Beschreibung");
    	
    	wb.createSheet(KOMPONENTEN_SHEET,2);
    	WritableSheet k_Sheet = wb.getSheet(2);
    	addCaption(k_Sheet,0,0,"lfd.-Nr.");
    	addCaption(k_Sheet,0,1,"Komponenten");
    	addCaption(k_Sheet,0,2,"System");
    	addCaption(k_Sheet,0,3,"Partner");
    	addCaption(k_Sheet,0,4,"Partner --- System --- Komponente");
    	addCaption(k_Sheet,0,5,"Beschreibung");
    	
    	int p_lfdnr = 0;  	int p_znr;
    	int s_lfdnr = 0;   	int s_znr;
    	int k_lfdnr = 0;   	int k_znr;
    	Query query = em.createQuery( "SELECT p FROM EdiPartner p ORDER BY p.name",EdiPartner.class);
    	for (Object p : query.getResultList()) {
    		p_znr = ++p_lfdnr;
    		EdiPartner partner = (EdiPartner) p;
    		addNumber(p_Sheet,p_znr,0,p_lfdnr);
    		addLabel(p_Sheet,p_znr,1,partner.getName());
    		addLabel(p_Sheet,p_znr,2,partner.getBeschreibung());
    		for (EdiSystem system : partner.getEdiSystem()) {
        		s_znr = ++s_lfdnr;
        		addNumber(s_Sheet,s_znr,0,s_lfdnr);
        		addLabel(s_Sheet,s_znr,1,system.getName());
        		addFormula(s_Sheet,s_znr,2,PARTNER_SHEET + "!B" + Integer.toString(p_znr+1));
        		addLabel(s_Sheet,s_znr,3,system.getBeschreibung());
        		for (EdiKomponente komponente : system.getEdiKomponente()) {
            		k_znr = ++k_lfdnr;
            		addNumber(k_Sheet,k_znr,0,k_lfdnr);
            		addLabel(k_Sheet,k_znr,1,komponente.getName());
            		addFormula(k_Sheet,k_znr,2,SYSTEM_SHEET + "!B" + i2Str(s_znr+1));
            		addFormula(k_Sheet,k_znr,3,PARTNER_SHEET + "!B" + i2Str(p_znr+1));
            		addFormula(k_Sheet,k_znr,4,verkette(verkette("D" + i2Str(k_znr+1),"\" --- \""),
            								   verkette(verkette("C" + i2Str(k_znr+1),"\" --- \""),
            										             "B" + i2Str(k_znr+1))));
            		addLabel(k_Sheet,k_znr,5,komponente.getBeschreibung());
        		}
    		}
    	}
	}
	private String verkette(String a, String b) {
		return "CONCATENATE(" + a + "," + b + ")"; 
	}
	
    private String i2Str(int i) {
    	return Integer.toString(i);
    }
	
    private void addCaption(WritableSheet sheet, int zeile, int spalte, String s) throws RowsExceededException, WriteException {
    	Label label = new Label(spalte, zeile, s, aktfont10bold);
    	sheet.addCell(label);
    }
    
	private void addLabel(WritableSheet sheet, int zeile, int spalte, String text ) throws RowsExceededException, WriteException {
		Label label = new Label(spalte, zeile, text, aktfont10);
		sheet.addCell(label);
	}
	
	private void addNumber(WritableSheet sheet, int zeile, int spalte, Integer integer) throws RowsExceededException, WriteException {
		Number number = new Number(spalte, zeile, integer, aktfont10);
		sheet.addCell(number);
	}
	
	private void addFormula(WritableSheet sheet, int zeile, int spalte, String formel) throws RowsExceededException, WriteException {
		StringBuffer buf = new StringBuffer(formel);
		Formula f = new Formula(spalte, zeile, buf.toString());
		sheet.addCell(f);
	}
	
	
}
