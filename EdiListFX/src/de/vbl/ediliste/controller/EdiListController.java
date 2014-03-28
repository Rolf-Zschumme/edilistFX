package de.vbl.ediliste.controller;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class EdiListController {
	private static final String PERSISTENCE_UNIT_NAME = "EdiListFX";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableColumn<?, ?> ediNrCol;

    @FXML
    private TitledPane ediEintragPane;

    @FXML
    private TextField senderName;

    @FXML
    private TextField datenart;

    @FXML
    private TextField ediLastChange;

    @FXML
    private TableColumn<?, ?> ediKurzbezCol;

    @FXML
    private TableView<?> ediNrTable;

    @FXML
    private TitledPane szenarioPane;

    @FXML
    private TextField ediBezeichnung;

    @FXML
    private TextField empfaengerName;

    @FXML
    private TitledPane anbindungPane;

    
    private EntityManager em;
    private ObservableList<EdiNrListElement> ediNrArrayList = FXCollections.observableArrayList();
    
    /* ------------------------------------------------------------------------
     * initialize() is the controllers "main"-method 
     * it is called after loading "EdiListe.fxml" 
     * ----------------------------------------------------------------------*/
    @FXML
    void initialize() {
    	checkFieldFromView();
        setupEntityManager();
        loadEdiNrListData();
        setupBindings();
    }
    
    private void setupBindings() {
    	
//    	ediNrTable.setItems(ediNrArrayList);
//    	
//    	ediNrCol.setCellValueFactory(new Callback<CellDataFeatures<EdiNrListElement, String>, ObservableValue<String> >() {
//    		@Override
//    		public ObservableValue<String> call (
//    				CellDataFeatures<EdiNrListElement, String> parameter) {
//    			return parameter.getValue().ediNrProperty();
//    		}
//    	});
    	
//    	ediNrCol.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediNr"));
//    	ediKurzbezCol.setCellValueFactory(new PropertyValueFactory<EdiNrListElement,String>("ediKurzBez"));
		
		
	}

	private void loadEdiNrListData() {
    	Query query = em.createQuery("SELECT e.id, e.ediNr, e.kurzBez FROM EdiEintrag e ORDER BY e.ediNr");

    	ediNrArrayList.clear();
    	for (Object zeile  : query.getResultList()) {
    		Object[] obj = (Object[]) zeile;
			ediNrArrayList.add(new EdiNrListElement( (Long) obj[0], (String) obj[1], (String) obj[2]));
    	}	
	}
    	
    class EdiNrListElement    {
    	private LongProperty ediId;
    	private StringProperty ediNr;
    	private StringProperty ediKurzbez;
    	
    	public EdiNrListElement(Long id, String nr, String kurzbez) {
    		this.ediId = new SimpleLongProperty(id);
    		this.ediNr = new SimpleStringProperty(nr);
    		this.ediKurzbez = new SimpleStringProperty(kurzbez);
    	}
    	
    	public StringProperty ediNrProperty() {
    		return ediNr;
    	}
    	
    	public StringProperty ediKurzbezProperty() {
    		return ediKurzbez;
    	}

    	public Long getEdiId() {
    		return ediId.getValue();
    	}
    }
    
	private void setupEntityManager() {
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    	em = factory.createEntityManager();
    }
    
    private void checkFieldFromView() {
		assert ediNrCol != null : "fx:id=\"ediNrCol\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediEintragPane != null : "fx:id=\"ediEintragPane\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert senderName != null : "fx:id=\"senderName\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert datenart != null : "fx:id=\"datenart\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediLastChange != null : "fx:id=\"ediLastChange\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediKurzbezCol != null : "fx:id=\"ediKurzbezCol\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediNrTable != null : "fx:id=\"ediNrTable\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert szenarioPane != null : "fx:id=\"szenarioPane\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert ediBezeichnung != null : "fx:id=\"ediBezeichnung\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert empfaengerName != null : "fx:id=\"empfaengerName\" was not injected: check your FXML file 'EdiListe.fxml'.";
		assert anbindungPane != null : "fx:id=\"anbindungPane\" was not injected: check your FXML file 'EdiListe.fxml'.";
	}
}
