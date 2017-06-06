package com.serega.recyclerviewfastscroller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.serega.fastscroller.FastScrollAdapter;
import com.serega.fastscroller.FastScroller;

import java.util.Arrays;
import java.util.List;

/**
 * @author S.A.Bobrischev
 *         Developed by Magora Team (magora-systems.com). 2017.
 */
public class PagerFragment extends Fragment {
    private static final String ARG_LIST_SIZE = "ARG_LIST_SIZE";

    public static PagerFragment newInstance(int listSize) {
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_SIZE, listSize);

        PagerFragment fragment = new PagerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pager_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv_list);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv.setVerticalScrollBarEnabled(false);
        int size = getArguments().getInt(ARG_LIST_SIZE, -1);
        if (size == -1) {
            Adapter adapter = new Adapter(Arrays.asList(NameGenerator.NAMES));
            rv.setAdapter(adapter);
        } else {
            Adapter adapter = new Adapter(Arrays.asList(NameGenerator.NAMES).subList(0, size));
            rv.setAdapter(adapter);
        }
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
