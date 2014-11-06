package de.vbl.im.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import javax.persistence.CacheStoreMode; 
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.im.controller.subs.DokumentAuswaehlenController;
import de.vbl.im.controller.subs.KontaktPersonAuswaehlenController;
import de.vbl.im.model.EdiEintrag;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.EdiKomponente;
import de.vbl.im.model.EdiPartner;
import de.vbl.im.model.EdiSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.Integration;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.KontaktPerson;
import de.vbl.im.tools.ExportToExcel;

public class EdiMainController {
	private static final Logger logger = LogManager.getLogger(EdiMainController.class.getName()); 
	private static final String APPL_NAME = "Integration Manager";
	private static final String PERSISTENCE_UNIT_NAME = "IntegrationManager";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

	private static String dbName;
	private static int maxEdiNr;
	private static Stage primaryStage;
    private EntityManager entityManager;
    private ObservableList<EdiEintrag> ediEintraegeList = FXCollections.observableArrayList();
    private ObservableList<EdiPartner> ediPartnerList = FXCollections.observableArrayList();    
    private ObservableList<EdiSystem> ediSystemList = FXCollections.observableArrayList();
    private ObservableList<EdiKomponente> ediKomponentenList = FXCollections.observableArrayList();
    private ObservableList<Integration> integrationList = FXCollections.observableArrayList();
    private ObservableList<Konfiguration> konfigurationList = FXCollections.observableArrayList();
    private ObservableList<KontaktPerson> kontaktPersonList = FXCollections.observableArrayList();
    private ObservableList<GeschaeftsObjekt> geschaeftsobjektList = FXCollections.observableArrayList();    

	
    @FXML private TextField txtInfoZeile;
    @FXML private TabPane tabPaneObjekte;
    
    @FXML private Tab tabEdiNr;
    @FXML private TableView<EdiEintrag> tableEdiNrAuswahl;
    @FXML private TableColumn<EdiEintrag, String> tColAuswahlEdiNr;
    @FXML private TableColumn<EdiEintrag, String> tColAuswahlEdiNrSender;
    @FXML private TableColumn<EdiEintrag, String> tColAuswahlEdiNrIngration;
    @FXML private TableColumn<EdiEintrag, String> tColAuswahlEdiNrBezeichnung;

    @FXML private Tab tabPartner;
    @FXML private TableView<EdiPartner> tablePartnerAuswahl;
    @FXML private TableColumn<EdiPartner, String> tColAuswahlPartnerName;
    @FXML private TableColumn<EdiPartner, String> tColAuswahlPartnerSysteme;
    @FXML private TableColumn<EdiPartner, String> tColAuswahlPartnerKomponenten;

    @FXML private Tab tabSysteme;
    @FXML private TableView<EdiSystem> tableSystemAuswahl;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemSystemName;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemPartnerName;
    @FXML private TableColumn<EdiSystem, String> tColSelSystemKomponenten;

    @FXML private Tab tabKomponenten;
    @FXML private TableView<EdiKomponente> tableKomponentenAuswahl;   
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoKomponten;
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoSysteme;
    @FXML private TableColumn<EdiKomponente, String> tColSelKompoPartner;
    
    @FXML private Tab tabIntegrationen;
    @FXML private TableView<Integration> tableIntegrationAuswahl;
    @FXML private TableColumn<Integration, String> tColSelIntegrationName;
    @FXML private TableColumn<Integration, String> tColSelIntegrationKonfigurationAnzahl;
    
    @FXML private Tab tabKonfigurationen;
    @FXML private TableView<Konfiguration> tableKonfigurationAuswahl;
    @FXML private TableColumn<Konfiguration, String> tColSelKonfigurationName;
    @FXML private TableColumn<Konfiguration, String> tColSelKonfigIntegrationName;
    
    @FXML private Tab tabKontaktPersonen;
    @FXML private TableView<KontaktPerson> tableKontaktAuswahl;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktUserId;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktNachname;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktVorname;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktAbteilung;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktTelefon;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktMailadresse;
    
    @FXML private Tab tabGeschaeftsobjekte;
    @FXML private TableView<GeschaeftsObjekt> tableGeschaeftsobjektAuswahl;
    @FXML private TableColumn<GeschaeftsObjekt, String> tColAuswahlGeschaeftsobjektName;
    
