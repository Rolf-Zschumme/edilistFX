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
import de.vbl.im.model.EdiSystem;
import de.vbl.im.model.KontaktPerson;

public class EdiSystemController {
	private static final Logger logger = LogManager.getLogger(EdiSystemController.class.getName());
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiSystem> ediSystem;
	private final ObservableList<EdiEmpfaenger> ediKomponentenList = FXCollections.observableArrayList();
	private final ObservableList<KontaktPerson> kontaktpersonList; 
	private EdiSystem aktSystem = null;
	private static String aktFullName;
	
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
    
    public EdiSystemController() {
    	this.ediSystem = new SimpleObjectProperty<>(this, "ediSystem", null);
    	this.kontaktpersonList = FXCollections.observableArrayList();
    }

	public static void setParent(EdiMainController mainController) {
		logger.entry();
		EdiSystemController.mainCtr = mainController;
		EdiSystemController.primaryStage = EdiMainController.getStage();
		EdiSystemController.entityManager = mainController.getEntityManager();
		logger.exit();
	}

	@FXML
	public void initialize() {
		checkFieldsFromView();
		
		ediSystem.addListener(new ChangeListener<EdiSystem>() {
			@Override
			public void changed(ObservableValue<? extends EdiSystem> ov,
					EdiSystem oldSystem, EdiSystem newSystem) {
				logger.info(((oldSystem==null) ? "null" : oldSystem.getFullname()) + " -> " 
						  + ((newSystem==null) ? "null" : newSystem.getFullname()) );
				btnLoeschen.disableProperty().unbind();
				if (oldSystem != null) {
					ediKomponentenList.clear();
					kontaktpersonList.clear();
				}
				tfBezeichnung.setText("");
				taBeschreibung.setText("");
				if (newSystem != null) {
					aktSystem = newSystem;
					logger.info("newSystem.Name="+ newSystem.getName());
					aktFullName = aktSystem.getFullname();
					readEdiListeforSystem(newSystem);
					tfBezeichnung.setText(newSystem.getName());
					if (newSystem.getBeschreibung() == null) {
						newSystem.setBeschreibung("");
					}
					taBeschreibung.setText(newSystem.getBeschreibung());
					btnLoeschen.disableProperty().bind(Bindings.lessThan(0, aktSystem.anzKomponentenProperty()));
					kontaktpersonList.addAll(newSystem.getKontaktPerson());
					btnRemoveKontaktPerson.disableProperty().bind(lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktSystem == null) {
				logger.error("aktSystem==null in Listener for tfBezeichnung");
			}
			else if (aktSystem.getName() == null) {
				logger.error("aktSystem.getName()==null in Listener for tfBezeichnung");
			} else {	
				String msg = "";
				if (aktSystem.getName().equals(newValue) == false) {
					msg = checkSystemName(newValue);
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(msg);			
			}
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.equals(aktSystem.getBeschreibung()) == false) {
				dataIsChanged.set(true);
			} else {	
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
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
		
		tvVerwendungen.setItems(ediKomponentenList);
		tcEdiNr.setCellValueFactory(cellData -> 
					Bindings.format(EdiEintrag.FORMAT_EDINR, cellData.getValue().getEdiNrProperty()));
		
		tcSender.setCellValueFactory(cellData -> cellData.getValue().senderNameProperty());
		tcSender.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String sender, boolean empty) {
					super.updateItem(sender, empty);
					if (sender == null || empty) 
						setText(null); 
					else {
						setText(sender);
						FontWeight fw = sender.startsWith(aktFullName) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		
		tcEmpfaenger.setCellValueFactory(cellData -> cellData.getValue().empfaengerNameProperty());
		tcEmpfaenger.setCellFactory(column -> {
			return new TableCell<EdiEmpfaenger, String>() {
				@Override
				protected void updateItem (String empf, boolean empty) {
					super.updateItem(empf, empty);
					if (empf == null || empty) 
						setText(null); 
					else {
						setText(empf);
						FontWeight fw = empf.startsWith(aktFullName) ? FontWeight.BOLD : FontWeight.NORMAL;
						setFont(Font.font(null, fw, getFont().getSize()));
					}
				}
			};
		});
		tcGeschaeftsobjekt.setCellValueFactory(cellData -> cellData.getValue().geschaeftsObjektNameProperty());
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
	}

	@FXML
	void loeschen(ActionEvent event) {
		if (ediKomponentenList.size() > 0) {
			mainCtr.setErrorText("Fehler: Komponente wird verwendet");
			return;
		}	
		String aktName = "System \"" + aktSystem.getName() + "\"";
		String neuName = aktName;
		if (aktSystem.getName().equals(tfBezeichnung.getText()) == false) {
			neuName = aktName + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(neuName + " des Partners " + " \"" + 
				aktSystem.getEdiPartner().getName() + "\" wirklich löschen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				aktSystem.getEdiPartner().getEdiSystem().remove(aktSystem);
				entityManager.getTransaction().begin();
				entityManager.remove(aktSystem);
				entityManager.getTransaction().commit();
				aktSystem = null;
				mainCtr.loadSystemListData();
				mainCtr.setInfoText("Das " + aktName + " wurde erfolgreich gelöscht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim Löschen der Komponente " + aktName)
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
		logger.info("aktSystem=" + (aktSystem==null ? "null" : aktSystem.getFullname()));
		if (aktSystem == null ) {
			return true;
		}
		String orgName = aktSystem.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktSystem.getBeschreibung()==null ? "" : aktSystem.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		
		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) && 
			aktSystem.getKontaktPerson().containsAll(kontaktpersonList) &&
			kontaktpersonList.containsAll(aktSystem.getKontaktPerson())    ) 
		{
			logger.info("Name und Bezeichnung unveraendert");
		} else {
			if (checkmode == Checkmode.ONLY_CHECK) {
				return false;
			}
			if (checkmode == Checkmode.ASK_FOR_UPDATE) {
				Action response = Dialogs.create()
						.owner(primaryStage).title(primaryStage.getTitle())
						.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
						.message("Sollen die Änderungen an dem System " + orgName + " gespeichert werden ?")
						.showConfirm();
				if (response == Dialog.Actions.CANCEL) {
					return false;
				}
				if (response == Dialog.Actions.NO) {
					aktSystem = null;
					return true;
				}
			}
			String msg = checkSystemName(newName);
			if (msg != null) {
				mainCtr.setErrorText(msg);
				tfBezeichnung.requestFocus();
				return false;
			}
			logger.info("Aenderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktSystem.setName(newName);
			aktSystem.setBeschreibung(newBeschreibung);
			aktSystem.getKontaktPerson().retainAll(kontaktpersonList);
			for (KontaktPerson k : kontaktpersonList) {
				if (aktSystem.getKontaktPerson().contains(k)== false) {
					aktSystem.getKontaktPerson().add(k);
				}
			}
			entityManager.getTransaction().commit();
			readEdiListeforSystem(aktSystem);
			mainCtr.setInfoText("Das System " + orgName + " wurde gespeichert");
		}
		return true;
	}
	
	private String checkSystemName(String newName) {
		if ("".equals(newName)) {
			return "Eine Bezeichnung ist erforderlich";
		}
		TypedQuery<EdiSystem> tq = entityManager.createQuery(
				"SELECT s FROM EdiSystem s WHERE LOWER(s.name) = LOWER(:n)",EdiSystem.class);
		tq.setParameter("n", newName);
		List<EdiSystem> systemList = tq.getResultList();
		for (EdiSystem s : systemList ) {
			if (s.getId() != aktSystem.getId() &&
				s.getEdiPartner().getId() == aktSystem.getEdiPartner().getId())  {
				if (s.getName().equalsIgnoreCase(newName)) {
					return "Eine anderes System des Partners \"" +
							aktSystem.getEdiPartner().getName() + "\" heißt bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforSystem( EdiSystem selSystem) {
		ediKomponentenList.clear();
		/* 1. lese alle EdiEintraege mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugehoerigen Empfaenger, falls kein Empfaenger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente.ediSystem = :s", EdiEintrag.class);
		tqS.setParameter("s", selSystem);
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			if (e.getEdiEmpfaenger().size() > 0)
				ediKomponentenList.addAll(e.getEdiEmpfaenger());
			else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				ediKomponentenList.addAll(tmpE);
			}
		}
		logger.info("fuer "+ selSystem.getName() + " " + 
			ediList.size() + " EDI-Eintraege" + " mit insgesamt " + 
			ediKomponentenList.size() + " Empfaenger gelesen");
		
		/* 2. lese alle Empfaenger mit Empfaenger = selektierte Komponente 
		 *    -> zeige alle Empfaenger  
		 */
		
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente.ediSystem = :s", EdiEmpfaenger.class);
		tqE.setParameter("s", selSystem);
		ediKomponentenList.addAll(tqE.getResultList());
		logger.info("fuer " + selSystem.getName() + " " + 
			tqE.getResultList().size() + " EDI-Empfaenger gelesen");
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
	
	public final ObjectProperty<EdiSystem> ediSystemProperty() {
		return ediSystem;
	}
	
	public final EdiSystem getEdiSystem() {
		return ediSystem.get() ;
	}
	
	public final void setEdiSystem(EdiSystem ediSystem) {
		this.ediSystem.set(ediSystem);
	}
    
	final static String fxmlFilename = "EdSystem.fxml";
	final static String fxmlErrortxt = "' was not injected: check FXML-file '" + fxmlFilename + "'.";
    void checkFieldsFromView() {
    	assert tfBezeichnung 			!= null : "fx:id='tfBezeichnung"  			+ fxmlErrortxt;
    	assert taBeschreibung 			!= null : "fx:id='taBeschreibung"  			+ fxmlErrortxt;
    	assert tcEdiNr 					!= null : "fx:id='tcEdiNr"  				+ fxmlErrortxt;
    	assert tcSender 				!= null : "fx:id='tcSender"  				+ fxmlErrortxt;
    	assert tcEmpfaenger 			!= null : "fx:id='tcEmpfaenger"  			+ fxmlErrortxt;
    	assert tcGeschaeftsobjekt		!= null : "fx:id='tcGeschaeftsobjekt"		+ fxmlErrortxt;
    	assert tcDatumAb 				!= null : "fx:id='tcDatumAb"  				+ fxmlErrortxt;
    	assert tcDatumBis 				!= null : "fx:id='tcDatumBis"  				+ fxmlErrortxt;
    	assert lvAnsprechpartner    	!= null : "fx:id='lvAnsprechpartner"  		+ fxmlErrortxt;
    	assert tvVerwendungen 			!= null : "fx:id='tvVerwendungen"  			+ fxmlErrortxt;
    	assert btnSpeichern 			!= null : "fx:id='btnSpeichern"  			+ fxmlErrortxt;
    	assert btnLoeschen 				!= null : "fx:id='btnLoeschen"  			+ fxmlErrortxt;
    	assert btnRemoveKontaktPerson	!= null : "fx:id='btnRemoveKontaktPerson" 	+ fxmlErrortxt;
    }
}
