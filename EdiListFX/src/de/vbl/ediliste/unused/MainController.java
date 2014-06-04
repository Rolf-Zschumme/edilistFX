package de.vbl.ediliste.unused;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vbl.ediliste.controller.EdiEintragController;
import de.vbl.ediliste.controller.EdiKomponenteController;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.EdiPartner;
import de.vbl.ediliste.model.EdiSystem;
import de.vbl.ediliste.model.GeschaeftsObjekt;
//import de.vbl.ediliste.view.EdiNrListElement;
import de.vbl.ediliste.view.PartnerListElement;

public class MainController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";
//	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private ResourceBundle resources;
    @FXML private URL location;

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
    
    
//  private static String applName;
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    private ObservableList<PartnerListElement> ediPartnerList = FXCollections.observableArrayList();
    private ObservableList<EdiSystem> ediSystemList = FXCollections.observableArrayList();
    private ObservableList<EdiKomponente> ediKomponentenList = FXCollections.observableArrayList();
    private ObservableList<GeschaeftsObjekt> geschaeftsobjektList = FXCollections.observableArrayList();
    
//  private int maxEdiNr;
    private Stage primaryStage;
    private Pane ediEintragPane;
    private Pane ediKomponentePane;
    private Pane testAnchorPane;
    
    private EdiEintragController ediEintragController;
    private EdiKomponenteController ediKomponenteController;

    public void start(Stage stage, String applikationName) {
    	primaryStage = stage;
//    	applName = applikationName;
 //   	ediEintragController.setInitial(this, primaryStage, applName, em);
    }

    @FXML
    void btnUeber(ActionEvent event) {
//    	Dialogs.showInformationDialog(primaryStage, 
//    			"Version 0.5 - 08.05.2014 mit Geschäftsobjekt", 
//    			"VBL-Tool zur Verwaltung der EDI-Liste", applName);
    }
    
	/* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "EdiListe.fxml" 
     * ----------------------------------------------------------------------*/
    
    @FXML
    void initialize() {
    	setupEntityManager();
		FXMLLoader loader; 
		loader = new FXMLLoader(getClass().getResource("../view/EdiEintrag.fxml"));
		ediEintragPane = loadPane(loader);
		ediEintragController = loader.getController();
		
		loader = new FXMLLoader(getClass().getResource("../view/EdiKomponente.fxml"));
		ediKomponentePane = loadPane(loader);
		ediKomponenteController = loader.getController();
///		ediKomponenteController.setInitial(this, primaryStage, applName, em);

		
		loader = new FXMLLoader(getClass().getResource("../view/TestAnchorPane.fxml"));
		testAnchorPane = loadPane(loader);
		
    	checkFieldFromView();
        
    	loadEdiNrListData();
        setupBindings();

        tabPaneObjekte.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<Tab>() {
        			@Override
        			public void changed(ObservableValue<? extends Tab> ov, Tab talt, Tab tneu) {
        				final Tab akttab = tneu;
//        				primaryStage.getScene().setCursor(Cursor.WAIT);
//        				Task<Void> task = new Task<Void>() {
//        					@Override
//        					protected Void call() throws Exception {
//        						if (akttab.equals(tabPartner)) {
//        							loadPartnerListData();
//        						}
//        						else if(akttab.equals(tabSysteme)) {
//        							loadSystemListData();
//        						}
//        						else if(akttab.equals(tabKomponenten)) {
//        							loadKomponentenListData();
//        						}
//        						else if(akttab.equals(tabGeschaeftsobjekt)) {
//        							loadGeschaeftobjektListData();
//        						}
//        						return null;
//        					}
//        				};
//        				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//							@Override
//							public void handle(WorkerStateEvent event) {
//		        				primaryStage.getScene().setCursor(Cursor.DEFAULT);
//							}
//						});
//        				new Thread(task).start();
        				System.out.println("tabPane.changed() " + akttab);
						if (akttab.equals(tabEdiNr)) {
							showSplitPane(ediEintragSplitPane, ediEintragPane);
						}
						else if (akttab.equals(tabPartner)) {
							loadPartnerListData();
							showSplitPane(testAnchorPane);
						}
						else if(akttab.equals(tabSysteme)) {
							loadSystemListData();
							showSplitPane(testAnchorPane);
						}
						else if(akttab.equals(tabKomponenten)) {
							loadKomponentenListData();
							showSplitPane(ediKomponentePane);
						} 
						else if(akttab.equals(tabGeschaeftsobjekt)) {
							loadGeschaeftobjektListData();
							showSplitPane(testAnchorPane);
						}
        			}

				}
        );

