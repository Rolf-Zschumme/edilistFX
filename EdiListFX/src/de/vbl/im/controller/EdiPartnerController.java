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

import de.vbl.im.controller.subs.KontaktPersonAuswaehlenController;
import de.vbl.im.model.EdiEintrag;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.EdiPartner;
import de.vbl.im.model.KontaktPerson;

public class EdiPartnerController {
	private static final Logger logger = LogManager.getLogger(EdiPartnerController.class.getName());
	private static Stage primaryStage = null;
	private static IntegrationManagerController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiPartner> ediPartner;
	private final ObservableSet<EdiEintrag> ediEintragsSet;      // all assigned EDI-Entities
	private final ObservableList<KontaktPerson> kontaktpersonList; 
	private final IntegerProperty ediSystemAnzahl; 
	
	private EdiPartner aktPartner = null;
	
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private ListView<KontaktPerson> lvAnsprechpartner;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnSpeichern;
    @FXML private Button btnLoeschen;
    @FXML private Button btnRemoveKontaktPerson;
    
    public EdiPartnerController() {
    	this.ediPartner = new SimpleObjectProperty<>(this, "ediPartner", null);
    	this.ediEintragsSet = FXCollections.observableSet();
    	this.ediSystemAnzahl = new SimpleIntegerProperty(0);
    	this.kontaktpersonList = FXCollections.observableArrayList();
    }

	public static void setParent(IntegrationManagerController managerController) {
		logger.entry(primaryStage);
		EdiPartnerController.mainCtr = managerController;
		EdiPartnerController.primaryStage = IntegrationManagerController.getStage();
		EdiPartnerController.entityManager = managerController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		logger.entry();
		checkFieldsFromView();
		
		ediPartner.addListener(new ChangeListener<EdiPartner>() {
			@Override
			public void changed(ObservableValue<? extends EdiPartner> ov,
					EdiPartner oldPartner, EdiPartner newPartner) {
				log("ChangeListener<EdiPartner>",
					((oldPartner==null) ? "null" : oldPartner.getName() + " -> " 
				  + ((newPartner==null) ? "null" : newPartner.getName() )));
				if (oldPartner != null) {
					if (newPartner == null) {
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
					}
					ediSystemAnzahl.unbind();
					kontaktpersonList.clear();
					ediEintragsSet.clear();
				}
				if (newPartner != null) {
					aktPartner = newPartner;
					readEdiListeforPartner(newPartner);
					tfBezeichnung.setText(newPartner.getName());
					if (newPartner.getBeschreibung() == null) {
						newPartner.setBeschreibung("");
					}
					taBeschreibung.setText(newPartner.getBeschreibung());
					ediSystemAnzahl.bind(aktPartner.anzSystemeProperty());
					kontaktpersonList.addAll(newPartner.getKontaktPerson());
					btnRemoveKontaktPerson.disableProperty().bind(lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));
		btnLoeschen.disableProperty().bind(Bindings.lessThan(0, ediSystemAnzahl));
//		btnLoeschen.disableProperty().bind(Bindings.not(Bindings.greaterThanOrEqual(0, Bindings.size(ediEintragsSet))));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			String msg = "";
			if (aktPartner.getName().equals(newValue) == false) {
				msg = checkPartnerName(newValue);
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
			mainCtr.setErrorText(msg);
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktPartner.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesWithMode(Checkmode.ONLY_CHECK));
			}
		});
		
		lvAnsprechpartner.setItems(kontaktpersonList);
		lvAnsprechpartner.setCellFactory( (list) -> {
			return new ListCell<KontaktPerson>() {
				@Override
				protected void updateItem(KontaktPerson k, boolean empty) {
					super.updateItem(k, empty);
					if (k == null || empty) {
						setText(null);
					} else {
						String suffix = k.getAbteilungSafe();
						if(suffix.equals("")  && k.getNummer() != null) {
							suffix = k.getNummer();
						}
						if (suffix.equals("") ) {
							setText(k.getVorname() + " " + k.getNachname());
						} else {
							setText(k.getVorname() + " " + k.getNachname() + " (" + suffix + ")");
						}
					}
				}
			};
		});
		
//	    Setup for Sub-Panel    
		
		tcEdiNr.setCellValueFactory(cellData -> Bindings.format(EdiEintrag.FORMAT_EDINR, 
												cellData.getValue().getEdiEintrag().ediNrProperty()));

		tcSender.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().getEdiKomponente().fullnameProperty());
		
