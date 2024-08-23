package com.j.background_sms;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;

import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.telephony.PhoneNumberUtils;

import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.net.Uri;
import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
/**
 * Encoded-string-value = Text-string | Value-length Char-set Text-string
 */
public class EncodedStringValue implements Cloneable {
    private static final String TAG = "EncodedStringValue";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    /**
     * The Char-set value.
     */
    private int mCharacterSet;
    /**
     * The Text-string value.
     */
    private byte[] mData;
    /**
     * Constructor.
     *
     * @param charset the Char-set value
     * @param data the Text-string value
     * @throws NullPointerException if Text-string value is null.
     */
    public EncodedStringValue(int charset, byte[] data) {
        // TODO: CharSet needs to be validated against MIBEnum.
        if(null == data) {
            throw new NullPointerException("EncodedStringValue: Text-string is null.");
        }
        mCharacterSet = charset;
        mData = new byte[data.length];
        System.arraycopy(data, 0, mData, 0, data.length);
    }
    /**
     * Constructor.
     *
     * @param data the Text-string value
     * @throws NullPointerException if Text-string value is null.
     */
    public EncodedStringValue(byte[] data) {
        this(CharacterSets.DEFAULT_CHARSET, data);
    }
    public EncodedStringValue(String data) {
        try {
            mData = data.getBytes(CharacterSets.DEFAULT_CHARSET_NAME);
            mCharacterSet = CharacterSets.DEFAULT_CHARSET;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Default encoding must be supported.", e);
        }
    }
    /**
     * Get Char-set value.
     *
     * @return the value
     */
    public int getCharacterSet() {
        return mCharacterSet;
    }
    /**
     * Set Char-set value.
     *
     * @param charset the Char-set value
     */
    public void setCharacterSet(int charset) {
        // TODO: CharSet needs to be validated against MIBEnum.
        mCharacterSet = charset;
    }
    /**
     * Get Text-string value.
     *
     * @return the value
     */
    public byte[] getTextString() {
        byte[] byteArray = new byte[mData.length];
        System.arraycopy(mData, 0, byteArray, 0, mData.length);
        return byteArray;
    }
    /**
     * Set Text-string value.
     *
     * @param textString the Text-string value
     * @throws NullPointerException if Text-string value is null.
     */
    public void setTextString(byte[] textString) {
        if(null == textString) {
            throw new NullPointerException("EncodedStringValue: Text-string is null.");
        }
        mData = new byte[textString.length];
        System.arraycopy(textString, 0, mData, 0, textString.length);
    }
    /**
     * Convert this object to a {@link java.lang.String}. If the encoding of
     * the EncodedStringValue is null or unsupported, it will be
     * treated as iso-8859-1 encoding.
     *
     * @return The decoded String.
     */
    public String getString()  {
        if (CharacterSets.ANY_CHARSET == mCharacterSet) {
            return new String(mData); // system default encoding.
        } else {
            try {
                String name = CharacterSets.getMimeName(mCharacterSet);
                return new String(mData, name);
            } catch (UnsupportedEncodingException e) {
            	if (LOCAL_LOGV) {
            		Log.v(TAG, e.getMessage(), e);
            	}
            	try {
                    return new String(mData, CharacterSets.MIMENAME_ISO_8859_1);
                } catch (UnsupportedEncodingException _) {
                    return new String(mData); // system default encoding.
                }
            }
        }
    }
    /**
     * Append to Text-string.
     *
     * @param textString the textString to append
     * @throws NullPointerException if the text String is null
     *                      or an IOException occured.
     */
    public void appendTextString(byte[] textString) {
        if(null == textString) {
            throw new NullPointerException("Text-string is null.");
        }
        if(null == mData) {
            mData = new byte[textString.length];
            System.arraycopy(textString, 0, mData, 0, textString.length);
        } else {
            ByteArrayOutputStream newTextString = new ByteArrayOutputStream();
            try {
                newTextString.write(mData);
                newTextString.write(textString);
            } catch (IOException e) {
                e.printStackTrace();
                throw new NullPointerException(
                        "appendTextString: failed when write a new Text-string");
            }
            mData = newTextString.toByteArray();
        }
    }
    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        int len = mData.length;
        byte[] dstBytes = new byte[len];
        System.arraycopy(mData, 0, dstBytes, 0, len);
        try {
            return new EncodedStringValue(mCharacterSet, dstBytes);
        } catch (Exception e) {
            Log.e(TAG, "failed to clone an EncodedStringValue: " + this);
            e.printStackTrace();
            throw new CloneNotSupportedException(e.getMessage());
        }
    }
    /**
     * Split this encoded string around matches of the given pattern.
     *
     * @param pattern the delimiting pattern
     * @return the array of encoded strings computed by splitting this encoded
     *         string around matches of the given pattern
     */
    public EncodedStringValue[] split(String pattern) {
        String[] temp = getString().split(pattern);
        EncodedStringValue[] ret = new EncodedStringValue[temp.length];
        for (int i = 0; i < ret.length; ++i) {
            try {
                ret[i] = new EncodedStringValue(mCharacterSet,
                        temp[i].getBytes());
            } catch (NullPointerException _) {
                // Can't arrive here
                return null;
            }
        }
        return ret;
    }
    /**
     * Extract an EncodedStringValue[] from a given String.
     */
    public static EncodedStringValue[] extract(String src) {
        String[] values = src.split(";");
        ArrayList<EncodedStringValue> list = new ArrayList<EncodedStringValue>();
        for (int i = 0; i < values.length; i++) {
            if (values[i].length() > 0) {
                list.add(new EncodedStringValue(values[i]));
            }
        }
        int len = list.size();
        if (len > 0) {
            return list.toArray(new EncodedStringValue[len]);
        } else {
            return null;
        }
    }
    /**
     * Concatenate an EncodedStringValue[] into a single String.
     */
    public static String concat(EncodedStringValue[] addr) {
        StringBuilder sb = new StringBuilder();
        int maxIndex = addr.length - 1;
        for (int i = 0; i <= maxIndex; i++) {
            sb.append(addr[i].getString());
            if (i < maxIndex) {
                sb.append(";");
            }
        }
        return sb.toString();
    }
    public static EncodedStringValue copy(EncodedStringValue value) {
        if (value == null) {
            return null;
        }
        return new EncodedStringValue(value.mCharacterSet, value.mData);
    }
    
    public static EncodedStringValue[] encodeStrings(String[] array) {
        int count = array.length;
        if (count > 0) {
            EncodedStringValue[] encodedArray = new EncodedStringValue[count];
            for (int i = 0; i < count; i++) {
                encodedArray[i] = new EncodedStringValue(array[i]);
            }
            return encodedArray;
        }
        return null;
    }
}
/**
 * The pdu part.
 */
