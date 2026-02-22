package com.lablens;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JavaBridge {

    private static String currentUser;

    public void login(String username) {
        currentUser = username;
        Main.loadPage("dashboard.html");
        startMonitoring();
    }

    public String getCurrentUser() {
        return currentUser;
    }

    private void startMonitoring() {

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {

                    Set<String> apps = getRunningApps();

                    Platform.runLater(() -> {

                        // Send username
                        Main.getWebView().getEngine()
                                .executeScript("setUser('" + currentUser + "');");

                        // Send apps
                        String jsArray = apps.stream()
                                .map(app -> "'" + app + "'")
                                .reduce((a, b) -> a + "," + b)
                                .orElse("");

                        Main.getWebView().getEngine()
                                .executeScript("updateApps([" + jsArray + "]);");
                    });

                }, 0, 5, TimeUnit.SECONDS);
    }

    private Set<String> getRunningApps() {

        Set<String> uniqueApps = new HashSet<>();

        try {
            Process process = new ProcessBuilder("tasklist").start();
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("Image Name") || line.startsWith("="))
                    continue;

                if (!line.trim().isEmpty()) {
                    String name = line.split("\\s+")[0];
                    uniqueApps.add(name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uniqueApps;
    }
}