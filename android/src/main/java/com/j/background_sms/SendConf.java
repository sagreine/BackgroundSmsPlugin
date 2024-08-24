package com.j.background_sms;

public class SendConf extends GenericPdu {
    /**
     * Empty constructor.
     * Since the Pdu corresponding to this class is constructed
     * by the Proxy-Relay server, this class is only instantiated
     * by the Pdu Parser.
     *
     * @throws InvalidHeaderValueException if error occurs.
     */
    public SendConf() throws InvalidHeaderValueException {
        super();
        setMessageType(PduHeaders.MESSAGE_TYPE_SEND_CONF);
    }
    /**
     * Constructor with given headers.
     *
     * @param headers Headers for this PDU.
     */
    SendConf(PduHeaders headers) {
        super(headers);
    }
    /**
     * Get Message-ID value.
     *
     * @return the value
     */
    public byte[] getMessageId() {
        return mPduHeaders.getTextString(PduHeaders.MESSAGE_ID);
    }
    /**
     * Set Message-ID value.
     *
     * @param value the value
     * @throws NullPointerException if the value is null.
     */
    public void setMessageId(byte[] value) {
        mPduHeaders.setTextString(value, PduHeaders.MESSAGE_ID);
    }
    /**
     * Get X-Mms-Response-Status.
     *
     * @return the value
     */
    public int getResponseStatus() {
        return mPduHeaders.getOctet(PduHeaders.RESPONSE_STATUS);
    }
    /**
     * Set X-Mms-Response-Status.
     *
     * @param value the values
     * @throws InvalidHeaderValueException if the value is invalid.
     */
    public void setResponseStatus(int value) throws InvalidHeaderValueException {
        mPduHeaders.setOctet(value, PduHeaders.RESPONSE_STATUS);
    }
    /**
     * Get X-Mms-Transaction-Id field value.
     *
     * @return the X-Mms-Report-Allowed value
     */
    public byte[] getTransactionId() {
        return mPduHeaders.getTextString(PduHeaders.TRANSACTION_ID);
    }
    /**
     * Set X-Mms-Transaction-Id field value.
     *
     * @param value the value
     * @throws NullPointerException if the value is null.
     */
    public void setTransactionId(byte[] value) {
            mPduHeaders.setTextString(value, PduHeaders.TRANSACTION_ID);
    }
    /*
     * Optional, not supported header fields:
     *
     *    public byte[] getContentLocation() {return null;}
     *    public void setContentLocation(byte[] value) {}
     *
     *    public EncodedStringValue getResponseText() {return null;}
     *    public void setResponseText(EncodedStringValue value) {}
     *
     *    public byte getStoreStatus() {return 0x00;}
     *    public void setStoreStatus(byte value) {}
     *
     *    public byte[] getStoreStatusText() {return null;}
     *    public void setStoreStatusText(byte[] value) {}
     */
}