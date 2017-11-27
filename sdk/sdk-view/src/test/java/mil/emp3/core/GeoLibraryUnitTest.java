package mil.emp3.core;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.GeographicLib;

/**
 * This class tests the Geo Library method.
 */
@RunWith(RobolectricTestRunner.class)
public class GeoLibraryUnitTest
{
    private static final String TAG                       = GeoLibraryUnitTest.class.getSimpleName();
    private static final double NAUTICAL_MILE_IN_METERS   = 1855.325;
    private static final double METERS_1_DEGREE           = NAUTICAL_MILE_IN_METERS * 60;
    private static final double DISTANCE_ERROR_PER_DEGREE = 200.0; // meters
    private static final double BEARING_DELTA             = 0.3;// arbitrary
    private static final double Epsilon                   = 1e-8;    //With nanometer precision

    //Distance Tests
    @Test
    public void testDistance() throws FileNotFoundException, URISyntaxException
    {
        //test values were generated from the following website:
        //https://geographiclib.sourceforge.io/cgi-bin/GeodSolve?type=I
        String fileName = "expectedDistanceValues.csv";
        testCsvValues(fileName, (start, end) -> GeographicLib.computeDistanceBetween(start,end));
    }


    //Compute RhumbDistance Test
    @Test
    public void testRhumblineDistance() throws FileNotFoundException, URISyntaxException
    {
        //test values were generated from the following website:
        //https://geographiclib.sourceforge.io/cgi-bin/RhumbSolve?type=I
        String fileName = "expectedRhumbDistanceValues.csv";
        testCsvValues(fileName, (start, end) -> GeographicLib.computeRhumbDistance(start,end));
    }

    private void testCsvValues(final String              fileName,
                               final IDistanceCalculator calculator) throws FileNotFoundException, URISyntaxException
    {
        //https://geographiclib.sourceforge.io/cgi-bin/GeodSolve?type=I

        //get the .csv file with test data values
        final File coordinatePointsFile = loadFileFromDisk(fileName);
        try(Scanner scanner = new Scanner(coordinatePointsFile))
        {
            scanner.useDelimiter("\n");
            //read the test data
            final ArrayList<TestDistanceData> testDistanceData = this.readTestDistanceDataValuesFromFile(scanner);
            //create a map of the invalid distance calculations where the TestDistanceData is the data
            //the distance calculation was on and the value is the actual value of the distance the calculator returned
            final HashMap<TestDistanceData, Double> invalidValues = new HashMap<>();
            //test each one of the test data
            for (final TestDistanceData testData : testDistanceData)
            {
                //use the calculator to get the actual distance
                final double actualDistance = calculator.calculateDistance(testData.getStartPosition(), testData.getEndPosition());
                //check if the actual distance is within a nanometer of precision of the expected distance
                if(!isEqualEpsilon(actualDistance, testData.expectedDistance))
                {
                    //add this to the invalid values
                    invalidValues.put(testData, actualDistance);
                }
            }

            //create an error message if there were any invalid calculations
            if(invalidValues.size() != 0)
            {
                //create an error title
                final String errorTitle = String.format("Number of incorrect coordinates: %d out of %d\n" +
                                                                "Following coordinates did not get the correct distance.\n.",
                                                        invalidValues.size(),
                                                        testDistanceData.size());

                final StringBuilder errorMessage = new StringBuilder(errorTitle);
                //create the list of coordinates and distances that were wrong
                for (final TestDistanceData failedData : invalidValues.keySet())
                {
                    errorMessage.append(String.format("\t Start: %s End: %s Expected: %.10f  Actual: %.10f \n",
                                                      convertToString(failedData.startPosition),
                                                      convertToString(failedData.endPosition),
                                                      failedData.getExpectedDistance(),
                                                      invalidValues.get(failedData)));
                }
                //fail with the error message
                Assert.fail(errorMessage.toString());
            }
        }
    }