public class PduPart {
    /**
     * Well-Known Parameters.
     */
    public static final int P_Q                  = 0x80;
    public static final int P_CHARSET            = 0x81;
    public static final int P_LEVEL              = 0x82;
    public static final int P_TYPE               = 0x83;
    public static final int P_DEP_NAME           = 0x85;
    public static final int P_DEP_FILENAME       = 0x86;
    public static final int P_DIFFERENCES        = 0x87;
    public static final int P_PADDING            = 0x88;
    // This value of "TYPE" s used with Content-Type: multipart/related
    public static final int P_CT_MR_TYPE         = 0x89;
    public static final int P_DEP_START          = 0x8A;
    public static final int P_DEP_START_INFO     = 0x8B;
    public static final int P_DEP_COMMENT        = 0x8C;
    public static final int P_DEP_DOMAIN         = 0x8D;
    public static final int P_MAX_AGE            = 0x8E;
    public static final int P_DEP_PATH           = 0x8F;
    public static final int P_SECURE             = 0x90;
    public static final int P_SEC                = 0x91;
    public static final int P_MAC                = 0x92;
    public static final int P_CREATION_DATE      = 0x93;
    public static final int P_MODIFICATION_DATE  = 0x94;
    public static final int P_READ_DATE          = 0x95;
    public static final int P_SIZE               = 0x96;
    public static final int P_NAME               = 0x97;
    public static final int P_FILENAME           = 0x98;
    public static final int P_START              = 0x99;
    public static final int P_START_INFO         = 0x9A;
    public static final int P_COMMENT            = 0x9B;
    public static final int P_DOMAIN             = 0x9C;
    public static final int P_PATH               = 0x9D;
    /**
     *  Header field names.
     */
     public static final int P_CONTENT_TYPE       = 0x91;
     public static final int P_CONTENT_LOCATION   = 0x8E;
     public static final int P_CONTENT_ID         = 0xC0;
     public static final int P_DEP_CONTENT_DISPOSITION = 0xAE;
     public static final int P_CONTENT_DISPOSITION = 0xC5;
    // The next header is unassigned header, use reserved header(0x48) value.
     public static final int P_CONTENT_TRANSFER_ENCODING = 0xC8;
     /**
      * Content=Transfer-Encoding string.
      */
     public static final String CONTENT_TRANSFER_ENCODING =
             "Content-Transfer-Encoding";
     /**
      * Value of Content-Transfer-Encoding.
      */
     public static final String P_BINARY = "binary";
     public static final String P_7BIT = "7bit";
     public static final String P_8BIT = "8bit";
     public static final String P_BASE64 = "base64";
     public static final String P_QUOTED_PRINTABLE = "quoted-printable";
     /**
      * Value of disposition can be set to PduPart when the value is octet in
      * the PDU.
      * "from-data" instead of Form-data<Octet 128>.
      * "attachment" instead of Attachment<Octet 129>.
      * "inline" instead of Inline<Octet 130>.
      */
     static final byte[] DISPOSITION_FROM_DATA = "from-data".getBytes();
     static final byte[] DISPOSITION_ATTACHMENT = "attachment".getBytes();
     static final byte[] DISPOSITION_INLINE = "inline".getBytes();
     /**
      * Content-Disposition value.
      */
     public static final int P_DISPOSITION_FROM_DATA  = 0x80;
     public static final int P_DISPOSITION_ATTACHMENT = 0x81;
     public static final int P_DISPOSITION_INLINE     = 0x82;
     /**
      * Header of part.
      */
     private Map<Integer, Object> mPartHeader = null;
     /**
      * Data uri.
      */
     private Uri mUri = null;
     /**
      * Part data.
      */
     private byte[] mPartData = null;
     private static final String TAG = "PduPart";
     /**
      * Empty Constructor.
      */
     public PduPart() {
         mPartHeader = new HashMap<Integer, Object>();
     }
     /**
      * Set part data. The data are stored as byte array.
      *
      * @param data the data
      */
     public void setData(byte[] data) {
         if(data == null) {
            return;
        }
         mPartData = new byte[data.length];
         System.arraycopy(data, 0, mPartData, 0, data.length);
     }
     /**
      * @return A copy of the part data or null if the data wasn't set or
      *         the data is stored as Uri.
      * @see #getDataUri
      */
     public byte[] getData() {
         if(mPartData == null) {
            return null;
         }
         byte[] byteArray = new byte[mPartData.length];
         System.arraycopy(mPartData, 0, byteArray, 0, mPartData.length);
         return byteArray;
     }
     /**
      * Set data uri. The data are stored as Uri.
      *
      * @param uri the uri
      */
     public void setDataUri(Uri uri) {
         mUri = uri;
     }
     /**
      * @return The Uri of the part data or null if the data wasn't set or
      *         the data is stored as byte array.
      * @see #getData
      */
     public Uri getDataUri() {
         return mUri;
     }
     /**
      * Set Content-id value
      *
      * @param contentId the content-id value
      * @throws NullPointerException if the value is null.
      */
     public void setContentId(byte[] contentId) {
         if((contentId == null) || (contentId.length == 0)) {
             throw new IllegalArgumentException(
                     "Content-Id may not be null or empty.");
         }
         if ((contentId.length > 1)
                 && ((char) contentId[0] == '<')
                 && ((char) contentId[contentId.length - 1] == '>')) {
             mPartHeader.put(P_CONTENT_ID, contentId);
             return;
         }
         // Insert beginning '<' and trailing '>' for Content-Id.
         byte[] buffer = new byte[contentId.length + 2];
         buffer[0] = (byte) (0xff & '<');
         buffer[buffer.length - 1] = (byte) (0xff & '>');
         System.arraycopy(contentId, 0, buffer, 1, contentId.length);
         mPartHeader.put(P_CONTENT_ID, buffer);
     }
     /**
      * Get Content-id value.
      *
      * @return the value
      */
     public byte[] getContentId() {
         return (byte[]) mPartHeader.get(P_CONTENT_ID);
     }
     /**
      * Set Char-set value.
      *
      * @param charset the value
      */
     public void setCharset(int charset) {
         mPartHeader.put(P_CHARSET, charset);
     }
     /**
      * Get Char-set value
      *
      * @return the charset value. Return 0 if charset was not set.
      */
     public int getCharset() {
         Integer charset = (Integer) mPartHeader.get(P_CHARSET);
         if(charset == null) {
             return 0;
         } else {
             return charset.intValue();
         }
     }
     /**
      * Set Content-Location value.
      *
      * @param contentLocation the value
      * @throws NullPointerException if the value is null.
      */
     public void setContentLocation(byte[] contentLocation) {
         if(contentLocation == null) {
             throw new NullPointerException("null content-location");
         }
         mPartHeader.put(P_CONTENT_LOCATION, contentLocation);
     }
     /**
      * Get Content-Location value.
      *
      * @return the value
      *     return PduPart.disposition[0] instead of <Octet 128> (Form-data).
      *     return PduPart.disposition[1] instead of <Octet 129> (Attachment).
      *     return PduPart.disposition[2] instead of <Octet 130> (Inline).
      */
     public byte[] getContentLocation() {
         return (byte[]) mPartHeader.get(P_CONTENT_LOCATION);
     }
     /**
      * Set Content-Disposition value.
      * Use PduPart.disposition[0] instead of <Octet 128> (Form-data).
      * Use PduPart.disposition[1] instead of <Octet 129> (Attachment).
      * Use PduPart.disposition[2] instead of <Octet 130> (Inline).
      *
      * @param contentDisposition the value
      * @throws NullPointerException if the value is null.
      */
     public void setContentDisposition(byte[] contentDisposition) {
         if(contentDisposition == null) {
             throw new NullPointerException("null content-disposition");
         }
         mPartHeader.put(P_CONTENT_DISPOSITION, contentDisposition);
     }
     /**
      * Get Content-Disposition value.
      *
      * @return the value
      */
     public byte[] getContentDisposition() {
         return (byte[]) mPartHeader.get(P_CONTENT_DISPOSITION);
     }
     /**
      *  Set Content-Type value.
      *
      *  @param value the value
      *  @throws NullPointerException if the value is null.
      */
     public void setContentType(byte[] contentType) {
         if(contentType == null) {
             throw new NullPointerException("null content-type");
         }
         mPartHeader.put(P_CONTENT_TYPE, contentType);
     }
     /**
      * Get Content-Type value of part.
      *
      * @return the value
      */
     public byte[] getContentType() {
         return (byte[]) mPartHeader.get(P_CONTENT_TYPE);
     }
     /**
      * Set Content-Transfer-Encoding value
      *
      * @param contentId the content-id value
      * @throws NullPointerException if the value is null.
      */
     public void setContentTransferEncoding(byte[] contentTransferEncoding) {
         if(contentTransferEncoding == null) {
             throw new NullPointerException("null content-transfer-encoding");
         }
         mPartHeader.put(P_CONTENT_TRANSFER_ENCODING, contentTransferEncoding);
     }
     /**
      * Get Content-Transfer-Encoding value.
      *
      * @return the value
      */
     public byte[] getContentTransferEncoding() {
         return (byte[]) mPartHeader.get(P_CONTENT_TRANSFER_ENCODING);
     }
     /**
      * Set Content-type parameter: name.
      *
      * @param name the name value
      * @throws NullPointerException if the value is null.
      */
     public void setName(byte[] name) {
         if(null == name) {
             throw new NullPointerException("null content-id");
         }
         mPartHeader.put(P_NAME, name);
     }
     /**
      *  Get content-type parameter: name.
      *
      *  @return the name
      */
     public byte[] getName() {
         return (byte[]) mPartHeader.get(P_NAME);
     }
     /**
      * Get Content-disposition parameter: filename
      *
      * @param fileName the filename value
      * @throws NullPointerException if the value is null.
      */
     public void setFilename(byte[] fileName) {
         if(null == fileName) {
             throw new NullPointerException("null content-id");
         }
         mPartHeader.put(P_FILENAME, fileName);
     }
     /**
      * Set Content-disposition parameter: filename
      *
      * @return the filename
      */
     public byte[] getFilename() {
         return (byte[]) mPartHeader.get(P_FILENAME);
     }
    public String generateLocation() {
        // Assumption: At least one of the content-location / name / filename
        // or content-id should be set. This is guaranteed by the PduParser
        // for incoming messages and by MM composer for outgoing messages.
        byte[] location = (byte[]) mPartHeader.get(P_NAME);
        if(null == location) {
            location = (byte[]) mPartHeader.get(P_FILENAME);
            if (null == location) {
                location = (byte[]) mPartHeader.get(P_CONTENT_LOCATION);
            }
        }
        if (null == location) {
            byte[] contentId = (byte[]) mPartHeader.get(P_CONTENT_ID);
            return "cid:" + new String(contentId);
        } else {
            return new String(location);
        }
    }
}

