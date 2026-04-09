package de.rwth_aachen.phyphox;

import android.app.Activity;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.collection.ArraySet;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// Simplified: removed Bluetooth, Camera, NetworkConnection imports

//This class holds all the information that makes up an experiment
//There are also some functions that the experiment should perform
public class PhyphoxExperiment implements Serializable, ExperimentTimeReference.Listener {
    int versionMinor;
    int versionMajor;

    boolean loaded = false; //Set to true if this instance holds a successfully loaded experiment
    boolean isLocal; //Set to true if this experiment was loaded from a local file. (if false, the experiment can be added to the library)
    byte[] source = null; //This holds the original source file
    Set<String> resources = new ArraySet<>();
    String resourceFolder = null;
    long crc32 = 0;
    String message = ""; //Holds error messages
    String title = ""; //The title of this experiment
    String baseTitle = ""; //The title of this experiment without translations
    String stateTitle = ""; //The title of this experiment
    String category = ""; //The category of this experiment
    String baseCategory = ""; //The category of this experiment without translations
    String icon = ""; //The icon. This is either a base64-encoded drawable (typically png) or (if its length is 3 or less characters) it is a short form which should be used in a simple generated logo (like "gyr" for gyroscope). (The experiment list will use the first three characters of the title if this is completely empty)
    String description = "There is no description available for this experiment."; //A long text, explaining details about the experiment
    public Map<String, String> links = new LinkedHashMap<>(); //This contains links to external documentation or similar stuff
    public Map<String, String> highlightedLinks = new LinkedHashMap<>(); //This contains highlighted (= showing up in the menu) links to external documentation or similar stuff
    public Vector<ExpView> experimentViews = new Vector<>(); //Instances of the experiment views (see expView.java) that define the views for this experiment
    public ExperimentTimeReference experimentTimeReference; //This class holds the time of the first sensor event as a reference to adjust the sensor time stamp for all sensors to start at a common zero
    public Vector<SensorInput> inputSensors = new Vector<>(); //Instances of sensorInputs (see sensorInput.java) which are used in this experiment
    // Simplified: removed depthInput, cameraInput, gpsIn, bluetoothInputs, bluetoothOutputs
    public final Vector<DataBuffer> dataBuffers = new Vector<>(); //Instances of dataBuffers (see dataBuffer.java) that are used to store sensor data, analysis results etc.
    public final Map<String, Integer> dataMap = new HashMap<>(); //This maps key names (string) defined in the experiment-file to the index of a dataBuffer
    public Vector<Analysis.AnalysisModule> analysis = new Vector<>(); //Instances of analysisModules (see analysis.java) that define all the mathematical processes in this experiment
    public Lock dataLock = new ReentrantLock();

    double analysisSleep = 0.; //Pause between analysis cycles. At 0 analysis is done as fast as possible.
    DataBuffer analysisDynamicSleep = null;
    double lastAnalysis = 0.0; //This variable holds the system time of the moment the last analysis process finished. This is necessary for experiments, which do analysis after given intervals
    double analysisTime; //This variable holds the experiment time of the moment the current analysis process started.
    double analysisLinearTime; //Same with the current system time
    boolean analysisOnUserInput = false; //Do the data analysis only if there is fresh input from the user.
    boolean newUserInput = true; //Will be set to true if the user changed any values
    DataBuffer requireFill = null; //Observe this buffer and only execute analysis cycle if this buffer has enough values
    int requireFillThreshold = 1; //Threshold fore 'requireBuffer'
    DataBuffer requireFillDynamic = null; //Instead of using the threshold, use the last value of another buffer to control 'requireFill'
    boolean newData = true; //Will be set to true if we have fresh data to present
    boolean recordingUsed = true; //This keeps track, whether the recorded data has been used, so the next call reading from the mic can clear the old data first

    int cycle = 0; //Keeps track of the current cycle for the cycles attribute of analysis modules

    boolean timedRun = false; //Timed run enabled?
    double timedRunStartDelay = 3.; //Start delay for timed runs
    double timedRunStopDelay = 10.; //Stop delay for timed runs

    // Simplified: removed audioOutput, audioRecord, networkConnections

    public DataExport exporter; //An instance of the DataExport class for exporting functionality (see DataExport.java)

    //The constructor will just instantiate the DataExport. Everything else will be set directly by the phyphoxFile loading function (see phyphoxFile.java)
    PhyphoxExperiment() {
        exporter = new DataExport(this);
        experimentTimeReference = new ExperimentTimeReference(this);
    }

    public void onExperimentTimeReferenceUpdated(ExperimentTimeReference experimentTimeReference) {
        for (ExpView ev : experimentViews) {
            for (ExpView.expViewElement eve : ev.elements) {
                eve.onTimeReferenceUpdate(experimentTimeReference);
            }
        }
    }

    //Create a new buffer
    public DataBuffer createBuffer(String key, int size, ExperimentTimeReference experimentTimeReference) {
        if (key == null)
            return null;

        DataBuffer output = new DataBuffer(key, size, experimentTimeReference);
        dataBuffers.add(output);
        dataMap.put(key, dataBuffers.size() - 1);
        return output;
    }

