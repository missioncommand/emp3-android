package mil.emp3.test.emp3vv.navItems.basic_capability_test;

import android.app.Activity;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.Overlay;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;

/**
 * Base class for single point basic shape capability test.
 */

public class SinglePointBasicShapeCapabilityTest<T extends Feature> extends NavItemBase {
    private static String TAG = SinglePointBasicShapeCapabilityTest.class.getSimpleName();

    private static final int NO_TEST = 0;
    private static final int CLEAN_TEST = NO_TEST + 1;
    private static final int STROKE_COLOR_TEST = CLEAN_TEST + 1;
    private static final int FILL_COLOR_TEST = STROKE_COLOR_TEST + 1;
    private static final int WIDTH_TEST = FILL_COLOR_TEST + 1;
    private static final int HEIGHT_TEST = WIDTH_TEST + 1;
    private static final int AZIMUTH_TEST = HEIGHT_TEST + 1;
    private static final int POSITION_TEST = AZIMUTH_TEST + 1;
    private static final int STROKE_WIDTH_TEST = POSITION_TEST + 1;
    private static final int LABEL_COLOR_TEST = STROKE_WIDTH_TEST + 1;
    private static final int SEMI_MAJOR_MINOR_TEST = LABEL_COLOR_TEST + 1;
    private static final int RADIUS_TEST = SEMI_MAJOR_MINOR_TEST + 1;

    private static final int START_TEST_STEPS = 0;
    private static final int END_TEST_STEPS = START_TEST_STEPS + 1;

    private static final int COLOR_TEST_STEP_STROKE_COLOR = END_TEST_STEPS + 1;
    private static final int COLOR_TEST_STEP_STROKE_ALPHA_DOWN = COLOR_TEST_STEP_STROKE_COLOR + 1;
    private static final int COLOR_TEST_STEP_STROKE_ALPHA_UP = COLOR_TEST_STEP_STROKE_ALPHA_DOWN + 1;
    private static final int COLOR_TEST_STEP_FILL_COLOR = COLOR_TEST_STEP_STROKE_ALPHA_UP + 1;
    private static final int COLOR_TEST_STEP_FILL_ALPHA_DOWN = COLOR_TEST_STEP_FILL_COLOR + 1;
    private static final int COLOR_TEST_STEP_FILL_ALPHA_UP = COLOR_TEST_STEP_FILL_ALPHA_DOWN + 1;

    private static final int WH_TEST_STEP_WIDTH_DOWN = END_TEST_STEPS + 1;
    private static final int WH_TEST_STEP_WIDTH_UP = WH_TEST_STEP_WIDTH_DOWN + 1;
    private static final int WH_TEST_STEP_HEIGHT_DOWN = WH_TEST_STEP_WIDTH_UP + 1;
    private static final int WH_TEST_STEP_HEIGHT_UP = WH_TEST_STEP_HEIGHT_DOWN + 1;

    private static final int AZIMUTH_TEST_STEPS_UP = END_TEST_STEPS + 1;
    private static final int AZIMUTH_TEST_STEPS_DOWN = AZIMUTH_TEST_STEPS_UP + 1;

    private static final int SMM_TEST_MAJOR_STEPS_DOWN = END_TEST_STEPS + 1;
    private static final int SMM_TEST_MAJOR_STEPS_UP = SMM_TEST_MAJOR_STEPS_DOWN + 1;
    private static final int SMM_TEST_MINOR_STEPS_DOWN = SMM_TEST_MAJOR_STEPS_UP + 1;
    private static final int SMM_TEST_MINOR_STEPS_UP = SMM_TEST_MINOR_STEPS_DOWN + 1;

    private static final int STROKE_WIDTH_TEST_STEPS_UP = END_TEST_STEPS + 1;
    private static final int STROKE_WIDTH_TEST_STEPS_DOWN = STROKE_WIDTH_TEST_STEPS_UP + 1;

