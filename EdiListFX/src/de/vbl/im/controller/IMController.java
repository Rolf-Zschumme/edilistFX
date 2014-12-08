package de.vbl.im.controller;

import java.io.File;
import java.io.FileNotFoundException;
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

import de.vbl.im.controller.subs.DokumentAuswaehlenController;
import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.InSystem;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.Ansprechpartner;
import de.vbl.im.tools.ExportToExcel;
import de.vbl.im.tools.IMconstant;

public class IMController {
	private static final Logger logger = LogManager.getLogger(IMController.class.getName()); 

	private static String dbName;
	private static int maxInNr;
	private static Stage primaryStage;
    private EntityManager entityManager;
    private ObservableList<Integration> integrationList = FXCollections.observableArrayList();
    private ObservableList<InPartner> inPartnerList = FXCollections.observableArrayList();    
    private ObservableList<InSystem> inSystemList = FXCollections.observableArrayList();
    private ObservableList<InKomponente> inKomponentenList = FXCollections.observableArrayList();
    private ObservableList<InSzenario> inSzenarioList = FXCollections.observableArrayList();
    private ObservableList<Konfiguration> konfigurationList = FXCollections.observableArrayList();
    private ObservableList<Ansprechpartner> ansprechpartnerList = FXCollections.observableArrayList();
    private ObservableList<GeschaeftsObjekt> geschaeftsobjektList = FXCollections.observableArrayList();    
	
    @FXML private TextField txtInfoZeile;
    @FXML private TabPane tabPaneObjekte;
    
    @FXML private Tab tabInNr;
    @FXML private TableView<Integration> tableInNrAuswahl;
    @FXML private TableColumn<Integration, String> tColAuswahlInNr;
    @FXML private TableColumn<Integration, String> tColAuswahlInNrSender;
    @FXML private TableColumn<Integration, String> tColAuswahlInNrInSzenario;
    @FXML private TableColumn<Integration, String> tColAuswahlInNrBezeichnung;

    @FXML private Tab tabPartner;
    @FXML private TableView<InPartner> tablePartnerAuswahl;
    @FXML private TableColumn<InPartner, String> tColAuswahlPartnerName;
    @FXML private TableColumn<InPartner, String> tColAuswahlPartnerSysteme;
    @FXML private TableColumn<InPartner, String> tColAuswahlPartnerKomponenten;

    @FXML private Tab tabSysteme;
    @FXML private TableView<InSystem> tableSystemAuswahl;
    @FXML private TableColumn<InSystem, String> tColSelSystemSystemName;
    @FXML private TableColumn<InSystem, String> tColSelSystemPartnerName;
    @FXML private TableColumn<InSystem, String> tColSelSystemKomponenten;

    @FXML private Tab tabKomponenten;
    @FXML private TableView<InKomponente> tableKomponentenAuswahl;   
    @FXML private TableColumn<InKomponente, String> tColSelKompoKomponten;
    @FXML private TableColumn<InKomponente, String> tColSelKompoSysteme;
    @FXML private TableColumn<InKomponente, String> tColSelKompoPartner;
    
    @FXML private Tab tabInSzenarien;
    @FXML private TableView<InSzenario> tableInSzenarioAuswahl;
    @FXML private TableColumn<InSzenario, String> tColSelInSzenarioNr;
    @FXML private TableColumn<InSzenario, String> tColSelInSzenarioName;
    
    @FXML private Tab tabKonfigurationen;
    @FXML private TableView<Konfiguration> tableKonfigurationAuswahl;
    @FXML private TableColumn<Konfiguration, String> tColSelKonfigurationName;
    @FXML private TableColumn<Konfiguration, String> tColSelKonfigInSzenarioName;
    
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
    @FXML private Pane inPartner;
    @FXML private Pane inSystem;
    @FXML private Pane inKomponente;
    @FXML private Pane inSzenario;
    @FXML private Pane konfiguration;
    @FXML private Pane ansprechpartner;
    @FXML private Pane geschaeftsObjekt;
    
    @FXML private IntegrationController integrationController;
    @FXML private InPartnerController inPartnerController;
    @FXML private InSystemController inSystemController;
    @FXML private InKomponenteController inKomponenteController;
    @FXML private InSzenarioController inSzenarioController;
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
    	primaryStage.setTitle(IMconstant.APPL_NAME);
    	 
