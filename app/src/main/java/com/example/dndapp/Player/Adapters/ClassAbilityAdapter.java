package com.example.dndapp.Player.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.classinfo.ClassAbility;

import java.util.ArrayList;


public class ClassAbilityAdapter extends ArrayAdapter {

    private final ArrayList<ClassAbility> abilities;
    private final int resource;
    private final Activity context;

    public ClassAbilityAdapter(@NonNull Activity context, @NonNull ArrayList<ClassAbility> abilities) {
        super(context, R.layout.row_class_ability, abilities);

        this.context = context;
        this.resource = R.layout.row_class_ability;
        this.abilities = abilities;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View rowView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        rowView = inflater.inflate(resource, null,true);

        TextView className = rowView.findViewById(R.id.class_name);
        TextView subclassName  = rowView.findViewById(R.id.subclass_name);
        TextView abilityName = rowView.findViewById(R.id.class_ability_name);
        TextView abilityInfo = rowView.findViewById(R.id.class_ability);

        ClassAbility data = (ClassAbility) getItem(position);
        assert data != null;

        if (data.getMainClass() != null) {
            className.setText(data.getMainClass().getName());
            subclassName.setText(""); // A main class has no subclass text.
        } else {
            className.setText(data.getSubClass().getName());
            subclassName.setText(data.getSubClass().getMainClassName());
        }
        abilityName.setText(data.getName());
        abilityInfo.setText(data.getInfo());

        return rowView;
    }
}
