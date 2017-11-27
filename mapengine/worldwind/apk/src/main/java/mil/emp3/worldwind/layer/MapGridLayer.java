package mil.emp3.worldwind.layer;

import android.util.Log;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.mapengine.interfaces.IMapGridLines;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.EMPtoWWFeatureConverter;

/**
 * This class implements the map grid layer.
 */

public class MapGridLayer extends RenderableLayer {
    final static private String TAG = MapGridLayer.class.getSimpleName();

    final private MapInstance mapInstance;
    private final EMPtoWWFeatureConverter featureConverter;
    private IMapGridLines gridGenerator = null;
    private long lastUpdateTime;

    public MapGridLayer(String displayName, MapInstance mapInstance) {
        super(displayName);

        this.mapInstance = mapInstance;
        this.featureConverter = new EMPtoWWFeatureConverter(mapInstance);
    }

    public void setMapGridGenerator(IMapGridLines generator) {
        this.gridGenerator = generator;
        this.lastUpdateTime = 0;
    }

    private void doPreRender(RenderContext rc) {
        if (this.lastUpdateTime < this.gridGenerator.getLastUpdated().getTime()) {
            Renderable renderable;

            this.clearRenderables();
            long startTS = System.currentTimeMillis();

            // The grid has been updated sense our last rendering.
            List<IFeature> featureList = this.gridGenerator.getGridFeatures();

            if (!featureList.isEmpty()) {
                for (IFeature feature : featureList) {
                    renderable = null;
                    switch (feature.getFeatureType()) {
                        case GEO_PATH: {
                            gov.nasa.worldwind.shape.Path path = this.featureConverter.createWWPath((Path) feature, false);
                            IGeoPosition pos1 = feature.getPositions().get(0);
                            IGeoPosition pos2 = feature.getPositions().get(1);
                            if ((Math.abs(pos1.getLatitude() - pos2.getLatitude()) > 24.0) ||
                                    (Math.abs(pos1.getLongitude() - pos2.getLongitude()) > 24.0)) {
                                // Optimize Grid rendering
                                path.setFollowTerrain(false);
                            }
                            renderable = path;
                            break;
                        }
                        case GEO_TEXT:
                            renderable = this.featureConverter.createWWLabel((Text) feature, false);
                            break;
                        default:
                            Log.d(TAG, "Invalid feature type for pre-rendering " + feature.getFeatureType().toString());
                            break;
                    }

                    if (null != renderable) {
                        this.addRenderable(renderable);
                    }
                }
            }
            this.lastUpdateTime = this.gridGenerator.getLastUpdated().getTime();
            Log.i(TAG, "feature conversion in " + (System.currentTimeMillis() - startTS) + " ms.");
        }
    }

    @Override
    protected void doRender(RenderContext rc) {
        if (null == this.gridGenerator) {
            // There is no grid generator.
            this.clearRenderables();
            return;
        }

        this.doPreRender(rc);
        long startTS = System.currentTimeMillis();
        super.doRender(rc);
        Log.i(TAG, "feature rendering in " + (System.currentTimeMillis() - startTS) + " ms.");
    }

    public boolean needsRendering() {
        return ((null != this.gridGenerator)? (this.lastUpdateTime < this.gridGenerator.getLastUpdated().getTime()): false);
    }
}
