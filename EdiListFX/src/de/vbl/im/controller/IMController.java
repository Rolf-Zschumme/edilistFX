package de.vbl.im.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
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

import sun.util.logging.resources.logging;
import de.vbl.im.controller.subs.DokumentAuswaehlenController;
import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Integration;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.EdiKomponente;
import de.vbl.im.model.EdiPartner;
import de.vbl.im.model.EdiSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.Iszenario;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.Ansprechpartner;
import de.vbl.im.tools.ExportToExcel;

public class IMController {
	private static final Logger logger = LogManager.getLogger(IMController.class.getName()); 
	private static final String APPL_NAME = "Integration Manager";
	private static final String PERSISTENCE_UNIT_NAME = "IntegrationManager";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

	private static String dbName;
	private static int maxEdiNr;
	private static Stage primaryStage;
    private EntityManager entityManager;
    private ObservableList<Integration> ediEintraegeList = FXCollections.observableArrayList();
    private ObservableList<EdiPartner> ediPartnerList = FXCollections.observableArrayList();    
    private ObservableList<EdiSystem> ediSystemList = FXCollections.observableArrayList();
    private ObservableList<EdiKomponente> ediKomponentenList = FXCollections.observableArrayList();
    private ObservableList<Iszenario> iszenarioList = FXCollections.observableArrayList();
    private ObservableList<Konfiguration> konfigurationList = FXCollections.observableArrayList();
    private ObservableList<Ansprechpartner> ansprechpartnerList = FXCollections.observableArrayList();
    private ObservableList<GeschaeftsObjekt> geschaeftsobjektList = FXCollections.observableArrayList();    
	
    @FXML private TextField txtInfoZeile;
    @FXML private TabPane tabPaneObjekte;
    
    @FXML private Tab tabEdiNr;
    @FXML private TableView<Integration> tableEdiNrAuswahl;
    @FXML private TableColumn<Integration, String> tColAuswahlEdiNr;
    @FXML private TableColumn<Integration, String> tColAuswahlEdiNrSender;
    @FXML private TableColumn<Integration, String> tColAuswahlEdiNrIszenario;
    @FXML private TableColumn<Integration, String> tColAuswahlEdiNrBezeichnung;

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
    
    @FXML private Tab tabIszenarien;
    @FXML private TableView<Iszenario> tableIszenarioAuswahl;
    @FXML private TableColumn<Iszenario, String> tColSelIszenarioName;
    
    @FXML private Tab tabKonfigurationen;
    @FXML private TableView<Konfiguration> tableKonfigurationAuswahl;
    @FXML private TableColumn<Konfiguration, String> tColSelKonfigurationName;
    @FXML private TableColumn<Konfiguration, String> tColSelKonfigIszenarioName;
    
    @FXML private Tab tabAnsprechpartner;
    @FXML private TableView<Ansprechpartner> tableKontaktAuswahl;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktUserId;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktNachname;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktVorname;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktArt;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktAbteilung;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktTelefon;
    @FXML private TableColumn<Ansprechpartner, String> tColKontaktMailadresse;
    
    @FXML private Tab tabGeschaeftsobjekte;
    @FXML private TableView<GeschaeftsObjekt> tableGeschaeftsobjektAuswahl;
    @FXML private TableColumn<GeschaeftsObjekt, String> tColAuswahlGeschaeftsobjektName;
    
    @FXML private Pane integration;
    @FXML private Pane ediPartner;
    @FXML private Pane ediSystem;
    @FXML private Pane ediKomponente;
    @FXML private Pane iszenario;
    @FXML private Pane konfiguration;
    @FXML private Pane ansprechpartner;
    @FXML private Pane geschaeftsObjekt;
    
    @FXML private IntegrationController integrationController;
    @FXML private EdiPartnerController ediPartnerController;
    @FXML private EdiSystemController ediSystemController;
    @FXML private EdiKomponenteController ediKomponenteController;
    @FXML private IszenarioController iszenarioController;
    @FXML private KonfigurationController konfigurationController;
    @FXML private AnsprechpartnerController ansprechpartnerController;
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
    	
