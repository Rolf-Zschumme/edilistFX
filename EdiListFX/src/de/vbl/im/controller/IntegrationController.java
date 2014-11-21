
package de.vbl.im.controller;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;
import org.tmatesoft.svn.core.SVNException;

import de.vbl.im.controller.subs.DokumentAuswaehlenController;
import de.vbl.im.controller.subs.KomponentenAuswahlController;
import de.vbl.im.controller.subs.NeueIntegrationController;
import de.vbl.im.controller.subs.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.im.model.DokuLink;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.Intervall;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.Repository;


public class IntegrationController {
	private static final Logger logger = LogManager.getLogger(IntegrationController.class.getName()); 
	private static final String INR_PANE_PREFIX = " I-Nr. ";
	private static final Integer MAX_EMPFAENGER = 3;
	private static final String DEFAULT_KONFIG_NAME = "Ohne XI/PO-Konfiguration";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

	private final ObjectProperty<Integration> integration;

	@FXML private TitledPane m_InSzenarioPane;
	@FXML private AnchorPane m_IntegrationPane;
    @FXML private TabPane tabPaneInNr;
    @FXML private Tab tabAktInNr;
    
    @FXML private ComboBox<Konfiguration> cmbKonfiguration;
    @FXML private ComboBox<InSzenario> cmbInSzenario;
    @FXML private Button m_SpeichernBtn;
    @FXML private Button m_NeuAnlageBtn;  // not used because always enabled  
    @FXML private Button m_LoeschenBtn;
    @FXML private Button m_NewInSzenarioBtn;
    @FXML private Button m_NewConfigurationBtn;
    @FXML private Button m_NewDokuLinkBtn;
    @FXML private Button m_RemoveDokuLinkBtn;
    
    @FXML private TableView<DokuLink> tvDokuLinks;
    @FXML private TableColumn<DokuLink, String> tColDokumentVorhaben;
    @FXML private TableColumn<DokuLink, String> tColDokumentName;
    @FXML private TableColumn<DokuLink, LocalDateTime> tColDokumentDatum;
    @FXML private TableColumn<DokuLink, String> tColDokumentQuelle;
    @FXML private TableColumn<DokuLink, String> tColDokumentRevision;
    @FXML private TableColumn<DokuLink, String> tColDokumentPfad;
    
    @FXML private TextArea  taBeschreibung;
    @FXML private ComboBox<String> cmbIntervall;
    @FXML private ComboBox<String> cmbBuOb1;
    @FXML private ComboBox<String> cmbBuOb2;
    @FXML private ComboBox<String> cmbBuOb3;
    @FXML private TextField tfLastChange;
    @FXML private TextField tfBezeichnung;
    @FXML private DatePicker dpProduktivSeit;
    @FXML private DatePicker dpProduktivBis;

    @FXML private Button btnSender;
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    
    @FXML private MenuButton mbtEmpfaenger2;
    @FXML private MenuButton mbtEmpfaenger3;
    
    private static Stage primaryStage = null;
    private static String applName = null;
	private static IMController managerController;
    private static EntityManager entityManager = null;

    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger1IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger2IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger3IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty buOb1Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb2Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb3Exist = new SimpleBooleanProperty(false);
    private BooleanProperty readOnlyAccess = new SimpleBooleanProperty(false);
    
    private Map<String,GeschaeftsObjekt> businessObjectMap; 
    private ObservableList<String> businessObjectName = FXCollections.observableArrayList();
    private ObservableList<String> intervallNameList  = FXCollections.observableArrayList();
    private ObservableList<DokuLink> dokuLinkList     = FXCollections.observableArrayList();

    private static class IntegrationPlus {
    	private int inNr;
    	private InSzenario inSzenario;
    	private Konfiguration konfiguration;
    	private String bezeichnung;
    	private String beschreibung;
    	private LocalDate seitDatum;
    	private LocalDate bisDatum;
    	private InKomponente sender; 
    	private InEmpfaenger empfaenger[] = new InEmpfaenger[MAX_EMPFAENGER];
    	private InKomponente empfaengerKomponente[] = new InKomponente[MAX_EMPFAENGER];
    	private GeschaeftsObjekt geschaeftsObjekt[] = new GeschaeftsObjekt[MAX_EMPFAENGER];
    	private String intervallName;
    	
    	void setData (Integration s) {
    		inNr = s.getInNr();
    		konfiguration = s.getKonfiguration();
    		inSzenario = konfiguration==null ? null : konfiguration.getInSzenario();
    		bezeichnung = s.getBezeichnung()==null ? "" : s.getBezeichnung();
    		beschreibung = s.getBeschreibung()==null ? "" : s.getBeschreibung();
    		intervallName = s.getIntervall()==null ? "" : s.getIntervall().getName();
    		sender = s.getInKomponente();
			String seitStr = s.seitDatumProperty().getValueSafe();
			seitDatum = seitStr.equals("") ? null : LocalDate.parse(seitStr);
			String bisStr = s.bisDatumProperty().getValueSafe();
			bisDatum = bisStr.equals("") ? null : LocalDate.parse(bisStr);
			int i=0;
			for(InEmpfaenger e : s.getInEmpfaenger()) {
				empfaenger[i] = e;
				empfaengerKomponente[i] = e.getKomponente();
				geschaeftsObjekt[i++] = e.getGeschaeftsObjekt();
			}
			while(i < MAX_EMPFAENGER) {
				empfaenger[i] = null;
				empfaengerKomponente[i] = null;
				geschaeftsObjekt[i++] = null;
			}
    	}
    }
    IntegrationPlus akt = new IntegrationPlus();
    IntegrationPlus org = new IntegrationPlus();
    
    
	public IntegrationController() {
    	this.integration = new SimpleObjectProperty<>(this, "integration", null);
		readOnlyAccess.set(false);
	}

	public static void setParent(IMController managerController) {
		logger.entry();
		logger.info("ManagerController:" + managerController);
		IntegrationController.managerController = managerController;
		IntegrationController.primaryStage = IMController.getStage();
		IntegrationController.entityManager = managerController.getEntityManager();
		logger.info("EntityManager:" + entityManager);
		applName = primaryStage.getTitle();
		logger.exit();
	}