public class PduBody {
    private Vector<PduPart> mParts = null;
    private Map<String, PduPart> mPartMapByContentId = null;
    private Map<String, PduPart> mPartMapByContentLocation = null;
    private Map<String, PduPart> mPartMapByName = null;
    private Map<String, PduPart> mPartMapByFileName = null;
    /**
     * Constructor.
     */
    public PduBody() {
        mParts = new Vector<PduPart>();
        mPartMapByContentId = new HashMap<String, PduPart>();
        mPartMapByContentLocation  = new HashMap<String, PduPart>();
        mPartMapByName = new HashMap<String, PduPart>();
        mPartMapByFileName = new HashMap<String, PduPart>();
    }
    private void putPartToMaps(PduPart part) {
        // Put part to mPartMapByContentId.
        byte[] contentId = part.getContentId();
        if(null != contentId) {
            mPartMapByContentId.put(new String(contentId), part);
        }
        // Put part to mPartMapByContentLocation.
        byte[] contentLocation = part.getContentLocation();
        if(null != contentLocation) {
            String clc = new String(contentLocation);
            mPartMapByContentLocation.put(clc, part);
        }
        // Put part to mPartMapByName.
        byte[] name = part.getName();
        if(null != name) {
            String clc = new String(name);
            mPartMapByName.put(clc, part);
        }
        // Put part to mPartMapByFileName.
        byte[] fileName = part.getFilename();
        if(null != fileName) {
            String clc = new String(fileName);
            mPartMapByFileName.put(clc, part);
        }
    }
    /**
     * Appends the specified part to the end of this body.
     *
     * @param part part to be appended
     * @return true when success, false when fail
     * @throws NullPointerException when part is null
     */
    public boolean addPart(PduPart part) {
        if(null == part) {
            throw new NullPointerException();
        }
        putPartToMaps(part);
        return mParts.add(part);
    }
    /**
     * Inserts the specified part at the specified position.
     *
     * @param index index at which the specified part is to be inserted
     * @param part part to be inserted
     * @throws NullPointerException when part is null
     */
    public void addPart(int index, PduPart part) {
        if(null == part) {
            throw new NullPointerException();
        }
        putPartToMaps(part);
        mParts.add(index, part);
    }
    /**
     * Removes the part at the specified position.
     *
     * @param index index of the part to return
     * @return part at the specified index
     */
    public PduPart removePart(int index) {
        return mParts.remove(index);
    }
    /**
     * Remove all of the parts.
     */
    public void removeAll() {
        mParts.clear();
    }
    /**
     * Get the part at the specified position.
     *
     * @param index index of the part to return
     * @return part at the specified index
     */
    public PduPart getPart(int index) {
        return mParts.get(index);
    }
    /**
     * Get the index of the specified part.
     *
     * @param part the part object
     * @return index the index of the first occurrence of the part in this body
     */
    public int getPartIndex(PduPart part) {
        return mParts.indexOf(part);
    }
    /**
     * Get the number of parts.
     *
     * @return the number of parts
     */
    public int getPartsNum() {
        return mParts.size();
    }
    /**
     * Get pdu part by content id.
     *
     * @param cid the value of content id.
     * @return the pdu part.
     */
    public PduPart getPartByContentId(String cid) {
        return mPartMapByContentId.get(cid);
    }
    /**
     * Get pdu part by Content-Location. Content-Location of part is
     * the same as filename and name(param of content-type).
     *
     * @param fileName the value of filename.
     * @return the pdu part.
     */
    public PduPart getPartByContentLocation(String contentLocation) {
        return mPartMapByContentLocation.get(contentLocation);
    }
    /**
     * Get pdu part by name.
     *
     * @param fileName the value of filename.
     * @return the pdu part.
     */
    public PduPart getPartByName(String name) {
        return mPartMapByName.get(name);
    }
    /**
     * Get pdu part by filename.
     *
     * @param fileName the value of filename.
     * @return the pdu part.
     */
    public PduPart getPartByFileName(String filename) {
        return mPartMapByFileName.get(filename);
    }
}

