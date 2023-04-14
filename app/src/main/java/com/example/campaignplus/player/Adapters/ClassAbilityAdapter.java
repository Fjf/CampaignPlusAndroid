package com.example.campaignplus.player.Adapters;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.classinfo.ClassAbility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;


public class ClassAbilityAdapter extends ArrayAdapter implements SectionIndexer {

    private final ArrayList<ClassAbility> abilities;
    private final int resource;
    private final Activity context;
    HashMap<Integer, Integer> alphaIndexer;
    Integer[] sections;

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        createIndex();
    }

    private void createIndex() {
        /*
         * Create index for section scrolling
         * https://stackoverflow.com/questions/6883785/android-sectionindexer-tutorial
         */
        alphaIndexer.clear();
        for (int i = 0; i < abilities.size(); i++) {
            if (!alphaIndexer.containsKey(abilities.get(i).getLevel()))
                alphaIndexer.put(abilities.get(i).getLevel(), i);
        }
        Set<Integer> sectionLetters = alphaIndexer.keySet();
        ArrayList<Integer> sectionList = new ArrayList<>(sectionLetters);
        Collections.sort(sectionList);
        sections = new Integer[sectionList.size()];
        sectionList.toArray(sections);
    }

    public ClassAbilityAdapter(@NonNull Activity context, @NonNull ArrayList<ClassAbility> abilities) {
        super(context, R.layout.row_class_ability, abilities);

        this.context = context;
        this.resource = R.layout.row_class_ability;
        this.abilities = abilities;
        alphaIndexer = new HashMap<Integer, Integer>();
        createIndex();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View rowView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        rowView = inflater.inflate(resource, null, true);

        TextView className = rowView.findViewById(R.id.class_name);
        TextView subclassName = rowView.findViewById(R.id.subclass_name);
        TextView abilityLevel = rowView.findViewById(R.id.class_ability_level);
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
        abilityInfo.setText(data.getDescription());
        abilityLevel.setText("Level " + String.valueOf(data.getLevel()));

        if (Integer.parseInt(selectedPlayer.statsData.getLevel()) < data.getLevel()) {
            abilityInfo.setTextColor(ContextCompat.getColor(context, R.color.overlaySettingsLight));
            abilityLevel.setTextColor(ContextCompat.getColor(context, R.color.overlaySettingsLight));
            className.setTextColor(ContextCompat.getColor(context, R.color.overlaySettingsLight));
            subclassName.setTextColor(ContextCompat.getColor(context, R.color.overlaySettingsLight));
        }

        return rowView;
    }


    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int i) {
        return alphaIndexer.get(sections[i]);
    }

    @Override
    public int getSectionForPosition(int i) {
        return 0;
    }
}
