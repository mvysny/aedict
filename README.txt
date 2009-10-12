Releasing:

1. mvn release:prepare release:perform

2. upload the target/checkout/aedict-apk/target/aedict-apk-*-signed.apk to the Google Downloads page: http://code.google.com/p/aedict/downloads/list

3. upload the target/checkout/aedict-apk/target/aedict-apk-*-signed.apk to the Android Market

4. Increase the version in AndroidManifest.xml: both android:versionCode and android:versionName

