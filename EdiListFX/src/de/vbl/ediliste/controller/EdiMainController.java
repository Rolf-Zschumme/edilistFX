package de.vbl.ediliste.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.model.GeschaeftsObjekt;
import de.vbl.ediliste.tools.ExportToExcel;
import de.vbl.ediliste.view.EdiNrListElement;
import de.vbl.ediliste.view.PartnerListElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;



public class EdiMainController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private TextField txtInfoZeile;
    @FXML private TabPane tabPaneObjekte;    
    @FXML private Tab tabEdiNr;
    @FXML private TableView<EdiNrListElement> tableEdiNrAuswahl;
    @FXML private TableColumn<EdiNrListElement, String> tColAuswahlEdiNr;
    @FXML private TableColumn<EdiNrListElement, String> tColAuswahlEdiNrBezeichnung;

    @FXML private Tab tabPartner;
    @FXML private TableView<PartnerListElement> tablePartnerAuswahl;
    @FXML private TableColumn<PartnerListElement, String> tColAuswahlPartnerName;
    @FXML private TableColumn<PartnerListElement, Integer> tColAuswahlPartnerSysteme;
    @FXML private TableColumn<PartnerListElement, Integer> tColAuswahlPartnerKomponenten;

    @FXML private Tab tabSysteme;
    @FXML private TableView<EdiSystem> tableSystemAuswahl;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemSystemName;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemPartnerName;
    @FXML private TableColumn<EdiSystem, Integer> tColSelSystemKomponenten;

    @FXML private Tab tabKomponenten;
    @FXML private TableView<EdiKomponente> tableKomponentenAuswahl;   
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoKomponten;
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoSysteme;
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoPartner;
    
    @FXML private Tab tabGeschaeftsobjekt;
    @FXML private TableView<GeschaeftsObjekt> tableGeschaeftsobjektAuswahl;
    @FXML private TableColumn<GeschaeftsObjekt, String> tColAuswahlGeschaeftsobjektName;
    @FXML private TableColumn<GeschaeftsObjekt, Integer> tColAuswahlGeschaeftsobjektAnzahl;
    
    @FXML private Button btnNewEdiNr;
    @FXML private Button btnDeleteEdiEintrag;
    @FXML private Button btnDeleteGeschaeftsobjekt;
    @FXML private Button btnExportExcel;

    @FXML private SplitPane splitPane;
    @FXML private AnchorPane ediEintragSplitPane;
    @FXML private AnchorPane komponenteSplitPane;
    
    
    
	private static String applName;
	private Stage primaryStage;
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    private int maxEdiNr;

