package de.vbl.ediliste.controller;

import java.io.IOException;
import java.util.Iterator;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.control.Dialogs.DialogResponse;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;

import de.vbl.ediliste.controller.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiKomponente;

public class EdiEintragController {
	private static final String EDI_PANEL_TITLE = "EDI-Eintrag";
	private static final String SICHERHEITSABFRAGE = "Sicherheitsabfrage";

    @FXML private TitledPane paneSzenario;
    @FXML private TitledPane paneAnbindung;
    @FXML private TitledPane paneEdiEintrag;
    
    @FXML private TextField tfEdiBezeichnung;
    @FXML private TextArea  taEdiBeschreibung;
    @FXML private TextField tfDatenart1;
    @FXML private TextField tfDatenart2;
    @FXML private TextField tfDatenart3;
    @FXML private TextField ediLastChange;

    @FXML private Button btnEdiEintragSpeichern;
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    @FXML private Button btnSender;
    
    private static Stage primaryStage;
    private static String applName;
    private static EntityManager em;

    private BooleanProperty ediEintragIsChanged = new SimpleBooleanProperty(false);
    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger1IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger2IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger3IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty datenart1Exist = new SimpleBooleanProperty(false);
    private BooleanProperty datenart2Exist = new SimpleBooleanProperty(false);
    private BooleanProperty datenart3Exist = new SimpleBooleanProperty(false);
    private BooleanProperty readOnlyAccess = new SimpleBooleanProperty(false);
    
    private static EdiEintrag aktEdi;
    private EdiEmpfaenger aktEmpfaenger[] = new EdiEmpfaenger[3];
    

    @FXML
    void initialize() {
    	checkFieldFromView();
    }	
    	
	public void setInitial(Stage stage, String applikationName, EntityManager entityManager) {
		primaryStage = stage;
		applName = applikationName;
		em = entityManager;
		readOnlyAccess.set(false);
	}
	
