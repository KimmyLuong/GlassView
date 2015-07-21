package com.example.kimmy.glasstest;

import java.util.Date;

/**
 * Created by Karl on 2/11/2015.
 *
 * SaveState Class is a group of signals for one "experiment"
 *
 * It is a container class for a group of signals that interact with a single VI.
 *
 */
public class SaveState
{
    Date lastEdit;
    Signal[] signalList;

    public Signal[] getSignalList() { return signalList; }

    public void setSignalList(Signal[] signalList) {
        this.signalList = signalList;
    }

    public Date getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Date lastEdit) {
        this.lastEdit = lastEdit;
    }



}
