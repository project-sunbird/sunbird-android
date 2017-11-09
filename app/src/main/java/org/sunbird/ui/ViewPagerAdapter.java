package org.sunbird.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.ArrayList;

import in.juspay.mystique.DynamicUI;
import in.juspay.mystique.Renderer;

/**
 * Created by Dayanidhi on 01/03/17.
 */

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> runInUI = new ArrayList<>();
    private DynamicUI dynamicUI;
    private Renderer renderer;
    private ArrayList<String> viewsArrayList;
    private ArrayList<String> titleTextArrayList;

    public ViewPagerAdapter(Context context, ArrayList<String> values, ArrayList<String> view, ArrayList<String> titleText, DynamicUI dynamicUI) {
        this.context = context;
        this.runInUI = values;
        this.dynamicUI = dynamicUI;
        this.renderer = dynamicUI.getJsInterface().getRenderer();
        this.viewsArrayList = view;
        this.titleTextArrayList = titleText;
    }

    @Override
    public int getCount() {
        return runInUI.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View convertView = createRowView(position);
        container.addView(convertView);
        String toRun = runInUI.get(position);
        toRun = toRun.replace("ctx", "parent");
        dynamicUI.getJsInterface().runInUI(convertView, toRun);
        return convertView;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        SpannableString spannableString = new SpannableString(titleTextArrayList.get(position));
        return spannableString;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private View createRowView(int position) {
        try {
            final JSONObject myObj = new JSONObject(viewsArrayList.get(position));
            return renderer.createView(myObj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void replacePage(int positon, String values, String views) {
        this.runInUI.set(positon, values);
        this.viewsArrayList.set(positon, views);
        notifyDataSetChanged();
    }
}