    //Helper function to get a dataBuffer by its key name
    public DataBuffer getBuffer(String key) {
        Integer index = this.dataMap.get(key);
        //Some sanity checks. To make this function more fail-safe
        if (index == null)
            return null;
        if (index >= dataBuffers.size())
            return null;

        return dataBuffers.get(index);
    }

    //Do the export using the DataExport class (see DataExport.java)
    public void export(Activity c) {
        exporter.export(c, false);
    }



    //This function gets called in the main loop and takes care of any inputElements in the current experiment view
    public void handleInputViews(boolean measuring) {
        if (!loaded)
            return;
        if (dataLock.tryLock()) {
            try {
                for (ExpView ev : experimentViews) {
                        for (ExpView.expViewElement eve : ev.elements) {
                            try {
                                if (eve.onMayWriteToBuffers(this)) //The element may now write to its buffers if it wants to do it on its own...
                                    newUserInput = true;
                            } catch (Exception e) {
                                Log.e("handleInputViews", "Unhandled exception in view module (input) " + eve.toString() + " while sending data.", e);
                            }
                        }
                }
            } finally {
                dataLock.unlock();
            }
        }
        //else: Lock not aquired, but this is not urgent. Try another time instead of blocking the UI thread
        if (newUserInput && !measuring)
            processAnalysis(false);
    }

    //called by th main loop to initialize the analysis process
    public void processAnalysis(boolean measuring) {
        if (!loaded)
            return;

        // Simplified: removed network and audio recording data handling

        Double sleep = analysisSleep;
        if (analysisDynamicSleep != null && !Double.isNaN(analysisDynamicSleep.value) && !Double.isInfinite(analysisDynamicSleep.value)) {
            sleep = analysisDynamicSleep.value;
        }

        //Check if the actual math should be done
        if (!newUserInput) {
            if (analysisOnUserInput) {
                //If set by the experiment, the analysis is only done when there is new input from the user
                return; //No new input. Nothing to do.
            } else if (measuring && experimentTimeReference.getExperimentTime() - lastAnalysis <= sleep) {
                //This is the default: The analysis is done periodically. Either as fast as possible or after a period defined by the experiment
                return; //Too soon. Nothing to do
            }
        }
        newUserInput = false;

        if (measuring) {
            for (SensorInput sensor : inputSensors)
                sensor.updateGeneratedRate();
        } else
            cycle = 0;

        if (requireFill != null && lastAnalysis != 0) {
            int threshold = requireFillThreshold;
            if (requireFillDynamic != null && requireFillDynamic.getFilledSize() > 0)
                threshold = (int)requireFillDynamic.value;
            if (requireFill.getFilledSize() < threshold) {
                return;
            }
        }

        dataLock.lock();
        analysisTime = experimentTimeReference.getExperimentTime();
        analysisLinearTime = experimentTimeReference.getLinearTime();

        //Call all the analysis modules and let them do their work.
        try {
            for (Analysis.AnalysisModule mod : analysis) {
                try {
                    Thread.yield();
                    mod.updateIfNotStatic(cycle);
                } catch (Exception e) {
                    Log.e("processAnalysis", "Unhandled exception in analysis module " + mod.toString() + ".", e);
                }
            }
        } finally {
            dataLock.unlock();
        }
        cycle++;

        // Simplified: removed audio playback, bluetooth, network data handling

        // recordingUsed = true; // Removed with audio recording
        newData = true; //We have fresh data to present.
        lastAnalysis = experimentTimeReference.getExperimentTime(); //Remember when we were done this time
    }

    //called by the main loop after everything is processed. Here we have to send all the analysis results to the appropriate views
    public boolean updateViews(int currentView, boolean force) {
        if (!loaded)
            return true;
        if (!(newData || force)) //New data to present? If not: Nothing to do, unless an update is forced
            return true;

        try {
            if (dataLock.tryLock(10, TimeUnit.MILLISECONDS)) {
                try {
                    for (ExpView experimentView : experimentViews) {
                        for (ExpView.expViewElement eve : experimentView.elements) {
                            eve.onMayReadFromBuffers(this); //Notify each view, that it should update from the buffers
                        }
                    }
                } finally {
                    dataLock.unlock();
                }
            } else
                return false; //This is not urgent. Try another time instead of blocking the UI thread!
        } catch (Exception e) {
            Log.d("updateViews", e.getMessage());
            return false;
        }

        newData = false;
        //Finally call dataComplete on every view to notify them that the data has been sent - heavy operation can now be done by the views while the buffers have been unlocked again
        for (ExpView.expViewElement eve : experimentViews.elementAt(currentView).elements) {
            try {
                eve.dataComplete();
            } catch (Exception e) {
                Log.e("updateViews", "Unhandled exception in view module " + eve.toString() + " on data completion.", e);
            }
        }
        return true;
    }

