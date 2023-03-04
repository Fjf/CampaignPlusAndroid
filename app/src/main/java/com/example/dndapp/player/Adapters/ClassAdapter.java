package com.example.dndapp.player.Adapters;

import static com.example.dndapp._data.DataCache.availableClasses;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.classinfo.MainClassInfo;

import java.util.ArrayList;


public class ClassAdapter extends ArrayAdapter {

    private final ArrayList<Integer> classes;
    private final int resource;
    private final Activity context;

    public ClassAdapter(@NonNull Activity context, @NonNull ArrayList<Integer> classes) {
        super(context, R.layout.row_class, R.id.class_name, classes);

        this.context = context;
        this.resource = R.layout.row_class;
        this.classes = classes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View rowView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (rowView == null)
            rowView = inflater.inflate(resource, null, true);

        TextView name = rowView.findViewById(R.id.class_name);

        MainClassInfo data = availableClasses.get(classes.get(position));
        name.setText(data.getName());

        return rowView;
    }


}