    private static final int RADIUS_TEST_STEPS_UP = END_TEST_STEPS + 1;
    private static final int RADIUS_TEST_STEPS_DOWN = RADIUS_TEST_STEPS_UP + 1;

    private static final int POSITION_TEST_STEPS_UP = END_TEST_STEPS + 1;

    private int currentTest = SinglePointBasicShapeCapabilityTest.NO_TEST;
    private int currentTestStep = 0;

    private IGeoColor strokeColor;
    private IGeoColor fillColor;
    private IGeoStrokeStyle strokeStyle;
    private IGeoFillStyle fillStyle;

    private Overlay rootOverlay;
    protected T testFeature;

    private double baseValue;
    private int tempAngle;
    private IGeoPosition viewCenter;


    public SinglePointBasicShapeCapabilityTest(Activity activity, IMap map1, IMap map2, T feature) {
        super(activity, map1, map2, TAG);

        strokeColor = new GeoColor();
        fillColor = new GeoColor();
        strokeStyle = new GeoStrokeStyle();
        fillStyle = new GeoFillStyle();

        strokeStyle.setStrokeColor(strokeColor);
        strokeStyle.setStrokeWidth(3);
        strokeStyle.setStipplingFactor(0);
        strokeStyle.setStipplingPattern((short) 0);

        fillStyle.setFillColor(fillColor);
        fillStyle.setFillPattern(IGeoFillStyle.FillPattern.solid);

        this.testFeature = feature;
    }

    protected boolean isNoTest() {
        return (this.currentTest == SinglePointBasicShapeCapabilityTest.NO_TEST);
    }
    protected double getFeatureWidth() {
        return 0;
    }

    protected void setFeatureWidth(double value) {}

    protected double getFeatureHeight() {
        return 0;
    }

    protected void setFeatureHeight(double value) {}

    protected double getFeatureSemiMajor() {
        return 0;
    }

    protected void setFeatureSemiMajor(double value){}

    protected double getFeatureSemiMinor() {
        return 0;
    }

    protected void setFeatureSemiMinor(double value){}

    protected double getFeatureRadius() {
        return 0;
    }

    protected void setFeatureRadius(double value) {}

    private void doStrokeColorTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(0);
                this.strokeColor.setBlue(0);
                this.strokeStyle.setStrokeWidth(3);

                this.testFeature.setFillStyle(null);
                updateStatus(TAG, "Stroke color Red, no fill.");
                this.currentTestStep = COLOR_TEST_STEP_STROKE_COLOR;
                break;
            case COLOR_TEST_STEP_STROKE_COLOR:
                if (this.strokeColor.getRed() == 255) {
                    this.strokeColor.setRed(0);
                    this.strokeColor.setGreen(255);
                    this.strokeColor.setBlue(0);
                    updateStatus(TAG, "Stroke color Green, no fill.");
                } else if (this.strokeColor.getGreen() == 255) {
                    this.strokeColor.setRed(0);
                    this.strokeColor.setGreen(0);
                    this.strokeColor.setBlue(255);
                    updateStatus(TAG, "Stroke color Blue, no fill.");
                } else {
                    this.currentTestStep = COLOR_TEST_STEP_STROKE_ALPHA_DOWN;
                    this.strokeColor.setAlpha(0.8);
                    updateStatus(TAG, "Stroke Alpha 0.8, no fill.");
                }
                break;
            case COLOR_TEST_STEP_STROKE_ALPHA_DOWN: {
                double dAlpha = this.strokeColor.getAlpha();

                if (dAlpha < 0.2) {
                    // End of alpha down.
                    this.strokeColor.setAlpha(0.2);
                    updateStatus(TAG, "Stroke Alpha 0.2.");
                    this.currentTestStep = COLOR_TEST_STEP_STROKE_ALPHA_UP;
                } else {
                    dAlpha -= 0.2;
                    if (dAlpha < 0.0) {
                        dAlpha = 0.0;
                    } else {
                        dAlpha = Math.floor(dAlpha * 10 + 0.5) / 10;
                    }
                    this.strokeColor.setAlpha(dAlpha);
                    updateStatus(TAG, "Stroke Alpha " + dAlpha + ", no fill.");
                }
                break;
            }
            case COLOR_TEST_STEP_STROKE_ALPHA_UP: {
                double dAlpha = this.strokeColor.getAlpha();

                if (dAlpha > 0.8) {
                    // End of alpha test.

                    this.strokeColor.setRed(0);
                    this.strokeColor.setGreen(0);
                    this.strokeColor.setBlue(255);
                    updateStatus(TAG, "Stroke Color done.");
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dAlpha += 0.2;
                    if (dAlpha > 1.0) {
                        dAlpha = 1.0;
                    } else {
                        dAlpha = Math.floor(dAlpha * 10 + 0.5) / 10;
                    }
                    updateStatus(TAG, "Stroke Alpha " + dAlpha + ", no fill.");
                    this.strokeColor.setAlpha(dAlpha);
                }
                break;
            }
            case END_TEST_STEPS:
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doFillColorTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.testFeature.setFillStyle(this.fillStyle);
                this.fillColor.setAlpha(1.0);
                this.fillColor.setRed(255);
                this.fillColor.setGreen(0);
                this.fillColor.setBlue(0);

