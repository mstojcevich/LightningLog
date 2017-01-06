package org.usfirst.team862.robolog.viewer;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.usfirst.team862.robolog.shared.LogHeader;
import org.usfirst.team862.robolog.shared.LoggerEvent;
import org.usfirst.team862.robolog.shared.LoggerEventType;
import org.usfirst.team862.robolog.shared.RioData;

import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Controller {

    @FXML
    private TableColumn<Map.Entry<String, String>, String> customPropKeyCol, customPropValCol;

    @FXML
    private ListView<LoggerEvent> eventList;

    @FXML
    private TableView<Map.Entry<String, String>> customPropTable;

    @FXML
    private RadioButton digital0Btn, digital1Btn, digital2Btn, digital3Btn, digital4Btn, digital5Btn, digital6Btn, digital7Btn, digital8Btn, digital9Btn, relay0Btn;

    @FXML
    private RadioButton relay1Btn, relay2Btn, relay3Btn;

    @FXML
    private RadioButton brownoutBtn;

    @FXML
    private ProgressBar batteryVoltageBar;

    @FXML
    private ProgressBar analog0Bar, analog1Bar, analog2Bar, analog3Bar;

    @FXML
    private TextArea stackTextArea;

    @FXML
    private TextField codeStartupTimeField, codeDeployTimeField, rioBootTimeField;

    @FXML
    private Label eventTypeLabel, eventTitleLabel, eventDescLabel, matchTimeLabel;

    @FXML
    private CheckBox showErrors, showWarnings, showExceptions, showInfo, showOther;

    @FXML
    private TextField searchField;

    @FXML
    private AnchorPane mainPane;

    private RadioButton[] digitalButtons;
    private RadioButton[] relayButtons;
    private ProgressBar[] analogBars;

    private List<LoggerEvent> events;

    @FXML
    public void initialize() {
        eventList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> setToEvent(newValue));
        eventList.setCellFactory(new EventCellFactory());
        digitalButtons = new RadioButton[] { digital0Btn, digital1Btn, digital2Btn, digital3Btn, digital4Btn, digital5Btn, digital6Btn, digital7Btn, digital8Btn, digital9Btn };
        relayButtons = new RadioButton[] { relay0Btn, relay1Btn, relay2Btn, relay3Btn };
        analogBars = new ProgressBar[] { analog0Bar, analog1Bar, analog2Bar, analog3Bar };

        customPropKeyCol.setCellValueFactory((p) -> new SimpleStringProperty(p.getValue().getKey()));

        writeTestLog(new File("testLog.llog.gz"), 1000);

        mainPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if(db.hasFiles()) { // load in the first file
                loadFile(db.getFiles().get(0));
            }

            event.setDropCompleted(true);
            event.consume();
        });

        mainPane.setOnDragOver(event -> {
            if(event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });
    }

    @FXML
    public void openLogFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Log File");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Compressed RoboLog Files (.llog.gz)", "*.llog.gz")
        );

        File chosen = chooser.showOpenDialog(Main.mainStage);

        loadFile(chosen);
    }

    void loadFile(File f) {
        try (
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                GZIPInputStream gis = new GZIPInputStream(bis);
                ObjectInputStream ois = new ObjectInputStream(gis);
        ) {
            LogHeader header = (LogHeader) ois.readObject();

            this.events = new LinkedList<>();
            try {
                while(true) {
                    events.add((LoggerEvent) ois.readObject());
                }
            } catch(EOFException ignored) {} // Reached the end of file

            loadLogHeader(header);
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, String.format("Failed to load log file at %s. Looks corrupt or wrong format.", f.getPath())).show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, String.format("Failed to load log file at %s", f.getPath())).show();
        }
    }

    private void setToEvent(LoggerEvent event) {
        if(event == null)
            return;

        updateRioData(event.getRioData());
        updateCustomTable(event.getCustomProperties());

        batteryVoltageBar.setProgress(event.getBatteryVoltage()/13f);
        batteryVoltageBar.setTooltip(new Tooltip(String.format("%.2f", event.getBatteryVoltage())));

        brownoutBtn.setSelected(event.getBrownout());

        stackTextArea.setText(event.getStackTrace());
        eventTypeLabel.setText(event.getType().name());
        eventTitleLabel.setText(event.getTitle());
        eventDescLabel.setText(event.getDescription());
        long matchTimeSeconds = (long) event.getMatchTime();
        matchTimeLabel.setText(String.format("%d minutes, %d seconds", TimeUnit.SECONDS.toMinutes(matchTimeSeconds),
                matchTimeSeconds % 60));
    }

    private boolean matchesFilters(LoggerEvent event) {
        if(!showErrors.isSelected() && event.getType() == LoggerEventType.ERROR) {
            return false;
        }
        if(!showWarnings.isSelected() && event.getType() == LoggerEventType.WARNING) {
            return false;
        }
        if(!showExceptions.isSelected() && event.getType() == LoggerEventType.EXCEPTION) {
            return false;
        }
        if(!showInfo.isSelected() && event.getType() == LoggerEventType.INFO) {
            return false;
        }
        if(!showOther.isSelected() && event.getType() == LoggerEventType.OTHER) {
            return false;
        }

        String searchText = searchField.getText().toLowerCase();
        if(searchText.length() != 0 && !(event.getTitle().toLowerCase().contains(searchText)
                || event.getDescription().toLowerCase().contains(searchText))) {
            return false;
        }

        return true;
    }

    @FXML
    private void refreshShownEvents() {
        ObservableList<LoggerEvent> shownEvents = FXCollections.observableArrayList();

        if(events != null) {
            for (LoggerEvent event : events) {
                if(matchesFilters(event)) {
                    shownEvents.add(event);
                }
            }
        }

        eventList.setItems(shownEvents);
    }

    private void loadLogHeader(LogHeader logFile) {
        // Copy over the event list
        refreshShownEvents();

        DateFormat datetimeFmt = DateFormat.getInstance();

        // Set the log file properties
        codeStartupTimeField.setText(datetimeFmt.format(logFile.getRobotCodeStartTime()));
        codeDeployTimeField.setText(datetimeFmt.format(logFile.getDeployTime()));
        rioBootTimeField.setText(datetimeFmt.format(logFile.getRioUpSince()));
    }

    private void updateCustomTable(Map<String, String> table) {
        customPropTable.setItems(FXCollections.observableArrayList(table.entrySet()));
        customPropKeyCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
        customPropValCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue()));
    }

    private void updateRioData(RioData rd) {
        // Update digital buttons
        for(int i = 0; i < digitalButtons.length; i++) {
            digitalButtons[i].setSelected(rd.getDigital(i));
        }

        // Update analog bars
        for(int i = 0; i < analogBars.length; i++) {
            analogBars[i].setProgress(rd.getAnalog(i)/5f);
            analogBars[i].setTooltip(new Tooltip(String.format("%.2f", rd.getAnalog(i))));
        }

        // Update relay buttons
        for(int i = 0; i < relayButtons.length; i++) {
            relayButtons[i].setSelected(rd.getRelay(i));
        }
    }

    private void writeTestLog(File f, int numEvents) {
        String[] names = {
                "Test event",
                "Auton debug",
                "Periodic test",
                "We messed up"
        };

        String[] descriptions = {
                "Something went very very very wrong",
                "The quick brown fox jumps over the lazy dog",
                "Everything is broken... everything."
        };

        String[] autonModes = {
                "Deathspin",
                "Do nothing",
                "Two ball auton but your partners instead of balls",
                "Do less than nothing"
        };

        try (
                FileOutputStream fos = new FileOutputStream(f);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                GZIPOutputStream gos = new GZIPOutputStream(bos);
                ObjectOutputStream oos = new ObjectOutputStream(gos)
        ) {
            LogHeader header = new LogHeader();

            oos.writeObject(header);

            Random rng = new Random();

            Map<String, String> customProps = new HashMap<>();
            customProps.put("Testing property", "test");

            for(int i = 0; i < numEvents; i++) {
                customProps.put("Auton mode", autonModes[rng.nextInt(autonModes.length)]);

                LoggerEvent event = new LoggerEvent(
                        LoggerEventType.values()[rng.nextInt(LoggerEventType.values().length)],
                        names[rng.nextInt(names.length)],
                        descriptions[rng.nextInt(descriptions.length)],
                        new Date(System.currentTimeMillis() + i*1000),
                        0, 0, false,
                        new RioData(rng.nextDouble()*5, rng.nextDouble()*5, rng.nextDouble()*5, rng.nextDouble()*5,
                                rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(),
                                rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean(), rng.nextBoolean()),
                        customProps,
                        ""
                );

                oos.writeObject(event);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
