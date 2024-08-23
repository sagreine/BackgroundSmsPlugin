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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.annotation.NonNull;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

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
                // Encoding.ASCII.GetBytes
                txtPart.SetData((msg.GetBytes(StandardCharsets.US_ASCII)));
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
