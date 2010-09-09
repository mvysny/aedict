Releasing:

- Run all tests, both on 2.1 and 1.5.

- update changes.xml and strings.xml/whatsNewText

- mvn release:prepare release:perform

- upload the target/checkout/aedict-apk/target/aedict-apk-*-signed.apk to the Google Downloads page: http://code.google.com/p/aedict/downloads/list

- upload the target/checkout/aedict-apk/target/aedict-apk-*-signed.apk to the Android Market

- upload the target/checkout/aedict-apk/target/aedict-apk-*-signed.apk to http://appslib.com

- upload the target/checkout/aedict-apk/target/aedict-apk-*-signed.apk to http://handster.com/administrator/

- Increase the version in AndroidManifest.xml: both android:versionCode and android:versionName

- Fix the aedict-common dependency in the aedict-apk/.classpath file

