package mil.emp3.api.shadows;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.ShadowMap;

import armyc2.c2sd.renderer.MilStdIconRenderer;

/**
 * @author Matt Miller & Jenifer Cochran
 *
 * Referenced: https://stackoverflow.com/questions/29629786/robolectric-shadow-not-working
 */

public class ShadowTestRunner extends RobolectricTestRunner
{

    public ShadowTestRunner(Class testClass) throws InitializationError
    {
        super(testClass);
    }

    @Override
    protected ShadowMap createShadowMap()
    {
        //add all shadow classes with static methods
        return new ShadowMap.Builder()
                            .addShadowClass(MilStdIconRenderer.class,     //class with static methods to override
                                            ShadowKMLExportThread.class,  //the shadow class with the implemented methods
                                            true,
                                            true,
                                            true)
                            .build();
    }
}
