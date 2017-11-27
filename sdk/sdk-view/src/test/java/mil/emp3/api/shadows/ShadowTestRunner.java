package mil.emp3.api.shadows;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.ShadowMap;

import mil.emp3.api.utils.MilStdUtilities;

/**
 * Created by matt.miller@rgi-corp.local on 11/27/17.
 */

public class ShadowTestRunner extends RobolectricTestRunner {

    public ShadowTestRunner(Class testClass) throws InitializationError {
        super(testClass);
    }

//    public InstrumentationConfiguration createClassLoaderConfig() {
//        InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
//        builder.addInstrumentedClass(KMLExportThread.class.getName());
//        return builder.build();
//    }

    @Override
    protected ShadowMap createShadowMap() {
        return new ShadowMap.Builder().addShadowClass(MilStdUtilities.class, ShadowKMLExportThread.class, true, true, true).build();
    }

    public InstrumentationConfiguration createClassLoaderConfig() {
        InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
        builder.addInstrumentedClass(MilStdUtilities.class.getName());
        return builder.build();
    }
}
