package com.eaglesakura.android.service.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 */
public class ParcelablePayload implements Parcelable {
    String type;
    Parcelable data;

    public ParcelablePayload(Parcelable data) {
        this.data = data;
        this.type = data.getClass().toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeParcelable(this.data, 0);
    }


    protected ParcelablePayload(Parcel in) {
        this.type = in.readString();

        try {
            Class<?> clazz = Class.forName(type);
            Field CREATOR = clazz.getField("CREATOR");
            Method createFromParcel = CREATOR.getType().getMethod("createFromParcel");
            this.data = (Parcelable) createFromParcel.invoke(CREATOR, in);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    public static final Creator<ParcelablePayload> CREATOR = new Creator<ParcelablePayload>() {
        public ParcelablePayload createFromParcel(Parcel source) {
            return new ParcelablePayload(source);
        }

        public ParcelablePayload[] newArray(int size) {
            return new ParcelablePayload[size];
        }
    };
}
