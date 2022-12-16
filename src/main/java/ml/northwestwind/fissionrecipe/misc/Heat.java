package ml.northwestwind.fissionrecipe.misc;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Heat {
    public static final ScriptEngine JS_ENGINE;

    static {
        ScriptEngineManager mgr = new ScriptEngineManager();
        JS_ENGINE = mgr.getEngineByName("JavaScript");
    }

    private final boolean isEqt;
    private final double constant;
    private final String equation;

    public Heat(boolean isEqt, double constant, String equation) {
        this.isEqt = isEqt;
        if (this.isEqt) {
            this.constant = 0.2;
            this.equation = equation;
        } else {
            this.constant = constant;
            this.equation = null;
        }
    }

    public boolean isEqt() {
        return isEqt;
    }

    public double getConstant() {
        return constant;
    }

    public String getEquation() {
        return equation;
    }
}
