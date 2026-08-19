package com.example.campaignplus.player.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.Counter;

import java.util.ArrayList;

public class CounterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_COUNTER = 0;
    private static final int TYPE_FOOTER = 1;
    
    private final Context context;
    private final ArrayList<Counter> counters;
    private final OnCounterChangeListener listener;
    private final OnCounterDeleteListener deleteListener;
    private final OnAddCounterListener addListener;

    public interface OnCounterChangeListener {
        void onCounterChanged(String name, int value);
    }

    public interface OnCounterDeleteListener {
        void onCounterDeleted(String name);
    }

    public interface OnAddCounterListener {
        void onAddCounter();
    }

    public static class CounterViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView valueText;
        Button decrementBtn;
        Button incrementBtn;

        public CounterViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.counter_name);
            valueText = itemView.findViewById(R.id.counter_value);
            decrementBtn = itemView.findViewById(R.id.counter_decrement);
            incrementBtn = itemView.findViewById(R.id.counter_increment);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView addText;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            addText = itemView.findViewById(R.id.add_counter_text);
        }
    }

    public CounterAdapter(Context context, ArrayList<Counter> counters, OnCounterChangeListener listener) {
        this(context, counters, listener, null, null);
    }

    public CounterAdapter(Context context, ArrayList<Counter> counters, OnCounterChangeListener listener, OnCounterDeleteListener deleteListener) {
        this(context, counters, listener, deleteListener, null);
    }

    public CounterAdapter(Context context, ArrayList<Counter> counters, OnCounterChangeListener listener, OnCounterDeleteListener deleteListener, OnAddCounterListener addListener) {
        this.context = context;
        this.counters = counters;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.addListener = addListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == counters.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_COUNTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.counter_add_footer, parent, false);
            return new FooterViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.counter_row, parent, false);
        return new CounterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.addText.setOnClickListener(v -> {
                if (addListener != null) {
                    addListener.onAddCounter();
                }
            });
        } else if (holder instanceof CounterViewHolder) {
            CounterViewHolder counterHolder = (CounterViewHolder) holder;
            Counter counter = counters.get(position);
            counterHolder.nameText.setText(counter.getName());
            
            if (counter.isCustom()) {
                counterHolder.nameText.setTypeface(null, Typeface.ITALIC);
            } else {
                counterHolder.nameText.setTypeface(null, Typeface.NORMAL);
            }
            
            counterHolder.valueText.setText(counter.getCurrentValue() + "/" + counter.getMaxValue());

            counterHolder.decrementBtn.setOnClickListener(v -> {
                counter.decrement();
                counterHolder.valueText.setText(counter.getCurrentValue() + "/" + counter.getMaxValue());
                if (listener != null) {
                    listener.onCounterChanged(counter.getName(), counter.getCurrentValue());
                }
            });

            counterHolder.incrementBtn.setOnClickListener(v -> {
                counter.increment();
                counterHolder.valueText.setText(counter.getCurrentValue() + "/" + counter.getMaxValue());
                if (listener != null) {
                    listener.onCounterChanged(counter.getName(), counter.getCurrentValue());
                }
            });

            if (counter.isCustom() && deleteListener != null) {
                counterHolder.nameText.setOnLongClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.getMenu().add("Delete");
                    popup.setOnMenuItemClickListener(item -> {
                        deleteListener.onCounterDeleted(counter.getName());
                        return true;
                    });
                    popup.show();
                    return true;
                });
            } else {
                counterHolder.nameText.setOnLongClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return counters.size() + 1;
    }
}
