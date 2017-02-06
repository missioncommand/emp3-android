package mil.emp3.core.editors;


import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.apk.core.aar.R;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * This class implements the editor control point used to mark feature movable points.
 */
public class ControlPoint extends mil.emp3.api.Point {
    public enum CPTypeEnum {
        NEW_POSITION_CP,
        POSITION_CP,
        ALTITUDE_CP,
        RADIUS_CP,
        INNER_RADIUS_CP,
        WIDTH_CP,
        LEFT_WIDTH_CP,
        RIGHT_WIDTH_CP,
        HEIGHT_CP,         // Added HEIGHT_CP to match height and width of rectangle feature. I could have used LENGTH_CP but
                           // that creates confusion.
        RANGE_CP,
        LENGTH_CP,
        AZIMUTH_CP,
        LEFT_AZIMUTH_CP,
        RIGHT_AZIMUTH_CP,
        ATTITUDE_CP,
        SEGMENT_CP,
        AOI_BUFFER
    }

    private CPTypeEnum eCPType;
    // The cpIndex associates the control point with feature positions.
    // POSITION_CP the index of the feature position.
    // NEW_POSITION_CP half way between the control point indicated by cpIndex and cpSubIndex
    private int cpIndex;
    // -1 for POSITION cp.
    private int cpSubIndex;

    private void init(CPTypeEnum eType, int index, int subIndex) {
        this.cpIndex = index;
        this.cpSubIndex = subIndex;
        this.setCPType(eType);
    }
    public ControlPoint(CPTypeEnum eType, int index, int subIndex) {
        super();
        init(eType, index, subIndex);
    }

    public ControlPoint(CPTypeEnum eType, int index, int subIndex, AltitudeMode altitudeMode) {
        super();
        init(eType, index, subIndex);
        this.setAltitudeMode(altitudeMode);
    }

    public void setCPType(CPTypeEnum eType) {
        this.eCPType = eType;

        switch (eType) {
            case NEW_POSITION_CP:
                this.setResourceId(R.drawable.cp_new);
                break;
            case POSITION_CP:
                this.setResourceId(R.drawable.cp_position);
                break;
            case ALTITUDE_CP:
                this.setResourceId(R.drawable.cp_altitude);
                break;
            case RADIUS_CP:
            case INNER_RADIUS_CP:
                this.setResourceId(R.drawable.cp_radius);
                break;
            case WIDTH_CP:
            case LEFT_WIDTH_CP:
            case RIGHT_WIDTH_CP:
            case RANGE_CP:
            case LENGTH_CP:
            case HEIGHT_CP:
            case AOI_BUFFER:
                this.setResourceId(R.drawable.cp_distance);
                break;
            case AZIMUTH_CP:
            case LEFT_AZIMUTH_CP:
            case RIGHT_AZIMUTH_CP:
            case ATTITUDE_CP:
                this.setResourceId(R.drawable.cp_angle);
                break;
            case SEGMENT_CP:
                break;
        }

        // The geo icon style does not support fraction. But we need to set the offsets to fractions.
        this.getIconStyle().setOffSetX(0.5);
        this.getIconStyle().setOffSetY(0.5);
    }

    public CPTypeEnum getCPType() {
        return this.eCPType;
    }

    public void setCPIndex(int value) {
        this.cpIndex = value;
    }

    public int getCPIndex() {
        return this.cpIndex;
    }

    public void setCPSubIndex(int value) {
        this.cpSubIndex = value;
    }

    public int getCPSubIndex() {
        return this.cpSubIndex;
    }

    /**
     * This method increases the indexes by 1 if they are equal or greater than startIndex
     * @param startIndex
     */
    public void increaseIndexes(int startIndex) {
        if (this.cpIndex >= startIndex) {
            this.cpIndex++;
        }
        if (this.cpSubIndex >= startIndex) {
            this.cpSubIndex++;
        }
    }

    /**
     * This method decreases the indexes by 1 if they are equal or greater than startIndex
     * @param startIndex
     */
    public void decreaseIndexes(int startIndex) {
        if (this.cpIndex >= startIndex) {
            this.cpIndex--;
        }
        if (this.cpSubIndex >= startIndex) {
            this.cpSubIndex--;
        }
    }

    /**
     * This method places the control point in the center of the line between pos1 and pos2
     * @param pos1
     * @param pos2
     */
    public void moveCPBetween(IGeoPosition pos1, IGeoPosition pos2) {
        double dDistance = GeoLibrary.computeDistanceBetween(pos1, pos2) / 2.0;
        double dBearing = GeoLibrary.computeBearing(pos1, pos2);
        if (this.getPositions().size() == 0) {
            this.setPosition(new GeoPosition());
        }
        GeoLibrary.computePositionAt(dBearing, dDistance, pos1, this.getPosition());
    }

    /**
     * This method moves the control point to location that is at the indicated beraing at the specified distance.
     * @param bearing
     * @param distance
     */
    public void moveControlPoint(double bearing, double distance) {
        if (this.getPositions().size() == 0) {
            // If there is not position it does nothing.
            return;
        }
        GeoLibrary.computePositionAt(bearing, distance, this.getPosition(), this.getPosition());
    }
}
