package mil.emp3.core.mapgridlines;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.emp3.api.Camera;
import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
import mil.emp3.mapengine.interfaces.ICoreMapGridLineGenerator;
import mil.emp3.mapengine.interfaces.IMapGridLines;

/**
 * This abstract class is the base class for all map grid line classes.
 */

public abstract class AbstractMapGridLine implements IMapGridLines, ICoreMapGridLineGenerator {
    private static final String TAG = AbstractMapGridLine.class.getSimpleName();

    private Date lastUpdated;
    private final List<IFeature> featureList = new ArrayList<>();
    protected final ICamera previousCamera;
    private final EmpBoundingBox boundingBox;

    protected AbstractMapGridLine() {
        this.lastUpdated = new Date();
        this.previousCamera = new Camera();
        this.boundingBox = new EmpBoundingBox();
    }

    @Override
    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    @Override
    public List<IFeature> getGridFeatures() {
        return this.featureList;
    }

    protected void clearFeatureList() {
        boolean resetTime = (this.featureList.size() > 0);
        this.featureList.clear();

        if (resetTime) {
            this.lastUpdated.setTime(System.currentTimeMillis());
        }
    }

    protected void addFeature(IFeature feature) {
        if (null == feature) {
            throw new InvalidParameterException("feature is null.");
        }
        this.featureList.add(feature);
        this.lastUpdated.setTime(System.currentTimeMillis());
    }

    @Override
    public void mapViewChange(IGeoBounds mapBounds, ICamera camera, int viewWidth, int viewHeight) {
        if ((null == mapBounds) || (null == camera)) {
            clearFeatureList();
            return;
        }

        this.boundingBox.copyFrom(mapBounds);

        this.processViewChange(this.boundingBox, camera, viewWidth, viewHeight);
        this.previousCamera.copySettingsFrom(camera);
    }

    /**
     * This method will not get called if the bounds are null.
     * @param mapBounds
     * @param camera
     * @param viewWidth
     * @param viewHeight
     */
    protected abstract void processViewChange(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight);

    protected abstract void setPathAttributes(Path path, String gridObjectType);

    protected abstract void setLabelAttributes(Text label, String gridObjectType);

    protected IFeature createPathFeature(List<IGeoPosition> positionList, String gridObjectType) {
        Path path = new Path(positionList);

        setPathAttributes(path, gridObjectType);

        return path;
    }

    protected IFeature createLabelFeature(IGeoPosition position, String text, String gridObjectType) {
        Text label = new Text(text);

        label.setPosition(position);
        setLabelAttributes(label, gridObjectType);
        return label;
    }
}
