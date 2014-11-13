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

import de.vbl.im.model.Integration;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.Iszenario;

public class IszenarioController {
	private static final Logger logger = LogManager.getLogger(IszenarioController.class.getName()); 
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<Iszenario> iszenario;
	private final ObservableSet<Integration> integrationSet;      // all assigned EDI-Entities
	private Iszenario aktIszenario = null;

	
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
    
    public IszenarioController() {
    	this.iszenario = new SimpleObjectProperty<>(this, "iszenario", null);
    	this.integrationSet = FXCollections.observableSet();
    }

	public static void setParent(IMController managerController) {
		logger.entry();
		IszenarioController.mainCtr = managerController;
		IszenarioController.primaryStage = IMController.getStage();
		IszenarioController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		iszenario.addListener(new ChangeListener<Iszenario>() {
			@Override
			public void changed(ObservableValue<? extends Iszenario> ov,
					Iszenario oldIszenario, Iszenario newIszenario) {
				log("ChangeListener<EdiKomponente>",
					((oldIszenario==null) ? "null" : oldIszenario.getName() + " -> " 
				  + ((newIszenario==null) ? "null" : newIszenario.getName() )));
				if (oldIszenario != null && newIszenario == null) {
					integrationSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
				}
				if (newIszenario != null) {
					aktIszenario = newIszenario;
					readEdiListeforIszenario(newIszenario);
					tfBezeichnung.setText(newIszenario.getName());
					if (newIszenario.getBeschreibung() == null) {
						newIszenario.setBeschreibung("");
					}
					taBeschreibung.setText(newIszenario.getBeschreibung());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(integrationSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktIszenario.getName().equals(newValue) == false) {
				msg = checkIszenarioName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktIszenario.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
//	    Setup for Sub-Panel    
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(Integration.FORMAT_EDINR, 
												cellData.getValue().getIntegration().ediNrProperty()));


		tcKonfiguration.setCellValueFactory(cellData -> cellData.getValue().getIntegration().konfigurationNameProperty());
		
		tcSender.setCellValueFactory(cellData -> cellData.getValue().getIntegration().getEdiKomponente().fullnameProperty());
		
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
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
		
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
		if (integrationSet.size() > 0) {
			mainCtr.setErrorText("Fehler beim Löschen der Iszenario " + aktIszenario.getName() +" wird verwendet");
			return;
		}	
		String iszenarioName1 = "Iszenario \"" + aktIszenario.getName() + "\"";
		String iszenarioName2 = iszenarioName1;
		if (aktIszenario.getName().equals(tfBezeichnung.getText()) == false) {
			iszenarioName2 = iszenarioName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(iszenarioName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktIszenario);
				entityManager.getTransaction().commit();
				aktIszenario = null;
				mainCtr.loadIszenarioListData();
				mainCtr.setInfoText("Die Iszenario \"" + iszenarioName1 +
									 "\" wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + iszenarioName1)
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
		log(l,"aktInte=" + (aktIszenario==null ? "null" : aktIszenario.getName()));
		if (aktIszenario == null ) {
			return true;
		}
		String orgName = aktIszenario.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktIszenario.getBeschreibung()==null ? "" : aktIszenario.getBeschreibung();
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
    				.message("Sollen die Änderungen der Iszenario " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) {
	    			return false;
	    		}
	    		if (response == Dialog.Actions.NO) {
	    			aktIszenario = null;
	    			return true;
	    		}
			}
			String msg = checkIszenarioName(newName);
			if (msg != null) {
				mainCtr.setErrorText(msg);
				tfBezeichnung.requestFocus();
				return false;
			}
			log(l,"Änderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktIszenario.setName(newName);
			aktIszenario.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiListeforIszenario(aktIszenario);
			mainCtr.setInfoText("Iszenario " + orgName + " wurde gespeichert");
		}
		return true;
	}
	
	private String checkIszenarioName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<Iszenario> tq = entityManager.createQuery(
				"SELECT i FROM Iszenario i WHERE LOWER(i.name) = LOWER(:n)",Iszenario.class);
		tq.setParameter("n", newName);
		List<Iszenario> iszenarioList = tq.getResultList();
		for (Iszenario i : iszenarioList ) {
			if (i.getId() != aktIszenario.getId() )  {
				if (i.getName().equalsIgnoreCase(newName)) {
					return "Ein anderes Integrationsszenario heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforIszenario( Iszenario selIszenario) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		/* 1. lese alle EdiEinträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.konfiguration.iszenario = :i", Integration.class);
		tqS.setParameter("i", selIszenario);
		List<Integration> ediList = tqS.getResultList();
		for(Integration e : ediList ) {
			integrationSet.add(e);
			if (e.getEdiEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getEdiEmpfaenger());
//				for(EdiEmpfaenger ee : e.getEdiEmpfaenger() ) ediKomponenteList.add(ee); 
			} else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setIntegration(e);
				empfaengerList.add(tmpE);
			}
		}
		tvVerwendungen.setItems(empfaengerList);
//		log("readEdiListeforKomponente","size="+ integrationSet.size());
	}

	public final ObjectProperty<Iszenario> iszenarioProperty() {
		return iszenario;
	}
	
	public final Iszenario getIszenario() {
		return iszenario.get() ;
	}
	
	public final void setIszenario(Iszenario iszenario) {
		this.iszenario.set(iszenario);
	}
    
	private static void log(String methode, String message) {
		if (message != null || methode != null) {
			String className = IszenarioController.class.getName().substring(16);
			System.out.println(className + "." + methode + "(): " + message); 
		}
	}

	void checkFieldsFromView() {
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'Iszenario.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'Iszenario.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'Iszenario.fxml'.";
    	assert tcKonfiguration != null : "fx:id=\"tcKonfiguration\" was not injected: check your FXML file 'Iszenario.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'Iszenario.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'Iszenario.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'Iszenario.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'Iszenario.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'Iszenario.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'Iszenario.fxml'.";
    }
    
}
