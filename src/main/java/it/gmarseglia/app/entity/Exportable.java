package it.gmarseglia.app.entity;

import java.io.Serializable;
import java.util.List;

public interface Exportable {

    List<String> getFieldsNames();

    List<Serializable> getFieldsValues();
}
