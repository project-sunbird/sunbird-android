package org.sunbird.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.json.JSONObject;
import org.sunbird.R;

import java.util.ArrayList;

import in.juspay.mystique.DynamicUI;
import in.juspay.mystique.Renderer;

/**
 * Created by stpl on 23/2/17.
 */

public class ListViewAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> runInUI;
    private final DynamicUI dynamicUI;
    private final Renderer renderer;
    private final ArrayList<String> views;
    private final ArrayList<Integer> viewTypeList;
    private int itemCount = 1;

    public ListViewAdapter(Context context, int itemCount, ArrayList<String> values, ArrayList<String> view,
                           ArrayList<Integer> viewTypeList, DynamicUI dynamicUI) throws Exception {
        super(context, R.layout.list_view_tem, values);
        this.context = context;
        this.itemCount = itemCount;
        this.runInUI = values;
        this.dynamicUI = dynamicUI;
        this.renderer = dynamicUI.getJsInterface().getRenderer();
        this.views = view;
        this.viewTypeList = viewTypeList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = createRowView(parent, position);
                viewHolder = new ViewHolder();
                viewHolder.element = convertView;
                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();
            String toRun = runInUI.get(position);
            toRun = toRun.replace("ctx", "parent");
            dynamicUI.getJsInterface().runInUI(viewHolder.element, toRun);

            return convertView;
        } catch (Exception e) {
            Log.d("BREAK:", e + "");
            return null;
        }
    }

    public void addItemsToList(ArrayList<String> values, ArrayList<String> view, ArrayList<Integer> viewTypeList) {
        this.views.addAll(0, view);
        this.viewTypeList.addAll(0, viewTypeList);
        this.runInUI.addAll(0, values);
        notifyDataSetChanged();
    }

    public void replaceItemsInList(String values, String view, Integer viewTypeList, int index) {
        this.views.set(index, view);
        this.viewTypeList.set(index, viewTypeList);
        this.runInUI.set(index, values);
        notifyDataSetChanged();
    }

    private View createRowView(ViewGroup parent, int position) throws Exception {
        final JSONObject myObj = new JSONObject(views.get(position));
        View myView = renderer.createView(myObj);
        return myView;
    }

    @Override
    public int getViewTypeCount() {
        if (views.size() == 0) {
            return 1;
        }
        return views.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class ViewHolder {
        public View element;
    }

}