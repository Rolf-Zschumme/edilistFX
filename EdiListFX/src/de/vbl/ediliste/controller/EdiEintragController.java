
package de.vbl.ediliste.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    	EdiKomponente empfaengerKomponente[] = new EdiKomponente[MAX_EMPFAENGER];
    	String busObjName[] = new String[MAX_EMPFAENGER]; 
    	
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
				busObjName[i] = e.geschaeftsObjektNameProperty().getValueSafe();
				empfaengerKomponente[i++] = e.getKomponente();
			}
			while(i < MAX_EMPFAENGER) {
				busObjName[i] = null;
				empfaengerKomponente[i++] = null;
			}
    	}
//    	void clearData () {
//    		integration = null;
//    		konfiguration = null;
//    		bezeichnung = "";
//    		beschreibung = "";
//    		seitDatum = null;
//    		bisDatum = null;
//    		senderId = 0L;
//    		senderName = null;
//    	}
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
//    				akt.clearData();
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
				if (akt.beschreibung.equals(org.beschreibung) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
			}	
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
//		cmbBuOb1.valueProperty().addListener(new ChangeListener<String>() {
//			@Override
//			public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
//				if (newValue != null) {
//					String checkedName = checkBusinessObject(newValue, aktEmpfaenger[0], busObjName[0]);
//					if (checkedName != null ) {
//						busObjName[0] = checkedName;
//						if (checkedName.equals(newValue) == false) {  
//							cmbBuOb1.getSelectionModel().select(busObjName[0]); // wegen Groß-/Kleinschrift
//						}
//						buOb1Exist.set(true);
//					}
//				}	
//			}

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
    		if (akt.seitDatum != org.seitDatum) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}
    	});
    	
    	dpProduktivBis.setOnAction(event -> {
    		akt.bisDatum = dpProduktivBis.getValue();
    		if (akt.bisDatum != org.bisDatum) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}
    	});
	}

	private String checkBusinessObjectName(String newName, int index) {
		String aktName = null;
		if (newName != null) {
			if (newName.equals(org.busObjName[0]) == true) {
				// equal -> check rest and set state
				akt.busObjName[index] = newName; 
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			} else {
				GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
				if (buOb != null) {
					aktName = buOb.getName();
					dataIsChanged.set(true);
				} else {
					aktName = askForNewBusinessObjektName(newName);
					if (aktName != null) {
						akt.busObjName[index] = aktName;
						dataIsChanged.set(true);
					}
				}
			}
		}
		return aktName;
	}
	
	private String askForNewBusinessObjektName(String newName) {
		String aktName = Dialogs.create().owner(primaryStage).title(applName)
				.message("Soll das folgende Geschäftsobjekt ergänzt werden?")
				.showTextInput(newName);
		if (aktName != null) {
			GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(aktName);
			businessObjectName.add(newName);
			businessObjectMap.put(newName.toUpperCase(), newBusObj);
		}
		return aktName;
	}

	// prüft ob das eingegebene BO (newName) in der BO-Tabelle (businessObjektMap) bereits
	// vorhanden ist. Zuvor wird geprüft ob das BO dem im Empfänger gespeicherten BO entspricht 
	
