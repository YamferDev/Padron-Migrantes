package pe.edu.upeu.model;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Builder
@Entity(name = "migrante")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Migrante {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @NotNull
    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @NotNull
    @NotBlank(message = "El país de origen no puede estar vacío")
    @Column(name = "pais_origen")
    private String paisOrigen;

    @NotNull
    @NotBlank(message = "El tipo de visa no puede estar vacío")
    @Column(name = "tipo_visa")
    private String tipoVisa;

    @NotNull(message = "La fecha de ingreso no puede estar vacía")
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @NotNull
    @NotBlank(message = "El status migratorio no puede estar vacío")
    @Column(name = "status_migratorio")
    private String statusMigratorio;

}
