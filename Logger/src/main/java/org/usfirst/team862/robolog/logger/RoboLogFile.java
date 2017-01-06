package org.usfirst.team862.robolog.logger;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.hal.AnalogJNI;
import edu.wpi.first.wpilibj.hal.DIOJNI;
import edu.wpi.first.wpilibj.hal.RelayJNI;
import org.usfirst.team862.robolog.shared.LogHeader;
import org.usfirst.team862.robolog.shared.LoggerEvent;
import org.usfirst.team862.robolog.shared.LoggerEventType;
import org.usfirst.team862.robolog.shared.RioData;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class RoboLogFile implements Closeable {

    // Output streams
    private final FileOutputStream fos;
    private final BufferedOutputStream bos;
    private final GZIPOutputStream gos;
    private final ObjectOutputStream out;

    private Map<String, String> emptyMap = new HashMap<>();

    public RoboLogFile(File path) throws IOException {
        fos = new FileOutputStream(path, false); // false for overwrite instead of append
        bos = new BufferedOutputStream(fos);
        gos = new GZIPOutputStream(bos);
        out = new ObjectOutputStream(gos);

        // Create and write the header
        LogHeader lh = new LogHeader();
        out.writeObject(lh);
    }

    public void logSimple(LoggerEventType type, String message, String description) {
        logWithAttachments(type, message, description, emptyMap, type == LoggerEventType.ERROR || type == LoggerEventType.EXCEPTION);
    }

    public void logWithAttachments(LoggerEventType type, String message, String description, Map<String, String> attachments, boolean stackTrace) {
        double batteryVoltage = DriverStation.getInstance().getBatteryVoltage();
        double matchTime = DriverStation.getInstance().getMatchTime();
        boolean brownedOut = DriverStation.getInstance().isBrownedOut();

        String stack = "";

        if(stackTrace) {
            StringBuilder stackBuilder = new StringBuilder();
            for(StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                stackBuilder.append(ste.toString());
            }
            stack = stackBuilder.toString();
        }

        LoggerEvent event = new LoggerEvent(type, message, description, new Date(), matchTime, batteryVoltage,
                brownedOut, getCurrentRioData(), attachments, stack);
        try {
            out.writeObject(event);
        } catch(IOException e) {
            e.printStackTrace(); // shouldn't happen unless the file disappeared during runtime
        }
    }

    private RioData getCurrentRioData() {
        boolean[] dio = new boolean[10];
        for(int i = 0; i < 10; i++) {
            dio[i] = DIOJNI.getDIO(i);
        }

        double[] analog = new double[4];
        for(int i = 0; i < 4; i++) {
            analog[i] = AnalogJNI.getAnalogVoltage(i);
        }

        boolean[] relay = new boolean[4];
        for(int i = 0; i < 4; i++) {
            relay[i] = RelayJNI.getDIO(i);
        }

        return new RioData(analog[0], analog[1], analog[2], analog[3],
                dio[0], dio[1], dio[2], dio[3], dio[4], dio[5], dio[6], dio[7], dio[8], dio[9],
                relay[0], relay[1], relay[2], relay[3]);
    }

    @Override
    public void close() throws IOException {
        out.close();
        gos.close();
        bos.close();
        fos.close();
    }

}
