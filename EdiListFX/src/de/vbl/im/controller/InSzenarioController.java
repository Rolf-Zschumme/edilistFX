package de.vbl.im.controller;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Ansprechpartner;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InSzenario;
import de.vbl.im.tools.IMconstant;

public class InSzenarioController {
	private static final Logger logger = LogManager.getLogger(InSzenarioController.class.getName()); 
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<InSzenario> inSzenario;
	private final ObservableSet<Integration> integrationSet;      // all integration for this IS
	private final ObservableList<Ansprechpartner> ansprechpartnerList; 
	
	private InSzenario aktInSzenario = null;
	
	private BooleanProperty editEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TitledPane m_inSzenarioDetailsPane;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private ListView<Ansprechpartner> lvAnsprechpartner;
    @FXML private TableView<InEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<InEmpfaenger, String> tcInNr;
    @FXML private TableColumn<InEmpfaenger, String> tcKonfiguration;
    @FXML private TableColumn<InEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<InEmpfaenger, String> tcSender;
    @FXML private TableColumn<InEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnNeu;
    @FXML private Button btnLoeschen;
    @FXML private Button btnAddAnsprechpartner;
    @FXML private Button btnRemoveAnsprechpartner;
    
    public InSzenarioController() {
    	this.inSzenario = new SimpleObjectProperty<>(this, "inSzenario", null);
    	this.integrationSet = FXCollections.observableSet();
    	this.ansprechpartnerList = FXCollections.observableArrayList();
    }

	public void setParent(IMController managerController) {
		logger.entry();
		InSzenarioController.mainCtr = managerController;
		InSzenarioController.primaryStage = IMController.getStage();
		InSzenarioController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		inSzenario.addListener(new ChangeListener<InSzenario>() {
			@Override
			public void changed(ObservableValue<? extends InSzenario> ov,
					InSzenario oldInSzenario, InSzenario newInSzenario) {
				logger.info((oldInSzenario==null) ? "null" : oldInSzenario.getName() + " -> " 
						 + ((newInSzenario==null) ? "null" : newInSzenario.getName() ));
				if (oldInSzenario != null) {
					if(newInSzenario == null) {
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
					}
					ansprechpartnerList.clear();
					integrationSet.clear();
				}
				if (newInSzenario != null) {
					aktInSzenario = newInSzenario;
					readTablesForInSzenario(newInSzenario);
					tfBezeichnung.setText(newInSzenario.getName());
					if (newInSzenario.getBeschreibung() == null) {
						newInSzenario.setBeschreibung("");
					}
					taBeschreibung.setText(newInSzenario.getBeschreibung());
					ansprechpartnerList.addAll(newInSzenario.getAnsprechpartner());
					btnRemoveAnsprechpartner.disableProperty().bind(
							lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
					editEnabled.set(true);
				}
				dataIsChanged.set(false);
			}
		});
		m_inSzenarioDetailsPane.disableProperty().bind(Bindings.not(editEnabled));
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.not(editEnabled)
							.or(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(integrationSet)))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktInSzenario.getName().equals(newValue) == false) {
				msg = checkInSzenarioName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktInSzenario.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
		lvAnsprechpartner.setItems(ansprechpartnerList);
		lvAnsprechpartner.setCellFactory( (list) -> {
			return new ListCell<Ansprechpartner>() {
				@Override
				protected void updateItem(Ansprechpartner k, boolean empty) {
					super.updateItem(k, empty);
					if (k == null || empty) {
						setText(null);
					} else {
						setText(k.getArtNameFirma());
					}
				}
			};
		});
		
//	    Setup for Sub-Panel    
		
		tcInNr.setCellValueFactory(cellData -> cellData.getValue().getIntegration().inNrStrExp());

		tcKonfiguration.setCellValueFactory(cellData -> cellData.getValue().getIntegration().konfigurationNameProperty());
		
		tcSender.setCellValueFactory(cellData -> cellData.getValue().getIntegration().getInKomponente().fullnameProperty());
		
//		tcSender.setCellFactory(column -> {
//			return new TableCell<InEmpfaenger, String>() {
//				@Override
//				protected void updateItem (String senderFullname, boolean empty) {
//					super.updateItem(senderFullname, empty);
//					if (senderFullname == null || empty) 
//						setText(null); 
//					else {
//						setText(senderFullname);
//					}
//				}
//			};
//		});
		
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());

//		tcEmpfaenger.setCellFactory(column -> {
//			return new TableCell<InEmpfaenger, String>() {
//				@Override
//				protected void updateItem (String empfaengerFullname, boolean empty) {
//					super.updateItem(empfaengerFullname, empty);
//					if (empfaengerFullname == null || empty) 
//						setText(null); 
//					else {
//						setText(empfaengerFullname);
//					}
//				}
//			};
//		});
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
		
