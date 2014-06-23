
package de.vbl.ediliste.controller;

import java.io.IOException;
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

import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;


//import javafx.scene.control.Dialogs;
//import javafx.scene.control.Dialogs.DialogOptions;
//import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.vbl.ediliste.controller.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;
import de.vbl.ediliste.model.GeschaeftsObjekt;

public class EdiEintragController {
	private static final String EDI_PANEL_TITLE = "EDI-Eintrag";
//	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";
	private static final Integer MAX_EMPFAENGER = 3;

	private final ObjectProperty<EdiEintrag> ediEintrag;
	private EdiEintrag orgEdi;
	private EdiEintrag aktEdi = new EdiEintrag();
    private EdiEmpfaenger aktEmpfaenger[] = new EdiEmpfaenger[MAX_EMPFAENGER];
    private String busObjName[] = { "", "", ""};

	
	@FXML private AnchorPane rightsplitpane;
    @FXML private VBox eintragVBox;

    @FXML private TitledPane paneSzenario;
    @FXML private TitledPane paneAnbindung;
    @FXML private TitledPane paneEdiEintrag;
    
    @FXML private TextArea  taEdiBeschreibung;
    @FXML private ComboBox<String> cmbBuOb1;
    @FXML private ComboBox<String> cmbBuOb2;
    @FXML private ComboBox<String> cmbBuOb3;
    @FXML private TextField ediLastChange;

    @FXML private Button btnEdiEintragSpeichern;
    @FXML private Button btnSender;
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    
    private static Stage primaryStage = null;
    private static String applName = null;
    private EntityManager entityManager = null;

    private BooleanProperty ediEintragIsChanged = new SimpleBooleanProperty(false);
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
		this.entityManager = null;
    	this.ediEintrag = new SimpleObjectProperty<>(this, "ediEintrag", null);
		readOnlyAccess.set(false);
	}

	public void setEntityManager(EntityManager entityManager) {
    	System.out.println("EdiEintragController.setEntityManager");
		this.entityManager = entityManager;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		EdiEintragController.primaryStage = primaryStage;
	}

    @FXML 
    void initialize() {
    	System.out.println("EdiEintragController.initialize() called");
    	checkFieldFromView();
    	
    	ediEintrag.addListener(new ChangeListener<EdiEintrag>() {
    		@Override
    		public void changed (ObservableValue<? extends EdiEintrag> ov,
    				EdiEintrag oldEintrag, EdiEintrag newEintrag) {
    			if (oldEintrag == null) {
    		    	setupLocalBindings();
    			} else {	
    				System.out.println("oldEintrag-Nr: " + oldEintrag.getEdiNrStr());
    				taEdiBeschreibung.setText("");
    				btnSender.setText("");
    				btnEmpfaenger1.setText("");
    				btnEmpfaenger2.setText("");
    				btnEmpfaenger3.setText("");
    			}
    			if (newEintrag != null) {
    				System.out.println("newEdi       " + newEintrag);
    				orgEdi = newEintrag;
    				aktEdi.copy(orgEdi);
    				
    				taEdiBeschreibung.setText(aktEdi.getBeschreibung());
    				if (aktEdi.getKomponente() == null) {
    					senderIsSelected.set(false);
    					paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE);
    					btnSender.setText("");
    				} else {
    					senderIsSelected.set(true);
    					paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE 
    							+ "  " + aktEdi.getEdiNrStr() 
    							+ "  " + aktEdi.bezeichnung() );
    					btnSender.setText(aktEdi.getKomponente().getFullname());
    				}
    				setEmpfaenger(aktEdi);
    				ediEintragIsChanged.set(false);
    			}
    		}

		});
    }	

	private void setupLocalBindings() {
		if (businessObjectMap != null) {    // verify: this methode is done only once
			return;
		}	
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

    	btnEmpfaenger3.visibleProperty().bind(buOb2Exist);
    	cmbBuOb3.visibleProperty().bind(buOb2Exist);
    	
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
//				newName = Dialogs.showInputDialog(primaryStage, msg, null, applName, newName);
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
//						Dialogs.showInformationDialog(primaryStage, msg);
						businessObjectName.add(newName);
						businessObjectMap.put(newName.toUpperCase(), newBusObj);
						aktName = newName;
						ediEintragIsChanged.set(true);
					} catch (RuntimeException er) {
						Dialogs.create().owner(primaryStage)
						   .title(applName).masthead("Datenbankfehler")
						   .message("Fehler beim speichern des Geschäftsobjektes")
						   .showException(er);
//						Dialogs.showErrorDialog(primaryStage,
//								"Fehler beim speichern des Geschäftsobjektes",
//								"Datenbankfehler",applName,er);
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
	
//		if (aktEdi == null || aktEdi.getId() != selEDI.getId() ) {
//			aktEdi = selEDI;
//			entityManager.detach(aktEdi);
//		}	
	
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

    private boolean checkForChanges() {
    	
    	return true;
    }
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
    	
		entityManager.getTransaction().begin(); 
		System.out.println("aktEdi-Sender: " + aktEdi.getKomponente().getFullname());

		aktEdi.getEdiEmpfaenger().clear();
		for (int i=0; i<MAX_EMPFAENGER; ++i) {
			EdiEmpfaenger empf = aktEmpfaenger[i];
			if (empf != null) {
				if (empf.getKomponente().getId() == 0L) {
					entityManager.persist(empf);
				}
				aktEdi.getEdiEmpfaenger().add(empf);
			}	
		}

		String tmpEdiBezeichnung = aktEdi.bezeichnung(); 
		if (aktEdi.getBezeichnung() == null) {
			aktEdi.setBezeichnung("");
		}
		if (aktEdi.getBezeichnung().equals(tmpEdiBezeichnung)==false) {
			aktEdi.setBezeichnung(tmpEdiBezeichnung);
			paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE + " "+ aktEdi.getEdiNrStr() + "  " + aktEdi.bezeichnung() );
		}
		orgEdi.copy(aktEdi);
		entityManager.getTransaction().commit();
