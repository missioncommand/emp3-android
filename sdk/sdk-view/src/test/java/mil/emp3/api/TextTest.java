package mil.emp3.api;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.GeoText;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;

import static mil.emp3.api.utils.ComparisonUtils.Epsilon;
import static mil.emp3.api.utils.ComparisonUtils.validateText;
import static org.junit.Assert.assertEquals;

/**
 * Created by Matt.Miller on 10/13/2017.
 */
public class TextTest extends TestBase{
    public static final String TAG = Text.class.getSimpleName();
    private Text t1;
    final private String testString = "test";

    @Test
    public void defaultConstructor() throws Exception {
        t1 = new Text();
        validateText(t1,
                     new GeoText(),
                     "",
                     0.0,
                     FeatureTypeEnum.GEO_TEXT,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     null,
                     Collections.EMPTY_LIST,
                     null,
                     new GeoStrokeStyle(),
                     new GeoFillStyle(),
                     new GeoLabelStyle(),
                     false,
                     true,
                     0.0,
                     0.0,
                     null,
                     false,
                     Collections.EMPTY_LIST,
                     false,
                     Collections.EMPTY_LIST,
                     "",
                     null,
                     "",
                     new HashMap());
    }

    @Test
    public void stringConstructor() {
        t1 = new Text(testString);
        final GeoText gt = new GeoText();
        gt.setName(testString);
        validateText(t1,
                     gt,
                     testString,
                     0.0,
                     FeatureTypeEnum.GEO_TEXT,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     null,
                     Collections.EMPTY_LIST,
                     null,
                     new GeoStrokeStyle(),
                     new GeoFillStyle(),
                     new GeoLabelStyle(),
                     false,
                     true,
                     0.0,
                     0.0,
                     null,
                     false,
                     Collections.EMPTY_LIST,
                     false,
                     Collections.EMPTY_LIST,
                     testString,
                     null,
                     "",
                     new HashMap());
    }

    @Test
    public void geoTextConstructor() {
        final GeoText gt = new GeoText();
        t1 = new Text(gt);
        validateText(t1,
                     gt,
                     "",
                     0.0,
                     FeatureTypeEnum.GEO_TEXT,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     null,
                     Collections.EMPTY_LIST,
                     null,
                     new GeoStrokeStyle(),
                     new GeoFillStyle(),
                     new GeoLabelStyle(),
                     false,
                     true,
                     0.0,
                     0.0,
                     null,
                     false,
                     Collections.EMPTY_LIST,
                     false,
                     Collections.EMPTY_LIST,
                     "",
                     null,
                     "",
                     new HashMap());
    }

    @Test
    public void setText() {
        t1 = new Text();
        t1.setText(testString);
        assertEquals(t1.getText(), testString);
    }

    @Test
    public void rotationAngle() {
        final double testAngle = 50.6;
        t1 = new Text();
        t1.setRotationAngle(testAngle);
        assertEquals(t1.getRotationAngle(), testAngle, Epsilon);
    }

    @Test
    public void negativeRotationAngle() {
        final double testAngle = -45.5;
        t1 = new Text();
        t1.setRotationAngle(testAngle);
        assertEquals(t1.getRotationAngle(), testAngle, Epsilon);
    }

    @Test
    public void defaultToString() {
        t1 = new Text();
        final String expectedText = "Text\n" +
                                    "\ttext: ";
        assertEquals(t1.toString(), expectedText);
    }

    @Test
    public void positionToString() {
        t1 = new Text();
        t1.setPosition(new GeoPosition());
        final String expectedText = "Text at\n" +
                                    "\tlatitude: 0.0\n" +
                                    "\tlongitude: 0.0\n" +
                                    "\taltitude: 0.0\n" +
                                    "\ttext: ";
        assertEquals(t1.toString(), expectedText);
    }
}