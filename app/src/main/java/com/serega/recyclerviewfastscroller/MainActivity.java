package com.serega.recyclerviewfastscroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.serega.fastscroller.FastScrollAdapter;
import com.serega.fastscroller.FastScroller;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setVerticalScrollBarEnabled(false);
        Adapter adapter = new Adapter(Arrays.asList(NameGenerator.NAMES));
        rv.setAdapter(adapter);
        rv.setVerticalScrollbarPosition(RecyclerView.SCROLLBAR_POSITION_RIGHT);

        FastScroller.wrap(rv);
    }

    class Adapter extends RecyclerView.Adapter<ItemHolder> implements FastScrollAdapter {
        private final List<String> names;

        Adapter(List<String> names) {
            this.names = names;
        }

        @Override
        public String getLetter(int position) {
            return names.get(position).charAt(0) + "";
        }

        @Override
        public int getPositionForScrollProgress(float progress) {
            return (int) ((getItemCount() - 1) * progress);
        }

        @Override
        public int getItemCount() {
            return names.size();
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return ItemHolder.createHolder(viewGroup);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.text.setText(names.get(position));
        }
    }
}
