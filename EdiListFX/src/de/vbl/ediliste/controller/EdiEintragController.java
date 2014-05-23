package de.vbl.ediliste.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
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
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";
	private static final Integer MAX_EMPFAENGER = 3;

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
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    @FXML private Button btnSender;
    
//  private static MainController mainController;
    private static Stage primaryStage;
    private static String applName;
    private static EntityManager em;

    private BooleanProperty ediEintragIsChanged = new SimpleBooleanProperty(false);
    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger1IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger2IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger3IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty buOb1Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb2Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb3Exist = new SimpleBooleanProperty(false);
    private BooleanProperty readOnlyAccess = new SimpleBooleanProperty(false);
    
    private Map<String,GeschaeftsObjekt> businessObjectMap = new HashMap<String,GeschaeftsObjekt>(); 
    private ObservableList<String> businessObjectName = FXCollections.observableArrayList();
    
    private static EdiEintrag aktEdi;
    private EdiEmpfaenger aktEmpfaenger[] = new EdiEmpfaenger[MAX_EMPFAENGER];
    private String busObjName[] = { "", "", ""};

    @FXML
    void initialize() {
    	System.out.println("EdiEintragController.initialize()");
    	checkFieldFromView();
    	
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
    	
	public void setInitial(Stage stage, String applikationName, EntityManager entityManager) {
//    	mainController = main;
		primaryStage = stage;
		applName = applikationName;
		em = entityManager;
		readOnlyAccess.set(false);

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
					if (aktEdi.beschreibungProperty().get().equals(newValue) == false) {
						aktEdi.beschreibungProperty().set(newValue);
						ediEintragIsChanged.set(true);
					} else if (ediEintragIsChanged.get()) {
						ediEintragIsChanged.set(aktEdiEqualPersistence()==false); 
					}
				}
			}
		});
		
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
				ediEintragIsChanged.set(aktEdiEqualPersistence()==false);
			}
		}
		else {
			GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
			if (buOb != null) {
				aktName = buOb.getName();
				ediEintragIsChanged.set(true);
			} else {
				String msg = "Soll das folgende Geschäftsobjekt neu angelegt werden?"; 
				newName = Dialogs.showInputDialog(primaryStage, msg, null, applName, newName);
				if (newName != null) {
					GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(newName);
					try {
						em.getTransaction().begin();
						em.persist(newBusObj);
						em.getTransaction().commit();
						msg = "Geschäftsobjekt \"" + newBusObj.getName() + "\" erfolgreich gespeichert";
						Dialogs.showInformationDialog(primaryStage, msg);
						businessObjectName.add(newName);
						businessObjectMap.put(newName.toUpperCase(), newBusObj);
						aktName = newName;
						ediEintragIsChanged.set(true);
					} catch (RuntimeException er) {
						Dialogs.showErrorDialog(primaryStage,
								"Fehler beim speichern des Geschäftsobjektes",
								"Datenbankfehler",applName,er);
					}
				}	
			}
		}
		return aktName;
	}	
	
	private void readBusinessObject() {
		businessObjectMap.clear();
		businessObjectName.clear();
		TypedQuery<GeschaeftsObjekt> tq = em.createQuery(
				"SELECT g FROM GeschaeftsObjekt g ORDER BY g.name", GeschaeftsObjekt.class);
		List<GeschaeftsObjekt> gList = tq.getResultList();
		for (GeschaeftsObjekt gObject : gList) {
			businessObjectName.add(gObject.getName());
			businessObjectMap.put(gObject.getName().toUpperCase(), gObject);
		}
		
	}
	
    public void setSelection( EdiEintrag selEDI) {
		if (aktEdi == null || aktEdi.getId() != selEDI.getId() ) {
			aktEdi = selEDI;
			em.detach(aktEdi);
		}	
		if (aktEdi.getBeschreibung() == null) {
			aktEdi.setBeschreibung("");
		}
		paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE + " "+ aktEdi.getEdiNrStr() + "  " + aktEdi.bezeichnung() );
		taEdiBeschreibung.textProperty().set(aktEdi.getBeschreibung());
		btnSender.setText(aktEdi.getKomponente()==null ? "" : aktEdi.getKomponente().getFullname());
		senderIsSelected.set(aktEdi.getKomponente()!=null);
    	Iterator<EdiEmpfaenger> empfaengerList = aktEdi.getEdiEmpfaenger().iterator();
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
		

		ediEintragIsChanged.set(false);
    }
	
	public boolean checkForContinueEditing() {
		if (aktEdi != null && aktEdiEqualPersistence() == false) {
			String msg = "Soll die Änderungen am EDI-Eintrag " + aktEdi.getEdiNrStr() + 
					   " \"" + aktEdi.getBezeichnung() + "\" gespeichert werden?";
			DialogResponse res = Dialogs.showConfirmDialog(primaryStage, msg , SICHERHEITSABFRAGE, 
											  applName, DialogOptions.YES_NO_CANCEL);
			if (res == DialogResponse.YES) {
				if (aktEdiEintragSpeichern()==false)
					return true;
				Dialogs.showInformationDialog(primaryStage, "Die Änderungen wurden gespeichert", "Info", applName);
			} else if (res != DialogResponse.NO) {
				return true;
			}
		}
		return false;
	}
	
	private boolean aktEdiEqualPersistence() {
		EdiEintrag orgEdi = em.find(EdiEintrag.class, aktEdi.getId());
		if (aktEdi.equaels(orgEdi)) {
			return true;
		}
		return false;
	}
	
    @FXML
    void ediEintragSpeichern(ActionEvent event) {
		aktEdiEintragSpeichern();
    }
    
    private boolean aktEdiEintragSpeichern() {
		// Prüfungen
    	if (aktEdi.getKomponente()==null) {
			String msg = "Sender ist erforderlich";
			Dialogs.showInformationDialog(primaryStage, msg, "Korrektur-Hinweis", applName);
			btnSender.requestFocus();
			return false;
    	}
		for (int i=0; i<MAX_EMPFAENGER; ++i) {
			EdiEmpfaenger empf = aktEmpfaenger[i];
			if (empf == null) {
				if (i==0) {
					String msg = "Empfänger ist erforderlich";
					Dialogs.showInformationDialog(primaryStage, msg, "Korrektur-Hinweis", applName);
					btnEmpfaenger1.requestFocus();
					return false;
				}
			} else {  
				if (busObjName[i].length() < 1) {
					String msg = "Bitte zum Empfänger \"" + empf.getKomponente().getFullname() + "\""  +
							    " auch ein Geschäftsobjekt eintragen/auswählen";
					Dialogs.showInformationDialog(primaryStage, msg, "Korrektur-Hinweis", applName);
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
		try {
			em.getTransaction().begin(); 
			aktEdi.getEdiEmpfaenger().clear();
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				EdiEmpfaenger empf = aktEmpfaenger[i];
				if (empf != null) {
					if (empf.getKomponente().getId() == 0L) {
						em.persist(empf);
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
//				mainController.refreshEdiNrListBezeichnung(aktEdi.getId(), tmpEdiBezeichnung);
				paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE + " "+ aktEdi.getEdiNrStr() + "  " + aktEdi.bezeichnung() );
			}
			aktEdi = em.merge(aktEdi);
			em.getTransaction().commit();
			em.detach(aktEdi);
		} catch (RuntimeException e) {
			Dialogs.showErrorDialog(primaryStage,
					"Fehler beim Speichern des Eintrags",
					"Datenbankfehler",applName,e);
			return false;
		}	
		ediEintragIsChanged.set(false);
		return true;
	}
    
	
    //    @FXML
//    void initialize() {
//
//        tfEdiBezeichnung.textProperty().addListener(
//        		new ChangeListener<String>() {
//        			@Override
//        			public void changed(ObservableValue<? extends String> o,
//        				String oldValue, String newValue) {
//        				if (newValue.equals(aktEdi.bezeichnungProperty().get())==false) {
//        					ediEintragIsChanged.set(true);
//        				}
//        			}
//        		}
//        );
    
    
    @FXML
    void senderButton(ActionEvent event) {

    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 

    	KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    	Long aktSenderId = (aktEdi.getKomponente()==null) ? 0L : aktEdi.getKomponente().getId();
    	komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktSenderId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == DialogResponse.OK ) {
	    	Long selKomponentenID = komponentenAuswahlController.getSelectedKomponentenId();
    	    if (aktSenderId != selKomponentenID	) {
   	    		aktEdi.setKomponente(em.find(EdiKomponente.class, selKomponentenID));
   	    		senderIsSelected.set(true);
    	    	btnSender.setText(aktEdi.getKomponente().getFullname());
    	    	ediEintragIsChanged.set(true);
    	    }
    	}
    }

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
    	if (komponentenAuswahlController.getResponse() == DialogResponse.OK ) {
    		Long selEmpfaengerID = komponentenAuswahlController.getSelectedKomponentenId();
    		if (aktEmpfaengerId != selEmpfaengerID) {
    			if (aktEmpfaenger[btnNr] == null) {
    				aktEmpfaenger[btnNr] = new EdiEmpfaenger(aktEdi);
    			}
    			aktEmpfaenger[btnNr].setKomponente(em.find(EdiKomponente.class,selEmpfaengerID));
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

//    private void syspr(String methode, String text) {
//		String classname = getClass().getName() + "." + methode;
//		while (classname.length()< 60)
//			classname += " ";
//		System.out.println(classname + " " + text);
//	}
    
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