//    	
//		aktEdiEintragSpeichern();
    }
    
//    private boolean aktEdiEintragSpeichern() {
//		// Prüfungen
//    	if (aktEdi.getKomponente()==null) {
//			Dialogs.create().owner(primaryStage)
//					.title(applName).masthead("Korrektur-Hinweis")
//					.message("Sender ist erforderlich")
//					.showWarning();
//			btnSender.requestFocus();
//			return false;
//    	}
//		for (int i=0; i<MAX_EMPFAENGER; ++i) {
//			EdiEmpfaenger empf = aktEmpfaenger[i];
//			if (empf == null) {
//				if (i==0) {
//					Dialogs.create().owner(primaryStage)
//							.title(applName).masthead("Korrektur-Hinweis")
//							.message("Empfänger ist erforderlich")
//							.showWarning();
//					btnEmpfaenger1.requestFocus();
//					return false;
//				}
//			} else {  
//				if (busObjName[i].length() < 1) {
//					Dialogs.create().owner(primaryStage)
//							.title(applName).masthead("Korrektur-Hinweis")
//							.message("Bitte zum Empfänger \"" + empf.getKomponente().getFullname() + "\""  +
//								    " auch ein Geschäftsobjekt eintragen/auswählen")
//							.showWarning();
//					switch(i) {
//						case 0: cmbBuOb1.requestFocus(); break;
//						case 1: cmbBuOb2.requestFocus(); break;
//						case 2: cmbBuOb3.requestFocus(); break;
//					}
//					return false;
//				}
//				empf.setGeschaeftsObjekt(businessObjectMap.get(busObjName[i].toUpperCase()));
//			}
//		}
//		try {
//			entityManager.getTransaction().begin(); 
//			aktEdi.getEdiEmpfaenger().clear();
//			for (int i=0; i<MAX_EMPFAENGER; ++i) {
//				EdiEmpfaenger empf = aktEmpfaenger[i];
//				if (empf != null) {
//					if (empf.getKomponente().getId() == 0L) {
//						entityManager.persist(empf);
//					}
//					aktEdi.getEdiEmpfaenger().add(empf);
//				}	
//			}
//			String tmpEdiBezeichnung = aktEdi.bezeichnung(); 
//			if (aktEdi.getBezeichnung() == null) {
//				aktEdi.setBezeichnung("");
//			}
//			if (aktEdi.getBezeichnung().equals(tmpEdiBezeichnung)==false) {
//				aktEdi.setBezeichnung(tmpEdiBezeichnung);
//				paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE + " "+ aktEdi.getEdiNrStr() + "  " + aktEdi.bezeichnung() );
//			}
//			aktEdi = entityManager.merge(aktEdi);
//			entityManager.getTransaction().commit();
//			entityManager.detach(aktEdi);
//		} catch (RuntimeException e) {
//			Dialogs.create().owner(primaryStage)
//			   .title(applName).masthead("Datenbankfehler")
//			   .message("Fehler beim speichern des Geschäftsobjektes")
//			   .showException(e);
//			return false;
//		}	
//		ediEintragIsChanged.set(false);
//		return true;
//	}
    
    //Action: Sender-Button is pressed
    @FXML
    void senderButton(ActionEvent event) {

    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktSenderId = aktEdi.getKomponente()==null ? 0L : aktEdi.getKomponente().getId();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktSenderId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == Actions.OK ) {
	    	Long selKomponentenID = komponentenAuswahlController.getSelectedKomponentenId();
    	    if (aktSenderId != selKomponentenID ) {
    	    	EdiKomponente sender = entityManager.find(EdiKomponente.class, selKomponentenID);
    	    	aktEdi.setKomponente(sender); 
    	    	System.out.println("aktEdi.senderName :" + aktEdi.senderNameProperty().get());
    	    	btnSender.setText(sender.getFullname());
    	    	ediEintragIsChanged.set(true);
    	    	senderIsSelected.set(true);
    	    }
    	}
    }

    //Action: EmpfaengerX-Button is pressed
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
    }

}
