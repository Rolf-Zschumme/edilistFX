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
import de.vbl.ediliste.model.EdiKomponente;

public class EdiKomponenteController {
	private static Stage primaryStage = null;
	private static TextField infoField;
	private static EntityManager entityManager;
	private final ObjectProperty<EdiKomponente> edikomponente;
	private final ObservableList<EdiEmpfaenger> ediKomponenteList = FXCollections.observableArrayList();
	private EdiKomponente aktKompo = null;
	
	@FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private AnchorPane ediKomponente;
    @FXML private TextField tfBezeichnung;
    @FXML private TextArea taBeschreibung;
    @FXML private TableView<EdiEmpfaenger> tvVerwendungen;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEmpfaenger;
    @FXML private TableColumn<EdiEmpfaenger, String> tcEdiNr;
    @FXML private TableColumn<EdiEmpfaenger, String> tcSender;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumAb;
    @FXML private TableColumn<EdiEmpfaenger, String> tcDatumBis;
    
    public EdiKomponenteController() {
    	this.edikomponente = new SimpleObjectProperty<>(this, "edikomponente", null);
    }

	public static void start(Stage primaryStage, TextField infoTextField, EntityManager entityManager) {
		EdiKomponenteController.primaryStage = primaryStage;
		EdiKomponenteController.infoField = infoTextField; 
		EdiKomponenteController.entityManager = entityManager;
	}

	@FXML
	public void initialize() {
		System.out.println("EdiKomponenteController.initialize()");
		checkFieldsFromView();
		
		edikomponente.addListener(new ChangeListener<EdiKomponente>() {
			@Override
			public void changed(ObservableValue<? extends EdiKomponente> ov,
					EdiKomponente oldKomponente, EdiKomponente newKomponente) {
				System.out.println("EdiKomponenteController.ChangeListener(edikomponente):"
									+ oldKomponente + " " +newKomponente);
				infoField.setText("");
				if (oldKomponente != null) {
					checkForChangesOk(true);
					ediKomponenteList.clear();
					if (newKomponente == null) {
						tfBezeichnung.setText("");
						taBeschreibung.setText("");
					}
				}
				if (newKomponente != null) {
					aktKompo = newKomponente;
					ediKomponenteList.clear();
					readEdiListeforKomponete(newKomponente);
					tfBezeichnung.setText(newKomponente.getName());
					taBeschreibung.setText(newKomponente.getBeschreibung());
				}
			}
		});

		tvVerwendungen.setItems(ediKomponenteList);
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
						if (sender.equals(aktKompo.getFullname()))
							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
						else
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
						if (empf.equals(aktKompo.getFullname()))
							setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
						else
							setFont(Font.font(null, FontWeight.NORMAL,getFont().getSize()));
					}
				}
			};
		});
		tcDatumAb.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().seitDatumProperty());
		tcDatumBis.setCellValueFactory(cellData -> cellData.getValue().getEdiEintrag().bisDatumProperty());
	}
	
	public boolean checkForChangesOk(boolean askForUpdate) {
		if (aktKompo == null ) {
			System.out.println(this.getClass().getName() + " checkForChanges() ohne aktKompo");
			return true;
		}
		String orgName = aktKompo.getName();
		String newName = tfBezeichnung.getText();
		String orgBeschreibung = (aktKompo.getBeschreibung()==null) ? "" : aktKompo.getBeschreibung();
		String newBeschreibung = (taBeschreibung.getText()==null) ? "" : taBeschreibung.getText();
		if (!orgName.equals(newName) ||
			!orgBeschreibung.equals(newBeschreibung) ) {
			if (askForUpdate) {
				Action response = Dialogs.create()
    				.owner(primaryStage).title(primaryStage.getTitle())
    				.actions(Dialog.Actions.OK, Dialog.Actions.CANCEL)
    				.message("Sollen die �nderungen an der Komponente " + orgName + " gespeichert werden")
    				.showConfirm();
	    		if (response != Dialog.Actions.OK) {
	    			return false;
	    		}	
			}	
			if (checkName(newName) == false) {
				infoField.setText("Fehler: Eine andere Komponente des Systems hei�t bereits so!");
				return false;
			}
			System.out.println("checkForChanges() - �nderung erkannt -> update");
			entityManager.getTransaction().begin();
			aktKompo.setName(newName);
			aktKompo.setBeschreibung(newBeschreibung);
			entityManager.getTransaction().commit();
			infoField.setText("Komponente wurde gespeichert");
		}
		return true;
	}
	
	private boolean checkName(String newName) {
		TypedQuery<EdiKomponente> tq = entityManager.createQuery(
				"SELECT k FROM EdiKomponente k WHERE LOWER(k.name) = LOWER(:n)",EdiKomponente.class);
		tq.setParameter("n", newName);
		List<EdiKomponente> kompoList = tq.getResultList();
		for (EdiKomponente k : kompoList ) {
			if (k.getId() != aktKompo.getId() &&
				k.getEdiSystem().getId() == aktKompo.getEdiSystem().getId())  {
				if (k.getName().equalsIgnoreCase(newName)) {
					return false;
				}
			}
		}
		return true;
	}

	@FXML
	void speichern(ActionEvent event) {
		checkForChangesOk(false);
	}
	
	private void readEdiListeforKomponete( EdiKomponente selKomponente) {
		TypedQuery<EdiEintrag> tqS = entityManager.createQuery(
				"SELECT e FROM EdiEintrag e WHERE e.ediKomponente = :k", EdiEintrag.class);
		tqS.setParameter("k", selKomponente);
//		tqS.setHint("javax.persistence.cache.storeMode", "REFRESH");
		List<EdiEintrag> ediList = tqS.getResultList();
		for(EdiEintrag e : ediList ) {
			if (e.getEdiEmpfaenger().size() > 0)
				ediKomponenteList.addAll(e.getEdiEmpfaenger());
			else {
				EdiEmpfaenger tmpE = new EdiEmpfaenger();
				tmpE.setEdiEintrag(e);
				ediKomponenteList.addAll(tmpE);
			}
		}
		System.out.println("KomponenteController:" + ediList.size() + " EDI-Eintr�ge gelesen");
		TypedQuery<EdiEmpfaenger> tqE = entityManager.createQuery(
				"SELECT e FROM EdiEmpfaenger e WHERE e.komponente = :k", EdiEmpfaenger.class);
		tqE.setParameter("k", selKomponente);
//		tqE.setHint("javax.persistence.cache.storeMode", "REFRESH");
		ediKomponenteList.addAll(tqE.getResultList());
		System.out.println("KomponenteController:" + tqE.getResultList().size() + " EDI-Empf�nger gelesen");
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
    	assert ediKomponente != null : "fx:id=\"ediKomponente\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tfBezeichnung != null : "fx:id=\"tfBezeichnung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert taBeschreibung != null : "fx:id=\"taBeschreibung\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcEdiNr != null : "fx:id=\"tcEdiNr\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    	assert tcSender != null : "fx:id=\"tcSender\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcEmpfaenger != null : "fx:id=\"tcEmpfaenger\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tcDatumBis != null : "fx:id=\"tcDatumBis\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
        assert tvVerwendungen != null : "fx:id=\"tvVerwendungen\" was not injected: check your FXML file 'EdiKomponente.fxml'.";
    }
    
}
