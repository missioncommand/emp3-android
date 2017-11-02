package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.GeoCamera;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoText;
import org.cmapi.primitives.IGeoPosition;
import org.junit.Assert;

import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/13/2017.
 */
public class TextTest extends TestBase{
    public static final String TAG = Text.class.getSimpleName();

    @org.junit.Test
    public void basicTextTest() throws Exception {
        Text textObject;
        textObject = new Text();
        textObject = new Text("hi");
        textObject.setText("");
        assertEquals(textObject.getText(), "");
        textObject.setText("test");
        assertEquals(textObject.getText(), "test");

        GeoText geoTextObject = new GeoText();
        textObject = new Text(geoTextObject);

        //check default value
        assertEquals(textObject.getRotationAngle(), 0,0);
        textObject.setRotationAngle(50.6);
        assertEquals(textObject.getRotationAngle(), 50.6, 0);
        textObject.setRotationAngle(-45.5);
        assertEquals(textObject.getRotationAngle(), -45.5, 0);

        Log.i(TAG, textObject.toString());
        IGeoPosition iGeoPositionTestObject = new GeoPosition();
        textObject.setPosition(iGeoPositionTestObject);
        Log.i(TAG, textObject.toString());
    }

}