    //Helper to stop all I/O of this experiment (i.e. when it should be stopped)
    public void stopAllIO() {
        if (!loaded)
            return;

        ExperimentTimeReference.TimeMapping event = experimentTimeReference.timeMappings.size() > 0 ? experimentTimeReference.timeMappings.get(experimentTimeReference.timeMappings.size() - 1) : null;
        if (event == null || event.event != ExperimentTimeReference.TimeMappingEvent.CLEAR)
            experimentTimeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.PAUSE);
        event = experimentTimeReference.timeMappings.size() > 0 ? experimentTimeReference.timeMappings.get(experimentTimeReference.timeMappings.size() - 1) : null;
        lastAnalysis = 0.0;

        // Simplified: removed audio recording, playback, gps, depth, camera, network, bluetooth

        //Sensors
        for (SensorInput sensor : inputSensors)
            sensor.stop();

    }


    //Helper to start all I/O of this experiment (i.e. when it should be started)
    public void startAllIO() {

        if (!loaded)
            return;

        experimentTimeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.START);

        newUserInput = true; //Set this to true to execute analysis at least ones with default values.

        for (SensorInput sensor : inputSensors)
            sensor.start();

        // Simplified: removed gps, depth, camera, audio, bluetooth, network

    }

    public void init(SensorManager sensorManager, LocationManager locationManager) throws Exception {
        //Update all the views
        for (int i = 0; i < experimentViews.size(); i++) {
            updateViews(i, true);
        }

        // Simplified: removed audio output init and audio record creation

        //Reconnect sensors
        for (SensorInput si : inputSensors) {
            si.attachSensorManager(sensorManager);
        }

        // Simplified: removed gps init

    }

    public void writeStateFileAsync(String customTitle, OutputStream os, Experiment.WriteStateFileCallback writeStateFileCallback){

        ExecutorService stateWriterExecutor = Executors.newSingleThreadExecutor();

        Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        stateWriterExecutor.execute(() -> {
            String result = null;

            try {
                result = writeStateFile(customTitle, os);
            } catch (Exception e){
                result = e.getMessage();
            } finally {
                try {
                    os.close();
                } catch (IOException e){
                    e.printStackTrace();
                    if (result == null)
                        result = "Failed to close stream: " +e.getMessage();
                }
            }

            String finalResult = result;

            mainThreadHandler.post(() -> {
                if (finalResult == null)
                    writeStateFileCallback.onSuccess();
                else
                    writeStateFileCallback.onError(finalResult);
            });

            stateWriterExecutor.shutdown();

        });

    }

    private String writeStateFile(String customTitle, OutputStream os) {

        if (source == null)
            return "Source is null.";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (Exception e) {
            return "Could not create DocumentBuilder: " + e.getMessage();
        }

        Document doc;
        InputStream is = new ByteArrayInputStream(source);
        try {
            doc = db.parse(is);
        } catch (Exception e) {
            return "Could not parse source: " + e.getMessage();
        }

        Element root = doc.getDocumentElement();
        if (root == null)
            return "Source has no root.";

        root.normalize();

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
            if (children.item(i).getNodeName().equals("state-title") || children.item(i).getNodeName().equals("color") || children.item(i).getNodeName().equals("events") )
                root.removeChild(children.item(i));

        Element customTitleEl = doc.createElement("state-title");
        customTitleEl.setTextContent(customTitle);
        root.appendChild(customTitleEl);

        Element colorEl = doc.createElement("color");
        colorEl.setTextContent("blue");
        root.appendChild(colorEl);

        Element eventsEl = doc.createElement("events");
        for (ExperimentTimeReference.TimeMapping event : experimentTimeReference.timeMappings) {

            Element eventEl = doc.createElement(event.event.name().toLowerCase());
            eventEl.setAttribute("experimentTime", event.experimentTime.toString());
            eventEl.setAttribute("systemTime", Long.toString(event.systemTime));
            eventsEl.appendChild(eventEl);
        }
        root.appendChild(eventsEl);

        NodeList containers = root.getElementsByTagName("data-containers");
        if (containers.getLength() != 1)
            return "Source needs exactly one data-container block.";

        NodeList buffers = containers.item(0).getChildNodes();

        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
        format.applyPattern("0.#########E0");
        DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(dfs);
        format.setGroupingUsed(false);

        for (int i = 0; i < buffers.getLength(); i++) {
            if (!buffers.item(i).getNodeName().equals("container"))
                continue;

            DataBuffer buffer = getBuffer(buffers.item(i).getTextContent());
            if (buffer == null)
                continue;

            Attr attr = doc.createAttribute("init");

            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (double v : buffer.getArray()) {
                if (first)
                    first = false;
                else
                    sb.append(",");
                sb.append(format.format(v));
            }

            attr.setValue(sb.toString());

            buffers.item(i).getAttributes().setNamedItem(attr);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t;
        try {
            t = tf.newTransformer();
        } catch (Exception e) {
            return "Could not create transformer: " + e.getMessage();
        }

        DOMSource domSource = new DOMSource(doc);


        StreamResult streamResult = new StreamResult(os);
        try {
            t.transform(domSource, streamResult);
        } catch (Exception e) {
            return "Transform failed: " + e.getMessage();
        }

        return null;
    }
}
