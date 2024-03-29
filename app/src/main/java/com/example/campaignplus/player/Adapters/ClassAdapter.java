package com.example.campaignplus.player.Adapters;

import static com.example.campaignplus._data.DataCache.availableClasses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Handler;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.classinfo.MainClassInfo;
import com.example.campaignplus._data.classinfo.SubClassInfo;
import com.example.campaignplus.player.MainFragments.ClassInformationFragment;

import java.util.ArrayList;
import java.util.HashMap;


public class ClassAdapter extends ArrayAdapter {

    private final ArrayList<Integer> classes;
    private final SubclassSelectedCallback callback;

    private final int resource;
    private final Context context;


    public interface SubclassSelectedCallback {
        void onSelected(int mainClassId, int subclassId);

        void onInfoButtonPressed(int mainClassId, int subclassId);
    }


    public ClassAdapter(Context context, @NonNull ArrayList<Integer> classes, SubclassSelectedCallback callback) {
        super(context, R.layout.row_class, R.id.class_name, classes);

        this.context = context;
        this.resource = R.layout.row_class;
        this.classes = classes;
        this.callback = callback;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View rowView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (rowView == null)
            rowView = inflater.inflate(resource, null, true);

        TextView name = rowView.findViewById(R.id.class_name);
        Spinner subclasses = rowView.findViewById(R.id.class_subclass);

        // Set name for the selected class
        MainClassInfo mainClass = availableClasses.get(classes.get(position));
        assert mainClass != null;
        name.setText(mainClass.name);

        name.setOnLongClickListener(view -> {
            classes.remove(position);
            notifyDataSetChanged();
            return false;
        });

        // Get subclasses for selected class, create a list of strings to show in the spinner
        ArrayList<String> subclassList = new ArrayList<>();
        ArrayList<SubClassInfo> currentSubclasses = mainClass.getSubclasses();
        for (SubClassInfo info : currentSubclasses) {
            subclassList.add(info.name);
        }
        subclasses.setPrompt("Select subclass");


        // Create subclass adapter, filter this later.
        ArrayAdapter<String> subclassAdapter = new ArrayAdapter<>(this.context, android.R.layout.simple_spinner_item, subclassList);
        subclassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        subclasses.setAdapter(subclassAdapter);
        subclassAdapter.notifyDataSetChanged();


        // Get the currently selected subclass for this player, if any is selected.
        SubClassInfo subclass = DataCache.selectedPlayer.getSubclassForMainclass(mainClass);
        if (subclass != null)
            subclasses.setSelection(subclassList.indexOf(subclass.name));

        // Add default subclass to selection based on current state (previous state or default)
        String currentSubclass = (String) subclasses.getSelectedItem();
        for (SubClassInfo sci : currentSubclasses) {
            if (currentSubclass.equals(sci.name)) {
                callback.onSelected(mainClass.getId(), sci.getId());
                break;
            }
        }

        // Update the subclass tracker list if the spinner selects an item.
        subclasses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                callback.onSelected(mainClass.getId(), currentSubclasses.get(i).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ImageButton infoButton = rowView.findViewById(R.id.subclass_info_button);
        infoButton.setOnClickListener(view -> {
            Log.d("____________________________________", "got " +  subclasses.getSelectedItem() + " and " +  subclasses.getSelectedItemId());
            callback.onInfoButtonPressed(mainClass.getId(), currentSubclasses.get((int) subclasses.getSelectedItemId()).getId());
        });

        return rowView;
    }
}
