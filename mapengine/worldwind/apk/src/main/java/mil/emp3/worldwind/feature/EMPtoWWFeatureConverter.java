package mil.emp3.worldwind.feature;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.shape.SurfaceImage;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IImageLayer;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.utils.Conversion;

/**
 * This class provides method to convert EMP feature to their WW equivalent
 */

public class EMPtoWWFeatureConverter {
    final static private String TAG = FeatureRenderableMapping.class.getSimpleName();

    // The display density of the current device.
    private static final double DISPLAY_PIXEL_DENSITY = (double) Resources.getSystem().getDisplayMetrics().densityDpi;

    // The width of the text outline factored with the display density.
    private static final float TEXT_OUTLINE_WIDTH = (float) (0.0125 * DISPLAY_PIXEL_DENSITY);

    // The stippling factor provided by the GeoStrokeStyle applies to a display with 96 dpi. Therefore we need
    // to apply an additional factor to account for the display density.
    private static final double STIPPLE_FACTOR_DISPLAY_DENSITY_MODIFIER = DISPLAY_PIXEL_DENSITY / 96.0;

    private final MapInstance mapInstance;

    public EMPtoWWFeatureConverter(MapInstance instance) {
        this.mapInstance = instance;
    }

    public MapInstance getMapInstance() {
        return mapInstance;
    }

