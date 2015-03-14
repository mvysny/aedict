# FAQ #

### Q: Aedict 3 does not work on Galaxy Tab 3 10.1 properly ###

A: As Marko Pareigis found out, Java String compareTo() function is borked on Galaxy Tab 3: "当".compareTo("本") must return value less than 0, but it returns 1 instead. This is a bug in Samsung's Android which I cannot fix nor work-around. I have written mail to Samsung about this issue and you can put some pressure on the guys at Samsung to fix this issue. See http://code.google.com/p/aedict/issues/detail?id=290 for details.

This issue only applies to the 10.1 Tab3: users of 7" and 8" Tab tablets have reported that Aedict works correctly for them.

### Q: Aedict 3 crashes during screen rotation ###

A: Yes, it happens. It happens because Android is buggy as hell and I am tired of walking around countless bugs with Fragments and Child Fragments and their random interaction during screen rotation and whatnot.

### Q: Aedict 3 returning less results than Aedict 2, missing the Exact / Ends With / Substring search options ###

A: In the "Word Search" activity, please click the upper right "wrench" icon which will allow you to configure the search options.
The Sense part matches the non-JP part:  english, french, dutch, etc. May have the following values: Exact - exact word match, e.g. "auto" will match "auto mechanic" but not "automechanic"; StartsWith? - "car" will match "crane carrier" but not "ecar"; EndsWith? and Substring are unsupported because of Lucene limitations

### Q: Aedict 3 crashes right after start ###

A: Aedict 3.4.10 crashes right away if the SDCard is not writable or is not inserted in the phone (this only applies to phones with SDCard slot - other phones pretend SD Card by providing internal memory space). Aedict requires that the "SD Card" (either the real one or the mapped internal memory) is writable because it stores the user data there.

Also, Aedict tend to crash if the dictionary files became somehow corrupted on the SD Card. Please try to delete all dictionary files and download them again.

### Q: What is meant by the User Data? ###

A:  Notepad, Tags, Recently Viewed entries and SRS statistics.

### Q: Kanji search by parts gives complete "preview" results but incomplete main results ###

