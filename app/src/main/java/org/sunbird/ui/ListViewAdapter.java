package org.sunbird.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.json.JSONObject;
import org.sunbird.R;

import java.util.ArrayList;

import in.juspay.mystique.DynamicUI;

/**
 * Created by stpl on 23/2/17.
 */

public class ListViewAdapter extends ArrayAdapter<String> {

    private int itemCount = 1;
    private ArrayList<String> runInUI;
    private final DynamicUI dynamicUI;
    private final in.juspay.mystique.Renderer renderer;
    private ArrayList<String> views;
    private ArrayList<Integer> viewTypeList;

    public ListViewAdapter(Context context, int itemCount, ArrayList<String> values, ArrayList<String> view, ArrayList<Integer> viewTypeList, DynamicUI dynamicUI) throws Exception {
        super(context, R.layout.list_view_tem, values);
        this.itemCount = itemCount;
        this.runInUI = values;
        this.dynamicUI = dynamicUI;
        this.renderer = dynamicUI.getJsInterface().getRenderer();
        this.views = view;
        this.viewTypeList = viewTypeList;
    }

    static class ViewHolder {
        public View element;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = createRowView(parent,position);
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
            return null;
        }
    }

    @Override
    public int getViewTypeCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
       /* Log.e("view!", "getItemViewType: "+viewTypeList.get(position));
        return viewTypeList.get(position);*/
    }

    public void addItemsToList(int itemCount, ArrayList<String> values, ArrayList<String> view, ArrayList<Integer> viewTypeList) {
        this.itemCount = itemCount;
        this.runInUI.addAll(values);
        this.views.addAll(view);
        this.viewTypeList.addAll(viewTypeList);
    }

    private View createRowView(ViewGroup parent, int position) throws Exception {
        final JSONObject myObj = new JSONObject(views.get(position));
        View myView = renderer.createView(myObj);
        return myView;
    }

    public View getViewFromJsx (String Jsx) {
        try {
            return renderer.createView(new JSONObject(Jsx));
        } catch (Exception e) {
            return null;
        }
    }

    public int getViewLength () {
        return this.views.size();
    }
}
