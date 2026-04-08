package de.rwth_aachen.phyphox;

public class NetworkConnection {
    public void disconnect() {
        // Empty implementation
    }

    public void connect(Object context) {
        // Empty implementation
    }

    public interface ScanDialogDismissedDelegate {
        void networkScanDialogDismissed();
    }

    public interface NetworkConnectionDataPolicyInfoDelegate {
        void dataPolicyInfoDismissed();
    }
}