//        tableEdiNrAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
//			public void handle(MouseEvent mouseEvent) {
//				if (ediEintragController.checkForContinueEditing()==true) {
//					mouseEvent.consume();
//				}
//			}
//		});
//
//		tableEdiNrAuswahl.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
//			public void handle(KeyEvent keyEvent) {
//				if (ediEintragController.checkForContinueEditing()==true) {
//					keyEvent.consume();
//				}
//			}
//		});
//        
//        tableEdiNrAuswahl.getSelectionModel().selectedItemProperty().addListener(
//        		
//    		new ChangeListener<EdiNrListElement>() {
//    			@Override
//    			public void changed(
//    					ObservableValue<? extends EdiNrListElement> observable,
//    					EdiNrListElement oldValue, EdiNrListElement newValue) {
//    				if (newValue != null) {
//    					ediEintragController.setSelection(em.find(EdiEintrag.class, newValue.getEdiId()));
//    					if (splitPane.getItems().contains(ediEintragPane) == false) {
//        					showSplitPane(ediEintragPane);	
//    					}
//    				}
//    				else {
//    					System.out.println("tableEdiNrAuswahl.changed() mit new=null old=" + oldValue);
//    					tableEdiNrAuswahl.getSelectionModel().clearSelection();
//    					showSplitPane(null);	
//    				}
//    			}
//    		}
//        );
        
        tableKomponentenAuswahl.getSelectionModel().selectedItemProperty().addListener(
    		new ChangeListener<EdiKomponente>() {
    			@Override
    			public void changed(
    					ObservableValue<? extends EdiKomponente> observable,
    					EdiKomponente oldValue, EdiKomponente newValue) {
    				if (newValue != null) {
//    					EdiKomponente k = em.find(EdiKomponente.class, newValue.getId());
    					if (ediKomponenteController == null) {
    						System.out.println("ediController ist null");
    					}
///    					ediKomponenteController.setSelection(newValue);
    					//toDo
    				}
    				else {
    					tableEdiNrAuswahl.getSelectionModel().clearSelection();
    					System.out.println("tableKomponentenAuswahl.changed() mit new=null old=" + oldValue);
    					showSplitPane(null);	
    				}
    			}
			}
    	);
    
     }

    private void showSplitPane(AnchorPane targetPane, Pane sourcePane) {
    	System.out.println("target:" + targetPane + " source:" + sourcePane);
    	System.out.println("Size:" + targetPane.getChildren().size());
    	if (targetPane.getChildren().size() == 0 ) {
			final StackPane sp = new StackPane();
			sp.getChildren().add(sourcePane);
			sourcePane.getChildren().add(sp);
    	}
    }

     private void showSplitPane(Pane pane) {
    	 
//    	System.out.println("Size:" + splitPane.getItems().size() + " pane:" +  pane);
//    	if (splitPane.getItems().size() < 2) {
//    		if (pane != null) {
//    			final StackPane sp = new StackPane();
//    			sp.getChildren().add(pane);
//    			splitPane.getItems().add(sp);
//    		}
//    	}
//   		else {
//   			splitPane.getItems().remove(1);
//			if (pane != null) {
//    			splitPane.getItems().add(pane);
//			}	
//   		}
     }

    
	 private AnchorPane loadPane(FXMLLoader loader) {
		 AnchorPane pane = null;
		 try {
			pane = (AnchorPane) loader.load();
		 } catch (IOException e) {
			e.printStackTrace();
		 }
		 return pane;
	}
    
    
    private void setupBindings() {
    	
        tableEdiNrAuswahl.setItems(ediNrArrayList);
    	tColAuswahlEdiNr.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediNr"));
    	tColAuswahlEdiNrBezeichnung.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("bezeichnung"));
    	
    	btnDeleteEdiEintrag.disableProperty().bind(
    			Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
//    	btnDeleteGeschaeftsobjekt.disableProperty().bind(
//    			Bindings.isNull(tableGeschaeftsobjektAuswahl.getSelectionModel().selectedItemProperty()));
    	tablePartnerAuswahl.setItems(ediPartnerList);
    	tColAuswahlPartnerName.setCellValueFactory(new PropertyValueFactory<PartnerListElement,String>("name"));
    	tColAuswahlPartnerSysteme.setCellValueFactory(new PropertyValueFactory<PartnerListElement,Integer>("anzSysteme"));
    	tColAuswahlPartnerKomponenten.setCellValueFactory(new PropertyValueFactory<PartnerListElement,Integer>("anzKomponenten"));
    	
    	tableSystemAuswahl.setItems(ediSystemList);
    	tColSelSystemSystemName.setCellValueFactory(new PropertyValueFactory<EdiSystem,String>("name"));
    	tColSelSystemPartnerName.setCellValueFactory(new PropertyValueFactory<EdiSystem,String>("partnerName"));
    	tColSelSystemKomponenten.setCellValueFactory(new PropertyValueFactory<EdiSystem,Integer>("AnzKomponenten"));
    	
    	tableKomponentenAuswahl.setItems(ediKomponentenList);
    	tColSelKompoKomponten.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("name"));
    	tColSelKompoSysteme.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("systemName"));
    	tColSelKompoPartner.setCellValueFactory(new PropertyValueFactory<EdiKomponente,String>("partnerName"));
    	
    	tableGeschaeftsobjektAuswahl.setItems(geschaeftsobjektList);
    	tColAuswahlGeschaeftsobjektName.setCellValueFactory(new PropertyValueFactory<GeschaeftsObjekt,String>("name"));
    	tColAuswahlGeschaeftsobjektAnzahl.setCellValueFactory(new PropertyValueFactory<GeschaeftsObjekt,Integer>("anzVerwendungen"));
	}

    public void refreshEdiNrListBezeichnung(Long aktId, String bez) {
    	Iterator<EdiNrListElement> it = ediNrArrayList.iterator();
    	while(it.hasNext()) {
    		EdiNrListElement e = it.next();
    		if (e.getEdiId() == aktId ) {
    			e.bezeichnungProperty().set(bez);
    			return;
    		}
    	}
    	System.out.println("Fehler: ID " + aktId + " nicht in Anzeigeliste");
    }
    
	private void loadEdiNrListData() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.bezeichnung FROM EdiEintrag e ORDER BY e.ediNr");
    	ediNrArrayList.clear();
