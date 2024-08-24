package com.j.background_sms;


import android.content.Context;
import android.drm.DrmConvertedStatus;
import android.drm.DrmManagerClient;
import android.util.Log;
//import android.provider.Downloads;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
public class DrmConvertSession {
    private DrmManagerClient mDrmClient;
    private int mConvertSessionId;
    private static final String TAG = "DrmConvertSession";
      * This download hasn't stated yet
         */
        public static final int STATUS_PENDING = 190;
        /**
         * This download has started
         */
        public static final int STATUS_RUNNING = 192;
        /**
         * This download has been paused by the owning app.
         */
        public static final int STATUS_PAUSED_BY_APP = 193;
        /**
         * This download encountered some network error and is waiting before retrying the request.
         */
        public static final int STATUS_WAITING_TO_RETRY = 194;
        /**
         * This download is waiting for network connectivity to proceed.
         */
        public static final int STATUS_WAITING_FOR_NETWORK = 195;
        /**
         * This download exceeded a size limit for mobile networks and is waiting for a Wi-Fi
         * connection to proceed.
         */
        public static final int STATUS_QUEUED_FOR_WIFI = 196;
        /**
         * This download couldn't be completed due to insufficient storage
         * space.  Typically, this is because the SD card is full.
         */
        public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 198;
        /**
         * This download couldn't be completed because no external storage
         * device was found.  Typically, this is because the SD card is not
         * mounted.
         */
        public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 199;
        /**
         * This download has successfully completed.
         * Warning: there might be other status values that indicate success
         * in the future.
         * Use isStatusSuccess() to capture the entire category.
         */
        public static final int STATUS_SUCCESS = 200;
        /**
         * This request couldn't be parsed. This is also used when processing
         * requests with unknown/unsupported URI schemes.
         */
        public static final int STATUS_BAD_REQUEST = 400;
        /**
         * This download can't be performed because the content type cannot be
         * handled.
         */
        public static final int STATUS_NOT_ACCEPTABLE = 406;
        /**
         * This download cannot be performed because the length cannot be
         * determined accurately. This is the code for the HTTP error "Length
         * Required", which is typically used when making requests that require
         * a content length but don't have one, and it is also used in the
         * client when a response is received whose length cannot be determined
         * accurately (therefore making it impossible to know when a download
         * completes).
         */
        public static final int STATUS_LENGTH_REQUIRED = 411;
        /**
         * This download was interrupted and cannot be resumed.
         * This is the code for the HTTP error "Precondition Failed", and it is
         * also used in situations where the client doesn't have an ETag at all.
         */
        public static final int STATUS_PRECONDITION_FAILED = 412;
        /**
         * The lowest-valued error status that is not an actual HTTP status code.
         */
        public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;
        /**
         * The requested destination file already exists.
         */
        public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;
        /**
         * Some possibly transient error occurred, but we can't resume the download.
         */
        public static final int STATUS_CANNOT_RESUME = 489;
        /**
         * This download was canceled
         */
        public static final int STATUS_CANCELED = 490;
        /**
         * This download has completed with an error.
         * Warning: there will be other status values that indicate errors in
         * the future. Use isStatusError() to capture the entire category.
         */
        public static final int STATUS_UNKNOWN_ERROR = 491;
        /**
         * This download couldn't be completed because of a storage issue.
         * Typically, that's because the filesystem is missing or full.
         * Use the more specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR}
         * and {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
         */
        public static final int STATUS_FILE_ERROR = 492;
        /**
         * This download couldn't be completed because of an HTTP
         * redirect response that the download manager couldn't
         * handle.
         */
        public static final int STATUS_UNHANDLED_REDIRECT = 493;
        /**
         * This download couldn't be completed because of an
         * unspecified unhandled HTTP code.
         */
        public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
        /**
         * This download couldn't be completed because of an
         * error receiving or processing data at the HTTP level.
         */
        public static final int STATUS_HTTP_DATA_ERROR = 495;
        /**
         * This download couldn't be completed because of an
         * HttpException while setting up the request.
         */
        public static final int STATUS_HTTP_EXCEPTION = 496;
        /**
         * This download couldn't be completed because there were
         * too many redirects.
         */
        public static final int STATUS_TOO_MANY_REDIRECTS = 497;
        /**
         * This download has failed because requesting application has been
         * blocked by {@link NetworkPolicyManager}.
         *
         * @hide
         * @deprecated since behavior now uses
         *             {@link #STATUS_WAITING_FOR_NETWORK}
         */
        @Deprecated
        public static final int STATUS_BLOCKED = 498;

