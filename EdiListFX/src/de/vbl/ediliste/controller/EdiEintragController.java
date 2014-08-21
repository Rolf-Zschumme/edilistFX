
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

	private final ObjectProperty<EdiEintrag> ediEintrag;
	private EdiEintrag orgEdi;
	private EdiEintrag aktEdi = new EdiEintrag();
    private EdiEmpfaenger aktEmpfaenger[] = new EdiEmpfaenger[MAX_EMPFAENGER];
    private String busObjName[] = { "", "", ""};

	
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

    @FXML private Button btnEdiEintragSpeichern;
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

    private BooleanProperty ediEintragIsChanged = new SimpleBooleanProperty(false);
    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger1IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger2IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger3IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty buOb1Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb2Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb3Exist = new SimpleBooleanProperty(false);
    private BooleanProperty readOnlyAccess = new SimpleBooleanProperty(false);
    
    private Integration selIntegration = null;
    private ObservableList<Integration> cmbIntegrationData = FXCollections.observableArrayList();
    private ObservableList<Konfiguration> cmbKonfigurationData = FXCollections.observableArrayList();

    private Map<String,GeschaeftsObjekt> businessObjectMap; 
    private ObservableList<String> businessObjectName = FXCollections.observableArrayList();

    
	public EdiEintragController() {
    	this.ediEintrag = new SimpleObjectProperty<>(this, "ediEintrag", null);
		readOnlyAccess.set(false);
	}

	public static void start(Stage primaryStage, EdiMainController mainController, EntityManager entityManager) {
		EdiEintragController.primaryStage = primaryStage;
		EdiEintragController.entityManager = entityManager;
		EdiEintragController.mainController = mainController;
		EdiEintragController.mainController.setInfoText("Hallo");
	}

    @FXML 
    void initialize() {
    	System.out.println("EdiEintragController.initialize() called");
    	checkFieldFromView();
    	
    	ediEintrag.addListener(new ChangeListener<EdiEintrag>() {
    		@Override
    		public void changed (ObservableValue<? extends EdiEintrag> ov,
    				EdiEintrag oldEintrag, EdiEintrag newEintrag) {
    			mainController.setErrorText("");
    			if (oldEintrag == null) {
    		    	setupLocalBindings();
    			} else {	
    				taEdiBeschreibung.setText("");
    				tfBezeichnung.setText("");
    				btnSender.setText("");
    				btnEmpfaenger1.setText("");
    				btnEmpfaenger2.setText("");
    				btnEmpfaenger3.setText("");
    				ediLastChange.setText("");
    				dpProduktivSeit.setValue(null);
    				dpProduktivBis.setValue(null);
    				selIntegration = null;
    				cmbKonfiguration.getSelectionModel().select(null);
    				tabAktEdiNr.setText(EDI_PANE_PREFIX + "000");
    			}
    			if (newEintrag != null) {
    				orgEdi = newEintrag;
    				aktEdi.copy(orgEdi);
    				Konfiguration selKonfiguration = aktEdi.getKonfiguration();
    				if (selKonfiguration != null) {
    					selIntegration = selKonfiguration.getIntegration();
    					readCmbKonfigurationData(selIntegration);
    					cmbKonfiguration.getSelectionModel().select(selKonfiguration);
    					// dodo
    					if (selKonfiguration.getEdiEintrag().size() > 0) {
    						
    					}
    				}
    				tabAktEdiNr.setText(EDI_PANE_PREFIX +  aktEdi.getEdiNrStr());
    				tfBezeichnung.setText(aktEdi.getBezeichnung()==null ? "" : aktEdi.getBezeichnung());
    				taEdiBeschreibung.setText(aktEdi.getBeschreibung());
    				
    				if (aktEdi.getEdiKomponente() == null) {
    					senderIsSelected.set(false);
    					btnSender.setText("");
    				} else {
    					senderIsSelected.set(true);
    					btnSender.setText(aktEdi.getEdiKomponente().getFullname());
    				}
    				setEmpfaenger(aktEdi);
    				
    				if (!aktEdi.seitDatumProperty().getValueSafe().equals("")) {
    					dpProduktivSeit.setValue(LocalDate.parse(aktEdi.getSeitDatum()));
    				}
    				if (!aktEdi.bisDatumProperty().getValueSafe().equals("")) {
    					dpProduktivBis.setValue(LocalDate.parse(aktEdi.getBisDatum()));
    				}
    				if (aktEdi.getLaeDatum() != null) {
    					LocalDateTime dt = LocalDateTime.parse(aktEdi.getLaeDatum());
    					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy"); 
    					ediLastChange.setText(aktEdi.getLaeUser() + "  " + formatter.format(dt));
    					String ttt = LocalTime.from(dt).toString().substring(0, 8);
    					ediLastChange.setTooltip(new Tooltip(ttt));
    				}
    				ediEintragIsChanged.set(false);
    			}
    			cmbIntegration.getSelectionModel().select(selIntegration);
    		}
		});
    }	

	private void setupLocalBindings() {
		if (businessObjectMap != null) {    // verify: this methode is done only once
			return;
		}	
		readIntegrations();
		cmbIntegration.setItems(cmbIntegrationData);
		cmbIntegration.setCellFactory((cmbBx) -> {
			return new ListCell<Integration>() {
				@Override
				protected void updateItem(Integration item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						System.out.println("Integration setCellFactory -" + item.getName());
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
			if (newIntegration != selIntegration) {
				selIntegration = cmbIntegration.getSelectionModel().getSelectedItem();
				readCmbKonfigurationData(selIntegration);
				ediEintragIsChanged.set(true);
			}	
		});
		
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
			Konfiguration newKonfiguration = cmbKonfiguration.getSelectionModel().getSelectedItem();
//			System.out.println("cmbKonfiguration setOnAction()");
			if (newKonfiguration != aktEdi.getKonfiguration()) {
				if (aktEdi.getKonfiguration() != null) {
					aktEdi.getKonfiguration().getEdiEintrag().remove(aktEdi);
				}
				aktEdi.setKonfiguration(newKonfiguration);
				if (newKonfiguration != null) {
					newKonfiguration.getEdiEintrag().add(aktEdi);
				}	
				ediEintragIsChanged.set(true);
			}

			// zusätzliche EdiNr-Reiter aktualisieren (entfernen/ergänzen)
			
//			if (tabPaneEdiNr.getTabs().size() > 1) {
				tabPaneEdiNr.getTabs().retainAll(tabAktEdiNr);
//			}
			if (newKonfiguration != null && newKonfiguration.getEdiEintrag() != null) {
				HashMap<Integer, Tab> tabMapAfter = new HashMap<Integer,Tab>();				
				HashMap<Integer, Tab> tabMapBefore = new HashMap<Integer,Tab>();				
				Iterator<EdiEintrag> i = newKonfiguration.getEdiEintrag().iterator();
				int aktEdiNr = aktEdi.getEdiNr();
				Tab extraTab = null;
				while (i.hasNext()) {
					EdiEintrag e = i.next();
					int iEdiNr = e.getEdiNr();
					if (iEdiNr != aktEdiNr ) extraTab = new Tab(EDI_PANE_PREFIX + e.getEdiNrStr());
					if (iEdiNr  < aktEdiNr ) tabMapBefore.put(iEdiNr, extraTab);
					if (iEdiNr  > aktEdiNr ) tabMapAfter.put(iEdiNr, extraTab);
				}
				if (tabMapAfter.size() > 0) {
					tabPaneEdiNr.getTabs().addAll(1, tabMapAfter.values());
				}	
				if (tabMapBefore.size() > 0) {
					tabPaneEdiNr.getTabs().addAll(0, tabMapBefore.values());
				}
			}
		});
		
		cmbKonfiguration.disableProperty().bind(cmbIntegration.getSelectionModel().selectedItemProperty().isNull());
		
		businessObjectMap = new HashMap<String,GeschaeftsObjekt>();		
		readBusinessObject();
		cmbBuOb1.setItems(businessObjectName);
		cmbBuOb1.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
				if (newValue != null) {
					String checkedName = checkBusinessObject(newValue, aktEmpfaenger[0], busObjName[0]);
					if (checkedName != null ) {
						busObjName[0] = checkedName;
						if (checkedName.equals(newValue) == false) {  
							cmbBuOb1.getSelectionModel().select(busObjName[0]); // wegen Groß-/Kleinschrift
						}
						buOb1Exist.set(true);
					}
				}	
			}
		});
		cmbBuOb2.setItems(businessObjectName);
		cmbBuOb2.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
				if (newValue != null) {
					String checkedName = checkBusinessObject(newValue, aktEmpfaenger[1], busObjName[1]);
					if (checkedName != null ) {
						busObjName[1] = checkedName;
						if (checkedName.equals(newValue) == false) {
							cmbBuOb2.getSelectionModel().select(busObjName[1]); // wegen Groß-/Kleinschrift
						}
					}
					buOb2Exist.set(true);
				}	
			}
		});
		cmbBuOb3.setItems(businessObjectName);
		cmbBuOb3.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
				if (newValue != null) {
					String checkedName = checkBusinessObject(newValue, aktEmpfaenger[2], busObjName[2]);
					if (checkedName != null ) {
						busObjName[2] = checkedName;
						if (checkedName.equals(newValue) == false) {
							cmbBuOb3.getSelectionModel().select(busObjName[2]); // wegen Groß-/Kleinschrift
						}
					}
					buOb3Exist.set(true);
				}	
			}
		});

		taEdiBeschreibung.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> o, String oldValue, String newValue) {
				if (newValue != null) {
					if (newValue != orgEdi.getBeschreibung()) { 
						ediEintragIsChanged.set(true);
//					} else {	
//						ediEintragIsChanged.set(aktEdiEqualPersistence()==false);
					}
				}
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
    	    String newDateStr = dpProduktivSeit.getValue() == null ? "" : 
    		                    dpProduktivSeit.getValue().toString();
    		if (newDateStr.equals(aktEdi.getSeitDatum()) == false) {
    			aktEdi.seitDatumProperty().set(newDateStr);
    			ediEintragIsChanged.set(true);
    		}
    	});
    	dpProduktivBis.setOnAction(event -> {
    		String newDateStr = dpProduktivBis.getValue() == null ? "" :
    							dpProduktivBis.getValue().toString();
    		if (newDateStr.equals(aktEdi.getBisDatum()) == false) {
    			aktEdi.bisDatumProperty().set(newDateStr);
    			ediEintragIsChanged.set(true);
    		}
    	});
    	
    	btnEdiEintragSpeichern.disableProperty().bind(Bindings.not(ediEintragIsChanged));
		
	}

	// prüft ob das eingegebene BO (newName) in der BO-Tabelle (businessObjektMap) bereits
	// vorhanden ist. Zuvor wird geprüft ob das BO dem im Empfänger gespeicherten BO entspricht 
	
	private String checkBusinessObject(String newName, EdiEmpfaenger e, String aktName) {
		String orgName = "";
		if (e.getGeschaeftsObjekt() != null) {
			orgName = e.getGeschaeftsObjekt().getName();
		} 
		if (newName.equalsIgnoreCase(orgName) == true) {
			if (aktName != orgName) {
				aktName = orgName;
//				ediEintragIsChanged.set(aktEdiEqualPersistence()==false);
			}
		}
		else {
			GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
			if (buOb != null) {
				aktName = buOb.getName();
				ediEintragIsChanged.set(true);
			} else {
				newName = Dialogs.create().owner(primaryStage).title(applName)
						.message("Soll das folgende Geschäftsobjekt neu angelegt werden?")
						.showTextInput(newName);
				if (newName != null) {
					GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(newName);
					try {
						entityManager.getTransaction().begin();
						entityManager.persist(newBusObj);
						entityManager.getTransaction().commit();
						String msg = "Das Geschäftsobjekt \"" + newBusObj.getName() + "\" wurde erfolgreich gespeichert";
						Dialogs.create().owner(primaryStage)
							   .title(applName).masthead(null)
							   .message(msg).showInformation();
						businessObjectName.add(newName);
						businessObjectMap.put(newName.toUpperCase(), newBusObj);
						aktName = newName;
						ediEintragIsChanged.set(true);
					} catch (RuntimeException er) {
						Dialogs.create().owner(primaryStage)
						   .title(applName).masthead("Datenbankfehler")
						   .message("Fehler beim speichern des Geschäftsobjektes")
						   .showException(er);
					}
				}	
			}
		}
		return aktName;
	}	
	
	private void readBusinessObject() {
		businessObjectMap.clear();
		businessObjectName.clear();
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(
				"SELECT g FROM GeschaeftsObjekt g ORDER BY g.name", GeschaeftsObjekt.class);
		List<GeschaeftsObjekt> gList = tq.getResultList();
		for (GeschaeftsObjekt gObject : gList) {
			businessObjectName.add(gObject.getName());
			businessObjectMap.put(gObject.getName().toUpperCase(), gObject);
		}
	}
	
	private void readIntegrations() {
		cmbIntegrationData.clear();
		TypedQuery<Integration> tq = entityManager.createQuery(
				"SELECT i FROM Integration i ORDER BY i.name", Integration.class);
		tq.setHint("javax.persistence.cache.storeMode", "REFRESH");
		cmbIntegrationData.addAll(tq.getResultList());
	}

	private void readCmbKonfigurationData(Integration integration) {
		cmbKonfigurationData.clear();
		TypedQuery<Konfiguration> tq = entityManager.createQuery(
				"SELECT k FROM Konfiguration k WHERE k.integration = :i ORDER BY k.name", Konfiguration.class);
		tq.setParameter("i", integration);
//		tq.setHint("javax.persistence.cache.storeMode", "REFRESH");
		cmbKonfigurationData.addAll(tq.getResultList());
		cmbKonfiguration.setItems(cmbKonfigurationData);
		
//		Iterator<Konfiguration> iterKonfig = tq.getResultList().iterator();
//		while (iterKonfig.hasNext()) {
//			Konfiguration k = iterKonfig.next();
//			System.out.println("readKonfiguration " + k + " \t" + k.getName() + " mit " + k.getEdiEintrag().size() + " EdiNrn");
//		}
	} 
	
	
    private void setEmpfaenger(EdiEintrag newEintrag) {
    	Iterator<EdiEmpfaenger> empfaengerList = newEintrag.getEdiEmpfaenger().iterator();
		for (int i=0; i<MAX_EMPFAENGER; ++i) {
			aktEmpfaenger[i] = null;
			busObjName[i] = "";
			if (empfaengerList.hasNext()) {
				aktEmpfaenger[i] = empfaengerList.next();
				if (aktEmpfaenger[i].getGeschaeftsObjekt()!=null) {
					busObjName[i] = aktEmpfaenger[i].getGeschaeftsObjekt().getName();
				}
			}	
		}
		if (aktEmpfaenger[0] != null) {
			btnEmpfaenger1.setText(aktEmpfaenger[0].getKomponente().getFullname());
			empfaenger1IsSelected.set(true);
			cmbBuOb1.getSelectionModel().select(busObjName[0]);
			buOb1Exist.set(aktEmpfaenger[0].getGeschaeftsObjekt()!=null);
		}
		else {
			btnEmpfaenger1.setText("");
			empfaenger1IsSelected.set(false);
			cmbBuOb1.getSelectionModel().select(null);
			buOb1Exist.set(false);
		}
		if (aktEmpfaenger[1]!=null) {
			btnEmpfaenger2.setText(aktEmpfaenger[1].getKomponente().getFullname());
			empfaenger2IsSelected.set(true);
			cmbBuOb2.getSelectionModel().select(busObjName[1]);
			buOb2Exist.set(aktEmpfaenger[1].getGeschaeftsObjekt()!=null);
		}
		else {
			btnEmpfaenger2.setText("");
			empfaenger2IsSelected.set(false);
			cmbBuOb2.getSelectionModel().select(null);
			buOb2Exist.set(false);
		}
		if (aktEmpfaenger[2]!=null) {
			btnEmpfaenger3.setText(aktEmpfaenger[2].getKomponente().getFullname());
			empfaenger3IsSelected.set(true);
			cmbBuOb3.getSelectionModel().select(busObjName[2]);
			buOb3Exist.set(aktEmpfaenger[2].getGeschaeftsObjekt()!=null);
		}
		else {
			btnEmpfaenger3.setText("");
			empfaenger3IsSelected.set(false);
			cmbBuOb3.getSelectionModel().select(null);
			buOb3Exist.set(false);
		}
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
	
//	private boolean aktEdiEqualPersistence() {
//		EdiEintrag orgEdi = entityManager.find(EdiEintrag.class, aktEdi.getId());
//		if (aktEdi.equaels(orgEdi)) {
//			return true;
//		}
//		return false;
//	}
	
    @FXML
    void ediEintragSpeichern(ActionEvent event) {
    	if (aktEdiEintragPruefen()==false)
    		return;
		try {
	 		entityManager.getTransaction().begin(); 
			aktEdi.getEdiEmpfaenger().clear();
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				EdiEmpfaenger empf = aktEmpfaenger[i];
				if (empf != null) {
					aktEdi.getEdiEmpfaenger().add(empf);
					empf.setEdiEintrag(aktEdi);
					if (empf.getKomponente().getId() == 0L) {
						System.out.println("HINWEIS: EdiEintragController findet Komponente ohne ID ???");
						entityManager.persist(empf);
					}
				}	
			}
			String tmpEdiBezeichnung = aktEdi.bezeichnung(); 
			if (aktEdi.getBezeichnung() == null) {
				aktEdi.setBezeichnung("");
			}
			if (aktEdi.getBezeichnung().equals(tmpEdiBezeichnung)==false) {
				aktEdi.setBezeichnung(tmpEdiBezeichnung);
				tfBezeichnung.textProperty().set(aktEdi.bezeichnung());
			}
//			Konfiguration prevKonfiguration = null; 
//			if (orgEdi.getKonfiguration() != aktEdi.getKonfiguration()) {
//				prevKonfiguration = orgEdi.getKonfiguration();
//			}
			orgEdi.copy(aktEdi);
			orgEdi.setLaeUser(System.getenv("USERNAME").toUpperCase());
			orgEdi.setLaeDatum(LocalDateTime.now().toString());
			
			entityManager.getTransaction().commit();

//			if (prevKonfiguration != null) {
//				entityManager.refresh(prevKonfiguration);
//				entityManager.refresh(orgEdi.getKonfiguration());
//			}
			
//			entityManager.getEntityManagerFactory().getCache().evict(Konfiguration.class, orgEdi.getKonfiguration().getId());
//			System.out.println("Org-Konfig(nR): " + orgEdi.getKonfiguration() + " mit " + orgEdi.getKonfiguration().getEdiEintrag().size() + " Edis");			
			mainController.setInfoText("Der EDI-Eintrag wurde gespeichert");
		} catch (RuntimeException e) {
			Dialogs.create().owner(primaryStage)
			.title(applName).masthead("Datenbankfehler")
			.message("Fehler beim speichern des Geschäftsobjektes")
			.showException(e);
		}	
		ediEintragIsChanged.set(false);
    }

    private boolean aktEdiEintragPruefen() {
    	if (aktEdi.getEdiKomponente()==null) {
    		Dialogs.create().owner(primaryStage)
    		.title(applName).masthead("Korrektur-Hinweis")
    		.message("Sender ist erforderlich")
    		.showWarning();
    		btnSender.requestFocus();
    		return false;
    	}
    	for (int i=0; i<MAX_EMPFAENGER; ++i) {
    		EdiEmpfaenger empf = aktEmpfaenger[i];
    		if (empf == null) {
    			if (i==0) {
    				Dialogs.create().owner(primaryStage)
    				.title(applName).masthead("Korrektur-Hinweis")
    				.message("Empfänger ist erforderlich")
    				.showWarning();
    				btnEmpfaenger1.requestFocus();
    				return false;
    			}
    		} else {  
    			if (busObjName[i].length() < 1) {
    				Dialogs.create().owner(primaryStage)
    				.title(applName).masthead("Korrektur-Hinweis")
    				.message("Bitte zum Empfänger \"" + empf.getKomponente().getFullname() + "\""  +
    						" auch ein Geschäftsobjekt eintragen/auswählen")
    						.showWarning();
    				switch(i) {
    				case 0: cmbBuOb1.requestFocus(); break;
    				case 1: cmbBuOb2.requestFocus(); break;
    				case 2: cmbBuOb3.requestFocus(); break;
    				}
    				return false;
    			}
    			empf.setGeschaeftsObjekt(businessObjectMap.get(busObjName[i].toUpperCase()));
    		}
    	}
    	if(selIntegration == null) {
    		mainController.setErrorText("Integration muss ausgewählt/zugeordnet werden");
    		cmbIntegration.requestFocus();
    		return false;
    	}
    	if (aktEdi.getKonfiguration() == null) {
    		mainController.setErrorText("Konfiguration notwendig auswählen");
    		cmbKonfiguration.requestFocus();
    		return false;
    	}
    	return true;
    }
    
 
    
    
    //Action: Sender-Button is pressed
    @FXML
    void senderButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktSenderId = aktEdi.getEdiKomponente()==null ? 0L : aktEdi.getEdiKomponente().getId();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktSenderId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
	    	Long selKomponentenID = komponentenAuswahlController.getSelectedKomponentenId();
    	    if (aktSenderId != selKomponentenID ) {
    	    	EdiKomponente sender = entityManager.find(EdiKomponente.class, selKomponentenID);
    	    	aktEdi.setEdiKomponente(sender); 
    	    	System.out.println("aktEdi.senderName :" + aktEdi.senderNameProperty().get());
    	    	btnSender.setText(sender.getFullname());
    	    	ediEintragIsChanged.set(true);
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
    	Long aktEmpfaengerId = (aktEmpfaenger[btnNr]==null ? 0L : aktEmpfaenger[btnNr].getKomponente().getId());
    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktEmpfaengerId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    		Long selEmpfaengerID = komponentenAuswahlController.getSelectedKomponentenId();
    		if (aktEmpfaengerId != selEmpfaengerID) {
    			if (aktEmpfaenger[btnNr] == null) {
    				aktEmpfaenger[btnNr] = new EdiEmpfaenger();
    			}
    			aktEmpfaenger[btnNr].setKomponente(entityManager.find(EdiKomponente.class,selEmpfaengerID));
    			ret = aktEmpfaenger[btnNr].getKomponente().getFullname();
    	    	ediEintragIsChanged.set(true);
    		}
    	}
    	return ret;
    }

    @FXML
    void actionEmpfaenger2loeschen(ActionEvent event) {
    	log("actionEmpfaenger2loeschen","called");
    	if (aktEmpfaenger[2]!=null) {
    		aktEmpfaenger[1] = aktEmpfaenger[2];
    		aktEmpfaenger[2] = null;
    		busObjName[1] = busObjName[2]; 
			btnEmpfaenger2.setText(aktEmpfaenger[1].getKomponente().getFullname());
// todo
//			cmbBuOb2.getSelectionModel().select(aktEmpfaenger[1].getGeschaeftsObjekt());
	    	btnEmpfaenger3.setText("");
    		empfaenger3IsSelected.set(false);
    		buOb3Exist.set(false);
    	} else {
    		aktEmpfaenger[1] = null;
	    	btnEmpfaenger2.setText("");
	    	cmbBuOb2.getSelectionModel().select(null);
    		empfaenger2IsSelected.set(false);
    		buOb2Exist.set(false);
    	}
    	ediEintragIsChanged.set(true);
    }
    
    @FXML
    void actionEmpfaenger3loeschen(ActionEvent event) {
    	log("actionEmpfaenger3loeschen","called");
    	aktEmpfaenger[2] = null;
    	btnEmpfaenger3.setText("");
		empfaenger3IsSelected.set(false);
		cmbBuOb3.getSelectionModel().select(null);
		buOb3Exist.set(false);
    	ediEintragIsChanged.set(true);
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

	private void log(String methode, String message) {
		String className = this.getClass().getName().substring(16);
		System.out.println(className + "." + methode + "(): " + message); 
	}
    
    private void checkFieldFromView() {
        assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneSzenario != null : "fx:id=\"paneSzenario\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert paneEdiEintrag != null : "fx:id=\"paneEdiEintrag\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert taEdiBeschreibung != null : "fx:id=\"taEdiBeschreibung\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb1 != null : "fx:id=\"cmbBuOb1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEdiEintragSpeichern != null : "fx:id=\"btnEdiEintragSpeichern\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
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
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert cmbBuOb1 != null : "fx:id=\"cmbBuOb1\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert dpProduktivBis != null : "fx:id=\"dpProduktivBis\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert btnEdiEintragSpeichern != null : "fx:id=\"btnEdiEintragSpeichern\" was not injected: check your FXML file 'EdiEintrag.fxml'.";


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
        assert btnEdiEintragSpeichern != null : "fx:id=\"btnEdiEintragSpeichern\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
        assert mbtEmpfaenger3 != null : "fx:id=\"mbtEmpfaenger3\" was not injected: check your FXML file 'EdiEintrag.fxml'.";
    }

    
    
}
