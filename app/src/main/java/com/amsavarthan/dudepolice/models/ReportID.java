package com.amsavarthan.dudepolice.models;

import android.support.annotation.NonNull;

class ReportID {
    public String reportID;

    public <T extends ReportID> T withId(@NonNull final String id) {
        this.reportID = id;
        return (T) this;
    }
}
