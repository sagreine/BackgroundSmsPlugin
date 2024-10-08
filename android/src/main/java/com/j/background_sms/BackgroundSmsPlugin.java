package com.j.background_sms; 

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

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

import android.util.Log;

import java.io.File;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
import androidx.core.content.FileProvider;
//import androidx.documentfile.provider.DocumentFile;

import java.util.UUID;
import android.app.Activity;
import android.content.Context;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** BackgroundSmsPlugin */
public class BackgroundSmsPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context context;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "background_sms");
    channel.setMethodCallHandler(this);     
    //context = flutterPluginBinding.applicationContext;
  }
  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding ) {
    activity = binding.getActivity();
}
      @Override
    public void onDetachedFromActivityForConfigChanges() {
        // TODO: the Activity your plugin was attached to was destroyed to change configuration.
        // This call will be followed by onReattachedToActivityForConfigChanges().
    }
    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        // TODO: your plugin is now attached to a new Activity after a configuration change.
    }

    @Override
    public void onDetachedFromActivity() {
        // TODO: your plugin is no longer associated with an Activity. Clean up references.
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
    }else if (call.method.equals("PrintFromJava")){
                String myString = PrintFromJava();
                result.success(myString);
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

  private String PrintFromJava(){
        System.out.print("this is a print in java mainactivity");
     //return str;
        return "This is a string returned from Java";
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
      result.success("Sent SMS");
    } catch (Exception ex) {
      ex.printStackTrace();
      result.error("Failed", "Sms Not Sent", "");
    }
  }

private void sendMMS(String num, String msg, String filePath, Integer simSlot,Result result)
    {
      context = activity.getApplicationContext();    
         try{   
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
            
            byte[] sendPDUData = GetMMSPDUData(num, filePath, msg, result);

            if (sendPDUData != null)
            {
              SendMMSData(sendPDUData, simSlot, smsManager, result);
             result.success("Sent MMS yay");
            }
           
        }catch (Exception ex) {
      ex.printStackTrace();
      result.error("Failed", "Mms Not Sent", "");
      }
   }

        private byte[] GetMMSPDUData(String DestinationNumber, String filePath, String msg, Result result)
        {
            byte[] pduData = null;
            try
            {
                SendReq sendReq = new SendReq();
                EncodedStringValue n = new EncodedStringValue(DestinationNumber);
                EncodedStringValue[] send = { n };
                sendReq.setTo(send);

                PduBody pduBody = new PduBody();

                // Add text message data to message
                PduPart txtPart = new PduPart();
                // Encoding.ASCII.GetBytes
                txtPart.setData((msg.getBytes(StandardCharsets.US_ASCII)));
                txtPart.setContentType(new EncodedStringValue("text/plan").getTextString());
                txtPart.setName(new EncodedStringValue("Message").getTextString());
                pduBody.addPart(txtPart);

                // Add image data 
              
                PduPart imgPart = new PduPart();
                byte[] sampleImageData = Files.readAllBytes(Paths.get(filePath));
                
                imgPart.setData(sampleImageData);
                imgPart.setContentType(new EncodedStringValue("image/jpg").getTextString());
                imgPart.setFilename((Paths.get(filePath).getFileName()).toString().getBytes());
                pduBody.addPart(imgPart);

                // Now create body of MMS                
                sendReq.setBody(pduBody);
              
                // Finally, generate the byte array to send to the MMS provider
                PduComposer composer = new PduComposer(context, sendReq);
                //PduComposer composer = new PduComposer(activity.getApplicationContext(), sendReq);
              
                pduData = composer.make();
              
            }
            catch(Exception ex)
            {
                // TODO: Do something here
              result.error("Failed", "GetMMSPDUData failure be like:" + ex.toString(), "");
              return pduData;
            }
            return pduData;
        }

        private void SendMMSData(byte[] PDUData, Integer simSlot, SmsManager sm, Result result)
        {
          Log.i("manual nag:" ,"scott says pdudata in sendmssdata length is: " + PDUData.length);
          //context = activity.getApplicationContext();         
          //SmsManager sm = SmsManager.getSmsManagerForSubscriptionId(simSlot);
            try
            {
              //String cacheFilePath = Paths.get(context.getCacheDir().toString(), "send." + "sendMe" + ".dat").toString();
              String cacheFilePath = Paths.get(context.getCacheDir().getAbsolutePath().toString(), "/", "send." + "sendMe" + ".dat").toString();
              //result.error("Failed", "SendMMSData cacheFilePath bes: " + cacheFilePath, "");      
              
              Files.write(Paths.get(cacheFilePath), PDUData);
              File testFile = new File(cacheFilePath);
              Log.i("manual nag:" ,"scott says testfile is this big in sendmssdata: " + testFile.length());
              //byte[] byteArray = Files.readAllBytes(Paths.get(cacheFilePath));              

                //String authString = context.PackageName + ".fileprovider";
              // this is dumb and bad, try reading and catch
              if(Files.exists(Paths.get(cacheFilePath)))
                {                   
                  PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context.getPackageName() + ".WAP_PUSH_DELIVER"), PendingIntent.FLAG_MUTABLE); 
              //result.error("Failed", "SendMMSData failure be like:" + FileProvider.getUriForFile(context, context.getPackageName().toString() + ".fileprovider", testFile), "");   
                  //sm.sendMultimediaMessage(context, FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", testFile), null, null, pendingIntent);   
                  Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", testFile);                 
                  //File taway = new File(uri.getPath());
                  Log.i("manual nag:" ,"scott says uri is: " + uri);
                  //Log.i("manual nag:" ,"scott says df is a file: " + taway.exists());
                  Log.i("manual nag:" ,"scott says uri.getPath() is: " + uri.getPath());
                  sm.sendMultimediaMessage(context, uri, null, null, pendingIntent);                  
                }              
            }
            catch(Exception ex)
            {
              result.error("Failed", "SendMMSData failure be like:" + ex.toString(), "");                
            }
            
        }

  
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}
