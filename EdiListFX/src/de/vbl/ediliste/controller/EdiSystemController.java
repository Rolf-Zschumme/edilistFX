package de.vbl.ediliste.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import de.vbl.ediliste.model.EdiEintrag;
import de.vbl.ediliste.model.EdiEmpfaenger;
import de.vbl.ediliste.model.EdiSystem;

public class EdiSystemController {
	private static Stage primaryStage = null;
	private static EdiMainController mainCtr;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiSystem> ediSystem;
	private final ObservableList<EdiEmpfaenger> ediKomponentenList = FXCollections.observableArrayList();
	private EdiSystem aktSystem = null;
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane ediSystemPane;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcGeschaeftsobjekt;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    @FXML private Button btnLoeschen;
    
    public EdiSystemController() {
    	this.ediSystem = new SimpleObjectProperty<>(this, "ediSystem", null);
    }

	public static void start(Stage 			   primaryStage, 
							 EdiMainController mainController, 
							 EntityManager     entityManager) {
		log("start","called");
		EdiSystemController.primaryStage = primaryStage;
		EdiSystemController.mainCtr = mainController;
		EdiSystemController.entityManager = entityManager;
	}

	@FXML
	public void initialize() {
		log("initialize","called");
		checkFieldsFromView();
		
		ediSystem.addListener(new ChangeListener<EdiSystem>() {
			@Override
			public void changed(ObservableValue<? extends EdiSystem> ov,
					EdiSystem oldSystem, EdiSystem newSystem) {
				log("ChangeListener<EdiSystem>",
					((oldSystem==null) ? "null" : oldSystem.getFullname()) + " -> " 
				  + ((newSystem==null) ? "null" : newSystem.getFullname()) );
				if (oldSystem != null && newSystem == null) {
						ediKomponentenList.clear();
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
				}
				if (newSystem != null) {
					aktSystem = newSystem;
					log("ediSystemListner.changed", "newSystem.Name="+ newSystem.getName());
					readEdiListeforSystem(newSystem, CacheRefresh.FALSE);
					tfBezeichnung.setText(newSystem.getName());
//					taBeschreibung.setText(newSystem.getBeschreibung());
				}
			}
		});
		
		btnLoeschen.disableProperty().bind(Bindings.isNotEmpty(ediKomponentenList));
		
		tvVerwendungen.setItems(ediKomponentenList);
		tcEdiNr.setCellValueFactory(cellData -> 
					Bindings.format(EdiEintrag.FORMAT_EDINR, cellData.getValue().ediNrProperty()));
		
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
// todo						
//						if (sender.equals(aktSysten.getFullname()))
//							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
//						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
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
// todo						if (empf.equals(aktKomponente.getFullname()))
//							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
//						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
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
		String aktName = "Komponente \"" + aktSystem.getName() + "\"";
		String neuName = aktName;
		if (aktSystem.getName().equals(tfBezeichnung.getText()) == false) {
			neuName = aktName + " / \"" + tfBezeichnung.getText() + "\"";
		}
		Action response = Dialogs.create()
				.owner(primaryStage).title(primaryStage.getTitle())
				.message(neuName + " wirklich l�schen ?")
				.showConfirm();
		if (response == Dialog.Actions.YES) {
			try {
				entityManager.getTransaction().begin();
				entityManager.remove(aktSystem);
				entityManager.getTransaction().commit();
				aktSystem = null;
				mainCtr.loadSystemListData();
				mainCtr.setInfoText("Die " + aktName + " wurde erfolgreich gel�scht !");
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
		checkForChangesAndSave(false);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesAndSave(true);
	}

	private boolean checkForChangesAndSave(boolean askForUpdate) {
		log("checkForChangesAndSave","aktSystem=" + (aktSystem==null ? "null" : aktSystem.getFullname()));
		if (aktSystem == null ) {
			return true;
		}
		String orgName = aktSystem.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = aktSystem.getBeschreibung()==null ? "" : aktSystem.getBeschreibung();
		String newBeschreibung = taBeschreibung.getText()==null ? "" : taBeschreibung.getText();
		if (!orgName.equals(newName) ||
			!orgBeschreibung.equals(newBeschreibung) ) {
			if (askForUpdate) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.YES, Dialog.Actions.NO, Dialog.Actions.CANCEL)
    				.message("Sollen die �nderungen an dem System " + orgName + " gespeichert werden ?")
    				.showConfirm();
	    		if (response == Dialog.Actions.CANCEL) 	
	    			return false;
	    		if (response == Dialog.Actions.NO) {
	    			aktSystem = null;
	    			return true;
	    		}
			}	
			if (checkSystemName(newName) == false) {
				mainCtr.setErrorText("Eine anderes System des Partners hei�t bereits so!");
				return false;
			}
			log("checkForChangesAndSave","�nderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktSystem.setName(newName);
			aktSystem.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			readEdiListeforSystem(aktSystem, CacheRefresh.TRUE);
			mainCtr.setInfoText("Das System " + orgName + " wurde gespeichert");
		}
		else {
			log("checkForChangesAndSave", "Name und Bezeichnung unver�ndert");
		}
		return true;
	}
	
	private boolean checkSystemName(String newName) {
		TypedQuery<EdiSystem> tq = entityManager.createQuery(
				"SELECT s FROM EdiSystem s WHERE LOWER(s.name) = LOWER(:n)",EdiSystem.class);
		tq.setParameter("n", newName);
		List<EdiSystem> systemList = tq.getResultList();
		for (EdiSystem s : systemList ) {
			if (s.getId() != aktSystem.getId() &&
				s.getEdiPartner().getId() == aktSystem.getEdiPartner().getId())  {
				if (s.getName().equalsIgnoreCase(newName)) {
					return false;
				}
			}
		}
		return true;
	}

	private enum CacheRefresh { TRUE, FALSE;
		CacheRefresh() {}
	}
	
	private void readEdiListeforSystem( EdiSystem selSystem, CacheRefresh cache) {
		ediKomponentenList.clear();
		/* 1. lese alle EdiEintr�ge mit Sender = selekierter Komponente 
		 * 		-> zeige jeweils alle zugeh�rigen Empf�nger, falls kein Empf�nger vorhanden dummy erzeugen
		*/
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente.ediSystem = :s", EdiEintrag.class);
		tqS.setParameter("s", selSystem);
		if (cache == CacheRefresh.TRUE) {
			tqS.setHint("javax.persistence.cache.storeMode", "REFRESH");
		}	
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
		log("readEdiListeforKomponete", "f�r "+ selSystem.getName() + " " + 
			ediList.size() + " EDI-Eintr�ge" + " mit insgesamt " + 
			ediKomponentenList.size() + " Empf�nger gelesen (Refresh=" + cache+ ")");
		
		/* 2. lese alle Empf�nger mit Empf�nger = selektierte Komponente 
		 *    -> zeige alle Empf�nger  
		 */
		
//		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
//				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente = :k", EdiEmpfaenger.class);
//		tqE.setParameter("k", selSystem);
//		if (cache == CacheRefresh.TRUE) {
//			tqE.setHint("javax.persistence.cache.storeMode", "REFRESH");
//		}	
//		ediKomponentenList.addAll(tqE.getResultList());
//		log("readEdiListeforKomponete", "f�r " + selSystem.getName() + " " + 
//			tqE.getResultList().size() + " EDI-Empf�nger gelesen (Refresh=" + cache+ ")");
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
    
	private static void log(String methode, String message) {
		String className = EdiSystemController.class.getName().substring(16);
		System.out.println(className + "." + methode + "(): " + message); 
	}
		
    void checkFieldsFromView() {
    	assert ediSystemPane != null : "fx:id=\"ediSystemPane\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiSystem.fxml'.";
        assert btnLoeschen != null : "fx:id=\"btnLoeschen\" was not injected: check your FXML file 'EdiSystem.fxml'.";
    }
    
}
