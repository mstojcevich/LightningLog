package org.usfirst.team862.robolog.shared;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHeader implements Serializable {

    private Date robotCodeStartTime;
    private Date codeDeployTime;
    private Date rioUpSince;

    public LogHeader() {
        this.robotCodeStartTime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime()); // TODO ask for as parameter
        this.codeDeployTime = new Date(new File("/home/lvuser/FRCUserProgram.jar").lastModified());

        { // Rio up since
            try {
                Process uptimeProc = Runtime.getRuntime().exec("uptime -s");
                BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
                this.rioUpSince = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(in.readLine());
            } catch (Exception e) {
                e.printStackTrace();
                this.rioUpSince = new Date(0);
            }
        }
    }

    public LogHeader(Date robotCodeStartTime, Date codeDeployTime, Date rioUpSince) {
        this.robotCodeStartTime = robotCodeStartTime;
        this.codeDeployTime = codeDeployTime;
        this.rioUpSince = rioUpSince;
    }

    public Date getRobotCodeStartTime() {
        return this.robotCodeStartTime;
    }

    public Date getRioUpSince() {
        return this.rioUpSince;
    }

    public Date getDeployTime() {
        return this.codeDeployTime;
    }

}
