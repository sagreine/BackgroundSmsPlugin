package com.j.background_sms;

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