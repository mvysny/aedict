# Changelog #

For newer changelog please see this page: http://www.aedict.eu/changelog.html

## 3.9 ##

Jan 4th, 2015

  * Added swipe support to some lists - just swipe the item to the right, to reveal quick actions (add to notepad, copy to clipboard, tag). [bug #368](https://code.google.com/p/aedict/issues/detail?id=368)
  * Example sentences with configured translation language are now shown first. [bug #397](https://code.google.com/p/aedict/issues/detail?id=397) [bug #381](https://code.google.com/p/aedict/issues/detail?id=381)
  * Advanced Search Settings are now shown in search result title. [bug #402](https://code.google.com/p/aedict/issues/detail?id=402)
  * Shows whether a sentence analysis was performed. [bug #402](https://code.google.com/p/aedict/issues/detail?id=402)
  * Kanji Search: shows waiting progress bar while the new kanji grid contents are recomputed. [bug #401](https://code.google.com/p/aedict/issues/detail?id=401)
  * Paid kanjipad now optionally matches kanjis with one or two strokes more or less. [bug #399](https://code.google.com/p/aedict/issues/detail?id=399)
  * Omnisearch itself now uses JP font :)
  * Kanji learning progress review: kanji sorting implemented. [bug #386](https://code.google.com/p/aedict/issues/detail?id=386)
  * Kanji learning progress review: filter is now remembered. [bug #405](https://code.google.com/p/aedict/issues/detail?id=405)
  * Entry Detail Activity: actionbar search is now remembered. [bug #404](https://code.google.com/p/aedict/issues/detail?id=404)
  * JLPT kanji draw quiz: Undo/Clear now works even when the answer is shown. [bug #406](https://code.google.com/p/aedict/issues/detail?id=406)

## 3.8 ##

Dec 18th, 2014

  * Optimized kanji draw / quiz review activity.
  * Fixed crash in kanji search landscape mode.

## 3.7 ##

Dec 18th, 2014

  * Dropped support for KanjiDraw, added support for Aedict KanjiPad Extended (paid)
  * Wadoku translations were not displayed if language was not set to German. [bug #384](https://code.google.com/p/aedict/issues/detail?id=384)
  * Added support for finding a kanji via OCR (paid).
  * Kanji Search: show details of all kanjis in the kanji grid. [bug #396](https://code.google.com/p/aedict/issues/detail?id=396)
  * Fixed threading issue which randomly caused incorrect search results.
  * Fixed: internal kanji pad analysis too slow. [bug #394](https://code.google.com/p/aedict/issues/detail?id=394)
  * Fixed: Kanji search fails when using kana only. [bug #383](https://code.google.com/p/aedict/issues/detail?id=383)
  * Clicking on kanji in Learning Progress Review shows the kanji details. [bug #386](https://code.google.com/p/aedict/issues/detail?id=386)
  * Removed scratchpad. [bug #389](https://code.google.com/p/aedict/issues/detail?id=389)
  * Learning Progress Review updates. [bug #386](https://code.google.com/p/aedict/issues/detail?id=386)

## 3.6 ##

Dec 7th, 2014

  * Fixed crash when tags with custom formatting are used. [bug #379](https://code.google.com/p/aedict/issues/detail?id=379)
  * Sentence analyzer now deinflects verbs and adjectives only.
  * The red floating navmenu button can now be hidden. [bug #382](https://code.google.com/p/aedict/issues/detail?id=382)

## 3.5 ##

Dec 5th, 2014

  * New experimental omnisearch bottom mode added; try it out, it should help to control Aedict with just one hand. [bug #366](https://code.google.com/p/aedict/issues/detail?id=366) [bug #365](https://code.google.com/p/aedict/issues/detail?id=365)
  * Unite all kanji search into one activity. [bug #373](https://code.google.com/p/aedict/issues/detail?id=373)
  * Learning Progress Review screen added, accessible from the Quiz screen. [bug #358](https://code.google.com/p/aedict/issues/detail?id=358)
  * Search for "na" will match both な and んあ. [bug #367](https://code.google.com/p/aedict/issues/detail?id=367)
  * Notepad: Fixed category menu popup on category move/delete and search. [bug #369](https://code.google.com/p/aedict/issues/detail?id=369)
  * Notepad: Fixed crash when launching quiz.
  * Fixed: SOD image were rendered very small on Samsung Galaxy Note 4. [bug #377](https://code.google.com/p/aedict/issues/detail?id=377)
  * Tags: added support for bold, italic and underlined text. [bug #346](https://code.google.com/p/aedict/issues/detail?id=346)

## 3.4.35 ##

Nov 27th, 2014

  * Tap the tab of the currently selected notepad category for a context menu. [bug #350](https://code.google.com/p/aedict/issues/detail?id=350)
  * Fixed: Text is cleared in the search bar while manipulating text. [bug #349](https://code.google.com/p/aedict/issues/detail?id=349)
  * Fixed: sometimes the dictionary loaders hang.
  * Quick search now always searches in both JMDict and Kanjidic and ignores filter settings.
  * Kanji parts major update. [bug #356](https://code.google.com/p/aedict/issues/detail?id=356)
  * Fixed: sentence analysis de-conjugates less aggresively, e.g. そういえば is no longer de-conjugated to souyou
  * Sentence analysis can now intelligently skip wa, ga, na particles.
  * Sentence analysis now shows deinflection used. [bug #353](https://code.google.com/p/aedict/issues/detail?id=353)
  * Notepad quiz: added check-all/uncheck-all checkbox. [bug #352](https://code.google.com/p/aedict/issues/detail?id=352)
  * Added support for disabling romaji input. [bug #347](https://code.google.com/p/aedict/issues/detail?id=347)
  * Notepad: moving items between categories will now place these items on top of the target category. [bug #359](https://code.google.com/p/aedict/issues/detail?id=359)
  * Omnisearch can now be moved to the bottom to the screen. [bug #348](https://code.google.com/p/aedict/issues/detail?id=348)
  * SKIP expert search: star #+ dot and space can now be used as a separator instead of dash, for example 4+1#4. [bug #357](https://code.google.com/p/aedict/issues/detail?id=357)
  * Search by parts: information about clicked part is now shown. [bug #361](https://code.google.com/p/aedict/issues/detail?id=361)
  * Upgraded icons to highres. [bug #355](https://code.google.com/p/aedict/issues/detail?id=355)
  * Fixed: TLSv1.2 support added for OwnCloud (only Android 4.1 and higher)
  * API: added support for search in kanjidic; added `return_user_selected_result`; added search-by-parts

## 3.4.34 ##

Nov 14th, 2014

  * Export: added %tag% support. [bug #343](https://code.google.com/p/aedict/issues/detail?id=343)
  * Fixed deinflection in sentence analysis.
  * Fixed -te iru deinflection.
  * Optimized search engine. [bug #344](https://code.google.com/p/aedict/issues/detail?id=344)
  * Fixed: Notepad quiz type could not be changed. [bug #345](https://code.google.com/p/aedict/issues/detail?id=345)
  * Added external intents for resolving kanjis.
  * Dropbox client upgrade to 3.1.1

## 3.4.33 ##

Oct 29th, 2014

  * Fixed crash in kana table when katakana is selected.
  * Fixed the 亻 part.
  * Added support for selection-bulk-delete recently viewed items.
  * Configuration option added: only the first tag line is now displayed. [bug #336](https://code.google.com/p/aedict/issues/detail?id=336)
  * Deinflects verbs when sentence analysis is performed. [Bug #335](https://code.google.com/p/aedict/issues/detail?id=335)
  * Quiz type is now configured in Quiz Launch Activity. [Bug #333](https://code.google.com/p/aedict/issues/detail?id=333)
  * Notepad icon is now correctly updated when entries are added to the notepad. [bug #327](https://code.google.com/p/aedict/issues/detail?id=327)
  * JMDict entry detail screen now shows notepad/tag icon; allows tagging of the entry. [bug #338](https://code.google.com/p/aedict/issues/detail?id=338)

## 3.4.32 ##

Oct 21th, 2014

  * Brand new sexy Parts view. [bug #324](https://code.google.com/p/aedict/issues/detail?id=324)
  * Improved accuracy of the Parts search.
  * Notepad export now optionally uses the custom formatting. [bug #330](https://code.google.com/p/aedict/issues/detail?id=330)
  * Fixed crash when searching for entries with particular kanji reading. [bug #329](https://code.google.com/p/aedict/issues/detail?id=329)
  * Minor optimization in search screen.
  * More informative error message on hotel wifi. [bug #325](https://code.google.com/p/aedict/issues/detail?id=325)
  * Quiz: back button now goes back one question. Added action bar button to abort quiz. [bug #328](https://code.google.com/p/aedict/issues/detail?id=328)
  * Quiz Launch Activity: added check-all/check-none button. [bug #330](https://code.google.com/p/aedict/issues/detail?id=330)
  * Quiz Launch Activity now remembers selected categories. [bug #330](https://code.google.com/p/aedict/issues/detail?id=330)

## 3.4.31 ##

Oct 9th, 2014

  * Added search filters (e.g. certain jlpt level only, verbs only, etc). [bug #309](https://code.google.com/p/aedict/issues/detail?id=309)
  * Kanji parts are now ordered: composite kanjis precede its parts. [bug #323](https://code.google.com/p/aedict/issues/detail?id=323)
  * Automatic vowel prolonging during search can now be disabled in Settings. [bug #321](https://code.google.com/p/aedict/issues/detail?id=321)
  * Added -rareru and -saserareru to example inflections. [bug #322](https://code.google.com/p/aedict/issues/detail?id=322)
  * Add file browser support to the welcome screen. [bug #320](https://code.google.com/p/aedict/issues/detail?id=320)
  * Added "prolong\_vowels" setting to Aedict API

## 3.4.30 ##

Sep 12th, 2014

  * quiz quit confirmation. [bug #315](https://code.google.com/p/aedict/issues/detail?id=315)
  * omnisearch will also find a mix of kanji and kana, for example searching for 見ため will also find 見た目 (JMDict only). Please update dictionaries. [bug #316](https://code.google.com/p/aedict/issues/detail?id=316)
  * reverted the sentence analyzer back to the old one, which is a bit slower, but more accurate. [bug #318](https://code.google.com/p/aedict/issues/detail?id=318)
  * Automatic checks for dictionary updates. [bug #317](https://code.google.com/p/aedict/issues/detail?id=317)

## 3.4.29 ##

Sep 10th, 2014

  * Fixed app freezing when auto-backup was enabled with Dropbox/OwnCloud backend. [bug #313](https://code.google.com/p/aedict/issues/detail?id=313) [bug #311](https://code.google.com/p/aedict/issues/detail?id=311)
  * notepad activity saves scroll position on resume, if the notepad has not been changed. [bug #310](https://code.google.com/p/aedict/issues/detail?id=310)
  * Parts: parts images upgraded to hi-res. [bug #312](https://code.google.com/p/aedict/issues/detail?id=312)
  * Keyboard to activate after selecting x on search bar. [bug #314](https://code.google.com/p/aedict/issues/detail?id=314)
  * Fixed: Unicode hex code was 0x0 for some kanjis.

## 3.4.28 ##

Sep 7th, 2014

  * Backup activity is now auto-refreshed after backup/delete.
  * Settings: current values are now shown in summary. [bug #305](https://code.google.com/p/aedict/issues/detail?id=305)
  * Added support for backup to Dropbox.
  * Notepad is now searchable
  * Added search bar to ResultActivity and EntryActivity. [bug #291](https://code.google.com/p/aedict/issues/detail?id=291)
  * Settings can now be saved to phone memory, Dropbox or ownCloud. [bug #301](https://code.google.com/p/aedict/issues/detail?id=301)
  * Fixed: sentence analyzer would split sentence into individual characters, instead into words.
  * Notepad load/save implemented. [bug #307](https://code.google.com/p/aedict/issues/detail?id=307)
  * Experimental: Search: Automatic vowel prolonging, e.g. しゅじん will also find しゅうじん. [bug #308](https://code.google.com/p/aedict/issues/detail?id=308)
  * Optimized Dropbox sync. [bug #306](https://code.google.com/p/aedict/issues/detail?id=306)

## 3.4.27 ##

Aug 27th, 2014

  * Added backup support for OwnCloud. [bug #303](https://code.google.com/p/aedict/issues/detail?id=303)
  * Fixed: Notepad tab selection is now preserved when the app is suspended.
  * Fixed: "to eat" would not be found.
  * Fixed crash. [bug #304](https://code.google.com/p/aedict/issues/detail?id=304)
  * Fixed: click on item's notepad icon opens notepad in default category, but not in the item's category.

## 3.4.26 ##

Aug 21th, 2014

  * Fixed crash with incompatible data write/read methods.
  * Fixed crash while reordering Notepad categories.

## 3.4.25 ##

Aug 20th, 2014

  * Fixed omnisearch crash when entering small tsu. [bug #300](https://code.google.com/p/aedict/issues/detail?id=300)
  * Parts: background color of disabled items in dark theme made more white-y, to improve readability in sunlight.
  * Parts: fixed incorrect black-on-white background.
  * CSV export: added support for storing the csv file to "disk", via a file save dialog. [bug #162](https://code.google.com/p/aedict/issues/detail?id=162)
  * Added file-browser dialog when selecting custom JP font. [bug #302](https://code.google.com/p/aedict/issues/detail?id=302)

## 3.4.24 ##

Aug 14th, 2014

  * Fixed another crash introduced in 3.4.22 when searching with omnibox. :-)

## 3.4.23 ##

Aug 12th, 2014

  * Fixed crash introduced in 3.4.22 when searching with omnibox.

## 3.4.22 ##

Aug 12th, 2014

  * JMDict/Kanjidic/Example sentence search more relaxed - entered words can now be present anywhere in the sentence, in no particular order. [bug #295](https://code.google.com/p/aedict/issues/detail?id=295) [bug #299](https://code.google.com/p/aedict/issues/detail?id=299)
  * JP entry is now displayed in the activity title.
  * Lucene query minor speed-up

## 3.4.21 ##

Aug 5th, 2014

  * Added Wadoku dictionary, please download it via Settings / Dictionary Manager
  * Fixed: live search is not restarted when dictionary set is changed
  * Notepad quiz can now be performed from a subset of categories. [bug #296](https://code.google.com/p/aedict/issues/detail?id=296)
  * Fixed katakana search issues. [bug #297](https://code.google.com/p/aedict/issues/detail?id=297)
  * Removed 'sort' config option as custom dictionaries are currently unsupported.
  * Fixed failing SKIP search. [bug #298](https://code.google.com/p/aedict/issues/detail?id=298)

## 3.4.20 ##

July 29th, 2014

  * Hardware Enter button no longer starts search in live search mode; instead, it hides the virtual keyboard. [bug #288](https://code.google.com/p/aedict/issues/detail?id=288)
  * Kanji draw: drawn image cleared when added to the Quick Scratch Pad. [bug #286](https://code.google.com/p/aedict/issues/detail?id=286)
  * Fixed highlight of search term for Kanjidic search.
  * Upgraded Lucene engine, optimized Lucene queries.
  * Too many kanjis in scratchpad would hide the "clear" button. [bug #293](https://code.google.com/p/aedict/issues/detail?id=293)
  * Implemented Self Test which tests whether Aedict provide correct results.
  * Skip search results are now shown in a more condensed form; fixed sorting. [bug #294](https://code.google.com/p/aedict/issues/detail?id=294)

## 3.4.19 ##

July 9th, 2014

  * Improved kanjidraw recognition accuracy on some devices.
  * Parts 邦阡 got replaced by 阝. Please update the dictionaries. [bug #287](https://code.google.com/p/aedict/issues/detail?id=287)
  * Settings screen is now categorized.
  * Fixes minor conjugation issues in example sentences. Please update the dictionaries.
  * Omnisearch pre-fill can now be disabled.
  * Fixed SOD image caching for Android 2.3 and older.
  * Fixed NPE crash in Dictionary Manager.

## 3.4.18 ##

June 18th, 2014

  * GUI improvements
  * Quick Scratch Pad added to the Kanji Drawing Activity. [bug #284](https://code.google.com/p/aedict/issues/detail?id=284)
  * Pasting kanji to omnibox no longer adds space before the kanji.
  * Heisig: added Kanji Index numbers from 6th edition, please update dictionaries.
  * Fixed crash when SD Card is removed from the phone.
  * Fixed radio button overflow on small screens. [bug #285](https://code.google.com/p/aedict/issues/detail?id=285)
  * Compatible with the Hodor keyboard

## 3.4.17 ##

June 5th, 2014

  * Fixed: notification icon now always starts the search activity with cleared onmisearch.
  * Fixed: Aedict could write zero-sized notepad.bin file, losing all notepad data.
  * Dictionary downloader: replaced old "Download All" button with new "Update All" button which only updates dictionaries - it does not download any new dictionaries. [bug #283](https://code.google.com/p/aedict/issues/detail?id=283)

## 3.4.16 ##

June 2nd, 2014

  * Fixed crash when searching kanjis by some parts combination (e.g. only the 辶 part)
  * Fixed: soft keyboard hidden when the "clear" X button is pressed in the omnisearch.
  * Added support for hiding the voice recognition button
  * Clicking the notification icon will now clear the omnisearch box.
  * Updated German Heisig keywords, thanks to Thomas Klimek. Please update the dictionaries.
  * Search for specific readings of kanji, in the kanji detail screen, in the DICT tab. [bug #281](https://code.google.com/p/aedict/issues/detail?id=281)

## 3.4.15 ##

May 28th, 2014

  * Japanese speech recognition support added.
  * Implemented workaround for non-working CSV exports to Jota+ and aNdClip
  * Copy to Clipboard: added option which copies all three of kanji, reading and meaning to the clipboard.

## 3.4.14 ##

May 24th, 2014

  * Improved search result ordering - please update dictionaries.
  * JMDict: Commonly used kanjis are now underlined. [bug #278](https://code.google.com/p/aedict/issues/detail?id=278)
  * "Add to scratchpad" now accessible when selecting kanjis in a kanji list.
  * Automatic local notepad backup added. [bug #279](https://code.google.com/p/aedict/issues/detail?id=279)
  * Omnibox now shows/hides the soft keyboard as configured.
  * German Heisig keywords added, thanks to Thomas Klimek. Please update dictionaries first.
  * Search results now highlights search term
  * Quicksearch results now show translation on one line only, which saves some space.
  * Fixed crash when exporting large quizzes to CSV. [bug #280](https://code.google.com/p/aedict/issues/detail?id=280)

## 3.4.13 ##

May 15th, 2014

  * Added a simple SRS/Leitner support to Quiz, see https://code.google.com/p/aedict/wiki/FAQ#Q:_SRS/Leitner? for details. [bug #200](https://code.google.com/p/aedict/issues/detail?id=200) [bug #277](https://code.google.com/p/aedict/issues/detail?id=277)
  * Added support of exporting quiz questions to Anki-compatible CSV and to AnkiDroid (requires mass-import support in AnkiDroid).
  * Added the Kotowaza proverbs dictionary. [bug #253](https://code.google.com/p/aedict/issues/detail?id=253)
  * Parts matching updated a bit - added the 辶, ユ and マ parts.
  * Fixed some crashes.
  * Fixed crash when Simeji requests to search for an empty string

## 3.4.12 ##

May 9th, 2014

  * Heisig index number and name is now displayed for a kanji (only after dictionary files update). [bug #274](https://code.google.com/p/aedict/issues/detail?id=274)
  * Fixed NullPointerException in VerbInflectionFragment
  * Heisig Quizzes added. [bug #275](https://code.google.com/p/aedict/issues/detail?id=275)
  * Dictionary data format is now forward-compatible with future releases of Aedict.
  * Fixed reported crashes.

## 3.4.11 ##

April 26th, 2014

  * Aedict should no longer crash at startup when SDCard is not writable
  * Flashcard export issue fixed. [bug #271](https://code.google.com/p/aedict/issues/detail?id=271)
  * Search is no longer repeated after returning to results screen. [bug #273](https://code.google.com/p/aedict/issues/detail?id=273)
  * Reordering of notepad categories and items in categories implemented. [bug #272](https://code.google.com/p/aedict/issues/detail?id=272)
  * Fixed: sentence analysis was not performed when the search term contained romaji.

## 3.4.10 ##
April 21th, 2014

  * JMDict entry page did not show kanjis. [bug #270](https://code.google.com/p/aedict/issues/detail?id=270)
  * Fixed crash with POBox keyboard on xperia
  * Reverted the sd card detection routine back to getExternalFilesDir()

## 3.4.9 ##

April 17th, 2014

  * Show the origin of the entry. [bug #265](https://code.google.com/p/aedict/issues/detail?id=265)
  * Searching in multiple dictionaries uses less memory and is more responsive
  * Search results now shows entry preview. [bug #266](https://code.google.com/p/aedict/issues/detail?id=266)
  * Expert: Added Aedict 2 prefs xml import. [bug #268](https://code.google.com/p/aedict/issues/detail?id=268)
  * CSV export: added %grammar\_codes% and %furigana%
  * Romanization: dzu and du are now accepted as づ, dji and di as ぢ, jya as じゃ and xtu and xtsu as っ
  * Initial support for mass-export to AnkiDroid. [bug #264](https://code.google.com/p/aedict/issues/detail?id=264)
  * Added support for exporting ruby furigana to AnkiDroid/CSV. [bug #269](https://code.google.com/p/aedict/issues/detail?id=269)
  * Example sentences are now optionally displayed in first tab. [bug #263](https://code.google.com/p/aedict/issues/detail?id=263)
  * API: added support for returning search result to calling application. [bug #243](https://code.google.com/p/aedict/issues/detail?id=243)

## 3.4.8 ##

April 4th, 2014

  * Added support for deleting old backups.
  * Fixed SDCard detection algorithm which caused Aedict to fail to download the dictionaries.
  * Fixed crash when rotating screen on some phones.

## 3.4.7 ##

April 2nd, 2014

  * Backup/Restore to the phone memory. [bug #260](https://code.google.com/p/aedict/issues/detail?id=260)
  * Improved Tatoeba/Tanaka indexer - removed kanjis from furigana
  * Fixed a crash when certain characters were entered into the omnisearch box
  * live search more responsive
  * SD-Card auto-detection improved. [bug #262](https://code.google.com/p/aedict/issues/detail?id=262)

## 3.4.6 ##

March 13th, 2014

  * Improved search result ordering. [bug #247](https://code.google.com/p/aedict/issues/detail?id=247) [bug #238](https://code.google.com/p/aedict/issues/detail?id=238) [bug #248](https://code.google.com/p/aedict/issues/detail?id=248)
  * Show the (P) popular indicator. [bug #242](https://code.google.com/p/aedict/issues/detail?id=242)
  * Show codes like (i-adj), (n) in search results. [bug #239](https://code.google.com/p/aedict/issues/detail?id=239)
  * Override dictionary location in the welcome screen. [bug #241](https://code.google.com/p/aedict/issues/detail?id=241)
  * Search button is no longer present when live search is enabled. [bug #245](https://code.google.com/p/aedict/issues/detail?id=245)
  * Added support for zero-length recently viewed list.
  * Minor GUI fixes. [bug #237](https://code.google.com/p/aedict/issues/detail?id=237) [bug #246](https://code.google.com/p/aedict/issues/detail?id=246)
  * Fixed Tatoeba translations, added ActionBar icon which links to Tatoeba web page.
  * Added ~n desu/~n da inflections. [bug #252](https://code.google.com/p/aedict/issues/detail?id=252)
  * Romanization: added support for va, vi, vu, ve, vo romanization.
  * Fixed Share issues. [bug #240](https://code.google.com/p/aedict/issues/detail?id=240)
  * Scratchpad search should also include kanji info for all kanjis. [bug #250](https://code.google.com/p/aedict/issues/detail?id=250)
  * Confirmation toast now shown when adding items to the Notepad.
  * Improved Parts search results. [bug #254](https://code.google.com/p/aedict/issues/detail?id=254)
  * Sentence analysis now resolves kanjis from Kanjidic as well. [bug #251](https://code.google.com/p/aedict/issues/detail?id=251)
  * Verb conjugations: labels, links to more documentation. [bug #249](https://code.google.com/p/aedict/issues/detail?id=249)
  * Entries present in notepad are now marked as such in the entry lists, improved Notepad category creation. [bug #256](https://code.google.com/p/aedict/issues/detail?id=256)
  * Anki CSV: support for custom format added. [bug #255](https://code.google.com/p/aedict/issues/detail?id=255)

## 3.4.5 ##

February 21th, 2014

  * Added support for 3-column export to Desktop Anki. [bug #232](https://code.google.com/p/aedict/issues/detail?id=232)
  * Clearing the "Recently Viewed" list now requires confirmation.
  * Dictionaries can now be deleted. [bug #233](https://code.google.com/p/aedict/issues/detail?id=233)
  * Added support for hiding the 'Speech Synthesis' button and 'Romaji/Kana' action bar menu item.
  * Make example sentences not numbered. [bug #234](https://code.google.com/p/aedict/issues/detail?id=234)
  * Replace "starts with/ends with" buttons with radio buttons. [bug #235](https://code.google.com/p/aedict/issues/detail?id=235)
  * Omnisearch can now be optionally pre-filled with last search string. [bug #236](https://code.google.com/p/aedict/issues/detail?id=236)

## 3.4.4 ##

February 12th, 2014

  * Added speech synthesis support. [bug #228](https://code.google.com/p/aedict/issues/detail?id=228)
  * Added support for inflecting/deinflection of adjectives. [bug #221](https://code.google.com/p/aedict/issues/detail?id=221)
  * The "Kanji Draw" by "Leafdigital" can now be used for Kanji search by drawing. [bug #160](https://code.google.com/p/aedict/issues/detail?id=160)
  * Pinyin readings now optionally shown with diacritics. [bug #231](https://code.google.com/p/aedict/issues/detail?id=231)
  * Internal KanjiPad now shows more results (prev 15, now 100)
  * Added "Radical" tab to the "Kanji Details" view. [bug #230](https://code.google.com/p/aedict/issues/detail?id=230)
  * Auto-show result entry if ResultActivity found only one item.
  * Minor GUI improvements. [bug #229](https://code.google.com/p/aedict/issues/detail?id=229)

## 3.4.3 ##

February 5th, 2014

  * Allow disabling of automatic clipboard paste to omnibox in the Main Activity
  * Fixed: Moving notepad items to different category will get rolled back
  * Exported Flashcard front/back can now be configured

## 3.4.2 ##

January 29th, 2014

  * perform search using multiple EDICT/JMDICT dictionaries at once. [bug #216](https://code.google.com/p/aedict/issues/detail?id=216)
  * suru/tou inflection fixes. [bug #220](https://code.google.com/p/aedict/issues/detail?id=220)
  * pinyin readings support (please wait until the new kanjidic is indexed). [bug #222](https://code.google.com/p/aedict/issues/detail?id=222)
  * SKIP search updates. [bug #224](https://code.google.com/p/aedict/issues/detail?id=224)
  * auto-search feature for clipboard content when the app is opened. [bug #225](https://code.google.com/p/aedict/issues/detail?id=225)
  * Live Search can now be turned off
  * XXHDPI icon.

## 3.4.1 ##

January 24th, 2014

  * Improved search accuracy. [bug #219](https://code.google.com/p/aedict/issues/detail?id=219)
  * User can use its custom ttf japanese font. [bug #211](https://code.google.com/p/aedict/issues/detail?id=211)
  * Fixed: Search settings always start opened. [bug #212](https://code.google.com/p/aedict/issues/detail?id=212)
  * Max column count configurable in the Edict Details activity. [bug #213](https://code.google.com/p/aedict/issues/detail?id=213)
  * Fixed crashes. [bug #214](https://code.google.com/p/aedict/issues/detail?id=214)
  * Quiz now shows current/max question numbers. [bug #217](https://code.google.com/p/aedict/issues/detail?id=217)
  * Fixed: Search of kanji in kanjidic yields no results. [bug #218](https://code.google.com/p/aedict/issues/detail?id=218)
  * Custom Japanese font is no longer used on Android 2.1 - it was not displayed.

## 3.4 ##

January 17th, 2014

**Note**: Please update the Kanjidic and SOD dictionaries, to gain access to the new features.

  * Dropbox Sync added for Notepad, recent\_entries and Tags. [bug #181](https://code.google.com/p/aedict/issues/detail?id=181)
  * Stroke-Order Kanji diagrams now cover 6500+ kanjis. [bug #144](https://code.google.com/p/aedict/issues/detail?id=144)
  * Word Search Activity now shows live search results. [bug #190](https://code.google.com/p/aedict/issues/detail?id=190)
  * Radical search renamed to Parts Search, more accurate, shows live results, GUI rework. [bug #205](https://code.google.com/p/aedict/issues/detail?id=205)
  * Romanization input more relaxed (e.g. Hepburn now accepts TU, Nihon-Shiki now accepts TSU etc). [bug #206](https://code.google.com/p/aedict/issues/detail?id=206)
  * Word Search is now started initially, instead of Welcome Activity. [bug #209](https://code.google.com/p/aedict/issues/detail?id=209)
  * Custom dictionary now selectable from the Word Search Activity. [bug #207](https://code.google.com/p/aedict/issues/detail?id=207)
  * Japanese font now used to draw characters. [bug #113](https://code.google.com/p/aedict/issues/detail?id=113)
  * Entry is not added to notepad if it is already present in the target category ([bug #208](https://code.google.com/p/aedict/issues/detail?id=208))
  * Fixed some crashes ([bug #210](https://code.google.com/p/aedict/issues/detail?id=210))

## 3.3 ##

January 7th, 2014

  * Initial support for Samsung multi-window feature. [bug #167](https://code.google.com/p/aedict/issues/detail?id=167)
  * Fixed: search term decomposition was removed by accident, added back. [bug #202](https://code.google.com/p/aedict/issues/detail?id=202)
  * Added support for tagging dictionary entries. [bug #138](https://code.google.com/p/aedict/issues/detail?id=138)
  * Added Japanese Proper Names dictionary. [bug #193](https://code.google.com/p/aedict/issues/detail?id=193)
  * Sense can now be copied to clipboard. [bug #201](https://code.google.com/p/aedict/issues/detail?id=201)
  * Implemented Kanji Scratchpad which allows you to search for entries containing multiple kanjis. [bug #196](https://code.google.com/p/aedict/issues/detail?id=196)
  * Bug fixes. [bug #198](https://code.google.com/p/aedict/issues/detail?id=198)
  * Entry activity max displayed column count is now configurable. [bug #199](https://code.google.com/p/aedict/issues/detail?id=199)

## 3.2 ##

December 31th, 2013

**Note**: Before using Kanjidic Word search, please make sure that the Kanjidic dictionary is updated (Configuration / Dictionary Manager)

  * Kanji draw quiz mode - user is presented with kanji readings and has to draw the kanji. [bug #139](https://code.google.com/p/aedict/issues/detail?id=139)
  * Kanji draw JLPT practice - [bug #140](https://code.google.com/p/aedict/issues/detail?id=140)
  * Added StartsWith, EndsWith and Exact search match - [bug #170](https://code.google.com/p/aedict/issues/detail?id=170)
  * Dictionary updates - [bug #175](https://code.google.com/p/aedict/issues/detail?id=175)
  * Documented Intent API - [bug #177](https://code.google.com/p/aedict/issues/detail?id=177)
  * Dictionary download can now be interrupted - [bug #178](https://code.google.com/p/aedict/issues/detail?id=178)
  * Implemented import from Aedict 2 Notepad backup - [bug #180](https://code.google.com/p/aedict/issues/detail?id=180)
  * Fixed copy-to-clipboard - [bug #182](https://code.google.com/p/aedict/issues/detail?id=182) [bug #183](https://code.google.com/p/aedict/issues/detail?id=183) [bug #184](https://code.google.com/p/aedict/issues/detail?id=184)
  * Configurable size of 'recently viewed entries' - [bug #194](https://code.google.com/p/aedict/issues/detail?id=194)
  * Navigation Drawer with search options added - [bug #192](https://code.google.com/p/aedict/issues/detail?id=192)
  * Radical search will now show impossible radical combination - [bug #191](https://code.google.com/p/aedict/issues/detail?id=191)
  * Added posibility to create shortcut to the Word Search (via QuickShortcutMaker) - see https://code.google.com/p/aedict/wiki/AEDICT3#Direct_access_to_various_Aedict_3_parts for details
  * Fixed crash when disabling the "Sort" configuration option. Fixed some other minor crashes.
  * Set targetSdkVersion to 15 to hide HTC One ugly compatibility menu bar
  * Added support for Hungarian, Slovene and Swedish for JMDict
  * All list view should behave the same. [bug #185](https://code.google.com/p/aedict/issues/detail?id=185)
  * Word search in Kanjidic implemented. [bug #186](https://code.google.com/p/aedict/issues/detail?id=186)

## 3.1 ##

December 18th, 2013

  * allow switching between holo light and dark theme in preferences. [bug #176](https://code.google.com/p/aedict/issues/detail?id=176)
  * Fix the CSV so that it is importable by desktop app ANKI. [bug #174](https://code.google.com/p/aedict/issues/detail?id=174)
  * You can now select text in a browser and Share it to Aedict for translation.
  * Notepad accessible from search+detail activity. [bug #173](https://code.google.com/p/aedict/issues/detail?id=173)
  * Add support for analyze in ResultActivity. [bug #172](https://code.google.com/p/aedict/issues/detail?id=172)
  * Fixed search issues. [bug #171](https://code.google.com/p/aedict/issues/detail?id=171)
  * Implement intent-based card adding from Aedict to Ankidroid. [bug #153](https://code.google.com/p/aedict/issues/detail?id=153)
  * Added tablet support. [bug #137](https://code.google.com/p/aedict/issues/detail?id=137)
  * JLPT Quiz can now be used to guess kanji from English.

## 3.0 ##

December 4th, 2013

  * Notepad is now sorted newest first as per [bug #141](https://code.google.com/p/aedict/issues/detail?id=141)
  * Aedict 3 now uses JMDict and displays all grammar information present in JMDict. Related bug: [bug #142](https://code.google.com/p/aedict/issues/detail?id=142)
  * New Kanjidic2 indexing was used which fixes issues with radical lookup: [bug #143](https://code.google.com/p/aedict/issues/detail?id=143)
  * Quiz length can now be specified. Also, you can launch quiz from selected notepad items. See [bug #145](https://code.google.com/p/aedict/issues/detail?id=145) for details.
  * French encoding issues fixed - JMDict uses UTF-8. Fixes [bug #148](https://code.google.com/p/aedict/issues/detail?id=148)
  * Romaji support fixed for certain words, e.g. Faia (ファイア): [bug #149](https://code.google.com/p/aedict/issues/detail?id=149)
  * Added Kunrei-Shiki romanization as per [bug #151](https://code.google.com/p/aedict/issues/detail?id=151)
  * Proper detection of external storage (SDCard) location: [bug #157](https://code.google.com/p/aedict/issues/detail?id=157)
  * Quiz can now optionally display readings along with the kanji. [bug #161](https://code.google.com/p/aedict/issues/detail?id=161)
  * Notepad can now be exported to CSV (no import though). [bug #162](https://code.google.com/p/aedict/issues/detail?id=162)
  * Delete button is now dialog-protected. [bug #165](https://code.google.com/p/aedict/issues/detail?id=165)