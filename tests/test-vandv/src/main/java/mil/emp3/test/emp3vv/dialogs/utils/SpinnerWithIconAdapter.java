package mil.emp3.test.emp3vv.dialogs.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import mil.emp3.test.emp3vv.R;

public class SpinnerWithIconAdapter extends ArrayAdapter<SpinnerWithIconItem> {
    private final LayoutInflater oInflater;
    private final Context oContext;
    private final java.util.List<? extends SpinnerWithIconItem> oItemList;

    /*************  CustomAdapter Constructor *****************/
    public SpinnerWithIconAdapter(
            LayoutInflater inflater,
            Context context,
            int textViewResourceId,
            java.util.List<? extends SpinnerWithIconItem> oList) {
        super(context, textViewResourceId, (java.util.List<SpinnerWithIconItem>) oList);

        /********** Take passed values **********/
        this.oInflater = inflater;
        this.oContext = context;
        this.oItemList  = oList;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // This funtion called for each row ( Called data.size() times )
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = this.oInflater.inflate(R.layout.spinner_with_icon_row, parent, false);

        /***** Get each Model object from Arraylist ********/
        SpinnerWithIconItem oItem = this.oItemList.get(position);

        TextView oItemText = (TextView) row.findViewById(R.id.item_text);
        ImageView oItemIcon = (ImageView) row.findViewById(R.id.optionalimage);

        // Set values for spinner each row
        oItemText.setText(oItem.getText());
        if (oItem.getImage() != -1) {
            oItemText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            oItemIcon.setImageResource(oItem.getImage());
        }

        return row;
    }
}