    public void setSelection( EdiEintrag selEDI) {
    	System.out.println("EdiEintragController.show()");
		if (aktEdi!=null) {
	    	btnEmpfaenger1.disableProperty().unbind();
	    	btnEmpfaenger2.disableProperty().unbind();
	    	btnEmpfaenger3.disableProperty().unbind();
	    	tfDatenart1.disableProperty().unbind();
	    	tfDatenart2.disableProperty().unbind();
	    	tfDatenart3.disableProperty().unbind();
		}
		aktEdi = selEDI;
		em.detach(aktEdi);
		
		tfEdiBezeichnung.disableProperty().bind(readOnlyAccess);
    	taEdiBeschreibung.disableProperty().bind(readOnlyAccess);
    	btnSender.disableProperty().bind(readOnlyAccess);

		ediEintragIsChanged.set(false);
		paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE + " "+ aktEdi.getEdiNrStr());
		tfEdiBezeichnung.textProperty().bindBidirectional(aktEdi.bezeichnungProperty());
		taEdiBeschreibung.textProperty().bindBidirectional(aktEdi.beschreibungProperty());
		btnSender.setText(aktEdi.getKomponente()==null ? "" : aktEdi.getKomponente().getFullname());
		senderIsSelected.set(aktEdi.getKomponente()!=null);
    	Iterator<EdiEmpfaenger> empfaengerList = aktEdi.getEdiEmpfaenger().iterator();
		for (int i=0; i<3;i++) {
			aktEmpfaenger[i] = null;
			if (empfaengerList.hasNext()) {
				aktEmpfaenger[i] = empfaengerList.next();
				if (aktEmpfaenger[i].getDatenart()==null)
					aktEmpfaenger[i].setDatenart("");
			}
		}
		if (aktEmpfaenger[0]!=null) {
			btnEmpfaenger1.setText(aktEmpfaenger[0].getKomponente().getFullname());
			empfaenger1IsSelected.set(true);
			tfDatenart1.setText(aktEmpfaenger[0].getDatenart());
			datenart1Exist.set(aktEmpfaenger[0].getDatenart()!=null && aktEmpfaenger[0].getDatenart().length()>0);
		}
		else {
			btnEmpfaenger1.setText("");
			empfaenger1IsSelected.set(false);
			tfDatenart1.setText("");
			datenart1Exist.set(false);
		}
		if (aktEmpfaenger[1]!=null) {
			btnEmpfaenger2.setText(aktEmpfaenger[1].getKomponente().getFullname());
			empfaenger2IsSelected.set(true);
			tfDatenart2.setText(aktEmpfaenger[1].getDatenart());
			datenart2Exist.set(aktEmpfaenger[1].getDatenart()!=null && aktEmpfaenger[1].getDatenart().length()>0);
		}
		else {
			btnEmpfaenger2.setText("");
			empfaenger2IsSelected.set(false);
			tfDatenart2.setText("");
			datenart2Exist.set(false);
		}
		if (aktEmpfaenger[2]!=null) {
			btnEmpfaenger3.setText(aktEmpfaenger[2].getKomponente().getFullname());
			empfaenger3IsSelected.set(true);
			tfDatenart3.setText(aktEmpfaenger[2].getDatenart());
			datenart3Exist.set(aktEmpfaenger[2].getDatenart()!=null && aktEmpfaenger[2].getDatenart().length()>0);
		}
		else {
			btnEmpfaenger3.setText("");
			empfaenger3IsSelected.set(false);
			tfDatenart3.setText("");
			datenart3Exist.set(false);
		}
////		btnEmpfaenger1.setText(aktEmpfaenger[0]==null ? "" : aktEmpfaenger[0].getKomponente().getFullname());
////		btnEmpfaenger2.setText(aktEmpfaenger[1]==null ? "" : aktEmpfaenger[1].getKomponente().getFullname());
////		btnEmpfaenger3.setText(aktEmpfaenger[2]==null ? "" : aktEmpfaenger[2].getKomponente().getFullname());
////		empfaenger1IsSelected.set(aktEmpfaenger[0]!=null);
////		empfaenger2IsSelected.set(aktEmpfaenger[1]!=null);
////		empfaenger3IsSelected.set(aktEmpfaenger[2]!=null);
////		datenart1Exist.set(aktEmpfaenger[0]!=null && aktEmpfaenger[0].get..()!=null);
////		datenart2Exist.set(aktEmpfaenger[1]!=null && aktEmpfaenger[1].get..()!=null);
////		datenart2Exist.set(aktEmpfaenger[2]!=null && aktEmpfaenger[2].get..()!=null);
		
    	btnEmpfaenger1.disableProperty().bind(Bindings.not(senderIsSelected));
    	btnEmpfaenger2.disableProperty().bind(Bindings.not(datenart1Exist));
    	btnEmpfaenger3.disableProperty().bind(Bindings.not(datenart2Exist));
    	tfDatenart1.disableProperty().bind(Bindings.not(empfaenger1IsSelected));
    	tfDatenart2.disableProperty().bind(Bindings.not(empfaenger2IsSelected));
    	tfDatenart3.disableProperty().bind(Bindings.not(empfaenger3IsSelected));
    	
    	btnEdiEintragSpeichern.disableProperty().bind(Bindings.not(ediEintragIsChanged));

    	// paneSzenario.textProperty().bind(ediEintrag.szenarioNameProperty());
    }
	
	public void checkForChanges() {
		if (aktEdi != null) {
			EdiEintrag orgEdi = em.find(EdiEintrag.class, aktEdi.getId());
			if (aktEdi != orgEdi && aktEdi.equaels(orgEdi) == false) {
				if ( Dialogs.showConfirmDialog(primaryStage, "Soll die Änderungen am EDI-Eintrag " +
						aktEdi.getEdiNrStr() + " \"" + aktEdi.getBezeichnung() + "\"" +
						" gespeichert werden?", SICHERHEITSABFRAGE, applName, 
						DialogOptions.YES_NO) == DialogResponse.YES) {
					aktEdiEintragSpeichern();
					Dialogs.showInformationDialog(primaryStage, "Die Änderungen wurden gespeichert", "Info", applName);
				}
			}
		}
	}
	
    @FXML
    void ediEintragSpeichern(ActionEvent event) {
		aktEdiEintragSpeichern();
    }
    
    private boolean aktEdiEintragSpeichern() {
		em.getTransaction().begin(); 
//		aktEdi.setBezeichnung(tfEdiBezeichnung.getText());
//		aktEdi.setBeschreibung(taEdiBeschreibung.getText());
		System.out.println("textfield:"+tfEdiBezeichnung.getText());
		System.out.println("aktedi   :"+aktEdi.getBezeichnung());
		aktEdi.getEdiEmpfaenger().clear();
		for (int i=0; i<3;i++) {
			if (aktEmpfaenger[i]!=null) {
				aktEdi.getEdiEmpfaenger().add(aktEmpfaenger[i]);
			}
		}
		try {
			em.merge(aktEdi);
			em.getTransaction().commit();
		} catch (RuntimeException e) {
			Dialogs.showErrorDialog(primaryStage,
					"Fehler beim speichern des Eintrags",
					"Datenbankfehler",applName,e);
			return false;
		}	
		ediEintragIsChanged.set(false);
		System.out.println("Der Eintrag " + aktEdi.getEdiNrStr() + " wurde gespeichert");
		return true;
	}

	
