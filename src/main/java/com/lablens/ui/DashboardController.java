package com.lablens.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController {

    @FXML
    private Label studentNameLabel;

    @FXML
    private Label sessionStartLabel;

    @FXML
    private Label currentTimeLabel;

    @FXML
    private ListView<String> appListView;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {

        studentNameLabel.setText("Student: Vicky");

        String startTime = LocalTime.now().format(formatter);
        sessionStartLabel.setText("Session Started: " + startTime);

        startClock();
        startAppMonitoring();
    }

    private void startClock() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    String now = LocalTime.now().format(formatter);
                    currentTimeLabel.setText("Current Time: " + now);
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void startAppMonitoring() {

        scheduler.scheduleAtFixedRate(() -> {

            Set<String> apps = getRunningApps();

            Platform.runLater(() -> {
                appListView.getItems().setAll(apps);
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

                if (line.startsWith("Image Name") || line.startsWith("=")) {
                    continue;
                }

                if (!line.trim().isEmpty()) {
                    String processName = line.split("\\s+")[0];

                    // basic filtering
                    if (!processName.equalsIgnoreCase("svchost.exe")
                            && !processName.equalsIgnoreCase("RuntimeBroker.exe")
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
