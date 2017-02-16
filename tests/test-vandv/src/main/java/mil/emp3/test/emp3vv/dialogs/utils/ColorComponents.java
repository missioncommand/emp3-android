package mil.emp3.test.emp3vv.dialogs.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.cmapi.primitives.IGeoColor;

import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.test.emp3vv.R;

public class ColorComponents {
    private static String TAG = ColorComponents.class.getSimpleName();

    final int defaultColorValue = 128;
    final int minColorValue = 0;
    final int maxColorValue = 255;
    final int colorResolution = 1;
    final int fastMultiplier = 10;

    final double minAlphaValue = 0.0;
    final double maxAlphaValue = 1.0;
    final double alphaResolution = 0.1;
    final double alphaFastMultiplier = 1.0;
    final double defaultAlphaValue = maxAlphaValue;

    IncrementDecrement<Integer> redIncDec;
    IncrementDecrement<Integer> greenIncDec;
    IncrementDecrement<Integer> blueIncDec;
    IncrementDecrement<Double> alphaIncDec;

    public void initColors(IGeoColor color) {
        if (null != color) {
            redIncDec = new IncrementDecrement<>(color.getRed(), minColorValue, maxColorValue, colorResolution, fastMultiplier);
            greenIncDec = new IncrementDecrement<>(color.getGreen(), minColorValue, maxColorValue, colorResolution, fastMultiplier);
            blueIncDec = new IncrementDecrement<>(color.getBlue(), minColorValue, maxColorValue, colorResolution, fastMultiplier);
            alphaIncDec = new IncrementDecrement<>(color.getAlpha(), minAlphaValue, maxAlphaValue, alphaResolution, alphaFastMultiplier);
        }
    }

    public IGeoColor getColor() {
        return new EmpGeoColor(alphaIncDec.getValue(), (int) redIncDec.getValue(), (int) greenIncDec.getValue(), (int) blueIncDec.getValue());
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(null == redIncDec) {
            redIncDec = new IncrementDecrement<>(defaultColorValue, minColorValue, maxColorValue, colorResolution, fastMultiplier);
        }
        redIncDec.onViewCreated(view.findViewById(R.id.color_components_red), savedInstanceState, true);
        redIncDec.setValueColor(view.getResources().getColor(R.color.red));

        if(null == greenIncDec) {
            greenIncDec = new IncrementDecrement<>(defaultColorValue, minColorValue, maxColorValue, colorResolution, fastMultiplier);
        }
        greenIncDec.onViewCreated(view.findViewById(R.id.color_components_green), savedInstanceState, true);
        greenIncDec.setValueColor(view.getResources().getColor(R.color.green));

        if(null == blueIncDec) {
            blueIncDec = new IncrementDecrement<>(defaultColorValue, minColorValue, maxColorValue, colorResolution, fastMultiplier);
        }
        blueIncDec.onViewCreated(view.findViewById(R.id.color_components_blue), savedInstanceState, true);
        blueIncDec.setValueColor(view.getResources().getColor(R.color.blue));
        if(null == alphaIncDec) {
            alphaIncDec = new IncrementDecrement<>(defaultAlphaValue, minAlphaValue, maxAlphaValue, alphaResolution, alphaFastMultiplier);
        }
        alphaIncDec.onViewCreated(view.findViewById(R.id.color_components_alpha), savedInstanceState, true);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState, IGeoColor color) {
        initColors(color);
        onViewCreated(view, savedInstanceState);
    }
}
