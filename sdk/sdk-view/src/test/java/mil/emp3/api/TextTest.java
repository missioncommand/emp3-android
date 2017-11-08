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
    private Text text1;
    final private String testString = "test";

    @Test
    public void defaultConstructor() throws Exception {
        text1 = new Text();
        final GeoText geoText = new GeoText();
        geoText.setTimeStamp(text1.getTimeStamp());
        validateText(text1,
                     geoText,
                     "",
                     0.0,
                     FeatureTypeEnum.GEO_TEXT,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     geoText.getTimeStamp(),
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
        text1 = new Text(testString);
        final GeoText geoText = new GeoText();
        geoText.setName(testString);
        geoText.setTimeStamp(text1.getTimeStamp());
        validateText(text1,
                     geoText,
                     testString,
                     0.0,
                     FeatureTypeEnum.GEO_TEXT,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     geoText.getTimeStamp(),
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
        final GeoText geoText = new GeoText();
        text1 = new Text(geoText);
        geoText.setTimeStamp(text1.getTimeStamp());
        validateText(text1,
                     geoText,
                     "",
                     0.0,
                     FeatureTypeEnum.GEO_TEXT,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     Collections.EMPTY_LIST,
                     geoText.getTimeStamp(),
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
        text1 = new Text();
        text1.setText(testString);
        assertEquals(text1.getText(), testString);
    }

    @Test
    public void rotationAngle() {
        final double testAngle = 50.6;
        text1 = new Text();
        text1.setRotationAngle(testAngle);
        assertEquals(text1.getRotationAngle(), testAngle, Epsilon);
    }

    @Test
    public void negativeRotationAngle() {
        final double testAngle = -45.5;
        text1 = new Text();
        text1.setRotationAngle(testAngle);
        assertEquals(text1.getRotationAngle(), testAngle, Epsilon);
    }

    @Test
    public void defaultToString() {
        text1 = new Text();
        final String expectedText = "Text\n" +
                "\ttext: ";
        assertEquals(text1.toString(), expectedText);
    }

    @Test
    public void positionToString() {
        text1 = new Text();
        text1.setPosition(new GeoPosition());
        final String expectedText = "Text at\n" +
                "\tlatitude: 0.0\n" +
                "\tlongitude: 0.0\n" +
                "\taltitude: 0.0\n" +
                "\ttext: ";
        assertEquals(text1.toString(), expectedText);
    }
}