//    	Integer max = 0;
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			ediNrArrayList.add(new EdiNrListElement( (Long) obj[0], (Integer) obj[1], (String) obj[2]));
//			max = (Integer) obj[1]; 
    	}	
//    	maxEdiNr = max;
	}
	
	private void loadPartnerListData() {
		ediPartnerList.clear();
    	Query query = em.createQuery("SELECT p FROM EdiPartner p ORDER BY p.name");
    	for (Object p : query.getResultList()) {
    		ediPartnerList.add(new PartnerListElement( (EdiPartner) p));
    	}
	}
	
	private void loadSystemListData() {
		ediSystemList.clear();
    	Query query = em.createQuery("SELECT s FROM EdiSystem s ORDER BY s.name");
    	for (Object s : query.getResultList()) {
    		ediSystemList.add((EdiSystem)s);
    	}
	}
	
	private void loadKomponentenListData() {
		ediKomponentenList.clear();
		Query query = em.createQuery("SELECT k FROM EdiKomponente k ORDER BY k.name");
		for (Object k : query.getResultList()) {
			ediKomponentenList.add((EdiKomponente)k);
		}	
	}
	
	private void loadGeschaeftobjektListData() {
		geschaeftsobjektList.clear();
		Query query = em.createQuery("SELECT g FROM GeschaeftsObjekt g ORDER BY g.name");
		for (Object g : query.getResultList()) {
			geschaeftsobjektList.add((GeschaeftsObjekt)g);
		}	
	}
	
    private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
	/* *****************************************************************************
	 * 
	 * ****************************************************************************/
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
    	
//    	NeuerEdiEintragController dialogController = loader.getController();
    	
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + 250);
    	dialog.setY(primaryStage.getY() + 100);
    	dialog.showAndWait();

