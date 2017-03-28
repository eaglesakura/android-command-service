package com.eaglesakura.android.service.data;

import com.eaglesakura.util.CollectionUtil;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * コマンドのやり取りに利用されるペイロード
 */
public final class Payload implements Parcelable {

    /**
     * デフォルトで使用されるバッファ
     */
    private byte[] mBuffer = null;

    /**
     * その他の拡張バッファ
     */
    private final Map<String, byte[]> mExtraBuffers = new HashMap<>();

    /**
     * その他の互換バッファ
     */
    private final Map<String, ParcelablePayload> mExtraParcelable = new HashMap<>();

    private static final int SERIALIZE_MAIN_BUFFER = 0x01 << 0;
    private static final int SERIALIZE_EXTRA_BUFFER = 0x01 << 1;
    private static final int SERIALIZE_EXTRA_PARCERCELABLE = 0x01 << 2;

    /**
     * 文字列から生成する
     */
    public static Payload fromString(String str) {
        if (str != null) {
            return new Payload(str.getBytes());
        } else {
            return new Payload((byte[]) null);
        }
    }

    public Payload(byte[] buffer) {
        if (buffer != null) {
            this.mBuffer = buffer;
        }
    }

    public byte[] getBuffer() {
        return mBuffer;
    }

    public Payload put(String key, byte[] buffer) {
        mExtraBuffers.put(key, buffer);
        return this;
    }

    public Payload put(String key, Parcelable data) {
        mExtraParcelable.put(key, new ParcelablePayload(data));
        return this;
    }

    public byte[] getBuffer(String key) {
        return mExtraBuffers.get(key);
    }

    public <T extends Parcelable> T getParcelable(String key) {
        ParcelablePayload payload = mExtraParcelable.get(key);
        if (payload == null) {
            return null;
        } else {
            return (T) (payload.data);
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static String deserializeStringOrNull(Payload payload) {
        if (payload != null && payload.getBuffer() != null) {
            return new String(payload.getBuffer());
        }
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int serializeFlags = 0;

        // シリアライズフラグを指定する
        {
            if (!CollectionUtil.isEmpty(mBuffer)) {
                serializeFlags |= SERIALIZE_MAIN_BUFFER;
            }
            if (!mExtraBuffers.isEmpty()) {
                serializeFlags |= SERIALIZE_EXTRA_BUFFER;
            }
            if (!mExtraParcelable.isEmpty()) {
                serializeFlags |= SERIALIZE_EXTRA_PARCERCELABLE;
            }

            dest.writeInt(serializeFlags);
        }

        if ((serializeFlags & SERIALIZE_MAIN_BUFFER) != 0) {
            dest.writeByteArray(this.mBuffer);
        }

        if ((serializeFlags & SERIALIZE_EXTRA_BUFFER) != 0) {
            dest.writeInt(mExtraBuffers.size());
            Iterator<Map.Entry<String, byte[]>> iterator = mExtraBuffers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, byte[]> entry = iterator.next();
                dest.writeString(entry.getKey());
                dest.writeByteArray(entry.getValue());
            }
        }

        if ((serializeFlags & SERIALIZE_EXTRA_PARCERCELABLE) != 0) {
            dest.writeInt(mExtraParcelable.size());
            Iterator<Map.Entry<String, ParcelablePayload>> iterator = mExtraParcelable.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ParcelablePayload> entry = iterator.next();
                dest.writeString(entry.getKey());
                entry.getValue().writeToParcel(dest, flags);
            }
        }
    }

    protected Payload(Parcel in) {

        int serializeFlags = in.readInt();
        if ((serializeFlags & SERIALIZE_MAIN_BUFFER) != 0) {
            this.mBuffer = in.createByteArray();
        }

        if ((serializeFlags & SERIALIZE_EXTRA_BUFFER) != 0) {
            int size = in.readInt();
            for (int i = 0; i < size; ++i) {
                String key = in.readString();
                mExtraBuffers.put(key, in.createByteArray());
            }
        }

        if ((serializeFlags & SERIALIZE_EXTRA_PARCERCELABLE) != 0) {
            int size = in.readInt();
            for (int i = 0; i < size; ++i) {
                String key = in.readString();
                ParcelablePayload pp = ParcelablePayload.CREATOR.createFromParcel(in);
                mExtraParcelable.put(key, pp);
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
