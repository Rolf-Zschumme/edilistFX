
package de.vbl.im.controller;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;
import org.tmatesoft.svn.core.SVNException;

import de.vbl.im.controller.InSzenarioController;
import de.vbl.im.controller.subs.DokumentAuswaehlenController;
import de.vbl.im.controller.subs.KomponentenAuswahlController;
import de.vbl.im.controller.subs.KomponentenAuswahlController.KomponentenTyp;
import de.vbl.im.model.DokuLink;
import de.vbl.im.model.Integration;
import de.vbl.im.model.InEmpfaenger;
import de.vbl.im.model.Intervall;
import de.vbl.im.model.InKomponente;
import de.vbl.im.model.GeschaeftsObjekt;
import de.vbl.im.model.InSzenario;
import de.vbl.im.model.Konfiguration;
import de.vbl.im.model.Repository;
import de.vbl.im.tools.IMconstant;
import de.vbl.im.tools.LongForByte;
 

public class IntegrationController {
	private static final Logger logger = LogManager.getLogger(IntegrationController.class.getName()); 
	private static final String INR_PANE_PREFIX = " I ";
	private static final Integer MAX_EMPFAENGER = 3;

	private static Stage primaryStage = null;
	private static IMController managerController;
	private static EntityManager entityManager = null;
	
	private final ObjectProperty<Integration> integration;

	@FXML private TitledPane m_InSzenarioPane;
	@FXML private AnchorPane m_IntegrationPane;
    @FXML private TabPane tabPaneInNr;
    @FXML private Tab tabAktInNr;
    
    @FXML private ComboBox<InSzenario> cmbInSzenario;
    @FXML private ComboBox<Konfiguration> cmbKonfiguration;
    @FXML private Button m_NewInSzenarioBtn;
    @FXML private Button m_NewConfigurationBtn;
    @FXML private Button m_NewDokuLinkBtn;
    @FXML private Button m_RemoveDokuLinkBtn;
    @FXML private Button m_SpeichernBtn;
    @FXML private Button m_NeuanlageBtn;
    @FXML private Button m_LoeschenBtn;
    
    @FXML private TableView<DokuLink> tvDokuLinks;
    @FXML private TableColumn<DokuLink, String> tColDokumentVorhaben;
    @FXML private TableColumn<DokuLink, String> tColDokumentName;
    @FXML private TableColumn<DokuLink, LocalDateTime> tColDokumentDatum;
    @FXML private TableColumn<DokuLink, String> tColDokumentQuelle;
    @FXML private TableColumn<DokuLink, String> tColDokumentRevision;
    @FXML private TableColumn<DokuLink, String> tColDokumentPfad;
    
    @FXML private TextArea  taBeschreibung;
    @FXML private ComboBox<String> cmbBuOb1;
    @FXML private ComboBox<String> cmbBuOb2;
    @FXML private ComboBox<String> cmbBuOb3;
    @FXML private TextField tfLastChange;
    @FXML private TextField tfBezeichnung;
    @FXML private DatePicker dpProduktivSeit;
    @FXML private DatePicker dpProduktivBis;

    @FXML private Button btnSender;
    @FXML private Button btnEmpfaenger1;
    @FXML private Button btnEmpfaenger2;
    @FXML private Button btnEmpfaenger3;
    
//    private DoubleProperty doubleProperty = new SimpleDoubleProperty(0.0);
    @FXML private ComboBox<String> m_Intervall;
    @FXML private TextField m_AnzahlMsg;
    @FXML private TextField m_GroesseMsg;
    @FXML private TextField m_MaxGroesseMsg;
    @FXML private ChoiceBox<String> m_GroesseEinheit;
    @FXML private ChoiceBox<String> m_MaxGroesseEinheit;    
    
    @FXML private MenuButton mbtEmpfaenger2;
    @FXML private MenuButton mbtEmpfaenger3;
    
    
    private BooleanProperty dataIsChanged = new SimpleBooleanProperty(false);
    private BooleanProperty senderIsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger1IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger2IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty empfaenger3IsSelected = new SimpleBooleanProperty(false);
    private BooleanProperty buOb1Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb2Exist = new SimpleBooleanProperty(false);
    private BooleanProperty buOb3Exist = new SimpleBooleanProperty(false);
    
    private BooleanProperty readOnlyAccess = new SimpleBooleanProperty(false);
    private BooleanProperty inSzenarioNotSelected = new SimpleBooleanProperty(true);
    private BooleanProperty editEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty editStatusNew = new SimpleBooleanProperty(false);
    
    private Map<String,GeschaeftsObjekt> businessObjectMap; 
    private ObservableList<String> businessObjectName = FXCollections.observableArrayList();
    private ObservableList<String> intervallNameList  = FXCollections.observableArrayList();
    private ObservableList<String> groesseEinheitList = FXCollections.observableArrayList("KB","MB","GB");
    private ObservableList<DokuLink> dokuLinkList     = FXCollections.observableArrayList();

    private static class IntegrationPlus {
    	private int inNr;
    	private InSzenario inSzenario;
    	private String bezeichnung;
    	private String beschreibung;
    	private LocalDate seitDatum;
    	private LocalDate bisDatum;
    	private InKomponente sender; 
    	private InEmpfaenger empfaenger[] = new InEmpfaenger[MAX_EMPFAENGER];
    	private InKomponente empfaengerKomponente[] = new InKomponente[MAX_EMPFAENGER];
    	private GeschaeftsObjekt geschaeftsObjekt[] = new GeschaeftsObjekt[MAX_EMPFAENGER];
    	private String intervallName;
    	private int anzahlMsg;
    	private LongForByte averageByte = new LongForByte();
    	private LongForByte maximalByte = new LongForByte();
    	private Konfiguration konfiguration;
    	
    	void setData (Integration s) {
    		inNr = s.getInNr();
    		inSzenario = s.getInSzenario();
    		bezeichnung   = s.getBezeichnung()==null ? "" : s.getBezeichnung();
    		beschreibung  = s.getBeschreibung()==null ? "" : s.getBeschreibung();
    		sender = s.getInKomponente();
			String seitStr = s.seitDatumProperty().getValueSafe();
			seitDatum = seitStr.equals("") ? null : LocalDate.parse(seitStr);
			String bisStr = s.bisDatumProperty().getValueSafe();
			bisDatum = bisStr.equals("") ? null : LocalDate.parse(bisStr);
			int i=0;
			for(InEmpfaenger e : s.getInEmpfaenger()) {
				empfaenger[i] = e;
				empfaengerKomponente[i] = e.getKomponente();
				geschaeftsObjekt[i++] = e.getGeschaeftsObjekt();
			}
			while(i < MAX_EMPFAENGER) {
				empfaenger[i] = null;
				empfaengerKomponente[i] = null;
				geschaeftsObjekt[i++] = null;
			}
			intervallName = s.getIntervall()==null ? "" : s.getIntervall().getName();
			
			konfiguration = s.getKonfiguration();
			anzahlMsg = s.getAnzahlMsg()==null ? 0 : s.getAnzahlMsg();
			averageByte.set(s.getAverageByte());
			maximalByte.set(s.getMaximalByte());
    	}
    	private String inNrStr() {
   			return String.format(Integration.FORMAT_INNR, inNr / 100, inNr % 100);
    	}
    }
    IntegrationPlus aktIn = new IntegrationPlus();
    IntegrationPlus orgIn = new IntegrationPlus();
    
    private enum Status  { DESELECT, NEW , OLD, DIRTY }

    private static class EditData {
    	private Status status = Status.DESELECT;
    }
    static EditData edit = new EditData();
    
	public IntegrationController() {
    	this.integration = new SimpleObjectProperty<>(this, "integration", null);
		readOnlyAccess.set(false);
	}

	// Subcontroller must know his parent - here it is set
	public void setParent(IMController managerController) {
		logger.info("entered");
		IntegrationController.managerController = managerController;
		IntegrationController.primaryStage = IMController.getStage();
		IntegrationController.entityManager = managerController.getEntityManager();
		readInSzenarioList();
		readCmbKonfigurationList();
		readBusinessObject();
		readIntervalle();
		logger.exit();
	}
	