//	public void setSelection(EdiEintrag newEdiEintrag) {
//		System.out.println("EdiEintragController.setSelection " + newEdiEintrag.getEdiNrStr());
//	}

//    @FXML
//    void initialize() {
//    	System.out.println("EdiEintragController.initialize() - vor checkFieldFrimView()");
//    	checkFieldFromView();
//    	System.out.println("EdiEintragController.initialize() - vor setupEntityManager()");
//        setupEntityManager();
//    	System.out.println("EdiEintragController.initialize() - vor loadEdiNrListData()");
//        loadEdiNrListData();
//        setupBindings();
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
//        tfDatenart1.textProperty().addListener(
//        		new ChangeListener<String>() {
//        			@Override
//        			public void changed(ObservableValue<? extends String> o,
//        				String oldValue, String newValue) {
//        				if (aktEmpfaenger[0]!=null) {
//        					if (newValue.equals(aktEmpfaenger[0].getDatenart())==false) {
//        						ediEintragIsChanged.set(true);
//        						System.out.println("aktE[0] :"+ aktEmpfaenger[0].getDatenart());
//        						System.out.println("newValue:"+ newValue);
//        						aktEmpfaenger[0].setDatenart(newValue);
//        					}
//        				}
//        			}
//        		}
//        );
//        tfDatenart2.textProperty().addListener(
//        		new ChangeListener<String>() {
//        			@Override
//        			public void changed(ObservableValue<? extends String> o,
//        				String oldValue, String newValue) {
//        				if (aktEmpfaenger[1]!=null) {
//        					if (newValue.equals(aktEmpfaenger[1].getDatenart())==false) {
//        						ediEintragIsChanged.set(true);
//        						System.out.println("aktE[1] :"+ aktEmpfaenger[1].getDatenart());
//        						System.out.println("newValue:"+ newValue);
//        						aktEmpfaenger[1].setDatenart(newValue);
//        					}
//        				}
//        			}
//        		}
//        );
//        tfDatenart3.textProperty().addListener(
//        		new ChangeListener<String>() {
//        			@Override
//        			public void changed(ObservableValue<? extends String> o,
//        				String oldValue, String newValue) {
//        				if (aktEmpfaenger[2]!=null) {
//        					if (newValue.equals(aktEmpfaenger[2].getDatenart())==false) {
//        						ediEintragIsChanged.set(true);
//        						System.out.println("aktE[2] :"+ aktEmpfaenger[2].getDatenart());
//        						System.out.println("newValue:"+ newValue);
//        						aktEmpfaenger[2].setDatenart(newValue);
//        					}
//        				}
//        			}
//        		}
//        );
//        
//        tableEdiNrAuswahl.getSelectionModel().selectedItemProperty().addListener(
//        		new ChangeListener<EdiNrListElement>() {
//        			@Override
//        			public void changed(
//        					ObservableValue<? extends EdiNrListElement> observable,
//        					EdiNrListElement oldValue, EdiNrListElement newValue) {
//        				System.out.println("oldValue=" + ((oldValue == null) ? "null" : oldValue.ediNrProperty().get()) 
//        							   + "  newValue=" + ((newValue == null) ? "null" : newValue.ediNrProperty().get()) );
//        				if (ediEintragIsChanged.get() == true) {
//        					if ( Dialogs.showConfirmDialog(primaryStage, "Soll die Änderungen am EDI-Eintrag " +
//        							aktEdi.getEdiNrStr() + " \"" + aktEdi.getBezeichnung() + "\"" +
//        							" gespeichert werden?", SICHERHEITSABFRAGE, applName, 
//        							DialogOptions.OK_CANCEL) == DialogResponse.OK) {
//        						aktEdiEintragSpeichern();
//        					}
//        						
//        				}
//        				final EdiEintrag defEdi = new EdiEintrag();
//        				if (oldValue != null) {
//        					tfEdiBezeichnung.textProperty().unbindBidirectional(defEdi.bezeichnungProperty());
//        					tfEdiBezeichnung.textProperty().unbindBidirectional(aktEdi.bezeichnungProperty());
//        				}
//        				if (newValue != null) {
//        					
//        					aktEdi = em.find(EdiEintrag.class, newValue.getEdiId());
//        					ediEintragIsChanged.set(false);
//        					paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE + " "+ aktEdi.getEdiNrStr());
//        					tfEdiBezeichnung.setText(aktEdi.bezeichnungProperty().get());
////        					tfEdiBezeichnung.textProperty().bindBidirectional(aktEdi.bezeichnungProperty());
//        					taEdiBeschreibung.setText(aktEdi.getBeschreibung()==null ? "" : aktEdi.getBeschreibung());
//        					btnSender.setText(aktEdi.getKomponente()==null ? "" : aktEdi.getKomponente().getFullname());
//        					senderIsSelected.set(aktEdi.getKomponente()!=null);
//        			    	Iterator<EdiEmpfaenger> empfaengerList = aktEdi.getEdiEmpfaenger().iterator();
//        					for (int i=0; i<3;i++) {
//        						aktEmpfaenger[i] = null;
//        						if (empfaengerList.hasNext()) {
//        							aktEmpfaenger[i] = empfaengerList.next();
//        							if (aktEmpfaenger[i].getDatenart()==null)
//        								aktEmpfaenger[i].setDatenart("");
//        						}
//        					}
//        					if (aktEmpfaenger[0]!=null) {
//        						btnEmpfaenger1.setText(aktEmpfaenger[0].getKomponente().getFullname());
//        						empfaenger1IsSelected.set(true);
//        						tfDatenart1.setText(aktEmpfaenger[0].getDatenart());
//        						datenart1Exist.set(aktEmpfaenger[0].getDatenart()!=null && aktEmpfaenger[0].getDatenart().length()>0);
//        					}
//        					else {
//        						btnEmpfaenger1.setText("");
//        						empfaenger1IsSelected.set(false);
//        						tfDatenart1.setText("");
//        						datenart1Exist.set(false);
//        					}
//        					if (aktEmpfaenger[1]!=null) {
//        						btnEmpfaenger2.setText(aktEmpfaenger[1].getKomponente().getFullname());
//        						empfaenger2IsSelected.set(true);
//        						tfDatenart2.setText(aktEmpfaenger[1].getDatenart());
//        						datenart2Exist.set(aktEmpfaenger[1].getDatenart()!=null && aktEmpfaenger[1].getDatenart().length()>0);
//        					}
//        					else {
//        						btnEmpfaenger2.setText("");
//        						empfaenger2IsSelected.set(false);
//        						tfDatenart2.setText("");
//        						datenart2Exist.set(false);
//        					}
//        					if (aktEmpfaenger[2]!=null) {
//        						btnEmpfaenger3.setText(aktEmpfaenger[2].getKomponente().getFullname());
//        						empfaenger3IsSelected.set(true);
//        						tfDatenart3.setText(aktEmpfaenger[2].getDatenart());
//        						datenart3Exist.set(aktEmpfaenger[2].getDatenart()!=null && aktEmpfaenger[2].getDatenart().length()>0);
//        					}
//        					else {
//        						btnEmpfaenger3.setText("");
//        						empfaenger3IsSelected.set(false);
//        						tfDatenart3.setText("");
//        						datenart3Exist.set(false);
//        					}
////        					btnEmpfaenger1.setText(aktEmpfaenger[0]==null ? "" : aktEmpfaenger[0].getKomponente().getFullname());
////        					btnEmpfaenger2.setText(aktEmpfaenger[1]==null ? "" : aktEmpfaenger[1].getKomponente().getFullname());
////        					btnEmpfaenger3.setText(aktEmpfaenger[2]==null ? "" : aktEmpfaenger[2].getKomponente().getFullname());
////        					empfaenger1IsSelected.set(aktEmpfaenger[0]!=null);
////        					empfaenger2IsSelected.set(aktEmpfaenger[1]!=null);
////        					empfaenger3IsSelected.set(aktEmpfaenger[2]!=null);
////        					datenart1Exist.set(aktEmpfaenger[0]!=null && aktEmpfaenger[0].get..()!=null);
////        					datenart2Exist.set(aktEmpfaenger[1]!=null && aktEmpfaenger[1].get..()!=null);
////        					datenart2Exist.set(aktEmpfaenger[2]!=null && aktEmpfaenger[2].get..()!=null);
//        				}
//        				else {
//        					paneEdiEintrag.textProperty().set(EDI_PANEL_TITLE);
//        					tfEdiBezeichnung.textProperty().bindBidirectional(defEdi.bezeichnungProperty());
//        					taEdiBeschreibung.setText("");
//        					btnSender.setText("");
//        					btnEmpfaenger1.setText("");
//        					btnEmpfaenger2.setText("");
//        					btnEmpfaenger3.setText("");
//        		    		senderIsSelected.set(false);
//        		    		empfaenger1IsSelected.set(false);
//        		    		empfaenger2IsSelected.set(false);
//        		    		empfaenger3IsSelected.set(false);
//        		    		datenart1Exist.set(false);
//        		    		datenart2Exist.set(false);
//        		    		datenart3Exist.set(false);
//        				}
//        			}
//				}
//        );
//    }
    
	/* *****************************************************************************
	 * 
	 * ****************************************************************************/
    
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
    	System.out.println(btnNr + " Empfänger:" + aktEmpfaenger[btnNr]);
    	Long aktEmpfaengerId = (aktEmpfaenger[btnNr]==null ? 0L : aktEmpfaenger[btnNr].getKomponente().getId());
    	komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktEmpfaengerId);
    	dialog.showAndWait();
    	if (komponentenAuswahlController.getResponse() == DialogResponse.OK ) {
    		Long selEmpfaengerID = komponentenAuswahlController.getSelectedKomponentenId();
    		if (aktEmpfaengerId != selEmpfaengerID) {
    			if (aktEmpfaenger[btnNr]==null) {
    				aktEmpfaenger[btnNr] = new EdiEmpfaenger(aktEdi);
    				em.persist(aktEmpfaenger[btnNr]);
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
    
    private void checkFieldFromView() {
        assert paneAnbindung != null : "fx:id=\"paneAnbindung\" was not injected: check your FXML file 'Main.fxml'.";
        assert paneSzenario != null : "fx:id=\"paneSzenario\" was not injected: check your FXML file 'Main.fxml'.";
        assert paneEdiEintrag != null : "fx:id=\"paneEdiEintrag\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnSender != null : "fx:id=\"btnSender\" was not injected: check your FXML file 'Main.fxml'.";
        assert tfEdiBezeichnung != null : "fx:id=\"tfEdiBezeichnung\" was not injected: check your FXML file 'Main.fxml'.";
        assert taEdiBeschreibung != null : "fx:id=\"taEdiBeschreibung\" was not injected: check your FXML file 'Main.fxml'.";
        assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'Main.fxml'.";
        assert tfDatenart1 != null : "fx:id=\"tfDatenart1\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnEmpfaenger1 != null : "fx:id=\"btnEmpfaenger1\" was not injected: check your FXML file 'Main.fxml'.";
        assert btnEdiEintragSpeichern != null : "fx:id=\"btnEdiEintragSpeichern\" was not injected: check your FXML file 'Main.fxml'.";
    }
}
