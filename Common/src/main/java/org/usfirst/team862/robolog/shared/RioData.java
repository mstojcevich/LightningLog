package org.usfirst.team862.robolog.shared;

import java.io.Serializable;

public class RioData implements Serializable {

    private double[] analog = new double[4];

    private boolean[] digital = new boolean[10];

    private boolean[] relay = new boolean[4];

    public RioData() {
        this(
                0, 0, 0, 0, // Analog 0-3
                false, false, false, false, false, // Digital 0-4
                false, false, false, false, false, // Digital 5-9
                false, false, false, false // Relay 0-3
        );
    }

    public RioData(double analog0, double analog1, double analog2, double analog3,
                   boolean digital0, boolean digital1, boolean digital2, boolean digital3, boolean digital4,
                   boolean digital5, boolean digital6, boolean digital7, boolean digital8, boolean digital9,
                   boolean relay0, boolean relay1, boolean relay2, boolean relay3) {
        analog = new double[]{ analog0, analog1, analog2, analog3 };
        digital = new boolean[]{ digital0, digital1, digital2, digital3, digital4,
                                digital5, digital6, digital7, digital8, digital9 };
        relay = new boolean[]{ relay0, relay1, relay2, relay3 };
    }

    public boolean getDigital(int idx) throws ArrayIndexOutOfBoundsException {
        return digital[idx];
    }

    public double getAnalog(int idx) throws ArrayIndexOutOfBoundsException {
        return analog[idx];
    }

    public boolean getRelay(int idx) throws ArrayIndexOutOfBoundsException {
        return relay[idx];
    }

}