A: Also see [bug #276](https://code.google.com/p/aedict/issues/detail?id=276) - please update the dictionaries to get rid of this problem.

### Q: BlackBerry version? ###

A: Available, please follow this link here: http://appworld.blackberry.com/webstore/content/48697890

Demonstration video: http://www.youtube.com/watch?v=_55s7loK1G8

### Q: Dropbox integration? ###

A: The Dropbox integration is present since 3.4 and should work correctly in cases when the device you are making changes on is online. If you are interested in gory details or how to behave while offline, please read on.

PRIVACY STATEMENT: Aedict is allowed by Dropbox to only modify files stored in the Apps/Aedict/ folder. It simply cannot and will not access any other files in any way - it will not read them, write them nor send them anywhere. Also, Aedict respects privacy of users and will not send  nor upload your data anywhere besides the Apps/Aedict/ Dropbox folder.

When the files are changed, Aedict uploads the user data to the Dropbox account to the Apps/Aedict/ folder. When Dropbox reports that these files have been changed on the server, Aedict takes them and OVERWRITES local files (Yes it should have been MERGE and that requires merging tool, which is not yet complete). This strategy is simply called "server wins". This works fine for most cases, e.g. when the changes are being made on one device only, which is (at some point) connected to the network at least briefly. However, if two devices are offline and both will change e.g. notepad, only one device wins - upon synchronization, one device succeeds in uploading the data to the server, while the other devices' changes are simply overwritten. Therefore, a WARNING: DROPBOX INTEGRATION IS CURRENTLY NOT PERFECT AND YOU MAY LOSE DATA IF NOT CAREFUL.

WARNING: when enabling Dropbox in Aedict for the first time, Aedict will upload all files to the server only if they are missing on the server. If the file is present on the server, the "server wins" and will OVERWRITE local Aedict files. Just keep in mind that when enabling Dropbox integration, enable it first on a device with most complete user data.

### Q: TTS, Text-To-Speech, Japanese reading does not work ###

A: Note that the "Pico TTS" which is the default Android engine does not support Japanese reading and will not work. You may switch to the Google TTS Engine (only present in newer Androids), which does support Japanese reading, but only when online. You may also buy commercial Japanese readers such as SVOX or DTalker which are able to synthesize Japanese speech offline. Please note that the speech is synthesized and may not be accurate in some corner cases.

### Q: How to write づ in romaji? ###

A: Use xzu to write づ, use xna to write んあ, use xnya, nxya or n'ya to write んや. For all possible writings please see the "Kana Table" screen in Aedict.

### Q: What does the "Sort results" option do? ###

A: Please see [Issue 56](https://code.google.com/p/aedict/issues/detail?id=56) for discussion on this topic. In short, the built-in sorting algorithm does not work well for some dictionaries. To force a different ordering you'll need to turn the sorting of the results off, to use Lucene ordering. Lucene orders results by the Boost value - the higher the Boost value is, the higher the record will appear in the result list. You'll then have to patch the Indexer, to assign the Boost value upon dictionary indexing.

### Q: SRS/Leitner? ###

A: Present since Aedict 3.4.13. When enabled in the configuration, Aedict tracks quiz answers for particular kanjis. When next quiz is launched, incorrectly answered kanjis are shown more often than correctly answered kanjis.

Aedict uses the http://en.wikipedia.org/wiki/Leitner_system and puts the kanjis into boxes numbered 1-5. Kanjis from box 1 have a chance of being present in the quiz five times higher than kanjis from box 5.

### Q: How to search for multi-kanji words? ###

A: Aedict 3 way: You can draw up multiple kanji before searching, and then search for words containing given kanjis only. This it not limited to kanji draw - this technique can be also used for SKIP search, Parts search, etc. This is called "Kanji Scratchpad" and you can add Kanjis to Scratchpad from the Kanji Detail page, regardless of method used to find the kanji.
Just click on a kanji to show kanji details and click on the upper-right button (Add Kanji To ScratchPad). You can add multiple kanjis to the scratchpad (it is shown in the navigation menu). When clicking on scratchpad, it will find entries which contain all the kanjis.

This can be done even quicker with kanjipad: just rotate your phone landscape, and the kanjipad should split into two parts: the drawing part and the search result part. Try to draw a kanji and search for kanjis; then, just long-click on particular kanji and select "Add Kanji To ScratchPad" button, present in the upper-right menu.

Please see the following short video for details: https://www.youtube.com/watch?v=cmVZYlexhvU

### Q: Hispadic? ###

Aedict 3 uses JMDict which has Hispadic integrated by default, therefore it is present by default. Open the Configuration and set the dictionary language to Spanish and it should work, e.g. search for "madre". Please note that if an entry does not have Spanish translation, English translation is displayed instead.

### Q: Kanjipad does not recognize kanjis well ###

A: Kanjipad expects correct stroke order and direction, and most importantly, it expects correct stroke count. The analyzer algorithm was stolen (do you know that great artists steal? :-) from Todd David Rudick.

You can configure Aedict to use the external [KanjiDraw](https://play.google.com/store/apps/details?id=com.leafdigital.kanji.android) application, which uses different matching algorithm which matches kanjis regardless of the drawing/stroke order. Simply open the Aedict configuration and set the "Kanji Pad" configuration option to KanjiDraw; Aedict will install KanjiDraw automatically. Then, activate the "Inexact mode" in the KanjiDraw app which should match the kanji regardless of the drawing/stroke order. Then, when your kanji is found, just press the "Copy And Close" button.

### Q: EOFException during dictionary download ###

A: EOFException tends to happen when the download gets interrupted because of network issues such as connectivity loss and timeouts. Please try to download the dictionaries once again, perhaps on a WIFI with strong signal and fast download.

### Q: Please add automatic check to kanji draw quiz - correct if search results contains asked kanji ###

A: In Kanji Quiz, the student wants to learn the proper way to draw a character. The kanji matcher algorithm must not only find the matching kanji, but it also must verify the stroke order, correct stroke placement, etc. This matching mode is known as "verification" mode.

There is also another mode, a "search" mode - in this mode the engine tries to find as many kanjis as possible, but the drawing is not required to match exactly: in a lot of cases the stroke order may not fit, in several cases it may also match kanjis which are completely dissimilar to the drawing.

If the "search" mode would be used in kanji draw quiz to accept the kanji, the quiz would accept the kanji even when the kanji is not drawn correctly or is completely dissimilar. This would trick the student to false feeling that he/she has drawn the kanji properly, which is wrong. Therefore, it is important that the "verification" mode is used.

Unfortunately, the engine used in Aedict 3 only support the "search" mode and it cannot be used in verification mode, therefore it cannot be used in the kanji draw quiz.

When the "verification" mode will be added? Sorry, this is quite complicated stuff and it will not be implemented in the foreseeable future. Perhaps I can use some Kanji recognizer with commercial-compatible license...

### Q: Copy+Paste in Aedict 3 ###

A: Regarding CopyPaste **from** Aedict3: Both Copy&Paste and "Add to Notepad" are available in Aedict 3 in any list view which shows dictionary entries. Just enter the selection mode by long-clicking any item, select a couple of items and then press "Copy/Paste" or "Add to Notepad" upper-right ActionBar icons.

Regarding CopyPaste **to** Aedict3: there are two ways to do this. One way works with programs which supports the "Share" functionality, such as the Chrome Browser. In such programs, just select a portion of text, activate the "Share" menu and select Aedict - the text will automatically be searched and/or analyzed. Other way to do this is to select text, copy it into the clipboard, then activate Aedict and paste the text into the omnisearch box. Note that you can add permanent notification icon for Aedict and thus make it always accessible, by activating the appropriate option in the Aedict configuration.

### Q: Romaji support? ###

A: Available, just click the Action Bar button. Or enable it in the Configuration.

### Q: Launch quiz for an entire notepad category? ###

A: You can long-click on a category item, then select all items, then launch quiz from selected items. This way you can launch quiz from an entire category.

To launch quiz from entire notepad, just click on the question mark icon in the Notepad screen action bar (upper-right section of the screen).

### Q: The dictionary files are not downloaded automatically, or the download fails, freezes etc. ###

A: THIS ONLY APPLIES TO AEDICT 2: As a workaround, you can download the dictionary files manually. Attach your phone to the computer using an USB cable - your SDCard will get mounted to let's say drive F: . The dictionary files are located as follows:

| **Name** | **Location on your SDCard** | **Download link** |
|:---------|:----------------------------|:------------------|
| Edict | `F:\aedict\index\` | http://www.baka.sk/aedict/dictionaries/edict-lucene.zip |
| Kanjidic | `F:\aedict\index-kanjidic\` | http://www.baka.sk/aedict/dictionaries/kanjidic-lucene.zip  |
| Example sentences | `F:\aedict\index-tanaka\` | http://www.baka.sk/aedict/dictionaries/tanaka-lucene.zip |
| Example sentences | `F:\aedict\index-tatoeba\` | http://www.baka.sk/aedict/dictionaries/tatoeba-lucene.zip |
| SOD Kanji images | `F:\aedict\sod\` | http://www.baka.sk/aedict/dictionaries/sod.dat.gz |

You'll need to download the zip file and unpack it to the abovementioned directory. If the directory does not exist, just create it. NOTE: You MUST use lower-case letters as Android file-system is case-sensitive: for example `F:\AEDICT\Index-Kanjidic` WON'T WORK.
After the files are unpacked, there should be 3 files
being unpacked directly to the directory, e.g. `F:\aedict\index\_0.cfs`.
The SOD images is a bit different: after unpacking the file, you will receive a single file sod.dat, which should be placed at `F:\aedict\sod\`

Note: There is a bug: if you download dictionaries this way, Aedict will insist on updating the dictionaries, even though they are correct. The workaround is to choose "NO" in the update screen.

### Q: I want a Home shortcut directly to Kanjipad ###

This is available since Aedict 2.0. Please install and run [AnyCut](https://play.google.com/store/apps/details?id=com.spring.bird.anycut&hl=en), click on New shortcut, Activity and select the activity:

| **Name** | **Description** |
|:---------|:----------------|
| Aedict | The main activity, launched when Aedict is started normally |
| Kanjipad | The Kanjipad |
| Skip Lookup | Lookup kanjis using the SKIP method |
| JLPT Quiz | Launches the JLPT quiz |
| Kana table | Shows the hiragana/katakana table |

### Q: Using Aedict on PC? ###

Disadvantages: no copy-paste between Aedict and PC.

Please read [this  great tutorial](http://shinobukaneko.blogspot.com/2010/12/running-aedict-in-desktop.html) by Kuppusamy-san.

Or, even better, you can install Android directly on your PC, or into a virtual machine: https://www.youtube.com/watch?v=TwwH6xBn4mg (download the Android ISO here: http://www.android-x86.org/releases/releasenote-4-4-r1 ) and then just install Aedict 3 via Google Play. Note that currently Aedict3 Dropbox integration will not work - let me know if you wish to enable this integration.

### Q: adding a comment option/possibility for users to add to each kanji? ###

A: You can tag any dictionary entry with a color and a text - just long-click any kanji or any entry in any list, select one or more kanjis and select "Tag" from the menu. The tag text and color will then be displayed right below the entry in the list.

### Q: Aedict 3 is LARGE ###

A: Yes. Aedict 3 takes quite a lot of space when installed. This is because of linked libraries:

  * 1 mb Japanese font
  * 1,5 mb Dropbox library
  * 1,5 mb compatibility libraries for Android 2.1 compatibility
  * 1,5 mb Lucene library
  * 1,5 mb the Aedict 3 codebase, images, resources and other stuff.

### Q: Some kanjis have no translation ###

A: Yes, for example 夨 has no translation present in the KanjiDict XML file.

### Q: Custom export format ###

A: Since Aedict 3.4.6 you can customize your own custom export format. Each entry is exported to a single line with user-defined formatting, for example:
```
"%kanji%"\t"%kanji% [%reading%]"\t%sense%\t%examples%
```
produces the following output:
```
"母"<tab>"母 [はは, はわ, かか, おも, いろは, あも]"<tab>mother<tab>母ちゃんも同じ事いってたな。だからどうしたってんだよ。オレにゃ関係ない。 - romaji - Mum said the same thing. But, so what? It's got nothing to do with me.
```
This custom formatting is available by selecting entries (by long-click on any dictionary entry list) and selecting "Export as Anki-compatible CSV". You can also export an entire Notepad using the custom formatting, by opening the Notepad activity, selecting "Export Notepad as CSV" and choosing the "Custom" option.

**Tip**: You can export the notepad category-by-category, by long-clicking an item, selecting all items (there is button for that :) and selecting "Export as Anki-compatible CSV".

The following items are allowed:

  * \t - replaced by the TAB character
  * %kanji% - replaced by the japanese form, may contain kanji, hiragana or katakana. May equal to %reading% for hiragana/katakana-only entries. Never empty. If multiple kanjis are present, they are comma-separated. For example 日の出
  * %reading% - entry reading in hiragana or katakana, never empty. If multiple readings are present, they are comma-separated. Example: ひので
  * %sense% - English (or other lang) translation, for example "sunrise"
  * %examples% - At most three example sentences containing %kanji%, separated by a configurable string, in the form of KANJI1 - READING1 - SENSE1 - KANJI2 - READING2 - ... (if the separator is  - ). Note that this item greatly slows the export process.
  * %strokes% - Stroke count number, 1..30. Empty if the entry does not originate from KanjiDic.
  * %skip% - The skip code, e.g. 4-1-4. Empty if the entry does not originate from KanjiDic.
  * %radical% - The kanji radical character. Empty if the entry does not originate from KanjiDic.
  * %grade% - The kanji grade level. 1 through 6 indicates a Kyouiku kanji and the grade in which the kanji is taught in Japanese schools. 8 indicates it is one of the remaining Jouyou Kanji to be learned in junior high school, and 9 or 10 indicates it is a Jinmeiyou (for use in names) kanji. Empty if the entry does not originate from KanjiDic.
  * %parts% - The kanji parts. Empty if the entry does not originate from KanjiDic.
  * %grammar\_codes% (since Aedict 3.4.9): the grammar code string, such as "(P) common, (n) noun (common) (futsuumeishi)"
  * %furigana% (since Aedict 3.4.9): the ruby furigana, in the form of `kanji[reading]`; if there are multiple kanjis with multiple readings, this is a space-separated list of ruby furigana. If the entry has no kanji, this is just the `reading`.
  * %notepad\_category% (since Aedict 3.4.32): if the entry is stored in the notepad, this will be replaced by the category name of the first category this entry appears in. If not, this will be an empty string.
  * %tag% (since Aedict 3.4.34): replaced by the text the entry is tagged with. If the entry has no tag, this will be an empty string.

### Q: Aedict downloads dictionaries to the internal memory ###

A: On some phones an incorrect path to the SDCard is returned, which points to the internal memory instead. It is not yet clear on how to detect SDCard properly on all phones, see http://stackoverflow.com/questions/5694933/find-an-external-sd-card-location . As a workaround, Aedict 3.4.6 now allows you to override this detection mechanism and specify the path yourself. To do that, enter Aedict configuration and click on Dictionary Manager, then delete all dictionaries. Then exit Aedict 3 and start it again - the Welcome Screen should be now shown. press the Expert Settings checkbox and fill in the correct path. You may use a file manager such as ES File Manager to find the correct path to the SD Card.

Take care though, on some phones the SD Card may not be writable. See the next question for details.

### Q: After upgrading Android, Aedict cannot update dictionaries and says that SD Card is read-only ###

A: Google is trying to get rid of the anyone-can-access SD Card, for security reasons (for example ransomware). This manifests on some phones as disabling write access to the SD Card. See http://www.androidpolice.com/2014/02/17/external-blues-google-has-brought-big-changes-to-sd-cards-in-kitkat-and-even-samsung-may-be-implementing-them/ for more information. Some vendors ignore this (including Cyanogen), some vendors are however incorporating this change into their OSes. So, it is possible (for example on Sony Xperia Z1 after upgrading to Android 4.4.2) that the sd card becomes read-only for Aedict, which will no longer be able to write new dictionary files to the SD Card.

If this is your case, unfortunately you have to persuade Aedict to move the dictionary files into the internal storage. To do that, please follow these steps:

  1. power down your phone and take off the sd card
  1. Insert the sd card into your computer, find the aedict3/dictionaries directory and delete it.
  1. Insert the sd card back into your computer.
  1. Start Aedict 3. The welcome screen should pop up and the dictionaries should be downloaded into the internal memory.


### Q: How to import notepad from Aedict 2 ###

A: Open Aedict 2 and choose "Notepad Backup". Then you can start the Notepad in Aedict 3 and select the "Import from Aedict 2 Backup" Action Bar menu item.

If Aedict 3 fails with "Aedict 2.x Notepad BACKUP file couldn't be found", please check that a file named `/sdcard/aedict/notepad.backup` should be created (you can use a file manager such as ES File Manager to verify the file presence).

If this method fails, please try to follow this tutorial: https://code.google.com/p/aedict/wiki/FAQ#Q:_What_does_'Read_from_Preferences.xml_value'_mean?

### Q: Backup of Aedict 3 Notepad ###

A: Please make sure that your Dropbox directory does not contain the `Apps/Aedict` directory; if it does, delete all `*`.bin files. This is very important - if they already exist, they will overwrite Aedict 3 data on your phone. After the files are gone, enable Dropbox integration in Aedict 3. After you modify the user data, changes are automatically propagated to Dropbox and to all linked devices. You can then link your Dropbox account to your PC and back up the bin files by copying them somewhere. To restore from a backup, just start up your PC and copy the files back to the `Dropbox/Apps/Aedict` directory.

Aedict 3.4.7 also allows notepad/tags/recent local backup, to the SD Card. You can have at most 10 backups, which are identified by the date when the back up has been created. The backup functionality is accessible from the Configuration screen.

### Q: Aedict 3 log ###

A: Aedict 3 logs what it is doing, to a standard Android log. If Aedict 3 does not work properly or keeps crashing, please drop me an email. During our conversation, I may ask for a log produced by Aedict 3. Please follow the following text to deliver the log to me :)

You can retrieve Android system log (which includes Aedict 3 logging messages) by following these steps:

  * Download Android SDK here: http://developer.android.com/sdk/index.html#download the "GET THE SDK FOR AN EXISTING IDE" link.
  * Enable debugging mode on your phone: http://www.wugfresh.com/faq/6/ or google for "android enable debug mode"
  * Connect your phone via the USB cable with your computer
  * Start the device monitor: http://developer.android.com/tools/help/monitor.html
  * Switch to the DDMS perspective: http://developer.android.com/tools/debugging/ddms.html - the logcat window is located at the bottom of the screen.

Alternatively, you can download CatLog: https://play.google.com/store/apps/details?id=com.nolanlawson.logcat
This program allows to browse your phone log, however it requires the phone to be rooted on modern phones, so it's no longer an option.

Caution - the log may contain sensitive information such as phone numbers you have dialed etc.
You should filter the log to only contain "sk.baka.aedict3" lines (these are lines produced by Aedict 3 logging). You can then send me these lines to my mail, or even better - you can send this file to yourself (via Mail, but also via Dropbox, or perhaps Google Drive), and check for any sensitive information. You can then edit the log and send it to me. This should help me knowing what is going on :)

The most important part is the crash information itself, or an exception stack-trace as it is called. It looks like this:

```
08-09 07:28:56.193  11090-11090/sk.baka.aedict3 E/AndroidRuntime﹕ FATAL EXCEPTION: main
    Process: sk.baka.aedict3, PID: 11090
    java.lang.RuntimeException: Unable to start activity ComponentInfo{sk.baka.aedict3/sk.baka.aedict3.MainActivity}: java.lang.RuntimeException: Greetings :-D
            at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2216)
            at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2265)
            at android.app.ActivityThread.access$800(ActivityThread.java:145)
            at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1206)
            at android.os.Handler.dispatchMessage(Handler.java:102)
            at android.os.Looper.loop(Looper.java:136)
            at android.app.ActivityThread.main(ActivityThread.java:5140)
            at java.lang.reflect.Method.invokeNative(Native Method)
            at java.lang.reflect.Method.invoke(Method.java:515)
            at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:795)
            at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:611)
            at dalvik.system.NativeStart.main(Native Method)
     Caused by: java.lang.RuntimeException: Greetings :-D
            at sk.baka.aedict3.MainActivity.onCreate(MainActivity.java:87)
            at android.app.Activity.performCreate(Activity.java:5231)
            at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1087)
            at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2170)
            at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2265)
            at android.app.ActivityThread.access$800(ActivityThread.java:145)
            at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1206)
            at android.os.Handler.dispatchMessage(Handler.java:102)
            at android.os.Looper.loop(Looper.java:136)
            at android.app.ActivityThread.main(ActivityThread.java:5140)
            at java.lang.reflect.Method.invokeNative(Native Method)
            at java.lang.reflect.Method.invoke(Method.java:515)
            at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:795)
            at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:611)
            at dalvik.system.NativeStart.main(Native Method)