public class MultimediaMessagePdu extends GenericPdu{
    /**
     * The body.
     */
    private PduBody mMessageBody;
    /**
     * Constructor.
     */
    public MultimediaMessagePdu() {
        super();
    }
    /**
     * Constructor.
     *
     * @param header the header of this PDU
     * @param body the body of this PDU
     */
    public MultimediaMessagePdu(PduHeaders header, PduBody body) {
        super(header);
        mMessageBody = body;
    }
    /**
     * Constructor with given headers.
     *
     * @param headers Headers for this PDU.
     */
    MultimediaMessagePdu(PduHeaders headers) {
        super(headers);
    }
    /**
     * Get body of the PDU.
     *
     * @return the body
     */
    public PduBody getBody() {
        return mMessageBody;
    }
    /**
     * Set body of the PDU.
     *
     * @param body the body
     */
    public void setBody(PduBody body) {
        mMessageBody = body;
    }
    /**
     * Get subject.
     *
     * @return the value
     */
    public EncodedStringValue getSubject() {
        return mPduHeaders.getEncodedStringValue(PduHeaders.SUBJECT);
    }
    /**
     * Set subject.
     *
     * @param value the value
     * @throws NullPointerException if the value is null.
     */
    public void setSubject(EncodedStringValue value) {
        mPduHeaders.setEncodedStringValue(value, PduHeaders.SUBJECT);
    }
    /**
     * Get To value.
     *
     * @return the value
     */
    public EncodedStringValue[] getTo() {
        return mPduHeaders.getEncodedStringValues(PduHeaders.TO);
    }
    /**
     * Add a "To" value.
     *
     * @param value the value
     * @throws NullPointerException if the value is null.
     */
    public void addTo(EncodedStringValue value) {
        mPduHeaders.appendEncodedStringValue(value, PduHeaders.TO);
    }
    /**
     * Get X-Mms-Priority value.
     *
     * @return the value
     */
    public int getPriority() {
        return mPduHeaders.getOctet(PduHeaders.PRIORITY);
    }
    /**
     * Set X-Mms-Priority value.
     *
     * @param value the value
     * @throws InvalidHeaderValueException if the value is invalid.
     */
    public void setPriority(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.PRIORITY);
    }
    /**
     * Get Date value.
     *
     * @return the value
     */
    public long getDate() {
        return mPduHeaders.getLongInteger(PduHeaders.DATE);
    }
    /**
     * Set Date value in seconds.
     *
     * @param value the value
     */
    public void setDate(long value) {
        mPduHeaders.setLongInteger(value, PduHeaders.DATE);
    }
}

