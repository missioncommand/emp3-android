package mil.emp3.test.emp3vv.dialogs.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;

import mil.emp3.test.emp3vv.R;

public class IncrementDecrement<T extends Number> {
    private static String TAG = IncrementDecrement.class.getSimpleName();

    private final T initialValue;
    private final T fastMultiplier;
    private final T resolution;
    private final T minValue;
    private final T maxValue;
    private TextView valueView;
    private double currentValue;

    private String pattern = "###.##";
    private IIncDecListener listener;

    public interface IIncDecListener {
        double onValueChanged(double newValue);
    }

    public IncrementDecrement(T initialValue, T minValue, T maxValue, T resolution, T fastMultiplier) {
        this.initialValue = initialValue;
        this.resolution = resolution;
        this.fastMultiplier = fastMultiplier;
        this.minValue = minValue;
        this.maxValue = maxValue;
        currentValue = initialValue.doubleValue();
    }

    public double getValue() {
        return currentValue;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState, boolean enableFast) {
        valueView = (TextView) view.findViewById(R.id.current_value);

        if (valueView != null) {
            updateDisplay(currentValue);
            ImageButton valueUpBtn = (ImageButton) view.findViewById(R.id.increment_value);
            if (valueUpBtn != null) {
                valueUpBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double newValue = (currentValue + resolution.doubleValue());
                        newValue = Math.round(newValue / resolution.doubleValue()) * resolution.doubleValue();
                        if(newValue <= maxValue.doubleValue()) {
                            updateDisplay(newValue);
                        }
                    }
                });
            }
            ImageButton valueDownBtn = (ImageButton) view.findViewById(R.id.decrement_value);
            if (valueDownBtn != null) {
                valueDownBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double newValue = (currentValue - resolution.doubleValue());
                        newValue = Math.round(newValue / resolution.doubleValue()) * resolution.doubleValue();
                        if(newValue >= minValue.doubleValue()) {
                            updateDisplay(newValue);
                        }
                    }
                });
            }

            ImageButton valueUpBtnFast = (ImageButton) view.findViewById(R.id.increment_value_fast);
            if (valueUpBtnFast != null) {
                if(!enableFast) {
                    valueUpBtnFast.setVisibility(View.GONE);
                } else {
                    valueUpBtnFast.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            double newValue = (currentValue + (resolution.doubleValue() * fastMultiplier.doubleValue()));
                            newValue = Math.round(newValue / resolution.doubleValue()) * resolution.doubleValue();
                            if (newValue <= maxValue.doubleValue()) {
                                updateDisplay(newValue);
                            }
                        }
                    });
                }
            }
            ImageButton valueDownBtnFast = (ImageButton) view.findViewById(R.id.decrement_value_fast);
            if (valueDownBtnFast != null) {
                if(!enableFast) {
                    valueDownBtnFast.setVisibility(View.GONE);
                } else {
                    valueDownBtnFast.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            double newValue = (currentValue - (resolution.doubleValue() * fastMultiplier.doubleValue()));
                            newValue = Math.round(newValue / resolution.doubleValue()) * resolution.doubleValue();
                            if (newValue >= minValue.doubleValue()) {
                                updateDisplay(newValue);
                            }
                        }
                    });
                }
            }
        }
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState, boolean enableFast, IIncDecListener listener) {
        this.listener = listener;
        onViewCreated(view, savedInstanceState, enableFast);
    }
    public void setValueColor(int color) {
        valueView.setTextColor(color);
    }

    public void setValueDecimalFormatPattern(String pattern) {
        this.pattern = pattern;
    }

    private void updateDisplay(double newValue) {
        if(null != listener) {
            currentValue = listener.onValueChanged(newValue);
        } else {
            currentValue = newValue;
        }
        
        if((initialValue instanceof Double) || (initialValue instanceof  Float)) {
            DecimalFormat myFormatter = new DecimalFormat(pattern);
            String output = myFormatter.format(currentValue);
            valueView.setText(String.valueOf(output));
        } else if((initialValue instanceof Integer) || (initialValue instanceof Short) || (initialValue instanceof Long) ||
                (initialValue instanceof  Byte)) {
            valueView.setText(String.valueOf((int) currentValue));
        } else {
            throw new InvalidParameterException("Update updateDisplay to handle your type " + initialValue.getClass().getSimpleName());
        }
    }
}
