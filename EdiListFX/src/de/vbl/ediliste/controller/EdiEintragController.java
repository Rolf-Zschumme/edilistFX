
package de.vbl.ediliste.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;

import de.vbl.ediliste.controller.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.GeschaeftsObjekt;
import de.vbl.ediliste.model.Integration;
import de.vbl.ediliste.model.Konfiguration;


public class EdiEintragController {
	private static final String EDI_PANE_PREFIX = "EDI ";
	private static final Integer MAX_EMPFAENGER = 3;
	private static final String DEFAULT_KONFIG_NAME = "Ohne XI/PO-Konfiguration";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

	private final ObjectProperty<EdiEintrag> ediEintrag;

    private static class EdiEintragPlus {
    	int EdiNr;
    	Integration integration;
    	Konfiguration konfiguration;
    	String bezeichnung;
    	String beschreibung;
    	LocalDate seitDatum;
    	LocalDate bisDatum;
    	EdiKomponente sender; 
    	EdiEmpfaenger empfaenger[] = new EdiEmpfaenger[MAX_EMPFAENGER];
    	EdiKomponente empfaengerKomponente[] = new EdiKomponente[MAX_EMPFAENGER];
    	GeschaeftsObjekt geschaeftsObjekt[] = new GeschaeftsObjekt[MAX_EMPFAENGER];
    	
    	void setData (EdiEintrag s) {
    		EdiNr = s.getEdiNr();
    		konfiguration = s.getKonfiguration();
    		integration = konfiguration==null ? null : konfiguration.getIntegration();
    		bezeichnung = s.getBezeichnung()==null ? "" : s.getBezeichnung();
    		beschreibung = s.getBeschreibung()==null ? "" : s.getBeschreibung();
    		sender = s.getEdiKomponente();
			String seitStr = s.seitDatumProperty().getValueSafe();
			seitDatum = seitStr.equals("") ? null : LocalDate.parse(seitStr);
			String bisStr = s.bisDatumProperty().getValueSafe();
			bisDatum = bisStr.equals("") ? null : LocalDate.parse(bisStr);
			int i=0;
			for(EdiEmpfaenger e : s.getEdiEmpfaenger()) {
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
    static final EdiEintragPlus akt = new EdiEintragPlus();
    static final EdiEintragPlus org = new EdiEintragPlus();
    
	@FXML private AnchorPane ediEintragPane;
    @FXML private VBox eintragVBox;

    @FXML private TitledPane paneSzenario;
    @FXML private TitledPane paneAnbindung;
    @FXML private TitledPane paneEdiEintrag;
    @FXML private TabPane tabPaneEdiNr;
    @FXML private Tab tabAktEdiNr;
    
    @FXML private ComboBox<Konfiguration> cmbKonfiguration;
    @FXML private ComboBox<Integration> cmbIntegration;
    @FXML private Button btnNewSzenario;
    @FXML private Button btnNewConfiguration;
    
    @FXML private TextArea  taEdiBeschreibung;
    @FXML private ComboBox<String> cmbIntervall;
    @FXML private ComboBox<String> cmbBuOb1;
    @FXML private ComboBox<String> cmbBuOb2;
    @FXML private ComboBox<String> cmbBuOb3;
    @FXML private TextField ediLastChange;
    @FXML private TextField tfBezeichnung;
    @FXML private DatePicker dpProduktivSeit;
    @FXML private DatePicker dpProduktivBis;

    @FXML private Button btnSpeichern;
    @FXML private Button btnSender;
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    
    @FXML private MenuButton mbtEmpfaenger2;
    @FXML private MenuButton mbtEmpfaenger3;
    
    private static Stage primaryStage = null;
    private static String applName = null;
	private static EdiMainController mainController;
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

    
	public EdiEintragController() {
    	this.ediEintrag = new SimpleObjectProperty<>(this, "ediEintrag", null);
		readOnlyAccess.set(false);
	}

	public static void start(Stage primaryStage, EdiMainController mainController, EntityManager entityManager) {
		log("start","called");
		EdiEintragController.primaryStage = primaryStage;
		EdiEintragController.entityManager = entityManager;
		EdiEintragController.mainController = mainController;
		EdiEintragController.mainController.setInfoText("Hallo");
	}

    @FXML 
    void initialize() {
    	log("initialize","called");
    	checkFieldFromView();
    	ediEintrag.addListener(new ChangeListener<EdiEintrag>() {
    		@Override
    		public void changed (ObservableValue<? extends EdiEintrag> ov,
    				EdiEintrag oldEintrag, EdiEintrag newEintrag) {
    			if (oldEintrag != null) {
    				taEdiBeschreibung.setText("");
    				tfBezeichnung.setText("");
    				btnEmpfaenger1.setText("");
    				btnEmpfaenger2.setText("");
    				btnEmpfaenger3.setText("");
    				ediLastChange.setText("");
    				tabAktEdiNr.setText(EDI_PANE_PREFIX + "000");
    				btnSender.textProperty().unbind();
    			}
    			if (newEintrag == null) {
    				log("ediEintrag.changed","newEintrag=null");
    				cmbIntegration.getSelectionModel().select(null);
    			} else {
    				readBusinessObject();
    				readIntegrationList();
    				org.setData(newEintrag);
    				akt.integration = null;  
    				cmbIntegration.getSelectionModel().select(org.integration);
    				akt.setData(newEintrag);
   					cmbKonfiguration.getSelectionModel().select(akt.konfiguration);
    				tabAktEdiNr.setText(EDI_PANE_PREFIX +  newEintrag.getEdiNrStr());
    				tfBezeichnung.setText(akt.bezeichnung);
    				taEdiBeschreibung.setText(akt.beschreibung);
    				if (akt.sender != null) {
    					btnSender.textProperty().bind(akt.sender.fullnameProperty());
    				} else {
        				btnSender.textProperty().unbind();
        				btnSender.setText("");
    				}
    				senderIsSelected.set(akt.sender != null);
    				setAktEmpfaenger();
    				
    				setLastChangeField(ediLastChange, newEintrag.getLaeDatum(), newEintrag.getLaeUser());
    			}
    			dpProduktivSeit.setValue(akt.seitDatum);
    			dpProduktivBis.setValue(akt.bisDatum);
    			dataIsChanged.set(false);
    		}
		});
    	setupLocalBindings();
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
		log("setupLocalBindings","called");
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		setupIntegrationComboBox();
		setupKonfigurationComboBox();

		taEdiBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				akt.beschreibung = newValue;
				setChangeFlag(akt.beschreibung.equals(org.beschreibung));
			}	
		});

