/*
 * Copyright (c) 2014, AXIA Studio (Tiziano Lattisi) - http://www.axiastudio.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the AXIA Studio nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY AXIA STUDIO ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL AXIA STUDIO BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.axiastudio.zoefx.desktop.view.search;

import com.axiastudio.zoefx.core.IOC;
import com.axiastudio.zoefx.core.beans.BeanClassAccess;
import com.axiastudio.zoefx.desktop.model.beans.LookupStringConverter;
import com.axiastudio.zoefx.desktop.model.converters.String2BigDecimal;
import com.axiastudio.zoefx.desktop.model.property.CallbackBuilder;
import com.axiastudio.zoefx.desktop.db.DataSet;
import com.axiastudio.zoefx.desktop.db.DataSetBuilder;
import com.axiastudio.zoefx.core.db.Database;
import com.axiastudio.zoefx.core.db.Manager;
import com.axiastudio.zoefx.desktop.view.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: tiziano
 * Date: 21/05/14
 * Time: 15:21
 */
public class SearchController<T> implements Initializable {

    @FXML
    private TableView results;

    @FXML
    private Button searchSearchForm;

    @FXML
    private VBox filterbox;

    @FXML
    private Button applySearchForm;

    @FXML
    private TitledPane filters;

    private Class entityClass;
    private Behavior behavior;
    private Callback<List<T>, Boolean> callback=null;
    private List<Criterion> criteria = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        results.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filters.setText(resourceBundle.getString("search.filters"));
        searchSearchForm.setText(resourceBundle.getString("search.search"));
        applySearchForm.setText(resourceBundle.getString("search.apply"));
    }

    public void setEntityClass(Class<? extends T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setBehavior(Behavior behavior) {
        this.behavior = behavior;
    }

    public void setCallback(Callback<List<T>, Boolean> callback) {
        this.callback = callback;
    }

    public void setColumns(List<String> columns) {
        for( String property: columns ) {
            TableColumn column = new TableColumn();
            column.setText(property);
            String lookup=null;
            if( behavior != null ){
                lookup = behavior.getProperties().getProperty(property + ".lookup");
            }
            Callback callback = CallbackBuilder.create().beanClass(entityClass)
                    .field(property)
                    .lookup(lookup)
                    .build();
            column.setCellValueFactory(callback);
            // custom date order
            BeanClassAccess beanClassAccess = new BeanClassAccess(entityClass, property);
            if( beanClassAccess.getReturnType() == null ){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to set search column '" + property + "' (maybe wrong searchcolumns property?))");
                return;
            }
            if( Date.class.isAssignableFrom(beanClassAccess.getReturnType()) ) {
                column.setComparator(Comparator.nullsFirst(Comparators.DateComparator));
            }
            results.getColumns().add(column);
        }
    }

    public void setCriteria(List<String> fields){
        for( String property: fields){
            HBox hBox = new HBox();
            hBox.setSpacing(10);
            Label label = new Label(property);
            label.setMinWidth(120.0);
            Node criterionNode=null;
            BeanClassAccess beanClassAccess = new BeanClassAccess(entityClass, property);
            Class<?> returnType = beanClassAccess.getReturnType();
            if( beanClassAccess.getReturnType() == null ){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to set search criteria '" + property + "' (maybe wrong searchcriteria property?))");
                return;
            }
            if( String.class.isAssignableFrom(beanClassAccess.getReturnType()) ) {
                TextField textField = new TextField();
                textField.setMinWidth(200.0);
                textField.setId(property);
                criterionNode = textField;
                criteria.add(new Criterion<String>(String.class, textField));
            } else if( BigDecimal.class.isAssignableFrom(beanClassAccess.getReturnType()) ) {
                TextField textField = new TextField();
                textField.setMinWidth(200.0);
                textField.setId(property);
                criterionNode = textField;
                criteria.add(new Criterion<BigDecimal>(BigDecimal.class, textField));
            } else if( Boolean.class.isAssignableFrom(beanClassAccess.getReturnType()) ) {
                CheckBox checkBox = new CheckBox();
                checkBox.setAllowIndeterminate(true);
                checkBox.setIndeterminate(true);
                checkBox.setMinWidth(200.0);
                checkBox.setId(property);
                criterionNode = checkBox;
                criteria.add(new Criterion<Boolean>(Boolean.class, checkBox));
            } else if( Date.class.isAssignableFrom(beanClassAccess.getReturnType()) ) {
                HBox dateHBox = new HBox();
                DatePicker fromDatePicker = new DatePicker();
                fromDatePicker.setId(property);
                DatePicker toDatePicker = new DatePicker();
                dateHBox.getChildren().addAll(fromDatePicker, toDatePicker);
                criterionNode = dateHBox;
                criteria.add(new Criterion<Date>(Date.class, fromDatePicker, toDatePicker));
            } else if( Object.class.isAssignableFrom(beanClassAccess.getReturnType()) ) {
                List superset = new ArrayList();
                if( returnType.isEnum() ) {
                    for (Object obj : returnType.getEnumConstants() ) {
                        superset.add(obj);
                    }

                } else {
                    Database database = IOC.queryUtility(Database.class);
                    if( database != null ) {
                        Manager<?> manager = database.createManager(returnType);
                        for (Object obj : manager.getAll()) {
                            superset.add(obj);
                        }
                    }
                }
                ChoiceBox choiceBox = new ChoiceBox();
                choiceBox.setId(property);
                if( behavior != null ) {
                    String lookup = behavior.getProperties().getProperty(property + ".lookup");
                    if (lookup != null) {
                        choiceBox.setConverter(new LookupStringConverter<>(lookup));
                    }
                }
                ObservableList choices = FXCollections.observableArrayList(superset);
                choiceBox.setItems(choices);
                criterionNode = choiceBox;
                criteria.add(new Criterion(Object.class, choiceBox));
            }
            if( criterionNode != null ) {
                hBox.getChildren().addAll(label, criterionNode);
                filterbox.getChildren().add(hBox);
            }
        }
    }

    @FXML
    private void search(ActionEvent event){
        Database db = IOC.queryUtility(Database.class);
        Manager<T> manager = db.createManager(entityClass);
        Map<String, Object> map = new HashMap<>();
        for( int i=0; i<criteria.size(); i++){
            Criterion criterion = criteria.get(i);
            if( criterion.getCriterionClass() == String.class ){
                TextField criteriaField = (TextField) criterion.getNodes().get(0);
                String fieldName = criteriaField.getId();
                String value = criteriaField.getText();
                if( value.length()>0 ) {
                    map.put(fieldName, value);
                }
            } else if( criterion.getCriterionClass() == Object.class ){
                ChoiceBox choiceBox = (ChoiceBox) criterion.getNodes().get(0);
                String fieldName = choiceBox.getId();
                Object value = choiceBox.getSelectionModel().getSelectedItem();
                map.put(fieldName, value);
            } else if( criterion.getCriterionClass() == Boolean.class ){
                CheckBox criteriaField = (CheckBox) criterion.getNodes().get(0);
                String fieldName = criteriaField.getId();
                if( !criteriaField.isIndeterminate() ) {
                    Boolean value = criteriaField.isSelected();
                    if (value != null) {
                        map.put(fieldName, value);
                    }
                }
            } else if( criterion.getCriterionClass() == BigDecimal.class ){
                TextField criteriaField = (TextField) criterion.getNodes().get(0);
                String fieldName = criteriaField.getId();
                BigDecimal value = (new String2BigDecimal()).call(criteriaField.getText());
                if (value != null) {
                    map.put(fieldName, value);
                }
            } else if( criterion.getCriterionClass() == Date.class ){
                DatePicker firstDateNode = (DatePicker) criterion.getNodes().get(0);
                String fieldName = firstDateNode.getId();
                DatePicker secondDateNode = (DatePicker) criterion.getNodes().get(1);
                List<Date> value = new ArrayList<>();
                LocalDate fromLocalDate = firstDateNode.getValue();
                if( fromLocalDate != null ) {
                    value.add(localDateToDate(fromLocalDate));
                    LocalDate toLocalDate = secondDateNode.getValue();
                    if( toLocalDate != null ) {
                        value.add(localDateToDate(toLocalDate));
                    } else {
                        value.add(localDateToDate(fromLocalDate));
                    }
                    map.put(fieldName, value);
                }
            }
        }
        DataSet<T> dataSet;
        if( map.keySet().size()>0 ){
            dataSet = DataSetBuilder.create(entityClass).store(manager.query(map)).manager(manager).build();
        } else {
            dataSet = DataSetBuilder.create(entityClass).store(manager.getAll()).manager(manager).build();
        }
        ObservableList<T> observableList;
        if( dataSet.size()>0 ) {
            observableList = FXCollections.observableArrayList(dataSet.getStore());
        } else {
            observableList = FXCollections.observableArrayList(new ArrayList<T>());
        }
        results.setItems(observableList);
    }

    @FXML
    private void apply(ActionEvent event){
        ObservableList<T> items = results.getSelectionModel().getSelectedItems();
        callback.call(items);
        Stage stage = (Stage) filterbox.getScene().getWindow();
        stage.close();
    }

    private Date localDateToDate(LocalDate localDate){
        Calendar calendar =  Calendar.getInstance();
        calendar.set(localDate.getYear(), localDate.getMonthValue()-1, localDate.getDayOfMonth());
        Date date = calendar.getTime();
        return date;
    }

}