public class SendReq extends MultimediaMessagePdu {
    private static final String TAG = "SendReq";
    public SendReq() {
        super();
            setMessageType(PduHeaders.MESSAGE_TYPE_SEND_REQ);
            setMmsVersion(PduHeaders.CURRENT_MMS_VERSION);
            // FIXME: Content-type must be decided according to whether
            // SMIL part present.
            setContentType("application/vnd.wap.multipart.related".getBytes());
            setFrom(new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()));
            setTransactionId(generateTransactionId());
    }
      public void AddTo(EncodedStringValue[] value) {
        mPduHeaders.setEncodedStringValues(value, PduHeaders.TO);
    }
}

/** BackgroundSmsPlugin */
public class BackgroundSmsPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "background_sms");
    channel.setMethodCallHandler(this);
  }


  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "background_sms");
    channel.setMethodCallHandler(new BackgroundSmsPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("sendSms")) {
      String num = call.argument("phone");
      String msg = call.argument("msg");
      Integer simSlot = call.argument("simSlot");
      sendSMS(num, msg, simSlot, result);
    }else if (call.method.equals("sendMms")) {
      String num = call.argument("phone");
      String msg = call.argument("msg");
      String filePath = call.argument("filePath");
      Integer simSlot = call.argument("simSlot");
      sendMMS(num, msg, filePath, simSlot, result);
    }else if(call.method.equals("isSupportMultiSim")) {
      isSupportCustomSim(result);
    } else{
      result.notImplemented();
    }
  }

  private void isSupportCustomSim(Result result){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
      result.success(true);
    }else{
      result.success(false);
    }
  }

  private void sendSMS(String num, String msg, Integer simSlot,Result result) {
    try {
      SmsManager smsManager;
      if (simSlot == null) {
        smsManager = SmsManager.getDefault();
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          smsManager = SmsManager.getSmsManagerForSubscriptionId(simSlot);
        } else {
          smsManager = SmsManager.getDefault();
        }
      }
      smsManager.sendTextMessage(num, null, msg, null, null);
      result.success("Sent");
    } catch (Exception ex) {
      ex.printStackTrace();
      result.error("Failed", "Sms Not Sent", "");
    }
  }

