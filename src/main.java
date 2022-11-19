
public class main {
    public static void main(String[] args) {
        new main().runApp(args);
    }

    public void runApp(String[] args) {
        new Thread() {
            @Override
            public void run() {
                javafx.application.Application.launch(PopupWindow.class);
            }
        }.start();
        // PopupWindow startUpTest = PopupWindow.waitForStartUpTest();
    }
}
