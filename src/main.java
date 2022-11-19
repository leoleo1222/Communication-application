import javafx.application.Application;
import javafx.stage.Stage;

public class main extends Application {
    PopupWindow log = new PopupWindow();
    gui chat = new gui();
    public static void main(String[] args) {
        new main().runApp(args);
    }

    public void runApp(String[] args) {
        log.launch(args);
        chat.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