    @FXML 
    void initialize() {
    	logger.info("init");
    	checkFieldFromView();
    	setupLocalBindings();
    	integration.addListener( (ov, oldIntegration, newIntegration) -> {
    		logger.info("integration.Listener ENTRY");
    		if (oldIntegration != null) {
    			logger.info("integration.Listener oldIntegration:" + oldIntegration.inNrStrExp().get());
    			taBeschreibung.setText("");
    			tfBezeichnung.setText("");
    			btnEmpfaenger1.setText("");
    			btnEmpfaenger2.setText("");
    			btnEmpfaenger3.setText("");
    			tfLastChange.setText("");
    			tabPaneInNr.getTabs().retainAll(tabAktInNr);
    			btnSender.textProperty().unbind();
    			btnSender.setText("");
    			m_AnzahlMsg.setText("");
    			m_GroesseEinheit.setValue(null);
    			m_GroesseMsg.setText("");
    			m_MaxGroesseEinheit.setValue(null);
    			m_MaxGroesseMsg.setText("");
    			cmbKonfiguration.getSelectionModel().select(null);
    		}
    		if (newIntegration == null) {
    			logger.info("integration.Listener newIntegration==null");
    			managerController.setInfoText("Neue Integration kann bearbeitet werden");
    			InSzenario prevInSzenario = aktIn.inSzenario;
    			Integration newI = new Integration();
    			aktIn.setData(newI);
    			orgIn.setData(newI);
    			if (prevInSzenario != null) {
    				aktIn.inSzenario = prevInSzenario;
    				aktIn.inNr = neueInNrErmitteln(aktIn.inSzenario);
    				tabAktInNr.setText(INR_PANE_PREFIX  + aktIn.inNrStr() );
    				tabsReiterErgaenzen(aktIn.inSzenario.getIntegration().iterator());
    			}
    			resetEmpfaenger();
    		} else {
    			logger.info("integration.Listener newIntegration=" + newIntegration.inNrStrExp().get());
    			orgIn.setData(newIntegration);
    			if (aktIn.inSzenario != newIntegration.getInSzenario()) {
    				aktIn.setData(newIntegration);
    				cmbInSzenario.getSelectionModel().select(aktIn.inSzenario);
    			}
    			else {
    				tabPaneInNr.getTabs().retainAll(tabAktInNr);
    				aktIn.setData(newIntegration);
    				tabsReiterErgaenzen(aktIn.inSzenario.getIntegration().iterator());
    			}
    			tabAktInNr.setText(INR_PANE_PREFIX + newIntegration.inNrStrExp().get());
    			
    			tfBezeichnung.setText(aktIn.bezeichnung);
    			taBeschreibung.setText(aktIn.beschreibung);
    			if (aktIn.sender != null) {
    				btnSender.textProperty().bind(aktIn.sender.fullnameProperty());
    			} else {
    				btnSender.textProperty().unbind();
    				btnSender.setText("");
    			}
    			senderIsSelected.set(aktIn.sender != null);
    			setAktEmpfaenger();
    			m_AnzahlMsg.setText(String.format("%d",aktIn.anzahlMsg));
    			m_GroesseEinheit.setValue(aktIn.averageByte.getEinheit());
    			m_GroesseMsg.setText(aktIn.averageByte.getValueStr());
    			m_MaxGroesseEinheit.setValue(aktIn.maximalByte.getEinheit());
    			m_MaxGroesseMsg.setText(aktIn.maximalByte.getValueStr());
    			m_Intervall.getSelectionModel().select(aktIn.intervallName);
    			cmbKonfiguration.getSelectionModel().select(aktIn.konfiguration);
    			
    			setLastChangeField(tfLastChange, newIntegration.getLaeDatum(), newIntegration.getLaeUser());
    			
    	    	edit.status = Status.OLD;
    	    	editStatusNew.set(false);
    	    	editEnabled.set(true);
    		}
    		dpProduktivSeit.setValue(aktIn.seitDatum);
    		dpProduktivBis.setValue(aktIn.bisDatum);
    		dataIsChanged.set(false);
    		logger.info("integration.Listener EXIT");
    	});
    }	
    
    
    private void setLastChangeField(TextField tf, String dateTime, String laeUser) {
    	if (dateTime == null) {
    		tf.setText("");
    		tf.setTooltip(null);
    	} else {	
    		LocalDateTime dt = LocalDateTime.parse(dateTime);
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy"); 
    		tf.setText(laeUser + "  " + formatter.format(dt));
    		String ttt = LocalTime.from(dt).toString().substring(0, 8);
    		tf.setTooltip(new Tooltip(ttt));
    	}
    }
    
	private void setupLocalBindings() {
		logger.info("entered");
		
		cmbInSzenario.disableProperty().bind(readOnlyAccess.or((integration.isNotNull().and(editStatusNew.not()))));
		cmbInSzenario.opacityProperty().set(1);
		
		m_NewInSzenarioBtn.disableProperty().bind(readOnlyAccess.or(editStatusNew.not()));
//		m_NewInSzenarioBtn.opacityProperty().set(1);
		
		m_NewConfigurationBtn.disableProperty().bind(readOnlyAccess.or(
				cmbInSzenario.getSelectionModel().selectedItemProperty().isNull()));

		m_SpeichernBtn.disableProperty().bind(Bindings.not(dataIsChanged));
//		m_SpeichernBtn.disableProperty().bind(inSzenarioNotSelected.or(dataIsChanged)); // Bindings.not(dataIsChanged));
		m_LoeschenBtn.disableProperty().bind(this.integration.isNull());
		m_NeuanlageBtn.disableProperty().bind(readOnlyAccess.or(dataIsChanged).or(inSzenarioNotSelected)
									.or((editStatusNew).and(Bindings.not(inSzenarioNotSelected)) ) );

//		m_InSzenarioPane.disableProperty().bind(Bindings.isNull(integration));
		m_IntegrationPane.disableProperty().bind(Bindings.not(editEnabled));
		
		setupInSzenarioComboBox();
		setupDokuLink();
		setupKonfigurationComboBox();
		
		
		tfBezeichnung.textProperty().addListener((observable, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				aktIn.bezeichnung = newValue;
				setChangeFlag(!aktIn.bezeichnung.equals(orgIn.bezeichnung));
			}	
			managerController.setErrorText(msg);
		});
		