    @FXML 
    void initialize() {
    	logger.info("init");
    	checkFieldFromView();
    	setupLocalBindings();
    	integration.addListener( (ov, oldEintrag ,newEintrag) -> {
    		if (oldEintrag != null) {
    			taBeschreibung.setText("");
    			tfBezeichnung.setText("");
    			btnEmpfaenger1.setText("");
    			btnEmpfaenger2.setText("");
    			btnEmpfaenger3.setText("");
    			tfLastChange.setText("");
    			tabAktInNr.setText(INR_PANE_PREFIX + "000");
    			btnSender.textProperty().unbind();
    		}
    		cmbInSzenario.setValue(null);
    		if (newEintrag == null) {
    			managerController.setInfoText("Neue Integration kann bearbeitet werden");
    			akt.seitDatum = null;
    			akt.bisDatum = null;
    		} else {
    			readBusinessObject();
    			readIntervalle();
    			readInSzenarioList();
    			akt.setData(newEintrag);
    			cmbInSzenario.getSelectionModel().select(akt.inSzenario);
    			cmbKonfiguration.getSelectionModel().select(akt.konfiguration);
    			tabAktInNr.setText(INR_PANE_PREFIX +  newEintrag.getInNrStr());
    			tfBezeichnung.setText(akt.bezeichnung);
    			taBeschreibung.setText(akt.beschreibung);
    			cmbIntervall.getSelectionModel().select(akt.intervallName);
    			if (akt.sender != null) {
    				btnSender.textProperty().bind(akt.sender.fullnameProperty());
    			} else {
    				btnSender.textProperty().unbind();
    				btnSender.setText("");
    			}
    			senderIsSelected.set(akt.sender != null);
    			setAktEmpfaenger();
    			
    			org.setData(newEintrag);
    			setLastChangeField(tfLastChange, newEintrag.getLaeDatum(), newEintrag.getLaeUser());
    			
    		}
    		dpProduktivSeit.setValue(akt.seitDatum);
    		dpProduktivBis.setValue(akt.bisDatum);
    		dataIsChanged.set(false);
		});
    }	
    
    
    private void setLastChangeField(TextField tf, String dateTime, String laeUser) {
    	if (dateTime == null) {
    		tf.setText("");
    		tf.setTooltip(null);
    	} else {	
    		LocalDateTime dt = LocalDateTime.parse(dateTime);
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy"); 
    		tf.setText(laeUser + "  " + formatter.format(dt));
    		String ttt = LocalTime.from(dt).toString().substring(0, 8);
    		tf.setTooltip(new Tooltip(ttt));
    	}
    }
    
