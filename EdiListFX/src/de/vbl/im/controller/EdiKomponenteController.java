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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;

import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Integration;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.EdiKomponente;
import de.vbl.im.model.Ansprechpartner;

public class EdiKomponenteController implements Initializable  {
	private static final Logger logger = LogManager.getLogger(EdiKomponenteController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiKomponente> edikomponente;
	private final ObservableSet<Integration> integrationSet;      // all assigned integrations
	private final ObservableList<Ansprechpartner> ansprechpartnerList; 
	private EdiKomponente aktKomponente = null;
	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private ListView<Ansprechpartner> lvAnsprechpartner;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    @FXML private Button btnRemoveAnsprechpartner;
    
    public EdiKomponenteController() {
    	edikomponente = new SimpleObjectProperty<>(this, "edikomponente", null);
    	integrationSet = FXCollections.observableSet();
    	ansprechpartnerList = FXCollections.observableArrayList();
    }

	public static void setParent(IMController managerController) {
		logger.entry();
		EdiKomponenteController.mainCtr = managerController;
		EdiKomponenteController.primaryStage = IMController.getStage();
		EdiKomponenteController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.entry();
		checkFieldsFromView();
		
		edikomponente.addListener(new ChangeListener<EdiKomponente>() {
			@Override
			public void changed(ObservableValue<? extends EdiKomponente> ov,
					EdiKomponente oldKomponente, EdiKomponente newKomponente) {
				logger.debug("ChangeListener<EdiKomponente>",
					((oldKomponente==null) ? "null" : oldKomponente.getFullname() + " -> " 
				  + ((newKomponente==null) ? "null" : newKomponente.getFullname() )));
				if (oldKomponente != null) {
					if (newKomponente == null) {
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
					}
					ansprechpartnerList.clear();
					integrationSet.clear();
				}
				if (newKomponente != null) {
					aktKomponente = newKomponente;
					readEdiListeforKomponete(newKomponente);
					tfBezeichnung.setText(newKomponente.getName());
					if (newKomponente.getBeschreibung() == null) {
						newKomponente.setBeschreibung("");
					}
					taBeschreibung.setText(newKomponente.getBeschreibung());
					ansprechpartnerList.addAll(newKomponente.getAnsprechpartner());
					btnRemoveAnsprechpartner.disableProperty().bind(lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(integrationSet))));
		

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktKomponente.getName().equals(newValue) == false) {
				msg = checkKomponentenName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktKomponente.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
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
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(Integration.FORMAT_EDINR, 
												cellData.getValue().getIntegration().ediNrProperty()));

		tcSender.setCellValueFactory(cellData -> cellData.getValue().getIntegration().getEdiKomponente().fullnameProperty());
		
		tcSender.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String senderFullname, boolean empty) {
					super.updateItem(senderFullname, empty);
					if (senderFullname == null || empty) 
						setText(null); 
					else {
						setText(senderFullname);
						if (senderFullname.equals(aktKomponente.getFullname()))
							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
					}
				}
			};
		});
		
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());

		tcEmpfaenger.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String empfaengerFullname, boolean empty) {
					super.updateItem(empfaengerFullname, empty);
					if (empfaengerFullname == null || empty) 
						setText(null); 
					else {
						setText(empfaengerFullname);
//						log("tcEmpfaengeItem","aktkombo:" + aktKomponente.getFullname() + 
//								" emfpaenger:" + empf);
						if (empfaengerFullname.equals(aktKomponente.getFullname()))
							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
					}
				}
			};
		});
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
		
		// todo: zum Absprung bei Select einer anderen Integration in der Sub-Tabelle
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EdiEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends EdiEmpfaenger> ov, EdiEmpfaenger oldValue, EdiEmpfaenger newValue) {
				logger.info("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (integrationSet.size() > 0) {
			mainCtr.setErrorText("Fehler beim löschen der Komponente " + aktKomponente.getFullname() +" wird verwendet");
			return;
		}	
		String kompoName1 = "Komponente \"" + aktKomponente.getName() + "\"";
		String kompoName2 = kompoName1;
		if (aktKomponente.getName().equals(tfBezeichnung.getText()) == false) {
			kompoName2 = kompoName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(kompoName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				aktKomponente.getEdiSystem().getEdiKomponente().remove(aktKomponente);
				entityManager.getTransaction().begin();
				entityManager.remove(aktKomponente);
				entityManager.getTransaction().commit();
				aktKomponente = null;
				mainCtr.loadKomponentenListData();
				mainCtr.setInfoText("Die " + kompoName1 + " wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + kompoName1)
				    .showException(er);
			}
		}
	}
	
	@FXML
	void speichern(ActionEvent event) {
		checkForChangesAndSave(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesAndSave(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesAndSave(Checkmode checkmode) {
		if (aktKomponente == null ) {
			logger.info("aktKomponente=NULL?");
			return true;
		}
		String orgName = aktKomponente.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktKomponente.getBeschreibung()==null ? "" : aktKomponente.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();

		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) && 
			aktKomponente.getAnsprechpartner().containsAll(ansprechpartnerList) &&
			ansprechpartnerList.containsAll(aktKomponente.getAnsprechpartner())
		) {
			return true;  // no changes -> nothing to do  
		}
		if (checkmode == Checkmode.ONLY_CHECK) {
			return false;
		}	
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			Action response = Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
					.message("Sollen die Änderungen an der Komponente " + orgName + " gespeichert werden ?")
					.showConfirm();
			if (response == Dialog.Actions.CANCEL) {
				return false;
			}
			if (response == Dialog.Actions.NO) {
				aktKomponente = null;
				return true;
			}
		}	
		String msg = checkKomponentenName(newName);
		if (msg != null) {
			mainCtr.setErrorText(msg);
			tfBezeichnung.requestFocus();
			return false;
		}
		logger.info("Update Komponente " + newName);
		try {
			entityManager.getTransaction().begin();
			aktKomponente.setName(newName);
			aktKomponente.setBeschreibung(newBeschreibung);
			boolean kontaktListChanged = aktKomponente.getAnsprechpartner().retainAll(ansprechpartnerList);
			for (Ansprechpartner k : ansprechpartnerList) {
				if (aktKomponente.getAnsprechpartner().contains(k)== false) {
					aktKomponente.getAnsprechpartner().add(k);
					kontaktListChanged = true;
				}
			}
			entityManager.getTransaction().commit();
			if (kontaktListChanged) {
				mainCtr.refreshKontaktReferences();
			}
			mainCtr.setInfoText("Komponente " + newName + " wurde gespeichert");
			dataIsChanged.set(false);
    	} catch (RuntimeException e) {
    		logger.error("Message:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage)
				.title(primaryStage.getTitle())
				.masthead("FEHLER")
				.message("Fehler beim Speichern der Komponentendaten:\n" + e.getMessage())
				.showException(e);
    	}
		readEdiListeforKomponete(aktKomponente);
		return true;
	}
	
	private String checkKomponentenName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k WHERE LOWER(k.name) = LOWER(:n)",EdiKomponente.class);
		tq.setParameter("n", newName);
		List<EdiKomponente> kompoList = tq.getResultList();
		for (EdiKomponente k : kompoList ) {
			if (k.getId() != aktKomponente.getId() &&
				k.getEdiSystem().getId() == aktKomponente.getEdiSystem().getId())  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return "Eine andere Komponente des Systems heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforKomponete( EdiKomponente selKomponente) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		/* 1. lese alle EdiEinträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.ediKomponente = :k", Integration.class);
		tqS.setParameter("k", selKomponente);
		List<Integration> ediList = tqS.getResultList();
		for(Integration e : ediList ) {
			integrationSet.add(e);
			if (e.getEdiEmpfaenger().size() > 0) {
				empfaengerList.addAll(e.getEdiEmpfaenger());
			} else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setIntegration(e);
				empfaengerList.add(tmpE);
			}
		}
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente = :k", EdiEmpfaenger.class);
		tqE.setParameter("k", selKomponente);
//		ediKomponenteList.addAll(tqE.getResultList());
		for(EdiEmpfaenger e : tqE.getResultList() ) {
			logger.debug("readEdiListeforKomponete", "Empfaenger:" + e.getKomponente().getFullname() + " add");
			empfaengerList.add(e);
			integrationSet.add(e.getIntegration());
		}
		
		tvVerwendungen.setItems(empfaengerList);
//		log("readEdiListeforKomponente","size="+ integrationSet.size());
	}

    @FXML
    void actionAddAnsprechpartner(ActionEvent event) {
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
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    				userInfo = "Der ausgewählte Kontakt wurde ergänzt";
    			}
    		}
    		mainCtr.setInfoText(userInfo);
    	}
    }

    @FXML
    void actionRemoveAnsprechpartner(ActionEvent event) {
    	Ansprechpartner toBeRemoved = lvAnsprechpartner.getSelectionModel().getSelectedItem();
    	logger.info("remove Kontakt " + toBeRemoved.getNachname());
    	ansprechpartnerList.remove(toBeRemoved);
    	mainCtr.setInfoText("Der Ansprechpartner \"" + toBeRemoved.getVorname() + " " + 
    					toBeRemoved.getNachname() + "\" wurde aus dieser Liste entfernt");
		dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    }
    
	public final ObjectProperty<EdiKomponente> komponenteProperty() {
		return edikomponente;
	}
	
	public final EdiKomponente getKomponente() {
		return edikomponente.get() ;
	}
	
	public final void setKomponente(EdiKomponente komponente) {
		this.edikomponente.set(komponente);
	}
    
	void checkFieldsFromView() {
//    	assert ediKomponentePane != null : "fx:id=\"ediKomponente\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert btnRemoveAnsprechpartner != null : "fx:id=\"btnRemoveAnsprechpartner\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
	}

}
