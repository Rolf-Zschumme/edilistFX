package de.vbl.ediliste.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.model.GeschaeftsObjekt;
import de.vbl.ediliste.tools.ExportToExcel;
import de.vbl.ediliste.view.PartnerListElement;

public class EdiMainController {
	private static final String APPL_NAME = "EDI-Liste";
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private TextField txtInfoZeile;
    @FXML private TabPane tabPaneObjekte;    
    @FXML private Tab tabEdiNr;
    @FXML private TableView<EdiEintrag> tableEdiNrAuswahl;
    @FXML private TableColumn<EdiEintrag, String> tColAuswahlEdiNr;
    @FXML private TableColumn<EdiEintrag, String> tColAuswahlEdiNrBezeichnung;

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
    
    @FXML private Pane ediEintrag;
    @FXML private Pane ediKomponente;
    
    @FXML private EdiKomponenteController ediKomponenteController ;
    @FXML private EdiEintragController ediEintragController;
    
	private static String applName;
	private Stage primaryStage;
    private EntityManager entityManager;
    private ObservableList<EdiEintrag> ediEintraegeList = FXCollections.observableArrayList();
    private ObservableList<EdiKomponente> ediKomponentenList = FXCollections.observableArrayList();    
    private int maxEdiNr;

    @FXML
	private void initialize () {
		System.out.println("EdiMainController.initialize()");
		System.out.println("	ediEintragController   :" + ediEintragController);
		System.out.println("	ediEintrag             :" + ediEintrag);
		System.out.println("	ediKomponenteController:" + ediKomponenteController);
		System.out.println("	ediKomponente          :" + ediKomponente);
		
		checkFieldsFromView();
		setupEntityManager();
    	loadEdiEintragListData();
        setupBindings();		
    }	

    public void start(Stage stage) {
    	System.out.println("EdiMainController.start() stage:"+stage);
    	primaryStage = stage;
    	primaryStage.setTitle(APPL_NAME);
    	EdiEintragController.setPrimaryStage(primaryStage);
//    	ediEintragController.setInitial(this, primaryStage, applName, entityManager);
    }
    