    private DrmConvertSession(DrmManagerClient drmClient, int convertSessionId) {
        mDrmClient = drmClient;
        mConvertSessionId = convertSessionId;
    }
    /**
     * Start of converting a file.
     *
     * @param context The context of the application running the convert session.
     * @param mimeType Mimetype of content that shall be converted.
     * @return A convert session or null in case an error occurs.
     */
    public static DrmConvertSession open(Context context, String mimeType) {
        DrmManagerClient drmClient = null;
        int convertSessionId = -1;
        if (context != null && mimeType != null && !mimeType.equals("")) {
            try {
                drmClient = new DrmManagerClient(context);
                try {
                    convertSessionId = drmClient.openConvertSession(mimeType);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Conversion of Mimetype: " + mimeType
                            + " is not supported.", e);
                } catch (IllegalStateException e) {
                    Log.w(TAG, "Could not access Open DrmFramework.", e);
                }
            } catch (IllegalArgumentException e) {
                Log.w(TAG,
                        "DrmManagerClient instance could not be created, context is Illegal.");
            } catch (IllegalStateException e) {
                Log.w(TAG, "DrmManagerClient didn't initialize properly.");
            }
        }
        if (drmClient == null || convertSessionId < 0) {
            return null;
        } else {
            return new DrmConvertSession(drmClient, convertSessionId);
        }
    }
    /**
     * Convert a buffer of data to protected format.
     *
     * @param buffer Buffer filled with data to convert.
     * @param size The number of bytes that shall be converted.
     * @return A Buffer filled with converted data, if execution is ok, in all
     *         other case null.
     */
    public byte [] convert(byte[] inBuffer, int size) {
        byte[] result = null;
        if (inBuffer != null) {
            DrmConvertedStatus convertedStatus = null;
            try {
                if (size != inBuffer.length) {
                    byte[] buf = new byte[size];
                    System.arraycopy(inBuffer, 0, buf, 0, size);
                    convertedStatus = mDrmClient.convertData(mConvertSessionId, buf);
                } else {
                    convertedStatus = mDrmClient.convertData(mConvertSessionId, inBuffer);
                }
                if (convertedStatus != null &&
                        convertedStatus.statusCode == DrmConvertedStatus.STATUS_OK &&
                        convertedStatus.convertedData != null) {
                    result = convertedStatus.convertedData;
                }
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Buffer with data to convert is illegal. Convertsession: "
                        + mConvertSessionId, e);
            } catch (IllegalStateException e) {
                Log.w(TAG, "Could not convert data. Convertsession: " +
                        mConvertSessionId, e);
            }
        } else {
            throw new IllegalArgumentException("Parameter inBuffer is null");
        }
        return result;
    }
    /**
     * Ends a conversion session of a file.
     *
     * @param fileName The filename of the converted file.
     * @return Downloads.Impl.STATUS_SUCCESS if execution is ok.
     *         Downloads.Impl.STATUS_FILE_ERROR in case converted file can not
     *         be accessed. Downloads.Impl.STATUS_NOT_ACCEPTABLE if a problem
     *         occurs when accessing drm framework.
     *         Downloads.Impl.STATUS_UNKNOWN_ERROR if a general error occurred.
     */
    public int close(String filename) {
        DrmConvertedStatus convertedStatus = null;
        int result = STATUS_UNKNOWN_ERROR;
        //int result = 0;
        if (mDrmClient != null && mConvertSessionId >= 0) {
            try {
                convertedStatus = mDrmClient.closeConvertSession(mConvertSessionId);
                if (convertedStatus == null ||
                        convertedStatus.statusCode != DrmConvertedStatus.STATUS_OK ||
                        convertedStatus.convertedData == null) {
                    result = STATUS_NOT_ACCEPTABLE;
                    //result = 0;
                } else {
                    RandomAccessFile rndAccessFile = null;
                    try {
                        rndAccessFile = new RandomAccessFile(filename, "rw");
                        rndAccessFile.seek(convertedStatus.offset);
                        rndAccessFile.write(convertedStatus.convertedData);
                        result = STATUS_SUCCESS;
                    } catch (FileNotFoundException e) {
                        result = STATUS_FILE_ERROR;
                        Log.w(TAG, "File: " + filename + " could not be found.", e);
                    } catch (IOException e) {
                        result = STATUS_FILE_ERROR;
                        Log.w(TAG, "Could not access File: " + filename + " .", e);
                    } catch (IllegalArgumentException e) {
                        result = STATUS_FILE_ERROR;
                        Log.w(TAG, "Could not open file in mode: rw", e);
                    } catch (SecurityException e) {
                        Log.w(TAG, "Access to File: " + filename +
                                " was denied denied by SecurityManager.", e);
                    } finally {
                        if (rndAccessFile != null) {
                            try {
                                rndAccessFile.close();
                            } catch (IOException e) {
                                result = STATUS_FILE_ERROR;
                                Log.w(TAG, "Failed to close File:" + filename
                                        + ".", e);
                            }
                        }
                    }
                }
            } catch (IllegalStateException e) {
                Log.w(TAG, "Could not close convertsession. Convertsession: " +
                        mConvertSessionId, e);
            }
        }
        return result;
    }
}