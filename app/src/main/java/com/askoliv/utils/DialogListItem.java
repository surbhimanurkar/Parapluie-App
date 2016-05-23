package com.askoliv.utils;

/**
 * Created by surbhimanurkar on 28-04-2016.
 */
public class DialogListItem {

    private String ListText;
    private int ListIcon;

    public DialogListItem(String listText, int listIcon) {
        ListText = listText;
        ListIcon = listIcon;
    }

    public String getListText() {
        return ListText;
    }

    public void setListText(String listText) {
        ListText = listText;
    }

    public int getListIcon() {
        return ListIcon;
    }

    public void setListIcon(int listIcon) {
        ListIcon = listIcon;
    }
}
