# Introduction #

If Aedict should crash (you'll see the standard "The application Aedict (process
sk.baka.aedict)has stopped unexpectedly. Please try again." dialog), it would be very helpful to know, where exactly it crashed - a stacktrace.

## Getting Stacktrace ##

### The easy way ###
(Thanks to Nicolas Raoul for finding this out)

Download the Log Collector application from the Android Market. It will allow you to send the log (which contains stacktrace) via e-mail to your computer, which you can then easily attach to the bug report - just attach the entire log file, or only the interesting portions, as the log contains also sensitive information, such as phone numbers. Just search for lines containing `sk.baka.`, and attach these lines (along with a couple of lines above and below) to the bug report. Example of the stack trace:

```
java.lang.IllegalArgumentException: View not attached to window manager
  at android.view.WindowManagerImpl.findViewLocked(WindowManagerImpl.java:355)
  at android.view.WindowManagerImpl.removeView(WindowManagerImpl.java:200)
  at android.view.Window$LocalWindowManager.removeView(Window.java:432)
  at android.app.Dialog.dismissDialog(Dialog.java:280)
  at android.app.Dialog.access$000(Dialog.java:73)
  at android.app.Dialog$1.run(Dialog.java:109)
  at android.app.Dialog.dismiss(Dialog.java:264)
  at sk.baka.autils.DialogAsyncTask.onPostExecute(DialogAsyncTask.java:132)
  at android.os.AsyncTask.finish(AsyncTask.java:417)
  at android.os.AsyncTask.access$300(AsyncTask.java:127)
  at android.os.AsyncTask$InternalHandler.handleMessage(AsyncTask.java:429)
  at android.os.Handler.dispatchMessage(Handler.java:99)
  at android.os.Looper.loop(Looper.java:123)
  at android.app.ActivityThread.main(ActivityThread.java:4363)
  at java.lang.reflect.Method.invokeNative(Method.java:-2)
  at java.lang.reflect.Method.invoke(Method.java:521)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:860)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:618)
  at dalvik.system.NativeStart.main(NativeStart.java:-2)
```

Prior acquiring the log, please make Aedict crash once more, just to be sure that the stack trace is present in the log.

### The hard way ###

  * Download and install [Android SDK](http://developer.android.com/sdk/1.6_r1/index.html)
  * Enable debug mode on your phone: Settings / Applications / Development / USB Debugging
  * Connect your phone to the computer using the USB cable
  * Optional: Make the application crash again, just to be sure
  * On your computer, run:

```
adb logcat *:W >logcat.txt
```
and please include the `logcat.txt` file in your bug report. Thanks!