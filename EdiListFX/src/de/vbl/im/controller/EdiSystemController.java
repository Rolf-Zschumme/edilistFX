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

import de.vbl.im.controller.subs.AnsprechpartnerAuswaehlenController;
import de.vbl.im.model.Integration;
import de.vbl.im.model.EdiEmpfaenger;
import de.vbl.im.model.EdiSystem;
import de.vbl.im.model.Ansprechpartner;

public class EdiSystemController {
	private static final Logger logger = LogManager.getLogger(EdiSystemController.class.getName());
	private static Stage primaryStage = null;
	private static IMController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiSystem> ediSystem;
	private final ObservableList<EdiEmpfaenger> ediKomponentenList = FXCollections.observableArrayList();
	private final ObservableList<Ansprechpartner> ansprechpartnerList; 
	private EdiSystem aktSystem = null;
	private static String aktFullName;
	
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
    
    public EdiSystemController() {
    	this.ediSystem = new SimpleObjectProperty<>(this, "ediSystem", null);
    	this.ansprechpartnerList = FXCollections.observableArrayList();
    }

	public static void setParent(IMController managerController) {
		logger.entry();
		EdiSystemController.mainCtr = managerController;
		EdiSystemController.primaryStage = IMController.getStage();
		EdiSystemController.entityManager = managerController.getEntityManager();
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
					ansprechpartnerList.clear();
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
					ansprechpartnerList.addAll(newSystem.getAnsprechpartner());
					btnRemoveAnsprechpartner.disableProperty().bind(lvAnsprechpartner.getSelectionModel().selectedItemProperty().isNull());
				}
				dataIsChanged.set(false);
			}
		});
		
		btnSpeichern.disableProperty().bind(Bindings.not(dataIsChanged));

		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue)  -> {
			if (aktSystem != null) {
				String userMsg = "";
				if (aktSystem.getName().equals(newValue) == false) {
					userMsg = checkSystemName(newValue);
				}
				dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				mainCtr.setErrorText(userMsg);			
			}
		}); 

		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			if (aktSystem != null) {
				String userMsg = "";
				if (newValue.equals(aktSystem.getBeschreibung()) == false) {
					dataIsChanged.set(true);
				} else {	
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
				}
				mainCtr.setErrorText(userMsg);			
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
		
		tvVerwendungen.setItems(ediKomponentenList);
		tcEdiNr.setCellValueFactory(cellData -> 
					Bindings.format(Integration.FORMAT_EDINR, cellData.getValue().getEdiNrProperty()));
		
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
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getIntegration().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getIntegration().bisDatumProperty());
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
				aktSystem.getEdiPartner().getName() + "\" wirklich l�schen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				aktSystem.getEdiPartner().getEdiSystem().remove(aktSystem);
				entityManager.getTransaction().begin();
				entityManager.remove(aktSystem);
				entityManager.getTransaction().commit();
				aktSystem = null;
				mainCtr.loadSystemListData();
				mainCtr.setInfoText("Das " + aktName + " wurde erfolgreich gel�scht !");
			} catch (RuntimeException er) {
				Dialogs.create()
					.owner(primaryStage).title(primaryStage.getTitle())
					.masthead("Datenbankfehler")
				    .message("Fehler beim L�schen der Komponente " + aktName)
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
		if (aktSystem == null ) {
			logger.info("aktSystem=NULL?");
			return true;
		}
		String orgName = aktSystem.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktSystem.getBeschreibung()==null ? "" : aktSystem.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		
		if (orgName.equals(newName) &&
			orgBeschreibung.equals(newBeschreibung) && 
			aktSystem.getAnsprechpartner().containsAll(ansprechpartnerList) &&
			ansprechpartnerList.containsAll(aktSystem.getAnsprechpartner())    ) 
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
					.message("Sollen die �nderungen an dem System " + orgName + " gespeichert werden ?")
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
		logger.info("Update System " + newName);
		try {
			entityManager.getTransaction().begin();
			aktSystem.setName(newName);
			aktSystem.setBeschreibung(newBeschreibung);
			boolean kontaktListChanged = aktSystem.getAnsprechpartner().retainAll(ansprechpartnerList);
			for (Ansprechpartner k : ansprechpartnerList) {
				if (aktSystem.getAnsprechpartner().contains(k)== false) {
					aktSystem.getAnsprechpartner().add(k);
					kontaktListChanged = true;
				}
			}
			entityManager.getTransaction().commit();
			if (kontaktListChanged) {
				mainCtr.refreshKontaktReferences();
			}
			mainCtr.setInfoText("Das System " + orgName + " wurde gespeichert");
			dataIsChanged.set(false);
    	} catch (RuntimeException e) {
    		logger.error("Message:"+ e.getMessage(),e);
			Dialogs.create().owner(primaryStage)
				.title(primaryStage.getTitle())
				.masthead("FEHLER")
				.message("Fehler beim Speichern der Systemdaten:\n" + e.getMessage())
				.showException(e);
    	}
		readEdiListeforSystem(aktSystem);
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
							aktSystem.getEdiPartner().getName() + "\" hei�t bereits so!";
				}
			}
		}
		return null;
	}

	private void readEdiListeforSystem( EdiSystem selSystem) {
		ediKomponentenList.clear();
		TypedQuery<Integration> tqS = entityManager.createQuery(
				"SELECT e FROM Integration e WHERE e.ediKomponente.ediSystem = :s", Integration.class);
		tqS.setParameter("s", selSystem);
		List<Integration> ediList = tqS.getResultList();
		for(Integration e : ediList ) {
			if (e.getEdiEmpfaenger().size() > 0)
				ediKomponentenList.addAll(e.getEdiEmpfaenger());
			else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setIntegration(e);
				ediKomponentenList.addAll(tmpE);
			}
		}
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente.ediSystem = :s", EdiEmpfaenger.class);
		tqE.setParameter("s", selSystem);
		ediKomponentenList.addAll(tqE.getResultList());
	}

    @FXML
    void actionAddAnsprechpartner(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	AnsprechpartnerAuswaehlenController controller = mainCtr.loadAnsprechpartnerAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		String userInfo = "Die Ansprechpartner-Auswahl wurde abgebrochen"; 
    		if (controller.getResponse() == Actions.OK) {
    			Ansprechpartner selectedKontakt = controller.getAnsprechpartner();
    			if (ansprechpartnerList.contains(selectedKontakt)) {
    				userInfo = "Der ausgew�hlte Ansprechpartner ist bereits eingetragen";
    			} else {
    				ansprechpartnerList.add(selectedKontakt);
					dataIsChanged.set(!checkForChangesAndSave(Checkmode.ONLY_CHECK));
    				userInfo = "Der ausgew�hlte Kontakt wurde erg�nzt";
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
    	assert btnRemoveAnsprechpartner	!= null : "fx:id='btnRemoveAnsprechpartner" 	+ fxmlErrortxt;
    }
}