                this.strokeColor.setRed(0);
                this.strokeColor.setGreen(0);
                this.strokeColor.setBlue(255);
                updateStatus(TAG, "Fill color Red.");
                this.currentTestStep = COLOR_TEST_STEP_FILL_COLOR;
                break;
            case COLOR_TEST_STEP_FILL_COLOR:
                if (this.fillColor.getRed() == 255) {
                    this.fillColor.setRed(0);
                    this.fillColor.setGreen(255);
                    this.fillColor.setBlue(0);
                    updateStatus(TAG, "Fill color Green.");
                } else if (this.fillColor.getGreen() == 255) {
                    this.fillColor.setRed(0);
                    this.fillColor.setGreen(0);
                    this.fillColor.setBlue(255);
                    this.strokeColor.setRed(255);
                    this.strokeColor.setBlue(0);
                    updateStatus(TAG, "Fill color Blue.");
                } else {
                    this.currentTestStep = COLOR_TEST_STEP_FILL_ALPHA_DOWN;
                    this.fillColor.setAlpha(0.8);
                    updateStatus(TAG, "Fill Alpha 0.8.");
                }
                break;
            case COLOR_TEST_STEP_FILL_ALPHA_DOWN: {
                double dAlpha = this.fillColor.getAlpha();

                if (dAlpha < 0.2) {
                    // End of alpha sdown.
                    this.fillColor.setAlpha(0.2);
                    updateStatus(TAG, "Fill Alpha 0.2.");
                    this.currentTestStep = COLOR_TEST_STEP_FILL_ALPHA_UP;
                } else {
                    dAlpha -= 0.2;
                    if (dAlpha < 0.0) {
                        dAlpha = 0.0;
                    } else {
                        dAlpha = Math.floor(dAlpha * 10 + 0.5) / 10;
                    }
                    this.fillColor.setAlpha(dAlpha);
                    updateStatus(TAG, "Fill Alpha " + dAlpha + ".");
                }
                break;
            }
            case COLOR_TEST_STEP_FILL_ALPHA_UP: {
                double dAlpha = this.fillColor.getAlpha();

                if (dAlpha > 0.8) {
                    // End of alpha test.
                    updateStatus(TAG, "Fill Color done.");
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dAlpha += 0.2;
                    if (dAlpha > 1.0) {
                        dAlpha = 1.0;
                    } else {
                        dAlpha = Math.floor(dAlpha * 10 + 0.5) / 10;
                    }
                    updateStatus(TAG, "Fill Alpha " + dAlpha + ".");
                    this.fillColor.setAlpha(dAlpha);
                }
                break;
            }
            case END_TEST_STEPS:
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doWidthTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.baseValue = this.getFeatureWidth();
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(0);
                this.strokeColor.setBlue(0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(255);
                this.fillColor.setBlue(0);

                updateStatus(TAG, "Width decrease.");
                this.currentTestStep = WH_TEST_STEP_WIDTH_DOWN;
                break;
            case WH_TEST_STEP_WIDTH_DOWN: {
                double dWidth = this.getFeatureWidth();

                if (dWidth < (this.baseValue / 2.0)) {
                    // now increase width
                    dWidth += (this.baseValue / 20.0);
                    this.setFeatureWidth(dWidth);
                    updateStatus(TAG, "Width increase.");
                    this.currentTestStep = WH_TEST_STEP_WIDTH_UP;
                } else {
                    dWidth -= (this.baseValue / 20.0);
                    this.setFeatureWidth(dWidth);
                }
                break;
            }
            case WH_TEST_STEP_WIDTH_UP: {
                double dWidth = this.getFeatureWidth();

                if (dWidth > (this.baseValue - (this.baseValue / 20.0))) {
                    // now increase width
                    dWidth = this.baseValue;
                    this.setFeatureWidth(dWidth);
                    updateStatus(TAG, "Width test done.");
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dWidth += (this.baseValue / 20.0);
                    this.setFeatureWidth(dWidth);
                }
                break;
            }
            case END_TEST_STEPS:
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doHeightTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.baseValue = this.getFeatureHeight();
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(0);
                this.strokeColor.setBlue(0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(255);
                this.fillColor.setBlue(0);

                updateStatus(TAG, "Height decrease.");
                this.currentTestStep = WH_TEST_STEP_HEIGHT_DOWN;
                break;
            case WH_TEST_STEP_HEIGHT_DOWN: {
                double dHeight = this.getFeatureHeight();

                if (dHeight < (this.baseValue / 2.0)) {
                    // now increase height
                    dHeight += (this.baseValue / 20.0);
                    this.setFeatureHeight(dHeight);
                    updateStatus(TAG, "Height increase.");
                    this.currentTestStep = WH_TEST_STEP_HEIGHT_UP;
                } else {
                    dHeight -= (this.baseValue / 20.0);
                    this.setFeatureHeight(dHeight);
                }
                break;
            }
            case WH_TEST_STEP_HEIGHT_UP: {
                double dHeight = this.getFeatureHeight();

                if (dHeight > (this.baseValue - (this.baseValue / 20.0))) {
                    // now increase width
                    dHeight = this.baseValue;
                    this.setFeatureHeight(dHeight);
                    updateStatus(TAG, "Height test done.");
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dHeight += (this.baseValue / 20.0);
                    this.setFeatureHeight(dHeight);
                }
                break;
            }
            case END_TEST_STEPS:
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doAzimuthSteps() {

        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(255);
                this.strokeColor.setBlue(0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(255);
                this.fillColor.setBlue(0);

                updateStatus(TAG, "Azimuth increase.");
                this.currentTestStep = AZIMUTH_TEST_STEPS_UP;
                break;
            case AZIMUTH_TEST_STEPS_UP: {
                double dAzimuth = this.testFeature.getAzimuth();

                if (dAzimuth >= 350) {
                    this.testFeature.setAzimuth(359.0);
                    updateStatus(TAG, "Azimuth decrease.");
                    this.currentTestStep = AZIMUTH_TEST_STEPS_DOWN;
                } else {
                    dAzimuth += 10.0;
                    this.testFeature.setAzimuth(dAzimuth);
                }
                break;
            }
            case AZIMUTH_TEST_STEPS_DOWN: {
                double dAzimuth = this.testFeature.getAzimuth();

                if (dAzimuth <= 10) {
                    this.testFeature.setAzimuth(0.0);
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dAzimuth -= 10.0;
                    this.testFeature.setAzimuth(dAzimuth);
                }
                break;
            }
            case END_TEST_STEPS:
                updateStatus(TAG, "Azimuth test done.");
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doPositionTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS: {
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(255);
                this.strokeColor.setBlue(0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(0);
                this.fillColor.setBlue(255);

                ICamera camera = maps[ExecuteTest.getCurrentMap()].getCamera();
                this.baseValue = camera.getAltitude() / 10;
                viewCenter = new GeoPosition();

                viewCenter.setLatitude(camera.getLatitude());
                viewCenter.setLongitude(camera.getLongitude());
                viewCenter.setAltitude(0);

                GeoLibrary.computePositionAt(0, this.baseValue, viewCenter, this.testFeature.getPosition());
                tempAngle = 0;

                updateStatus(TAG, "Position Test.");
                this.currentTestStep = POSITION_TEST_STEPS_UP;
                break;
            }
            case POSITION_TEST_STEPS_UP: {
                ICamera camera = maps[ExecuteTest.getCurrentMap()].getCamera();

                if (this.tempAngle >= 360.0) {
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    this.tempAngle += 10;
                    GeoLibrary.computePositionAt(this.tempAngle, this.baseValue, viewCenter, this.testFeature.getPosition());
                }
                break;
            }
            case END_TEST_STEPS:
                updateStatus(TAG, "Position test done.");
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doStrokeWidthTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS: {
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(255);
                this.strokeColor.setBlue(0);
                this.strokeStyle.setStrokeWidth(1.0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(0);
                this.fillColor.setBlue(255);

                updateStatus(TAG, "Stroke Width increase.");
                this.currentTestStep = SinglePointBasicShapeCapabilityTest.STROKE_WIDTH_TEST_STEPS_UP;
                break;
            }
            case STROKE_WIDTH_TEST_STEPS_UP: {
                double width = this.strokeStyle.getStrokeWidth();

                width = Math.rint(width + 1);
                this.strokeStyle.setStrokeWidth(width);
                updateStatus(TAG, "Stroke Width " + width + ".");
                //Log.d(TAG, "Stroke Width set to: " + width);
                if (width >= 10) {
                    this.currentTestStep = SinglePointBasicShapeCapabilityTest.STROKE_WIDTH_TEST_STEPS_DOWN;
                }
                break;
            }
            case STROKE_WIDTH_TEST_STEPS_DOWN: {
                double width = this.strokeStyle.getStrokeWidth();

                width = Math.rint(width - 1);
                this.strokeStyle.setStrokeWidth(width);
                updateStatus(TAG, "Stroke Width " + width + ".");
                //Log.d(TAG, "Stroke Width set to: " + width);
                if (width <= 1) {
                    this.currentTestStep = SinglePointBasicShapeCapabilityTest.END_TEST_STEPS;
                }
                break;
            }
            case END_TEST_STEPS:
                updateStatus(TAG, "Stroke Width test done.");
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doRadiusTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.baseValue = this.getFeatureRadius();
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(0);
                this.strokeColor.setBlue(0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(255);
                this.fillColor.setBlue(0);

                updateStatus(TAG, "Radius decrease.");
                this.currentTestStep = RADIUS_TEST_STEPS_DOWN;
                break;
            case RADIUS_TEST_STEPS_DOWN: {
                double dRadius = this.getFeatureRadius();

                if (dRadius < (this.baseValue / 2.0)) {
                    // now increase height
                    dRadius += (this.baseValue / 20.0);
                    this.setFeatureRadius(dRadius);
                    updateStatus(TAG, "Radius increase.");
                    this.currentTestStep = RADIUS_TEST_STEPS_UP;
                } else {
                    dRadius -= (this.baseValue / 20.0);
                    this.setFeatureRadius(dRadius);
                }
                break;
            }
            case RADIUS_TEST_STEPS_UP: {
                double dRadius = this.getFeatureRadius();

                if (dRadius > (this.baseValue - (this.baseValue / 20.0))) {
                    // now increase width
                    dRadius = this.baseValue;
                    this.setFeatureRadius(dRadius);
                    updateStatus(TAG, "Radius test done.");
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dRadius += (this.baseValue / 20.0);
                    this.setFeatureRadius(dRadius);
                }
                break;
            }
            case END_TEST_STEPS:
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    private void doSemiMajorMinorTestSteps() {
        switch (this.currentTestStep) {
            case START_TEST_STEPS:
                this.baseValue = this.getFeatureSemiMajor();
                this.strokeColor.setAlpha(1.0);
                this.strokeColor.setRed(255);
                this.strokeColor.setGreen(0);
                this.strokeColor.setBlue(0);

                this.fillColor.setRed(0);
                this.fillColor.setGreen(255);
                this.fillColor.setBlue(0);

                updateStatus(TAG, "Semi Major decrease.");
                this.currentTestStep = SMM_TEST_MAJOR_STEPS_DOWN;
                break;
            case SMM_TEST_MAJOR_STEPS_DOWN: {
                double dRadius = this.getFeatureSemiMajor();

                if (dRadius < (this.baseValue / 2.0)) {
                    // now increase height
                    dRadius += (this.baseValue / 20.0);
                    this.setFeatureSemiMajor(dRadius);
                    updateStatus(TAG, "Semi Major increase.");
                    this.currentTestStep = SMM_TEST_MAJOR_STEPS_UP;
                } else {
                    dRadius -= (this.baseValue / 20.0);
                    this.setFeatureSemiMajor(dRadius);
                }
                break;
            }
            case SMM_TEST_MAJOR_STEPS_UP: {
                double dRadius = this.getFeatureSemiMajor();

                if (dRadius > (this.baseValue - (this.baseValue / 20.0))) {
                    // now increase width
                    dRadius = this.baseValue;
                    this.setFeatureSemiMajor(dRadius);
                    this.baseValue = this.getFeatureSemiMinor();
                    updateStatus(TAG, "Semi Minor decrease.");
                    this.currentTestStep = SMM_TEST_MINOR_STEPS_DOWN;
                } else {
                    dRadius += (this.baseValue / 20.0);
                    this.setFeatureSemiMajor(dRadius);
                }
                break;
            }
            case SMM_TEST_MINOR_STEPS_DOWN: {
                double dRadius = this.getFeatureSemiMinor();

                if (dRadius < (this.baseValue / 2.0)) {
                    // now increase height
                    dRadius += (this.baseValue / 20.0);
                    this.setFeatureSemiMinor(dRadius);
                    updateStatus(TAG, "Semi Minor increase.");
                    this.currentTestStep = SMM_TEST_MINOR_STEPS_UP;
                } else {
                    dRadius -= (this.baseValue / 20.0);
                    this.setFeatureSemiMinor(dRadius);
                }
                break;
            }
            case SMM_TEST_MINOR_STEPS_UP: {
                double dRadius = this.getFeatureSemiMinor();

                if (dRadius > (this.baseValue - (this.baseValue / 20.0))) {
                    // now increase width
                    dRadius = this.baseValue;
                    this.setFeatureSemiMinor(dRadius);
                    updateStatus(TAG, "Semi Major Minor done.");
                    this.currentTestStep = END_TEST_STEPS;
                } else {
                    dRadius += (this.baseValue / 20.0);
                    this.setFeatureSemiMinor(dRadius);
                }
                break;
            }
            case END_TEST_STEPS:
                this.currentTest = CLEAN_TEST;
                break;
            default:
                break;
        }
    }

    @Override
    protected void test0() {
        try {
            while (!Thread.interrupted()) {
                try {
                    switch (currentTest) {
                        case NO_TEST:
                            Thread.sleep(small_waitInterval);
                            break;
                        case CLEAN_TEST:
                            this.removeFeature();
                            this.removeOverlays();
                            this.currentTest = NO_TEST;
                            break;
                        case STROKE_COLOR_TEST:
                            doStrokeColorTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(small_waitInterval * 2);
                            break;
                        case STROKE_WIDTH_TEST:
                            doStrokeWidthTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(500);
                            break;
                        case FILL_COLOR_TEST:
                            doFillColorTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(small_waitInterval * 2);
                            break;
                        case WIDTH_TEST:
                            doWidthTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(250);
                            break;
                        case HEIGHT_TEST:
                            doHeightTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(250);
                            break;
                        case AZIMUTH_TEST:
                            doAzimuthSteps();
                            this.testFeature.apply();
                            Thread.sleep(250);
                            break;
                        case POSITION_TEST:
                            doPositionTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(250);
                            break;
                        case RADIUS_TEST:
                            doRadiusTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(250);
                            break;
                        case SEMI_MAJOR_MINOR_TEST:
                            doSemiMajorMinorTestSteps();
                            this.testFeature.apply();
                            Thread.sleep(250);
                            break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            clearMaps();
            endTest();
        }
    }

    @Override
    protected boolean exitTest() {
        String userAction = "Exit";
        return(actOn(userAction));
    }

    @Override
    public boolean actOn(String userAction) {
        try {
            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if (userAction.equals("ClearMap")) {
                clearMaps();
            } else if (this.currentTest == SinglePointBasicShapeCapabilityTest.NO_TEST) {
                if (userAction.equals("Stroke Color")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.STROKE_COLOR_TEST;
                        }
                    }
                } else if (userAction.equals("Stroke Width")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.STROKE_WIDTH_TEST;
                        }
                    }
                } else if (userAction.equals("Fill Color")) {
                        clearMaps();
                        setCameraLocation();
                        if (createOverlay()) {
                            if (prepareFeatures()) {
                                this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                                this.currentTest = SinglePointBasicShapeCapabilityTest.FILL_COLOR_TEST;
                            }
                        }
                } else if (userAction.equals("Width")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.WIDTH_TEST;
                        }
                    }
                } else if (userAction.equals("Height")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.HEIGHT_TEST;
                        }
                    }
                } else if (userAction.equals("Azimuth")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.AZIMUTH_TEST;
                        }
                    }
                } else if (userAction.equals("Position")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.POSITION_TEST;
                        }
                    }
                } else if (userAction.equals("Radius")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.RADIUS_TEST;
                        }
                    }
                } else if (userAction.equals("Radii")) {
                    clearMaps();
                    setCameraLocation();
                    if (createOverlay()) {
                        if (prepareFeatures()) {
                            this.currentTestStep = SinglePointBasicShapeCapabilityTest.START_TEST_STEPS;
                            this.currentTest = SinglePointBasicShapeCapabilityTest.SEMI_MAJOR_MINOR_TEST;
                        }
                    }
                }
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private void setCameraLocation() {
        ICamera camera = maps[ExecuteTest.getCurrentMap()].getCamera();

        camera.setLongitude(-106);
        camera.setLatitude(33);
        camera.setAltitude(80000);
        camera.apply(false);
    }

    private boolean createOverlay() {
        this.rootOverlay = new Overlay();
        this.rootOverlay.setName("Root Overlay");
        try {
            this.maps[ExecuteTest.getCurrentMap()].addOverlay(this.rootOverlay, true);
            return true;
        } catch (EMP_Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean prepareFeatures() {
        ICamera camera = maps[ExecuteTest.getCurrentMap()].getCamera();
        IGeoPosition pos = new GeoPosition();

        pos.setLongitude(camera.getLongitude());
        pos.setLatitude(camera.getLatitude());
        pos.setAltitude(0);

        this.testFeature.setPosition(pos);
        this.testFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        this.testFeature.setStrokeStyle(this.strokeStyle);
        this.testFeature.setFillStyle(this.fillStyle);

        this.testFeature.setName("Test Feature");

        try {
            this.rootOverlay.addFeature(this.testFeature, true);
            return true;
        } catch (EMP_Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private void removeFeature() {
        try {
            this.rootOverlay.removeFeature(this.testFeature);
        } catch (EMP_Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeOverlays() {
        try {
            this.maps[ExecuteTest.getCurrentMap()].removeOverlay(this.rootOverlay);
            this.rootOverlay = null;
        } catch (EMP_Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