	private void setupLocalBindings() {
		
		m_NewConfigurationBtn.disableProperty().bind(cmbInSzenario.getSelectionModel().selectedItemProperty().isNull());
		
		m_SpeichernBtn.disableProperty().bind(Bindings.not(dataIsChanged));
		m_LoeschenBtn.disableProperty().bind(this.integration.isNull());

		m_InSzenarioPane.disableProperty().bind(Bindings.isNull(integration));
		m_IntegrationPane.disableProperty().bind(Bindings.isNull(integration));
		
		setupInSzenarioComboBox();
		setupDokuLink();
		setupKonfigurationComboBox();
		
		
		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				akt.bezeichnung = newValue;
				setChangeFlag(!akt.bezeichnung.equals(org.bezeichnung));
			}	
			managerController.setErrorText(msg);
		});
		
		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				akt.beschreibung = newValue;
				setChangeFlag(!akt.beschreibung.equals(org.beschreibung));
			}	
			managerController.setErrorText(msg);
		});

		cmbIntervall.setItems(intervallNameList);
		
		cmbIntervall.valueProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null && akt.intervallName.equals(newValue) == false) {
				logger.debug("cmbIntervall.changed to " + newValue);
				akt.intervallName = newValue;
				setChangeFlag(!akt.intervallName.equals(org.intervallName));
			}
			managerController.setErrorText(msg);			
		});
		
		businessObjectMap = new HashMap<String,GeschaeftsObjekt>();		
		cmbBuOb1.setItems(businessObjectName);
		cmbBuOb2.setItems(businessObjectName);
		cmbBuOb3.setItems(businessObjectName);
		
		
		cmbBuOb1.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 0);
			if (checkedName != null) {
				cmbBuOb1.getSelectionModel().select(checkedName); // wegen Groß-/Kleinschrift
				buOb1Exist.set(true);
			}
		});
		cmbBuOb2.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 1);
			if (checkedName != null) {
				cmbBuOb2.getSelectionModel().select(checkedName); // wegen Groß-/Kleinschrift
				buOb2Exist.set(true);
			}
		});
		cmbBuOb3.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 2);
			if (checkedName != null) {
				cmbBuOb3.getSelectionModel().select(checkedName); // wegen Groß-/Kleinschrift
				buOb3Exist.set(true);
			}
		});

    	btnEmpfaenger1.disableProperty().bind(Bindings.not(senderIsSelected));
    	btnEmpfaenger2.disableProperty().bind(Bindings.not(buOb1Exist));
    	btnEmpfaenger3.disableProperty().bind(Bindings.not(buOb2Exist));
    
    	cmbBuOb1.disableProperty().bind(Bindings.not(empfaenger1IsSelected));
    	cmbBuOb2.disableProperty().bind(Bindings.not(empfaenger2IsSelected));
    	cmbBuOb3.disableProperty().bind(Bindings.not(empfaenger3IsSelected));
    	
    	btnEmpfaenger2.visibleProperty().bind(buOb1Exist);
    	cmbBuOb2.visibleProperty().bind(buOb1Exist);

    	mbtEmpfaenger2.visibleProperty().bind(buOb2Exist);
    	btnEmpfaenger3.visibleProperty().bind(buOb2Exist);
    	cmbBuOb3.visibleProperty().bind(buOb2Exist);
    	
    	mbtEmpfaenger3.visibleProperty().bind(buOb3Exist);
    	
    	dpProduktivSeit.setShowWeekNumbers(true);
    	dpProduktivBis.setShowWeekNumbers(true);
    	
    	dpProduktivSeit.setOnAction(event -> {
    		akt.seitDatum = dpProduktivSeit.getValue();
    		setChangeFlag(akt.seitDatum != org.seitDatum);
    		if (akt.bisDatum != null && akt.seitDatum.isAfter(akt.bisDatum)) {
        		managerController.setInfoText("Seit-Datum sollte vor Bis-Datum liegen");  			
    		} else {
    			managerController.setInfoText("");  			
    		}
    	});
    	
    	dpProduktivBis.setOnAction(event -> {
    		akt.bisDatum = dpProduktivBis.getValue();
    		setChangeFlag(akt.bisDatum != org.bisDatum);
    		if (akt.seitDatum != null && akt.bisDatum.isBefore(akt.seitDatum)) {
        		managerController.setInfoText("Bis-Datum sollte nach Set-Datum liegen");  			
    		} else {
    			managerController.setInfoText("");  			
    		}
    	});
	}
	
	private void setupInSzenarioComboBox() {
    	
		cmbInSzenario.setCellFactory((cmbBx) -> {
			return new ListCell<InSzenario>() {
				@Override
				protected void updateItem(InSzenario item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						setText(item.getName());
						managerController.setInfoText("");
					}
				}
			};
		});
		
		cmbInSzenario.setConverter(new StringConverter<InSzenario>() {
			@Override
			public String toString(InSzenario item) {
				return item==null ? null : item.getName();
			}
			@Override
			public InSzenario fromString(String string) {
				return null; // No conversion fromString needed
			}
		});
		
		// checks to be done before changing the inSzenario 
		cmbInSzenario.setOnAction((event) -> {
			if (akt.inNr == org.inNr &&				 
				akt.inSzenario == org.inSzenario) {
				if (verifyDokuLinkListIsUnchanged() == false) {
					// DokuLinkListe has been changed -> this changes may be lost  
					// ask User if changes should be stored
					Action response = Dialogs.create().owner(primaryStage)
							.title(SICHERHEITSABFRAGE)
							.message("Sollen die Änderungen an den Doku-Referenzen für " + akt.inSzenario.getName() +
									 " gespeichert werden?")
							.actions(Dialog.Actions.YES, Dialog.Actions.NO)
							.showConfirm();
					if (response == Dialog.Actions.YES) {
						try {
							entityManager.getTransaction().begin();
							updateDokuLinkListInDatabase();
							entityManager.getTransaction().commit();
							logger.info("Transaktionstatus(isActive):" + entityManager.getTransaction().isActive());
						} catch (RuntimeException er) {
							Dialogs.create().owner(primaryStage)
							.title(applName).masthead("Datenbankfehler")
							.message("Fehler beim Speichern der DokuLinkList-Anderungen")
							.showException(er);
						}

					}
				}
			}
//			InSzenario selInSzenario = cmbInSzenario.getSelectionModel().getSelectedItem();
//			logger.info("selected InSzenario:" + selInSzenario.getName());
//			setChangeFlag(akt.inSzenario != org.inSzenario);
//			akt.inSzenario = selIszneario;
//			readCmbKonfigurationList(akt.inSzenario);
		});
		
		cmbInSzenario.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
			logger.trace("selected InSzenario:" + (newValue == null ? "null" : newValue.getName()) + 
					  				   " (old:" + (oldValue == null ? "null" : oldValue.getName()) + ")");
			setChangeFlag(newValue != org.inSzenario);
			akt.inSzenario = newValue;
			// refresh dokuLinkList
			if (oldValue != newValue) {
				dokuLinkList.clear();
				if(akt.inSzenario != null) {
					dokuLinkList.addAll(akt.inSzenario.getDokuLink());
				}
				managerController.setInfoText("");
			}
			readCmbKonfigurationList(akt.inSzenario);
			checkBezeichnungUpdate();
		});
	}
	
    private void checkBezeichnungUpdate() {
    	
		// TODO Auto-generated method stub
	}

	private void setupDokuLink() {
		m_NewDokuLinkBtn.disableProperty().bind(cmbInSzenario.getSelectionModel().selectedItemProperty().isNull());
		m_RemoveDokuLinkBtn.disableProperty().bind(tvDokuLinks.getSelectionModel().selectedItemProperty().isNull());	
		tvDokuLinks.setItems(dokuLinkList);
		tColDokumentVorhaben.setCellValueFactory(cellData -> cellData.getValue().vorhabenProperty());
		tColDokumentName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
//		tColDokumentPfad.setCellValueFactory(cellData -> cellData.getValue().pfadProperty());
		
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
		
		tColDokumentDatum.setCellValueFactory(cellData -> cellData.getValue().datumProperty());
		tColDokumentDatum.setCellFactory(column -> {
			return new TableCell<DokuLink, LocalDateTime>() {
				@Override
				protected void updateItem (LocalDateTime item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(dtf.format(item));
					}
				}
			};
		});
		
		tColDokumentQuelle.setCellValueFactory(cellData -> cellData.getValue().getRepository().nameProperty());
		tColDokumentPfad.setCellValueFactory(cellData -> cellData.getValue().pfadProperty());
		tColDokumentRevision.setCellValueFactory(cellData -> Bindings.format("%5d",cellData.getValue().revisionProperty()));


    	tvDokuLinks.setRowFactory(new Callback<TableView<DokuLink>, TableRow<DokuLink>>() {
			@Override
			public TableRow<DokuLink> call(TableView<DokuLink> table) {
				final TableRow<DokuLink> row = new TableRow<DokuLink>();
				final ContextMenu contextMenu = new ContextMenu();
				
				final MenuItem openMenuItem = new MenuItem("Öffnen...");
				openMenuItem.setOnAction( event -> { 
					dokumentExternAnzeigen(row.getItem()); 	
				});
				final MenuItem validateMenuItem = new MenuItem("Prüfen der Aktualität...");
				validateMenuItem.setOnAction(event -> { 
					managerController.setInfoText("Diese Option ist noch nicht realisiert");
				});
				final MenuItem removeMenuItem = new MenuItem("Löschen");
				removeMenuItem.setOnAction( event -> {
					removeDokuLinkfromList(row.getItem());
				});
				contextMenu.getItems().addAll(openMenuItem, validateMenuItem, removeMenuItem);
				
				row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu)null).otherwise(contextMenu));
				return row;
			}
		});
	}

    private void dokumentExternAnzeigen(DokuLink doku) {
    	
		Repository repository = doku.getRepository();
		try {
			repository.open();
		} catch (SVNException e) {
			managerController.setErrorText("Fehler beim öffen des Repository " + repository.getName() + " :" + e.getMessage());
			return;
		}
		String filePath = repository.getStartPfad() + doku.getPfad() + "/" + doku.getName();
		ByteArrayOutputStream baos = repository.getFileStream(filePath, -1);
		
		int extPos = filePath.indexOf(".");
		String ext = filePath.substring(extPos);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(doku.getName() + ext);
			baos.writeTo(fos);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			managerController.setErrorText(e.getMessage());
			return;
		} finally {
			try {
				if (baos != null) baos.close();
				if (fos != null)  fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				managerController.setErrorText(e.getMessage());
			}
		}
		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}
		try {
			desktop.open(new File(doku.getName() + ext));
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			managerController.setErrorText(e.getMessage());
		}
	}
       
    @FXML 
    void actionDocumentContextMenuRequested() {
    	actionNewDokuLink();
    }
    
    @FXML 
    void actionNewDokuLink() {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	DokumentAuswaehlenController controller = managerController.loadDokumentAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		String userInfo = "Dokumentenauswahl wurde abgebrochen";
    		if (controller.getResponse() == Actions.OK) {
    			DokuLink dokuLink = controller.getSelectedDokuLink();
    			String dokName = dokuLink.getName();
    			userInfo = "Der Verweis auf das Dokument '" + dokName + "' ";
    			if (dokuLinkListContains(dokuLink) == true) {
    				userInfo += "ist bereits vorhanden";
    			} else {
    				dokuLinkList.add(dokuLink);
    				userInfo += "wurde eingetragen";
    			}
    			setChangeFlag(org.inSzenario == null || org.inSzenario.getDokuLink() == null);
    		}
        	managerController.setInfoText(userInfo);
    	}
    }
    private boolean dokuLinkListContains(DokuLink newDokuLink) {
    	String newName = newDokuLink.getName();
    	String newPfad = newDokuLink.getPfad();
    	for (DokuLink dokuLink : dokuLinkList) {
    		if (dokuLink.getName().equals(newName) && dokuLink.getPfad().equals(newPfad)) {
    			return true;
    		}
    	}
    	return false;
    }


	@FXML 
    void actionRemoveDokuLink() {
    	removeDokuLinkfromList(tvDokuLinks.getSelectionModel().selectedItemProperty().get());
    }
    
    private void removeDokuLinkfromList (DokuLink tobeRemoved) {
    	String dokname = tobeRemoved.getName();
    	tvDokuLinks.getItems().remove(tobeRemoved);
    	setChangeFlag(org.inSzenario == null || org.inSzenario.getDokuLink() == null);
    	managerController.setInfoText("Der Verweis auf das Dokument '" + dokname + "' wurde entfernt");
    }
    
    
	private void setupKonfigurationComboBox() {
		
		cmbKonfiguration.disableProperty().bind(cmbInSzenario.getSelectionModel().selectedItemProperty().isNull());
		
		cmbKonfiguration.setCellFactory((cmbBx) -> {
			return new ListCell<Konfiguration>() {
				@Override
				protected void updateItem(Konfiguration item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						setText(item.getName());
					}
				}
			};
		});
		cmbKonfiguration.setConverter(new StringConverter<Konfiguration>() {
			@Override
			public String toString(Konfiguration item) {
				if (item == null) {
					return null;
				} else {
					return item.getName();
				}
			}
			@Override
			public Konfiguration fromString(String string) {
				return null; // No conversion fromString needed
			}
		});

		final HashMap<Integer, Tab> tabMapAfter = new HashMap<Integer,Tab>();				
		final HashMap<Integer, Tab> tabMapBefore = new HashMap<Integer,Tab>();
		
		cmbKonfiguration.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//			log("cmbKonfiguration.changed"," newValue=" +newValue);
			akt.konfiguration = newValue;
			setChangeFlag(newValue != org.konfiguration);
			
			// zusätzliche inNr-Reiter aktualisieren (entfernen/ergänzen)
			
			tabPaneInNr.getTabs().retainAll(tabAktInNr);
			if (newValue != null && newValue.getIntegration() != null) {
				Iterator<Integration> i = newValue.getIntegration().iterator();
//				log("cmbKonfiguration.changed","extraTab="+extraTab);
				tabMapBefore.clear();
				tabMapAfter.clear();
				while (i.hasNext()) {
					Integration e = i.next();
					int inNr = e.getInNr();
					if (inNr != akt.inNr ) {
						Tab extraTab = new Tab(INR_PANE_PREFIX + e.getInNrStr());
						extraTab.setUserData(e);
						if (inNr  < akt.inNr ) tabMapBefore.put(inNr, extraTab);
						if (inNr  > akt.inNr ) tabMapAfter.put(inNr, extraTab);
					}	
				}
				if (tabMapAfter.size() > 0) {
					tabPaneInNr.getTabs().addAll(1, tabMapAfter.values());
				}	
				if (tabMapBefore.size() > 0) {
					tabPaneInNr.getTabs().addAll(0, tabMapBefore.values());
				}
			}
			managerController.setInfoText("");
		});
		
		// check if user want to change the current entity
		tabPaneInNr.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->  {
			Node node = (Node) event.getTarget();
			if (node instanceof Text) {
				Parent parent = node.getParent();
				if (parent instanceof Label) {
					Label label = (Label) parent;
					// compare label-text with all Integration-Panel-text 
					Integration e = null;
					for(Tab t : tabPaneInNr.getTabs()) {
						if (t.getText() == label.getText()) {
							if (t.getUserData() instanceof Integration) {
								e = (Integration) t.getUserData();
								break;
							}
						}
					}
					if (e != null) {
						if(checkForChangesAndAskForSave()) {
							managerController.setSelectedIntegration(e);
						}
						event.consume();
					}
				} 
			}
			
		});
		
		tabPaneInNr.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			Node node = (Node) event.getTarget();
			if (node instanceof TabPane) {
				Integration e = null;
				TabPane tabPane = (TabPane) node;
				logger.debug(tabPane.getSelectionModel().selectedItemProperty().get());
				e = (Integration) tabPane.getSelectionModel().selectedItemProperty().get().getUserData();
				if (e != null) {
					if(checkForChangesAndAskForSave()) {
						managerController.setSelectedIntegration(e);
					}
				}
			}
		});		
	}
	
	// prüft ob das eingegebene BO (newName) in der BO-Tabelle (businessObjektMap) 
	// bereits vorhanden ist.  

	private String checkBusinessObjectName(String newName, int index) {
		String aktName = null;
		if (newName != null) {
			GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
			if (buOb != null && buOb == org.geschaeftsObjekt[index]) {
				akt.geschaeftsObjekt[index] = buOb; 
			} else {
				if (buOb == null) {
					buOb = askForNewBusinessObjektName(newName);
				}	
				if (buOb != null) {
					akt.geschaeftsObjekt[index] = buOb; 
					aktName = buOb.getName();
				}
			}
			setChangeFlag(akt.geschaeftsObjekt[index] != org.geschaeftsObjekt[index]);
		}
		return aktName;
	}
	
	private GeschaeftsObjekt askForNewBusinessObjektName(String newName) {
		Optional<String> aktName = Dialogs.create()
				.owner(primaryStage)
				.title(applName)
				.message("Soll das folgende Geschäftsobjekt gespeichert werden?")
				.showTextInput(newName);
		if (aktName.isPresent()) {
			return (geschaeftsObjektAnlegen(aktName.get()));
		}	
		return null;
	}

	private GeschaeftsObjekt geschaeftsObjektAnlegen(String aktName) {
		
		String select = "SELECT g FROM GeschaeftsObjekt g WHERE g.name= '" + aktName + "'";
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(select, GeschaeftsObjekt.class);
		List<GeschaeftsObjekt> gList = tq.getResultList();
		
		if (gList.size() > 0) {
			managerController.setInfoText("Geschäftsobjekt ist bereits "+ gList.size() +" mal vorhanden");
			return gList.get(0);
		}
		try {
			GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(aktName);
			entityManager.getTransaction().begin();
			entityManager.persist(newBusObj);
			entityManager.getTransaction().commit();
			businessObjectName.add(aktName);
			businessObjectMap.put(aktName.toUpperCase(), newBusObj);
			managerController.setInfoText("Das Geschäftsobjekt \"" + 
					aktName + "\" wurde erfolgreich gespeichert");
			return newBusObj;
		} catch (RuntimeException er) {
			Dialogs.create().owner(primaryStage)
			.title(applName).masthead("Datenbankfehler")
			.message("Fehler beim speichern des Geschäftsobjektes")
			.showException(er);
		}
		return null;
	}	

	private void readBusinessObject() {
		businessObjectMap.clear();
		businessObjectName.clear();
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(
				"SELECT g FROM GeschaeftsObjekt g ORDER BY g.name", GeschaeftsObjekt.class);
		final List<GeschaeftsObjekt> gList = tq.getResultList();
		for (GeschaeftsObjekt gObject : gList) {
			businessObjectName.add(gObject.getName());
			businessObjectMap.put(gObject.getName().toUpperCase(), gObject);
		}
	}
	
	private void readIntervalle() {
		intervallNameList.clear();
		TypedQuery<Intervall> tq = entityManager.createQuery(
				"SELECT i FROM Intervall i ORDER BY i.name", Intervall.class);
		final List<Intervall> iList = tq.getResultList();
		for (Intervall iObject : iList) {
			intervallNameList.add(iObject.getName());
		}
	}
	
	private void readInSzenarioList() {
        final ObservableList<InSzenario> aktList = FXCollections.observableArrayList();
		TypedQuery<InSzenario> tq = entityManager.createQuery(
				"SELECT i FROM InSzenario i ORDER BY i.name", InSzenario.class);
		
		aktList.addAll(tq.getResultList());
		if (cmbInSzenario.getItems().isEmpty()) {
			cmbInSzenario.setItems(aktList);
		}
		else {
			cmbInSzenario.getItems().retainAll(aktList);
			cmbInSzenario.getItems().setAll(aktList);
		}
		
	}

	private void readCmbKonfigurationList(InSzenario inSzenario) {
		cmbKonfiguration.getItems().clear();
		TypedQuery<Konfiguration> tq = entityManager.createQuery(
				"SELECT k FROM Konfiguration k WHERE k.inSzenario = :i ORDER BY k.name", Konfiguration.class);
		tq.setParameter("i", inSzenario);
		
		ObservableList<Konfiguration> aktList = FXCollections.observableArrayList(tq.getResultList());

		// find default KONFIGURATION in DB-table
		Boolean found = false;  
		for (Konfiguration k : aktList) {
			if (DEFAULT_KONFIG_NAME.equals(k.getName())) {
				found = true;
				break;
			}
		}
		if (!found) {
			Konfiguration defKonfig = new Konfiguration(DEFAULT_KONFIG_NAME);
			aktList.add(defKonfig);
		}
		cmbKonfiguration.setItems(aktList);
	} 
	
    private void setAktEmpfaenger() {
		if (akt.empfaengerKomponente[0] != null) {
			btnEmpfaenger1.setText(akt.empfaengerKomponente[0].getFullname());
			empfaenger1IsSelected.set(true);
			cmbBuOb1.getSelectionModel().select(akt.geschaeftsObjekt[0].getName());
			buOb1Exist.set(akt.geschaeftsObjekt[0] != null);
		}
		else {
			btnEmpfaenger1.setText("");
			empfaenger1IsSelected.set(false);
			cmbBuOb1.getSelectionModel().select(null);
			buOb1Exist.set(false);
		}
		if (akt.empfaengerKomponente[1] != null) {
			btnEmpfaenger2.setText(akt.empfaengerKomponente[1].getFullname());
			empfaenger2IsSelected.set(true);
			cmbBuOb2.getSelectionModel().select(akt.geschaeftsObjekt[1].getName());
			buOb2Exist.set(akt.geschaeftsObjekt[1] != null);
		}
		else {
			btnEmpfaenger2.setText("");
			empfaenger2IsSelected.set(false);
			cmbBuOb2.getSelectionModel().select(null);
			buOb2Exist.set(false);
		}
		if (akt.empfaengerKomponente[2] != null) {
			btnEmpfaenger3.setText(akt.empfaengerKomponente[2].getFullname());
			empfaenger3IsSelected.set(true);
			cmbBuOb3.getSelectionModel().select(akt.geschaeftsObjekt[2].getName());
			buOb3Exist.set(akt.geschaeftsObjekt[2] != null);
		}
		else {
			btnEmpfaenger3.setText("");
			empfaenger3IsSelected.set(false);
			cmbBuOb3.getSelectionModel().select(null);
			buOb3Exist.set(false);
		}
    }

    @FXML
    void actionNeuanlage(ActionEvent event) {

    	if (checkForChangesAndAskForSave() == false) {
    		return;
    	}
    	FXMLLoader loader = new FXMLLoader();
    	String fullname = "subs/NeueIntegration"
    			+ ".fxml";
    	loader.setLocation(getClass().getResource(fullname));
    	if (loader.getLocation()==null) {
    		logger.error("Resource not found :" + fullname);
    		managerController.setErrorText("FEHLER: Resource ("+fullname+") not found");
    		return;
    	}
    	try {
    		loader.load();
    	} catch (IOException e) {
    		logger.error("Fehler beim Laden der Resource:" + e.getMessage());
    		managerController.setErrorText("FEHLER: " + e.getMessage());
    		return;
    	}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	dialog.setTitle(primaryStage.getTitle());
    	
    	NeueIntegrationController dialogController = loader.getController();
    	dialogController.setEntityManager(entityManager);
    	dialogController.start();
    	
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + 250);
    	dialog.setY(primaryStage.getY() + 100);
    	dialog.showAndWait();
    	
    	if (dialogController.getResponse() == Dialog.Actions.OK) {
    		Integration newI = dialogController.getNewIntegration();
    		managerController.loadIntegrationListData();
    		managerController.setSelectedIntegration(newI);
    	}
    }    

    @FXML
    void actionLoeschen(ActionEvent event) {
    	managerController.handleIntegrationLoeschen(event);
    }
    

    private static enum Checkmode { CHECK_ONLY, ASK_FOR_UPDATE, SAVE_DONT_ASK };

    private void setChangeFlag(Boolean different) {
    	if (!different) {
    		different = !checkForChangesWithMode(Checkmode.CHECK_ONLY);
    	}
    	logger.debug("Status dataIsChanged wird auf " + different + " gesetzt");
    	dataIsChanged.set(different);
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private boolean checkForChangesWithMode(Checkmode checkmode) {
		if (integration.get() == null) {
			return true;
		}
		if (akt.konfiguration == org.konfiguration        &&
			akt.sender == org.sender                      &&
			verifyEmpfaengerAreUnchanged() == true        &&
			localDateEquals(akt.seitDatum, org.seitDatum) &&
			localDateEquals(akt.bisDatum, org.bisDatum)   &&
			akt.bezeichnung.equals(org.bezeichnung)       &&
			akt.beschreibung.equals(org.beschreibung)     &&
			akt.intervallName.equals(org.intervallName) &&
			verifyDokuLinkListIsUnchanged() == true			     )
		{
			logger.debug(checkmode + ": no change found -> no update");
			return true;  
		}
		if (checkmode == Checkmode.CHECK_ONLY) {
			logger.debug(checkmode + ": change found");
			return false; 
		}
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			Action response = Dialogs.create().owner(primaryStage)
							.title(applName).masthead(SICHERHEITSABFRAGE)
							.message("Soll die Änderungen an der Integration " + integration.get().getInNrStr() + 
									" \"" + integration.get().getBezeichnung() + "\" gespeichert werden?")
							.showConfirm();
			if (response == Dialog.Actions.CANCEL) {
				return false;
			} 
			if (response == Dialog.Actions.NO) {
				logger.info("Aenderungen nicht Speichern ausgewaehlt");
				return true;
			}
		}
		
		// start validation before insert/update
		
    	if (akt.sender == null) {
    		Dialogs.create().owner(primaryStage)
    		.title(applName).masthead("Korrektur-Hinweis")
    		.message("Sender ist erforderlich")
    		.showWarning();
    		btnSender.requestFocus();
    		return false;
    	}
    	for (int i=0; i<MAX_EMPFAENGER; ++i) {
    		InKomponente empf = akt.empfaengerKomponente[i];
    		if (empf == null) {
    			if (i==0) {
    				String msg = "Ein Empfänger ist erforderlich";
    	    		managerController.setErrorText(msg);
    				btnEmpfaenger1.requestFocus();
    				return false;
    			}
    		} else {    
    			if (akt.geschaeftsObjekt[i] == null || 
    				akt.geschaeftsObjekt[i].getName().length() < 1) {
    				
    				String msg = "Bitte zum Empfänger \"" + empf.getFullname() 
    					+ "\" auch ein Geschäftsobjekt eintragen/auswählen";
    	    		managerController.setErrorText(msg);
    				switch(i) {
    				case 0: cmbBuOb1.requestFocus(); break;
    				case 1: cmbBuOb2.requestFocus(); break;
    				case 2: cmbBuOb3.requestFocus(); break;
    				}
    				return false;
    			}
    		}
    	}
    	if(akt.inSzenario == null) {
    		managerController.setErrorText("Eine InSzenario muss ausgewählt oder angelegt werden");
    		cmbInSzenario.requestFocus();
    		return false;
    	}
    	if (akt.konfiguration == null) {
    		managerController.setErrorText("Eine Konfiguration muss ausgewählt oder angelegt werden");
    		cmbKonfiguration.requestFocus();
    		return false;
    	}
    	
    	// end of validation -> start update/insert
    	// ----------------------------------------
		try {
			Integration aktIn = integration.get();
			entityManager.getTransaction().begin();
			// if configuration changed the Integration must be removed from previous configuration	
			if (org.konfiguration != null && akt.konfiguration != org.konfiguration) {
				org.konfiguration.getIntegration().remove(aktIn);
			}
			if (akt.inSzenario.getId() == 0L) {
				entityManager.persist(akt.inSzenario);
			}
			if (akt.konfiguration.getId() == 0L) {    	// new configuration for persistence
				entityManager.persist(akt.konfiguration);
				akt.konfiguration.setInSzenario(akt.inSzenario);
			}
			if (akt.konfiguration.getIntegration().contains(aktIn) == false) {
				akt.konfiguration.getIntegration().add(aktIn);
			}
			aktIn.setKonfiguration(akt.konfiguration);
			
			updateDokuLinkListInDatabase();
			
			aktIn.setInKomponente(akt.sender); 
			aktIn.setBeschreibung(akt.beschreibung);
			
			if (!akt.intervallName.equals(org.intervallName)) {
				aktIn.setIntervall(newIntervall(akt.intervallName));
			}
			
			Collection<InEmpfaenger> tmpEmpfaengerList = new ArrayList<InEmpfaenger>();
			
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				InEmpfaenger empf = akt.empfaenger[i];
				if (empf == null && akt.empfaengerKomponente[i] != null) {
					empf = new InEmpfaenger();
					aktIn.getInEmpfaenger().add(empf);
					entityManager.persist(empf);
				}
				if (akt.empfaengerKomponente[i] != null) {
					empf.setIntegration(aktIn);
					empf.setKomponente(akt.empfaengerKomponente[i]);
					empf.setGeschaeftsObjekt(akt.geschaeftsObjekt[i]);
					tmpEmpfaengerList.add(empf);
// TODO				empf.getGeschaeftsObjekt().anzVerwendungenProperty().add(1);
				}
			}
			// InEmpfaenger at the original EmpfaengerList must be removed 
			// from the database if they are not in the new EmpfaengerList  
			//
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				InEmpfaenger empf = org.empfaenger[i];
				if (empf != null && tmpEmpfaengerList.contains(empf) == false) {
					entityManager.remove(empf);
				}
			}
			aktIn.setInEmpfaenger(tmpEmpfaengerList);
			
