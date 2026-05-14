package pe.edu.upeu.controller;


import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import pe.edu.upeu.component.ToltipCustom;
import pe.edu.upeu.component.ValidadorFormulario;
import pe.edu.upeu.model.Migrante;
import pe.edu.upeu.service.MigranteServiceInter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

@Singleton
public class MigranteController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPais;
    @FXML private ComboBox<String> cbVisa;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbEstatus;
    @FXML private Button btnGuardar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;
    @FXML private TableView<Migrante> tvMigrantes;

    @Inject
    private MigranteServiceInter service;

    private ObservableList<Migrante> migrantesList = FXCollections.observableArrayList();
    private int indiceSeleccionado = -1;

    private ToltipCustom ttc = new ToltipCustom();
    private ValidadorFormulario<Migrante> validador;
    private Map<String, Control> camposUI;

    // Eliminado DefaultInternalConstraintValidatorFactory porque no es necesario y causa error de inyección.

    @FXML public void initialize() {
        cbEstatus.setItems(FXCollections.observableArrayList("Regular", "Irregular", "Solicitante de asilo"));
        cbVisa.setItems(FXCollections.observableArrayList("Turista", "Estudiante", "Trabajo", "Residente", "Docente"));
        configurarFormatoFecha();

        validador = new ValidadorFormulario<>(initValidator(service), ttc);
        camposUI = Map.of(
                "nombreCompleto", txtNombre,
                "paisOrigen", txtPais,
                "tipoVisa", cbVisa,
                "fechaIngreso", dpFecha,
                "statusMigratorio", cbEstatus
        );

        // Ya no usamos bind() de JavaFX porque usaremos ValidadorFormulario
        btnEliminar.disableProperty().bind(
                tvMigrantes.getSelectionModel().selectedItemProperty().isNull());

        TableColumn<Migrante, String> colNombre = new TableColumn<>("Nombre Completo");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));

        TableColumn<Migrante, String> colPais = new TableColumn<>("Pais");
        colPais.setCellValueFactory(new PropertyValueFactory<>("paisOrigen"));

        TableColumn<Migrante, String> colVisa = new TableColumn<>("Visa");
        colVisa.setCellValueFactory(new PropertyValueFactory<>("tipoVisa"));

        TableColumn<Migrante, LocalDate> colFecha = new TableColumn<>("Fecha Ingreso");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));

        TableColumn<Migrante, String> colEstatus = new TableColumn<>("Estatus");
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("statusMigratorio"));

        tvMigrantes.getColumns().addAll(colNombre, colPais, colVisa, colFecha, colEstatus);
        actualizarTabla();

        tvMigrantes.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
            if(newSelection != null && newSelection.intValue() >= 0){
                indiceSeleccionado = newSelection.intValue();
                Migrante m = tvMigrantes.getItems().get(indiceSeleccionado);
                txtNombre.setText(m.getNombreCompleto());
                txtPais.setText(m.getPaisOrigen());
                cbVisa.setValue(m.getTipoVisa());
                dpFecha.setValue(m.getFechaIngreso());
                cbEstatus.setValue(m.getStatusMigratorio());
            }
        });
    }

    private Validator initValidator(MigranteServiceInter csx){
        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(new ConstraintValidatorFactory() {
            @Override
            public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
                try {
                    return key.getDeclaredConstructor().newInstance();
                }catch (Exception e){
                    throw new RuntimeException("No se pudo instanciar: " +key, e);
                }
            }
            @Override
            public void releaseInstance(ConstraintValidator<?, ?> constraintValidator) {
            }
        });
        return config.buildValidatorFactory().getValidator();
    }

    @FXML
    void guardar(ActionEvent event) {
        Migrante m = new Migrante(
                0,
                txtNombre.getText(),
                txtPais.getText(),
                cbVisa.getValue(),
                dpFecha.getValue(),
                cbEstatus.getValue());

        if(!validador.validar(m, camposUI, Set.of())) return;

        if(indiceSeleccionado == -1){
            m.setId(null); // Para que H2 autoincremente
            service.agregarMigrante(m);
        }else{
            int idReal = tvMigrantes.getItems().get(indiceSeleccionado).getId();
            service.actualizarMigrante(m, idReal);
            indiceSeleccionado = -1;
        }
        limpiar(null);
        actualizarTabla();
    }

    @FXML
    void eliminar(ActionEvent event) {
        if(indiceSeleccionado != -1){
            int idReal = tvMigrantes.getItems().get(indiceSeleccionado).getId();
            service.eliminarMigrante(idReal);
            indiceSeleccionado = -1;
            limpiar(null);
            actualizarTabla();
        }
    }

    @FXML
    void limpiar(ActionEvent event) {
        txtNombre.clear();
        txtPais.clear();
        cbVisa.setValue(null);
        dpFecha.setValue(null);
        cbEstatus.setValue(null);
        indiceSeleccionado = -1;
        tvMigrantes.getSelectionModel().clearSelection();
        camposUI.values().forEach(ttc::limpiarCampo);
    }

    private void actualizarTabla(){
        migrantesList.clear();
        migrantesList.addAll(service.listarMigrantes());
        tvMigrantes.setItems(migrantesList);
    }

    private void configurarFormatoFecha() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpFecha.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formato.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, formato);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
    }
}