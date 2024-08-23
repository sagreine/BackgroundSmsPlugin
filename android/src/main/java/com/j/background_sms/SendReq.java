package com.j.background_sms;

import android.util.Log;

public class SendReq extends MultimediaMessagePdu {
    private static final String TAG = "SendReq";
    private static final int P_CONTENT_TYPE_IN       = 0x91;
    public SendReq() {
        super();
        
        try {
            setMessageType(PduHeaders.MESSAGE_TYPE_SEND_REQ);
            setMmsVersion(PduHeaders.CURRENT_MMS_VERSION);
            // FIXME: Content-type must be decided according to whether
            // SMIL part present.
            setContentType("application/vnd.wap.multipart.related".getBytes());
              //mPartHeader.put(P_CONTENT_TYPE_IN, "application/vnd.wap.multipart.related".getBytes());
                 //mPduHeaders.put(P_CONTENT_TYPE_IN, "application/vnd.wap.multipart.related".getBytes());
     
            setFrom(new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()));
            setTransactionId(generateTransactionId());
                  } catch (InvalidHeaderValueException e) {
            // Impossible to reach here since all headers we set above are valid.
            Log.e(TAG, "Unexpected InvalidHeaderValueException.", e);
            throw new RuntimeException(e);
        }
    }
    }
      public void AddTo(EncodedStringValue[] value) {
        mPduHeaders.setEncodedStringValues(value, PduHeaders.TO);
    }
        public void setContentType(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.CONTENT_TYPE);
    }
    public void setTransactionId(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.TRANSACTION_ID);
    }
    public byte[] getTransactionId() {
        return mPduHeaders.getTextString(PduHeaders.TRANSACTION_ID);
    }
    private byte[] generateTransactionId() {
        String transactionId = "T" + Long.toHexString(System.currentTimeMillis());
        return transactionId.getBytes();
    }
}
