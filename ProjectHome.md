# Aedict #

A free, open-source english-japanese dictionary for Android which uses Jim Breen's edict data. Does not require japanese keyboard. Works offline. The dictionary data is downloaded automatically.

**FOR AEDICT 3 PLEASE SEE THIS PAGE: [AEDICT3](http://www.aedict.eu/)**

**FOR AEDICT 3 OCR MODULE PLEASE SEE THIS PAGE: [AEDICT3OCR](AEDICT3OCR.md)**

**IMPORTANT**: Importing old Notepad contents from Aedict 2 to Aedict 3 is now fully supported.

**IMPORTANT**: Dear users, I am very sorry that no updates have been published for a long time. Unfortunately, I have a family now and I can no longer afford to develop Aedict for free. I will keep Aedict 2.9 available forever. I am currently preparing a new version of Aedict, with new ActionBar-based GUI (compatible back to Android 2.1), using regularly updated JMdict, Kanjidic2 and Tatoeba as its source, for a price of $5 (one beer). I know this is disappointing and I am sorry, but the only other option I had was to abandon Aedict entirely.

## News ##

News on twitter: http://twitter.com/aedict

Mailing list: [Aedict Users](http://groups.google.com/group/aedict-users/topics)

Change log: [ChangeLog](ChangeLog.md) [Old ChangeLog](http://baka.sk/aedict/changes-report.html)

For programmers trying to access Aedict: [API](API.md)

## Features ##
  * simple user interface, tested on HTC Magic. No settings, just type a word and do a search
  * Automatic download of the indexed EDict/Kanjidic/Tanaka dictionaries (Warning: the dictionaries files are quite large, e.g. English EDict zip file is 8mb long, which may take some time to download. Perform this download over a Wi-Fi or with a quick internet access if possible)
  * Allows automatic download of a German/French EDict, the names dictionary and the computer terminology dictionary
  * Once the dictionary is downloaded it works completely off-line
  * Kanjipad for drawing and searching kanji characters
  * Search kanji characters by radicals and stroke numbers
  * Shows a kanji analysis in a word
  * [SKIP](http://www.basic-japanese.com/Hilfsdateien/skipCode.html) search (a search based on the kanji visual look)
  * [Custom dictionaries](CustomEdictFile.md)
  * Optionally shows romaji instead of hiragana/katakana - good for beginner learners. Also provides a hiragana/katakana learning table.
  * Search in Tanaka Example sentences
  * Optional verb deinflection (e.g. searching for あえない will find あう)
  * Kanji drawing order for more than 1500 kanjis
  * A basic sentence translation
  * Integration with Simeji
  * Verb conjugations with example sentences
  * Integrates with Android Search (Android 1.6 or higher only)

Aedict requires Android 1.5 or higher.

## Screenshots ##

| ![http://baka.sk/aedict/images/screenshot1.png](http://baka.sk/aedict/images/screenshot1.png) | ![http://baka.sk/aedict/images/screenshot2.png](http://baka.sk/aedict/images/screenshot2.png) |
|:----------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------|

## Download ##

Available at [Google Play](https://play.google.com/store/apps/details?id=sk.baka.aedict&hl=en). OR: Just search for "aedict" or browse the Travel category. To download the Indexer please follow the [Downloads](http://code.google.com/p/aedict/downloads/list) link.

## Tips ##

  * To access various parts of Aedict quickly, use AnyCut to create shortcuts (e.g. to Kanjipad), see [FAQ](FAQ.md) for details
  * You can use notepad as an advanced clipboard: just create a category named 'temp' and copy items there.
  * To access the verb conjugation quiz, just long-click on a verb, either when in the main screen, notepad, or search results.

## FAQ ##

Please find the [FAQ](FAQ.md) page here. More documentation [here](http://code.google.com/p/aedict/w/list).

## Acknowledgements ##
Aedict uses:
  * Jim Breen's excellent [edict dictionary](http://www.csse.monash.edu.au/~jwb/japanese.html)
  * Apache Lucene for quick search
  * [Maven+Masa to build](Compile.md)
  * Kanji Recognizer (C) by Todd David Rudick, http://www.cs.arizona.edu/projects/japan/JavaDict/
  * SKIP code system (C) by Jack Halpern
  * [Stroke Order Diagram Editor-Retrographer (SODER)](http://www.kanjicafe.com/using_soder.htm) (C) by James Rose
  * [Conjugation chart with example sentences](http://www.timwerx.net/language/jpverbs/index.htm) (C) by Tim R. Matheson