//  private EdiEintragController ediEintragController;
	
    public void start(Stage stage, String applikationName) {
    	primaryStage = stage;
    	applName = applikationName;
//    	ediEintragController.setInitial(this, primaryStage, applName, em);
    }

    @FXML
	private void initialize () {
		System.out.println("EdiMainController.initialize()");
		setupEntityManager();
		checkFieldsFromView();
    	loadEdiNrListData();
		
	}	

	private void loadEdiNrListData() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.bezeichnung FROM EdiEintrag e ORDER BY e.ediNr");
    	ediNrArrayList.clear();
    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			ediNrArrayList.add(new EdiNrListElement( (Long) obj[0], (Integer) obj[1], (String) obj[2]));
			max = (Integer) obj[1]; 
    	}	
    	maxEdiNr = max;
	}
    
    
	@FXML
    void btnUeber(ActionEvent event) {
    	Dialogs.showInformationDialog(primaryStage, 
    			"Version 0.5 - 08.05.2014 mit Geschäftsobjekt", 
    			"VBL-Tool zur Verwaltung der EDI-Liste", applName);
    }

    @FXML
    void newEdiNr(ActionEvent event) {
    
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("../view/NeuerEdiEintrag.fxml"));
    	try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	
    	NeuerEdiEintragController dialogController = loader.getController();
    	
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + 250);
    	dialog.setY(primaryStage.getY() + 100);
    	dialog.showAndWait();

    	if (dialogController.getResponse() == DialogResponse.OK) {
    		EdiEintrag newEE = dialogController.getNewEdiEintrag();
			EdiNrListElement newListElement = new EdiNrListElement(newEE.getId(),newEE.getEdiNr(),newEE.getBezeichnung());
			ediNrArrayList.add(newListElement);
			if (newEE.getEdiNr() > maxEdiNr) 
				maxEdiNr = newEE.getEdiNr();
			tableEdiNrAuswahl.getSelectionModel().select(newListElement);
    	}
    }    

    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
    void deleteEdiEintrag(ActionEvent event) {
    	EdiNrListElement selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		String ediNr = selectedlistElement.ediNrProperty().get();
    		DialogResponse response = Dialogs.showConfirmDialog(primaryStage, 
    				"EDI-Eintrag mit der Nr. " + ediNr + " wirklich löschen?",
    				SICHERHEITSABFRAGE,"", DialogOptions.OK_CANCEL);
    		if (response == DialogResponse.OK) {
    			EdiEintrag ediEintrag = em.find(EdiEintrag.class, selectedlistElement.getEdiId());
    			if (ediEintrag==null) {
    				System.out.println("FEHLER: EDI-Eintrag " + ediNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		em.getTransaction().begin();
	        		Iterator<EdiEmpfaenger> empf = ediEintrag.getEdiEmpfaenger().iterator();
	        		while (empf.hasNext()) {
	        			em.remove(empf.next());
	        			empf.remove();
	        		}
	        		em.remove(ediEintrag);
	        		em.getTransaction().commit();
	        		Dialogs.showInformationDialog(primaryStage, "Edi-Eintrag " + ediNr + " erfolgreich gelöscht");
    			}	
    			ediNrArrayList.remove(selectedlistElement);
    			tableEdiNrAuswahl.getSelectionModel().clearSelection();
    		}
    	}
    }
    
    
    static String initialExportFilePath = System.getProperty("user.home");
    static String initialExportFileName = "EDI-List-Excel-Export.xlsx";

    @FXML
    void btnExportExcel (ActionEvent event) {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Name und Ablageort der Export-Datei eingeben");
    	fileChooser.setInitialFileName(initialExportFileName);
    	fileChooser.getExtensionFilters().add(
    			new FileChooser.ExtensionFilter("Excel-Arbeitsmappe", "*.xlsx"));
    	File filepath = new File(initialExportFilePath);
    	if (filepath.isDirectory()==false)
    		initialExportFilePath = System.getProperty("user.home");
   		fileChooser.setInitialDirectory(new File (initialExportFilePath));

   		File file = fileChooser.showSaveDialog(primaryStage);
   		
    	if (file != null) {
			initialExportFilePath = file.getParent();
			initialExportFileName = file.getName();
    		ExportToExcel export = new ExportToExcel(em);
    		try {
    			int lines = export.write(file);
				Dialogs.showInformationDialog(primaryStage, 
						lines + " Zeilen in der Datei " + file.getName() + 
						" im Verzeichnis " + file.getParent() +" gespeichert",
						"Hinweis", applName);
			} catch (IOException e1) {
				Dialogs.showErrorDialog(primaryStage, e1.getMessage(),
				       "Fehler beim Speichern der Exportdatei", applName, e1);
			}
    	}
    }
    private void checkFieldsFromView() {
        assert tabEdiNr != null : "fx:id=\"tabEdiNr\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelKompoKomponten != null : "fx:id=\"tColSelKompoKomponten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlEdiNr != null : "fx:id=\"tColAuswahlEdiNr\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelSystemSystemName != null : "fx:id=\"tColSelSystemSystemName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabPaneObjekte != null : "fx:id=\"tabPaneObjekte\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelKompoPartner != null : "fx:id=\"tColSelKompoPartner\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelSystemPartnerName != null : "fx:id=\"tColSelSystemPartnerName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tableEdiNrAuswahl != null : "fx:id=\"tableEdiNrAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabSysteme != null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlGeschaeftsobjektName != null : "fx:id=\"tColAuswahlGeschaeftsobjektName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert btnDeleteEdiEintrag != null : "fx:id=\"btnDeleteEdiEintrag\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tablePartnerAuswahl != null : "fx:id=\"tablePartnerAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlEdiNrBezeichnung != null : "fx:id=\"tColAuswahlEdiNrBezeichnung\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tableKomponentenAuswahl != null : "fx:id=\"tableKomponentenAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tableGeschaeftsobjektAuswahl != null : "fx:id=\"tableGeschaeftsobjektAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlPartnerName != null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabKomponenten != null : "fx:id=\"tabKomponenten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelKompoSysteme != null : "fx:id=\"tColSelKompoSysteme\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabGeschaeftsobjekt != null : "fx:id=\"tabGeschaeftsobjekt\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert ediEintragSplitPane != null : "fx:id=\"ediEintragSplitPane\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tableSystemAuswahl != null : "fx:id=\"tableSystemAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabPartner != null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlPartnerSysteme != null : "fx:id=\"tColAuswahlPartnerSysteme\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert txtInfoZeile != null : "fx:id=\"txtInfoZeile\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert btnNewEdiNr != null : "fx:id=\"btnNewEdiNr\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelSystemKomponenten != null : "fx:id=\"tColSelSystemKomponenten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert komponenteSplitPane != null : "fx:id=\"komponenteSplitPane\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlGeschaeftsobjektAnzahl != null : "fx:id=\"tColAuswahlGeschaeftsobjektAnzahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
	}

    
}