    @FXML private Button btnNewEdiNr;
    @FXML private Button btnDeleteEdiEintrag;
//  @FXML private Button btnDeleteGeschaeftsobjekt;
    @FXML private Button btnExportExcel;

    @FXML private Pane ediEintrag;
    @FXML private Pane ediPartner;
    @FXML private Pane ediSystem;
    @FXML private Pane ediKomponente;
    @FXML private Pane integration;
    @FXML private Pane konfiguration;
    @FXML private Pane kontaktPerson;
    @FXML private Pane geschaeftsObjekt;
    
    @FXML private EdiEintragController ediEintragController;
    @FXML private EdiPartnerController ediPartnerController;
    @FXML private EdiSystemController ediSystemController;
    @FXML private EdiKomponenteController ediKomponenteController;
    @FXML private IntegrationController integrationController;
    @FXML private KonfigurationController konfigurationController;
    @FXML private KontaktPersonController kontaktPersonController;
    @FXML private GeschaeftsObjektController geschaeftsObjektController;   
    
    @FXML
	private void initialize () throws Exception {
    	logger.info("entered");
		checkFieldsFromView();
		setupEntityManager();
        setupBindings();
    }	

    public void start(Stage stage) {
    	primaryStage = stage;
    	primaryStage.setTitle(APPL_NAME);
    	
    	EdiEintragController.setParent(this);
    	EdiPartnerController.setParent(this);
    	EdiSystemController.setParent(this);
    	EdiKomponenteController.setParent(this);
    	IntegrationController.setParent(this);
    	KonfigurationController.setParent(this);
    	KontaktPersonController.setParent(this);
    	GeschaeftsObjektController.setParent(this);
        
    	// Check for data changes on close request from MainWindow
    	primaryStage.setOnCloseRequest(event -> {
    		if (checkAllOk() == false) {
    			event.consume();
    		}
    	});
    }
    
	public void setPrimaryStage(Stage primaryStage) {
		EdiMainController.primaryStage = primaryStage;
	}
	
    static public Stage getStage() {
    	return primaryStage;
    }
    	
    public EntityManager getEntityManager() {
    	return entityManager;
    }
    
    public void setInfoText(String txt) {
    	txtInfoZeile.setStyle("-fx-font-weight: normal;-fx-text-fill: black");
    	txtInfoZeile.setText(txt);
    }
    
    public void setErrorText(String txt) {
    	txtInfoZeile.setStyle("-fx-font-weight: bold;-fx-text-fill: red");
    	txtInfoZeile.setText(txt);
    }
    
