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
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.Ansprechpartner;

public class InKomponenteController implements Initializable  {
	private static final Logger logger = LogManager.getLogger(InKomponenteController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<InKomponente> inkomponente;
	private final ObservableSet<Integration> integrationSet;      // all assigned integrations
	private final ObservableList<Ansprechpartner> ansprechpartnerList; 
	private InKomponente aktKomponente = null;
	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private ListView<Ansprechpartner> lvAnsprechpartner;
    @FXML private TableView<InEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<InEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<InEmpfaenger, String> tcInNr;
    @FXML private TableColumn<InEmpfaenger, String> tcSender;
    @FXML private TableColumn<InEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<InEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    @FXML private Button btnRemoveAnsprechpartner;
    
    public InKomponenteController() {
    	inkomponente = new SimpleObjectProperty<>(this, "inkomponente", null);
    	integrationSet = FXCollections.observableSet();
    	ansprechpartnerList = FXCollections.observableArrayList();
    }

	public static void setParent(IMController managerController) {
		logger.entry();
		InKomponenteController.mainCtr = managerController;
		InKomponenteController.primaryStage = IMController.getStage();
		InKomponenteController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.entry();
		checkFieldsFromView();
		
		inkomponente.addListener(new ChangeListener<InKomponente>() {
			@Override
			public void changed(ObservableValue<? extends InKomponente> ov,
					InKomponente oldKomponente, InKomponente newKomponente) {
				logger.debug("ChangeListener<InKomponente>",
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
					readTablesForKomponete(newKomponente);
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
		
		tcInNr.setCellValueFactory(cellData -> Bindings.format(Integration.FORMAT_INNR, 
												cellData.getValue().getIntegration().inNrProperty()));

		tcSender.setCellValueFactory(cellData -> cellData.getValue().getIntegration().getInKomponente().fullnameProperty());
		
		tcSender.setCellFactory(column -> {
			return new TableCell<InEmpfaenger, String>() {
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
			return new TableCell<InEmpfaenger, String>() {
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
				aktKomponente.getInSystem().getInKomponente().remove(aktKomponente);
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
		readTablesForKomponete(aktKomponente);
		return true;
	}
	
	private String checkKomponentenName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<InKomponente> tq = entityManager.createQuery(
				"SELECT k FROM InKomponente k WHERE LOWER(k.name) = LOWER(:n)",InKomponente.class);
		tq.setParameter("n", newName);
		List<InKomponente> kompoList = tq.getResultList();
		for (InKomponente k : kompoList ) {
			if (k.getId() != aktKomponente.getId() &&
				k.getInSystem().getId() == aktKomponente.getInSystem().getId())  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return "Eine andere Komponente des Systems heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readTablesForKomponete( InKomponente selKomponente) {
		tvVerwendungen.getItems().clear();
		ObservableList<InEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		/* 1. lese alle Einträge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehörigen Empfänger, falls kein Empfänger vorhanden dummy erzeugen
		*/
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.inKomponente = :k", Integration.class);
		tqS.setParameter("k", selKomponente);
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
		TypedQuery<InEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM InEmpfaenger e WHERE e.komponente = :k", InEmpfaenger.class);
		tqE.setParameter("k", selKomponente);
//		inKomponenteList.addAll(tqE.getResultList());
		for(InEmpfaenger e : tqE.getResultList() ) {
			logger.debug("Empfaenger:" + e.getKomponente().getFullname() + " add");
			empfaengerList.add(e);
			integrationSet.add(e.getIntegration());
		}
		
		tvVerwendungen.setItems(empfaengerList);
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
    
	public final ObjectProperty<InKomponente> komponenteProperty() {
		return inkomponente;
	}
	
	public final InKomponente getKomponente() {
		return inkomponente.get() ;
	}
	
	public final void setKomponente(InKomponente komponente) {
		this.inkomponente.set(komponente);
	}
    
	void checkFieldsFromView() {
//    	assert inKomponentePane != null : "fx:id=\"inKomponente\" was not injected: check your FXML file 'InKomponente.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'InKomponente.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'InKomponente.fxml'.";
    	assert tcInNr != null : "fx:id=\"tcInNr\" was not injected: check your FXML file 'InKomponente.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'InKomponente.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'InKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'InKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'InKomponente.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'InKomponente.fxml'.";
        assert btnSpeichern != null : "fx:id=\"btnSpeichern\" was not injected: check your FXML file 'InKomponente.fxml'.";
        assert btnRemoveAnsprechpartner != null : "fx:id=\"btnRemoveAnsprechpartner\" was not injected: check your FXML file 'InKomponente.fxml'.";
	}

}
