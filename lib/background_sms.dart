import 'dart:async';

import 'package:flutter/services.dart';

enum SmsStatus { sent, failed }
enum MmsStatus { sent, failed }

class BackgroundSms {
  static const MethodChannel _channel = const MethodChannel('background_sms');

  static Future<SmsStatus> sendSMS(
      {required String phoneNumber,
      required String message,
      int? simSlot}) async {
    try {
      String? result = await _channel.invokeMethod('sendSms', <String, dynamic>{
        "phone": phoneNumber,
        "msg": message,
        "simSlot": simSlot
      });
      return result == "Sent" ? SmsStatus.sent : SmsStatus.failed;
    } on PlatformException catch (e) {
      print(e.toString());
      return SmsStatus.failed;
    }
  }
    static Future<MmsStatus> sendMMS(
      {required String phoneNumber,
      required String message,
       required String filePath,
      int? simSlot}) async {
    try {
      String? result = await _channel.invokeMethod('sendMms', <String, dynamic>{
        "phone": phoneNumber,
        "msg": message,
        "filePath": filePath,
        "simSlot": simSlot          
      });
      return result == "Sent" ? MmsStatus.sent : MmsStatus.failed;
    } on PlatformException catch (e) {
      print(e.toString());
      return MmsStatus.failed;
    }
  }

  static Future<bool?> get isSupportCustomSim async {
    try {
      return await _channel.invokeMethod('isSupportMultiSim');
    } on PlatformException catch (e) {
      print(e.toString());
      return true;
    }
  }
}
