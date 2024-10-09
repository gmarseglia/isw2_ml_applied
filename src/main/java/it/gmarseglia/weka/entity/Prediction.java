package it.gmarseglia.weka.entity;

import it.gmarseglia.app.entity.Exportable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

public class Prediction implements Exportable {
    private final String id;
    private final int size;
    private final double predicted;
    private final String actual;

    public Prediction(String id, int size, double predicted, String actual) {
        this.id = id;
        this.size = size;
        this.predicted = predicted;
        this.actual = actual;
    }

    @Override
    public List<String> getFieldsNames() {
        return List.of("ID", "Size", "Predicted", "Actual");
    }

    @Override
    public List<Serializable> getFieldsValues() {
        DecimalFormat df = new DecimalFormat("0.000");
        return List.of(id, size, df.format(predicted), actual);
    }
}