		businessObjectMap = new HashMap<String,GeschaeftsObjekt>();		
		cmbBuOb1.setItems(businessObjectName);
		cmbBuOb2.setItems(businessObjectName);
		cmbBuOb3.setItems(businessObjectName);
		
		cmbBuOb1.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 0);
			if (checkedName != null) {
				cmbBuOb1.getSelectionModel().select(checkedName); // wegen Gro�-/Kleinschrift
				buOb1Exist.set(true);
			}
		});
		cmbBuOb2.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 1);
			if (checkedName != null) {
				cmbBuOb2.getSelectionModel().select(checkedName); // wegen Gro�-/Kleinschrift
				buOb2Exist.set(true);
			}
		});
		cmbBuOb3.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 2);
			if (checkedName != null) {
				cmbBuOb3.getSelectionModel().select(checkedName); // wegen Gro�-/Kleinschrift
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
    		setChangeFlag(akt.seitDatum == org.seitDatum);
    	});
    	
    	dpProduktivBis.setOnAction(event -> {
    		akt.bisDatum = dpProduktivBis.getValue();
    		setChangeFlag(akt.bisDatum == org.bisDatum);
    	});
	}
	
    private void setupIntegrationComboBox() {

		cmbIntegration.setCellFactory((cmbBx) -> {
			return new ListCell<Integration>() {
				@Override
				protected void updateItem(Integration item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						setText(item.getName());
					}
				}
			};
		});
		
		cmbIntegration.setConverter(new StringConverter<Integration>() {
			@Override
			public String toString(Integration item) {
				return item==null ? null : item.getName();
			}
			@Override
			public Integration fromString(String string) {
				return null; // No conversion fromString needed
			}
		});
		
		cmbIntegration.setOnAction((event) -> {
			Integration selIntegration = cmbIntegration.getSelectionModel().getSelectedItem();
			if (selIntegration != akt.integration) {
				akt.integration = selIntegration;
				readCmbKonfigurationList(akt.integration);
			}	
			setChangeFlag(akt.integration == org.integration);
		});
	}

	private void setupKonfigurationComboBox() {
		cmbKonfiguration.disableProperty().bind(cmbIntegration.getSelectionModel().selectedItemProperty().isNull());
		
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
		cmbKonfiguration.setOnAction((event) -> {
			Konfiguration selKonfiguration = cmbKonfiguration.getSelectionModel().getSelectedItem();
			akt.konfiguration = selKonfiguration;
			setChangeFlag(akt.konfiguration == org.konfiguration);

			// zus�tzliche EdiNr-Reiter aktualisieren (entfernen/erg�nzen)
			
			tabPaneEdiNr.getTabs().retainAll(tabAktEdiNr);
			if (selKonfiguration != null && selKonfiguration.getEdiEintrag() != null) {
				HashMap<Integer, Tab> tabMapAfter = new HashMap<Integer,Tab>();				
				HashMap<Integer, Tab> tabMapBefore = new HashMap<Integer,Tab>();				
				Iterator<EdiEintrag> i = selKonfiguration.getEdiEintrag().iterator();
				Tab extraTab = new Tab();
				while (i.hasNext()) {
					EdiEintrag e = i.next();
					int iEdiNr = e.getEdiNr();
					if (iEdiNr != akt.EdiNr ) {
						extraTab = new Tab(EDI_PANE_PREFIX + e.getEdiNrStr());
						if (iEdiNr  < akt.EdiNr ) tabMapBefore.put(iEdiNr, extraTab);
						if (iEdiNr  > akt.EdiNr ) tabMapAfter.put(iEdiNr, extraTab);
					}	
				}
				if (tabMapAfter.size() > 0) {
					tabPaneEdiNr.getTabs().addAll(1, tabMapAfter.values());
				}	
				if (tabMapBefore.size() > 0) {
					tabPaneEdiNr.getTabs().addAll(0, tabMapBefore.values());
				}
			}
		});
	}

	// pr�ft ob das eingegebene BO (newName) in der BO-Tabelle (businessObjektMap) 
	// bereits vorhanden ist.  

	private String checkBusinessObjectName(String newName, int index) {
		String aktName = null;
		if (newName != null) {
			GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
			if (buOb == org.geschaeftsObjekt[index]) {
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
			setChangeFlag(akt.geschaeftsObjekt[index] == org.geschaeftsObjekt[index]);
		}
		return aktName;
	}
	
	private GeschaeftsObjekt askForNewBusinessObjektName(String newName) {
		String aktName = Dialogs.create().owner(primaryStage).title(applName)
				.message("Soll das folgende Gesch�ftsobjekt gespeichert werden?")
				.showTextInput(newName);
		if (aktName == null) {
			return null;
		}	
		return (geschaeftsObjektAnlegen(aktName));
	}

	private GeschaeftsObjekt geschaeftsObjektAnlegen(String aktName) {
		
		String select = "SELECT g FROM GeschaeftsObjekt g WHERE g.name= '" + aktName + "'";
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(select, GeschaeftsObjekt.class);
		List<GeschaeftsObjekt> gList = tq.getResultList();
		
		if (gList.size() > 0) {
			mainController.setInfoText("Gesch�ftsobjekt ist bereits "+ gList.size() +" mal vorhanden");
			return gList.get(0);
		}
		try {
			GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(aktName);
			entityManager.getTransaction().begin();
			entityManager.persist(newBusObj);
			entityManager.getTransaction().commit();
			businessObjectName.add(aktName);
			businessObjectMap.put(aktName.toUpperCase(), newBusObj);
			mainController.setInfoText("Das Gesch�ftsobjekt \"" + 
					aktName + "\" wurde erfolgreich gespeichert");
			return newBusObj;
		} catch (RuntimeException er) {
			Dialogs.create().owner(primaryStage)
			.title(applName).masthead("Datenbankfehler")
			.message("Fehler beim speichern des Gesch�ftsobjektes")
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
	
	private void readIntegrationList() {
        final ObservableList<Integration> aktList = FXCollections.observableArrayList();
		TypedQuery<Integration> tq = entityManager.createQuery(
				"SELECT i FROM Integration i ORDER BY i.name", Integration.class);
		
		aktList.addAll(tq.getResultList());
		if (cmbIntegration.getItems().isEmpty()) {
			cmbIntegration.setItems(aktList);
		}
		else {
			cmbIntegration.getItems().retainAll(aktList);
			cmbIntegration.getItems().setAll(aktList);
		}
		
	}

	private void readCmbKonfigurationList(Integration integration) {
		cmbKonfiguration.getItems().clear();
		TypedQuery<Konfiguration> tq = entityManager.createQuery(
				"SELECT k FROM Konfiguration k WHERE k.integration = :i ORDER BY k.name", Konfiguration.class);
		tq.setParameter("i", integration);
		
		ObservableList<Konfiguration> aktList = FXCollections.observableArrayList(tq.getResultList());

		// find default KONFIGURATION in DB-table
		Boolean found = false;  
		for (Konfiguration k : aktList) {
			if (k.getName().equals(DEFAULT_KONFIG_NAME)) {
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

//	public boolean checkForContinueEditing() {
//		if (aktEdi != null && aktEdiEqualPersistence() == false) {
//		}
//		return false;
//	}

    private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };

    private void setChangeFlag(Boolean equal) {
    	if (equal) {
    		equal = checkForChangesWithMode(Checkmode.ONLY_CHECK);
    	}
    	dataIsChanged.set(!equal);
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		log("checkForChangesAndAskForSave"," aktEdi=" + akt.bezeichnung);
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private boolean checkForChangesWithMode(Checkmode checkmode) {
//		if (org.EdiNr < 1) {
		if (ediEintrag.get() == null) {
			log("checkForChangesAndSave","mode="+ checkmode +  " orgEdiNr=" + org.EdiNr);
			return true;
		}
//		log("checkForChangesWithMode","mode="+ checkmode +  " aktEdi=" + akt.bezeichnung);
		if (akt.konfiguration == org.konfiguration &&
			akt.sender == org.sender &&
			verifyEmpfaengerAreUnchanged() == true   &&
			akt.seitDatum == org.seitDatum  &&
			akt.bisDatum == org.bisDatum  &&
			akt.beschreibung.equals(org.beschreibung) ) {
			// no changes -> no update
			log("checkForChangesWithMode"," no changes found");		
			return true;  
		}
		if (checkmode == Checkmode.ONLY_CHECK) {
			log("checkForChangesWithMode","changes found but mode=ONLY_CHECK");		
			return false; 
		}
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			// ask if update should be done
			Action response = Dialogs.create().owner(primaryStage)
							.title(applName).masthead(SICHERHEITSABFRAGE)
							.message("Soll die �nderungen am EDI-Eintrag " + ediEintrag.get().getEdiNrStr() + 
									" \"" + ediEintrag.get().getBezeichnung() + "\" gespeichert werden?")
							.showConfirm();
			if (response == Dialog.Actions.CANCEL) {
				return false;
			} 
			if (response == Dialog.Actions.NO) {
				return true;
			}
			log("checkForChangesWithMode","--> next do check/update");
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
    		EdiKomponente empf = akt.empfaengerKomponente[i];
    		if (empf == null) {
    			if (i==0) {
    				String msg = "Ein Empf�nger ist erforderlich";
    	    		mainController.setErrorText(msg);
    				btnEmpfaenger1.requestFocus();
    				return false;
    			}
    		} else {    
    			if (akt.geschaeftsObjekt[i] == null || 
    				akt.geschaeftsObjekt[i].getName().length() < 1) {
    				
    				String msg = "Bitte zum Empf�nger \"" + empf.getFullname() 
    					+ "\" auch ein Gesch�ftsobjekt eintragen/ausw�hlen";
    	    		mainController.setErrorText(msg);
    				switch(i) {
    				case 0: cmbBuOb1.requestFocus(); break;
    				case 1: cmbBuOb2.requestFocus(); break;
    				case 2: cmbBuOb3.requestFocus(); break;
    				}
    				return false;
    			}
    		}
    	}
    	if(akt.integration == null) {
    		mainController.setErrorText("Eine Integration muss ausgew�hlt oder angelegt werden");
    		cmbIntegration.requestFocus();
    		return false;
    	}
    	if (akt.konfiguration == null) {
    		mainController.setErrorText("Eine Konfiguration muss ausgew�hlt oder angelegt werden");
    		cmbKonfiguration.requestFocus();
    		return false;
    	}
    	
    	// end validation -> start update/insert
		log("checkForChangesWithMode","changes found --> UPDATE");		
		try {
			EdiEintrag aktEdi = ediEintrag.get();
			entityManager.getTransaction().begin();
			// if configuration changed the EdiEintrag must be removed from previous configuration	
			if (org.konfiguration != null && akt.konfiguration != org.konfiguration) {
				org.konfiguration.getEdiEintrag().remove(aktEdi);
			}
			if (akt.integration.getId() == 0L) {
				entityManager.persist(akt.integration);
			}
			if (akt.konfiguration.getId() == 0L) {    	// new configuration for persistence
				entityManager.persist(akt.konfiguration);
				akt.konfiguration.setIntegration(akt.integration);
			}
			if (akt.konfiguration.getEdiEintrag().contains(aktEdi) == false) {
				akt.konfiguration.getEdiEintrag().add(aktEdi);
			}
			aktEdi.setKonfiguration(akt.konfiguration);
			aktEdi.setEdiKomponente(akt.sender); 
			aktEdi.setBeschreibung(akt.beschreibung);
			
			Collection<EdiEmpfaenger> tmpEmpfaengerList = new ArrayList<EdiEmpfaenger>();
			
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				EdiEmpfaenger empf = akt.empfaenger[i];
				if (empf == null && akt.empfaengerKomponente[i] != null) {
					empf = new EdiEmpfaenger();
					aktEdi.getEdiEmpfaenger().add(empf);
					entityManager.persist(empf);
				}
				if (akt.empfaengerKomponente[i] != null) {
					empf.setEdiEintrag(aktEdi);
					empf.setKomponente(akt.empfaengerKomponente[i]);
					empf.setGeschaeftsObjekt(akt.geschaeftsObjekt[i]);
					tmpEmpfaengerList.add(empf);
				}
			}
			// EdiEmpfaenger at the original EmpfaengerList must be removed 
			// from the database if the are not in the new EmpfaengerList  
			//
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				EdiEmpfaenger empf = org.empfaenger[i];
				if (empf != null && tmpEmpfaengerList.contains(empf) == false) {
					entityManager.remove(empf);
				}
			}
			aktEdi.setEdiEmpfaenger(tmpEmpfaengerList);
			
			String tmpEdiBezeichnung = aktEdi.autoBezeichnung(); 
			if (aktEdi.getBezeichnung().equals(tmpEdiBezeichnung) == false) {
				aktEdi.setBezeichnung(tmpEdiBezeichnung);
				tfBezeichnung.textProperty().set(aktEdi.autoBezeichnung());
			}
			LocalDate aktSeitDatum = dpProduktivSeit.getValue();
			aktEdi.seitDatumProperty().set(aktSeitDatum==null ? "" : aktSeitDatum.toString());
			
			LocalDate aktBisDatum = dpProduktivBis.getValue();
			aktEdi.bisDatumProperty().set(aktBisDatum==null ? "" : aktBisDatum.toString());
			
			aktEdi.setLaeUser(System.getenv("USERNAME").toUpperCase());
			aktEdi.setLaeDatum(LocalDateTime.now().toString());
			
			entityManager.getTransaction().commit();
			
			setLastChangeField(ediLastChange, aktEdi.getLaeDatum(), aktEdi.getLaeUser());			

			mainController.setInfoText("Der EDI-Eintrag wurde gespeichert");
			akt.setData(aktEdi);
			org.setData(aktEdi);
		} catch (RuntimeException e) {
			Dialogs.create().owner(primaryStage)
			.title(applName).masthead("Datenbankfehler")
			.message("Fehler beim speichern des Gesch�ftsobjektes")
			.showException(e);
		}	
		dataIsChanged.set(false);
    	
		return true;
	}	
	
	private boolean verifyEmpfaengerAreUnchanged () {
		for(int i=0; i < MAX_EMPFAENGER; ++i ) {
			if (akt.empfaengerKomponente[i] != org.empfaengerKomponente[i] ||
					akt.geschaeftsObjekt[i] != org.geschaeftsObjekt[i]) {
				log("verifyEmpfaenger","false bei "+i);
				return false;
			}
		}
		return true;
	}
		
    
    //Action: Sender-Button is pressed
    @FXML
    void senderButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, akt.sender, entityManager);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    		EdiKomponente selKomponente = komponentenAuswahlController.getSelectedKomponente();
    	    if (akt.sender != selKomponente ) {
    	    	akt.sender = selKomponente; 
    	    	btnSender.textProperty().unbind();
    	    	btnSender.textProperty().bind(akt.sender.fullnameProperty());
    	    	senderIsSelected.set(true);
    	    }
    	    setChangeFlag(akt.sender == org.sender);
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
    	
    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, akt.empfaengerKomponente[btnNr], entityManager);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    		if (akt.empfaengerKomponente[btnNr] != komponentenAuswahlController.getSelectedKomponente()) {
    			akt.empfaengerKomponente[btnNr] = komponentenAuswahlController.getSelectedKomponente();
    			ret = akt.empfaengerKomponente[btnNr].getFullname();
    		}
    		setChangeFlag(akt.empfaengerKomponente[btnNr] == org.empfaengerKomponente[btnNr]);
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
    	loader.setLocation(getClass().getResource("../view/KomponentenAuswahl.fxml"));
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
    
	public final ObjectProperty<EdiEintrag> ediEintragProperty() {
		return ediEintrag;
	}
	
	public final EdiEintrag getEdiEintrag() {
		return ediEintrag.get() ;
	}
	
	public final void setEdiEintrag(EdiEintrag ediEintrag) {
		this.ediEintrag.set(ediEintrag);
	}

	private static void log(String methode, String message) {
		String className = EdiEintragController.class.getName().substring(16);
		System.out.println(className + "." + methode + "(): " + message); 
	}
    
    private void checkFieldFromView() {
    	assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneSzenario != null : "fx:id=\"paneSzenario\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneEdiEintrag != null : "fx:id=\"paneEdiEintrag\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnNewSzenario != null : "fx:id=\"btnNewSzenario\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbKonfiguration != null : "fx:id=\"cmbKonfiguration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";

        assert taEdiBeschreibung != null : "fx:id=\"taEdiBeschreibung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert eintragVBox != null : "fx:id=\"eintragVBox\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediEintrag != null : "fx:id=\"ediEintrag\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbIntervall != null : "fx:id=\"cmbIntervall\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger2 != null : "fx:id=\"btnEmpfaenger2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger3 != null : "fx:id=\"btnEmpfaenger3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnNewConfiguration != null : "fx:id=\"btnNewConfiguration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbIntegration != null : "fx:id=\"cmbIntegration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb1 != null : "fx:id=\"cmbBuOb1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb2 != null : "fx:id=\"cmbBuOb2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb3 != null : "fx:id=\"cmbBuOb3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert mbtEmpfaenger2 != null : "fx:id=\"mbtEmpfaenger2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert mbtEmpfaenger3 != null : "fx:id=\"mbtEmpfaenger3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivSeit != null : "fx:id=\"dpProduktivSeit\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivBis != null : "fx:id=\"dpProduktivBis\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
    }

    
    
}
