package mil.emp3.core.mapgridlines.utils;

import mil.emp3.api.Polygon;

/**
 * This class represents an angle in degrees, minutes and seconds.
 */

public class DMSAngle {
    public static final double DEGREES_PER_MILLISECOND = 1.0 / 3600000.0;
    public static final double DEGREES_PER_SECOND = 1.0 / 3600.0;
    public static final double DEGREES_PER_MINUTE = 1.0 / 60.0;

    private double angleDegrees;
    private int sign = 0;
    private int degreeValue = 0;
    private int minuteValue = 0;
    private double secondValue = 0.0;

    public DMSAngle() {}

    public DMSAngle(double degrees) {
        this.setDecimalDegrees(degrees);
    }

    /**
     * This static method creates a DMSAngle object from the parameters.
     * @param degrees    The angle in degrees.
     * @return an DMSAngle object.
     * @throws IllegalArgumentException if the argument is NaN or Infinity.
     */
    public  static DMSAngle fromDD(double degrees) {
        if (Double.isNaN(degrees) || Double.isInfinite(degrees)) {
            throw new IllegalArgumentException("Arguments can not be NaN or Infinity.");
        }

        return new DMSAngle(degrees);
    }

    public void setDecimalDegrees(double degrees) {
        if (Double.isNaN(degrees) || Double.isInfinite(degrees)) {
            throw new IllegalArgumentException("Arguments can not be NaN or Infinity.");
        }
        this.angleDegrees = degrees;
        this.sign = (int) Math.signum(degrees);

        degrees = Math.abs(degrees);

        double degreeTemp = (int) Math.floor(degrees);
        double minuteTemp = (int) Math.floor((degrees - degreeTemp) * 60.0);
        double secondTemp = (3600.0 * (degrees - degreeTemp)) - (minuteTemp * 60.0);

        if (secondTemp >= 60.0) {
            secondTemp = 60.0 - secondTemp;
            minuteTemp += 1.0;
        }

        if (minuteTemp >= 60.0) {
            minuteTemp = 60.0 - minuteTemp;
            degreeTemp += 1.0;
        }

        this.degreeValue = (int) degreeTemp;
        this.minuteValue = (int) minuteTemp;
        this.secondValue = secondTemp;
    }

    public int getSign() {
        return this.sign;
    }

    public int getDegrees() {
        return this.degreeValue;
    }

    public int getMinutes() {
        return this.minuteValue;
    }

    public double getSeconds() {
        return this.secondValue;
    }

    public String toString() {
        return String.format("%d\u00b0 %02d' %05.3f\"", this.degreeValue, this.minuteValue, this.secondValue);
    }

    public String toString(String format) {
        String tmpFormat = format.replace("D", "1$");

        tmpFormat = tmpFormat.replace("M", "2$");
        tmpFormat = tmpFormat.replace("S", "3$");
        return String.format(tmpFormat, this.degreeValue, this.minuteValue, this.secondValue);
    }

    public void addDegrees(int deltaDegrees) {
        setDecimalDegrees(this.angleDegrees + (double) deltaDegrees);
    }

    public void addMinutes(int deltaMin) {
        setDecimalDegrees(this.angleDegrees + ((double) deltaMin * DEGREES_PER_MINUTE));
    }

    public void addSeconds(double deltaSec) {
        setDecimalDegrees(this.angleDegrees + (deltaSec * DEGREES_PER_SECOND));
    }

    public double toDD() {
        return this.angleDegrees;
    }
}
