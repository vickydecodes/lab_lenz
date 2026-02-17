package com.lablens.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ProcessMonitor {

    private final Set<String> ignoredProcesses = Set.of(
            "svchost.exe",
            "RuntimeBroker.exe",
            "csrss.exe",
            "winlogon.exe",
            "services.exe",
            "lsass.exe",
            "fontdrvhost.exe",
            "dwm.exe",
            "sihost.exe",
            "taskhostw.exe",
            "conhost.exe",
            "SearchHost.exe"
    );

    public void printRunningApps() {

        try {
            ProcessBuilder builder = new ProcessBuilder("tasklist");
            Process process = builder.start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    );

            String line;
            Set<String> uniqueApps = new HashSet<>();

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("Image Name") || line.startsWith("=")) {
                    continue;
                }

                if (!line.trim().isEmpty()) {
                    String processName = line.split("\\s+")[0];

                    if (!ignoredProcesses.contains(processName)) {
                        uniqueApps.add(processName);
                    }
                }
            }

            System.out.println("User-Level Apps Running:");
            uniqueApps.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
