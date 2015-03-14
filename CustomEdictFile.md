**Important**: Only Aedict 2 currently supports custom dictionaries; Aedict 3 does not currently support custom dictionaries. However, Aedict 3 comes with a rich selection of additional dictionaries (13), so this feature is probably no longer necessary.

**Note**: English, German, French, the Japanese proper name, place names & surnames dictionary (ENAMDIC) and computer terminology dictionary (COMPDICT) dictionaries are now directly downloadable from Aedict. Just open the configuration window and click on the "Download additional dictionaries" button.

# Introduction #

To use a custom EDict file with Aedict you'll have to convert the EDict file to a Lucene Index file as required by Aedict. To do so, just [download](http://code.google.com/p/aedict/downloads/list) the aedict-indexer application, unzip it and run:
```
ai -f the_edict_file
```
Note that the Indexer requires Java JRE 5 or greater to be installed.

The Indexer will create a file named `edict-lucene.zip` on successful run. To get the dictionary to your phone just follow the following steps:

  1. Connect your phone as a mass storage device to your computer
  1. Browse the SDCard contents and find the "aedict" directory, in the root of the SD card. Create it if it is not present
  1. Create the aedict/index-_DictionaryName_/ directory (you can use any name except "kanjidic" and "tanaka")
  1. Unzip the `edict-lucene.zip` file to the aedict/index-_DictionaryName_/ directory.

That's it. Just fire up the Configuration screen and a new dictionary, _DictionaryName_ will be made available. Note that `index/` and `index-kanjidic/` directories are reserved and the `index-kanjidic/` directory will never show in the dictionary picker as it is not an Edict-formatted file.

## Troubleshooting ##

**The `org.apache.commons.cli.ParseException: At least one of -u, -d or -f switch must be specified` message is always shown on Windows**

Yes, I released it too hastily. My bad :) Please edit the ai.bat file, the 12th line and change
```
java %JAVA_OPTS% -classpath "%CLSPATH%" sk.baka.aedict.indexer.Main $1 $2 $3 $4 $5 $6 $7 $8 $9
```
to
```
java %JAVA_OPTS% -classpath "%CLSPATH%" sk.baka.aedict.indexer.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
```

**The following error message is shown: `org.apache.commons.cli.ParseException: Charset EUC_JP is not supported by JVM.`**

It seems that Java JRE does not support the EUC\_JP encoding. Please [download a Java SE Development Kit (JDK)](http://java.sun.com/javase/downloads/index.jsp) and install it.