		// TODO: for direct jump to another integration from this table
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<InEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends InEmpfaenger> ov, InEmpfaenger oldValue, InEmpfaenger newValue) {
				logger.info("Jump to newValue: " + newValue);
			}
		});
	}


    @FXML
    void action_Neu(ActionEvent event) {
    	if (checkForChangesAndAskForSave() == true) {
    		InSzenario neuesIS = neuesInSzenarioAnlegen();
    		if (neuesIS != null) {
    			mainCtr.setSelectedInSzenario(neuesIS);
    		}
    	}
    }
    	
    static public InSzenario neuesInSzenarioAnlegen () {	
    	InSzenario inSzenario = null;
    	String aktName = "";
    	String masterhead = null;
		while (true) {
			Optional<String> newName = Dialogs.create()
				.owner(primaryStage)
				.title(IMconstant.APPL_NAME)
				.masthead(masterhead)
				.message("Wie soll das neue Integrationsszenario heißen?")
				.showTextInput(aktName);
			if ( !newName.isPresent() ) {
				mainCtr.setInfoText("Die Neuanlage wurde abgebrochen");
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
				masterhead = "Das Intgrationsszenario\n\"" +iList.get(0).getName() + "\"" + 
							 "\nist bereits vorhanden.\nBitte ändern oder abbrechen";
				continue;
			}
			try {
				inSzenario = new InSzenario(aktName);
				inSzenario.setIsNr(inSzenario.getMaxIsNr(entityManager)+1);
				entityManager.getTransaction().begin();
				entityManager.persist(inSzenario);
				entityManager.getTransaction().commit();
				
				mainCtr.loadInSzenarioListData();
				mainCtr.setInfoText("Das Intergrationsszenario \"" + aktName + "\"" + 
					" wurde erfolgreich erstellt");
				break;
			} catch (RuntimeException er) {
				Dialogs.create().owner(primaryStage)
					.title(IMconstant.APPL_NAME)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen einer neuen InSzenario")
					.showException(er);
			}
		}
		return inSzenario;
    }

	@FXML
	void action_Loeschen(ActionEvent event) {
		if (integrationSet.size() > 0) {
			mainCtr.setErrorText("Fehler beim Löschen der InSzenario " + aktInSzenario.getName() +" wird verwendet");
			return;
		}	
		String inSzenarioName1 = "InSzenario \"" + aktInSzenario.getName() + "\"";
		String inSzenarioName2 = inSzenarioName1;
		if (aktInSzenario.getName().equals(tfBezeichnung.getText()) == false) {
			inSzenarioName2 = inSzenarioName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(inSzenarioName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktInSzenario);
				entityManager.getTransaction().commit();
				aktInSzenario = null;
				mainCtr.loadInSzenarioListData();
				mainCtr.setInfoText("Die InSzenario \"" + inSzenarioName1 +
									 "\" wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + inSzenarioName1)
				    .showException(er);
			}
		}
	}
	
	@FXML
	void action_Speichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesWithMode(Checkmode checkmode) {
		logger.info("checkMode =" + checkmode);
		if (aktInSzenario == null ) {
			return true;
		}
		String orgName = aktInSzenario.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktInSzenario.getBeschreibung()==null ? "" : aktInSzenario.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) &&
			aktInSzenario.getAnsprechpartner().containsAll(ansprechpartnerList) &&
			ansprechpartnerList.containsAll(aktInSzenario.getAnsprechpartner())   )
		{
			logger.info("Name und Bezeichnung unveraendert");
			return true;
		}
		if (checkmode == Checkmode.ONLY_CHECK) {
			return false;
		}	
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			Action response = Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
					.message("Sollen die Änderungen der Integrationsszenario '" + orgName + "' gespeichert werden ?")
					.showConfirm();
			if (response == Dialog.Actions.CANCEL) {
				return false;
			}
			if (response == Dialog.Actions.NO) {
				aktInSzenario = null;
				return true;
			}
		}
		String msg = checkInSzenarioName(newName);
		if (msg != null) {
			mainCtr.setErrorText(msg);
			tfBezeichnung.requestFocus();
			return false;
		}
		logger.info("Änderung erkannt -> update " + newName);
		try {
			entityManager.getTransaction().begin();
			aktInSzenario.setName(newName);
			aktInSzenario.setBeschreibung(newBeschreibung);
			boolean kontaktListChanged = aktInSzenario.getAnsprechpartner()
					.retainAll(ansprechpartnerList);
			for (Ansprechpartner k : ansprechpartnerList) {
				if (aktInSzenario.getAnsprechpartner().contains(k)== false) {
					aktInSzenario.getAnsprechpartner().add(k);
					kontaktListChanged = true;
				}
			}
			entityManager.getTransaction().commit();
			if (kontaktListChanged) {
				mainCtr.refreshKontaktReferences();
			}
			mainCtr.setInfoText("Das Integrationsszenario '" + newName + "' wurde gespeichert");
			dataIsChanged.set(false);
		} catch (RuntimeException e) {
			logger.error("Message:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage)
			.title(primaryStage.getTitle())
			.masthead("FEHLER")
			.message("Fehler beim Speichern des Integrationsszenarios:\n" + e.getMessage())
			.showException(e);
		}
		readTablesForInSzenario(aktInSzenario);
		return true;
	}
	
	private String checkInSzenarioName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<InSzenario> tq = entityManager.createQuery(
				"SELECT i FROM InSzenario i WHERE LOWER(i.name) = LOWER(:n)",InSzenario.class);
		tq.setParameter("n", newName);
		List<InSzenario> inSzenarioList = tq.getResultList();
		for (InSzenario i : inSzenarioList ) {
			if (i.getId() != aktInSzenario.getId() )  {
				if (i.getName().equalsIgnoreCase(newName)) {
					return "Ein anderes Integrationsszenario heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readTablesForInSzenario( InSzenario selInSzenario) {
		tvVerwendungen.getItems().clear();
		ObservableList<InEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		/* 1. lese alle Einträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.inSzenario = :i", Integration.class);
		tqS.setParameter("i", selInSzenario);
		List<Integration> resultList = tqS.getResultList();
		for(Integration e : resultList ) {
			integrationSet.add(e);
			if (e.getInEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getInEmpfaenger());
//				for(InEmpfaenger ee : e.getInEmpfaenger() ) inKomponenteList.add(ee); 
			} else {
				InEmpfaenger tmpE = new InEmpfaenger();
				tmpE.setIntegration(e);
				empfaengerList.add(tmpE);
			}
		}
		tvVerwendungen.setItems(empfaengerList);
	}

    @FXML
    void action_AddAnsprechpartner(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	AnsprechpartnerAuswaehlenController controller = mainCtr.loadAnsprechpartnerAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		String userInfo = "Die Kontakt-Auswahl wurde abgebrochen"; 
    		if (controller.getResponse() == Actions.OK) {
    			Ansprechpartner selectedKontakt = controller.getAnsprechpartner();
    			if (ansprechpartnerList.contains(selectedKontakt)) {
    				userInfo = "Der ausgewählte Kontakt ist bereits eingetragen";
    			} else {
    				ansprechpartnerList.add(selectedKontakt);
    				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
    				userInfo = "Der ausgewählte Kontakt wurde ergänzt";
    			}
    		}
    		mainCtr.setInfoText(userInfo);
    	}
    }

    @FXML
    void action_RemoveAnsprechpartner(ActionEvent event) {
    	Ansprechpartner toBeRemoved = lvAnsprechpartner.getSelectionModel().getSelectedItem();
    	logger.info("remove Kontakt " + toBeRemoved.getNachname());
    	ansprechpartnerList.remove(toBeRemoved);
    	mainCtr.setInfoText("Der Ansprechpartner \"" + toBeRemoved.getVorname() + " " + 
    					toBeRemoved.getNachname() + "\" wurde aus dieser Liste entfernt");
		dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
    }
	
	public final ObjectProperty<InSzenario> inSzenarioProperty() {
		return inSzenario;
	}
	
	public final InSzenario getInSzenario() {
		return inSzenario.get() ;
	}
	
	public final void setInSzenario(InSzenario inSzenario) {
		this.inSzenario.set(inSzenario);
	}
    
	final static String fxmlFilename = "InSzenario.fxml";
	final static String fxmlErrortxt = "' was not injected: check FXML-file '" + fxmlFilename + "'.";
	void checkFieldsFromView() {
		assert m_inSzenarioDetailsPane	!= null : "fx:id=\"m_inSzenarioDetailsPane"	 + fxmlErrortxt;
		assert btnSpeichern 			!= null : "fx:id=\"btnSpeichern"			 + fxmlErrortxt;
		assert btnNeu 					!= null : "fx:id=\"btnNeu"					 + fxmlErrortxt;
		assert btnLoeschen 				!= null : "fx:id=\"btnLoeschen"				 + fxmlErrortxt;
		assert btnAddAnsprechpartner 	!= null : "fx:id=\"btnAddAnsprechpartner" 	 + fxmlErrortxt;
		assert btnRemoveAnsprechpartner != null : "fx:id=\"btnRemoveAnsprechpartner" + fxmlErrortxt;
    	assert tfBezeichnung 			!= null : "fx:id='tfBezeichnung"   			 + fxmlErrortxt;
    	assert taBeschreibung			!= null : "fx:id='taBeschreibung" 			 + fxmlErrortxt;
    	assert tvVerwendungen 			!= null : "fx:id=\"tvVerwendungen"			 + fxmlErrortxt;
    	assert tcInNr 					!= null : "fx:id=\"tcInNr"					 + fxmlErrortxt;
    	assert tcKonfiguration			!= null : "fx:id=\"tcKonfiguration"			 + fxmlErrortxt;
    	assert tcSender 				!= null : "fx:id=\"tcSender"				 + fxmlErrortxt;
        assert tcGeschaeftsobjekt 		!= null : "fx:id=\"tcGeschaeftsobjekt"		 + fxmlErrortxt;
        assert tcDatumAb 				!= null : "fx:id=\"tcDatumAb"				 + fxmlErrortxt;
        assert tcDatumBis 				!= null : "fx:id=\"tcDatumBis"				 + fxmlErrortxt;
    }
    
}