//			old: auto generation of field
//			String tmpBezeichnung = aktIn.autoBezeichnung(); 
//			if (aktIn.getBezeichnung().equals(tmpBezeichnung) == false) {
//				aktIn.setBezeichnung(tmpBezeichnung);
//				tfBezeichnung.textProperty().set(aktIn.autoBezeichnung());
//			}
//			new: normal manuell input
//			temp: set bezeichnung if emppty			
			if (akt.bezeichnung.isEmpty() || akt.bezeichnung.equals("(I-Nummer Reserviert)")) {
				tfBezeichnung.textProperty().set(Integration.autobezeichnung(
						akt.konfiguration, akt.sender,akt.geschaeftsObjekt[0]));
			}
			aktIn.setBezeichnung(akt.bezeichnung);
			
			LocalDate aktSeitDatum = dpProduktivSeit.getValue();
			aktIn.seitDatumProperty().set(aktSeitDatum==null ? "" : aktSeitDatum.toString());
			
			LocalDate aktBisDatum = dpProduktivBis.getValue();
			aktIn.bisDatumProperty().set(aktBisDatum==null ? "" : aktBisDatum.toString());
			
			aktIn.setLaeUser(System.getenv("USERNAME").toUpperCase());
			aktIn.setLaeDatum(LocalDateTime.now().toString());
			
			entityManager.getTransaction().commit();
			
			// do things that are necessary after DB-update:
			if (!akt.intervallName.equals(org.intervallName)) {
				readIntervalle();	// this will remove selection -> refresh 
				cmbIntervall.getSelectionModel().select(akt.intervallName);
			}
			setLastChangeField(tfLastChange, aktIn.getLaeDatum(), aktIn.getLaeUser());			
			akt.setData(aktIn);
			org.setData(aktIn);
			
			managerController.setInfoText("Die Integration " + aktIn.getInNrStr() +
										  " wurde gespeichert");
		} catch (RuntimeException e) {
			Dialogs.create().owner(primaryStage)
			.title(applName).masthead("Datenbankfehler")
			.message("Fehler beim Speichern der Integration")
			.showException(e);
		}	
		dataIsChanged.set(false);
    	
		return true;
	}	
	
	private void updateDokuLinkListInDatabase() {
		// if dokuLinks are removed they must be removed from database 
		if (akt.inSzenario.getDokuLink() != null) {
			Collection<DokuLink> toBeRemoved = new ArrayList<DokuLink>();
			for (DokuLink dok : akt.inSzenario.getDokuLink()) {
				if (dokuLinkList.contains(dok) == false) {
					toBeRemoved.add(dok);
				}
			}
			for (DokuLink dok : toBeRemoved) {
				akt.inSzenario.getDokuLink().remove(dok);
				entityManager.remove(dok);
			}
		}
		for (DokuLink dok : dokuLinkList) {
			if (dok.getId() == 0L) {
				entityManager.persist(dok);
			}
			if (akt.inSzenario.getDokuLink().contains(dok) == false) {
				akt.inSzenario.getDokuLink().add(dok);
			}
		}
	}

	private Intervall newIntervall(String iName) {
		logger.entry();
		Intervall intervall;
		String select = "SELECT i FROM Intervall i WHERE LOWER(i.name) = LOWER(:i)";
		TypedQuery<Intervall> tq = entityManager.createQuery(select, Intervall.class);
		tq.setParameter("i", iName);
		List<Intervall> iList = tq.getResultList();
		
		logger.info("iListe.size=" + iList.size());
		if (iList.size() == 0) {
			intervall = new Intervall();
			entityManager.persist(intervall);
		} else {
			intervall = iList.get(0);
		}
		intervall.setName(iName);
		return intervall;
	}

	private boolean localDateEquals(LocalDate x, LocalDate y) {
		if (x == null && y == null)
			return true;
		if (x != null && y != null) {
			return (x.compareTo(y)==0);
		}
		return false;
	}

	private boolean verifyDokuLinkListIsUnchanged() {
		Collection<DokuLink> orgDokuLink = (org.inSzenario == null) ? null : org.inSzenario.getDokuLink();
		if ( orgDokuLink == null) {
			return dokuLinkList.size() == 0; // org is 0 -> unchanged if dokLinkList == 0 
		}
		if (( orgDokuLink.size()  != dokuLinkList.size() )	  ||
			  orgDokuLink.containsAll(dokuLinkList) == false  ||
			  dokuLinkList.containsAll(orgDokuLink) == false  )
		{
			return false;
		}	
		return true;
	}
	
	private boolean verifyEmpfaengerAreUnchanged () {
		for(int i=0; i < MAX_EMPFAENGER; ++i ) {
			if (akt.empfaengerKomponente[i] != org.empfaengerKomponente[i] ||
					akt.geschaeftsObjekt[i] != org.geschaeftsObjekt[i]) {
				return false;
			}
		}
		return true;
	}
		
    @FXML
    void actionNewInSzenario(ActionEvent event) {
    	String aktName = "";
    	String masterhead = null;
		while (true) {
			Optional<String> newName = Dialogs.create()
				.owner(primaryStage).title(applName)
				.masthead(masterhead)
				.message("Wie soll das neue Integrationsszenario heißen?")
				.showTextInput(aktName);
			if ( !newName.isPresent() ) {
				managerController.setInfoText("Die Neuanlage wurde abgebrochen");
				break;
			}
			aktName = newName.get().trim();
			if (aktName.length() < 1) {
				masterhead = "Eine Eingabe ist erforderlich!\n" + 
							 "Bitte ändern oder abbrechen";
				continue;
			} 
			String sql="SELECT i FROM InSzenario i WHERE LOWER(i.name) = LOWER(:n)";
			TypedQuery<InSzenario> tq = entityManager.createQuery(sql, InSzenario.class);
			tq.setParameter("n", aktName);
			List<InSzenario> iList = tq.getResultList();
			
			if (iList.size() > 0) {
				masterhead = "InSzenario \"" +iList.get(0).getName() +"\" ist bereits vorhanden." + 
						  "\n Bitte ändern oder abbrechen";
				continue;
			}
			try {
				InSzenario inSzenario = new InSzenario(aktName);
				entityManager.getTransaction().begin();
				entityManager.persist(inSzenario);
				entityManager.getTransaction().commit();
				
				readInSzenarioList();
				cmbInSzenario.getSelectionModel().select(inSzenario);
				m_NewConfigurationBtn.requestFocus();
				
				managerController.setInfoText("Die InSzenario \"" + aktName + "\"" + 
					" wurde erfolgreich erstellt und hier ausgewählt");
				return;
			} catch (RuntimeException er) {
				Dialogs.create().owner(primaryStage)
					.title(applName).masthead("Datenbankfehler")
					.message("Fehler beim Anlegen einer neuen InSzenario")
					.showException(er);
			}
		}
    }	
    
    @FXML
    void newKonfiguration(ActionEvent event) {
    	String aktName = "";
    	String masterhead = null;
		while (true) {
			Optional<String> newName = Dialogs.create()
				.owner(primaryStage).title(applName)
				.masthead(masterhead)
				.message("Wie soll die neue Konfiguration heißen?")
				.showTextInput(aktName);
			if (newName.isPresent() == false) {
				managerController.setInfoText("Neuanlager einer Konfiguartion wurde vom Benutzer abgebrochen");
				break;
			}
			aktName = newName.get().trim();
			if (aktName.length() < 1) {
				masterhead = "Eine Eingabe ist erforderlich!" + 
						  "\n Bitte ändern oder abbrechen";
				continue;
			} 
			String sql="SELECT k FROM Konfiguration k WHERE k.inSzenario=:i AND LOWER(k.name) = LOWER(:n)";
			TypedQuery<Konfiguration> tq = entityManager.createQuery(sql, Konfiguration.class);
			tq.setParameter("i", akt.inSzenario);
			tq.setParameter("n", aktName);
			List<Konfiguration> kList = tq.getResultList();
			
			if (kList.size() > 0) {
				masterhead = "Konfiguration \"" +kList.get(0).getName() +"\" ist bereits vorhanden.\n" + 
						     "Bitte ändern oder abbrechen";
				continue;
			}
			try {
				Konfiguration konfiguration = new Konfiguration(aktName);
				entityManager.getTransaction().begin();
				entityManager.persist(konfiguration);
				konfiguration.setInSzenario(akt.inSzenario);
				entityManager.getTransaction().commit();
				
				readCmbKonfigurationList(akt.inSzenario);
				cmbKonfiguration.getSelectionModel().select(konfiguration);
				cmbKonfiguration.requestFocus();
				
				managerController.setInfoText("Die Konfiguartion \"" + aktName + "\"" + 
					" wurde der InSzenario \"" + akt.inSzenario.getName()  + "\"" +
					" erfolgreich zugefügt und hier ausgewählt");
				return;
			} catch (RuntimeException er) {
				Dialogs.create().owner(primaryStage)
					.title(applName).masthead("Datenbankfehler")
					.message("Fehler beim Anlegen einer neuen Konfiguration")
					.showException(er);
			}
		}	
    }
    
    //Action: Sender-Button is pressed
    @FXML
    void senderButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 
    	if (loader != null) {
    		KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    		komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, akt.sender, entityManager);
    		dialog.showAndWait();
    		if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    			InKomponente selKomponente = komponentenAuswahlController.getSelectedKomponente();
    			if (akt.sender != selKomponente ) {
    				akt.sender = selKomponente; 
    				btnSender.textProperty().unbind();
    				btnSender.textProperty().bind(akt.sender.fullnameProperty());
    				senderIsSelected.set(true);
    			}
    			setChangeFlag(akt.sender != org.sender);
    		}
    	}
    }

    //Action: EmpfaengerX-Button pressed
    @FXML
    void empfaengerButton1(ActionEvent event) {
    	String ret = empfaengerButton(0);
    	if (ret != null) {
			btnEmpfaenger1.setText(ret);
			empfaenger1IsSelected.set(true);
    	}
    }	
    @FXML
    void empfaengerButton2(ActionEvent event) {
    	String ret = empfaengerButton(1);
    	if (ret != null) {
			btnEmpfaenger2.setText(ret);
			empfaenger2IsSelected.set(true);
    	}
    }	
    @FXML
    void empfaengerButton3(ActionEvent event) {
    	String ret = empfaengerButton(2);
    	if (ret != null) {
			btnEmpfaenger3.setText(ret);
			empfaenger3IsSelected.set(true);
    	}
    }	
    	
    private String empfaengerButton(int btnNr) {
    	String ret = null;
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 400, 350); 
    	if (loader!= null) {
    		KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    		komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, akt.empfaengerKomponente[btnNr], entityManager);
    		dialog.showAndWait();
    		if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    			if (akt.empfaengerKomponente[btnNr] != komponentenAuswahlController.getSelectedKomponente()) {
    				akt.empfaengerKomponente[btnNr] = komponentenAuswahlController.getSelectedKomponente();
    				ret = akt.empfaengerKomponente[btnNr].getFullname();
    			}
    			setChangeFlag(akt.empfaengerKomponente[btnNr] != org.empfaengerKomponente[btnNr]);
    		}	
    	}
    	return ret;
    }

	@FXML
    void actionEmpfaenger2loeschen(ActionEvent event) {
		// if no.2 (line 3) exist --> move no.2 to no.1
    	if (akt.empfaengerKomponente[2] != null) {
    		akt.empfaengerKomponente[1] = akt.empfaengerKomponente[2];
    		akt.empfaengerKomponente[2] = null;
    		btnEmpfaenger2.setText(akt.empfaengerKomponente[1].getFullname());
    		
			akt.geschaeftsObjekt[1] = akt.geschaeftsObjekt[2];  // businessObjectMap.get(akt.busObjName[2].toUpperCase()).getName();
			akt.geschaeftsObjekt[2] = null;
			cmbBuOb2.getSelectionModel().select(akt.geschaeftsObjekt[1].getName());
			
	    	btnEmpfaenger3.setText("");
	    	cmbBuOb3.getSelectionModel().select(null);
    		buOb3Exist.set(false);
    		empfaenger3IsSelected.set(false);
    	} 
    	else { // if no.2 (line 3) is empty -> just delete no.2
    		akt.empfaengerKomponente[1] = null;
    		btnEmpfaenger2.setText("");
    		akt.geschaeftsObjekt[1] = null;
	    	cmbBuOb2.getSelectionModel().select(null);
    		buOb2Exist.set(false);
    		empfaenger2IsSelected.set(false);
    	}
    	dataIsChanged.set(true);
    }
    
    @FXML
    void actionEmpfaenger3loeschen(ActionEvent event) {
    	akt.empfaengerKomponente[2] = null;
    	btnEmpfaenger3.setText("");
    	akt.geschaeftsObjekt[2] = null;
		cmbBuOb3.getSelectionModel().select(null);
		empfaenger3IsSelected.set(false);
		buOb3Exist.set(false);
    	dataIsChanged.set(true);
    }
    
    private FXMLLoader loadKomponentenAuswahl(Stage dialog, int xOffset, int yOffset) {
    	FXMLLoader loader = new FXMLLoader();
    	String fullName = "subs/KomponentenAuswahl.fxml";
  		loader.setLocation(getClass().getResource(fullName));
    	if (loader.getLocation()==null) {
    		logger.error("Resource not found :" + fullName);
    		managerController.setErrorText("FEHLER: Resource ("+fullName+") not found");
    		return null;
    	}
    	try {
    		loader.load();
    	} catch (IOException e) {
    		e.printStackTrace(); 
    	}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	dialog.setTitle(primaryStage.getTitle());
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + xOffset);
    	dialog.setY(primaryStage.getY() + yOffset);
		return loader;
	}
    
	public final ObjectProperty<Integration> integrationProperty() {
		return integration;
	}
	
	public final Integration getIntegration() {
		return integration.get() ;
	}
	
	public final void setIntegration(Integration integration) {
		this.integration.set(integration);
	}

    private void checkFieldFromView() {
        assert m_InSzenarioPane		!= null : "fx:id=\"m_InSzenarioPane\"      was not injected: check your FXML file 'Integration.fxml'.";
        assert m_IntegrationPane    	!= null : "fx:id=\"m_IntegrationPane\"        was not injected: check your FXML file 'Integration.fxml'.";
        assert m_SpeichernBtn       != null : "fx:id=\"m_SpeichernBtn\"       was not injected: check your FXML file 'Integration.fxml'.";
        assert m_NewInSzenarioBtn  	!= null : "fx:id=\"m_NewInSzenarioBtn\"    was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbKonfiguration     != null : "fx:id=\"cmbKonfiguration\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_NewDokuLinkBtn     != null : "fx:id=\"m_NewDokuLinkBtn\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_RemoveDokuLinkBtn  != null : "fx:id=\"m_RemoveDokuLinkBtn\"  was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentName     != null : "fx:id=\"tColDokumentName\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentVorhaben != null : "fx:id=\"tColDokumentVorhaben\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentDatum    != null : "fx:id=\"tColDokumentDatum\"    was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentQuelle   != null : "fx:id=\"tColDokumentQuelle\"   was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentRevision != null : "fx:id=\"tColDokumentRevision\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentPfad     != null : "fx:id=\"tColDokumentPfad\"     was not injected: check your FXML file 'Integration.fxml'.";

        assert taBeschreibung	 != null : "fx:id=\"taBeschreibung\"      was not injected: check your FXML file 'Integration.fxml'.";
        assert integration 			 != null : "fx:id=\"integration\" 		     was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbIntervall 	 	 != null : "fx:id=\"cmbIntervall\" 		     was not injected: check your FXML file 'Integration.fxml'.";
        assert btnEmpfaenger1 		 != null : "fx:id=\"btnEmpfaenger1\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert btnEmpfaenger2		 != null : "fx:id=\"btnEmpfaenger2\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert btnEmpfaenger3		 != null : "fx:id=\"btnEmpfaenger3\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert m_NewConfigurationBtn != null : "fx:id=\"btnNewConfigurationBtn\" was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbInSzenario			 != null : "fx:id=\"cmbInSzenario\"           was not injected: check your FXML file 'Integration.fxml'.";
        assert tfBezeichnung		 != null : "fx:id=\"tfBezeichnung\" 		 was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbBuOb1				 != null : "fx:id=\"cmbBuOb1\" 			     was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbBuOb2				 != null : "fx:id=\"cmbBuOb2\" 			     was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbBuOb3				 != null : "fx:id=\"cmbBuOb3\" 			     was not injected: check your FXML file 'Integration.fxml'.";
        assert btnSender			 != null : "fx:id=\"btnSender\" 			 was not injected: check your FXML file 'Integration.fxml'.";
        assert mbtEmpfaenger2		 != null : "fx:id=\"mbtEmpfaenger2\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert mbtEmpfaenger3		 != null : "fx:id=\"mbtEmpfaenger3\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert tfLastChange		 != null : "fx:id=\"tfLastChange\" 		 was not injected: check your FXML file 'Integration.fxml'.";
        assert dpProduktivSeit		 != null : "fx:id=\"dpProduktivSeit\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert dpProduktivBis		 != null : "fx:id=\"dpProduktivBis\" 	     was not injected: check your FXML file 'Integration.fxml'.";
    }

    
    
}
