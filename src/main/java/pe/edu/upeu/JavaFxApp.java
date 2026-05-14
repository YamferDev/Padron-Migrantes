package pe.edu.upeu;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public class JavaFxApp extends Application {
    private ApplicationContext context;
    private Parent parent;

    @Override
    public void init() throws Exception {
        context = Micronaut.build(getParameters().getRaw().toArray(new String[0]))
                .mainClass(JavaFxApp.class).start();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_migrantes.fxml"));
        loader.setControllerFactory(context::getBean);
        parent = loader.load();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        Scene scene = new Scene(parent, bounds.getWidth(), bounds.getHeight() - 100);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Padrón de Migrantes - Micronaut");
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (context != null) context.close();
        super.stop();
    }
}
