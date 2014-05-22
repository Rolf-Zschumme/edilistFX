package de.vbl.ediliste.main;

import javafx.scene.Parent;
import javafx.scene.input.KeyCode;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.categories.TestFX;

@Category(TestFX.class)
public class EdiListMainTest extends GuiTest {
	
	
	@Override
	public Parent getRootNode() {
//		EdiListMain ediListMain = new EdiListMain(); 
//		return ediListMain.loadAndStartController();
		return null;
	  }
	
	  @Test
	  public void ediEintragNeuanlage1Empfaenger() {
		  click("#btnNewEdiNr");
		  click("OK");
		  exists("#btnEdiEintragSpeichern");
		  click("#btnSender");
		  click("#partnerCB").click("VBL");
		  click("#systemCB").click("SAP CRM");
		  click("#komponenteCB").click("Zulage");
		  click("OK");
		  click("#btnEmpfaenger1");
		  click("#partnerCB").click("Finanzamt");
		  click("#systemCB").click("ZfA-Server");
		  click("#komponenteCB").click("MQ: MAV");
		  click("OK");
		  click("#cmbBuOb1").type("ZfA-Meldungen");
		  click("#btnEdiEintragSpeichern");
	  }
	  
	  @Test
	  public void ediEintragNeuanlage2Empfaenger() {
		  click("#btnNewEdiNr");
		  click("OK");
		  exists("#btnEdiEintragSpeichern");
		  click("#btnSender");
		  click("#partnerCB").click("VBL");
		  click("#systemCB").click("SAP CRM");
		  click("#komponenteCB").click("ANW");
		  click("OK");
		  click("#btnEmpfaenger1");
		  click("#partnerCB").click("Beteiligte");
		  click("#systemCB").click("Internet");
		  click("#komponenteCB").click("ftp-Client");
		  click("OK");
		  click("#cmbBuOb1").type("anw-meldungen").push(KeyCode.TAB);
		  click("#btnEmpfaenger2");
		  click("#partnerCB").click("Beteiligte");
		  click("#systemCB").click("Internet");
		  click("#komponenteCB").click("ftp-Client");
		  click("OK");
		  click("#cmbBuOb2").type("ANW-Meldungsdokumente");
		  click("#btnEdiEintragSpeichern");
	  }
	  
}