//	private String checkBusinessObject(String newName, EdiEmpfaenger e, String aktName) {
//		String orgName = "";
//		if (e.getGeschaeftsObjekt() != null) {
//			orgName = e.getGeschaeftsObjekt().getName();
//		} 
//		if (newName.equalsIgnoreCase(orgName) == true) {
//			if (aktName != orgName) {
//				aktName = orgName;
////				ediEintragIsChanged.set(aktEdiEqualPersistence()==false);
//			}
//		}
//		else {
//			GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
//			if (buOb != null) {
//				aktName = buOb.getName();
//				dataIsChanged.set(true);
//			} else {
//				newName = Dialogs.create().owner(primaryStage).title(applName)
//						.message("Soll das folgende Geschäftsobjekt neu angelegt werden?")
//						.showTextInput(newName);
//				if (newName != null) {
//					GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(newName);
//					try {
//						entityManager.getTransaction().begin();
//						entityManager.persist(newBusObj);
//						entityManager.getTransaction().commit();
//						String msg = "Das Geschäftsobjekt \"" + newBusObj.getName() + "\" wurde erfolgreich gespeichert";
//						Dialogs.create().owner(primaryStage)
//							   .title(applName).masthead(null)
//							   .message(msg).showInformation();
//						businessObjectName.add(newName);
//						businessObjectMap.put(newName.toUpperCase(), newBusObj);
//						aktName = newName;
//						dataIsChanged.set(true);
//					} catch (RuntimeException er) {
//						Dialogs.create().owner(primaryStage)
//						   .title(applName).masthead("Datenbankfehler")
//						   .message("Fehler beim speichern des Geschäftsobjektes")
//						   .showException(er);
//					}
//				}	
//			}
//		}
//		return aktName;
//	}	
	
	
    private void setupIntegrationComboBox() {

//		cmbIntegration.setItems(readIntegrations());
		
		cmbIntegration.setCellFactory((cmbBx) -> {
			return new ListCell<Integration>() {
				@Override
				protected void updateItem(Integration item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
//						log("cmbIntegration.setCellFactory.updateItem","call with item " + item.getName());
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
			Integration newIntegration = cmbIntegration.getSelectionModel().getSelectedItem();
			if (newIntegration != org.integration) {
				dataIsChanged.set(true);
			} else {
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}	
			if (newIntegration != akt.integration) {
				akt.integration = newIntegration;
				readCmbKonfigurationList(akt.integration);
			}	
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
			String aktKonfigName = akt.konfiguration==null ? "null" : akt.konfiguration.getName();
			String selKonfigName = selKonfiguration==null ? "null" : selKonfiguration.getName();
			log("cmbKonfiguration.setOnAction"," Änderung von " + aktKonfigName + " nach " + selKonfigName);
			if (selKonfiguration != org.konfiguration) {
				dataIsChanged.set(true);
			} else {
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}
			akt.konfiguration = selKonfiguration;

			// zusätzliche EdiNr-Reiter aktualisieren (entfernen/ergänzen)
			
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
//		tq.setHint("javax.persistence.cache.storeMode", "REFRESH");
		
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
//		tq.setHint("javax.persistence.cache.storeMode", "REFRESH");
		
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
	
//    private void copyEmpfaengerList(Iterator<EdiEmpfaenger> empfaengerList) {
//		for (int i=0; i<MAX_EMPFAENGER; ++i) {
//			aktEmpfaenger[i] = null;
//			busObjName[i] = "";
//			if (empfaengerList.hasNext()) {
//				aktEmpfaenger[i] = empfaengerList.next();
//				if (aktEmpfaenger[i].getGeschaeftsObjekt()!=null) {
//					busObjName[i] = aktEmpfaenger[i].getGeschaeftsObjekt().getName();
//				}
//			}	
//		}
//    }
    
    private void setAktEmpfaenger() {
//    	Iterator<EdiEmpfaenger> aktEmpfList = akt.empfaenger.iterator();
		if (akt.empfaengerKomponente[0] != null) {
			btnEmpfaenger1.setText(akt.empfaengerKomponente[0].getFullname());
			empfaenger1IsSelected.set(true);
			cmbBuOb1.getSelectionModel().select(akt.busObjName[0]);
			buOb1Exist.set(akt.busObjName[0] != null);
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
			cmbBuOb2.getSelectionModel().select(akt.busObjName[1]);
			buOb2Exist.set(akt.busObjName[1]!=null);
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
			cmbBuOb3.getSelectionModel().select(akt.busObjName[2]);
			buOb3Exist.set(akt.busObjName[2]!=null);
		}
		else {
			btnEmpfaenger3.setText("");
			empfaenger3IsSelected.set(false);
			cmbBuOb3.getSelectionModel().select(null);
			buOb3Exist.set(false);
		}
//		if (aktEmpfaenger[2]!=null) {
//			btnEmpfaenger3.setText(aktEmpfaenger[2].getKomponente().getFullname());
//			empfaenger3IsSelected.set(true);
//			cmbBuOb3.getSelectionModel().select(busObjName[2]);
//			buOb3Exist.set(aktEmpfaenger[2].getGeschaeftsObjekt()!=null);
//		}
//		else {
//			btnEmpfaenger3.setText("");
//			empfaenger3IsSelected.set(false);
//			cmbBuOb3.getSelectionModel().select(null);
//			buOb3Exist.set(false);
//		}
    }

//    private boolean checkForChanges() {
//    	
//    	return true;
//    }
    
//	public boolean checkForContinueEditing() {
//		if (aktEdi != null && aktEdiEqualPersistence() == false) {
//			Action response = Dialogs.create().owner(primaryStage)
//					.title(applName).masthead(SICHERHEITSABFRAGE)
//					.message("Soll die Änderungen am EDI-Eintrag " + aktEdi.getEdiNrStr() + 
//					   " \"" + aktEdi.getBezeichnung() + "\" gespeichert werden?")
//					.showConfirm();
////			DialogResponse res = Dialogs.showConfirmDialog(primaryStage, msg , SICHERHEITSABFRAGE, 
////											  applName, DialogOptions.YES_NO_CANCEL);
//			if (response == Dialog.Actions.YES) {
//				if (aktEdiEintragSpeichern()==false)
//					return true;
//				Dialogs.create().owner(primaryStage)
//					   .title(applName).masthead(null)
//					   .message("Die Änderungen wurden gespeichert")
//					   .showInformation();
////				Dialogs.showInformationDialog(primaryStage, "Die Änderungen wurden gespeichert", "Info", applName);
//			} else if (response != Dialog.Actions.NO) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesAndSave(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesAndSave(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesAndSave(Checkmode checkmode) {
		log("checkForChangesAndSave","mode="+ checkmode +  " aktEdi=" + akt.bezeichnung);
		if (org.EdiNr < 1) {
			return true;
		}
		if (akt.konfiguration == org.konfiguration &&
			akt.sender == org.sender &&
			verifyEmpfaengerAreUnchanged() == true   &&
			akt.seitDatum == org.seitDatum  &&
			akt.bisDatum == org.bisDatum ) {
			// no changes -> no update
			return true;  
		}
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			// ask if update should be done
		}
		if (checkmode != Checkmode.ONLY_CHECK) {
			// now we do the update 
		}
		return true;
	}	
//		Collection<EdiEmpfaenger> tmpEmpfaengerList = new ArrayList<>();
		
		
//		String tmpEdiBezeichnung = aktEdi.autoBezeichnung(); 
		
//		try {
//	 		entityManager.getTransaction().begin();
//	    	// if configuration changed the EdiEintrag must be removed from previous configuration	
//	    	if (aktEdi.getKonfiguration() != null && aktEdi.getKonfiguration() != aktKonfiguration) {
//	    		aktEdi.getKonfiguration().getEdiEintrag().remove(aktEdi);
//	    	}
//	    	if (aktKonfiguration.getId() == 0L) {    	// new configuration for persistence
//	    		entityManager.persist(aktKonfiguration);
//	    		aktKonfiguration.setIntegration(aktIntegration);
//	    	}
//	    	if (aktKonfiguration.getEdiEintrag().contains(aktEdi) == false) {
//	    		aktKonfiguration.getEdiEintrag().add(aktEdi);
//	    	}
//	    	aktEdi.setKonfiguration(aktKonfiguration);
//	    	
//	    	aktEdi.setEdiKomponente(entityManager.find(EdiKomponente.class, ae.senderId)); 
//	    	
//	 		tmpEmpfaengerList = new ArrayList<>();
//			for (int i=0; i<MAX_EMPFAENGER; ++i) {
//				EdiEmpfaenger empf = aktEmpfaenger[i];
//				if (empf != null) {
//					empf.setEdiEintrag(aktEdi);
//					if (empf.getKomponente().getId() == 0L) {
//						log("ediEintragSpeichern","HINWEIS: Komponente ohne ID gefunden!");
//						entityManager.persist(empf);
//					}
//					tmpEmpfaengerList.add(empf);
//				}	
//			}
//            for (EdiEmpfaenger e : aktEdi.getEdiEmpfaenger()) {
//                if (tmpEmpfaengerList.contains(e) == false) {
//                       entityManager.remove(e);
//                }
//            }
//            aktEdi.setEdiEmpfaenger(tmpEmpfaengerList);
//            
//			String tmpEdiBezeichnung = aktEdi.autoBezeichnung(); 
//			if (aktEdi.getBezeichnung().equals(tmpEdiBezeichnung) == false) {
//				aktEdi.setBezeichnung(tmpEdiBezeichnung);
//				tfBezeichnung.textProperty().set(aktEdi.autoBezeichnung());
//			}
//			LocalDate aktSeitDatum = dpProduktivSeit.getValue();
//			aktEdi.seitDatumProperty().set(aktSeitDatum==null ? "" : aktSeitDatum.toString());
//			
//			LocalDate aktBisDatum = dpProduktivBis.getValue();
//			aktEdi.seitDatumProperty().set(aktBisDatum==null ? "" : aktBisDatum.toString());
//			
//			aktEdi.setLaeUser(System.getenv("USERNAME").toUpperCase());
//			aktEdi.setLaeDatum(LocalDateTime.now().toString());
//			
//			entityManager.getTransaction().commit();
//			
//			setLastChangeField(ediLastChange, aktEdi.getLaeDatum(), aktEdi.getLaeUser());			
////			if (prevKonfiguration != null) {
////				entityManager.refresh(prevKonfiguration);
////				entityManager.refresh(orgEdi.getKonfiguration());
////			}
//			
////			entityManager.getEntityManagerFactory().getCache().evict(Konfiguration.class, orgEdi.getKonfiguration().getId());
////			System.out.println("Org-Konfig(nR): " + orgEdi.getKonfiguration() + " mit " + orgEdi.getKonfiguration().getEdiEintrag().size() + " Edis");			
//			mainController.setInfoText("Der EDI-Eintrag wurde gespeichert");
//		} catch (RuntimeException e) {
//			Dialogs.create().owner(primaryStage)
//			.title(applName).masthead("Datenbankfehler")
//			.message("Fehler beim speichern des Geschäftsobjektes")
//			.showException(e);
//		}	
//		dataIsChanged.set(false);


//	private boolean verifyEmpfaengerAreUnchanged () {
//		Iterator<EdiEmpfaenger> e = orgEDI.getEdiEmpfaenger().iterator();
//		int i = 0;
//    	while (i < MAX_EMPFAENGER) {
//    		if (e.hasNext() == false)
//    			break;
//    		EdiEmpfaenger empfaenger = e.next(); 
//    		if (empfaenger.getKomponente() != aktEmpfaenger[i].getKomponente() ||
//    		   !empfaenger.getGeschaeftsObjekt().getName().equals(busObjName[i]) ) {
//    			return false;
//    		}
//    	}
//    	return (i == orgEDI.getEdiEmpfaenger().size());
//  }
	
	private boolean verifyEmpfaengerAreUnchanged () {
		for(int i=0; i < MAX_EMPFAENGER; ++i ) {
			if (akt.empfaengerKomponente[i] != org.empfaengerKomponente[i] ||
				akt.busObjName[i] != org.busObjName[i])
				return false;
		}
		return true;
		
//		if (akt.empfaenger.size() != org.empfaenger.size())
//			return false;
//		Iterator<EdiEmpfaenger> orgEmpfaengerList = org.empfaenger.iterator();  
//		for (EdiEmpfaenger aktE : akt.empfaenger) {
//			EdiEmpfaenger orgE = orgEmpfaengerList.next();
//			if (aktE.getKomponente() != orgE.getKomponente() ||
//				aktE.getGeschaeftsObjekt() != orgE.getGeschaeftsObjekt() )
//				return false;
//		}
	}
	
//	private boolean checkForChanges() {
//    	if (akt.senderId == 0L) {
//    		Dialogs.create().owner(primaryStage)
//    		.title(applName).masthead("Korrektur-Hinweis")
//    		.message("Sender ist erforderlich")
//    		.showWarning();
//    		btnSender.requestFocus();
//    		return false;
//    	}
//    	for (int i=0; i<MAX_EMPFAENGER; ++i) {
//    		EdiEmpfaenger empf = aktEmpfaenger[i];
//    		if (empf == null) {
//    			if (i==0) {
//    				Dialogs.create().owner(primaryStage)
//    				.title(applName).masthead("Korrektur-Hinweis")
//    				.message("Empfänger ist erforderlich")
//    				.showWarning();
//    				btnEmpfaenger1.requestFocus();
//    				return false;
//    			}
//    		} else {  
//    			if (busObjName[i].length() < 1) {
//    				Dialogs.create().owner(primaryStage)
//    				.title(applName).masthead("Korrektur-Hinweis")
//    				.message("Bitte zum Empfänger \"" + empf.getKomponente().getFullname() + "\""  +
//    						" auch ein Geschäftsobjekt eintragen/auswählen")
//    						.showWarning();
//    				switch(i) {
//    				case 0: cmbBuOb1.requestFocus(); break;
//    				case 1: cmbBuOb2.requestFocus(); break;
//    				case 2: cmbBuOb3.requestFocus(); break;
//    				}
//    				return false;
//    			}
//    			empf.setGeschaeftsObjekt(businessObjectMap.get(busObjName[i].toUpperCase()));
//    		}
//    	}
//    	if(akt.integration == null) {
//    		mainController.setErrorText("Integration muss ausgewählt/zugeordnet werden");
//    		cmbIntegration.requestFocus();
//    		return false;
//    	}
//    	if (akt.konfiguration == null) {
//    		mainController.setErrorText("Eine Konfiguration muss auswählen/zugeordnet werden");
//    		cmbKonfiguration.requestFocus();
//    		return false;
//    	}
//    	return true;
//    }
//	private boolean checkForChangesAndSaveXXX(Checkmode checkmode) {
//    	Collection<EdiEmpfaenger> tmpEmpfaengerList;
//    	if (checkForChanges()==false)
//    		return false;
//		try {
//	 		entityManager.getTransaction().begin();
//	    	// if configuration changed the EdiEintrag must be removed from previous configuration	
//	    	if (aktEdi.getKonfiguration() != null && aktEdi.getKonfiguration() != aktKonfiguration) {
//	    		aktEdi.getKonfiguration().getEdiEintrag().remove(aktEdi);
//	    	}
//	    	if (aktKonfiguration.getId() == 0L) {    	// new configuration for persistence
//	    		entityManager.persist(aktKonfiguration);
//	    		aktKonfiguration.setIntegration(aktIntegration);
//	    	}
//	    	if (aktKonfiguration.getEdiEintrag().contains(aktEdi) == false) {
//	    		aktKonfiguration.getEdiEintrag().add(aktEdi);
//	    	}
//	    	aktEdi.setKonfiguration(aktKonfiguration);
//	    	
//	    	aktEdi.setEdiKomponente(entityManager.find(EdiKomponente.class, ae.senderId)); 
//	    	
//	 		tmpEmpfaengerList = new ArrayList<>();
//			for (int i=0; i<MAX_EMPFAENGER; ++i) {
//				EdiEmpfaenger empf = aktEmpfaenger[i];
//				if (empf != null) {
//					empf.setEdiEintrag(aktEdi);
//					if (empf.getKomponente().getId() == 0L) {
//						log("ediEintragSpeichern","HINWEIS: Komponente ohne ID gefunden!");
//						entityManager.persist(empf);
//					}
//					tmpEmpfaengerList.add(empf);
//				}	
//			}
//            for (EdiEmpfaenger e : aktEdi.getEdiEmpfaenger()) {
//                if (tmpEmpfaengerList.contains(e) == false) {
//                       entityManager.remove(e);
//                }
//            }
//            aktEdi.setEdiEmpfaenger(tmpEmpfaengerList);
//            
//			String tmpEdiBezeichnung = aktEdi.autoBezeichnung(); 
//			if (aktEdi.getBezeichnung().equals(tmpEdiBezeichnung) == false) {
//				aktEdi.setBezeichnung(tmpEdiBezeichnung);
//				tfBezeichnung.textProperty().set(aktEdi.autoBezeichnung());
//			}
//			LocalDate sdate = dpProduktivSeit.getValue();
//			aktEdi.seitDatumProperty().set(sdate==null ? "" : sdate.toString());
//			
//			LocalDate bdate = dpProduktivBis.getValue();
//			aktEdi.seitDatumProperty().set(bdate==null ? "" : bdate.toString());
//			
//			aktEdi.setLaeUser(System.getenv("USERNAME").toUpperCase());
//			aktEdi.setLaeDatum(LocalDateTime.now().toString());
//			
//			entityManager.getTransaction().commit();
//			
//			setLastChangeField(ediLastChange, aktEdi.getLaeDatum(), aktEdi.getLaeUser());			
////			if (prevKonfiguration != null) {
////				entityManager.refresh(prevKonfiguration);
////				entityManager.refresh(orgEdi.getKonfiguration());
////			}
//			
////			entityManager.getEntityManagerFactory().getCache().evict(Konfiguration.class, orgEdi.getKonfiguration().getId());
////			System.out.println("Org-Konfig(nR): " + orgEdi.getKonfiguration() + " mit " + orgEdi.getKonfiguration().getEdiEintrag().size() + " Edis");			
//			mainController.setInfoText("Der EDI-Eintrag wurde gespeichert");
//		} catch (RuntimeException e) {
//			Dialogs.create().owner(primaryStage)
//			.title(applName).masthead("Datenbankfehler")
//			.message("Fehler beim speichern des Geschäftsobjektes")
//			.showException(e);
//		}	
//		dataIsChanged.set(false);
//		return true;
//    }
    
    //Action: Sender-Button is pressed
    @FXML
    void senderButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, akt.sender.getId(), entityManager);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    		EdiKomponente selKomponente = komponentenAuswahlController.getSelectedKomponente();
    	    if (akt.sender != selKomponente ) {
    	    	akt.sender = selKomponente; 
    	    	log("senderButton","senderName :" + akt.sender.fullnameProperty().get());
    	    	btnSender.textProperty().unbind();
    	    	btnSender.textProperty().bind(akt.sender.fullnameProperty());
    	    	if (akt.sender != org.sender) {
    	    		dataIsChanged.set(true);
    	    	}
    	    	else {
    	    		dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    	    	}
    	    	senderIsSelected.set(true);
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
    	
    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktEmpfaengerId = (akt.empfaengerKomponente[btnNr]==null ? 0L : akt.empfaengerKomponente[btnNr].getId());
    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktEmpfaengerId, entityManager);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    		Long selEmpfaengerID = komponentenAuswahlController.getSelectedKomponentenId();
    		if (selEmpfaengerID != aktEmpfaengerId) {
    			akt.empfaengerKomponente[btnNr] = komponentenAuswahlController.getSelectedKomponente();
    			ret = akt.empfaengerKomponente[btnNr].getFullname();
    		}
    		if (akt.empfaengerKomponente[btnNr] != org.empfaengerKomponente[btnNr]) {
    			dataIsChanged.set(true);
    		} else {
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    		}
    	}	
    	return ret;
    }

    @FXML
    void actionEmpfaenger2loeschen(ActionEvent event) {
    	if (akt.empfaengerKomponente[2] != null) {
    		akt.empfaengerKomponente[1] = akt.empfaengerKomponente[2];
    		akt.empfaengerKomponente[2] = null;
    		btnEmpfaenger2.setText(akt.empfaengerKomponente[1].getFullname());
    		
			akt.busObjName[1] = akt.busObjName[1]; // businessObjectMap.get(akt.busObjName[2].toUpperCase()).getName();
			akt.busObjName[2] = null;
			cmbBuOb2.getSelectionModel().select(akt.busObjName[1]);
			
	    	btnEmpfaenger3.setText("");
	    	cmbBuOb3.getSelectionModel().select(null);
    		buOb3Exist.set(false);
    		empfaenger3IsSelected.set(false);
    	} 
    	else { // 3 is empty -> delete 2
    		akt.empfaengerKomponente[1] = null;
    		btnEmpfaenger2.setText("");
    		akt.busObjName[1] = null;
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
    	akt.busObjName[2] = null;
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
        assert taEdiBeschreibung != null : "fx:id=\"taEdiBeschreibung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb1 != null : "fx:id=\"cmbBuOb1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not enjected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert taEdiBeschreibung != null : "fx:id=\"taEdiBeschreibung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert eintragVBox != null : "fx:id=\"eintragVBox\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivSeit != null : "fx:id=\"dpProduktivSeit\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediEintrag != null : "fx:id=\"ediEintrag\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger3 != null : "fx:id=\"btnEmpfaenger3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger2 != null : "fx:id=\"btnEmpfaenger2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
//      assert cmbIntervall != null : "fx:id=\"cmbIntervall\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneEdiEintrag != null : "fx:id=\"paneEdiEintrag\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbIntegration != null : "fx:id=\"cmbIntegration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbKonfiguration != null : "fx:id=\"cmbKonfiguration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb2 != null : "fx:id=\"cmbBuOb2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb3 != null : "fx:id=\"cmbBuOb3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb1 != null : "fx:id=\"cmbBuOb1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivBis != null : "fx:id=\"dpProduktivBis\" was not injected: check your FXML file 'EdiEintrag.fxml'.";


        assert cmbKonfiguration != null : "fx:id=\"cmbKonfiguration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert taEdiBeschreibung != null : "fx:id=\"taEdiBeschreibung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnNewSzenario != null : "fx:id=\"btnNewSzenario\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert eintragVBox != null : "fx:id=\"eintragVBox\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivSeit != null : "fx:id=\"dpProduktivSeit\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediEintrag != null : "fx:id=\"ediEintrag\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnNewConfiguration != null : "fx:id=\"btnNewConfiguration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger3 != null : "fx:id=\"btnEmpfaenger3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger2 != null : "fx:id=\"btnEmpfaenger2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbIntervall != null : "fx:id=\"cmbIntervall\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbIntegration != null : "fx:id=\"cmbIntegration\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb2 != null : "fx:id=\"cmbBuOb2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb3 != null : "fx:id=\"cmbBuOb3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivBis != null : "fx:id=\"dpProduktivBis\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb1 != null : "fx:id=\"cmbBuOb1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert mbtEmpfaenger2 != null : "fx:id=\"mbtEmpfaenger2\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert mbtEmpfaenger3 != null : "fx:id=\"mbtEmpfaenger3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
    }

    
    
}
