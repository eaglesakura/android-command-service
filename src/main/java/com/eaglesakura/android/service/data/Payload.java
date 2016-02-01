package com.eaglesakura.android.service.data;

import com.eaglesakura.android.db.BaseProperties;
import com.eaglesakura.util.Util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * コマンドのやり取りに利用されるペイロード
 */
public class Payload implements Parcelable {

    /**
     * デフォルトで使用されるバッファ
     */
    private byte[] buffer = null;

    /**
     * その他の拡張バッファ
     */
    private List<byte[]> extraBuffers = null;

    /**
     * その他の互換バッファ
     */
    private List<ParcelablePayload> extraParcelable = null;

    private static final int SERIALIZE_MAIN_BUFFER = 0x01 << 0;
    private static final int SERIALIZE_EXTRA_BUFFER = 0x01 << 1;
    private static final int SERIALIZE_EXTRA_PARCERCELABLE = 0x01 << 2;

    public Payload(byte[] buffer) {
        if (buffer != null) {
            this.buffer = buffer;
        }
    }

    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public <T extends BaseProperties> T deserializePropOrNull(Class<T> clazz) {
        try {
            if (Util.isEmpty(buffer)) {
                return null;
            }

            return BaseProperties.deserializeInstance(null, clazz, buffer);
        } catch (Exception e) {
        }
        return null;
    }

    public static <T extends BaseProperties> T deserializePropOrNull(Payload payload, Class<T> clazz) {
        if (payload != null) {
            return payload.deserializePropOrNull(clazz);
        } else {
            return null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int serializeFlags = 0;

        // シリアライズフラグを指定する
        {
            if (!Util.isEmpty(buffer)) {
                serializeFlags |= SERIALIZE_MAIN_BUFFER;
            }
            if (!Util.isEmpty(extraBuffers)) {
                serializeFlags |= SERIALIZE_EXTRA_BUFFER;
            }
            if (!Util.isEmpty(extraParcelable)) {
                serializeFlags |= SERIALIZE_EXTRA_PARCERCELABLE;
            }

            dest.writeInt(serializeFlags);
        }

        if ((serializeFlags & SERIALIZE_MAIN_BUFFER) != 0) {
            dest.writeByteArray(this.buffer);
        }

        if ((serializeFlags & SERIALIZE_EXTRA_BUFFER) != 0) {
            dest.writeInt(extraBuffers.size());
            for (byte[] buf : extraBuffers) {
                dest.writeByteArray(buf);
            }
        }

        if ((serializeFlags & SERIALIZE_EXTRA_PARCERCELABLE) != 0) {
            dest.writeInt(extraParcelable.size());
            for (ParcelablePayload pp : extraParcelable) {
                pp.writeToParcel(dest, flags);
            }
        }
    }

    protected Payload(Parcel in) {

        int serializeFlags = in.readInt();
        if ((serializeFlags & SERIALIZE_MAIN_BUFFER) != 0) {
            this.buffer = in.createByteArray();
        }

        if ((serializeFlags & SERIALIZE_EXTRA_BUFFER) != 0) {
            extraBuffers = new ArrayList<>();
            int size = in.readInt();
            for (int i = 0; i < size; ++i) {
                extraBuffers.add(in.createByteArray());
            }
        }

        if ((serializeFlags & SERIALIZE_EXTRA_PARCERCELABLE) != 0) {
            extraParcelable = new ArrayList<>();
            int size = in.readInt();
            for (int i = 0; i < size; ++i) {
                ParcelablePayload pp = ParcelablePayload.CREATOR.createFromParcel(in);
                extraParcelable.add(pp);
            }
        }

    }

    public static final Creator<Payload> CREATOR = new Creator<Payload>() {
        public Payload createFromParcel(Parcel source) {
            return new Payload(source);
        }

        public Payload[] newArray(int size) {
            return new Payload[size];
        }
    };
}
