package de.rwth_aachen.phyphox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

public class RemoteServer {

    public PhyphoxExperiment experiment;
    public HTTPServer httpServer;
    public ExecutorService executor;
    public static int httpServerPort = 8080;
    public Context context;
    public Experiment callActivity;
    public String sessionID = "";
    public boolean forceFullUpdate = false;
    public static String indexHTML, styleCSS;
    private Vector<Integer> htmlID2View = new Vector<>();
    private Vector<Integer> htmlID2Element = new Vector<>();

    protected void buildStyleCSS() {
        // Simplified implementation
        styleCSS = "";
    }

    protected String getBase64PNG(Drawable src) {
        // Simplified implementation
        return "";
    }

    protected void buildViewsJson(StringBuilder sb) {
        // Simplified implementation
        sb.append("var views = [];");
    }

    protected void buildIndexHTML() {
        // Simplified implementation
        indexHTML = "<html><body><h1>Gyroscope Lightweight Remote</h1></body></html>";
    }

    public RemoteServer(PhyphoxExperiment experiment, Experiment callActivity, String sessionID) {
        this.experiment = experiment;
        this.callActivity = callActivity;
        this.context = (Context) callActivity;
        buildStyleCSS();
        buildIndexHTML();
        this.sessionID = sessionID;
    }

    public RemoteServer(PhyphoxExperiment experiment, Experiment callActivity) {
        this(experiment, callActivity, String.format("%06x", (System.nanoTime() & 0xffffff)));
    }

    public static String getAddresses(Context context) {
        String ret = "";
        Inet4Address filterMobile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] networks = connectivityManager.getAllNetworks();
            for (Network network : networks) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    continue;
                }
                LinkProperties properties = connectivityManager.getLinkProperties(network);
                for (LinkAddress address : properties.getLinkAddresses()) {
                    if (address.getAddress() instanceof Inet4Address) {
                        filterMobile = (Inet4Address) address.getAddress();
                        break;
                    }
                }
            }
        }
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (!inetAddress.isAnyLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address && !inetAddress.equals(filterMobile)) {
                        ret += "http://" + inetAddress.getHostAddress() + ":" + httpServerPort + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("getAddresses", "Error getting the IP.", e);
        }
        return ret;
    }

    public synchronized void start() {
        httpServerPort = 8080;
        httpServer = new HTTPServer(httpServerPort);
        executor = Executors.newCachedThreadPool();
        httpServer.setExecutor(executor);
        HTTPServer.VirtualHost host = httpServer.getVirtualHost(null);
        host.addContext("/", this::handleHome);
        host.addContext("/style.css", this::handleStyle);
        host.addContext("/get", this::handleGet);
        host.addContext("/control", this::handleControl, "GET", "POST");
        try {
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void stop() {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    protected int respond(Response response, String contentType, InputStream in, long length) throws IOException {
        try {
            response.sendHeaders(200, length, System.currentTimeMillis(), null, contentType, null);
            response.sendBody(in, -1, null);
        } finally {
            in.close();
        }
        return 0;
    }

    protected int respond(Response response, String contentType, String content) throws IOException {
        byte[] bytes = content.getBytes();
        InputStream in = new ByteArrayInputStream(bytes);
        return respond(response, contentType, in, bytes.length);
    }

    protected int respond(Response response, String json) throws IOException {
        return respond(response, "application/json", json);
    }

    protected int respond(Response response, boolean result) throws IOException {
        return respond(response, result ? "{\"result\": true}" : "{\"result\": false}");
    }

    public int handleHome(Request request, Response response) throws IOException {
        return respond(response, "text/html", indexHTML);
    }

    public int handleStyle(Request request, Response response) throws IOException {
        return respond(response, "text/css", styleCSS);
    }

    protected static class BufferRequest {
        public String name;
        public Double threshold;
        public String reference;
    }

    protected BufferRequest parseBufferRequest(String name, String value, boolean forceFullUpdate) {
        BufferRequest br = new BufferRequest();
        br.name = name;
        br.reference = "";
        if (value == null || value.isEmpty()) {
            br.threshold = Double.NaN;
        } else if (value.equals("full") || forceFullUpdate) {
            br.threshold = Double.NEGATIVE_INFINITY;
        } else {
            int subsplit = value.indexOf('|');
            if (subsplit == -1)
                br.threshold = Double.valueOf(value);
            else {
                br.threshold = Double.valueOf(value.substring(0, subsplit));
                br.reference = value.substring(subsplit + 1);
            }
        }
        return br;
    }

    protected Set<BufferRequest> getBufferRequests(Request request) throws IOException {
        Set<BufferRequest> buffers = new LinkedHashSet<>();
        List<String[]> params = request.getParamsList();
        if (!params.isEmpty()) {
            for (String[] param : params) {
                BufferRequest br = parseBufferRequest(param[0], param[1], forceFullUpdate);
                buffers.add(br);
            }
            forceFullUpdate = false;
        }
        return buffers;
    }

    protected void buildBuffer(BufferRequest buffer, DataBuffer db, DecimalFormat format, StringBuilder sb) {
        sb.append("\"").append(db.name).append("\":{\"size\":");
        sb.append(db.size);
        sb.append(",\"updateMode\":\"");
        if (Double.isNaN(buffer.threshold))
            sb.append("single");
        else if (Double.isInfinite(buffer.threshold))
            sb.append("full");
        else
            sb.append("partial");
        sb.append("\", \"buffer\":[");

        if (Double.isNaN(buffer.threshold)) {
            if (Double.isNaN(db.value) || Double.isInfinite(db.value))
                sb.append("null");
            else
                sb.append(format.format(db.value));
        } else {
            boolean firstValue = true;
            Double[] data = db.getArray();
            int n = db.getFilledSize();
            for (int i = 0; i < n; i++) {
                if (firstValue)
                    firstValue = false;
                else
                    sb.append(",");
                Double v = data[i];
                if (Double.isNaN(v) || Double.isInfinite(v))
                    sb.append("null");
                else
                    sb.append(format.format(v));
            }
        }
        sb.append("]}");
    }

    public int handleGet(Request request, Response response) throws IOException {
        Set<BufferRequest> buffers = getBufferRequests(request);
        StringBuilder sb = new StringBuilder();
        experiment.dataLock.lock();
        try {
            sb.append("{\"buffer\":{\n");
            boolean firstBuffer = true;
            DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
            format.applyPattern("0.#######E0");
            for (BufferRequest buffer : buffers) {
                DataBuffer db = experiment.getBuffer(buffer.name);
                if (db == null)
                    continue;
                if (firstBuffer)
                    firstBuffer = false;
                else
                    sb.append(",\n");
                buildBuffer(buffer, db, format, sb);
            }
            sb.append("\n},\n\"status\":{\n");
            sb.append("\"session\":\"").append(sessionID).append("\", \"measuring\":");
            sb.append(callActivity.measuring);
            sb.append(", \"timedRun\":false, \"countDown\":0\n}\n}\n");
        } finally {
            experiment.dataLock.unlock();
        }
        return respond(response, sb.toString());
    }

    public int handleControl(Request request, Response response) throws IOException {
        String cmd = request.getParams().get("cmd");
        if (cmd != null) {
            switch (cmd) {
                case "start":
                    callActivity.remoteStartMeasurement();
                    return respond(response, true);
                case "stop":
                    callActivity.remoteStopMeasurement();
                    return respond(response, true);
                case "clear":
                    callActivity.clearData();
                    return respond(response, true);
                default:
                    return respond(response, false);
            }
        } else
            return respond(response, false);
    }
}
