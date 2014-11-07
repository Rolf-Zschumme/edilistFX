package de.vbl.im.controller.subs;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.Dialog.Actions;

import de.vbl.im.controller.IntegrationManagerController;
import de.vbl.im.model.KontaktPerson;

public class KontaktPersonAuswaehlenController implements Initializable {
	private static final Logger logger = LogManager.getLogger(KontaktPersonAuswaehlenController.class.getName());

	private static Stage primaryStage = null;
    private static String applName = null;
	private static IntegrationManagerController managerController;
    private static EntityManager entityManager = null;
    
    private ObservableList<KontaktPerson> kontaktPersonList = FXCollections.observableArrayList();
	/**
	 * injection from 'KontaktPersonAuswaehlen.fxml'
	 */
    @FXML private TabPane tabPane;
    
    @FXML private Tab tabKontaktPersonenListe;
    @FXML private TableView<KontaktPerson> tableKontaktAuswahl;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktUserId;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktNachname;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktVorname;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktAbteilung;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktTelefon;
    @FXML private TableColumn<KontaktPerson, String> tColKontaktMailadresse;
    
    @FXML private Tab tabOutlookAuswahl;
	@FXML private TextField tfNachnameOutlook; 
	@FXML private TextField tfNummerOutlook; 
	@FXML private TextField tfVornameOutlook; 
    @FXML private TextField tfAbteilungOutlook; 
    @FXML private TextField tfMailadresseOutlook; 
    @FXML private TextField tfTelefonOutlook;
    @FXML private Label     lbHinweisOutlook;
    @FXML private Label     lbHinweisNeueingabe;
    @FXML private Button    btnOK; 

    @FXML private Tab tabNeueingabe;
    @FXML private TextField tfNachname; 
	@FXML private TextField tfVorname; 
    @FXML private TextField tfAbteilung; 
    @FXML private TextField tfMailadresse; 
    @FXML private TextField tfTelefon;
    