private void sendMMS(String num, String msg, String filePath, Integer simSlot,Result result)
        {
             
      SmsManager smsManager;
      if (simSlot == null) {
        smsManager = SmsManager.getDefault();
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          smsManager = SmsManager.getSmsManagerForSubscriptionId(simSlot);
        } else {
          smsManager = SmsManager.getDefault();
        }
      }
            
            byte[] sendPDUData = GetMMSPDUData(num, filePath, msg);

            if (sendPDUData != null)
            {
                SendMMSData(sendPDUData);
            }
        }

        private byte[] GetMMSPDUData(String DestinationNumber, String filePath, String msg)
        {
            byte[] pduData = null;
            try
            {
                SendReq sendReq = new SendReq();

                sendReq.AddTo(new EncodedStringValue(DestinationNumber));

                PduBody pduBody = new PduBody();

                // Add text message data to message
                PduPart txtPart = new PduPart();
                txtPart.SetData(Encoding.ASCII.GetBytes(msg));
                txtPart.SetContentType(new EncodedStringValue("text/plan").GetTextString());
                txtPart.SetName(new EncodedStringValue("Message").GetTextString());
                pduBody.AddPart(txtPart);

                // Add image data 
                // TODO: Later, this will be audio file. But image file for testing
                PduPart imgPart = new PduPart();
                byte[] sampleImageData = System.IO.File.ReadAllBytes(filePath);

                imgPart.SetData(sampleImageData);
                imgPart.SetContentType(new EncodedStringValue("image/jpg").GetTextString());
                imgPart.SetFilename(new EncodedStringValue(System.IO.Path.GetFileName(filePath)).GetTextString());
                pduBody.AddPart(imgPart);

                // Now create body of MMS
                sendReq.Body = pduBody;
                // Finally, generate the byte array to send to the MMS provider
                PduComposer composer = new PduComposer(sendReq);
                pduData = composer.Make();
            }
            catch(Exception ex)
            {
                // TODO: Do something here
            }
            return pduData;

        }

        private boolean SendMMSData(byte[] PDUData)
        {
            Context ctx = MainActivity.Instance;
            Android.Telephony.SmsManager sm = Android.Telephony.SmsManager.Default;
          
            try
            {
                String cacheFilePath = System.IO.Path.Combine(CTX.CacheDir.AbsolutePath, "send." + "sendMe" + ".dat");
                System.IO.File.WriteAllBytes(cacheFilePath, PDUData);
                Java.IO.File testFile = new Java.IO.File(cacheFilePath);
                byte[] byteArray = System.IO.File.ReadAllBytes(cacheFilePath);


                String authString = CTX.PackageName + ".fileprovider";
                if (System.IO.File.Exists(cacheFilePath))
                {
                    //Android.Net.Uri contentURI = (AndroidX.Core.Content.FileProvider.GetUriForFile(CTX, CTX.PackageName + ".fileprovider", testFile));
                    Android.Net.Uri contentUri = (FileProvider.GetUriForFile(ctx, ctx.PackageName + ".fileprovider", testFile));
                    PendingIntent pendingIntent = PendingIntent.GetBroadcast(CTX, 0, new Intent(CTX.PackageName + ".WAP_PUSH_DELIVER"), 0);

                    sm.SendMultimediaMessage(CTX, contentURI, null, null, pendingIntent);
                }
            }
            catch(Exception ex)
            {
                String exString = ex.ToString();
                return false;
            }
            return true;
        }

  
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
