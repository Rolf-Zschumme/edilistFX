package de.vbl.im.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.InPartner;
import de.vbl.im.model.Ansprechpartner;

public class InPartnerController {
	private static final Logger logger = LogManager.getLogger(InPartnerController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<InPartner> inPartner;
	private final ObservableSet<Integration> integrationSet;      // all assigned integrations
	private final ObservableList<Ansprechpartner> ansprechpartnerList; 
	private final IntegerProperty inSystemAnzahl; 
	
	private InPartner aktPartner = null;
	
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
    
    public InPartnerController() {
    	this.inPartner = new SimpleObjectProperty<>(this, "inPartner", null);
    	this.integrationSet = FXCollections.observableSet();
    	this.inSystemAnzahl = new SimpleIntegerProperty(0);
    	this.ansprechpartnerList = FXCollections.observableArrayList();
    }

	public static void setParent(IMController managerController) {
		logger.entry(primaryStage);
		InPartnerController.mainCtr = managerController;
		InPartnerController.primaryStage = IMController.getStage();
		InPartnerController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		logger.entry();
		checkFieldsFromView();
		
		inPartner.addListener(new ChangeListener<InPartner>() {
			@Override
			public void changed(ObservableValue<? extends InPartner> ov,
					InPartner oldPartner, InPartner newPartner) {
				if (oldPartner != null) {
					if (newPartner == null) {
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
					}
					inSystemAnzahl.unbind();
					ansprechpartnerList.clear();
					integrationSet.clear();
				}
				if (newPartner != null) {
					aktPartner = newPartner;
					readTablesforPartner(newPartner);
					tfBezeichnung.setText(newPartner.getName());
					if (newPartner.getBeschreibung() == null) {
						newPartner.setBeschreibung("");
					}
					taBeschreibung.setText(newPartner.getBeschreibung());
					inSystemAnzahl.bind(aktPartner.anzSystemeProperty());
					ansprechpartnerList.addAll(newPartner.getAnsprechpartner());
					btnRemoveAnsprechpartner.disableProperty().bind(lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.lessThan(0, inSystemAnzahl));
//		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(integrationSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String userMsg = "";
			if (aktPartner.getName().equals(newValue) == false) {
				userMsg = checkPartnerName(newValue);
			}
			dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			mainCtr.setErrorText(userMsg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			String userMsg = "";
			if (newValue.equals(aktPartner.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(userMsg);
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
				protected void updateItem (String senderName, boolean empty) {
					super.updateItem(senderName, empty);
					if (senderName == null || empty) 
						setText(null); 
					else {
						setText(senderName);
						FontWeight fw = senderName.startsWith(aktPartner.getName()) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().getKomponente().fullnameProperty());

		tcEmpfaenger.setCellFactory(column -> {
			return new TableCell<InEmpfaenger, String>() {
				@Override
				protected void updateItem (String empfaengerName, boolean empty) {
					super.updateItem(empfaengerName, empty);
					if (empfaengerName == null || empty) 
						setText(null); 
					else {
						setText(empfaengerName);
						FontWeight fw = empfaengerName.startsWith(aktPartner.getName()) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
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
				logger.info("noch nicht impelmentiert: Absprung zu " + newValue.getIntegration().getInNrStr());
			}
		});
		logger.exit();
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (integrationSet.size() > 0) {
			String msg = "Fehler beim Löschen des Partners \"" + aktPartner.getName() +"\" da er verwendet wird";
			mainCtr.setErrorText(msg);
			logger.warn(msg);
			return; 
		}	
		String partnerName1 = "Partner \"" + aktPartner.getName() + "\"";
		String partnerName2 = partnerName1;
		if (aktPartner.getName().equals(tfBezeichnung.getText()) == false) {
			partnerName2 = partnerName1 + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(partnerName2 + " wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktPartner);
				entityManager.getTransaction().commit();
				aktPartner = null;
				mainCtr.loadPartnerListData();
				mainCtr.setInfoText("Der " + partnerName1 + " wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				String msg = "Fehler beim Löschen der Partners " + partnerName1;
				logger.fatal(msg, er);
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message(msg)
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
		if (aktPartner == null ) {
			logger.info("aktPartner=NULL?");
			return true;
		}
		String orgName = aktPartner.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktPartner.getBeschreibung()==null ? "" : aktPartner.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();

		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) && 
			aktPartner.getAnsprechpartner().containsAll(ansprechpartnerList) &&
			ansprechpartnerList.containsAll(aktPartner.getAnsprechpartner())    ) 
		{
			return true;  // no changes -> nothing to do
		}
		if (checkmode == Checkmode.ONLY_CHECK) {
			return false;
		}
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			Action response = Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
					.message("Sollen die Änderungen am Partner " + orgName + " gespeichert werden ?")
					.showConfirm();
			if (response == Dialog.Actions.CANCEL) {
				return false;
			}
			if (response == Dialog.Actions.NO) {
				aktPartner = null;
				return true;
			}
		}	
		String msg = checkPartnerName(newName);
		if (msg != null) {
			mainCtr.setErrorText(msg);
			tfBezeichnung.requestFocus();
			return false;
		}
		logger.info("Update Partner " + newName);
		try {
			entityManager.getTransaction().begin();
			aktPartner.setName(newName);
			aktPartner.setBeschreibung(newBeschreibung);
			boolean kontaktListChanged = aktPartner.getAnsprechpartner().retainAll(ansprechpartnerList);
			for (Ansprechpartner k : ansprechpartnerList) {
				if (aktPartner.getAnsprechpartner().contains(k)== false) {
					aktPartner.getAnsprechpartner().add(k);
					kontaktListChanged = true;
				}
			}
			entityManager.getTransaction().commit();
			if (kontaktListChanged) {
				mainCtr.refreshKontaktReferences();
			}
			mainCtr.setInfoText("Der Partner \"" + aktPartner.getName() + "\" wurde gespeichert");
			dataIsChanged.set(false);
    	} catch (RuntimeException e) {
    		logger.error("Message:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage)
				.title(primaryStage.getTitle())
				.masthead("FEHLER")
				.message("Fehler beim Speichern der Partnerdaten:\n" + e.getMessage())
				.showException(e);
    	}
		readTablesforPartner(aktPartner);
		return true;
	}
	
	private String checkPartnerName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<InPartner> tq = entityManager.createQuery(
				"SELECT p FROM InPartner p WHERE LOWER(p.name) = LOWER(:n)",InPartner.class);
		tq.setParameter("n", newName);
		List<InPartner> partnerList = tq.getResultList();
		for (InPartner p : partnerList ) {
			if (p.getId() != aktPartner.getId()) {
				if (p.getName().equalsIgnoreCase(newName)) {
					return "Ein anderer Partner heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readTablesforPartner( InPartner newPartner) {
		tvVerwendungen.getItems().clear();
		ObservableList<InEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		integrationSet.clear(); 
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.inKomponente.inSystem.inPartner = :p", Integration.class);
		tqS.setParameter("p", newPartner);
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
				"SELECT e FROM InEmpfaenger e WHERE e.komponente.inSystem.inPartner = :p", InEmpfaenger.class);
		tqE.setParameter("p", newPartner);
		for(InEmpfaenger e : tqE.getResultList() ) {
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
	
	public final ObjectProperty<InPartner> inPartnerProperty() {
		return inPartner;
	}
	
	public final InPartner getInPartner() {
		return inPartner.get() ;
	}
	
	public final void setInPartner(InPartner inPartner) {
		this.inPartner.set(inPartner);
	}
    
	final static String fxmlFilename = "InPartner.fxml";
	final static String fxmlErrortxt = "' was not injected: check FXML-file '" + fxmlFilename + "'.";
	void checkFieldsFromView() {
    	assert tfBezeichnung 			!= null : "fx:id='tfBezeichnung"   			+ fxmlErrortxt;
    	assert taBeschreibung			!= null : "fx:id='taBeschreibung"  			+ fxmlErrortxt;
    	assert tcInNr					!= null : "fx:id='tcInNr"  				+ fxmlErrortxt;
    	assert tcSender					!= null : "fx:id='tcSender"  				+ fxmlErrortxt;
        assert tcEmpfaenger				!= null : "fx:id='tcEmpfaenger"  			+ fxmlErrortxt;
        assert tcGeschaeftsobjekt		!= null : "fx:id='tcGeschaeftsobjekt"  		+ fxmlErrortxt;
        assert tcDatumAb				!= null : "fx:id='tcDatumAb"  				+ fxmlErrortxt;
        assert tcDatumBis				!= null : "fx:id='tcDatumBis"  				+ fxmlErrortxt;
        assert tvVerwendungen			!= null : "fx:id='tvVerwendungen"  			+ fxmlErrortxt;
        assert lvAnsprechpartner		!= null : "fx:id='lvAnsprechpartner"		+ fxmlErrortxt;
        assert btnLoeschen				!= null : "fx:id='btnLoeschen"  			+ fxmlErrortxt;
        assert btnSpeichern 			!= null : "fx:id='btnSpeichern"  			+ fxmlErrortxt;
        assert btnRemoveAnsprechpartner	!= null : "fx:id='btnRemoveAnsprechpartner"	+ fxmlErrortxt;
    }
}