    	IntegrationController.setParent(this);
    	EdiPartnerController.setParent(this);
    	EdiSystemController.setParent(this);
    	EdiKomponenteController.setParent(this);
    	IszenarioController.setParent(this);
    	KonfigurationController.setParent(this);
    	AnsprechpartnerController.setParent(this);
    	GeschaeftsObjektController.setParent(this);
        
    	// Check for data changes on close request from MainWindow
    	primaryStage.setOnCloseRequest(event -> {
    		if (checkAllOk() == false) {
    			event.consume();
    		}
    	});
    }
    
	public void setPrimaryStage(Stage primaryStage) {
		IMController.primaryStage = primaryStage;
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
    	logger.info("entered");;
    	loadIntegrationListData();
    	setupIntegrationPane();
    	setupEdiPartnerPane();
    	setupEdiSystemPane();
    	setupKomponentenPane();
    	setupIszenarioPane();
    	setupKonfigurationPane();
    	setupAnsprechpartnerPane();
    	setupGeschaeftsobjektPane();

        tabPaneObjekte.getSelectionModel().selectedItemProperty().addListener(
        		new ChangeListener<Tab>() {
        			@Override
        			public void changed(ObservableValue<? extends Tab> ov, Tab talt, Tab tneu) {
        				final Tab akttab = tneu;
						logger.info("tabPane.changed:", akttab.textProperty().get());

						primaryStage.getScene().setCursor(Cursor.WAIT);
						
						if (akttab.equals(tabEdiNr)) {
							loadIntegrationListData();
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
						else if(akttab.equals(tabIszenarien)) {
							loadIszenarioListData();
						}
						else if(akttab.equals(tabKonfigurationen)) {
							loadKonfigurationListData();
						}
						else if(akttab.equals(tabAnsprechpartner)) {
							loadAnsprechpartnerListData();
						}
						else if(akttab.equals(tabGeschaeftsobjekte)) {
							loadGeschaeftobjektListData();
						}
        				primaryStage.getScene().setCursor(Cursor.DEFAULT);
        			}

				}
        );
        tableEdiNrAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkIntegration(e));
        tableEdiNrAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkIntegration(e));

        tablePartnerAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkPartner(e));
        tablePartnerAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkPartner(e));
        
        tableSystemAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkSystem(e));
        tableSystemAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkSystem(e));
        
        tableKomponentenAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkKomponente(e));
		tableKomponentenAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkKomponente(e));
		
		tableIszenarioAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkIszenario(e));
		tableIszenarioAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkIszenario(e));
		
		tableKonfigurationAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkKonfiguration(e));
		tableKonfigurationAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkKonfiguration(e));
		
		tableKontaktAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkAnsprechpartner(e));
		tableKontaktAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkAnsprechpartner(e));
		
		tableGeschaeftsobjektAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkGeschaeftsObjekt(e));
		tableGeschaeftsobjektAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkGeschaeftsObjekt(e));
		
    }
    
	private void checkIntegration(Event event) {
    	if(integrationController.checkForChangesAndAskForSave() == false) {
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
	
	private void checkIszenario(Event event) {
		if (iszenarioController.checkForChangesAndAskForSave() == false) {
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

	private void checkAnsprechpartner(Event event) {
		if (ansprechpartnerController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private boolean checkAllOk() {
    	return integrationController.checkForChangesAndAskForSave() || 
    		   ediPartnerController.checkForChangesAndAskForSave() ||
    	       ediSystemController.checkForChangesAndAskForSave()  ||
    	       ediKomponenteController.checkForChangesAndAskForSave() ||
    		   iszenarioController.checkForChangesAndAskForSave()   ||
    		   konfigurationController.checkForChangesAndAskForSave() ||
    		   ansprechpartnerController.checkForChangesAndAskForSave() ||
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
	 * set a new selected Integration from other controllers
	 * ***********************************************************************/
	protected void setSelectedIntegration (Integration e) {
		tableEdiNrAuswahl.getSelectionModel().select(e);
	}
    
	private void setupIntegrationPane() {
		logger.info("entered");
    	tableEdiNrAuswahl.setItems(ediEintraegeList);
    	tColAuswahlEdiNr.setCellValueFactory(cellData -> Bindings.format(Integration.FORMAT_EDINR, 
    			cellData.getValue().ediNrProperty()));
    	tColAuswahlEdiNrSender.setCellValueFactory(cellData -> 
    			cellData.getValue().senderNameProperty());
    	tColAuswahlEdiNrBezeichnung.setCellValueFactory(cellData -> 
    			cellData.getValue().bezeichnungProperty());
    	tColAuswahlEdiNrIszenario.setCellValueFactory(cellData -> 
    			cellData.getValue().iszenarioNameProperty());

    	integration.setDisable(false);

    	integrationController.integrationProperty().bind(
    						    tableEdiNrAuswahl.getSelectionModel().selectedItemProperty());
    	tableEdiNrAuswahl.setRowFactory(new Callback<TableView<Integration>, TableRow<Integration>>() {
			@Override
			public TableRow<Integration> call(TableView<Integration> table) {
				final TableRow<Integration> row = new TableRow<Integration>();
				final ContextMenu contextMenu = new ContextMenu();
				final MenuItem removeMenuItem = new MenuItem("Löschen");
				removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						integrationLoeschen();
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
		logger.info("entered");
		tablePartnerAuswahl.setItems(ediPartnerList);
		tColAuswahlPartnerName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColAuswahlPartnerSysteme.setCellValueFactory(cellData -> Bindings.format("%7d", cellData.getValue().anzSystemeProperty()));
		tColAuswahlPartnerKomponenten.setCellValueFactory(cell -> Bindings.format("%7d", cell.getValue().anzKomponentenProperty()));
		
		ediPartnerController.ediPartnerProperty().bind(tablePartnerAuswahl.getSelectionModel().selectedItemProperty());
		ediPartner.disableProperty().bind(Bindings.isNull(tablePartnerAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
	private void setupEdiSystemPane() {
		logger.info("entered");
		tableSystemAuswahl.setItems(ediSystemList);
		tColSelSystemSystemName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColSelSystemPartnerName.setCellValueFactory(cellData -> cellData.getValue().getEdiPartner().nameProperty());
		tColSelSystemKomponenten.setCellValueFactory(cellData -> Bindings.format("%7d", cellData.getValue().anzKomponentenProperty()));
		
		ediSystemController.ediSystemProperty().bind(tableSystemAuswahl.getSelectionModel().selectedItemProperty());
		ediSystem.disableProperty().bind(Bindings.isNull(tableSystemAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
	private void setupKomponentenPane() {
		logger.info("entered");
		tableKomponentenAuswahl.setItems(ediKomponentenList);
    	tColSelKompoKomponten.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    	tColSelKompoSysteme.setCellValueFactory(cellData -> cellData.getValue().getEdiSystem().nameProperty());
    	tColSelKompoPartner.setCellValueFactory(cellData -> cellData.getValue().getEdiSystem().getEdiPartner().nameProperty());

    	ediKomponenteController.komponenteProperty().bind(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty());
    	ediKomponente.disableProperty().bind(Bindings.isNull(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupIszenarioPane() {
		logger.info("entered");
		tableIszenarioAuswahl.setItems(iszenarioList);
		tColSelIszenarioName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		
		iszenarioController.iszenarioProperty().bind(tableIszenarioAuswahl.getSelectionModel().selectedItemProperty());
		iszenario.disableProperty().bind(Bindings.isNull(tableIszenarioAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupKonfigurationPane() {
		logger.info("entered");
		tableKonfigurationAuswahl.setItems(konfigurationList);
		tColSelKonfigurationName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColSelKonfigIszenarioName.setCellValueFactory(cd -> cd.getValue().iSzenarioNameProperty());
		
		konfigurationController.konfigurationProperty().bind(tableKonfigurationAuswahl.getSelectionModel().selectedItemProperty());
		konfiguration.disableProperty().bind(Bindings.isNull(tableKonfigurationAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupAnsprechpartnerPane() {
		logger.info("entered");
		tableKontaktAuswahl.setItems(ansprechpartnerList);
		tColKontaktUserId.setCellValueFactory(cellData -> cellData.getValue().nummerProperty());
		tColKontaktNachname.setCellValueFactory(cellData -> cellData.getValue().nachnameProperty());
		tColKontaktVorname.setCellValueFactory(cellData -> cellData.getValue().vornameProperty());
		tColKontaktArt.setCellValueFactory(cellData -> cellData.getValue().artProperty());
		tColKontaktAbteilung.setCellValueFactory(cellData -> cellData.getValue().abteilungProperty());
		tColKontaktTelefon.setCellValueFactory(cellData -> cellData.getValue().telefonProperty());
		tColKontaktMailadresse.setCellValueFactory(cellData -> cellData.getValue().mailProperty());
		
		ansprechpartnerController.ansprechpartnerProperty().bind(tableKontaktAuswahl.getSelectionModel().selectedItemProperty());
		ansprechpartner.disableProperty().bind(Bindings.isNull(tableKontaktAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
    private void setupGeschaeftsobjektPane() {
		logger.info("entered");
    	tableGeschaeftsobjektAuswahl.setItems(geschaeftsobjektList);
    	tColAuswahlGeschaeftsobjektName.setCellValueFactory(cell -> cell.getValue().nameProperty());
    	
    	geschaeftsObjektController.geschaeftsObjektProperty().bind(tableGeschaeftsobjektAuswahl.getSelectionModel().selectedItemProperty());
    	geschaeftsObjekt.disableProperty().bind(Bindings.isNull(tableGeschaeftsobjektAuswahl.getSelectionModel().selectedItemProperty()));
    }
    
	protected void loadIntegrationListData() {
    	TypedQuery<Integration> tq = entityManager.createQuery(
				"SELECT e FROM Integration e ORDER BY e.ediNr", Integration.class);
		List<Integration> aktuList = tq.getResultList();

		ediEintraegeList.retainAll(aktuList);
		maxEdiNr = 0;
		for(Integration e : aktuList ) {
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

	protected void loadIszenarioListData() {
		TypedQuery<Iszenario> tq = entityManager.createQuery(
				"SELECT i FROM Iszenario i ORDER BY i.name", Iszenario.class);
		List<Iszenario> aktuList = tq.getResultList();
		
		iszenarioList.retainAll(aktuList); // remove delete entities
		for (Iszenario i : aktuList) {	 // and insert new entities
			if (iszenarioList.contains(i) == false) {
				iszenarioList.add(aktuList.indexOf(i),i);
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

	protected void loadAnsprechpartnerListData() {
		TypedQuery<Ansprechpartner> tq = entityManager.createQuery(
				"SELECT k FROM Ansprechpartner k ORDER BY k.nachname", Ansprechpartner.class);
		List<Ansprechpartner> aktulist = tq.getResultList();
		ansprechpartnerList.retainAll(aktulist);
		for (Ansprechpartner k : aktulist) {
			if (ansprechpartnerList.contains(k) == false ) {
				ansprechpartnerList.add(aktulist.indexOf(k), k);
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
			.masthead("VBL-Tool zur Verwaltung der Integrationsszenarios")
			.message("\nProgramm-Version 1.1.0 - 11.11.2014\n" +
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

//    @FXML
//    void newEdiNr(ActionEvent event) {
//    
//    	FXMLLoader loader = new FXMLLoader();
//    	String fullname = "subs/NeueIntegration.fxml";
//    	loader.setLocation(getClass().getResource(fullname));
//    	if (loader.getLocation()==null) {
//    		logger.error("Resource not found :" + fullname);
//    		setErrorText("FEHLER: Resource ("+fullname+") not found");
//    		return;
//    	}
//    	try {
//			loader.load();
//		} catch (IOException e) {
//			logger.error("Fehler beim Laden der Resource:" + e.getMessage());
//			setErrorText("FEHLER: " + e.getMessage());
//			return;
//		}
//    	Parent root = loader.getRoot();
//    	Scene scene = new Scene(root);
//    	
//    	Stage dialog = new Stage(StageStyle.UTILITY);
//    	dialog.initModality(Modality.APPLICATION_MODAL);
//    	dialog.initOwner(primaryStage);
//    	dialog.setTitle(primaryStage.getTitle());
//    	
//    	NeueIntegrationController dialogController = loader.getController();
//    	dialogController.setEntityManager(entityManager);
//    	dialogController.start();
//    	
//    	dialog.setScene(scene);
//    	dialog.setX(primaryStage.getX() + 250);
//    	dialog.setY(primaryStage.getY() + 100);
//    	dialog.showAndWait();
//
//    	if (dialogController.getResponse() == Dialog.Actions.OK) {
//    		Integration newEE = dialogController.getNewIntegration();
//			ediEintraegeList.add(newEE);
//			if (newEE.getEdiNr() > maxEdiNr) 
//				maxEdiNr = newEE.getEdiNr();
//			tableEdiNrAuswahl.getSelectionModel().select(newEE);
//    	}
//    }    

    @FXML    
    void handleIntegrationLoeschen(ActionEvent event) {
    	integrationLoeschen();
    }
    private void integrationLoeschen() {
    	Integration selectedlistElement = tableEdiNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		String ediNr = Integer.toString(selectedlistElement.ediNrProperty().get());
    		Action response = Dialog.Actions.OK;
    		if (selectedlistElement.getEdiKomponente() != null) {
    			response = Dialogs.create()
					.owner(primaryStage).title(APPL_NAME)
					.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
					.masthead(SICHERHEITSABFRAGE)
					.message("Integration mit der Nr. " + ediNr + " wirklich löschen?")
					.showConfirm();
    		}
    		if (response == Dialog.Actions.OK) {
    			Integration integration = entityManager.find(Integration.class, selectedlistElement.getId());
    			if (integration==null) {
    				logger.error("FEHLER: Integration " + ediNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		entityManager.getTransaction().begin();
	        		Iterator<EdiEmpfaenger> empf = integration.getEdiEmpfaenger().iterator();
	        		while (empf.hasNext()) {
	        			entityManager.remove(empf.next());
	        			empf.remove();
	        		}
	        		if (integration.getKonfiguration() != null) {
	        			integration.getKonfiguration().getIntegration().remove(integration);
	        		}
	        		entityManager.remove(integration);
	        		entityManager.getTransaction().commit();
	        		setInfoText("Integration mit der Nr. " + ediNr + " erfolgreich gelöscht");
    			}	
    			ediEintraegeList.remove(selectedlistElement);
    			tableEdiNrAuswahl.getSelectionModel().clearSelection();
    		}
    	}
    }
    
    /* ------------------------------------------------------------------------
     * Loads von Sub-Controller
     * --------------------------------------------------------------------- */
    
    public AnsprechpartnerAuswaehlenController loadAnsprechpartnerAuswahl(Stage dialog) {
    	AnsprechpartnerAuswaehlenController controller = null;
    	FXMLLoader loader = load("subs/AnsprechpartnerAuswaehlen.fxml");
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
    static String initialExportFileName = "IM-List-Export.xlsx";

    @FXML
    void btnExportExcel (ActionEvent event) {
    	LocalDate heute =  LocalDate.now();
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
    
    public void refreshKontaktReferences() {
    	if (ansprechpartnerController.getAnsprechpartner() != null) {
    		ansprechpartnerController.readEdiKomponentenListeforPerson();
    	}
    }
    
    public void setupEntityManager() {
    	logger.info("Datenbankverbindung zu {} wird aufgebaut",PERSISTENCE_UNIT_NAME);
    	EntityManagerFactory factory = null;
    	try {
    		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	} catch (RuntimeException e) {
    		String msg = "Fehler beim Öffnen der Datenbank (" + PERSISTENCE_UNIT_NAME + ")";
    		System.out.println(msg);
    		logger.error(msg + "\nMessage:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage).title(APPL_NAME)
				.masthead("FEHLER")
				.message("Fehler beim Aufbau der Datenbankverbindung:\n" + e.getMessage())
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
    	assert integration 				!= null : "fx:id=\"integration\" was not injected: check your FXML file 'IM.fxml'.";
    	assert tabEdiNr					!= null : "fx:id=\"tabEdiNr\" was not injected: check your FXML file 'IM.fxml'.";

        assert tabPaneObjekte 			!= null : "fx:id=\"tabPaneObjekte\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabPartner 				!= null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabSysteme				!= null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabKomponenten			!= null : "fx:id=\"tabKomponenten\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabIszenarien			!= null : "fx:id=\"tabIszenarien\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabKonfigurationen		!= null : "fx:id=\"tabKonfigurationen\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabAnsprechpartner		!= null : "fx:id=\"tabAnsprechpartner\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabGeschaeftsobjekte		!= null : "fx:id=\"tabGeschaeftsobjekte\" was not injected: check your FXML file 'IM.fxml'.";

        assert txtInfoZeile				!= null : "fx:id=\"txtInfoZeile\" was not injected: check your FXML file 'IM.fxml'.";

        assert tableEdiNrAuswahl 			!= null : "fx:id=\"tableEdiNrAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlEdiNr 			!= null : "fx:id=\"tColAuswahlEdiNr\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlEdiNrIszenario	!= null : "fx:id=\"tColAuswahlEdiNrIszenario\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelKompoKomponten 		!= null : "fx:id=\"tColSelKompoKomponten\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelSystemSystemName 		!= null : "fx:id=\"tColSelSystemSystemName\" was not injected: check your FXML file 'IM.fxml'.";
        
        assert tColSelIszenarioName 		!= null : "fx:id=\"tColSelIszenarioName\" was not injected: check your FXML file 'IM.fxml'.";
        
        assert tablePartnerAuswahl 			!= null : "fx:id=\"tablePartnerAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlPartnerName 		!= null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlEdiNrSender 		!= null : "fx:id=\"tColAuswahlEdiNrSender\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlEdiNrBezeichnung 	!= null : "fx:id=\"tColAuswahlEdiNrBezeichnung\" was not injected: check your FXML file 'IM.fxml'.";

        assert tableKomponentenAuswahl 		!= null : "fx:id=\"tableKomponentenAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelKompoPartner 			!= null : "fx:id=\"tColSelKompoPartner\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelKompoSysteme 			!= null : "fx:id=\"tColSelKompoSysteme\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelSystemPartnerName 	!= null : "fx:id=\"tColSelSystemPartnerName\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelSystemKomponenten 	!= null : "fx:id=\"tColSelSystemKomponenten\" was not injected: check your FXML file 'IM.fxml'.";

        assert tableSystemAuswahl 			!= null : "fx:id=\"tableSystemAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlPartnerSysteme 	!= null : "fx:id=\"tColAuswahlPartnerSysteme\" was not injected: check your FXML file 'IM.fxml'.";
        
        assert konfigurationController 		!= null : "\"konfigurationController\" was not injected in IM.fxml";
        assert geschaeftsObjektController 	!= null : "\"geschaeftsObjektController\" was not injected in IM.fxml";
        
        assert tableKontaktAuswahl 			!= null : "\"tableKontaktAuswahl\" was not injected in IM.fxml";
        assert tColKontaktUserId 			!= null : "\"tColKontaktUserId\" was not injected in IM.fxml";
        assert tColKontaktNachname 			!= null : "\"tColKontaktNachname\" was not injected in IM.fxml";
        assert tColKontaktVorname 			!= null : "\"tColKontaktVorname\" was not injected in IM.fxml";
        assert tColKontaktArt 				!= null : "\"tColKontaktArt\" was not injected in IM.fxml";
        assert tColKontaktAbteilung 		!= null : "\"tColKontaktAbteilung\" was not injected in IM.fxml";
        assert tColKontaktTelefon 			!= null : "\"tColKontaktTelefon\" was not injected in IM.fxml";
        assert tColKontaktMailadresse 		!= null : "\"tColKontaktMailadresse\" was not injected in IM.fxml";
        
        assert tableGeschaeftsobjektAuswahl 	!= null : "fx:id=\"tableGeschaeftsobjektAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlGeschaeftsobjektName 	!= null : "fx:id=\"tColAuswahlGeschaeftsobjektName\" was not injected: check your FXML file 'IM.fxml'.";
        logger.info("passed");
    }

}