    /**
     * This method creates a WW placemark from an EMP Point feature.
     * @param feature The EMP Point feature.
     * @param isSelected  True if the placemark is to be selected, false otherwise.
     * @return A WW placemark or null if one is not created.
     */
    public Placemark createPlacemark(mil.emp3.api.Point feature, boolean isSelected) {
        Offset imageOffset;
        PlacemarkAttributes oAttr;
        IGeoIconStyle oIconStyle = feature.getIconStyle();
        IGeoPosition oPos = feature.getPosition();
        String sURL = feature.getIconURI();
        IconSizeEnum iconSizeEnum = getMapInstance().getIconSizeSetting();
        double dScale = iconSizeEnum.getScaleFactor() * feature.getIconScale();

        if (oPos == null) {
            Log.e(TAG, "Point feature with no coordinate.");
            return null;
        }

        if (isSelected) {
            dScale = dScale * getMapInstance().getEmpResources().getSelectedIconScale(getMapInstance());
        }

        if (feature.getResourceId() != 0) {
            IEmpImageInfo imageInfo =  getMapInstance().getEmpResources().getAndroidResourceIconImageInfo(feature.getResourceId());

            if (imageInfo == null) {
                Log.e(TAG, "Android resource for point feature not found.");
                return null;
            }

            Rect imageBounds = imageInfo.getImageBounds();      // The bounds of the entire image, including text

            if (oIconStyle != null) {
                imageOffset = new Offset(
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, oIconStyle.getOffSetX(), // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, 1.0 - oIconStyle.getOffSetY()); // y offset
            } else {
                imageOffset = new Offset(gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, 0, // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, imageBounds.height()); // y offset
            }
            oAttr = PlacemarkAttributes.createWithImage(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(imageOffset);
        } else if ((sURL == null) || (sURL.length() == 0)) {
            IEmpImageInfo imageInfo =  getMapInstance().getEmpResources().getDefaultIconImageInfo();

            if (imageInfo == null) {
                Log.e(TAG, "Failed to load default icon for point feature.");
                return null;
            }

            Rect imageBounds = imageInfo.getImageBounds();      // The bounds of the entire image, including text
            Point centerPoint = imageInfo.getCenterPoint();     // The center of the core symbol
            IGeoIconStyle defaultIconStyle = getMapInstance().getEmpResources().getDefaultIconStyle();

            if (defaultIconStyle != null) {
                oIconStyle = defaultIconStyle;
            } else {
                Log.w(TAG, "Default icon style returned null.");
            }
            imageOffset = new Offset(
                    gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, oIconStyle.getOffSetX(), // x offset
                    gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, 1.0 - (oIconStyle.getOffSetY() / imageBounds.height())); // y offset
            oAttr = PlacemarkAttributes.createWithImage(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(imageOffset);
        } else {
            if (oIconStyle != null) {
                imageOffset = new Offset(
                        gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, oIconStyle.getOffSetX(), // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, oIconStyle.getOffSetY() * -1); // y offset
            } else {
                imageOffset = new Offset(
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, 0, // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, 1.0); // y offset
            }
            oAttr = PlacemarkAttributes.createWithImage(ImageSource.fromUrl(sURL)).setImageOffset(imageOffset);
        }

        oAttr.setImageScale(dScale);

        Placemark oIcon = new Placemark(
                Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()),
                oAttr);

        oIcon.setPickDelegate(feature);

        return oIcon;
    }

    public gov.nasa.worldwind.shape.Path createWWPath(Path feature, boolean isSelected) {
        gov.nasa.worldwind.shape.Path wwPath = null;
        IGeoStrokeStyle selectedStrokeStyle = null;

        if (feature.getPositions().size() < 2) {
            Log.i(TAG, "Path feature (" + feature.getName() + ") with less than 2 points.");
            return wwPath;
        }

        if (isSelected) {
            selectedStrokeStyle = getMapInstance().getEmpResources().getSelectedStrokeStyle(getMapInstance());
        }

        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        wwPath = new gov.nasa.worldwind.shape.Path(wwPositionList, shapeAttribute);

        setWWPathAttributes(feature, wwPath, selectedStrokeStyle);

        wwPath.setPickDelegate(feature);

        return wwPath;
    }

    public gov.nasa.worldwind.shape.Polygon createWWPolygon(Polygon feature, boolean isSelected) {
        IGeoStrokeStyle selectedStrokeStyle = null;
        gov.nasa.worldwind.shape.Polygon wwPolygon = null;

        if (feature.getPositions().size() < 2) {
            Log.i(TAG, "Path feature (" + feature.getName() + ") with less than 2 points.");
            return wwPolygon;
        }

        if (isSelected) {
            selectedStrokeStyle = getMapInstance().getEmpResources().getSelectedStrokeStyle(getMapInstance());
        }

        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        wwPolygon = new gov.nasa.worldwind.shape.Polygon(wwPositionList, shapeAttribute);

        setWWPolygonAttributes(feature, wwPolygon, selectedStrokeStyle);

        wwPolygon.setPickDelegate(feature);
        wwPolygon.setPathType(WorldWind.GREAT_CIRCLE);

        return wwPolygon;
    }

    public gov.nasa.worldwind.shape.Label createWWLabel(Text feature, boolean isSelected) {
        gov.nasa.worldwind.shape.Label wwLabel = null;
        IGeoLabelStyle selectedLabelStyle;
        IGeoLabelStyle labelStyle;
        gov.nasa.worldwind.geom.Position textPosition;
        gov.nasa.worldwind.shape.TextAttributes textAttribute;
        int fontPixelSize;

        if (feature.getPositions().size() < 1) {
            Log.i(TAG, "Path feature (" + feature.getName() + ") with no points.");
            return wwLabel;
        }

        IGeoPosition textFeaturePosition = feature.getPosition();

        textPosition = new gov.nasa.worldwind.geom.Position();
        textAttribute = new gov.nasa.worldwind.shape.TextAttributes();

        labelStyle = feature.getLabelStyle();
        textPosition.set(textFeaturePosition.getLatitude(), textFeaturePosition.getLongitude(), textFeaturePosition.getAltitude());

        gov.nasa.worldwind.geom.Offset textOffset;

        if (isSelected) {
            selectedLabelStyle = getMapInstance().getEmpResources().getSelectedLabelStyle(getMapInstance());
            if (null != selectedLabelStyle) {
                textAttribute.setTextColor(Conversion.covertColor(selectedLabelStyle.getColor()));
            }
            textAttribute.setEnableOutline(true);
            textAttribute.setOutlineWidth(TEXT_OUTLINE_WIDTH);
        }

        if (null != labelStyle) {
            if (!isSelected && (null != labelStyle.getColor())) {
                textAttribute.setTextColor(Conversion.covertColor(labelStyle.getColor()));
            }

            switch (labelStyle.getJustification()) {
                case LEFT:
                    textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
                    break;
                case RIGHT:
                    textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_INSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
                    break;
                case CENTER:
                default:
                    textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5);
                    break;
            }

            String fontFamily = labelStyle.getFontFamily();

            if ((null == fontFamily) || fontFamily.isEmpty()) {
                fontFamily = "Ariel";
            }

            switch (labelStyle.getTypeface()) {
                case BOLD:
                    textAttribute.setTypeface(Typeface.create(fontFamily, Typeface.BOLD));
                    break;
                case ITALIC:
                    textAttribute.setTypeface(Typeface.create(fontFamily, Typeface.ITALIC));
                    break;
                case BOLDITALIC:
                    textAttribute.setTypeface(Typeface.create(fontFamily, Typeface.BOLD_ITALIC));
                    break;
                case REGULAR:
                default:
                    textAttribute.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
                    break;
            }

            if (!isSelected) {
                if (null != labelStyle.getOutlineColor()) {
                    textAttribute.setEnableOutline(true);
                    textAttribute.setOutlineWidth(TEXT_OUTLINE_WIDTH);
                } else {
                    textAttribute.setEnableOutline(false);
                }
            }
        } else {
            textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
            textAttribute.setTypeface(Typeface.create("Ariel", Typeface.NORMAL));
        }

        textAttribute.setTextOffset(textOffset);
        fontPixelSize = FontUtilities.getTextPixelSize(labelStyle, getMapInstance().getFontSizeModifier());
        textAttribute.setTextSize(fontPixelSize);

        wwLabel = new gov.nasa.worldwind.shape.Label(textPosition, feature.getText(), textAttribute);

        Conversion.convertAndSetIGeoAltitudeMode(wwLabel, feature.getAltitudeMode());
        wwLabel.setRotationMode(WorldWind.RELATIVE_TO_GLOBE);
        wwLabel.setRotation(feature.getRotationAngle());
        wwLabel.setPickDelegate(feature);

        return wwLabel;
    }

    /**
     * This method must be override by the subclass to create a buffer renderable for the feature.
     * @param buffer The buffer distance in meters.
     * @return This method must return a WW Renderable object or null.
     */
    protected Renderable generateBuffer(double buffer) {
        return null;
    }

    public gov.nasa.worldwind.shape.Polygon createWWSurfaceImageSelect(IImageLayer imageLayer) {
        gov.nasa.worldwind.geom.Position pos;
        IGeoStrokeStyle selectedStrokeStyle = getMapInstance().getEmpResources().getSelectedStrokeStyle(getMapInstance());;
        IGeoBounds bBox = imageLayer.getBoundingBox();
        List<Position> wwPositionList = new ArrayList<>();
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        gov.nasa.worldwind.shape.Polygon wwPolygon = new gov.nasa.worldwind.shape.Polygon(wwPositionList, shapeAttribute);

        pos = new gov.nasa.worldwind.geom.Position(bBox.getNorth(), bBox.getWest(), 0);
        wwPositionList.add(pos);

        pos = new gov.nasa.worldwind.geom.Position(bBox.getNorth(), bBox.getEast(), 0);
        wwPositionList.add(pos);

        pos = new gov.nasa.worldwind.geom.Position(bBox.getSouth(), bBox.getEast(), 0);
        wwPositionList.add(pos);

        pos = new gov.nasa.worldwind.geom.Position(bBox.getSouth(), bBox.getWest(), 0);
        wwPositionList.add(pos);

        shapeAttribute.setDrawVerticals(false);

        shapeAttribute.setDrawInterior(false);
        shapeAttribute.setDrawOutline(true);
        Conversion.covertColor(selectedStrokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
        shapeAttribute.setOutlineWidth((float) selectedStrokeStyle.getStrokeWidth());

        wwPolygon.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        return wwPolygon;
    }

    public SurfaceImage createWWSurfaceImage(IImageLayer imageLayer) {
        IGeoBounds bBox = imageLayer.getBoundingBox();
        double deltaLatitude = bBox.getNorth() - bBox.getSouth();
        double deltaLongitude = bBox.getEast() - bBox.getWest();
        Sector sector = Sector.fromDegrees(bBox.getSouth(), bBox.getWest(), deltaLatitude, deltaLongitude);
        ImageSource imageSource = ImageSource.fromUrl(imageLayer.getURL().toString());
        SurfaceImage surfaceImage = new SurfaceImage(sector, imageSource);

        surfaceImage.setPickDelegate(imageLayer);
        return surfaceImage;
    }

    private void setWWPathAttributes(mil.emp3.api.Path feature, gov.nasa.worldwind.shape.Path wwPath, IGeoStrokeStyle strokeStyle) {
        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = wwPath.getPositions();
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = wwPath.getAttributes();

        if (shapeAttribute == null) {
            shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        }

        if (wwPositionList == null) {
            wwPositionList = new java.util.ArrayList<>();
        }

        try {
            Conversion.convertToWWPositionList(feature.getPositions(), wwPositionList);
        } catch (EMP_Exception ex) {
            Log.e(TAG, "CoordinateConvertion.convertToWWPositionList fail.", ex);
            return;
        }

        if (feature.getExtrude()) {
            shapeAttribute.setDrawVerticals(true);
        } else {
            shapeAttribute.setDrawVerticals(false);
        }
        shapeAttribute.setDrawInterior(false);

        if ((strokeStyle == null) && (feature.getStrokeStyle() != null)) {
            strokeStyle = feature.getStrokeStyle();
        }

        if (strokeStyle != null) {
            shapeAttribute.setDrawOutline(true);
            Conversion.covertColor(strokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
            shapeAttribute.setOutlineWidth((float) strokeStyle.getStrokeWidth());

            if (strokeStyle.getStipplingPattern() != 0) {
                ImageSource imageSource = ImageSource.fromLineStipple((int) ((double) strokeStyle.getStipplingFactor() * STIPPLE_FACTOR_DISPLAY_DENSITY_MODIFIER),
                        strokeStyle.getStipplingPattern());
                shapeAttribute.setOutlineImageSource(imageSource);
            }
        } else {
            shapeAttribute.setDrawOutline(false);
        }

        Conversion.convertAndSetIGeoAltitudeMode(wwPath, feature.getAltitudeMode());

        if (feature.getAltitudeMode() == IGeoAltitudeMode.AltitudeMode.ABSOLUTE){
            wwPath.setFollowTerrain(false);
        } else {
            wwPath.setFollowTerrain(true);
        }
        wwPath.setPickDelegate(feature);
        wwPath.setPositions(wwPositionList);
        wwPath.setAttributes(shapeAttribute);
        wwPath.setHighlighted(false);
        switch (feature.getPathType()) {
            case GREAT_CIRCLE:
                wwPath.setPathType(WorldWind.GREAT_CIRCLE);
                break;
            case LINEAR:
                wwPath.setPathType(WorldWind.LINEAR);
                break;
            case RHUMB_LINE:
                wwPath.setPathType(WorldWind.RHUMB_LINE);
                break;
        }

        if (feature.getExtrude()) {
            wwPath.setExtrude(true);
        }
    }

    private void setWWPolygonAttributes(mil.emp3.api.Polygon feature, gov.nasa.worldwind.shape.Polygon wwPolygon, IGeoStrokeStyle strokeStyle) {
        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = wwPolygon.getBoundary(0);
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = wwPolygon.getAttributes();

        if (shapeAttribute == null) {
            shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        }

        if (wwPositionList == null) {
            wwPositionList = new java.util.ArrayList<>();
        }

        try {
            Conversion.convertToWWPositionList(feature.getPositions(), wwPositionList);
        } catch (EMP_Exception ex) {
            Log.e(TAG, "CoordinateConvertion.convertToWWPositionList fail.", ex);
            return;
        }

        if (feature.getExtrude()) {
            shapeAttribute.setDrawVerticals(true);
        } else {
            shapeAttribute.setDrawVerticals(false);
        }

        if ((strokeStyle == null) && (feature.getStrokeStyle() != null)) {
            strokeStyle = feature.getStrokeStyle();
        }

        if (strokeStyle != null) {
            shapeAttribute.setDrawOutline(true);
            Conversion.covertColor(strokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
            shapeAttribute.setOutlineWidth((float) strokeStyle.getStrokeWidth());

            if (strokeStyle.getStipplingPattern() != 0) {
                ImageSource imageSource = ImageSource.fromLineStipple((int) ((double) strokeStyle.getStipplingFactor() * STIPPLE_FACTOR_DISPLAY_DENSITY_MODIFIER),
                        strokeStyle.getStipplingPattern());
                shapeAttribute.setOutlineImageSource(imageSource);
            }
        } else {
            shapeAttribute.setDrawOutline(false);
        }

        if (feature.getFillStyle() != null) {
            IGeoFillStyle fillStyle = feature.getFillStyle();

            shapeAttribute.setDrawInterior(true);

            switch (fillStyle.getFillPattern()) {
                case crossHatched:
                    shapeAttribute.setInteriorImageSource(mapInstance.getCrossHatchImage());
                    break;
                case hatched:
                    shapeAttribute.setInteriorImageSource(mapInstance.getHatchImage());
                    break;
                case solid:
                    // nothing additional to fill
                    break;
                default:
            }
            Conversion.covertColor(fillStyle.getFillColor(), shapeAttribute.getInteriorColor());
        } else {
            shapeAttribute.setDrawInterior(false);
        }

        Conversion.convertAndSetIGeoAltitudeMode(wwPolygon, feature.getAltitudeMode());

        if (feature.getAltitudeMode() == IGeoAltitudeMode.AltitudeMode.ABSOLUTE){
            wwPolygon.setFollowTerrain(false);
        } else {
            wwPolygon.setFollowTerrain(true);
        }
        wwPolygon.setPickDelegate(feature);
        wwPolygon.setBoundary(0, wwPositionList);
        wwPolygon.setAttributes(shapeAttribute);
        wwPolygon.setHighlighted(false);

        if (feature.getExtrude()) {
            wwPolygon.setExtrude(true);
        }

        switch (feature.getPathType()) {
            case GREAT_CIRCLE:
                wwPolygon.setPathType(WorldWind.GREAT_CIRCLE);
                break;
            case LINEAR:
                wwPolygon.setPathType(WorldWind.LINEAR);
                break;
            case RHUMB_LINE:
                wwPolygon.setPathType(WorldWind.RHUMB_LINE);
                break;
        }
    }
}