    @Test
    public void computeBearingTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computeBearingTest.");
        final IGeoPosition oPos1 = new GeoPosition();
        final IGeoPosition oPos2 = new GeoPosition();
        double             dAzimuth;

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);

        oPos2.setAltitude(0.0);

        oPos2.setLatitude(1.0);
        oPos2.setLongitude(0.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 0 degrees.");
        Assert.assertEquals(0.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(1.0);
        oPos2.setLongitude(1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 45 degrees.");
        Assert.assertEquals(45.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 90 degrees.");
        Assert.assertEquals(90.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(-1.0);
        oPos2.setLongitude(1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 135 degrees.");
        Assert.assertEquals(135.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(-1.0);
        oPos2.setLongitude(0.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 180 degrees.");
        Assert.assertEquals(180.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(-1.0);
        oPos2.setLongitude(-1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 225 degrees.");
        Assert.assertEquals(225.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(-1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 270 degrees.");
        Assert.assertEquals(270.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(1.0);
        oPos2.setLongitude(-1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 315 degrees.");
        Assert.assertEquals(315.0, dAzimuth, BEARING_DELTA);

        Log.d(TAG, "End computeBearingTest.");
    }

    private void positionAtTest(final double dBearing, final double dDegreeDist, final IGeoPosition oPos1) throws EMP_Exception, InterruptedException {
        final IGeoPosition oPos2;
        final double       dDist = METERS_1_DEGREE * dDegreeDist;
        final double       dCalculateDist;
        final double       dCalculateBearing;

        oPos2 = GeographicLib.computePositionAt(dBearing, dDist, oPos1);
        Log.d(TAG, "    Testing " + dBearing + "degrees Dist: " + dDist + " m.");
        Log.d(TAG, "            Bearing.");
        dCalculateBearing = GeographicLib.computeBearing(oPos1, oPos2);
        Assert.assertEquals(dBearing, dCalculateBearing, BEARING_DELTA);
        Log.d(TAG, "            Distance.");
        dCalculateDist = GeographicLib.computeDistanceBetween(oPos1, oPos2);
        Assert.assertEquals(dDist, dCalculateDist, DISTANCE_ERROR_PER_DEGREE * dDegreeDist);
    }

    @Test
    public void computePositionAtTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computePositionAtTest.");
        final IGeoPosition oPos1 = new GeoPosition();

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);
        this.positionAtTest(0.0, 0, oPos1);

        this.positionAtTest(0.0, 1, oPos1);
        this.positionAtTest(45.0, 1, oPos1);
        this.positionAtTest(90.0, 1, oPos1);
        this.positionAtTest(135.0, 1, oPos1);
        this.positionAtTest(180.0, 1, oPos1);
        this.positionAtTest(225.0, 1, oPos1);
        this.positionAtTest(270.0, 1, oPos1);
        this.positionAtTest(315.0, 1, oPos1);

        this.positionAtTest(0.0, 10, oPos1);
        this.positionAtTest(45.0, 10, oPos1);
        this.positionAtTest(90.0, 10, oPos1);
        this.positionAtTest(135.0, 10, oPos1);
        this.positionAtTest(180.0, 10, oPos1);
        this.positionAtTest(225.0, 10, oPos1);
        this.positionAtTest(270.0, 10, oPos1);
        this.positionAtTest(315.0, 10, oPos1);

        this.positionAtTest(0.0, 100, oPos1);
        this.positionAtTest(45.0, 100, oPos1);
        this.positionAtTest(90.0, 100, oPos1);
        this.positionAtTest(135.0, 100, oPos1);
        this.positionAtTest(180.0, 100, oPos1);
        this.positionAtTest(225.0, 100, oPos1);
        this.positionAtTest(270.0, 100, oPos1);
        this.positionAtTest(315.0, 100, oPos1);

        Log.d(TAG, "End computePositionAtTest.");
    }

    @Test
    public void midPointBetweenTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting midPointBetweenTest.");
        final IGeoPosition oTopLeft     = new GeoPosition();
        final IGeoPosition oTopRight    = new GeoPosition();
        final IGeoPosition oBottomLeft  = new GeoPosition();
        final IGeoPosition oBottomRight = new GeoPosition();
        final IGeoPosition oCenter1;
        final IGeoPosition oCenter2;

        oTopLeft.setLatitude(1.0);
        oTopLeft.setLongitude(-1.0);
        oTopLeft.setAltitude(0.0);

        oTopRight.setLatitude(1.0);
        oTopRight.setLongitude(1.0);
        oTopRight.setAltitude(0.0);

        oBottomLeft.setLatitude(-1.0);
        oBottomLeft.setLongitude(-1.0);
        oBottomLeft.setAltitude(0.0);

        oBottomRight.setLatitude(-1.0);
        oBottomRight.setLongitude(1.0);
        oBottomRight.setAltitude(0.0);

        oCenter1 = GeographicLib.midPointBetween(oTopLeft, oBottomRight);
        oCenter2 = GeographicLib.midPointBetween(oBottomLeft, oTopRight);
        Log.d(TAG, "  Testing Top Left to Bottom Right latitude.");
        Assert.assertEquals(0.0, oCenter1.getLatitude(), 0.1);
        Log.d(TAG, "  Testing Top Left to Bottom Right longitude.");
        Assert.assertEquals(0.0, oCenter1.getLongitude(), 0.1);
        Log.d(TAG, "  Testing Bottom Left to top Right latitude.");
        Assert.assertEquals(0.0, oCenter2.getLatitude(), 0.1);
        Log.d(TAG, "  Testing Bottom Left to top Right longitude.");
        Assert.assertEquals(0.0, oCenter2.getLongitude(), 0.1);

        Log.d(TAG, "End midPointBetweenTest.");
    }

    @Test
    public void computeRhumbDistanceTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computeRhumbDistanceTest.");
        final IGeoPosition oPos1 = new GeoPosition();
        final IGeoPosition oPos2 = new GeoPosition();

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(1.0);
        oPos2.setAltitude(0.0);

        double dDist = GeographicLib.computeRhumbDistance(oPos1, oPos2);
        Log.d(TAG, "    Testing 1 degree.");
        Assert.assertEquals(METERS_1_DEGREE, dDist, DISTANCE_ERROR_PER_DEGREE);

        oPos2.setLongitude(10.0);
        dDist = GeographicLib.computeRhumbDistance(oPos1, oPos2);
        Log.d(TAG, "    Testing 10 degree.");
        Assert.assertEquals(METERS_1_DEGREE * 10, dDist, DISTANCE_ERROR_PER_DEGREE * 10.0);

        oPos2.setLongitude(100.0);
        dDist = GeographicLib.computeRhumbDistance(oPos1, oPos2);
        Log.d(TAG, "    Testing 100 degree.");
        Assert.assertEquals(METERS_1_DEGREE * 100, dDist, DISTANCE_ERROR_PER_DEGREE * 100.0);

        Log.d(TAG, "End computeRhumbDistanceTest.");
    }

    /**
     * Takes the {@link String) name of a file in the test resources and returns a valid {@link File} object.
     *
     * @param fileName {@link String} file name that must reside in the test resources folder
     *
     * @return A {@link File} object that can correctly work on multiple platforms
     *
     * @throws URISyntaxException If an error occurs while getting the resource
     */
    private File loadFileFromDisk(final String fileName) throws URISyntaxException
    {
        return new File(this.getClass().getClassLoader().getResource(fileName).toURI());
    }

    /**
     * Container to hold test distance data
     * (start, end, expected distance)
     */
    private class TestDistanceData
    {
        private final IGeoPosition startPosition;
        private final IGeoPosition endPosition;
        private final double expectedDistance;

        /**
         * Constructor
         * @param start start location
         * @param end end location
         * @param expectedDistance the expected distance between the two points
         */
        private TestDistanceData(final IGeoPosition start,
                                 final IGeoPosition end,
                                 final double       expectedDistance)
        {
            this.startPosition    = start;
            this.endPosition      = end;
            this.expectedDistance = expectedDistance;
        }

        public IGeoPosition getStartPosition()
        {
            return this.startPosition;
        }

        public IGeoPosition getEndPosition()
        {
            return this.endPosition;
        }

        public double getExpectedDistance()
        {
            return this.expectedDistance;
        }
    }

    /**
     * Interface to calculate distances
     */
    private interface IDistanceCalculator
    {
        double calculateDistance(IGeoPosition start, IGeoPosition end);
    }

    /**
     * GeoPosition stringify
     * @param geoPosition the position to convert to a readable string value
     * @return the position in a human readable string
     */
    private String convertToString(final IGeoPosition geoPosition)
    {
        return String.format("(%f, %f, %f) (Longitude, Latitude, Altitude)",
                             geoPosition.getLongitude(),
                             geoPosition.getLatitude(),
                             geoPosition.getAltitude());
    }

    /***
     * Converts the scanner values into TestDistanceData
     * @param scanner the scanner of the csv file
     * @return the list of TestDistanceData values from the csv file
     */
    private ArrayList<TestDistanceData> readTestDistanceDataValuesFromFile(final Scanner scanner)
    {
        final ArrayList<TestDistanceData> testDistanceData = new ArrayList<>();
        scanner.next();//skip header information
        while(scanner.hasNext())
        {
            final String line = scanner.next();
            final String[] values = line.split(",", 5);
            //format should be lat1, long1, lat2, long2, expectedDistance
            final TestDistanceData coordinate = new TestDistanceData(createGeoPosition(Double.parseDouble(values[0]),Double.parseDouble(values[1]), 0.0),
                                                                     createGeoPosition(Double.parseDouble(values[2]),Double.parseDouble(values[3]), 0.0),
                                                                     Double.parseDouble(values[4]));
            testDistanceData.add(coordinate);
        }
        return testDistanceData;
    }

    /**
     * returns true if value1 equals value2 with a nanometer precision
     * @param value1 first value
     * @param value2 second value
     * @return true if value1 equals value2 with a nanometer precision, false otherwise
     */
    private boolean isEqualEpsilon(final double value1,
                                   final double value2)
    {
        return value1 == value2 ? true : Math.abs(value1 - value2) < Epsilon;
    }

    /***
     * Helper function to create GeoPositions
     * @param latitude latitude value in degrees
     * @param longitude longitude value in degrees
     * @param altitude altitude value in meters
     * @return Geoposition with the given coordinates
     */
    private static IGeoPosition createGeoPosition(final double latitude,
                                                  final double longitude,
                                                  final double altitude)
    {
        final IGeoPosition position = new GeoPosition();

        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setAltitude(altitude);

        return position;
    }
}
