package mil.emp3.api;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.IGeoCircle;
import org.cmapi.primitives.IGeoEllipse;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;
import org.cmapi.primitives.IGeoSquare;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import mil.emp3.api.utils.EmpGeoPosition;

/**
 * Test basic shapes Circle, Ellipse, Rectangle and Square for parameter validation.
 */
@RunWith(RobolectricTestRunner.class)
public class BasicShapesValidationTest extends TestBaseSingleMap {

    private static String TAG = BasicAddOverlayAndFeatureTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test (expected=IllegalArgumentException.class)
    public void invalidRadiusCircle() {
        Circle circle = new Circle(0.01);
    }

    @Test (expected=IllegalArgumentException.class)
    public void setInvalidRadiusCircle() {
        Circle circle = new Circle(100.0);
        circle.setRadius(.05);
    }

    @Test
    public void negativeCircleRadius() {
        Circle circle = new Circle(-10.0);
        Assert.assertEquals("Radius should be positive", 10.0, circle.getRadius(), .001);

        circle.setRadius(-120.0);
        Assert.assertEquals("Radius should be positive", 120.0, circle.getRadius(), .001);
    }

    @Test (expected=IllegalArgumentException.class)
    public void nullGeoCircle() {
        IGeoCircle geoCircle = null;
        Circle circle = new Circle(geoCircle);
    }

    @Test (expected=IllegalArgumentException.class)
    public void invalidGeoCircle() {
        IGeoCircle geoCircle = new GeoCircle();
        geoCircle.setRadius(0.02);
        Circle circle = new Circle(geoCircle);
    }

    @Test (expected=IllegalArgumentException.class)
    public void CircleRadiusNaN() {
        Circle circle = new Circle(Double.NaN);
    }

    // Ellipse
    @Test (expected=IllegalArgumentException.class)
    public void invalidMajorRadiusEllipse() {
        Ellipse ellipse = new Ellipse(0.01, 20.0);
    }

    @Test (expected=IllegalArgumentException.class)
    public void invalidMinorRadiusEllipse() {
        Ellipse ellipse = new Ellipse(20.0, .09);
    }

    @Test (expected=IllegalArgumentException.class)
    public void setInvalidMajorRadiusEllipse() {
        Ellipse ellipse = new Ellipse(100.0, 200.0);
        ellipse.setSemiMajor(.05);
    }

    @Test (expected=IllegalArgumentException.class)
    public void setInvalidMinorRadiusEllipse() {
        Ellipse ellipse = new Ellipse(100.0, 200.0);
        ellipse.setSemiMinor(.05);
    }

    @Test
    public void negativeMajorMinorRadius() {
        Ellipse ellipse = new Ellipse(-100.0, -200.0);
        Assert.assertEquals("Major Radius should be positive", 100.0, ellipse.getSemiMajor(), .001);
        Assert.assertEquals("Minor Radius should be positive", 200.0, ellipse.getSemiMinor(), .001);

        ellipse.setSemiMajor(-400.0);
        Assert.assertEquals("Major Radius should be positive", 400.0, ellipse.getSemiMajor(), .001);

        ellipse.setSemiMinor(-300.0);
        Assert.assertEquals("Minor Radius should be positive", 300.0, ellipse.getSemiMinor(), .001);
    }

    @Test (expected=IllegalArgumentException.class)
    public void nullGeoEllipse() {
        IGeoEllipse geoEllipse = null;
        Ellipse ellipse = new Ellipse(geoEllipse);
    }

    @Test (expected=IllegalArgumentException.class)
    public void invalidGeoEllipse() {
        IGeoEllipse geoEllipse = new GeoEllipse();
        geoEllipse.setAzimuth(-361.0);
        Ellipse ellipse = new Ellipse(geoEllipse);
    }

    @Test (expected=IllegalArgumentException.class)
    public void ellipseRadiusNaN() {
        Ellipse ellispe = new Ellipse(Double.NaN, Double.NaN);
    }

    @Test (expected=IllegalArgumentException.class)
    public void ellipseInvalidAzimuth() {
        Ellipse ellispe = new Ellipse(400.0, 200.0, 381);
    }

    // Rectangle
    @Test (expected=IllegalArgumentException.class)
    public void rectangleAtInvalidCenter() {
        IGeoPosition center = new GeoPosition();
        center.setLatitude(-91.0);
        Rectangle rectangle = new Rectangle(center);
    }

    @Test (expected=IllegalArgumentException.class)
    public void rectangleAtInvalidWidth() {
        EmpGeoPosition center = new EmpGeoPosition(0, 0);
        Rectangle rectangle = new Rectangle(center, 0.034, 200.0);
    }

    @Test (expected=IllegalArgumentException.class)
    public void rectangleAtInvalidHeight() {
        EmpGeoPosition center = new EmpGeoPosition(0, 0);
        Rectangle rectangle = new Rectangle(center, 34, 0.99);
    }

    @Test (expected=IllegalArgumentException.class)
    public void invalidGeoRectangle() {
        IGeoRectangle geoRectangle = new GeoRectangle();
        geoRectangle.setAzimuth(-400);
        Rectangle rectangle = new Rectangle(geoRectangle);
    }

    @Test (expected=IllegalArgumentException.class)
    public void negativeHeightWidthRectangle() {
        EmpGeoPosition center = new EmpGeoPosition(0, 0);
        Rectangle rectangle = new Rectangle(center, -400, -300);
    }

    // Square
    @Test (expected=IllegalArgumentException.class)
    public void squareAtInvalidCenter() {
        IGeoPosition center = new GeoPosition();
        center.setLatitude(-91.0);
        Square square = new Square(center);
    }

    @Test (expected=IllegalArgumentException.class)
    public void squareAtInvalidWidth() {
        EmpGeoPosition center = new EmpGeoPosition(0, 0);
        Square square = new Square(center, 0.034);
    }

    @Test (expected=IllegalArgumentException.class)
    public void invalidGeoSquare() {
        IGeoSquare geoSquare = new GeoSquare();
        geoSquare.setAzimuth(361);
        Square square = new Square(geoSquare);
    }

    @Test
    public void negativeWidthSquare() {
        EmpGeoPosition center = new EmpGeoPosition(0, 0);
        Square square = new Square(center, -400);

        Assert.assertEquals("Width should be positive", 400.0, square.getWidth(), .001);

    }

}