		taBeschreibung.textProperty().addListener((observable, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				aktIn.beschreibung = newValue;
				setChangeFlag(!aktIn.beschreibung.equals(orgIn.beschreibung));
			}	
			managerController.setErrorText(msg);
		});

    	dpProduktivSeit.setShowWeekNumbers(true);
    	dpProduktivBis.setShowWeekNumbers(true);
    	
    	dpProduktivSeit.setOnAction(event -> {
    		aktIn.seitDatum = dpProduktivSeit.getValue();
    		setChangeFlag(aktIn.seitDatum != orgIn.seitDatum);
    		if (aktIn.bisDatum != null && aktIn.seitDatum.isAfter(aktIn.bisDatum)) {
        		managerController.setInfoText("Seit-Datum sollte vor Bis-Datum liegen");  			
    		} else {
    			managerController.setInfoText("");  			
    		}
    	});
    	
    	dpProduktivBis.setOnAction(event -> {
    		aktIn.bisDatum = dpProduktivBis.getValue();
    		setChangeFlag(aktIn.bisDatum != orgIn.bisDatum);
    		if (aktIn.seitDatum != null && aktIn.bisDatum.isBefore(aktIn.seitDatum)) {
        		managerController.setInfoText("Bis-Datum sollte nach Seit-Datum liegen");  			
    		} else {
    			managerController.setInfoText("");  			
    		}
    	});
    	
		businessObjectMap = new HashMap<String,GeschaeftsObjekt>();		
		cmbBuOb1.setItems(businessObjectName);
		cmbBuOb2.setItems(businessObjectName);
		cmbBuOb3.setItems(businessObjectName);
		
		
		cmbBuOb1.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 0);
			if (checkedName != null) {
				cmbBuOb1.getSelectionModel().select(checkedName); // wegen Groß-/Kleinschrift
				buOb1Exist.set(true);
			}
		});
		cmbBuOb2.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 1);
			if (checkedName != null) {
				cmbBuOb2.getSelectionModel().select(checkedName); // wegen Groß-/Kleinschrift
				buOb2Exist.set(true);
			}
		});
		cmbBuOb3.valueProperty().addListener((observable, oldValue, newValue) -> {
			String checkedName = checkBusinessObjectName(newValue, 2);
			if (checkedName != null) {
				cmbBuOb3.getSelectionModel().select(checkedName); // wegen Groß-/Kleinschrift
				buOb3Exist.set(true);
			}
		});

		cmbBuOb1.disableProperty().bind(Bindings.not(senderIsSelected));
		btnEmpfaenger1.disableProperty().bind(Bindings.not(buOb1Exist));
    	
    	cmbBuOb2.disableProperty().bind(Bindings.not(empfaenger1IsSelected));
    	btnEmpfaenger2.disableProperty().bind(Bindings.not(buOb2Exist));
    	
    	cmbBuOb3.disableProperty().bind(Bindings.not(empfaenger2IsSelected));
    	btnEmpfaenger3.disableProperty().bind(Bindings.not(buOb3Exist));
		
    	cmbBuOb2.visibleProperty().bind(empfaenger1IsSelected);
    	mbtEmpfaenger2.visibleProperty().bind(buOb2Exist);
    	btnEmpfaenger2.visibleProperty().bind(empfaenger1IsSelected);
    	
    	cmbBuOb3.visibleProperty().bind(empfaenger2IsSelected);
    	mbtEmpfaenger3.visibleProperty().bind(buOb3Exist);
    	btnEmpfaenger3.visibleProperty().bind(empfaenger2IsSelected);
    	
		m_Intervall.setItems(intervallNameList);
		m_Intervall.valueProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null && aktIn.intervallName.equals(newValue) == false) {
				logger.debug("cmbIntervall.changed to " + newValue);
				aktIn.intervallName = newValue;
				setChangeFlag(!aktIn.intervallName.equals(orgIn.intervallName));
			}
			managerController.setErrorText(msg);			
		});
		
		m_AnzahlMsg.addEventFilter(KeyEvent.KEY_TYPED, integer_Validation(7));
		
		m_AnzahlMsg.textProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				if (newValue.equals("")) {
					newValue = "0";
				}
				aktIn.anzahlMsg = Integer.parseInt(newValue);
				setChangeFlag(aktIn.anzahlMsg != orgIn.anzahlMsg);
			}
			managerController.setErrorText(msg);			
		});
		
		m_GroesseMsg.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation(3));
		m_GroesseMsg.textProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				if (m_GroesseEinheit.getSelectionModel().isEmpty()) {
					m_GroesseEinheit.setValue("KB");
				}
				msg = aktIn.averageByte.setValue(newValue);
				setChangeFlag(aktIn.averageByte.get() != orgIn.averageByte.get());
			}
			managerController.setErrorText(msg);			
		});
		
		m_GroesseEinheit.setItems(groesseEinheitList);
		m_GroesseEinheit.valueProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				msg = aktIn.averageByte.setEinheit(newValue);
				setChangeFlag(aktIn.averageByte.get() != orgIn.averageByte.get());
			}
			managerController.setErrorText(msg);			
		});
		
		m_MaxGroesseMsg.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation(3));
		m_MaxGroesseMsg.textProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				if (m_MaxGroesseEinheit.getSelectionModel().isEmpty()) {
					m_MaxGroesseEinheit.setValue("KB");
				}
				msg = aktIn.maximalByte.setValue(newValue);
				setChangeFlag(aktIn.maximalByte.get() != orgIn.maximalByte.get());
			}
			managerController.setErrorText(msg);			
		});
		
		m_MaxGroesseEinheit.setItems(groesseEinheitList);
		m_MaxGroesseEinheit.valueProperty().addListener((ov, oldValue, newValue) -> {
			String msg = "";
			if (newValue != null) {
				msg = aktIn.maximalByte.setEinheit(newValue);
				setChangeFlag(aktIn.maximalByte.get() != orgIn.maximalByte.get());
			}
			managerController.setErrorText(msg);			
		});
		
    	logger.exit();
	}
	
	public EventHandler<KeyEvent> numeric_Validation(int maxLen) {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				TextField textField = (TextField) e.getSource();
				
				if (textField.getText().length() >= maxLen) {
					e.consume();
				}
				if (e.getCharacter().matches("[0-9,]")) {
					if(textField.getText().contains(",") && e.getCharacter().matches("[,]")) {
						e.consume();
					} else if(textField.getText().length() == 0 && e.getCharacter().matches("[0,]")) {
						e.consume();
					}
				} else {
					e.consume();
				}	
			}
		};
	}
	public EventHandler<KeyEvent> integer_Validation(int maxLen) {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				TextField textField = (TextField) e.getSource();
				if (textField.getText().length() >= maxLen || 
					!e.getCharacter().matches("[0-9]")     || 
					(textField.getText().length() == 0  && 
					  e.getCharacter().matches("[0]"))      ) {
						e.consume();
				}	
			}
		};
	}

	private void setupInSzenarioComboBox() {
		cmbInSzenario.setCellFactory((cmbBx) -> {
			return new ListCell<InSzenario>() {
				@Override
				protected void updateItem(InSzenario item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						setText(item.getIsNrStr() + " - " + item.getName());
						managerController.setInfoText("");
					}
				}
			};
		});
		 
		cmbInSzenario.setConverter(new StringConverter<InSzenario>() {
			@Override
			public String toString(InSzenario item) {
				return item==null ? null : item.getName();
			}
			@Override
			public InSzenario fromString(String string) {
				return null; // No conversion fromString needed
			}
		});
		
		// do checks which must be done before changing the inSzenario 
		cmbInSzenario.setOnAction((event) -> {
//			if (aktIn.inSzenario != null && edit.status != Status.NEW) {
//				Dialogs.create().owner(primaryStage).title("Hinweis")
//					.message("Nachträgliches Ändern der Zuordnung zu einem Integrationsszenario is derzeit nicht möglich")
//					.showInformation();
//				event.consume();
//			}
			if (orgIn.inSzenario != null && aktIn.inNr == orgIn.inNr &&				 
				aktIn.inSzenario == orgIn.inSzenario) {
				logger.info("cmbInSzenarion.action -> verifyDokuLinkIsUnchanged");
				if (verifyDokuLinkListIsUnchanged() == false) {
					// DokuLinkListe has been changed -> this changes may be lost  
					// ask User if changes should be stored
					Action response = Dialogs.create().owner(primaryStage)
							.title(IMconstant.SICHERHEITSABFRAGE)
							.message("Sollen die Änderungen an den Doku-Referenzen für das " +
									 "Integrationsszenario '" +aktIn.inSzenario.getName() +
									 "' gespeichert werden?")
							.actions(Dialog.Actions.YES, Dialog.Actions.NO)
							.showConfirm();
					if (response == Dialog.Actions.YES) {
						try {
							entityManager.getTransaction().begin();
							updateDokuLinkListInDatabase();
							entityManager.getTransaction().commit();
							logger.info("Transaction-Status(isActive):" + entityManager.getTransaction().isActive());
						} catch (RuntimeException er) {
							Dialogs.create().owner(primaryStage)
								.title(IMconstant.APPL_NAME)
								.masthead("Datenbankfehler")
								.message("Fehler beim Speichern der DokuLinkList-Anderungen")
								.showException(er);
						}

					}
				}
			}
		});
		
		cmbInSzenario.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
			logger.info("cmbInSzenarion.selected InSzenario:" + (newValue == null ? "null" : newValue.getName()) + 
					  				          " (old:" + (oldValue == null ? "null" : oldValue.getName()) + ")");
			if (orgIn.inSzenario != null) {
				setChangeFlag(newValue != orgIn.inSzenario);
			}
			aktIn.inSzenario = newValue;
			// refresh dokuLinkList
			if (oldValue != newValue) {
				dokuLinkList.clear();
				if(aktIn.inSzenario != null) {
					dokuLinkList.addAll(aktIn.inSzenario.getDokuLink());
				}
				if (edit.status == Status.NEW) {
    				aktIn.inNr = neueInNrErmitteln(aktIn.inSzenario);
				}
				tabAktInNr.setText(INR_PANE_PREFIX  + aktIn.inNrStr() );
			}
			inSzenarioNotSelected.set(newValue==null);
			
			if (newValue!= null) {
				m_NeuanlageBtn.requestFocus();
			}
			// zusätzliche INR-Reiter aktualisieren (entfernen/ergänzen)
			tabPaneInNr.getTabs().retainAll(tabAktInNr);
			if (newValue != null && aktIn.inNr > 0 && newValue.getIntegration() != null) {
				tabsReiterErgaenzen(newValue.getIntegration().iterator());
			}
			managerController.setInfoText("");
			logger.exit();
		});
	}
	
    private void tabsReiterErgaenzen(Iterator<Integration> iterator) {
    	final HashMap<Integer, Tab> tabMapAfter = new HashMap<Integer,Tab>();				
    	final HashMap<Integer, Tab> tabMapBefore = new HashMap<Integer,Tab>();
    	
    	while (iterator.hasNext()) {
    		Integration e = iterator.next();
    		int inNr = e.getInNr();
    		logger.info("akt-inNr:" + aktIn.inNr +"inNR:" + inNr);
    		if (inNr != aktIn.inNr ) {
    			Tab extraTab = new Tab(INR_PANE_PREFIX + e.inNrStrExp().get());
    			extraTab.setUserData(e);
    			Button btn = new Button("Hallo");  
    			extraTab.setContent(btn);
    			if (inNr  < aktIn.inNr ) tabMapBefore.put(inNr, extraTab);
    			if (inNr  > aktIn.inNr ) tabMapAfter.put(inNr, extraTab);
    		}	
    	}
    	if (tabMapAfter.size() > 0) {
    		logger.trace("After :" + tabMapAfter.size() + " aktsize:" + tabPaneInNr.getTabs().size());
    		for(Tab t : tabMapAfter.values()) {
    			logger.trace("Tab:" + t.getText() + " sel:" + tabPaneInNr.getSelectionModel().getSelectedIndex());
    			tabPaneInNr.getSelectionModel().select(0);
    			tabPaneInNr.getTabs().add(t);   //(i++, t);
    		}
    	}	
    	if (tabMapBefore.size() > 0) {
    		logger.trace("Before:" + tabMapBefore.size());
    		tabPaneInNr.getTabs().addAll(0, tabMapBefore.values());
    	}
	}

	private void setupDokuLink() {
		m_NewDokuLinkBtn.disableProperty().bind(cmbInSzenario.getSelectionModel().selectedItemProperty().isNull());
		m_RemoveDokuLinkBtn.disableProperty().bind(tvDokuLinks.getSelectionModel().selectedItemProperty().isNull());	
		tvDokuLinks.setItems(dokuLinkList);
		tColDokumentVorhaben.setCellValueFactory(cellData -> cellData.getValue().vorhabenProperty());
		tColDokumentName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
//		tColDokumentPfad.setCellValueFactory(cellData -> cellData.getValue().pfadProperty());
		
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
		
		tColDokumentDatum.setCellValueFactory(cellData -> cellData.getValue().datumProperty());
		tColDokumentDatum.setCellFactory(column -> {
			return new TableCell<DokuLink, LocalDateTime>() {
				@Override
				protected void updateItem (LocalDateTime item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(dtf.format(item));
					}
				}
			};
		});
		
		tColDokumentQuelle.setCellValueFactory(cellData -> cellData.getValue().getRepository().nameProperty());
		tColDokumentPfad.setCellValueFactory(cellData -> cellData.getValue().pfadProperty());
		tColDokumentRevision.setCellValueFactory(cellData -> Bindings.format("%5d",cellData.getValue().revisionProperty()));


    	tvDokuLinks.setRowFactory(new Callback<TableView<DokuLink>, TableRow<DokuLink>>() {
			@Override
			public TableRow<DokuLink> call(TableView<DokuLink> table) {
				final TableRow<DokuLink> row = new TableRow<DokuLink>();
				final ContextMenu contextMenu = new ContextMenu();
				
				final MenuItem openMenuItem = new MenuItem("Öffnen...");
				openMenuItem.setOnAction( event -> { 
					dokumentExternAnzeigen(row.getItem()); 	
				});
				final MenuItem validateMenuItem = new MenuItem("Prüfen der Aktualität...");
				validateMenuItem.setOnAction(event -> { 
					managerController.setInfoText("Diese Option ist noch nicht realisiert");
				});
				final MenuItem removeMenuItem = new MenuItem("Löschen");
				removeMenuItem.setOnAction( event -> {
					removeDokuLinkfromList(row.getItem());
				});
				contextMenu.getItems().addAll(openMenuItem, validateMenuItem, removeMenuItem);
				
				row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu)null).otherwise(contextMenu));
				return row;
			}
		});
	}

    private void dokumentExternAnzeigen(DokuLink doku) {
    	
		Repository repository = doku.getRepository();
		try {
			repository.open();
		} catch (SVNException e) {
			managerController.setErrorText("Fehler beim öffen des Repository " + repository.getName() + " :" + e.getMessage());
			return;
		}
		String filePath = repository.getStartPfad() + doku.getPfad() + "/" + doku.getName();
		ByteArrayOutputStream baos = repository.getFileStream(filePath, -1);
		
		int extPos = filePath.indexOf(".");
		String ext = filePath.substring(extPos);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(doku.getName() + ext);
			baos.writeTo(fos);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			managerController.setErrorText(e.getMessage());
			return;
		} finally {
			try {
				if (baos != null) baos.close();
				if (fos != null)  fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				managerController.setErrorText(e.getMessage());
			}
		}
		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
		}
		try {
			desktop.open(new File(doku.getName() + ext));
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			managerController.setErrorText(e.getMessage());
		}
	}
       
    @FXML 
    void actionDocumentContextMenuRequested() {
    	actionNewDokuLink();
    }
    
    @FXML 
    void actionNewDokuLink() {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	DokumentAuswaehlenController controller = managerController.loadDokumentAuswahl(dialog);
    	if (controller != null) {
    		dialog.showAndWait();
    		String userInfo = "Dokumentenauswahl wurde abgebrochen";
    		if (controller.getResponse() == Actions.OK) {
    			DokuLink dokuLink = controller.getSelectedDokuLink();
    			String dokName = dokuLink.getName();
    			userInfo = "Der Verweis auf das Dokument '" + dokName + "' ";
    			if (dokuLinkListContains(dokuLink) == true) {
    				userInfo += "ist bereits vorhanden";
    			} else {
    				dokuLinkList.add(dokuLink);
    				userInfo += "wurde eingetragen";
    			}
    			setChangeFlag(orgIn.inSzenario == null || orgIn.inSzenario.getDokuLink() == null);
    		}
        	managerController.setInfoText(userInfo);
    	}
    }
    private boolean dokuLinkListContains(DokuLink newDokuLink) {
    	String newName = newDokuLink.getName();
    	String newPfad = newDokuLink.getPfad();
    	for (DokuLink dokuLink : dokuLinkList) {
    		if (dokuLink.getName().equals(newName) && dokuLink.getPfad().equals(newPfad)) {
    			return true;
    		}
    	}
    	return false;
    }


	@FXML 
    void actionRemoveDokuLink() {
    	removeDokuLinkfromList(tvDokuLinks.getSelectionModel().selectedItemProperty().get());
    }
    
    private void removeDokuLinkfromList (DokuLink tobeRemoved) {
    	String dokname = tobeRemoved.getName();
    	tvDokuLinks.getItems().remove(tobeRemoved);
    	setChangeFlag(orgIn.inSzenario == null || orgIn.inSzenario.getDokuLink() == null);
    	managerController.setInfoText("Der Verweis auf das Dokument '" + dokname + "' wurde entfernt");
    }
    
    
	private void setupKonfigurationComboBox() {
		
		cmbKonfiguration.disableProperty().bind(cmbInSzenario.getSelectionModel().selectedItemProperty().isNull());
		
		cmbKonfiguration.setCellFactory((cmbBx) -> {
			return new ListCell<Konfiguration>() {
				@Override
				protected void updateItem(Konfiguration item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						setText(item.getName());
					}
				}
			};
		});
		cmbKonfiguration.setConverter(new StringConverter<Konfiguration>() {
			@Override
			public String toString(Konfiguration item) {
				if (item == null) {
					return null;
				} else {
					return item.getName();
				}
			}
			@Override
			public Konfiguration fromString(String string) {
				return null; // No conversion fromString needed
			}
		});

		cmbKonfiguration.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			logger.info("cmbKonfiguration.selected: newValue=" + (newValue==null ? "null" : newValue.getName()));
			aktIn.konfiguration = newValue;
			setChangeFlag(newValue == null || integration == null || integration.get().getKonfiguration() != aktIn.konfiguration);
			managerController.setInfoText("");
		});
		
		// check if user want to change the current entity
		tabPaneInNr.addEventFilter(MouseEvent.MOUSE_PRESSED, event ->  {
			Node node = (Node) event.getTarget();
			if (node instanceof Text) {
				Parent parent = node.getParent();
				if (parent instanceof Label) {
					Label label = (Label) parent;
					// compare label-text with all Integration-Panel-text 
					Integration e = null;
					for(Tab t : tabPaneInNr.getTabs()) {
						if (t.getText() == label.getText()) {
							if (t.getUserData() instanceof Integration) {
								e = (Integration) t.getUserData();
								break;
							}
						}
					}
					if (e != null) {
						event.consume();
						if(checkForChangesAndAskForSave()) {
							managerController.setSelectedIntegration(e);
						}
					}
				} 
			}
			
		});
		
		tabPaneInNr.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			Node node = (Node) event.getTarget();
			if (node instanceof TabPane) {
				Integration e = null;
				TabPane tabPane = (TabPane) node;
				logger.debug(tabPane.getSelectionModel().selectedItemProperty().get());
				e = (Integration) tabPane.getSelectionModel().selectedItemProperty().get().getUserData();
				if (e != null) {
					if(checkForChangesAndAskForSave()) {
						managerController.setSelectedIntegration(e);
					}
				}
			}
		});		
	}
	
	// prüft ob das eingegebene BO (newName) in der BO-Tabelle (businessObjektMap) 
	// bereits vorhanden ist.  

	private String checkBusinessObjectName(String newName, int index) {
		String aktName = null;
		if (newName != null) {
			GeschaeftsObjekt buOb = businessObjectMap.get(newName.toUpperCase());
			if (buOb != null && buOb == orgIn.geschaeftsObjekt[index]) {
				aktIn.geschaeftsObjekt[index] = buOb; 
			} else {
				if (buOb == null) {
					buOb = askForNewBusinessObjektName(newName);
				}	
				if (buOb != null) {
					aktIn.geschaeftsObjekt[index] = buOb; 
					aktName = buOb.getName();
				}
			}
			setChangeFlag(aktIn.geschaeftsObjekt[index] != orgIn.geschaeftsObjekt[index]);
		}
		return aktName;
	}
	
	private GeschaeftsObjekt askForNewBusinessObjektName(String newName) {
		Optional<String> aktName = Dialogs.create()
				.owner(primaryStage)
				.title(IMconstant.APPL_NAME)
				.message("Soll das folgende Geschäftsobjekt gespeichert werden?")
				.showTextInput(newName);
		if (aktName.isPresent()) {
			return (geschaeftsObjektAnlegen(aktName.get()));
		}	
		return null;
	}

	private GeschaeftsObjekt geschaeftsObjektAnlegen(String aktName) {
		
		String select = "SELECT g FROM GeschaeftsObjekt g WHERE g.name= '" + aktName + "'";
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(select, GeschaeftsObjekt.class);
		List<GeschaeftsObjekt> gList = tq.getResultList();
		
		if (gList.size() > 0) {
			managerController.setInfoText("Geschäftsobjekt ist bereits "+ gList.size() +" mal vorhanden");
			return gList.get(0);
		}
		try {
			GeschaeftsObjekt newBusObj = new GeschaeftsObjekt(aktName);
			entityManager.getTransaction().begin();
			entityManager.persist(newBusObj);
			entityManager.getTransaction().commit();
			businessObjectName.add(aktName);
			businessObjectMap.put(aktName.toUpperCase(), newBusObj);
			managerController.setInfoText("Das Geschäftsobjekt \"" + 
					aktName + "\" wurde erfolgreich gespeichert");
			return newBusObj;
		} catch (RuntimeException er) {
			Dialogs.create().owner(primaryStage)
				.title(IMconstant.APPL_NAME)
				.masthead("Datenbankfehler")
				.message("Fehler beim speichern des Geschäftsobjektes")
				.showException(er);
		}
		return null;
	}	

	private void readBusinessObject() {
		logger.entry();
		businessObjectMap.clear();
		businessObjectName.clear();
		TypedQuery<GeschaeftsObjekt> tq = entityManager.createQuery(
				"SELECT g FROM GeschaeftsObjekt g ORDER BY g.name", GeschaeftsObjekt.class);
		final List<GeschaeftsObjekt> gList = tq.getResultList();
		for (GeschaeftsObjekt gObject : gList) {
			businessObjectName.add(gObject.getName());
			businessObjectMap.put(gObject.getName().toUpperCase(), gObject);
		}
		logger.exit(gList.size() + " Geschaefstobjekte");
	}
	
	private void readIntervalle() {
		logger.entry();
		intervallNameList.clear();
		TypedQuery<Intervall> tq = entityManager.createQuery(
				"SELECT i FROM Intervall i ORDER BY i.name", Intervall.class);
		final List<Intervall> iList = tq.getResultList();
		for (Intervall iObject : iList) {
			intervallNameList.add(iObject.getName());
		}
		logger.exit(iList.size() + " Intervalle");
	}
	
	
	private void readInSzenarioList() {
		logger.entry("size vor dem Lesen:" + cmbInSzenario.getItems().size());
		final ObservableList<InSzenario> aktList = FXCollections.observableArrayList();
		TypedQuery<InSzenario> tq = entityManager.createQuery(
				"SELECT i FROM InSzenario i ORDER BY i.name", InSzenario.class);
		
		aktList.addAll(tq.getResultList());
		if (cmbInSzenario.getItems().isEmpty()) {
			cmbInSzenario.setItems(aktList);
		}
		else {
			cmbInSzenario.getItems().retainAll(aktList);
			cmbInSzenario.getItems().setAll(aktList);
		}
		logger.exit(aktList.size() + " Integrationsszenarios");
	}

	private void readCmbKonfigurationList() {
		logger.entry("size vor dem Lesen:" + cmbKonfiguration.getItems().size());
		final ObservableList<Konfiguration> aktList = FXCollections.observableArrayList();
		TypedQuery<Konfiguration> tq = entityManager.createQuery(
				"SELECT k FROM Konfiguration k ORDER BY k.name", Konfiguration.class);
		aktList.addAll(tq.getResultList());
		if (cmbKonfiguration.getItems().isEmpty()) {
			cmbKonfiguration.setItems(aktList);
		}
		else {
			cmbKonfiguration.getItems().retainAll(aktList);
			cmbKonfiguration.getItems().setAll(aktList);
		}
		logger.exit("Es wurden " + aktList.size() + " Eintraege gelesen");
		
//		cmbKonfiguration.getItems().clear();
//		TypedQuery<Konfiguration> tq = entityManager.createQuery(
//				"SELECT k FROM Konfiguration k WHERE k.inSzenario = :i ORDER BY k.name", Konfiguration.class);
//		tq.setParameter("i", inSzenario);
//		final ObservableList<Konfiguration> aktList = FXCollections.observableArrayList(tq.getResultList());
//
//		// find default KONFIGURATION in DB-table
//		Boolean found = false;  
//		for (Konfiguration k : aktList) {
//			if (DEFAULT_KONFIG_NAME.equals(k.getName())) {
//				found = true;
//				break;
//			}
//		}
//		if (!found) {
//			Konfiguration defKonfig = new Konfiguration(DEFAULT_KONFIG_NAME);
//			aktList.add(defKonfig);
//		}
	} 
	
	private void resetEmpfaenger() {
		for (int i=0 ; i < MAX_EMPFAENGER; ++i) {
			aktIn.empfaengerKomponente[i] = null;
		}
		cmbBuOb1.getSelectionModel().select(null);
		cmbBuOb1.getSelectionModel().select(null);
		cmbBuOb3.getSelectionModel().select(null);
		buOb1Exist.set(false);
		buOb2Exist.set(false);
		buOb3Exist.set(false);
		btnEmpfaenger1.setText("");
		btnEmpfaenger2.setText("");
		btnEmpfaenger3.setText("");
		empfaenger1IsSelected.set(false);
		empfaenger2IsSelected.set(false);
		empfaenger3IsSelected.set(false);
	}
	
    private void setAktEmpfaenger() {
    	logger.entry();
		if (aktIn.empfaengerKomponente[0] != null) {
			btnEmpfaenger1.setText(aktIn.empfaengerKomponente[0].getFullname());
			empfaenger1IsSelected.set(true);
			cmbBuOb1.getSelectionModel().select(aktIn.geschaeftsObjekt[0].getName());
			buOb1Exist.set(aktIn.geschaeftsObjekt[0] != null);
		}
		else {
			btnEmpfaenger1.setText("");
			empfaenger1IsSelected.set(false);
			cmbBuOb1.getSelectionModel().select(null);
			buOb1Exist.set(false);
		}
		if (aktIn.empfaengerKomponente[1] != null) {
			btnEmpfaenger2.setText(aktIn.empfaengerKomponente[1].getFullname());
			empfaenger2IsSelected.set(true);
			cmbBuOb2.getSelectionModel().select(aktIn.geschaeftsObjekt[1].getName());
			buOb2Exist.set(aktIn.geschaeftsObjekt[1] != null);
		}
		else {
			btnEmpfaenger2.setText("");
			empfaenger2IsSelected.set(false);
			cmbBuOb2.getSelectionModel().select(null);
			buOb2Exist.set(false);
		}
		if (aktIn.empfaengerKomponente[2] != null) {
			btnEmpfaenger3.setText(aktIn.empfaengerKomponente[2].getFullname());
			empfaenger3IsSelected.set(true);
			cmbBuOb3.getSelectionModel().select(aktIn.geschaeftsObjekt[2].getName());
			buOb3Exist.set(aktIn.geschaeftsObjekt[2] != null);
		}
		else {
			btnEmpfaenger3.setText("");
			empfaenger3IsSelected.set(false);
			cmbBuOb3.getSelectionModel().select(null);
			buOb3Exist.set(false);
		}
		logger.exit();
    }

    @FXML
    void actionAendern(ActionEvent event) {
    	logger.info("TODO");
    }
    
    
    @FXML
    void actionNeuanlage(ActionEvent event) {
    	logger.entry();
    	if (checkForChangesAndAskForSave() == false) {
    		return;
    	}
    	//  
    	if (integration != null) {
    		managerController.setSelectedIntegration(null);
    	}
    	edit.status = Status.NEW;
    	editStatusNew.set(true);
    	editEnabled.set(true);
    	cmbInSzenario.requestFocus();
    	logger.exit();
    }    

    @FXML
    void actionLoeschen(ActionEvent event) {
    	managerController.handleIntegrationLoeschen(event);
    }
    

    private static enum Checkmode { CHECK_ONLY, ASK_FOR_UPDATE, SAVE_DONT_ASK };

    private void setChangeFlag(Boolean different) {
    	if (!different) {
    		different = !checkForChangesWithMode(Checkmode.CHECK_ONLY);
    	}
//    	logger.info("Status dataIsChanged wird auf " + different + " gesetzt");
    	dataIsChanged.set(different);
	}
	
	@FXML
	void actionSpeichern(ActionEvent event) {
		checkForChangesWithMode(Checkmode.SAVE_DONT_ASK);
	}
	
	public boolean checkForChangesAndAskForSave() {
		return checkForChangesWithMode(Checkmode.ASK_FOR_UPDATE);
	}

	private boolean checkForChangesWithMode(Checkmode checkmode) {
		if (integration.get() == null) {
			if (edit.status != Status.NEW) {
				logger.warn("NO INTEGRATION AND status NOT NEW");
				// e.g. first selection
				return true; 
			} 
		}
		if (aktIn.inSzenario == orgIn.inSzenario               &&
			aktIn.sender == orgIn.sender                       &&
			verifyEmpfaengerAreUnchanged() == true             &&
			localDateEquals(aktIn.seitDatum, orgIn.seitDatum)  &&
			localDateEquals(aktIn.bisDatum, orgIn.bisDatum)    &&
			aktIn.bezeichnung.equals(orgIn.bezeichnung)        &&
			aktIn.beschreibung.equals(orgIn.beschreibung)      &&
			aktIn.intervallName.equals(orgIn.intervallName)    &&
			aktIn.anzahlMsg == orgIn.anzahlMsg				   &&
			aktIn.averageByte.get() == orgIn.averageByte.get() &&
			aktIn.maximalByte.get() == orgIn.maximalByte.get() &&
			aktIn.konfiguration == orgIn.konfiguration         &&
			verifyDokuLinkListIsUnchanged() == true			     )
		{
			logger.info(checkmode + ": no change found -> no update");
			return true;  
		}
		if (checkmode == Checkmode.CHECK_ONLY) {
			logger.info(checkmode + ": change found");
			return false; 
		}
		if (checkmode == Checkmode.ASK_FOR_UPDATE) {
			String inBez = aktIn.bezeichnung==null ? "" : aktIn.bezeichnung; 
			Action response = Dialogs.create().owner(primaryStage)
					.title(IMconstant.APPL_NAME)
					.masthead(IMconstant.SICHERHEITSABFRAGE)
					.message("Soll die Änderungen an der Integration " + aktIn.inNr + 
							" \"" + inBez + "\" gespeichert werden?")
					.showConfirm();
			if (response == Dialog.Actions.CANCEL) {
				return false;
			} 
			if (response == Dialog.Actions.NO) {
				logger.info("Aenderungen nicht Speichern ausgewaehlt");
				return true;
			}
		}
		
		// start validation before insert/update
		
    	if (aktIn.sender == null) {
    		Dialogs.create().owner(primaryStage)
    			.title(IMconstant.APPL_NAME)
    			.masthead("Korrektur-Hinweis")
    			.message("Sender ist erforderlich")
    			.showWarning();
    		btnSender.requestFocus();
    		return false;
    	}
    	for (int i=0; i<MAX_EMPFAENGER; ++i) {
    		InKomponente empf = aktIn.empfaengerKomponente[i];
    		if (empf == null) {
    			if (i==0) {
    				String msg = "Ein Empfänger ist erforderlich";
    	    		managerController.setErrorText(msg);
    				btnEmpfaenger1.requestFocus();
    				return false;
    			}
    		} else {    
    			if (aktIn.geschaeftsObjekt[i] == null || 
    				aktIn.geschaeftsObjekt[i].getName().length() < 1) {
    				
    				String msg = "Bitte zum Empfänger \"" + empf.getFullname() 
    					+ "\" auch ein Geschäftsobjekt eintragen/auswählen";
    	    		managerController.setErrorText(msg);
    				switch(i) {
    				case 0: cmbBuOb1.requestFocus(); break;
    				case 1: cmbBuOb2.requestFocus(); break;
    				case 2: cmbBuOb3.requestFocus(); break;
    				}
    				return false;
    			}
    		}
    	}
    	if(aktIn.inSzenario == null) {
    		managerController.setErrorText("Eine InSzenario muss ausgewählt oder angelegt werden");
    		cmbInSzenario.requestFocus();
    		return false;
    	}
//    	if (aktIn.konfiguration == null) {
//    		managerController.setErrorText("Eine Konfiguration muss ausgewählt oder angelegt werden");
//    		cmbKonfiguration.requestFocus();
//    		return false;
//    	}
    	
    	// end of validation -> start update/insert
    	// ----------------------------------------
    	Integration sIntegration = integration.get();
		try {
			entityManager.getTransaction().begin();
			// if configuration changed the Integration must be removed from previous configuration	
			if (orgIn.inSzenario != null && aktIn.inSzenario != orgIn.inSzenario) {
				orgIn.inSzenario.getIntegration().remove(sIntegration);
			}
			if (aktIn.inSzenario.getId() == 0L) {
				entityManager.persist(aktIn.inSzenario);
			}
//			if (aktIn.konfiguration.getId() == 0L) {    	// new configuration for persistence
//				entityManager.persist(aktIn.konfiguration);
//				aktIn.konfiguration.setInSzenario(aktIn.inSzenario);
//	TODO			aktIn.inSzenario.getKonfiguration().add(aktIn.konfiguration);
//			}
			if (sIntegration == null) {
				sIntegration = new Integration();
				entityManager.persist(sIntegration);
			}
			
			int isNr = aktIn.inSzenario.getIsNr();
			if (sIntegration.getInNr() == 0 || (sIntegration.getInNr() / 100) !=  isNr) {
				sIntegration.setInNr(neueInNrErmitteln(aktIn.inSzenario));
			}
			
			if (aktIn.inSzenario.getIntegration().contains(sIntegration) == false) {
				aktIn.inSzenario.getIntegration().add(sIntegration);
			}
			sIntegration.setInSzenario(aktIn.inSzenario);;
			
			updateDokuLinkListInDatabase();
			
			sIntegration.setInKomponente(aktIn.sender); 
			sIntegration.setBeschreibung(aktIn.beschreibung);
			
			Collection<InEmpfaenger> tmpEmpfaengerList = new ArrayList<InEmpfaenger>();
			
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				InEmpfaenger empf = aktIn.empfaenger[i];
				if (empf == null && aktIn.empfaengerKomponente[i] != null) {
					empf = new InEmpfaenger();
					sIntegration.getInEmpfaenger().add(empf);
					entityManager.persist(empf);
				}
				if (aktIn.empfaengerKomponente[i] != null) {
					empf.setIntegration(sIntegration);
					empf.setKomponente(aktIn.empfaengerKomponente[i]);
					empf.setGeschaeftsObjekt(aktIn.geschaeftsObjekt[i]);
					tmpEmpfaengerList.add(empf);
// TODO				empf.getGeschaeftsObjekt().anzVerwendungenProperty().add(1);
				}
			}
			// InEmpfaenger at the original EmpfaengerList must be removed 
			// from the database if they are not in the new EmpfaengerList  
			//
			for (int i=0; i<MAX_EMPFAENGER; ++i) {
				InEmpfaenger empf = orgIn.empfaenger[i];
				if (empf != null && tmpEmpfaengerList.contains(empf) == false) {
					entityManager.remove(empf);
				}
			}
			sIntegration.setInEmpfaenger(tmpEmpfaengerList);
			
//			old: auto generation of field
//			String tmpBezeichnung = aktIn.autoBezeichnung(); 
//			if (aktIn.getBezeichnung().equals(tmpBezeichnung) == false) {
//				aktIn.setBezeichnung(tmpBezeichnung);
//				tfBezeichnung.textProperty().set(aktIn.autoBezeichnung());
//			}
//			new: normal manuell input
//			tmp: set bezeichnung if emppty			
			if (aktIn.bezeichnung == null || 
				aktIn.bezeichnung.isEmpty()) 
			{
				tfBezeichnung.textProperty().set(sIntegration.autobezeichnung(
						aktIn.inSzenario, aktIn.sender,aktIn.geschaeftsObjekt[0]));
			}
			sIntegration.setBezeichnung(aktIn.bezeichnung);
			
			LocalDate aktSeitDatum = dpProduktivSeit.getValue();
			sIntegration.seitDatumProperty().set(aktSeitDatum==null ? "" : aktSeitDatum.toString());
			
			LocalDate aktBisDatum = dpProduktivBis.getValue();
			sIntegration.bisDatumProperty().set(aktBisDatum==null ? "" : aktBisDatum.toString());
			
			sIntegration.setLaeUser(System.getenv("USERNAME").toUpperCase());
			sIntegration.setLaeDatum(LocalDateTime.now().toString());
			
			if (aktIn.intervallName == null) {
				aktIn.intervallName = "";
			}
			if (!aktIn.intervallName.equals(orgIn.intervallName)) {
				sIntegration.setIntervall(newIntervall(aktIn.intervallName));
			}
			sIntegration.setAnzahlMsg(aktIn.anzahlMsg);
			sIntegration.setAverageByte(aktIn.averageByte.get());
			sIntegration.setMaximalByte(aktIn.maximalByte.get());
			
			sIntegration.setKonfiguration(aktIn.konfiguration);
			
			entityManager.getTransaction().commit();
			
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			Dialogs.create().owner(primaryStage)
				.title(IMconstant.APPL_NAME)
				.masthead("Datenbankfehler")
				.message("Fehler beim Speichern der Integration")
				.showException(e);
		}	
		// do things that are necessary after DB-update:
		if (aktIn.intervallName != null) {
			if (!aktIn.intervallName.equals(orgIn.intervallName)) {
				readIntervalle();	// this will remove selection -> refresh 
				m_Intervall.getSelectionModel().select(aktIn.intervallName);
			}
		}