		tcSender.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
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
			return new TableCell<EdiEmpfaenger, String>() {
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
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
		
		// todo: zum Absprung bei Select eines Edi-Eintrages in der Sub-Tabelle
		tvVerwendungen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EdiEmpfaenger>() {
			@Override
			public void changed (ObservableValue<? extends EdiEmpfaenger> ov, EdiEmpfaenger oldValue, EdiEmpfaenger newValue) {
				log("tvVerwendungen.select.changed" ,"newValue" + newValue);
			}
		});
		logger.exit();
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediEintragsSet.size() > 0) {
			String msg = "Fehler beim Lˆschen des Partners \"" + aktPartner.getName() +"\" da er verwendet wird";
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
				.message(partnerName2 + " wirklich lˆschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktPartner);
				entityManager.getTransaction().commit();
				aktPartner = null;
				mainCtr.loadPartnerListData();
				mainCtr.setInfoText("Der " + partnerName1 + " wurde erfolgreich gelˆscht !");
			} catch (RuntimeException er) {
				String msg = "Fehler beim Lˆschen der Partners " + partnerName1;
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
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private static enum Checkmode { ONLY_CHECK, ASK_FOR_UPDATE, SAVE_DONT_ASK };
	
	private boolean checkForChangesWithMode(Checkmode checkmode) {
		log("checkForChangesWithMode","aktPartner=" + (aktPartner==null ? "null" : aktPartner.getName()));
		if (aktPartner == null ) {
			return true;
		}
		String orgName = aktPartner.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktPartner.getBeschreibung()==null ? "" : aktPartner.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();

		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) && 
			aktPartner.getKontaktPerson().containsAll(kontaktpersonList) &&
			kontaktpersonList.containsAll(aktPartner.getKontaktPerson())    ) 
		{
			log("checkForChangesWithMode", "Name und Bezeichnung sind unver‰ndert");
		} else {	
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die ƒnderungen am Partner " + orgName + " gespeichert werden ?")
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
			log("checkForChangesWithMode","ƒnderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktPartner.setName(newName);
			aktPartner.setBeschreibung(newBeschreibung);
			aktPartner.getKontaktPerson().retainAll(kontaktpersonList);
			for (KontaktPerson k : kontaktpersonList) {
				if (aktPartner.getKontaktPerson().contains(k)== false) {
					aktPartner.getKontaktPerson().add(k);
				}
			}
			entityManager.getTransaction().commit();
			readEdiListeforPartner(aktPartner);
			mainCtr.setInfoText("Der Partner \"" + aktPartner.getName() + "\" wurde gespeichert");
		}
		return true;
	}
	
	private String checkPartnerName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<EdiPartner> tq = entityManager.createQuery(
				"SELECT p FROM EdiPartner p WHERE LOWER(p.name) = LOWER(:n)",EdiPartner.class);
		tq.setParameter("n", newName);
		List<EdiPartner> partnerList = tq.getResultList();
		for (EdiPartner p : partnerList ) {
			if (p.getId() != aktPartner.getId()) {
				if (p.getName().equalsIgnoreCase(newName)) {
					return "Ein anderer Partner heiﬂt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforPartner( EdiPartner newPartner) {
		tvVerwendungen.getItems().clear();
		ObservableList<EdiEmpfaenger> empfaengerList = FXCollections.observableArrayList();
		ediEintragsSet.clear(); 
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente.ediSystem.ediPartner = :p", EdiEintrag.class);
		tqS.setParameter("p", newPartner);
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
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente.ediSystem.ediPartner = :p", EdiEmpfaenger.class);
		tqE.setParameter("p", newPartner);
		for(EdiEmpfaenger e : tqE.getResultList() ) {
			log("readEdiListeforKomponete", "Empfaenger:" + e.getKomponente().getFullname() + " add");
			empfaengerList.add(e);
			ediEintragsSet.add(e.getEdiEintrag());
		}
		tvVerwendungen.setItems(empfaengerList);
	}

    @FXML
    void actionAddKontaktPerson(ActionEvent event) {
    	logger.entry();
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	KontaktPersonAuswaehlenController controller = mainCtr.loadKontaktPersonAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		if (controller.getResponse() == Actions.OK) {
    			kontaktpersonList.add(controller.getKontaktperson());
    			dataIsChanged.set(true);
    		}
    	}
    	logger.exit();
    }

    @FXML
    void actionRemoveKontaktPerson(ActionEvent event) {
    	logger.entry();
    	KontaktPerson toBeRemoved = lvAnsprechpartner.getSelectionModel().getSelectedItem();
    	kontaktpersonList.remove(toBeRemoved);
    	mainCtr.setInfoText("Die Kontaktperson \"" + toBeRemoved.getVorname() + " " + 
    					toBeRemoved.getNachname() + "\" wurde aus dieser Kontaktliste entfernt");
		dataIsChanged.set(true);
    }
	
	public final ObjectProperty<EdiPartner> ediPartnerProperty() {
		return ediPartner;
	}
	
	public final EdiPartner getEdiPartner() {
		return ediPartner.get() ;
	}
	
	public final void setEdiPartner(EdiPartner ediPartner) {
		this.ediPartner.set(ediPartner);
	}
    
	private static void log(String methode, String message) {
		if (message == null || methode == null) {
			String className = EdiPartnerController.class.getName().substring(16);
			System.out.println(className + "." + methode + "(): " + message); 
		}
	}
	
	final static String fxmlFilename = "EdiPartner.fxml";
	final static String fxmlErrortxt = "' was not injected: check FXML-file '" + fxmlFilename + "'.";
	void checkFieldsFromView() {
    	assert tfBezeichnung 			!= null : "fx:id='tfBezeichnung"   			+ fxmlErrortxt;
    	assert taBeschreibung			!= null : "fx:id='taBeschreibung"  			+ fxmlErrortxt;
    	assert tcEdiNr					!= null : "fx:id='tcEdiNr"  				+ fxmlErrortxt;
    	assert tcSender					!= null : "fx:id='tcSender"  				+ fxmlErrortxt;
        assert tcEmpfaenger				!= null : "fx:id='tcEmpfaenger"  			+ fxmlErrortxt;
        assert tcGeschaeftsobjekt		!= null : "fx:id='tcGeschaeftsobjekt"  		+ fxmlErrortxt;
        assert tcDatumAb				!= null : "fx:id='tcDatumAb"  				+ fxmlErrortxt;
        assert tcDatumBis				!= null : "fx:id='tcDatumBis"  				+ fxmlErrortxt;
        assert tvVerwendungen			!= null : "fx:id='tvVerwendungen"  			+ fxmlErrortxt;
        assert lvAnsprechpartner		!= null : "fx:id='lvAnsprechpartner"		+ fxmlErrortxt;
        assert btnLoeschen				!= null : "fx:id='btnLoeschen"  			+ fxmlErrortxt;
        assert btnSpeichern 			!= null : "fx:id='btnSpeichern"  			+ fxmlErrortxt;
        assert btnRemoveKontaktPerson	!= null : "fx:id='btnRemoveKontaktPerson"	+ fxmlErrortxt;
    }
}
