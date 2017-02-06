package mil.emp3.worldwind.utils;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.draw.DrawableSurfaceTexture;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.shape.SurfaceImage;
import gov.nasa.worldwind.util.Pool;

/**
 * This class sub classes the NASA WW SurfaceImage to allow us to force as Texture retrieveal
 */

public class EmpSurfaceImage extends SurfaceImage {
    private boolean forceUpdateTexture = false;


    public EmpSurfaceImage() {
        super();
    }

    public EmpSurfaceImage(Sector sector, ImageSource imageSource) {
        super(sector, imageSource);
    }

    public void imageSourceUpdated() {
        forceUpdateTexture = true;
    }

    protected void doRender(RenderContext rc) {
        if(!this.sector.isEmpty()) {
            if(rc.terrain.getSector().intersects(this.sector)) {
                Texture texture = null;

                if (!forceUpdateTexture) {
                    texture = rc.getTexture(this.imageSource);
                }

                if(texture == null) {
                    texture = rc.retrieveTexture(this.imageSource, this.imageOptions);
                    forceUpdateTexture = false;
                }

                if(texture != null) {
                    SurfaceTextureProgram program = this.getShaderProgram(rc);
                    Pool pool = rc.getDrawablePool(DrawableSurfaceTexture.class);
                    DrawableSurfaceTexture drawable = DrawableSurfaceTexture.obtain(pool).set(program, this.sector, texture, texture.getTexCoordTransform());
                    rc.offerSurfaceDrawable(drawable, 0.0D);
                    if(rc.pickMode) {
                        int pickedObjectId = rc.nextPickedObjectId();
                        PickedObject.identifierToUniqueColor(pickedObjectId, drawable.color);
                        rc.offerPickedObject(PickedObject.fromRenderable(pickedObjectId, this, rc.currentLayer));
                    }

                }
            }
        }
    }
}
