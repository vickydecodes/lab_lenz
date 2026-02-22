package com.lablens.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController {

    @FXML
    private WebView webView;

    private WebEngine engine;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {

        engine = webView.getEngine();
        engine.load(getClass().getResource("/dashboard.html").toExternalForm());

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                startMonitoring();  // start only after page fully loads
            }
        });
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Monitoring tick...");

            Set<String> apps = getRunningApps();

            Platform.runLater(() -> {
                String jsArray = apps.stream()
                        .map(app -> "'" + app + "'")
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");

                engine.executeScript("updateApps([" + jsArray + "]);");
            });

        }, 0, 5, TimeUnit.SECONDS);
    }

    private Set<String> getRunningApps() {

        Set<String> uniqueApps = new HashSet<>();

        try {
            ProcessBuilder builder = new ProcessBuilder("tasklist");
            Process process = builder.start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("Image Name") || line.startsWith("="))
                    continue;

                if (!line.trim().isEmpty()) {
                    String processName = line.split("\\s+")[0];

                    if (!processName.equalsIgnoreCase("svchost.exe")
                            && !processName.equalsIgnoreCase("System")) {

                        uniqueApps.add(processName);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uniqueApps;
    }
}