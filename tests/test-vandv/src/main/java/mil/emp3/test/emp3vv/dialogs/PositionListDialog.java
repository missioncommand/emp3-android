package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.dialogs.utils.PositionItemAdapter;

import static android.view.View.GONE;

/**
 * Used by PositionListUtility class to show a dialog where user can type in the positions.
 */
public class PositionListDialog extends Emp3TesterDialogBase {

    private static String TAG = PositionListDialog.class.getSimpleName();

    PositionItemAdapter positionItemAdapter;
    int minRequiredPositions;

    public interface IPositionListDialogListener extends Emp3TesterDialogBase.IEmp3TesterDialogBaseListener {
        void positionsSet(PositionListDialog positionListDialog);
    }

    public PositionListDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static PositionListDialog newInstance(String title, IPositionListDialogListener listener, IMap map, List<EmpGeoPosition> positionList, int minRequiredPositions) {

        PositionListDialog frag = new PositionListDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.setPositionList(positionList, minRequiredPositions);
        return frag;
    }

    public static PositionListDialog newInstanceForOptItem(String title, IPositionListDialogListener listener, IMap map, List<EmpGeoPosition> positionList, int minRequiredPositions) {

        PositionListDialog frag = new PositionListDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.setPositionList(positionList, minRequiredPositions);
        return frag;
    }

    private void setPositionList(List<EmpGeoPosition> positionList, int minRequiredPositions) {
        positionItemAdapter = new PositionItemAdapter(positionList);
        this.minRequiredPositions = minRequiredPositions;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.position_list_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = (ListView) view.findViewById(R.id.userPositions);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(positionItemAdapter);

        Button addPositionItem = (Button) view.findViewById(R.id.add_position);
        addPositionItem.setOnClickListener(v -> {
            // positionItemAdapter.saveUserInput(listView);
            positionItemAdapter.addEmptyPosition(-1);
            positionItemAdapter.notifyDataSetChanged();
        });

        Button deletePositionItem = (Button) view.findViewById(R.id.delete_position);
        deletePositionItem.setOnClickListener(v -> {
            // positionItemAdapter.saveUserInput(listView);
            if(positionItemAdapter.deleteSelectedPosition()) {
                positionItemAdapter.notifyDataSetChanged();
            }
        });

        Button movePositionItem = (Button) view.findViewById(R.id.move_position);
        movePositionItem.setOnClickListener(v -> ErrorDialog.showError(getContext(), "Move, not yet supported"));

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> {
            if(positionItemAdapter.getCount() >= minRequiredPositions) {
                if (null != PositionListDialog.this.listener) {
                    ((IPositionListDialogListener) PositionListDialog.this.listener).positionsSet(PositionListDialog.this);
                    PositionListDialog.this.dismiss();
                }
            } else {
                ErrorDialog.showError(getContext(), "need minimum " + minRequiredPositions + " positions");
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if(positionItemAdapter.getCount() >= minRequiredPositions) {
                if (null != PositionListDialog.this.listener) {
                    ((IPositionListDialogListener) PositionListDialog.this.listener).positionsSet(PositionListDialog.this);
                }
            } else {
                ErrorDialog.showError(getContext(), "need minimum " + minRequiredPositions + " positions");
            }
        });
    }
}