    	integrationController.setParent(this);
    	inPartnerController.setParent(this);
    	inSystemController.setParent(this);
    	inKomponenteController.setParent(this);
    	inSzenarioController.setParent(this);
    	konfigurationController.setParent(this);
    	ansprechpartnerController.setParent(this);
    	geschaeftsObjektController.setParent(this);
        
    	// Check for data changes on close request from MainWindow
    	primaryStage.setOnCloseRequest(event -> {
    		if (checkAllOk() == false) {
    			logger.info("Close abgebrochen");
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
    	setupInPartnerPane();
    	setupInSystemPane();
    	setupKomponentenPane();
    	setupInSzenarioPane();
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
						
						if (akttab.equals(tabInNr)) {
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
						else if(akttab.equals(tabInSzenarien)) {
							loadInSzenarioListData();
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
        tableInNrAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkIntegration(e));
        tableInNrAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkIntegration(e));

        tablePartnerAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkPartner(e));
        tablePartnerAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkPartner(e));
        
        tableSystemAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkSystem(e));
        tableSystemAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkSystem(e));
        
        tableKomponentenAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkKomponente(e));
		tableKomponentenAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkKomponente(e));
		
		tableInSzenarioAuswahl.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> checkInSzenario(e));
		tableInSzenarioAuswahl.addEventFilter(KeyEvent.KEY_PRESSED,     e -> checkInSzenario(e));
		
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
		if (inPartnerController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}
	
	private void checkSystem(Event event) {
		if (inSystemController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}

	private void checkKomponente(Event event) {
		if (inKomponenteController.checkForChangesAndAskForSave() == false) {
			event.consume();
		}
	}
	
	private void checkInSzenario(Event event) {
		if (inSzenarioController.checkForChangesAndAskForSave() == false) {
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
    	return integrationController.checkForChangesAndAskForSave() 	&& 
    		   inPartnerController.checkForChangesAndAskForSave() 		&&
    	       inSystemController.checkForChangesAndAskForSave()  		&&
    	       inKomponenteController.checkForChangesAndAskForSave()	&&
    		   inSzenarioController.checkForChangesAndAskForSave()		&&
    		   konfigurationController.checkForChangesAndAskForSave()	&&
    		   ansprechpartnerController.checkForChangesAndAskForSave() &&
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
	void actionInNrContextMenuRequested (Event event) {
		// TODO
		System.out.println("Contextmenu for Integration-Table requested");
	}
	
	/* ************************************************************************
	 * set a new selected Integration from other controllers
	 * ***********************************************************************/
	protected void setSelectedIntegration (Integration e) {
		tableInNrAuswahl.getSelectionModel().select(e);
	}
    
	protected void setSelectedInSzenario ( InSzenario is) {
		tableInSzenarioAuswahl.getSelectionModel().select(is);
	}
	
	private void setupIntegrationPane() {
		logger.info("entered");
    	tableInNrAuswahl.setItems(integrationList);
    	tColAuswahlInNr.setCellValueFactory(cellData -> 
    										cellData.getValue().inNrStrExp());
    	tColAuswahlInNrSender.setCellValueFactory(cellData -> 
    										cellData.getValue().senderNameProperty());
    	tColAuswahlInNrBezeichnung.setCellValueFactory(cellData -> 
    										cellData.getValue().bezeichnungProperty());
    	tColAuswahlInNrInSzenario.setCellValueFactory(cellData -> 
    										cellData.getValue().inSzenarioNameProperty());

    	integration.setDisable(false);

    	integrationController.integrationProperty().bind(
    						    tableInNrAuswahl.getSelectionModel().selectedItemProperty());
    	tableInNrAuswahl.setRowFactory(new Callback<TableView<Integration>, TableRow<Integration>>() {
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
	
	private void setupInPartnerPane() {
		logger.info("entered");
		tablePartnerAuswahl.setItems(inPartnerList);
		tColAuswahlPartnerName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColAuswahlPartnerSysteme.setCellValueFactory(cellData -> Bindings.format("%7d", cellData.getValue().anzSystemeProperty()));
		tColAuswahlPartnerKomponenten.setCellValueFactory(cell -> Bindings.format("%7d", cell.getValue().anzKomponentenProperty()));
		
		inPartnerController.inPartnerProperty().bind(tablePartnerAuswahl.getSelectionModel().selectedItemProperty());
		inPartner.disableProperty().bind(Bindings.isNull(tablePartnerAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
	private void setupInSystemPane() {
		logger.info("entered");
		tableSystemAuswahl.setItems(inSystemList);
		tColSelSystemSystemName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		tColSelSystemPartnerName.setCellValueFactory(cellData -> cellData.getValue().getinPartner().nameProperty());
		tColSelSystemKomponenten.setCellValueFactory(cellData -> Bindings.format("%7d", cellData.getValue().anzKomponentenProperty()));
		
		inSystemController.inSystemProperty().bind(tableSystemAuswahl.getSelectionModel().selectedItemProperty());
		inSystem.disableProperty().bind(Bindings.isNull(tableSystemAuswahl.getSelectionModel().selectedItemProperty()));
	}
	
	private void setupKomponentenPane() {
		logger.info("entered");
		tableKomponentenAuswahl.setItems(inKomponentenList);
    	tColSelKompoKomponten.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    	tColSelKompoSysteme.setCellValueFactory(cellData -> cellData.getValue().getInSystem().nameProperty());
    	tColSelKompoPartner.setCellValueFactory(cellData -> cellData.getValue().getInSystem().getinPartner().nameProperty());

    	inKomponenteController.komponenteProperty().bind(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty());
    	inKomponente.disableProperty().bind(Bindings.isNull(tableKomponentenAuswahl.getSelectionModel().selectedItemProperty()));
	}

	private void setupInSzenarioPane() {
		logger.info("entered");
		tableInSzenarioAuswahl.setItems(inSzenarioList);
		tColSelInSzenarioNr.setCellValueFactory(cellData -> Bindings.format("%03d",cellData.getValue().isNrProperty()));
		tColSelInSzenarioName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		
		inSzenarioController.inSzenarioProperty().bind(tableInSzenarioAuswahl.getSelectionModel().selectedItemProperty());
//		inSzenario.disableProperty().bind(Bindings.isNull(tableInSzenarioAuswahl.getSelectionModel().selectedItemProperty()));
		inSzenario.setDisable(false);
	}

	private void setupKonfigurationPane() {
		logger.info("entered");
		tableKonfigurationAuswahl.setItems(konfigurationList);
		tColSelKonfigurationName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
//		tColSelKonfigInSzenarioName.setCellValueFactory(cd -> cd.getValue().inSzenarioNameProperty());
		
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
		TypedQuery<Integration> tq = null;
		try {
			tq = entityManager.createQuery(
					"SELECT e FROM Integration e ORDER BY e.inNr", Integration.class);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		List<Integration> resultList = tq.getResultList();
		integrationList.retainAll(resultList);
		maxInNr = 0;
		for(Integration e : resultList) {
			if (integrationList.contains(e) == false) {
				integrationList.add(resultList.indexOf(e), e);
			}
	    	if (e.getInNr() > maxInNr) maxInNr = e.getInNr();
		}
	}
	
	protected void loadPartnerListData() {
		TypedQuery<InPartner> tq = entityManager.createQuery(
				"SELECT p FROM InPartner p ORDER BY p.name", InPartner.class);
		List<InPartner> aktuList = tq.getResultList(); 

		inPartnerList.retainAll(aktuList);  // remove all delete entities  

		for ( InPartner p : aktuList) {     // insert or update all entities
			if (inPartnerList.contains(p) == false) {
				inPartnerList.add(aktuList.indexOf(p), p);
			} else {
//				log("loadPartnerListData","name:" + p.getName() );
				int aktPos = inPartnerList.indexOf(p);
				InPartner p1 = inPartnerList.get(aktPos);
//				p1.anzSystemeProperty().set(p.anzSystemeProperty().get());
				p1.anzKomponentenProperty().set(p.anzKomponentenProperty().get());
			}
		}
	}

	protected void loadSystemListData() {
		TypedQuery<InSystem> tq = entityManager.createQuery(
				"SELECT s FROM InSystem s ORDER BY s.name", InSystem.class);
		List<InSystem> aktuList = tq.getResultList();
		inSystemList.retainAll(aktuList);  // remove all delete entities  
		for (InSystem s : aktuList) {		// insert or update all entities
			if (inSystemList.contains(s) == false) {
				inSystemList.add(aktuList.indexOf(s), s);
//			} else {
//				int aktPos = inSystemList.indexOf(s);
//				InSystem s1 = inSystemList.get(aktPos);
//				s1.anzKomponentenProperty().set(s.anzKomponentenProperty().get());
			}
		}
	}

	protected void loadKomponentenListData() {
		TypedQuery<InKomponente> tq = entityManager.createQuery(
				"SELECT k FROM InKomponente k ORDER BY k.name", InKomponente.class);
		List<InKomponente> aktuList = tq.getResultList();
		
		inKomponentenList.retainAll(aktuList); // remove delete entities  
		for ( InKomponente k : aktuList) {     // and insert new entities
			if (inKomponentenList.contains(k) == false) {
				inKomponentenList.add(aktuList.indexOf(k), k);
			}
		}
	}

	protected void loadInSzenarioListData() {
		TypedQuery<InSzenario> tq = entityManager.createQuery(
				"SELECT i FROM InSzenario i ORDER BY i.name", InSzenario.class);
		List<InSzenario> aktuList = tq.getResultList();
		
		inSzenarioList.retainAll(aktuList); // remove delete entities
		for (InSzenario i : aktuList) {	 // and insert new entities
			if (inSzenarioList.contains(i) == false) {
				inSzenarioList.add(aktuList.indexOf(i),i);
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
			.owner(primaryStage).title(IMconstant.APPL_NAME)
			.masthead("VBL-Tool zur Verwaltung der Integrationsszenarios")
			.message("\nProgramm-Version 1.2.0 - 05.12.2014\n" +
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
		Dialogs.create().title(IMconstant.APPL_NAME)
					    .masthead(javaVersion)
					    .message(message)
					    .showInformation();
	}

//    @FXML
//    void newInNr(ActionEvent event) {
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
//			integrationList.add(newEE);
//			if (newEE.getInNr() > maxInNr) 
//				maxInNr = newEE.getInNr();
//			tableInNrAuswahl.getSelectionModel().select(newEE);
//    	}
//    }    

    @FXML    
    void handleIntegrationLoeschen(ActionEvent event) {
    	integrationLoeschen();
    }
    private void integrationLoeschen() {
    	Integration selectedlistElement = tableInNrAuswahl.getSelectionModel().getSelectedItem();
    	if (selectedlistElement != null) {
    		String inNr = selectedlistElement.inNrStrExp().getValueSafe();
    		Action response = Dialog.Actions.OK;
    		if (selectedlistElement.getInKomponente() != null) {
    			response = Dialogs.create()
					.owner(primaryStage).title(IMconstant.APPL_NAME)
					.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
					.masthead(IMconstant.SICHERHEITSABFRAGE)
					.message("Integration mit der Nr. " + inNr + " wirklich löschen?")
					.showConfirm();
    		}
    		if (response == Dialog.Actions.OK) {
    			Integration integration = entityManager.find(Integration.class, selectedlistElement.getId());
    			if (integration==null) {
    				logger.error("FEHLER: Integration " + inNr + " ist nicht (mehr) gespeichert");
    			}
    			else {
	        		entityManager.getTransaction().begin();
	        		Iterator<InEmpfaenger> empf = integration.getInEmpfaenger().iterator();
	        		while (empf.hasNext()) {
	        			entityManager.remove(empf.next());
	        			empf.remove();
	        		}
	        		if (integration.getInSzenario() != null) {
	        			integration.getInSzenario().getIntegration().remove(integration);
	        		}
	        		entityManager.remove(integration);
	        		entityManager.getTransaction().commit();
	        		setInfoText("Integration mit der Nr. " + inNr + " erfolgreich gelöscht");
    			}	
    			integrationList.remove(selectedlistElement);
    			tableInNrAuswahl.getSelectionModel().clearSelection();
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
    static String initialExportFileName = "IM-List-Export-" + LocalDate.now() + ".xlsx";

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
   		
   		Action response = Dialog.Actions.CANCEL;
    	if (file != null) {
			initialExportFilePath = file.getParent();
			initialExportFileName = file.getName();
	   		response = Dialog.Actions.YES;
    	}
    	while (response == Dialog.Actions.YES) {
			Cursor aktCursor = primaryStage.getScene().getCursor();
			primaryStage.getScene().setCursor(Cursor.WAIT);
    		ExportToExcel export = new ExportToExcel(entityManager);
    		try {
    			int lines = export.write(file);
    			primaryStage.getScene().setCursor(aktCursor);
    			Dialogs.create().owner(primaryStage).title(IMconstant.APPL_NAME)
    					.masthead(null)
    					.message(lines + " Zeilen in der Datei " + file.getName() + 
    							" im Verzeichnis " + file.getParent() +" gespeichert")
    					.showInformation();
    			response = Dialog.Actions.CLOSE;
    		} catch (FileNotFoundException e) {
				primaryStage.getScene().setCursor(aktCursor);
				response = Dialogs.create().owner(primaryStage).title(IMconstant.APPL_NAME)
						.masthead("HINWEIS")
						.message(e.getMessage() + "\n\nVorgang wiederholen?")
						.actions(Dialog.Actions.YES, Dialog.Actions.CANCEL)
						.showConfirm();
    		} catch (IOException e) {
				primaryStage.getScene().setCursor(aktCursor);
				Dialogs.create().owner(primaryStage).title(IMconstant.APPL_NAME)
						.masthead("FEHLER")
						.message("Fehler beim Speichern der Exportdatei")
						.showException(e);
				file = null;
			}
    	}
    }
    
    public void refreshKontaktReferences() {
    	if (ansprechpartnerController.getAnsprechpartner() != null) {
    		ansprechpartnerController.readInKomponentenListeforPerson();
    	}
    }
    
    public void setupEntityManager() {
    	final String PERSISTENCE_UNIT = IMconstant.PERSISTENCE_UNIT_NAME;
    	logger.info("Datenbankverbindung zu {} wird aufgebaut",PERSISTENCE_UNIT);
    	EntityManagerFactory factory = null;
    	try {
    		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    	} catch (RuntimeException e) {
    		String msg = "Fehler beim Öffnen der Datenbank (" + PERSISTENCE_UNIT + ")";
    		System.out.println(msg);
    		logger.error(msg + "\nMessage:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage).title(IMconstant.APPL_NAME)
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
    	if (dbName.startsWith("xid")) {
    		IMconstant.APPL_NAME += " DEV"; 
    	}
    	logger.info("Datenbankverbindung zur DB \"" + dbName + "\" erfolgreich hergestellt.");
    }
    
    private void checkFieldsFromView() throws Exception {
    	logger.info("entered");
    	assert integration 				!= null : "fx:id=\"integration\" was not injected: check your FXML file 'IM.fxml'.";
    	assert tabInNr					!= null : "fx:id=\"tabInNr\" was not injected: check your FXML file 'IM.fxml'.";

        assert tabPaneObjekte 			!= null : "fx:id=\"tabPaneObjekte\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabPartner 				!= null : "fx:id=\"tabPartner\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabSysteme				!= null : "fx:id=\"tabSysteme\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabKomponenten			!= null : "fx:id=\"tabKomponenten\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabInSzenarien			!= null : "fx:id=\"tabInSzenarien\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabKonfigurationen		!= null : "fx:id=\"tabKonfigurationen\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabAnsprechpartner		!= null : "fx:id=\"tabAnsprechpartner\" was not injected: check your FXML file 'IM.fxml'.";
        assert tabGeschaeftsobjekte		!= null : "fx:id=\"tabGeschaeftsobjekte\" was not injected: check your FXML file 'IM.fxml'.";

        assert txtInfoZeile				!= null : "fx:id=\"txtInfoZeile\" was not injected: check your FXML file 'IM.fxml'.";

        assert tableInNrAuswahl 			!= null : "fx:id=\"tableInNrAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlInNr 			!= null : "fx:id=\"tColAuswahlInNr\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlInNrInSzenario	!= null : "fx:id=\"tColAuswahlInNrInSzenario\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelKompoKomponten 		!= null : "fx:id=\"tColSelKompoKomponten\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelSystemSystemName 		!= null : "fx:id=\"tColSelSystemSystemName\" was not injected: check your FXML file 'IM.fxml'.";
        
        assert tColSelInSzenarioNr 			!= null : "fx:id=\"tColSelInSzenarioNr\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColSelInSzenarioName 		!= null : "fx:id=\"tColSelInSzenarioName\" was not injected: check your FXML file 'IM.fxml'.";
        
        assert tablePartnerAuswahl 			!= null : "fx:id=\"tablePartnerAuswahl\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlPartnerKomponenten != null : "fx:id=\"tColAuswahlPartnerKomponenten\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlPartnerName 		!= null : "fx:id=\"tColAuswahlPartnerName\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlInNrSender 		!= null : "fx:id=\"tColAuswahlInNrSender\" was not injected: check your FXML file 'IM.fxml'.";
        assert tColAuswahlInNrBezeichnung 	!= null : "fx:id=\"tColAuswahlInNrBezeichnung\" was not injected: check your FXML file 'IM.fxml'.";

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