//    	if (dialogController.getResponse() == DialogResponse.OK) {
//    		EdiEintrag newEE = dialogController.getNewEdiEintrag();
//			EdiNrListElement newListElement = new EdiNrListElement(newEE.getId(),newEE.getEdiNr(),newEE.getBezeichnung());
//			ediNrArrayList.add(newListElement);
//			if (newEE.getEdiNr() > maxEdiNr) 
//				maxEdiNr = newEE.getEdiNr();
//			tableEdiNrAuswahl.getSelectionModel().select(newListElement);
//    	}
    }    
    
    @FXML
    void deleteEdiEintrag(ActionEvent event) {
    	EdiNrListElement selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
//    		String ediNr = selectedlistElement.ediNrProperty().get();
//    		DialogResponse response = Dialogs.showConfirmDialog(primaryStage, 
//    				"EDI-Eintrag mit der Nr. " + ediNr + " wirklich löschen?",
//    				SICHERHEITSABFRAGE,"", DialogOptions.OK_CANCEL);
//    		if (response == DialogResponse.OK) {
//    			EdiEintrag ediEintrag = em.find(EdiEintrag.class, selectedlistElement.getEdiId());
//    			if (ediEintrag==null) {
//    				System.out.println("FEHLER: EDI-Eintrag " + ediNr + " ist nicht (mehr) gespeichert");
//    			}
//    			else {
//	        		em.getTransaction().begin();
//	        		Iterator<EdiEmpfaenger> empf = ediEintrag.getEdiEmpfaenger().iterator();
//	        		while (empf.hasNext()) {
//	        			em.remove(empf.next());
//	        			empf.remove();
//	        		}
//	        		em.remove(ediEintrag);
//	        		em.getTransaction().commit();
//	        		Dialogs.showInformationDialog(primaryStage, "Edi-Eintrag " + ediNr + " erfolgreich gelöscht");
//    			}	
//    			ediNrArrayList.remove(selectedlistElement);
//    			tableEdiNrAuswahl.getSelectionModel().clearSelection();
//    		}
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
//    		ExportToExcel export = new ExportToExcel(em);
//    		try {
//    			int lines = export.write(file);
//				Dialogs.showInformationDialog(primaryStage, 
//						lines + " Zeilen in der Datei " + file.getName() + 
//						" im Verzeichnis " + file.getParent() +" gespeichert",
//						"Hinweis", applName);
//			} catch (IOException e1) {
//				Dialogs.showErrorDialog(primaryStage, e1.getMessage(),
//				       "Fehler beim Speichern der Exportdatei", applName, e1);
//			}
    	}
    }
    
    private void checkFieldFromView() {
    	assert btnNewEdiNr != null : "fx:id=\"btnNewEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
    	assert btnDeleteEdiEintrag != null : "fx:id=\"btnDeleteEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tabPartner != null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'Main.fxml'.";
    	assert splitPane != null : "fx:id=\"splitPane\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tabSysteme != null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tabKomponenten != null : "fx:id=\"tabKomponenten\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tablePartnerAuswahl != null : "fx:id=\"tablePartnerAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tableSystemAuswahl != null : "fx:id=\"tableSystemAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tableKomponentenAuswahl != null : "fx:id=\"tableKomponentenAuswahl\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlEdiNr != null : "fx:id=\"tColAuswahlEdiNr\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlEdiNrBezeichnung != null : "fx:id=\"tColAuswahlEdiNrBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlGeschaeftsobjektName != null : "fx:id=\"tColAuswahlGeschaeftsobjektName\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlGeschaeftsobjektAnzahl != null : "fx:id=\"tColAuswahlGeschaeftsobjektAnzahl\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlPartnerSysteme != null : "fx:id=\"tColAuswahlPartnerSysteme\" was not injected: check your FXML file 'Main.fxml'.";
    	assert tColAuswahlPartnerName != null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelKompoKomponten != null : "fx:id=\"tColSelKompoKomponten\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelKompoPartner != null : "fx:id=\"tColSelKompoPartner\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelKompoSysteme != null : "fx:id=\"tColSelKompoSysteme\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelSystemPartnerName != null : "fx:id=\"tColSelSystemPartnerName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelSystemSystemName != null : "fx:id=\"tColSelSystemSystemName\" was not injected: check your FXML file 'Main.fxml'.";
        assert tColSelSystemKomponenten != null : "fx:id=\"tColSelSystemKomponenten\" was not injected: check your FXML file 'Main.fxml'.";
    }
}
