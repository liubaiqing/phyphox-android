package de.rwth_aachen.phyphox;

import java.io.File;

public class DataExport {

    public static class ExportFormat {
        public String getName() {
            return "CSV";
        }

        public String getType(boolean compressed) {
            return "text/csv";
        }

        public String getFilename(boolean compressed) {
            return "gyroscope_data.csv";
        }
    }

    public static class ExportSet {
        public String name;
        public static class SourceMapping {
            public String name;
            public String source;
        }
        public SourceMapping[] sources;
    }

    public ExportFormat[] exportFormats = {new ExportFormat()};
    public ExportSet[] exportSets = new ExportSet[0];
    private PhyphoxExperiment experiment;

    public DataExport(PhyphoxExperiment experiment) {
        this.experiment = experiment;
    }

    public File exportDirect(ExportFormat format, File directory, boolean compressed, String fileName, Object context) {
        // Simplified implementation
        return new File(directory, format.getFilename(compressed));
    }
}
