package com.lablens;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Main extends Application {

    private static WebView webView;
    private static final JavaBridge bridge = new JavaBridge(); // ONE instance

    public static WebView getWebView() {
        return webView;
    }

    @Override
    public void start(Stage stage) {

        webView = new WebView();

        loadPage("login.html");

        Scene scene = new Scene(webView, 900, 600);

        stage.setTitle("Lab Lens");
        stage.setScene(scene);
        stage.show();
    }

    public static void loadPage(String page) {

        webView.getEngine().load(
                Main.class.getResource("/" + page).toExternalForm()
        );

        webView.getEngine().getLoadWorker().stateProperty().addListener(
                (obs, oldState, newState) -> {

                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {

                        JSObject window =
                                (JSObject) webView.getEngine().executeScript("window");

                        window.setMember("javaApp", bridge); // use same instance
                    }
                }
        );
    }

    public static void main(String[] args) {
        launch();
    }
}