package pe.edu.upeu.component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import javafx.scene.control.Control;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utilidad genérica de validación para formularios JavaFX.
 *
 * <p>Desacopla la lógica de validación del controller: en lugar de un switch
 * hardcodeado por campo, recibe un Map que relaciona el nombre de la propiedad
 * del modelo con su Control correspondiente.</p>
 *
 * <p>Uso básico en cualquier controller:</p>
 * <pre>
 *   Map&lt;String, Control&gt; campos = Map.of(
 *       "dni",    txtDni,
 *       "nombre", txtNombre,
 *       "email",  txtEmail
 *   );
 *   boolean ok = ValidadorFormulario.validar(validator, cliente, campos, ttc);
 * </pre>
 *
 * @param <T> Tipo del objeto de modelo anotado con Bean Validation.
 */
public class ValidadorFormulario<T> {

    private final Validator validator;
    private final ToltipCustom ttc;

    public ValidadorFormulario(Validator validator, ToltipCustom ttc) {
        this.validator = validator;
        this.ttc = ttc;
    }

    /**
     * Valida {@code modelo} y aplica feedback visual (borde rojo + tooltip)
     * sobre cada Control cuyo nombre de campo tenga una violación.
     *
     * @param modelo             Objeto a validar.
     * @param camposUI           Map [nombre de propiedad → Control].
     * @param anotacionesExcluir Nombres de anotaciones a ignorar (ej. "DniUnic"
     *                           al actualizar). Puede ser vacío o null.
     * @return {@code true} si no hay violaciones relevantes; {@code false} si hay errores.
     */
    public boolean validar(T modelo,
                           Map<String, Control> camposUI,
                           Set<String> anotacionesExcluir) {

        // 1. Limpiar estado visual previo
        camposUI.values().forEach(ttc::limpiarCampo);

        // 2. Ejecutar Bean Validation
        Set<ConstraintViolation<T>> violations = validator.validate(modelo);

        // 3. Agrupar mensajes por campo, aplicando exclusiones
        Map<String, StringBuilder> mensajesPorCampo = new HashMap<>();
        for (ConstraintViolation<T> v : violations) {
            String campo     = v.getPropertyPath().toString();
            String anotacion = v.getConstraintDescriptor()
                    .getAnnotation().annotationType().getSimpleName();

            if (anotacionesExcluir != null && anotacionesExcluir.contains(anotacion)) continue;

            mensajesPorCampo
                    .computeIfAbsent(campo, k -> new StringBuilder())
                    .append(v.getMessage()).append(" ");
        }

        if (mensajesPorCampo.isEmpty()) return true;

        // 4. Marcar solo los campos presentes en el Map del formulario
        mensajesPorCampo.forEach((campo, msg) -> {
            Control tf = camposUI.get(campo);
            if (tf != null) ttc.marcarError(tf, msg.toString().trim());
        });

        return false;
    }
}