    private BooleanProperty listIsNotSelected = new SimpleBooleanProperty();
    private BooleanProperty outlookIsNotSelected = new SimpleBooleanProperty(false);
    private BooleanProperty newPersonNotEntered = new SimpleBooleanProperty(false);

    
    private KontaktPerson kontaktperson = new KontaktPerson();
    private BooleanProperty nachnameFilled = new SimpleBooleanProperty(false);
    private Actions retAction = Actions.CLOSE;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
			assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tableKontaktAuswahl != null : "fx:id=\"tableKontaktAuswahl\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tColKontaktUserId != null : "fx:id=\"tColKontaktUserId\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tColKontaktNachname != null : "fx:id=\"tColKontaktNachname\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tColKontaktVorname != null : "fx:id=\"tColKontaktVorname\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tColKontaktAbteilung != null : "fx:id=\"tColKontaktAbteilung\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tColKontaktTelefon != null : "fx:id=\"tColKontaktTelefon\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tColKontaktMailadresse != null : "fx:id=\"tColKontaktMailadresse\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			
			assert tabOutlookAuswahl != null : "fx:id=\"tabOutlookAuswahl\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfNummerOutlook != null : "fx:id=\"tfNummerOutlook\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfVornameOutlook != null : "fx:id=\"tfVornameOutlook\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfAbteilungOutlook != null : "fx:id=\"tfAbteilungOutlook\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfMailadresseOutlook != null : "fx:id=\"tfMailadresseOutlook\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfTelefonOutlook != null : "fx:id=\"tfTelefonOutlook\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert btnOK != null : "fx:id=\"btnOK\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			
			assert tabNeueingabe != null : "fx:id=\"tabNeueingabe\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfNachname != null : "fx:id=\"tfNachname\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfVorname != null : "fx:id=\"tfVorname\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfAbteilung != null : "fx:id=\"tfAbteilung\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfMailadresse != null : "fx:id=\"tfMailadresse\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfTelefon != null : "fx:id=\"tfTelefon\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
			assert tfNachnameOutlook != null : "fx:id=\"tfNachnameOutlook\" was not injected: check your FXML file 'KontaktPersonAuswaehlen.fxml'.";
		} catch (AssertionError e) {
			logger.error(e.getMessage(), e);
		}
    	kontaktperson.setVorname("");
    	
		tableKontaktAuswahl.setItems(kontaktPersonList);
		tColKontaktUserId.setCellValueFactory(cellData -> cellData.getValue().nummerProperty());
		tColKontaktNachname.setCellValueFactory(cellData -> cellData.getValue().nachnameProperty());
		tColKontaktVorname.setCellValueFactory(cellData -> cellData.getValue().vornameProperty());
		tColKontaktAbteilung.setCellValueFactory(cellData -> cellData.getValue().abteilungProperty());
		tColKontaktTelefon.setCellValueFactory(cellData -> cellData.getValue().telefonProperty());
		tColKontaktMailadresse.setCellValueFactory(cellData -> cellData.getValue().mailProperty());
		
    	tfNachname.textProperty().addListener((ov, oldValue, newValue) ->  {
    		kontaktperson.setNachname(newValue.trim());
    		nachnameFilled.set(newValue.trim().length() > 0);
    		lbHinweisNeueingabe.setText("");
    	}); 
    	
    	tfVorname.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setVorname(newValue.trim());
    		lbHinweisNeueingabe.setText("");
    	});
    	
    	tfAbteilung.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setAbteilung(newValue.trim());
    		lbHinweisNeueingabe.setText("");
    	});
    	
    	tfTelefon.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setTelefon(newValue.trim());
    	});
    	
    	tfMailadresse.textProperty().addListener((ov, oldValue, newValue) -> {
    		kontaktperson.setMail(newValue.trim());
    	});
    	listIsNotSelected.bind(tabKontaktPersonenListe.selectedProperty().and(Bindings.isNull(tableKontaktAuswahl.getSelectionModel().selectedItemProperty())));
    	outlookIsNotSelected.bind(tabOutlookAuswahl.selectedProperty().and(Bindings.isEmpty(tfNummerOutlook.promptTextProperty())));
    	newPersonNotEntered.bind(tabNeueingabe.selectedProperty().and(Bindings.not(nachnameFilled)));
    	
    	btnOK.disableProperty().bind(listIsNotSelected.or(outlookIsNotSelected).or(newPersonNotEntered));
    	
    }

    public void start(Stage primaryStage, IntegrationManagerController managerController, EntityManager entityManager) {

    	KontaktPersonAuswaehlenController.primaryStage = primaryStage;
    	KontaktPersonAuswaehlenController.entityManager = entityManager;
    	KontaktPersonAuswaehlenController.managerController = managerController;
		applName = primaryStage.getTitle();
		
		loadKontaktPersonListData();
	}

	public Actions getResponse () {
		return retAction;
	}

	public KontaktPerson getKontaktperson() {
		return kontaktperson;
	}
    
    @FXML
    private void okPressed(ActionEvent event) {
    	if (applName == null && managerController == null && primaryStage == null ) {
    		// TODO just for suppressing "unused" warning;
    	}
//    	Tab akttab = tabPane.getSelectionModel().getSelectedItem();
    	
    	if (tabKontaktPersonenListe.isSelected()) {
    		kontaktperson = tableKontaktAuswahl.getSelectionModel().selectedItemProperty().get();
    		retAction = Actions.OK;
    		close(event);
    	} else if ( tabOutlookAuswahl.isSelected()) {
    		
    		// TODO
    		
    	} else if ( tabNeueingabe.isSelected()) {
    		if (saveInputData() == true) {
    			retAction = Actions.OK;
    			close(event);
    		}
    	}	
    }

	@FXML
    private void escapePressed(ActionEvent event) {
		retAction = Actions.CANCEL;
    	close(event);
    }
    
    private void close(ActionEvent event) {
    	Node source = (Node) event.getSource();
    	Stage stage = (Stage) source.getScene().getWindow();
    	stage.close();
    }
    
    private boolean saveInputData() {
    	String ret = checkKontakt(kontaktperson);
    	if (ret != null) {
    		lbHinweisNeueingabe.textProperty().set(ret);
    		return false;
    	}
		entityManager.getTransaction().begin();
		entityManager.persist(kontaktperson);
		entityManager.getTransaction().commit();
		
		logger.info("Neuer Kontakt (" + kontaktperson.getId() + ") erfolgreich angelegt");
    	return true;
    }

	private String checkKontakt(KontaktPerson kp) {
		if ("".equals(kp.getNachname())) {
			return "Ein Nachname ist erforderlich";
		}
		TypedQuery<KontaktPerson> tq = entityManager.createQuery(
				"SELECT k FROM KontaktPerson k WHERE LOWER(k.nachname) = LOWER(:n)",KontaktPerson.class);
		tq.setParameter("n", kp.getNachname());
		List<KontaktPerson> kontaktPersonList = tq.getResultList();
		for (KontaktPerson k : kontaktPersonList) {
			String vorname = k.vornameProperty().getValueSafe(); 
			String abteilung = k.getAbteilungSafe();
			if (kp.getVorname().equalsIgnoreCase(vorname) && 
				kp.getAbteilungSafe().equalsIgnoreCase(abteilung) ) {
				if (vorname.length() > 0)   
					vorname += " ";
				if (abteilung.length() > 0) 
					abteilung = " bei " + abteilung;
				return "Es gibt schon einen \"" + vorname + k.getNachname() + "\"" + abteilung;
			}
		}
		return null;
	}
	
	private void loadKontaktPersonListData() {
		TypedQuery<KontaktPerson> tq = entityManager.createQuery(
				"SELECT k FROM KontaktPerson k ORDER BY k.nachname", KontaktPerson.class);
		List<KontaktPerson> aktulist = tq.getResultList();
		kontaktPersonList.retainAll(aktulist);
		for (KontaktPerson k : aktulist) {
			if (kontaktPersonList.contains(k) == false ) {
				kontaktPersonList.add(aktulist.indexOf(k), k);
			}
		}
	}
}