    private void setupBindings() {
    	
    	setupEdiEintragPane();
    	setupKomponentenPane();
    	
//    	btnDeleteGeschaeftsobjekt.disableProperty().bind(
//    			Bindings.isNull(tableGeschaeftsobjektAuswahl.getSelectionModel().selectedItemProperty()));
//    	tablePartnerAuswahl.setItems(ediPartnerList);
//    	tColAuswahlPartnerName.setCellValueFactory(new PropertyValueFactory<PartnerListElement,String>("name"));
//    	tColAuswahlPartnerSysteme.setCellValueFactory(new PropertyValueFactory<PartnerListElement,Integer>("anzSysteme"));
//    	tColAuswahlPartnerKomponenten.setCellValueFactory(new PropertyValueFactory<PartnerListElement,Integer>("anzKomponenten"));
//    	
//    	tableSystemAuswahl.setItems(ediSystemList);
//    	tColSelSystemSystemName.setCellValueFactory(new PropertyValueFactory<EdiSystem,String>("name"));
//    	tColSelSystemPartnerName.setCellValueFactory(new PropertyValueFactory<EdiSystem,String>("partnerName"));
//    	tColSelSystemKomponenten.setCellValueFactory(new PropertyValueFactory<EdiSystem,Integer>("AnzKomponenten"));
//    	
//    	tableGeschaeftsobjektAuswahl.setItems(geschaeftsobjektList);
//    	tColAuswahlGeschaeftsobjektName.setCellValueFactory(new PropertyValueFactory<GeschaeftsObjekt,String>("name"));
//    	tColAuswahlGeschaeftsobjektAnzahl.setCellValueFactory(new PropertyValueFactory<GeschaeftsObjekt,Integer>("anzVerwendungen"));
    	
        tabPaneObjekte.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<Tab>() {
        			@Override
        			public void changed(ObservableValue<? extends Tab> ov, Tab talt, Tab tneu) {
        				final Tab akttab = tneu;
        				primaryStage.getScene().setCursor(Cursor.WAIT);
        				Task<Void> task = new Task<Void>() {
        					@Override
        					protected Void call() throws Exception {
//        						if (akttab.equals(tabPartner)) {
//        							loadPartnerListData();
//        						}
//        						else if(akttab.equals(tabSysteme)) {
//        							loadSystemListData();
//        						}
        						if(akttab.equals(tabKomponenten)) {
        							loadKomponentenListData();
        						}
//        						else if(akttab.equals(tabGeschaeftsobjekt)) {
//        							loadGeschaeftobjektListData();
//        						}
        						return null;
        					}
        				};
        				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
							@Override
							public void handle(WorkerStateEvent event) {
		        				primaryStage.getScene().setCursor(Cursor.DEFAULT);
							}
						});
        				new Thread(task).start();
        				System.out.println("tabPane.changed() " + akttab);
//						if (akttab.equals(tabEdiNr)) {
//							showSplitPane(ediEintragSplitPane, ediEintragPane);
//						}
//						else if (akttab.equals(tabPartner)) {
//							loadPartnerListData();
//							showSplitPane(testAnchorPane);
//						}
//						else if(akttab.equals(tabSysteme)) {
//							loadSystemListData();
//							showSplitPane(testAnchorPane);
//						}
//						else if(akttab.equals(tabKomponenten)) {
//							loadKomponentenListData();
//							showSplitPane(ediKomponentePane);
//						} 
//						else if(akttab.equals(tabGeschaeftsobjekt)) {
//							loadGeschaeftobjektListData();
//							showSplitPane(testAnchorPane);
//						}
        			}
				}
        ); 		
    	
    }

    private void setupEdiEintragPane() {
    	tableEdiNrAuswahl.setItems(ediEintraegeList);
    	tColAuswahlEdiNr.setCellValueFactory(new PropertyValueFactory<EdiEintrag,String>("ediNr"));
    	tColAuswahlEdiNrBezeichnung.setCellValueFactory(new PropertyValueFactory<EdiEintrag,String>("bezeichnung"));
    	
    	btnDeleteEdiEintrag.disableProperty().bind(
    			Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	ediEintrag.disableProperty().bind(
    			Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	
    	ediEintragController.setEntityManager(entityManager);
    	ediEintragController.ediEintragProperty().bind(
    			tableEdiNrAuswahl.getSelectionModel().selectedItemProperty());
    	
    }
    
	private void setupKomponentenPane() {
    	tableKomponentenAuswahl.setItems(ediKomponentenList);
    	tColSelKompoKomponten.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("name"));
    	tColSelKompoSysteme.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("systemName"));
    	tColSelKompoPartner.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("partnerName"));
    	
    	ediKomponenteController.setEntityManager(entityManager);
    	ediKomponenteController.komponenteProperty().bind(
    			tableKomponentenAuswahl.getSelectionModel().selectedItemProperty());
    	
    	ediKomponente.disableProperty().bind(
    			Bindings.isNull(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty()));
    	
	}

	private void loadEdiEintragListData() {
    	ediEintraegeList.clear();
    	TypedQuery<EdiEintrag> tq = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e ORDER BY e.ediNr", EdiEintrag.class);
		List<EdiEintrag> ediList = tq.getResultList();
		ediEintraegeList.addAll(ediList);
		maxEdiNr = 0;
		for(EdiEintrag e : ediList ) {
	    	if (e.getEdiNr() > maxEdiNr) maxEdiNr = e.getEdiNr();
		}
	}
        	    
	private void loadKomponentenListData() {
		ediKomponentenList.clear();
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k ORDER BY k.name", EdiKomponente.class);
		ediKomponentenList.addAll(tq.getResultList());
		
//		Query query = entityManager.createQuery("SELECT k FROM EdiKomponente k ORDER BY k.name");
//		for (Object k : query.getResultList()) {
//			ediKomponentenList.add((EdiKomponente)k);
//		}	
	}
	
	@FXML
    void btnUeber(ActionEvent event) {
		Dialogs.create()
			.owner(primaryStage).title(applName)
			.masthead("VBL-Tool zur Verwaltung der EDI-Liste")
			.message("Version 0.5 - 08.05.2014 mit Gesch�ftsobjekt")
			.showInformation();
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

    	if (dialogController.getResponse() == Dialog.Actions.OK) {
    		EdiEintrag newEE = dialogController.getNewEdiEintrag();
			ediEintraegeList.add(newEE);
			if (newEE.getEdiNr() > maxEdiNr) 
				maxEdiNr = newEE.getEdiNr();
			tableEdiNrAuswahl.getSelectionModel().select(newEE);
    	}
    }    

    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	entityManager = factory.createEntityManager();
    }
    
    @FXML    
    void deleteEdiEintrag(ActionEvent event) {
    	EdiEintrag selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		String ediNr = Integer.toString(selectedlistElement.ediNrProperty().get());
    		Action response = Dialogs.create()
    				.owner(primaryStage).title(applName)
    				.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
    				.masthead(SICHERHEITSABFRAGE)
    				.message("EDI-Eintrag mit der Nr. " + ediNr + " wirklich l�schen?")
    				.showConfirm();
    		if (response == Dialog.Actions.OK) {
    			EdiEintrag ediEintrag = entityManager.find(EdiEintrag.class, selectedlistElement.getId());
    			if (ediEintrag==null) {
    				System.out.println("FEHLER: EDI-Eintrag " + ediNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		entityManager.getTransaction().begin();
	        		Iterator<EdiEmpfaenger> empf = ediEintrag.getEdiEmpfaenger().iterator();
	        		while (empf.hasNext()) {
	        			entityManager.remove(empf.next());
	        			empf.remove();
	        		}
	        		entityManager.remove(ediEintrag);
	        		entityManager.getTransaction().commit();
	        		Dialogs.create().owner(primaryStage).masthead(null)
	        				.message("Edi-Eintrag " + ediNr + " erfolgreich gel�scht")
	        				.showInformation();
    			}	
    			ediEintraegeList.remove(selectedlistElement);
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
    		ExportToExcel export = new ExportToExcel(entityManager);
    		try {
    			int lines = export.write(file);
    			Dialogs.create().owner(applName).title(applName)
    					.masthead(null)
    					.message(lines + " Zeilen in der Datei " + file.getName() + 
    							" im Verzeichnis " + file.getParent() +" gespeichert")
    					.showInformation();		
			} catch (IOException e1) {
				Dialogs.create().owner(primaryStage).title(applName)
						.masthead("FEHLER")
						.message("Fehler beim Speichern der Exportdatei")
						.showException(e1);
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