package mil.emp3.test.emp3vv.dialogs.utils;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.test.emp3vv.R;

public class PositionItemAdapter extends BaseAdapter implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {
    private final static String TAG = PositionItemAdapter.class.getSimpleName();
    private final List<EmpGeoPosition> positionList;
    View selectedRow = null;
    TagHolder tagHoldeWithFocus;

    public PositionItemAdapter(List<EmpGeoPosition> positionList) {
        if(null == positionList) {
            throw new IllegalArgumentException("positionList must be non-null");
        }
        this.positionList = positionList;
    }
    @Override
    public int getCount() {
        return positionList.size();
    }

    @Override
    public Object getItem(int position) {
        return positionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        EditText latitude;
        EditText longitude;
        EditText altitude;
        ImageView info;
    }

    private static class TagHolder {
        final String property;
        final int position;
        EditText et;

        TagHolder(String property, int position) {
            this.property = property;
            this.position = position;
        }
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        try {
            EmpGeoPosition positionItem = (EmpGeoPosition) getItem(position);

            ViewHolder viewHolder;
            if(null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.position_item, parent, false);

                viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);
                viewHolder.latitude = (EditText) convertView.findViewById(R.id.latitude);
                viewHolder.longitude = (EditText) convertView.findViewById(R.id.longitude);
                viewHolder.altitude = (EditText) convertView.findViewById(R.id.altitude);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.info.setOnClickListener(this);
            viewHolder.info.setTag(position);

            viewHolder.latitude.addTextChangedListener(this);
            viewHolder.latitude.setOnFocusChangeListener(this);
            viewHolder.latitude.setTag(new TagHolder("latitude", position));

            viewHolder.longitude.addTextChangedListener(this);
            viewHolder.longitude.setOnFocusChangeListener(this);
            viewHolder.longitude.setTag(new TagHolder("longitude", position));

            viewHolder.altitude.addTextChangedListener(this);
            viewHolder.altitude.setOnFocusChangeListener(this);
            viewHolder.altitude.setTag(new TagHolder("altitude", position));

            viewHolder.latitude.setText(String.valueOf(positionItem.getLatitude()));
            viewHolder.longitude.setText(String.valueOf(positionItem.getLongitude()));
            viewHolder.altitude.setText(String.valueOf(positionItem.getAltitude()));

            return convertView;
        } catch (Exception e) {
            Log.e(TAG, "getView ", e);
            return null;
        }
    }

    public void addEmptyPosition(int atIndex) {
        if(atIndex < 0 || atIndex >= positionList.size()) {
            positionList.add(new EmpGeoPosition());
        } else {
            positionList.add(atIndex, new EmpGeoPosition());
        }
    }

    public boolean deleteSelectedPosition() {
        if(null != selectedRow) {
            int position = (Integer) selectedRow.getTag();
            positionList.remove(position);
            selectedRow.setBackgroundColor(Color.WHITE);
            selectedRow = null;
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(null != selectedRow) {
            selectedRow.setBackgroundColor(Color.WHITE);
        }
        selectedRow = v;
        Log.d(TAG, "onClick selected Row " + selectedRow.getTag());
        v.setBackgroundColor(Color.RED);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        try {
            Log.d(TAG, "onFocusChange position " + v.getTag(0) + " property " + v.getTag(1) + " hasFocus " + hasFocus);
            tagHoldeWithFocus = (TagHolder) v.getTag();
            tagHoldeWithFocus.et = (EditText) v;
//            int position = th.position;
//            EditText et = (EditText) v;
//            if (th.property.equals("latitude")) {
//                positionList.get(position).setLatitude(Double.parseDouble(et.getText().toString()));
//            } else if (th.property.equals("longitude")) {
//                positionList.get(position).setLongitude(Double.parseDouble(et.getText().toString()));
//            } else if (th.property.equals("altitude")) {
//                positionList.get(position).setAltitude(Double.parseDouble(et.getText().toString()));
//            }
        } catch (Exception e) {
            Log.e(TAG, "saveUserInput ", e);
        }
    }

//    public void saveUserInput(ListView listView) {
//        for(int ii = 0; ii < listView.getCount(); ii++) {
//            ViewHolder holderView = (ViewHolder) listView.getChildAt(ii).getTag();
//            try {
//                positionList.get(ii).setLatitude(Double.parseDouble(holderView.latitude.getText().toString()));
//                positionList.get(ii).setLongitude(Double.parseDouble(holderView.longitude.getText().toString()));
//                positionList.get(ii).setAltitude(Double.parseDouble(holderView.altitude.getText().toString()));
//            } catch (Exception e) {
//                Log.e(TAG, "saveUserInput ", e);
//            }
//        }
//    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            int position = tagHoldeWithFocus.position;
            EditText et = tagHoldeWithFocus.et;
            if (tagHoldeWithFocus.property.equals("latitude")) {
                positionList.get(position).setLatitude(Double.parseDouble(et.getText().toString()));
            } else if (tagHoldeWithFocus.property.equals("longitude")) {
                positionList.get(position).setLongitude(Double.parseDouble(et.getText().toString()));
            } else if (tagHoldeWithFocus.property.equals("altitude")) {
                positionList.get(position).setAltitude(Double.parseDouble(et.getText().toString()));
            }
        } catch (Exception e) {
            // Log.e(TAG, "afterTextChanged ", e);
        }
    }
}
