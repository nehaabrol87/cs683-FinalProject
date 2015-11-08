package com.nehaabrol.parkingpursuit;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class CustomAutoCompleteTextView extends AutoCompleteTextView {

    private Context context;

    public CustomAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("description");
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {

//        String urlString = "http://api.parkwhiz.com/search/?destination=312+N+wacker+Dr,+Chicago&start=1446942628&end=1446953428&key=62d882d8cfe5680004fa849286b6ce20";
//        GetAPIResults apiResults = new GetAPIResults(context);
//        apiResults.execute(urlString);
    }

}
