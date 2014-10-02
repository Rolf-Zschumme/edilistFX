package de.vbl.ediliste.controller;

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

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.Konfiguration;

public class KonfigurationController {
	private static final Logger logger = LogManager.getLogger(KonfigurationController.class.getName());
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<Konfiguration> konfiguration;
	private final ObservableSet<EdiEintrag> ediEintragsSet;      // all assigned EDI-Entities
	private Konfiguration aktKonfiguration = null;
	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    
    public KonfigurationController() {
    	this.konfiguration = new SimpleObjectProperty<>(this, "konfiguration", null);
    	this.ediEintragsSet = FXCollections.observableSet();
    }

	public static void start(Stage 			   primaryStage, 
							 EdiMainController mainController, 
							 EntityManager     entityManager) {
		logger.entry();
		KonfigurationController.primaryStage = primaryStage;
		KonfigurationController.mainCtr = mainController;
		KonfigurationController.entityManager = entityManager;
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		konfiguration.addListener(new ChangeListener<Konfiguration>() {
			@Override
			public void changed(ObservableValue<? extends Konfiguration> ov,
					Konfiguration oldKonfiguration, Konfiguration newKonfiguration) {
				if (oldKonfiguration != null && newKonfiguration == null) {
					ediEintragsSet.clear();
					tfBezeichnung.setText("");
					taBeschreibung.setText("");
				}
				if (newKonfiguration != null) {
					aktKonfiguration = newKonfiguration;
					readEdiListeforKonfiguration(newKonfiguration);
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
		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(ediEintragsSet))));

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
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(EdiEintrag.FORMAT_EDINR, 
												cellData.getValue().getEdiEintrag().ediNrProperty()));

		tcSender.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().getEdiKomponente().fullnameProperty());
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
		
		// todo: zum Absprung bei Select eines Edi-Eintrages in der Sub-Tabelle
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EdiEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends EdiEmpfaenger> ov, EdiEmpfaenger oldValue, EdiEmpfaenger newValue) {
				logger.info("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediEintragsSet.size() > 0) {
			mainCtr.setErrorText("Fehler beim Löschen der Integration " + aktKonfiguration.getName() +" wird verwendet");
			return;
		}	
		String integrationName1 = "Integration \"" + aktKonfiguration.getName() + "\"";
		String integrationName2 = integrationName1;
		if (aktKonfiguration.getName().equals(tfBezeichnung.getText()) == false) {
			integrationName2 = integrationName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(integrationName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktKonfiguration);
				entityManager.getTransaction().commit();
				aktKonfiguration = null;
				mainCtr.loadKonfigurationListData();
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
			readEdiListeforKonfiguration(aktKonfiguration);
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
			if (k != aktKonfiguration &&
				k.getIntegration() == aktKonfiguration.getIntegration())  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return "Eine andere Konfiguration der Integration " + 
							aktKonfiguration.getIntegration().getName() + " heißt bereits so";
				}
			}
		}
		return null;
	}

	private void readEdiListeforKonfiguration( Konfiguration selKonfiguration) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		ediEintragsSet.clear(); 
		/* 1. lese alle EdiEinträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT k FROM EdiEintrag k WHERE k.konfiguration = :k", EdiEintrag.class);
		tqS.setParameter("k", selKonfiguration);
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			ediEintragsSet.add(e);
			if (e.getEdiEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getEdiEmpfaenger());
			} else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				empfaengerList.add(tmpE);
			}
		}
		tvVerwendungen.setItems(empfaengerList);
//		log("readEdiListeforKomponente","size="+ ediEintragsSet.size());
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
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'Konfiguration.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'Konfiguration.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'Integration.fxml'.";
    }
    
}
