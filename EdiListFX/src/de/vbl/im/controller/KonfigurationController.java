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
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.Konfiguration;

/*
 * Konfiguration: 
 * 
 *  - gehört zu genau einer InSzenario und 
 *  - besteht aus einer oder mehreren Integrationen
 * 
 */


public class KonfigurationController {
	private static final Logger logger = LogManager.getLogger(KonfigurationController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<Konfiguration> konfiguration;
	private final ObservableSet<Integration> integrationSet;      // all integrations from this IS
	private Konfiguration aktKonfiguration = null;
	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<InEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<InEmpfaenger, String> tcInNr;
    @FXML private TableColumn<InEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<InEmpfaenger, String> tcSender;
    @FXML private TableColumn<InEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public KonfigurationController() {
    	this.konfiguration = new SimpleObjectProperty<>(this, "konfiguration", null);
    	this.integrationSet = FXCollections.observableSet();
    }

	public void setParent(IMController managerController) {
		logger.entry();
		KonfigurationController.mainCtr = managerController;
		KonfigurationController.primaryStage = IMController.getStage();
		KonfigurationController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		konfiguration.addListener(new ChangeListener<Konfiguration>() {
			@Override
			public void changed(ObservableValue<? extends Konfiguration> ov,
					Konfiguration oldKonfiguration, Konfiguration newKonfiguration) {
				if (oldKonfiguration != null && newKonfiguration == null) {
					integrationSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
				}
				if (newKonfiguration != null) {
					aktKonfiguration = newKonfiguration;
					readTablesForKonfiguration(newKonfiguration);
					tfBezeichnung.setText(newKonfiguration.getName());
					if (newKonfiguration.getBeschreibung() == null) {
						newKonfiguration.setBeschreibung("");
					}
					taBeschreibung.setText(newKonfiguration.getBeschreibung());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(integrationSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktKonfiguration.getName().equals(newValue) == false) {
				msg = checkKonfigurationName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktKonfiguration.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
//	    Setup for Sub-Panel    
		
		tcInNr.setCellValueFactory(cellData -> cellData.getValue().getIntegration().inNrStrExp());
		tcSender.setCellValueFactory(cellData -> cellData.getValue().getIntegration().getInKomponente().fullnameProperty());
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
		
		// TODO: for direct jump to another integration from this table
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<InEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends InEmpfaenger> ov, InEmpfaenger oldValue, InEmpfaenger newValue) {
				logger.info("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (integrationSet.size() > 0) {
			mainCtr.setErrorText("Fehler beim Löschen der InSzenario " + aktKonfiguration.getName() +" wird verwendet");
			return;
		}	
		String inSzenarioName1 = "InSzenario \"" + aktKonfiguration.getName() + "\"";
		String inSzenarioName2 = inSzenarioName1;
		if (aktKonfiguration.getName().equals(tfBezeichnung.getText()) == false) {
			inSzenarioName2 = inSzenarioName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(inSzenarioName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktKonfiguration);
				entityManager.getTransaction().commit();
				aktKonfiguration = null;
				mainCtr.loadKonfigurationListData();
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
	void speichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesWithMode(Checkmode checkmode) {
		String mn = "checkForChangesWithMode-" + checkmode;
		logger.debug(mn,"aktInte=" + (aktKonfiguration==null ? "null" : aktKonfiguration.getName()));
		if (aktKonfiguration == null ) {
			return true;
		}
		String orgName = aktKonfiguration.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktKonfiguration.getBeschreibung()==null ? "" : aktKonfiguration.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();

		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) )  {
			logger.trace(mn, "Name und Bezeichnung unverändert");
		} else {
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die Änderungen der Konfiguration " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) {
	    			return false;
	    		}
	    		if (response == Dialog.Actions.NO) {
	    			aktKonfiguration = null;
	    			return true;
	    		}
			}
			String msg = checkKonfigurationName(newName);
			if (msg != null) {
				mainCtr.setErrorText(msg);
				tfBezeichnung.requestFocus();
				return false;
			}
			logger.trace(mn,"Änderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktKonfiguration.setName(newName);
			aktKonfiguration.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readTablesForKonfiguration(aktKonfiguration);
			mainCtr.setInfoText("Konfiguration " + newName + " wurde gespeichert");
		}
		return true;
	}
	
	private String checkKonfigurationName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<Konfiguration> tq = entityManager.createQuery(
				"SELECT k FROM Konfiguration k WHERE LOWER(k.name) = LOWER(:n)",Konfiguration.class);
		tq.setParameter("n", newName);
		List<Konfiguration> konfigurationList = tq.getResultList();
		for (Konfiguration k : konfigurationList ) {
			System.out.println("K.name=" + k.getName());
			if (k != aktKonfiguration)  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return "Eine andere Konfiguration heißt bereits so";
				}
			}
		}
		return null;
	}

	private void readTablesForKonfiguration( Konfiguration selKonfiguration) {
		tvVerwendungen.getItems().clear();
		ObservableList<InEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		/* 1. lese alle EdiEinträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT k FROM Integration k WHERE k.konfiguration = :k", Integration.class);
		tqS.setParameter("k", selKonfiguration);
		List<Integration> resultList = tqS.getResultList();
		for(Integration e : resultList ) {
			integrationSet.add(e);
			if (e.getInEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getInEmpfaenger());
			} else {
				InEmpfaenger tmpE = new InEmpfaenger();
				tmpE.setIntegration(e);
				empfaengerList.add(tmpE);
			}
		}
		tvVerwendungen.setItems(empfaengerList);
//		logger.info("size="+ integrationSet.size());
	}

	public final ObjectProperty<Konfiguration> konfigurationProperty() {
		return konfiguration;
	}
	
	public final Konfiguration getKonfiguration() {
		return konfiguration.get() ;
	}
	
	public final void setKonfiguration(Konfiguration konfiguration) {
		this.konfiguration.set(konfiguration);
	}
    
	void checkFieldsFromView() {
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'Konfiguration.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'Konfiguration.fxml'.";
    	assert tcInNr != null : "fx:id=\"tcInNr\" was not injected: check your FXML file 'Konfiguration.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'InSzenario.fxml'.";
    }
    
}