    private void setupBindings() {
    	loadEdiEintragListData();
    	setupEdiEintragPane();
    	setupEdiPartnerPane();
    	setupEdiSystemPane();
    	setupKomponentenPane();
    	setupIntegrationPane();
    	setupKonfigurationPane();
    	setupKontaktPersonPane();
    	setupGeschaeftsobjektPane();

        tabPaneObjekte.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<Tab>() {
        			@Override
        			public void changed(ObservableValue<? extends Tab> ov, Tab talt, Tab tneu) {
        				final Tab akttab = tneu;
						logger.info("tabPane.changed", akttab.textProperty().get());

						primaryStage.getScene().setCursor(Cursor.WAIT);
						
						if (akttab.equals(tabEdiNr)) {
							loadEdiEintragListData();
						}
						else if (akttab.equals(tabPartner)) {
							loadPartnerListData();
						}
						else if(akttab.equals(tabSysteme)) {
							loadSystemListData();
						}
						else if(akttab.equals(tabKomponenten)) {
							loadKomponentenListData();
						}
						else if(akttab.equals(tabIntegrationen)) {
							loadIntegrationListData();
						}
						else if(akttab.equals(tabKonfigurationen)) {
							loadKonfigurationListData();
						}
						else if(akttab.equals(tabKontaktPersonen)) {
							loadKontaktPersonListData();
						}
						else if(akttab.equals(tabGeschaeftsobjekte)) {
							loadGeschaeftobjektListData();
						}
        				primaryStage.getScene().setCursor(Cursor.DEFAULT);
        			}

				}
        );
        tableEdiNrAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkEdiEintrag(e));
        tableEdiNrAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkEdiEintrag(e));

        tablePartnerAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkPartner(e));
        tablePartnerAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkPartner(e));
        
        tableSystemAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkSystem(e));
        tableSystemAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkSystem(e));
        
        tableKomponentenAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkKomponente(e));
		tableKomponentenAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkKomponente(e));
		
		tableIntegrationAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkIntegration(e));
		tableIntegrationAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkIntegration(e));
		
		tableKonfigurationAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkKonfiguration(e));
		tableKonfigurationAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkKonfiguration(e));
		
		tableKontaktAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkKontaktPerson(e));
		tableKontaktAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkKontaktPerson(e));
		
		tableGeschaeftsobjektAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkGeschaeftsObjekt(e));
		tableGeschaeftsobjektAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkGeschaeftsObjekt(e));
		
    }
    
	private void checkEdiEintrag(Event event) {
    	if(ediEintragController.checkForChangesAndAskForSave() == false) {
    		event.consume();
    	}
    }
    
	private void checkPartner(Event event) {
		if (ediPartnerController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}
	
	private void checkSystem(Event event) {
		if (ediSystemController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private void checkKomponente(Event event) {
		if (ediKomponenteController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}
	
	private void checkIntegration(Event event) {
		if (integrationController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private void checkKonfiguration(Event event) {
		if (konfigurationController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private void checkGeschaeftsObjekt(Event event) {
		if (geschaeftsObjektController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private void checkKontaktPerson(Event event) {
		if (kontaktPersonController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private boolean checkAllOk() {
    	return ediEintragController.checkForChangesAndAskForSave() || 
    		   ediPartnerController.checkForChangesAndAskForSave() ||
    	       ediSystemController.checkForChangesAndAskForSave()  ||
    	       ediKomponenteController.checkForChangesAndAskForSave() ||
    		   integrationController.checkForChangesAndAskForSave()   ||
    		   konfigurationController.checkForChangesAndAskForSave() ||
    		   kontaktPersonController.checkForChangesAndAskForSave() ||
    		   geschaeftsObjektController.checkForChangesAndAskForSave(); 
	}
	
	// Aufruf "Beenden" via Menue
	@FXML
	void onActionCLose(ActionEvent event) {
		if (checkAllOk() == true) {
			primaryStage.close();
		}
	}
	
	@FXML
	void actionEdiNrContextMenuRequested (Event event) {
		// TODO
		System.out.println("Contextmenu for EdiNr-Table requested");
	}
	
	/* ************************************************************************
	 * set a new selected EdiEintrag from other controllers
	 * ***********************************************************************/
	protected void setSelectedEdiEintrag (EdiEintrag e) {
		tableEdiNrAuswahl.getSelectionModel().select(e);
	}
    
	private void setupEdiEintragPane() {
    	tableEdiNrAuswahl.setItems(ediEintraegeList);
    	tColAuswahlEdiNr.setCellValueFactory(cellData -> Bindings.format(EdiEintrag.FORMAT_EDINR, 
    			cellData.getValue().ediNrProperty()));
    	tColAuswahlEdiNrSender.setCellValueFactory(cellData -> 
    			cellData.getValue().senderNameProperty());
    	tColAuswahlEdiNrBezeichnung.setCellValueFactory(cellData -> 
    			cellData.getValue().bezeichnungProperty());
    	tColAuswahlEdiNrIngration.setCellValueFactory(cellData -> 
    			cellData.getValue().intregrationName());

    	btnDeleteEdiEintrag.disableProperty().bind(
    			Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	ediEintrag.disableProperty().bind(
    			Bindings.isNull(tableEdiNrAuswahl.getSelectionModel().selectedItemProperty()));
    	ediEintragController.ediEintragProperty().bind(
    						    tableEdiNrAuswahl.getSelectionModel().selectedItemProperty());

   	
    	tableEdiNrAuswahl.setRowFactory(new Callback<TableView<EdiEintrag>, TableRow<EdiEintrag>>() {
			@Override
			public TableRow<EdiEintrag> call(TableView<EdiEintrag> table) {
				final TableRow<EdiEintrag> row = new TableRow<EdiEintrag>();
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem removeMenuItem = new MenuItem("Löschen");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						ediEintragLoeschen();
//						table.getItems().remove(row.getItem());
					}
				});
				contextMenu.getItems().add(removeMenuItem);
				row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu)null).otherwise(contextMenu));
				return row;
			}
		});
    }
	
	private void setupEdiPartnerPane() {
		tablePartnerAuswahl.setItems(ediPartnerList);
		tColAuswahlPartnerName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColAuswahlPartnerSysteme.setCellValueFactory(cellData -> Bindings.format("%7d", cellData.getValue().anzSystemeProperty()));
		tColAuswahlPartnerKomponenten.setCellValueFactory(cell -> Bindings.format("%7d", cell.getValue().anzKomponentenProperty()));
		
		ediPartnerController.ediPartnerProperty().bind(tablePartnerAuswahl.getSelectionModel().selectedItemProperty());
		ediPartner.disableProperty().bind(Bindings.isNull(tablePartnerAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
	private void setupEdiSystemPane() {
		tableSystemAuswahl.setItems(ediSystemList);
		tColSelSystemSystemName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColSelSystemPartnerName.setCellValueFactory(cellData -> cellData.getValue().getEdiPartner().nameProperty());
		tColSelSystemKomponenten.setCellValueFactory(cellData -> Bindings.format("%7d", cellData.getValue().anzKomponentenProperty()));
		
		ediSystemController.ediSystemProperty().bind(tableSystemAuswahl.getSelectionModel().selectedItemProperty());
		ediSystem.disableProperty().bind(Bindings.isNull(tableSystemAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
	private void setupKomponentenPane() {
		tableKomponentenAuswahl.setItems(ediKomponentenList);
    	tColSelKompoKomponten.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    	tColSelKompoSysteme.setCellValueFactory(cellData -> cellData.getValue().getEdiSystem().nameProperty());
    	tColSelKompoPartner.setCellValueFactory(cellData -> cellData.getValue().getEdiSystem().getEdiPartner().nameProperty());

    	ediKomponenteController.komponenteProperty().bind(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty());
    	ediKomponente.disableProperty().bind(Bindings.isNull(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupIntegrationPane() {
		tableIntegrationAuswahl.setItems(integrationList);
		tColSelIntegrationName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		
		integrationController.integrationProperty().bind(tableIntegrationAuswahl.getSelectionModel().selectedItemProperty());
		integration.disableProperty().bind(Bindings.isNull(tableIntegrationAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupKonfigurationPane() {
		tableKonfigurationAuswahl.setItems(konfigurationList);
		tColSelKonfigurationName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColSelKonfigIntegrationName.setCellValueFactory(cd -> cd.getValue().integrationNameProperty());
		
		konfigurationController.konfigurationProperty().bind(tableKonfigurationAuswahl.getSelectionModel().selectedItemProperty());
		konfiguration.disableProperty().bind(Bindings.isNull(tableKonfigurationAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupKontaktPersonPane() {
		tableKontaktAuswahl.setItems(kontaktPersonList);
		tColKontaktUserId.setCellValueFactory(cellData -> cellData.getValue().nummerProperty());
		tColKontaktNachname.setCellValueFactory(cellData -> cellData.getValue().nachnameProperty());
		tColKontaktVorname.setCellValueFactory(cellData -> cellData.getValue().vornameProperty());
		tColKontaktAbteilung.setCellValueFactory(cellData -> cellData.getValue().abteilungProperty());
		tColKontaktTelefon.setCellValueFactory(cellData -> cellData.getValue().telefonProperty());
		tColKontaktMailadresse.setCellValueFactory(cellData -> cellData.getValue().mailProperty());
		
		kontaktPersonController.kontaktPersonProperty().bind(tableKontaktAuswahl.getSelectionModel().selectedItemProperty());
		kontaktPerson.disableProperty().bind(Bindings.isNull(tableKontaktAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
    private void setupGeschaeftsobjektPane() {
    	tableGeschaeftsobjektAuswahl.setItems(geschaeftsobjektList);
    	tColAuswahlGeschaeftsobjektName.setCellValueFactory(cell -> cell.getValue().nameProperty());
    	
    	geschaeftsObjektController.geschaeftsObjektProperty().bind(tableGeschaeftsobjektAuswahl.getSelectionModel().selectedItemProperty());
    	geschaeftsObjekt.disableProperty().bind(Bindings.isNull(tableGeschaeftsobjektAuswahl.getSelectionModel().selectedItemProperty()));
    }
    
	protected void loadEdiEintragListData() {
    	TypedQuery<EdiEintrag> tq = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e ORDER BY e.ediNr", EdiEintrag.class);
		List<EdiEintrag> aktuList = tq.getResultList();

		ediEintraegeList.retainAll(aktuList);
		maxEdiNr = 0;
		for(EdiEintrag e : aktuList ) {
			if (ediEintraegeList.contains(e) == false) {
				ediEintraegeList.add(aktuList.indexOf(e), e);
			}
	    	if (e.getEdiNr() > maxEdiNr) maxEdiNr = e.getEdiNr();
		}
	}
	
	protected void loadPartnerListData() {
		TypedQuery<EdiPartner> tq = entityManager.createQuery(
				"SELECT p FROM EdiPartner p ORDER BY p.name", EdiPartner.class);
		List<EdiPartner> aktuList = tq.getResultList(); 

		ediPartnerList.retainAll(aktuList);  // remove all delete entities  

		for ( EdiPartner p : aktuList) {     // insert or update all entities
			if (ediPartnerList.contains(p) == false) {
				ediPartnerList.add(aktuList.indexOf(p), p);
			} else {
//				log("loadPartnerListData","name:" + p.getName() );
				int aktPos = ediPartnerList.indexOf(p);
				EdiPartner p1 = ediPartnerList.get(aktPos);
//				p1.anzSystemeProperty().set(p.anzSystemeProperty().get());
				p1.anzKomponentenProperty().set(p.anzKomponentenProperty().get());
			}
		}
	}

	protected void loadSystemListData() {
		TypedQuery<EdiSystem> tq = entityManager.createQuery(
				"SELECT s FROM EdiSystem s ORDER BY s.name", EdiSystem.class);
		List<EdiSystem> aktuList = tq.getResultList();
		ediSystemList.retainAll(aktuList);  // remove all delete entities  
		for (EdiSystem s : aktuList) {		// insert or update all entities
			if (ediSystemList.contains(s) == false) {
				ediSystemList.add(aktuList.indexOf(s), s);
//			} else {
//				int aktPos = ediSystemList.indexOf(s);
//				EdiSystem s1 = ediSystemList.get(aktPos);
//				s1.anzKomponentenProperty().set(s.anzKomponentenProperty().get());
			}
		}
	}

	protected void loadKomponentenListData() {
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k ORDER BY k.name", EdiKomponente.class);
		List<EdiKomponente> aktuList = tq.getResultList();
		
		ediKomponentenList.retainAll(aktuList); // remove delete entities  
		for ( EdiKomponente k : aktuList) {     // and insert new entities
			if (ediKomponentenList.contains(k) == false) {
				ediKomponentenList.add(aktuList.indexOf(k), k);
			}
		}
	}

	protected void loadIntegrationListData() {
		TypedQuery<Integration> tq = entityManager.createQuery(
				"SELECT i FROM Integration i ORDER BY i.name", Integration.class);
		List<Integration> aktuList = tq.getResultList();
		
		integrationList.retainAll(aktuList); // remove delete entities
		for (Integration i : aktuList) {	 // and insert new entities
			if (integrationList.contains(i) == false) {
				integrationList.add(aktuList.indexOf(i),i);
			}
		}
	}

	protected void loadKonfigurationListData() {
		TypedQuery<Konfiguration> tq = entityManager.createQuery(
				"SELECT k FROM Konfiguration k ORDER BY k.name", Konfiguration.class);
		List<Konfiguration> aktuList = tq.getResultList();
		
		konfigurationList.retainAll(aktuList); // remove delete entities
		for (Konfiguration k : aktuList) {	 // and insert new entities
			if (konfigurationList.contains(k) == false) {
				konfigurationList.add(aktuList.indexOf(k),k);
			}
		}
	}

	protected void loadKontaktPersonListData() {
		TypedQuery<KontaktPerson> tq = entityManager.createQuery(
				"SELECT k FROM KontaktPerson k ORDER BY k.nachname", KontaktPerson.class);
		List<KontaktPerson> aktulist = tq.getResultList();
		kontaktPersonList.retainAll(aktulist);
		for (KontaktPerson k : aktulist) {
			if (kontaktPersonList.contains(k) == false ) {
				kontaktPersonList.add(aktulist.indexOf(k), k);
			}
		}
	}

	protected void loadGeschaeftobjektListData() {
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(
				"SELECT g FROM GeschaeftsObjekt g ORDER BY g.name", GeschaeftsObjekt.class);
		List<GeschaeftsObjekt> aktuList = tq.getResultList();
		geschaeftsobjektList.retainAll(aktuList);
		for (GeschaeftsObjekt g : aktuList) {
			if (geschaeftsobjektList.contains(g) == false ) {
				geschaeftsobjektList.add(aktuList.indexOf(g),g);
			}
		}
	}

	@FXML
    void btnUeber(ActionEvent event) {
		Dialogs.create()
			.owner(primaryStage).title(APPL_NAME)
			.masthead("VBL-Tool zur Verwaltung der Datentransfers")
			.message("\nProgramm-Version 1.0.0 - 04.11.2014\n" +
					 "\nDatenbank-Name: " + dbName +
			   	     "\nJava-Runtime-Verion: " + System.getProperty("java.version"))
			.showInformation();
    }
	
	
	@FXML
	void actionPartnerNew(ActionEvent event) throws Exception {
		setInfoText("Zukünftige Funktion - Bitte Bedarf beim CAB melden");
	}
	
	@FXML
	void actionSystemNew(ActionEvent event) throws Exception {
		setInfoText("Zukünftige Funktion - Bitte Bedarf beim CAB melden");
	}
		
	@FXML
	void actionKomponenteNew(ActionEvent event) throws Exception {
		setInfoText("Zukünftige Funktion - Bitte Bedarf beim CAB melden");
	}
	
//	@FXML
//	void actionBearbeiten(ActionEvent event) throws Exception {
//		logger.info("entered");
//		throw (new Exception("Test Exception (actionBearbeiten)"));
//	}
	
	@FXML
	void showJavaInfo (ActionEvent event) {
		String javaVersion = "Java-Version: " + System.getProperty("java.version");
		String javaHome = System.getProperty("java.home");
		String classpath = System.getProperty("java.class.path");
		String [] classpathEntries = classpath.split(File.pathSeparator);
		String message = "Java-Home:\n\t" + javaHome + "\n\nJava-Classpath:";
		for ( String l : classpathEntries) {
			message += "\n" + l;
		}
		Dialogs.create().title(APPL_NAME).masthead(javaVersion).message(message).showInformation();
	}

    @FXML
    void newEdiNr(ActionEvent event) {
    
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("/de/vbl/ediliste/view/NeuerEdiEintrag.fxml"));
    	if (loader.getLocation()==null) {
    		logger.error("Resource not found");
    		setErrorText("Programm-FEHLER: Resource not found");
    		return;
    	}
    	try {
			loader.load();
		} catch (IOException e) {
			logger.error("Fehler beim Laden der Resource (fxml)");
			e.printStackTrace();
		}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	
    	NeuerEdiEintragController dialogController = loader.getController();
    	dialogController.setEntityManager(entityManager);
    	dialogController.start();
    	
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

    @FXML    
    void handleEdiEintragLoeschen(ActionEvent event) {
    	ediEintragLoeschen();
    }
    private void ediEintragLoeschen() {
    	EdiEintrag selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		String ediNr = Integer.toString(selectedlistElement.ediNrProperty().get());
    		Action response = Dialog.Actions.OK;
    		if (selectedlistElement.getEdiKomponente() != null) {
    			response = Dialogs.create()
					.owner(primaryStage).title(APPL_NAME)
					.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
					.masthead(SICHERHEITSABFRAGE)
					.message("EDI-Eintrag mit der Nr. " + ediNr + " wirklich löschen?")
					.showConfirm();
    		}
    		if (response == Dialog.Actions.OK) {
    			EdiEintrag ediEintrag = entityManager.find(EdiEintrag.class, selectedlistElement.getId());
    			if (ediEintrag==null) {
    				logger.error("deleteEdiEintrag", "FEHLER: EDI-Eintrag " + ediNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		entityManager.getTransaction().begin();
	        		Iterator<EdiEmpfaenger> empf = ediEintrag.getEdiEmpfaenger().iterator();
	        		while (empf.hasNext()) {
	        			entityManager.remove(empf.next());
	        			empf.remove();
	        		}
	        		if (ediEintrag.getKonfiguration() != null) {
	        			ediEintrag.getKonfiguration().getEdiEintrag().remove(ediEintrag);
	        		}
	        		entityManager.remove(ediEintrag);
	        		entityManager.getTransaction().commit();
	        		setInfoText("Edi-Eintrag mit der Nr. " + ediNr + " erfolgreich gelöscht");
    			}	
    			ediEintraegeList.remove(selectedlistElement);
    			tableEdiNrAuswahl.getSelectionModel().clearSelection();
    		}
    	}
    }
    
    /* ------------------------------------------------------------------------
     * Loads von Sub-Controller
     * --------------------------------------------------------------------- */
    
    public KontaktPersonAuswaehlenController loadKontaktPersonAuswahl(Stage dialog) {
    	KontaktPersonAuswaehlenController controller = null;
    	FXMLLoader loader = load("subs/KontaktPersonAuswaehlen.fxml");
    	if (loader != null) {
    		controller = loader.getController();
    		controller.start(primaryStage, this, entityManager);
    		Parent root = loader.getRoot();
    		Scene scene = new Scene(root);
    		dialog.initModality(Modality.APPLICATION_MODAL);
    		dialog.initOwner(primaryStage);
    		dialog.setTitle(primaryStage.getTitle());
    		dialog.setScene(scene);
    	}
    	return controller;
	}
    
    public DokumentAuswaehlenController loadDokumentAuswahl(Stage dialog) {
    	DokumentAuswaehlenController controller = null;
    	FXMLLoader loader = load("subs/DokumentAuswaehlen.fxml");
    	if (loader != null) {
    		controller = loader.getController();
    		controller.start(primaryStage, this, entityManager);
    		Parent root = loader.getRoot();
    		Scene scene = new Scene(root);
    		dialog.initModality(Modality.APPLICATION_MODAL);
    		dialog.initOwner(primaryStage);
    		dialog.setTitle(primaryStage.getTitle());
    		dialog.setScene(scene);
    	}
    	return controller;
	}
    
    private FXMLLoader load(String ressourceName) {
    	FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource(ressourceName));
    	if (loader.getLocation()==null) {
    		String msg = "Resource \"" + ressourceName + "\" nicht gefunden";
    		setErrorText("FEHLER: " + msg);
    		logger.error(msg);
    	}
    	try {
    		loader.load();
    	} catch (IOException e) {
    		if (txtInfoZeile == null) { 
    			e.printStackTrace();
    		} else {
    			setErrorText("FEHLER: " + e.getMessage());
    		}	
    		logger.error(e);
    	}
    	return loader;
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
			Cursor aktCursor = primaryStage.getScene().getCursor();
			primaryStage.getScene().setCursor(Cursor.WAIT);
    		ExportToExcel export = new ExportToExcel(entityManager);
    		try {
    			int lines = export.write(file);
    			primaryStage.getScene().setCursor(aktCursor);
    			Dialogs.create().owner(primaryStage).title(APPL_NAME)
    					.masthead(null)
    					.message(lines + " Zeilen in der Datei " + file.getName() + 
    							" im Verzeichnis " + file.getParent() +" gespeichert")
    					.showInformation();		
			} catch (IOException e1) {
				primaryStage.getScene().setCursor(aktCursor);
				Dialogs.create().owner(primaryStage).title(APPL_NAME)
						.masthead("FEHLER")
						.message("Fehler beim Speichern der Exportdatei")
						.showException(e1);
			}
    	}
    }
    
    public void setupEntityManager() {
    	logger.info("Datenbankverbindung zu {} wird aufgebaut",PERSISTENCE_UNIT_NAME);
    	EntityManagerFactory factory = null;
    	try {
    		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	} catch (RuntimeException e) {
			Dialogs.create().owner(primaryStage).title(APPL_NAME)
				.masthead("FEHLER")
				.message("Fehler beim Aufbau der Datenbankverbindung")
				.showException(e);
    	}
    	
//    	String db = (String) factory.getProperties().get("javax.persistence.jdbc.url");
//    	System.out.println("DB: " + db);
    	
    	entityManager = factory.createEntityManager();
    	entityManager.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.REFRESH);
    	// Explanation for CacheStoreMode:
    	// REFRESH = refresh data in cache on find and query 
    	// USE = use cache without refresh if data exists in cache (=default)
    	// BYPASS = do not use cache
    	Map<String, Object> properties = entityManager.getProperties();
    	String dbUrl = (String) properties.get("javax.persistence.jdbc.url");
    	dbName = dbUrl.substring(dbUrl.lastIndexOf("/")+1);
    	logger.info("Datenbankverbindung zur DB \"" + dbName + "\" erfolgreich hergestellt.");
    }
    
    private void checkFieldsFromView() throws Exception {
    	logger.info("entered");
        assert tabEdiNr != null : "fx:id=\"tabEdiNr\" was not injected: check your FXML file 'EdiMain.fxml'.";

        assert tabPaneObjekte != null : "fx:id=\"tabPaneObjekte\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabPartner != null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabSysteme != null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabKomponenten != null : "fx:id=\"tabKomponenten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabIntegrationen != null : "fx:id=\"tabIntegrationen\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabKonfigurationen != null : "fx:id=\"tabKonfigurationen\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabKontaktPersonen != null : "fx:id=\"tabKontaktPersonen\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tabGeschaeftsobjekte != null : "fx:id=\"tabGeschaeftsobjekte\" was not injected: check your FXML file 'EdiMain.fxml'.";

        assert btnDeleteEdiEintrag != null : "fx:id=\"btnDeleteEdiEintrag\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert btnNewEdiNr != null : "fx:id=\"btnNewEdiNr\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert txtInfoZeile != null : "fx:id=\"txtInfoZeile\" was not injected: check your FXML file 'EdiMain.fxml'.";

        assert tableEdiNrAuswahl != null : "fx:id=\"tableEdiNrAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelKompoKomponten != null : "fx:id=\"tColSelKompoKomponten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlEdiNr != null : "fx:id=\"tColAuswahlEdiNr\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelSystemSystemName != null : "fx:id=\"tColSelSystemSystemName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelIntegrationName != null : "fx:id=\"tColSelIntegrationName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        
        assert tablePartnerAuswahl != null : "fx:id=\"tablePartnerAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlPartnerName != null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlEdiNrSender != null : "fx:id=\"tColAuswahlEdiNrSender\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlEdiNrBezeichnung != null : "fx:id=\"tColAuswahlEdiNrBezeichnung\" was not injected: check your FXML file 'EdiMain.fxml'.";

        assert tableKomponentenAuswahl != null : "fx:id=\"tableKomponentenAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelKompoPartner != null : "fx:id=\"tColSelKompoPartner\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelSystemPartnerName != null : "fx:id=\"tColSelSystemPartnerName\" was not injected: check your FXML file 'EdiMain.fxml'.";

        assert tColSelKompoSysteme != null : "fx:id=\"tColSelKompoSysteme\" was not injected: check your FXML file 'EdiMain.fxml'.";

        assert tableSystemAuswahl != null : "fx:id=\"tableSystemAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlPartnerSysteme != null : "fx:id=\"tColAuswahlPartnerSysteme\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColSelSystemKomponenten != null : "fx:id=\"tColSelSystemKomponenten\" was not injected: check your FXML file 'EdiMain.fxml'.";
        
        assert konfigurationController != null : "\"konfigurationController\" was not injected in EdiMain.fxml";
        assert geschaeftsObjektController != null : "\"geschaeftsObjektController\" was not injected in EdiMain.fxml";
        
        assert tableKontaktAuswahl != null : "\"tableKontaktAuswahl\" was not injected in EdiMain.fxml";
        assert tColKontaktUserId != null : "\"tColKontaktUserId\" was not injected in EdiMain.fxml";
        assert tColKontaktNachname != null : "\"tColKontaktNachname\" was not injected in EdiMain.fxml";
        assert tColKontaktVorname != null : "\"tColKontaktVorname\" was not injected in EdiMain.fxml";
        assert tColKontaktAbteilung != null : "\"tColKontaktAbteilung\" was not injected in EdiMain.fxml";
        assert tColKontaktTelefon != null : "\"tColKontaktTelefon\" was not injected in EdiMain.fxml";
        assert tColKontaktMailadresse != null : "\"tColKontaktMailadresse\" was not injected in EdiMain.fxml";
        
        assert tableGeschaeftsobjektAuswahl != null : "fx:id=\"tableGeschaeftsobjektAuswahl\" was not injected: check your FXML file 'EdiMain.fxml'.";
        assert tColAuswahlGeschaeftsobjektName != null : "fx:id=\"tColAuswahlGeschaeftsobjektName\" was not injected: check your FXML file 'EdiMain.fxml'.";
        logger.info("passed");
    }

}
