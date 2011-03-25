#!/bin/bash
cd ..

# Index Edict
# We need edict.gz to be present locally, for Tanaka/Tatoeba parsing
wget http://ftp.monash.edu.au/pub/nihongo/edict.gz || exit 1
./ai.sh -f edict.gz -g --upload || exit 1

# Index Kanjidic
./ai.sh -d -k --upload || exit 1

# Index Tanaka
./ai.sh -d -t --upload || exit 1

# Index Tatoeba
wget http://tatoeba.org/files/downloads/links.csv || exit 1
wget http://tatoeba.org/files/downloads/jpn_indices.csv || exit 1
./ai.sh -d -T --upload  || exit 1

# Index compdict dictionary:
# Computing & Telecommunications dictionary
# Copyright (C) 2008 The Electronic Dictionary Research and Development Group,http://www.csse.monash.edu.au/~jwb/compdic_doc.html
./ai.sh -u http://ftp.monash.edu.au/pub/nihongo/compdic.gz -n compdict -g --upload || exit 1

# Index enamdict dictionary:
# Japanese proper names: place-names; surnames; given names; (some) company names and product names
# Copyright (C) 2009 The Electronic Dictionary Research and Development Group, http://www.csse.monash.edu.au/~jwb/enamdict_doc.html
./ai.sh -u http://ftp.monash.edu.au/pub/nihongo/enamdict.gz -n enamdict -g --upload || exit 1

# Index wdjteuc dictionary:
# German WdJTEUC,WADOKU JITEN JAPANISCH-DEUTSCH (C) 2003 ULRICH APEL - KONV. H.-J. BIBIKO 211.300 DATENSAETZE
# http://www.bibiko.de/dlde.htm
wget http://www.bibiko.de/WdJTUTF.zip || exit 1
unzip WdJTUTF.zip || exit 1
./ai.sh -f WdJTUTF.txt -n wdjteuc -e UTF-8 --upload || exit 1

# Index french-fj dictionary:
# French FJDICT,FJDICT 20DEC02 V00-001-PR3/Dictionnaire francais-japonais version preliminaire 3/Copyright J.-M. Desperrier + autres - 2002
# http://dico.fj.free.fr/index.php
./ai.sh -u http://dico.fj.free.fr/fj.dct -n french-fj --upload || exit 1

# Index hispadic dictionary:
# Hispadic Diccionarios Japonés-Español,18488715,Edict is Copyright Electronic Dictionary Research & Development Group/Hispadic is Copyright Abel Camarena
# License: http://creativecommons.org/licenses/by-sa/3.0/
# Webpage: http://hispadic.byethost3.com/
wget http://sites.google.com/site/hispadic/download/hispamix_euc.zip?attredirects=0 -O hispamix_euc.zip || exit 1
unzip hispamix_euc.zip || exit 1
./ai.sh -f hispamix.euc -n hispadic --upload || exit 1

