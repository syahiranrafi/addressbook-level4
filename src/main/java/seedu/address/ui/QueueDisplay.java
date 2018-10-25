package seedu.address.ui;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import seedu.address.MainApp;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.QueueUpdatedEvent;
import seedu.address.model.PatientQueue;
import seedu.address.model.ServedPatientList;
import seedu.address.model.person.CurrentPatient;
import seedu.address.model.person.Patient;
import seedu.address.model.person.ServedPatient;

/**
 * A display panel that shows the queue information to the user.
 */
public class QueueDisplay extends UiPart<Region> {

    public static final String DEFAULT_PAGE = "QueueDisplay.html";

    private static final String FXML = "QueueDisplay.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    @FXML
    private WebView display;

    private static int counter = 0;

    public QueueDisplay() {
        super(FXML);

        // To prevent triggering events for typing inside the loaded Web page.
        getRoot().setOnKeyPressed(Event::consume);

        loadQueueDisplay(null, null, null);
        registerAsAnEventHandler(this);
    }

    public void loadPage(String url) {
        Platform.runLater(() -> display.getEngine().load(url));
    }

    private void runScript(String script, int scriptCounter) {
        display.getEngine().getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {
                    if (newValue != Worker.State.SUCCEEDED) {
                        // Browser not loaded, return.
                        return;
                    }
                    if (this.counter != scriptCounter) {
                        return;
                    }
                    System.out.println("Script to be run: " + script);
                    Platform.runLater(() -> display.getEngine().executeScript(script));
                    this.counter++;
                });
    }

    /**
     * Loads a HTML file representing the queue display.
     */
    private void loadQueueDisplay(PatientQueue patientQueue, ServedPatientList servedPatientList,
                                  CurrentPatient currentPatient) {
        List<Patient> patientQueueList = patientQueue == null ? null : patientQueue.getPatientsAsList();
        String currentPatientString;
        if (currentPatient == null) {
            currentPatientString = "empty";
        } else {
            try {
                currentPatientString = currentPatient.getPatient().getName().fullName;
            } catch (NullPointerException npe) {
                currentPatientString = "empty";
            }
        }
        List<ServedPatient> servedPatients = servedPatientList == null ? null : servedPatientList.getPatientsAsList();

        String queueDisplayPage = MainApp.class.getResource(FXML_FILE_FOLDER + DEFAULT_PAGE).toExternalForm();
        runScript(getScriptForQueueDisplay(patientQueueList, currentPatientString, servedPatients), counter);
        loadPage(queueDisplayPage);
    }



    private String getScriptForQueueDisplay(List<Patient> patientQueueList, String currentPatientString,
                                            List<ServedPatient> servedPatientList) {
        String script = "loadQueueDisplay(";
        for (int index = 0; index < 6; index++) {
            try {
                script += "'";
                script += patientQueueList.get(index).getIcNumber().value.substring(5, 9);
            } catch (IndexOutOfBoundsException ioobe) {
                script += "empty";
            } catch (NullPointerException npe) {
                script += "empty";
            } finally {
                script += "', ";
            }
        }
        script = script.substring(0, script.length() - 2);

        script += ", '";
        script += currentPatientString;
        script += "', ";

        for (int index = 0; index < 6; index++) {
            try {
                script += "'";
                script += servedPatientList.get(index).getIcNumber().value.substring(5, 9);
            } catch (IndexOutOfBoundsException ioobe) {
                script += "empty";
            } catch (NullPointerException npe) {
                script += "empty";
            } finally {
                script += "', ";
            }
        }
        script = script.substring(0, script.length() - 2);

        script += ");";
        System.out.println(script);
        return script;
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeResources() {
        display = null;
    }

    @Subscribe
    private void handleQueueUpdatedEvent(QueueUpdatedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        loadQueueDisplay(event.getPatientQueue(), event.getServedPatientList(), event.getCurrentPatient());
    }

    /**
     * Generate a list of patient queues separated with newline.
     * @param patientQueue list of patients waiting for doctor.
     * @return string representation of the list.
     */
    private String generatePatientQueuePrettyString(PatientQueue patientQueue) {
        if (patientQueue == null) {
            return "(none)";
        }
        int counter = 1;
        String result = "";
        for (Patient patient: patientQueue.getPatientsAsList()) {
            result += counter++ + ".) ";
            result += patient.toNameAndIc();
            result += "<br>";
        }
        return result;
    }

    /**
     * Generates URL parameters representing the patients in the list.
     * @param list to convert to string.
     * @return url string addon.
     */
    private String generateUrlParamsFromPatientQueue(List<Patient> list) {
        String result = "";
        for (int index = 0; index < 6; index++) {
            result += "queue";
            result += (index + 1);
            result += "=";
            try {
                result += list.get(index).getIcNumber().toString().substring(5, 9); //need to change
            } catch (IndexOutOfBoundsException ioobe) {
                result += "empty";
            } catch (NullPointerException npe) {
                result += "empty";
            }
            result += "&";
        }
        return result;
    }

    /**
     * Generate a list of patient queues separated with newline.
     * @param servedPatientList list of served patients.
     * @return string representation of the list.
     */
    private String generateServedPatientListPrettyString(ServedPatientList servedPatientList) {
        if (servedPatientList == null) {
            return "(none)";
        }

        int counter = 1;
        String result = "";
        for (ServedPatient patient: servedPatientList.getPatientsAsList()) {
            result += counter++ + ".) ";
            result += patient.toNameAndIc();
            result += "<br>";
        }
        return result;
    }

    /**
     * Generates URL parameters representing the patients in the list.
     * @param list to convert to string.
     * @return url string addon.
     */
    private String generateUrlParamsFromServedPatientList(List<ServedPatient> list) {
        String result = "";
        for (int index = 0; index < 6; index++) {
            result += "served";
            result += (index + 1);
            result += "=";
            try {
                result += list.get(index).getIcNumber().toString().substring(5, 9); //need to change
            } catch (IndexOutOfBoundsException ioobe) {
                result += "empty";
            } catch (NullPointerException npe) {
                result += "empty";
            }
            result += "&";
        }
        return result;
    }
}
