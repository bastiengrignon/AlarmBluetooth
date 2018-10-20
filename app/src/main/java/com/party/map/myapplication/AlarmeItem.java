package com.party.map.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmeItem implements Parcelable {
    private String hAlarm;
    private boolean isAlarmEnabled;

    private AlarmeItem(Parcel in) {
        hAlarm = in.readString();
        isAlarmEnabled = in.readByte() != 0;
    }
    AlarmeItem(){}

    public static final Creator<AlarmeItem> CREATOR = new Creator<AlarmeItem>() {
        @Override
        public AlarmeItem createFromParcel(Parcel in) {
            return new AlarmeItem(in);
        }

        @Override
        public AlarmeItem[] newArray(int size) {
            return new AlarmeItem[size];
        }
    };

    String gethAlarm() {
        return hAlarm;
    }

    public void sethAlarm(String hAlarm) {
        this.hAlarm = hAlarm;
    }

    boolean isAlarmEnabled() {
        return isAlarmEnabled;
    }

    public void setAlarmEnabled(boolean alarmEnabled) {
        isAlarmEnabled = alarmEnabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hAlarm);
        dest.writeByte((byte) (isAlarmEnabled ? 1 : 0));    //memo: Si boolean == true -> byte = 1 sinon byte = 0
    }
}
