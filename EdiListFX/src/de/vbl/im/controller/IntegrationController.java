package de.vbl.im.controller;

import java.net.URL;
import java.util.List;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.im.model.EdiEintrag;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.Integration;

public class IntegrationController {
	private static final Logger logger = LogManager.getLogger(IntegrationController.class.getName()); 
	private static Stage primaryStage = null;
	private static IntegrationManagerController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<Integration> integration;
	private final ObservableSet<EdiEintrag> ediEintragsSet;      // all assigned EDI-Entities
	private Integration aktIntegration = null;

	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcKonfiguration;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public IntegrationController() {
    	this.integration = new SimpleObjectProperty<>(this, "integration", null);
    	this.ediEintragsSet = FXCollections.observableSet();
    }

	public static void setParent(IntegrationManagerController managerController) {
		logger.entry();
		IntegrationController.mainCtr = managerController;
		IntegrationController.primaryStage = IntegrationManagerController.getStage();
		IntegrationController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		integration.addListener(new ChangeListener<Integration>() {
			@Override
			public void changed(ObservableValue<? extends Integration> ov,
					Integration oldIntegration, Integration newIntegration) {
				log("ChangeListener<EdiKomponente>",
					((oldIntegration==null) ? "null" : oldIntegration.getName() + " -> " 
				  + ((newIntegration==null) ? "null" : newIntegration.getName() )));
				if (oldIntegration != null && newIntegration == null) {
					ediEintragsSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
				}
				if (newIntegration != null) {
					aktIntegration = newIntegration;
					readEdiListeforIntegration(newIntegration);
					tfBezeichnung.setText(newIntegration.getName());
					if (newIntegration.getBeschreibung() == null) {
						newIntegration.setBeschreibung("");
					}
					taBeschreibung.setText(newIntegration.getBeschreibung());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(ediEintragsSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktIntegration.getName().equals(newValue) == false) {
				msg = checkIntegrationName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktIntegration.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
//	    Setup for Sub-Panel    
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(EdiEintrag.FORMAT_EDINR, 
												cellData.getValue().getEdiEintrag().ediNrProperty()));


		tcKonfiguration.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().konfigurationNameProperty());
		
		tcSender.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().getEdiKomponente().fullnameProperty());
		
//		tcSender.setCellFactory(column -> {
//			return new TableCell<EdiEmpfaenger, String>() {
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
//			return new TableCell<EdiEmpfaenger, String>() {
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
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
		
		// todo: zum Absprung bei Select eines Edi-Eintrages in der Sub-Tabelle
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EdiEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends EdiEmpfaenger> ov, EdiEmpfaenger oldValue, EdiEmpfaenger newValue) {
				log("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediEintragsSet.size() > 0) {
			mainCtr.setErrorText("Fehler beim Löschen der Integration " + aktIntegration.getName() +" wird verwendet");
			return;
		}	
		String integrationName1 = "Integration \"" + aktIntegration.getName() + "\"";
		String integrationName2 = integrationName1;
		if (aktIntegration.getName().equals(tfBezeichnung.getText()) == false) {
			integrationName2 = integrationName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(integrationName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktIntegration);
				entityManager.getTransaction().commit();
				aktIntegration = null;
				mainCtr.loadIntegrationListData();
				mainCtr.setInfoText("Die Integration \"" + integrationName1 +
									 "\" wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + integrationName1)
				    .showException(er);
			}
		}
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesWithMode(Checkmode checkmode) {
		String l = "checkForChangesWithMode-" + checkmode;
		log(l,"aktInte=" + (aktIntegration==null ? "null" : aktIntegration.getName()));
		if (aktIntegration == null ) {
			return true;
		}
		String orgName = aktIntegration.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktIntegration.getBeschreibung()==null ? "" : aktIntegration.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) ) {
			log(l, "Name und Bezeichnung unverändert");
		} else {
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}	
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die Änderungen der Integration " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) {
	    			return false;
	    		}
	    		if (response == Dialog.Actions.NO) {
	    			aktIntegration = null;
	    			return true;
	    		}
			}
			String msg = checkIntegrationName(newName);
			if (msg != null) {
				mainCtr.setErrorText(msg);
				tfBezeichnung.requestFocus();
				return false;
			}
			log(l,"Änderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktIntegration.setName(newName);
			aktIntegration.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiListeforIntegration(aktIntegration);
			mainCtr.setInfoText("Integration " + orgName + " wurde gespeichert");
		}
		return true;
	}
	
	private String checkIntegrationName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<Integration> tq = entityManager.createQuery(
				"SELECT i FROM Integration i WHERE LOWER(i.name) = LOWER(:n)",Integration.class);
		tq.setParameter("n", newName);
		List<Integration> integrationList = tq.getResultList();
		for (Integration i : integrationList ) {
			if (i.getId() != aktIntegration.getId() )  {
				if (i.getName().equalsIgnoreCase(newName)) {
					return "Eine andere Integration heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforIntegration( Integration selIntegration) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		ediEintragsSet.clear(); 
		/* 1. lese alle EdiEinträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.konfiguration.integration = :i", EdiEintrag.class);
		tqS.setParameter("i", selIntegration);
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			ediEintragsSet.add(e);
			if (e.getEdiEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getEdiEmpfaenger());
//				for(EdiEmpfaenger ee : e.getEdiEmpfaenger() ) ediKomponenteList.add(ee); 
			} else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				empfaengerList.add(tmpE);
			}
		}
		tvVerwendungen.setItems(empfaengerList);
//		log("readEdiListeforKomponente","size="+ ediEintragsSet.size());
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
    
	private static void log(String methode, String message) {
		if (message != null || methode != null) {
			String className = IntegrationController.class.getName().substring(16);
			System.out.println(className + "." + methode + "(): " + message); 
		}
	}

	void checkFieldsFromView() {
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'Integration.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'Integration.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'Integration.fxml'.";
    	assert tcKonfiguration != null : "fx:id=\"tcKonfiguration\" was not injected: check your FXML file 'Integration.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'Integration.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'Integration.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'Integration.fxml'.";
    }
    
}