//		setLastChangeField(tfLastChange, sIntegration.getLaeDatum(), sIntegration.getLaeUser());			
		aktIn.setData(sIntegration);
		orgIn.setData(sIntegration);
		
		managerController.loadIntegrationListData();
		managerController.setSelectedIntegration(sIntegration);
		managerController.setInfoText("Die Integration " + sIntegration.inNrStrExp().get() +
				" wurde gespeichert");
		dataIsChanged.set(false);
    	
		return true;
	}	
	
	private int neueInNrErmitteln(InSzenario inSzenario) {
		int isNr = inSzenario.getIsNr();
		return (isNr * 100 + Integration.getMaxInNr(entityManager, isNr) + 1);
	}
	
	private void updateDokuLinkListInDatabase() {
		// if dokuLinks are removed they must be removed from database 
		if (aktIn.inSzenario.getDokuLink() != null) {
			Collection<DokuLink> toBeRemoved = new ArrayList<DokuLink>();
			for (DokuLink dok : aktIn.inSzenario.getDokuLink()) {
				if (dokuLinkList.contains(dok) == false) {
					toBeRemoved.add(dok);
				}
			}
			for (DokuLink dok : toBeRemoved) {
				aktIn.inSzenario.getDokuLink().remove(dok);
				entityManager.remove(dok);
			}
		}
		for (DokuLink dok : dokuLinkList) {
			if (dok.getId() == 0L) {
				entityManager.persist(dok);
			}
			if (aktIn.inSzenario.getDokuLink().contains(dok) == false) {
				aktIn.inSzenario.getDokuLink().add(dok);
			}
		}
	}

	private Intervall newIntervall(String iName) {
		logger.entry();
		Intervall intervall;
		String select = "SELECT i FROM Intervall i WHERE LOWER(i.name) = LOWER(:i)";
		TypedQuery<Intervall> tq = entityManager.createQuery(select, Intervall.class);
		tq.setParameter("i", iName);
		List<Intervall> iList = tq.getResultList();
		
		logger.info("iListe.size=" + iList.size());
		if (iList.size() == 0) {
			intervall = new Intervall();
			entityManager.persist(intervall);
		} else {
			intervall = iList.get(0);
		}
		intervall.setName(iName);
		return intervall;
	}

	private boolean localDateEquals(LocalDate x, LocalDate y) {
		if (x == null && y == null)
			return true;
		if (x != null && y != null) {
			return (x.compareTo(y)==0);
		}
		return false;
	}

	private boolean verifyDokuLinkListIsUnchanged() {
		Collection<DokuLink> orgDokuLink = (orgIn.inSzenario == null) ? null : orgIn.inSzenario.getDokuLink();
		if ( orgDokuLink == null) {
			return dokuLinkList.size() == 0; // org is 0 -> unchanged if dokLinkList == 0 
		}
		if (( orgDokuLink.size()  != dokuLinkList.size() )	  ||
			  orgDokuLink.containsAll(dokuLinkList) == false  ||
			  dokuLinkList.containsAll(orgDokuLink) == false  )
		{
			return false;
		}	
		return true;
	}
	
	private boolean verifyEmpfaengerAreUnchanged () {
		for(int i=0; i < MAX_EMPFAENGER; ++i ) {
			if (aktIn.empfaengerKomponente[i] != orgIn.empfaengerKomponente[i] ||
					aktIn.geschaeftsObjekt[i] != orgIn.geschaeftsObjekt[i]) {
				return false;
			}
		}
		return true;
	}
		
    @FXML
    void actionNewInSzenario(ActionEvent event) {
    	InSzenario newIS = InSzenarioController.neuesInSzenarioAnlegen();
    	if (newIS != null) {
    		readInSzenarioList();
    		cmbInSzenario.getSelectionModel().select(newIS);
    		m_NewConfigurationBtn.requestFocus();
    	}
    }	
    
    @FXML
    void newKonfiguration(ActionEvent event) {
    	String aktName = "";
    	String masterhead = null;
		while (true) {
			Optional<String> newName = Dialogs.create()
				.owner(primaryStage).title(IMconstant.APPL_NAME)
				.masthead(masterhead)
				.message("Wie soll die neue Konfiguration heißen?")
				.showTextInput(aktName);
			if (newName.isPresent() == false) {
				managerController.setInfoText("Neuanlager einer Konfiguartion wurde vom Benutzer abgebrochen");
				break;
			}
			aktName = newName.get().trim();
			if (aktName.length() < 1) {
				masterhead = "Eine Eingabe ist erforderlich!" + 
						  "\n Bitte ändern oder abbrechen";
				continue;
			} 
			String sql="SELECT k FROM Konfiguration k WHERE LOWER(k.name) = LOWER(:n)";
			TypedQuery<Konfiguration> tq = entityManager.createQuery(sql, Konfiguration.class);
			tq.setParameter("n", aktName);
			List<Konfiguration> kList = tq.getResultList();
			
			if (kList.size() > 0) {
				masterhead = "Konfiguration \"" +kList.get(0).getName() +"\" ist bereits vorhanden.\n" + 
						     "Bitte anderen Namen eingeben oder die Neuanlage abbrechen";
				continue;
			}
			try {
				Konfiguration konfiguration = new Konfiguration(aktName);
				entityManager.getTransaction().begin();
				entityManager.persist(konfiguration);
				entityManager.getTransaction().commit();
				
				readCmbKonfigurationList();
				cmbKonfiguration.getSelectionModel().select(konfiguration);
				cmbKonfiguration.requestFocus();
				
				managerController.setInfoText("Die Konfiguartion \"" + aktName + "\"" + 
					" wurde der InSzenario \"" + aktIn.inSzenario.getName()  + "\"" +
					" erfolgreich zugefügt und hier ausgewählt");
				return;
			} catch (RuntimeException er) {
				Dialogs.create().owner(primaryStage)
					.title(IMconstant.APPL_NAME)
					.masthead("Datenbankfehler")
					.message("Fehler beim Anlegen einer neuen Konfiguration")
					.showException(er);
			}
		}	
    }
    
    //Action: Sender-Button is pressed
    @FXML
    void senderButton(ActionEvent event) {
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 100, 250); 
    	if (loader != null) {
    		KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    		komponentenAuswahlController.setKomponente(KomponentenTyp.SENDER, aktIn.sender, entityManager);
    		dialog.showAndWait();
    		if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    			InKomponente selKomponente = komponentenAuswahlController.getSelectedKomponente();
    			if (aktIn.sender != selKomponente ) {
    				aktIn.sender = selKomponente; 
    				btnSender.textProperty().unbind();
    				btnSender.textProperty().bind(aktIn.sender.fullnameProperty());
    				senderIsSelected.set(true);
    			}
    			setChangeFlag(aktIn.sender != orgIn.sender);
    		}
    	}
    }

    //Action: EmpfaengerX-Button pressed
    @FXML
    void empfaengerButton1(ActionEvent event) {
    	String ret = empfaengerButton(0);
    	if (ret != null) {
			btnEmpfaenger1.setText(ret);
			empfaenger1IsSelected.set(true);
    	}
    }	
    @FXML
    void empfaengerButton2(ActionEvent event) {
    	String ret = empfaengerButton(1);
    	if (ret != null) {
			btnEmpfaenger2.setText(ret);
			empfaenger2IsSelected.set(true);
    	}
    }	
    @FXML
    void empfaengerButton3(ActionEvent event) {
    	String ret = empfaengerButton(2);
    	if (ret != null) {
			btnEmpfaenger3.setText(ret);
			empfaenger3IsSelected.set(true);
    	}
    }	
    	
    private String empfaengerButton(int btnNr) {
    	String ret = null;
    	Stage dialog = new Stage(StageStyle.UTILITY);
    	FXMLLoader loader = loadKomponentenAuswahl(dialog, 400, 350); 
    	if (loader!= null) {
    		KomponentenAuswahlController komponentenAuswahlController = loader.getController();
    		komponentenAuswahlController.setKomponente(KomponentenTyp.RECEIVER, aktIn.empfaengerKomponente[btnNr], entityManager);
    		dialog.showAndWait();
    		if (komponentenAuswahlController.getResponse() == Actions.OK ) {
    			if (aktIn.empfaengerKomponente[btnNr] != komponentenAuswahlController.getSelectedKomponente()) {
    				aktIn.empfaengerKomponente[btnNr] = komponentenAuswahlController.getSelectedKomponente();
    				ret = aktIn.empfaengerKomponente[btnNr].getFullname();
    			}
    			setChangeFlag(aktIn.empfaengerKomponente[btnNr] != orgIn.empfaengerKomponente[btnNr]);
    		}	
    	}
    	return ret;
    }

	@FXML
    void actionEmpfaenger2loeschen(ActionEvent event) {
		// if no.2 (line 3) exist --> move no.2 to no.1
    	if (aktIn.empfaengerKomponente[2] != null) {
    		aktIn.empfaengerKomponente[1] = aktIn.empfaengerKomponente[2];
    		aktIn.empfaengerKomponente[2] = null;
    		btnEmpfaenger2.setText(aktIn.empfaengerKomponente[1].getFullname());
    		
			aktIn.geschaeftsObjekt[1] = aktIn.geschaeftsObjekt[2];  // businessObjectMap.get(aktIn.busObjName[2].toUpperCase()).getName();
			aktIn.geschaeftsObjekt[2] = null;
			cmbBuOb2.getSelectionModel().select(aktIn.geschaeftsObjekt[1].getName());
			
	    	btnEmpfaenger3.setText("");
	    	cmbBuOb3.getSelectionModel().select(null);
    		buOb3Exist.set(false);
    		empfaenger3IsSelected.set(false);
    	} 
    	else { // if no.2 (line 3) is empty -> just delete no.2
    		aktIn.empfaengerKomponente[1] = null;
    		btnEmpfaenger2.setText("");
    		aktIn.geschaeftsObjekt[1] = null;
	    	cmbBuOb2.getSelectionModel().select(null);
    		buOb2Exist.set(false);
    		empfaenger2IsSelected.set(false);
    	}
    	dataIsChanged.set(true);
    }
    
    @FXML
    void actionEmpfaenger3loeschen(ActionEvent event) {
    	aktIn.empfaengerKomponente[2] = null;
    	btnEmpfaenger3.setText("");
    	aktIn.geschaeftsObjekt[2] = null;
		cmbBuOb3.getSelectionModel().select(null);
		empfaenger3IsSelected.set(false);
		buOb3Exist.set(false);
    	dataIsChanged.set(true);
    }
    
    final Pattern pattern = Pattern.compile("^\\d*\\.?\\d*$");
    final TextField tf = new TextField() {
       @Override
       public void replaceText(int start, int end, String text) {
           String newText = getText().substring(0, start)+text+getText().substring(end);
            if (pattern.matcher(newText).matches()) {
                super.replaceText(start, end, text);
            }
        }

        @Override
        public void replaceSelection(String text) {
            int start = getSelection().getStart();
            int end = getSelection().getEnd();
            String newText = getText().substring(0, start)+text+getText().substring(end);
            if (pattern.matcher(newText).matches()) {
                super.replaceSelection(text);
            }
        }
    };
    
    
    private FXMLLoader loadKomponentenAuswahl(Stage dialog, int xOffset, int yOffset) {
    	FXMLLoader loader = new FXMLLoader();
    	String fullName = "subs/KomponentenAuswahl.fxml";
  		loader.setLocation(getClass().getResource(fullName));
    	if (loader.getLocation()==null) {
    		logger.error("Resource not found :" + fullName);
    		managerController.setErrorText("FEHLER: Resource ("+fullName+") not found");
    		return null;
    	}
    	try {
    		loader.load();
    	} catch (IOException e) {
    		e.printStackTrace(); 
    	}
    	Parent root = loader.getRoot();
    	Scene scene = new Scene(root);
    	dialog.initModality(Modality.APPLICATION_MODAL);
    	dialog.initOwner(primaryStage);
    	dialog.setTitle(primaryStage.getTitle());
    	dialog.setScene(scene);
    	dialog.setX(primaryStage.getX() + xOffset);
    	dialog.setY(primaryStage.getY() + yOffset);
		return loader;
	}
    
	public final ObjectProperty<Integration> integrationProperty() {
		return integration;
	}
	
	public final Integration getIntegration() {
		return integration.get() ;
	}
	
	public final void setIntegration(Integration integration) {
		this.integration.set(integration);
	}

    private void checkFieldFromView() {
        assert m_InSzenarioPane		!= null : "fx:id=\"m_InSzenarioPane\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_IntegrationPane    != null : "fx:id=\"m_IntegrationPane\"    was not injected: check your FXML file 'Integration.fxml'.";
        assert m_SpeichernBtn       != null : "fx:id=\"m_SpeichernBtn\"       was not injected: check your FXML file 'Integration.fxml'.";
        assert m_NewInSzenarioBtn  	!= null : "fx:id=\"m_NewInSzenarioBtn\"   was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbKonfiguration     != null : "fx:id=\"cmbKonfiguration\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_NewDokuLinkBtn     != null : "fx:id=\"m_NewDokuLinkBtn\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_RemoveDokuLinkBtn  != null : "fx:id=\"m_RemoveDokuLinkBtn\"  was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentName     != null : "fx:id=\"tColDokumentName\"     was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentVorhaben != null : "fx:id=\"tColDokumentVorhaben\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentDatum    != null : "fx:id=\"tColDokumentDatum\"    was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentQuelle   != null : "fx:id=\"tColDokumentQuelle\"   was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentRevision != null : "fx:id=\"tColDokumentRevision\" was not injected: check your FXML file 'Integration.fxml'.";
        assert tColDokumentPfad     != null : "fx:id=\"tColDokumentPfad\"     was not injected: check your FXML file 'Integration.fxml'.";

        assert taBeschreibung	 	 != null : "fx:id=\"taBeschreibung\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert integration 			 != null : "fx:id=\"integration\" 		     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_Intervall	 	 	 != null : "fx:id=\"m_Intervall\" 		     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_AnzahlMsg		 	 != null : "fx:id=\"m_AnzahlMsg\"		     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_GroesseMsg	 	 	 != null : "fx:id=\"m_GroesseMsg\" 		     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_GroesseEinheit	 	 != null : "fx:id=\"m_GroesseEinheit\"	     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_MaxGroesseMsg	 	 != null : "fx:id=\"m_MaxGroesseMsg\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert m_MaxGroesseEinheit	 != null : "fx:id=\"m_MaxGroesseEinheit\"    was not injected: check your FXML file 'Integration.fxml'.";
        assert btnEmpfaenger1 		 != null : "fx:id=\"btnEmpfaenger1\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert btnEmpfaenger2		 != null : "fx:id=\"btnEmpfaenger2\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert btnEmpfaenger3		 != null : "fx:id=\"btnEmpfaenger3\"         was not injected: check your FXML file 'Integration.fxml'.";
        assert m_NewConfigurationBtn != null : "fx:id=\"m_NewConfigurationBtn\"  was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbInSzenario		 != null : "fx:id=\"cmbInSzenario\"          was not injected: check your FXML file 'Integration.fxml'.";
        assert tfBezeichnung		 != null : "fx:id=\"tfBezeichnung\" 		 was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbBuOb1				 != null : "fx:id=\"cmbBuOb1\" 			     was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbBuOb2				 != null : "fx:id=\"cmbBuOb2\" 			     was not injected: check your FXML file 'Integration.fxml'.";
        assert cmbBuOb3				 != null : "fx:id=\"cmbBuOb3\" 			     was not injected: check your FXML file 'Integration.fxml'.";
        assert btnSender			 != null : "fx:id=\"btnSender\" 			 was not injected: check your FXML file 'Integration.fxml'.";
        assert mbtEmpfaenger2		 != null : "fx:id=\"mbtEmpfaenger2\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert mbtEmpfaenger3		 != null : "fx:id=\"mbtEmpfaenger3\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert tfLastChange			 != null : "fx:id=\"tfLastChange\" 		 	 was not injected: check your FXML file 'Integration.fxml'.";
        assert dpProduktivSeit		 != null : "fx:id=\"dpProduktivSeit\" 	     was not injected: check your FXML file 'Integration.fxml'.";
        assert dpProduktivBis		 != null : "fx:id=\"dpProduktivBis\" 	     was not injected: check your FXML file 'Integration.fxml'.";
    }
}

