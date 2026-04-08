package de.rwth_aachen.phyphox;

import java.util.ArrayList;
import java.util.List;

public class ExpView {
    public String name;
    public List<expViewElement> elements = new ArrayList<>();

    public static class expViewElement {
        public String label;
        public String[] inputs;

        public void onViewSelected(boolean selected) {
            // Empty implementation
        }

        public String getUpdateMode() {
            return "auto";
        }

        public String getViewHTML(int id) {
            return "";
        }

        public String dataCompleteHTML() {
            return "function() {}";
        }

        public String setDataHTML() {
            return "function() {}";
        }

        public void trigger() {
            // Empty implementation
        }
    }
}
