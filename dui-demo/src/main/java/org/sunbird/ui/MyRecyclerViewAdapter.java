package org.sunbird.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONObject;
import org.sunbird.R;

import java.util.ArrayList;

import in.juspay.mystique.DynamicUI;
import in.juspay.mystique.Renderer;

/**
 * Created by amitrohan on 01/05/17.
 */

public class MyRecyclerViewAdapter extends
        RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<String> runInUI;
    private final DynamicUI dynamicUI;
    private final Renderer renderer;
    private final ArrayList<String> views;
    private final ArrayList<Integer> viewTypeList;
    private final Activity mActivity;
    private int itemCount = 1;

    public MyRecyclerViewAdapter(Context context, int itemCount, ArrayList<String> values, ArrayList<String> view,
                                 ArrayList<Integer> viewTypeList, DynamicUI dynamicUI, Activity activity) {
        this.context = context;
        this.itemCount = itemCount;
        this.runInUI = values;
        this.dynamicUI = dynamicUI;
        this.renderer = dynamicUI.getJsInterface().getRenderer();
        this.views = view;
        this.viewTypeList = viewTypeList;
        this.mActivity = activity;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Log.e("recyclerViewAdapter :", position + "");
        if (holder.valueSet == 0)
            try {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View convertView = null;
                        try {
                            convertView = createRowView(position);

                            convertView.setTag(holder);
                            holder.parent.addView(convertView);
                            holder.valueSet = 1;
//                    // holder.setIsRecyclable(false);
                            String toRun = runInUI.get(position);
                            Log.e("RUNN :", toRun);
                            toRun = toRun.replace("ctx", "parent");
                            dynamicUI.getJsInterface().runInUI(convertView, toRun);
                        } catch (Exception e) {
                            Log.e("Errore ,", "asdad");
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void addItemsToList(ArrayList<String> values, ArrayList<String> view, ArrayList<Integer> viewTypeList) {
        this.views.addAll(0, view);
        this.viewTypeList.addAll(0, viewTypeList);
        this.runInUI.addAll(0, values);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (views.size() == 0)
            return 1;
        return views.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        Object listItem = views.get(position);
        return listItem.hashCode();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_tem, parent, false);

        return new MyViewHolder(v);
    }

    private View createRowView(int position) throws Exception {
        final JSONObject myObj = new JSONObject(views.get(position));
        View myView = renderer.createView(myObj);
        return myView;
    }

    /**
     * View holder class
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        int valueSet;
        LinearLayout parent;

        public MyViewHolder(View view) {
            super(view);
            valueSet = 0;
            parent = (LinearLayout) view.findViewById(R.id.contentList);

        }
    }
}