```

### Q: Search by Parts / Radicals: what does the Stroke Count mean? ###

A: Sometimes when you search by kanji radical or kanji parts, there is too many results to see. You can then limit the results by searching kanjis with given stroke count only, by counting strokes in the target kanji character and setting the count in the stroke count field. However, a beginner may count strokes incorrectly and he/she may receive no results. Therefore, you can make some room for incorrect counts by specifying Stroke Count +- to 1 or 2. For example if you have counted kanji strokes to be 7 strokes, you can then search for 7 strokes +- 1, which will find kanjis with 6, 7 or 8 strokes.

### Q: What does 'Read from Preferences.xml value' mean? ###

A: Sometimes Aedict 2 backs up an empty notepad, which is a bug in Aedict 2. As a work-around, browse your phone internal memory and find a file named `/data/data/sk.baka.aedict/shared_prefs/sk.baka.aedict_preferences.xml`. Open it and find the row containing the `notepadItems2` text. Copy this XML element text contents - the text should contain the notepad items separated by four # and @ characters. Paste the text to the Aedict 3 import dialog, which should import the notepad contents into Aedict 3. See https://code.google.com/p/aedict/issues/detail?id=268 for more information. Also see https://groups.google.com/forum/#!topic/aedict-users/xX0lhfxDBgs for more information.

### Q: Mass export to Anki/AnkiDroid? ###

A: Mass export to the Desktop Anki is possible, via CSV export. You can define in Aedict 3 configuration what information is exported. You can also define a completely custom export file format, which does not even have to be a CSV. This way you can export to e.g. Excel or other software.

Regarding AnkiDroid: In Aedict 3.4.8 and older, only a basic export functionality is available, which can only export one entry at a time. Support for mass export to AnkiDroid has been added in Aedict 3.4.9 - you will need to activate it in the configuration. Note that this stuff is new and may not yet be implemented in AnkiDroid - please wait until a new AnkiDroid version is released. TODO add a manual on how to create a custom note type in AnkiDroid in order to import from Aedict 3 efficiently.

### Q: Heisig support ###

A: Heisig quiz was added in Aedict 3.4.12, please update the app to gain access to the quiz. Heisig kanji information is available in the newest dictionaries - please update the dictionaries to gain access to this information.

The Heisig Index numbers are valid for Heisig 3rd edition. Since 3.4.18, Aedict also supports the 6th edition numbers - please update the dictionaries and select the Edition number in Aedict configuration.

### Q: Kotowaza ###

A: Available since Aedict 3.4.13 - just download the Kotowaza dictionary and it will be automatically shown in the "Examples sentences" tab (with high priority - first matching kotowaza entries are shown, then Tatoeba example sentences are shown). You can also search for kotowaza in the main search screen, by forcing search in example sentences (click the upper wrench icon and then select "Examples" search).

### Q: Why are some JMDict kana underlined? ###

A: Underlined kanjis/reading marks the common kanji/reading for that entry.

### Q: Why is the soft keyboard not shown? ###

A: When the main window is displayed and the cursor is inside the search text box, the keyboard is not initially shown. This is to let you see the full list of previously searched entries, and/or live search results.

You can force Aedict to show the soft keyboard, by opening the Aedict configuration and making sure that the "Omnisearch: Show Soft Input" configuration option in the "GUI" section is checked.

### Q: Sentence analysis? ###

A: Aedict supports japanese sentence analysis, or sentence breakdown. Just paste any japanese sentence to the search box and make sure that the "Dict" mode is selected (just click the upper-right "wrench" icon and select "Search In":"Dict"). It will take some time but Aedict will eventually break down the sentence into individual words.

### Q: Search in Example Sentences ###

A: Please open the main search screen and click the upper-right "cog wheel" icon - this should open the Advanced Search Settings view. Then, select "Search In":"Examples". You can now enter any text into the search box and example search will be performed automatically.

### Q: Wadoku or German support? ###

A: Jim Breen personally merges Wadoku entries into JMDict: https://groups.yahoo.com/neo/groups/edict-jmdict/conversations/topics/2241
Aedict3 contains all language mutations included in JMDict, therefore to show Wadoku German terms, just enter Aedict3 configuration, the "dictionaries" section and set the "language" option to german. This wadoku dictionary is a bit older.

If you wish to use newest Wadoku dictionary, head to Aedict 3 Settings, Dictionaries and download the Wadoku dictionary. Do not forget do activate the dictionary itself, in the "Custom Dictionary" settings item (or in the Main Activity, in the screwdriver menu).

### Q: Soft keyboard is not hidden/shown properly ###

A: I spent two hours cursing over the Android soft keyboard API which is broken horribly. I got it to a state when it mostly works. I tried to fix it to "always working" but I always ended up by "seldom works". Just an example of how horribly broken the API is: showSoftInput does not work, toggleSoftInput mostly works but it hides the soft input if it is visible; checking for the soft input visibility by calling isActive() does not work because it returns false even when the soft keyboard is visible...

In short, soft keyboard in Aedict mostly works and can not be fixed until Google fixes the soft keyboard API.

### Q: How many Kanjis does Aedict 3 contain? ###

A: Aedict3 contains the entire Kanjidic2 from Jim Breen (I index and upload kanjidic update for Aedict3 once per month, so it is always reasonably up-to-date). Currently (as of Nov 6th, 2014), Kanjidic2 contains 13108 kanjis.