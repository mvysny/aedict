package sk.baka.aedict.indexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import sk.baka.aedict.indexer.Main.Config;
import sk.baka.aedict.indexer.TanakaParser.BLineParser;
import sk.baka.aedict.indexer.TanakaParser.Edict;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;

/**
 * Parses the Tatoeba dictionary files (see http://tatoeba.org/eng/download_tatoeba_example_sentences for details).
 * @author Martin Vysny
 */
public class TatoebaParser implements IDictParser {

    private final Edict edict;
    /**
     * Maps index of the Japanese sentence to a list of translations.
     */
    private final Map<Integer, Sentences> sentences = new HashMap<Integer, Sentences>();
    private final List<Sentence> nonJpSentences = new ArrayList<Sentence>();
    private final Config cfg;
    private static final String JAPAN_ISO_639_3_CODE = "jpn";
    public static final Set<String> ISO_639_3_CODES = new HashSet<String>(Arrays.asList("aaa","aab","aac","aad","aae","aaf","aag","aah","aai","aak","aal","aam","aan","aao","aap","aaq","aar","aas","aat","aau","aaw","aax","aaz","aba","abb","abc","abd","abe","abf","abg","abh","abi","abj","abk","abl","abm","abn","abo","abp","abq","abr","abs","abt","abu","abv","abw","abx","aby","abz","aca","acb","acd","ace","acf","ach","aci","ack","acl","acm","acn","acp","acq","acr","acs","act","acu","acv","acw","acx","acy","acz","ada","adb","add","ade","adf","adg","adh","adi","adj","adl","adn","ado","adp","adq","adr","ads","adt","adu","adw","adx","ady","adz","aea","aeb","aec","aed","aee","aek","ael","aem","aen","aeq","aer","aes","aeu","aew","aey","aez","afb","afd","afe","afg","afh","afi","afk","afn","afo","afp","afr","afs","aft","afu","afz","aga","agb","agc","agd","age","agf","agg","agh","agi","agj","agk","agl","agm","agn","ago","agq","agr","ags","agt","agu","agv","agw","agx","agy","agz","aha","ahb","ahg","ahh","ahi","ahk","ahl","ahm","ahn","aho","ahp","ahr","ahs","aht","aia","aib","aic","aid","aie","aif","aig","aih","aii","aij","aik","ail","aim","ain","aio","aip","aiq","air","ais","ait","aiw","aix","aiy","aja","ajg","aji","ajp","ajt","aju","ajw","ajz","aka","akb","akc","akd","ake","akf","akg","akh","aki","akj","akk","akl","akm","ako","akp","akq","akr","aks","akt","aku","akv","akw","akx","aky","akz","ala","alc","ald","ale","alf","alh","ali","alj","alk","all","alm","aln","alo","alp","alq","alr","als","alt","alu","alw","alx","aly","alz","ama","amb","amc","ame","amf","amg","amh","ami","amj","amk","aml","amm","amn","amo","amp","amq","amr","ams","amt","amu","amv","amw","amx","amy","amz","ana","anb","anc","and","ane","anf","ang","anh","ani","anj","ank","anl","anm","ann","ano","anp","anq","anr","ans","ant","anu","anv","anw","anx","any","anz","aoa","aob","aoc","aod","aoe","aof","aog","aoh","aoi","aoj","aok","aol","aom","aon","aor","aos","aot","aox","aoz","apb","apc","apd","ape","apf","apg","aph","api","apj","apk","apl","apm","apn","apo","app","apq","apr","aps","apt","apu","apv","apw","apx","apy","apz","aqc","aqg","aqm","aqn","aqp","aqr","aqz","ara","arb","arc","ard","are","arg","arh","ari","arj","ark","arl","arn","aro","arp","arq","arr","ars","aru","arv","arw","arx","ary","arz","asa","asb","asc","asd","ase","asf","asg","ash","asi","asj","ask","asl","asm","asn","aso","asp","asq","asr","ass","ast","asu","asv","asw","asx","asy","asz","ata","atb","atc","atd","ate","atg","ati","atj","atk","atl","atm","atn","ato","atp","atq","atr","ats","att","atu","atv","atw","atx","aty","atz","aua","aub","auc","aud","aue","aug","auh","aui","auj","auk","aul","aum","aun","auo","aup","auq","aur","aut","auu","auw","aux","auy","auz","ava","avb","avd","ave","avi","avk","avl","avn","avo","avs","avt","avu","avv","awa","awb","awc","awe","awh","awi","awk","awm","awn","awo","awr","aws","awt","awu","awv","aww","awx","awy","axb","axg","axk","axm","axx","aya","ayb","ayc","ayd","aye","ayg","ayh","ayi","ayk","ayl","aym","ayn","ayo","ayp","ayq","ayr","ays","ayt","ayu","ayx","ayy","ayz","aza","azb","aze","azg","azj","azm","azo","azt","azz","baa","bab","bac","bae","baf","bag","bah","baj","bak","bal","bam","ban","bao","bap","bar","bas","bau","bav","baw","bax","bay","baz","bba","bbb","bbc","bbd","bbe","bbf","bbg","bbh","bbi","bbj","bbk","bbl","bbm","bbn","bbo","bbp","bbq","bbr","bbs","bbt","bbu","bbv","bbw","bbx","bby","bbz","bca","bcb","bcc","bcd","bce","bcf","bcg","bch","bci","bcj","bck","bcl","bcm","bcn","bco","bcp","bcq","bcr","bcs","bct","bcu","bcv","bcw","bcy","bcz","bda","bdb","bdc","bdd","bde","bdf","bdg","bdh","bdi","bdj","bdk","bdl","bdm","bdn","bdo","bdp","bdq","bdr","bds","bdt","bdu","bdv","bdw","bdx","bdy","bdz","bea","beb","bec","bed","bee","bef","beg","beh","bei","bej","bek","bel","bem","ben","beo","bep","beq","bes","bet","beu","bev","bew","bex","bey","bez","bfa","bfb","bfc","bfd","bfe","bff","bfg","bfh","bfi","bfj","bfk","bfl","bfm","bfn","bfo","bfp","bfq","bfr","bfs","bft","bfu","bfw","bfx","bfy","bfz","bga","bgb","bgc","bgd","bge","bgf","bgg","bgi","bgj","bgk","bgl","bgm","bgn","bgo","bgp","bgq","bgr","bgs","bgt","bgu","bgv","bgw","bgx","bgy","bgz","bha","bhb","bhc","bhd","bhe","bhf","bhg","bhh","bhi","bhj","bhl","bhm","bhn","bho","bhp","bhq","bhr","bhs","bht","bhu","bhv","bhw","bhx","bhy","bhz","bia","bib","bic","bid","bie","bif","big","bij","bik","bil","bim","bin","bio","bip","biq","bir","bis","bit","biu","biv","biw","bix","biy","biz","bja","bjb","bjc","bjd","bje","bjf","bjg","bjh","bji","bjj","bjk","bjl","bjm","bjn","bjo","bjq","bjr","bjs","bjt","bju","bjv","bjw","bjx","bjy","bjz","bka","bkc","bkd","bkf","bkg","bkh","bki","bkj","bkk","bkl","bkm","bkn","bko","bkp","bkq","bkr","bks","bkt","bku","bkv","bkw","bkx","bky","bkz","bla","blb","blc","bld","ble","blf","blg","blh","bli","blj","blk","bll","blm","bln","blo","blp","blq","blr","bls","blt","blv","blw","blx","bly","blz","bma","bmb","bmc","bmd","bme","bmf","bmg","bmh","bmi","bmj","bmk","bml","bmm","bmn","bmo","bmp","bmq","bmr","bms","bmt","bmu","bmv","bmw","bmx","bmy","bmz","bna","bnb","bnc","bnd","bne","bnf","bng","bni","bnj","bnk","bnl","bnm","bnn","bno","bnp","bnq","bnr","bns","bnu","bnv","bnw","bnx","bny","bnz","boa","bob","bod","boe","bof","bog","boh","boi","boj","bok","bol","bom","bon","boo","bop","boq","bor","bos","bot","bou","bov","bow","box","boy","boz","bpa","bpb","bpd","bpg","bph","bpi","bpj","bpk","bpl","bpm","bpn","bpo","bpp","bpq","bpr","bps","bpt","bpu","bpv","bpw","bpx","bpy","bpz","bqa","bqb","bqc","bqd","bqf","bqg","bqh","bqi","bqj","bqk","bql","bqm","bqn","bqo","bqp","bqq","bqr","bqs","bqt","bqu","bqv","bqw","bqx","bqy","bqz","bra","brb","brc","brd","bre","brf","brg","brh","bri","brj","brk","brl","brm","brn","bro","brp","brq","brr","brs","brt","bru","brv","brw","brx","bry","brz","bsa","bsb","bsc","bse","bsf","bsg","bsh","bsi","bsj","bsk","bsl","bsm","bsn","bso","bsp","bsq","bsr","bss","bst","bsu","bsv","bsw","bsx","bsy","bta","btc","btd","bte","btf","btg","bth","bti","btj","btl","btm","btn","bto","btp","btq","btr","bts","btt","btu","btv","btw","btx","bty","btz","bua","bub","buc","bud","bue","buf","bug","buh","bui","buj","buk","bul","bum","bun","buo","bup","buq","bus","but","buu","buv","buw","bux","buy","buz","bva","bvb","bvc","bvd","bve","bvf","bvg","bvh","bvi","bvj","bvk","bvl","bvm","bvn","bvo","bvq","bvr","bvt","bvu","bvv","bvw","bvx","bvy","bvz","bwa","bwb","bwc","bwd","bwe","bwf","bwg","bwh","bwi","bwj","bwk","bwl","bwm","bwn","bwo","bwp","bwq","bwr","bws","bwt","bwu","bww","bwx","bwy","bwz","bxa","bxb","bxc","bxd","bxe","bxf","bxg","bxh","bxi","bxj","bxk","bxl","bxm","bxn","bxo","bxp","bxq","bxr","bxs","bxu","bxv","bxw","bxx","bxz","bya","byb","byc","byd","bye","byf","byg","byh","byi","byj","byk","byl","bym","byn","byo","byp","byq","byr","bys","byt","byv","byw","byx","byy","byz","bza","bzb","bzd","bze","bzf","bzg","bzh","bzi","bzj","bzk","bzl","bzm","bzn","bzo","bzp","bzq","bzr","bzs","bzt","bzu","bzv","bzw","bzx","bzy","bzz","caa","cab","cac","cad","cae","caf","cag","cah","caj","cak","cal","cam","can","cao","cap","caq","car","cas","cat","cav","caw","cax","cay","caz","cbb","cbc","cbd","cbe","cbg","cbh","cbi","cbj","cbk","cbl","cbn","cbo","cbr","cbs","cbt","cbu","cbv","cbw","cby","cca","ccc","ccd","cce","ccg","cch","ccj","ccl","ccm","cco","ccp","ccq","ccr","cda","cde","cdf","cdg","cdh","cdi","cdj","cdm","cdn","cdo","cdr","cds","cdy","cdz","cea","ceb","ceg","cen","ces","cet","cfa","cfd","cfg","cfm","cga","cgc","cgg","cgk","cha","chb","chc","chd","che","chf","chg","chh","chj","chk","chl","chm","chn","cho","chp","chq","chr","cht","chu","chv","chw","chx","chy","chz","cia","cib","cic","cid","cie","cih","cik","cim","cin","cip","cir","ciw","ciy","cja","cje","cjh","cji","cjk","cjm","cjn","cjo","cjp","cjs","cjv","cjy","cka","ckb","ckh","ckl","cko","ckq","ckr","cks","ckt","cku","ckv","ckx","cky","ckz","cla","clc","cld","cle","clh","cli","clk","cll","clm","clo","clu","clw","cly","cma","cme","cmg","cmi","cml","cmm","cmn","cmo","cmr","cms","cmt","cna","cnb","cnc","cng","cnh","cni","cnk","cnl","cno","cns","cnt","cnu","cnw","cnx","coa","cob","coc","cod","coe","cof","cog","coh","coj","cok","col","com","con","coo","cop","coq","cor","cos","cot","cou","cov","cow","cox","coy","coz","cpa","cpb","cpc","cpg","cpi","cpn","cps","cpu","cpx","cpy","cqd","cqu","cra","crb","crc","crd","cre","crf","crg","crh","cri","crj","crk","crl","crm","crn","cro","crq","crr","crs","crt","crv","crw","crx","cry","crz","csa","csb","csc","csd","cse","csf","csg","csh","csi","csk","csl","csm","csn","cso","csq","csr","css","cst","csw","csy","csz","cta","ctc","ctd","cte","ctg","ctl","ctm","ctn","cto","ctp","cts","ctt","ctu","ctz","cua","cub","cuc","cug","cuh","cui","cuj","cuk","cul","cum","cuo","cup","cuq","cur","cut","cuu","cuv","cuw","cux","cvg","cvn","cwa","cwb","cwd","cwe","cwg","cwt","cya","cyb","cym","cyo","czh","czk","czn","czo","czt","daa","dac","dad","dae","daf","dag","dah","dai","daj","dak","dal","dam","dan","dao","dap","daq","dar","das","dau","dav","daw","dax","daz","dba","dbb","dbd","dbe","dbf","dbg","dbi","dbj","dbl","dbm","dbn","dbo","dbp","dbq","dbr","dbu","dbv","dby","dcc","dcr","ddd","dde","ddg","ddi","ddj","ddn","ddo","dds","ddw","dec","ded","dee","def","deg","deh","dei","dek","del","dem","den","dep","deq","der","des","deu","dev","dez","dga","dgb","dgc","dgd","dge","dgg","dgh","dgi","dgk","dgn","dgo","dgr","dgs","dgu","dgx","dgz","dha","dhd","dhg","dhi","dhl","dhm","dhn","dho","dhr","dhs","dhu","dhv","dhw","dia","dib","dic","did","dif","dig","dih","dii","dij","dik","dil","dim","din","dio","dip","diq","dir","dis","dit","diu","div","diw","dix","diy","diz","djb","djc","djd","dje","djf","dji","djj","djk","djl","djm","djn","djo","djr","dju","djw","dka","dkk","dkl","dkr","dks","dkx","dlg","dlm","dln","dma","dmc","dme","dmg","dmk","dml","dmm","dmo","dmr","dms","dmu","dmv","dmx","dmy","dna","dnd","dne","dng","dni","dnk","dnn","dnr","dnt","dnu","dnw","dny","doa","dob","doc","doe","dof","doh","doi","dok","dol","don","doo","dop","doq","dor","dos","dot","dov","dow","dox","doy","doz","dpp","drb","drd","dre","drg","dri","drl","drn","dro","drq","drr","drs","drt","dru","dry","dsb","dse","dsh","dsi","dsl","dsn","dso","dsq","dta","dtb","dti","dtk","dtm","dtp","dtr","dts","dtt","dtu","dua","dub","duc","dud","due","duf","dug","duh","dui","duj","duk","dul","dum","dun","duo","dup","duq","dur","dus","duu","duv","duw","dux","duy","duz","dva","dwa","dwl","dwr","dws","dww","dya","dyb","dyd","dyg","dyi","dym","dyn","dyo","dyu","dyy","dza","dzd","dzg","dzl","dzn","dzo","ebg","ebk","ebo","ebr","ebu","ecr","ecs","ecy","eee","efa","efe","efi","ega","egl","ego","egy","ehu","eip","eit","eiv","eja","eka","eke","ekg","eki","ekk","ekl","ekm","eko","ekp","ekr","eky","ele","elh","eli","elk","ell","elm","elo","elp","elu","elx","ema","emb","eme","emg","emi","emk","emm","emn","emo","emp","ems","emu","emw","emx","emy","ena","enb","enc","end","enf","eng","enh","enm","enn","eno","enq","enr","enu","env","enw","eot","epi","epo","era","erg","erh","eri","erk","ero","err","ers","ert","erw","ese","esh","esi","esk","esl","esm","esn","eso","esq","ess","est","esu","etb","etc","eth","etn","eto","etr","ets","ett","etu","etx","etz","eus","eve","evh","evn","ewe","ewo","ext","eya","eyo","eze","faa","fab","fad","faf","fag","fah","fai","faj","fak","fal","fam","fan","fao","fap","far","fas","fat","fau","fax","fay","faz","fbl","fcs","fer","ffi","ffm","fgr","fia","fie","fij","fil","fin","fip","fir","fit","fiw","fkv","fla","flh","fli","fll","fln","flr","fly","fmp","fmu","fng","fni","fod","foi","fom","fon","for","fos","fpe","fqs","fra","frc","frd","frk","frm","fro","frp","frq","frr","frs","frt","fry","fse","fsl","fss","fub","fuc","fud","fue","fuf","fuh","fui","fuj","ful","fum","fun","fuq","fur","fut","fuu","fuv","fuy","fvr","fwa","fwe","gaa","gab","gac","gad","gae","gaf","gag","gah","gai","gaj","gak","gal","gam","gan","gao","gap","gaq","gar","gas","gat","gau","gaw","gax","gay","gaz","gba","gbb","gbc","gbd","gbe","gbf","gbg","gbh","gbi","gbj","gbk","gbl","gbm","gbn","gbo","gbp","gbq","gbr","gbs","gbu","gbv","gbx","gby","gbz","gcc","gcd","gce","gcf","gcl","gcn","gcr","gct","gda","gdb","gdc","gdd","gde","gdf","gdg","gdh","gdi","gdj","gdk","gdl","gdm","gdn","gdo","gdq","gdr","gdu","gdx","gea","geb","gec","ged","geg","geh","gei","gej","gek","gel","geq","ges","gew","gex","gey","gez","gfk","gft","gga","ggb","ggd","gge","ggg","ggk","ggl","ggn","ggo","ggr","ggt","ggu","ggw","gha","ghc","ghe","ghh","ghk","ghl","ghn","gho","ghr","ghs","ght","gia","gib","gic","gid","gig","gil","gim","gin","gio","gip","giq","gir","gis","git","giw","gix","giy","giz","gji","gjk","gjn","gju","gka","gke","gkn","gkp","gla","glc","gld","gle","glg","glh","gli","glj","glk","glo","glr","glu","glv","glw","gly","gma","gmb","gmd","gmh","gml","gmm","gmn","gmu","gmv","gmx","gmy","gna","gnb","gnc","gnd","gne","gng","gnh","gni","gnk","gnl","gnm","gnn","gno","gnq","gnr","gnt","gnu","gnw","gnz","goa","gob","goc","god","goe","gof","gog","goh","goi","goj","gok","gol","gom","gon","goo","gop","goq","gor","gos","got","gou","gow","gox","goy","goz","gpa","gpn","gqa","gqi","gqn","gqr","gra","grb","grc","grd","grg","grh","gri","grj","grm","grn","gro","grq","grr","grs","grt","gru","grv","grw","grx","gry","grz","gse","gsg","gsl","gsm","gsn","gso","gsp","gss","gsw","gta","gti","gua","gub","guc","gud","gue","guf","gug","guh","gui","guj","guk","gul","gum","gun","guo","gup","guq","gur","gus","gut","guu","guv","guw","gux","guz","gva","gvc","gve","gvf","gvj","gvl","gvm","gvn","gvo","gvp","gvr","gvs","gvy","gwa","gwb","gwc","gwd","gwe","gwf","gwg","gwi","gwj","gwn","gwr","gwt","gwu","gww","gwx","gxx","gya","gyb","gyd","gye","gyf","gyg","gyi","gyl","gym","gyn","gyr","gyy","gza","gzi","gzn","haa","hab","hac","had","hae","haf","hag","hah","hai","haj","hak","hal","ham","han","hao","hap","haq","har","has","hat","hau","hav","haw","hax","hay","haz","hba","hbb","hbn","hbo","hbs","hbu","hca","hch","hdn","hds","hdy","hea","heb","hed","heg","heh","hei","hem","her","hgm","hgw","hhi","hhr","hhy","hia","hib","hid","hif","hig","hih","hii","hij","hik","hil","hin","hio","hir","hit","hiw","hix","hji","hka","hke","hkk","hks","hla","hlb","hld","hle","hlt","hlu","hma","hmb","hmc","hmd","hme","hmf","hmg","hmh","hmi","hmj","hmk","hml","hmm","hmn","hmo","hmp","hmq","hmr","hms","hmt","hmu","hmv","hmw","hmy","hmz","hna","hnd","hne","hnh","hni","hnj","hnn","hno","hns","hnu","hoa","hob","hoc","hod","hoe","hoh","hoi","hoj","hol","hom","hoo","hop","hor","hos","hot","hov","how","hoy","hoz","hpo","hps","hra","hre","hrk","hrm","hro","hrr","hrt","hru","hrv","hrx","hrz","hsb","hsh","hsl","hsn","hss","hti","hto","hts","htu","htx","hub","huc","hud","hue","huf","hug","huh","hui","huj","huk","hul","hum","hun","huo","hup","huq","hur","hus","hut","huu","huv","huw","hux","huy","huz","hvc","hve","hvk","hvn","hvv","hwa","hwc","hwo","hya","hye","iai","ian","iap","iar","iba","ibb","ibd","ibe","ibg","ibi","ibl","ibm","ibn","ibo","ibr","ibu","iby","ica","ich","icl","icr","ida","idb","idc","idd","ide","idi","ido","idr","ids","idt","idu","ifa","ifb","ife","iff","ifk","ifm","ifu","ify","igb","ige","igg","igl","igm","ign","igo","igs","igw","ihb","ihi","ihp","iii","ijc","ije","ijj","ijn","ijs","ike","iki","ikk","ikl","iko","ikp","ikt","iku","ikv","ikw","ikx","ikz","ila","ilb","ile","ilg","ili","ilk","ill","ilo","ils","ilu","ilv","ilw","ima","ime","imi","iml","imn","imo","imr","ims","imy","ina","inb","ind","ing","inh","inj","inl","inm","inn","ino","inp","ins","int","inz","ior","iou","iow","ipi","ipk","ipo","iqu","ire","irh","iri","irk","irn","irr","iru","irx","iry","isa","isc","isd","ise","isg","ish","isi","isk","isl","ism","isn","iso","isr","ist","isu","ita","itb","ite","iti","itk","itl","itm","ito","itr","its","itt","itv","itw","itx","ity","itz","ium","ivb","ivv","iwk","iwm","iwo","iws","ixc","ixl","iya","iyo","iyx","izh","izi","izr","jaa","jab","jac","jad","jae","jaf","jah","jaj","jak","jal","jam","jao","jaq","jar","jas","jat","jau","jav","jax","jay","jaz","jbe","jbj","jbn","jbo","jbr","jbt","jbu","jcs","jct","jda","jdg","jdt","jeb","jee","jeg","jeh","jei","jek","jel","jen","jer","jet","jeu","jgb","jge","jgo","jhi","jhs","jia","jib","jic","jid","jie","jig","jih","jii","jil","jim","jio","jiq","jit","jiu","jiv","jiy","jko","jku","jle","jls","jma","jmb","jmc","jmd","jmi","jml","jmn","jmr","jms","jmx","jna","jnd","jng","jni","jnj","jnl","jns","job","jod","jor","jos","jow","jpa","jpn","jpr","jqr","jra","jrb","jrr","jrt","jru","jsl","jua","jub","juc","jud","juh","juk","jul","jum","jun","juo","jup","jur","jus","jut","juu","juw","juy","jvd","jvn","jwi","jya","jye","jyy","kaa","kab","kac","kad","kae","kaf","kag","kah","kai","kaj","kak","kal","kam","kan","kao","kap","kaq","kas","kat","kau","kav","kaw","kax","kay","kaz","kba","kbb","kbc","kbd","kbe","kbf","kbg","kbh","kbi","kbj","kbk","kbl","kbm","kbn","kbo","kbp","kbq","kbr","kbs","kbt","kbu","kbv","kbw","kbx","kby","kbz","kca","kcb","kcc","kcd","kce","kcf","kcg","kch","kci","kcj","kck","kcl","kcm","kcn","kco","kcp","kcq","kcr","kcs","kct","kcu","kcv","kcw","kcx","kcy","kcz","kda","kdc","kdd","kde","kdf","kdg","kdh","kdi","kdj","kdk","kdl","kdm","kdn","kdp","kdq","kdr","kdt","kdu","kdv","kdw","kdx","kdy","kdz","kea","keb","kec","ked","kee","kef","keg","keh","kei","kej","kek","kel","kem","ken","keo","kep","keq","ker","kes","ket","keu","kev","kew","kex","key","kez","kfa","kfb","kfc","kfd","kfe","kff","kfg","kfh","kfi","kfj","kfk","kfl","kfm","kfn","kfo","kfp","kfq","kfr","kfs","kft","kfu","kfv","kfw","kfx","kfy","kfz","kga","kgb","kgc","kgd","kge","kgf","kgg","kgh","kgi","kgj","kgk","kgl","kgm","kgn","kgo","kgp","kgq","kgr","kgs","kgt","kgu","kgv","kgw","kgx","kgy","kha","khb","khc","khd","khe","khf","khg","khh","khj","khk","khl","khm","khn","kho","khp","khq","khr","khs","kht","khu","khv","khw","khx","khy","khz","kia","kib","kic","kid","kie","kif","kig","kih","kii","kij","kik","kil","kim","kin","kio","kip","kiq","kir","kis","kit","kiu","kiv","kiw","kix","kiy","kiz","kja","kjb","kjc","kjd","kje","kjf","kjg","kjh","kji","kjj","kjk","kjl","kjm","kjn","kjo","kjp","kjq","kjr","kjs","kjt","kju","kjx","kjy","kjz","kka","kkb","kkc","kkd","kke","kkf","kkg","kkh","kki","kkj","kkk","kkl","kkm","kkn","kko","kkp","kkq","kkr","kks","kkt","kku","kkv","kkw","kkx","kky","kkz","kla","klb","klc","kld","kle","klf","klg","klh","kli","klj","klk","kll","klm","kln","klo","klp","klq","klr","kls","klt","klu","klv","klw","klx","kly","klz","kma","kmb","kmc","kmd","kme","kmf","kmg","kmh","kmi","kmj","kmk","kml","kmm","kmn","kmo","kmp","kmq","kmr","kms","kmt","kmu","kmv","kmw","kmx","kmy","kmz","kna","knb","knc","knd","kne","knf","kng","kni","knj","knk","knl","knm","knn","kno","knp","knq","knr","kns","knt","knu","knv","knw","knx","kny","knz","koa","koc","kod","koe","kof","kog","koh","koi","koj","kok","kol","kom","kon","koo","kop","koq","kor","kos","kot","kou","kov","kow","kox","koy","koz","kpa","kpb","kpc","kpd","kpe","kpf","kpg","kph","kpi","kpj","kpk","kpl","kpm","kpn","kpo","kpp","kpq","kpr","kps","kpt","kpu","kpv","kpw","kpx","kpy","kpz","kqa","kqb","kqc","kqd","kqe","kqf","kqg","kqh","kqi","kqj","kqk","kql","kqm","kqn","kqo","kqp","kqq","kqr","kqs","kqt","kqu","kqv","kqw","kqx","kqy","kqz","kra","krb","krc","krd","kre","krf","krh","kri","krj","krk","krl","krm","krn","krp","krr","krs","krt","kru","krv","krw","krx","kry","krz","ksa","ksb","ksc","ksd","kse","ksf","ksg","ksh","ksi","ksj","ksk","ksl","ksm","ksn","kso","ksp","ksq","ksr","kss","kst","ksu","ksv","ksw","ksx","ksy","ksz","kta","ktb","ktc","ktd","kte","ktf","ktg","kth","kti","ktj","ktk","ktl","ktm","ktn","kto","ktp","ktq","ktr","kts","ktt","ktu","ktv","ktw","ktx","kty","ktz","kua","kub","kuc","kud","kue","kuf","kug","kuh","kui","kuj","kuk","kul","kum","kun","kuo","kup","kuq","kur","kus","kut","kuu","kuv","kuw","kux","kuy","kuz","kva","kvb","kvc","kvd","kve","kvf","kvg","kvh","kvi","kvj","kvk","kvl","kvm","kvn","kvo","kvp","kvq","kvr","kvs","kvt","kvu","kvv","kvw","kvx","kvy","kvz","kwa","kwb","kwc","kwd","kwe","kwf","kwg","kwh","kwi","kwj","kwk","kwl","kwm","kwn","kwo","kwp","kwq","kwr","kws","kwt","kwu","kwv","kww","kwx","kwy","kwz","kxa","kxb","kxc","kxd","kxe","kxf","kxh","kxi","kxj","kxk","kxl","kxm","kxn","kxo","kxp","kxq","kxr","kxs","kxt","kxu","kxv","kxw","kxx","kxy","kxz","kya","kyb","kyc","kyd","kye","kyf","kyg","kyh","kyi","kyj","kyk","kyl","kym","kyn","kyo","kyp","kyq","kyr","kys","kyt","kyu","kyv","kyw","kyx","kyy","kyz","kza","kzb","kzc","kzd","kze","kzf","kzg","kzh","kzi","kzj","kzk","kzl","kzm","kzn","kzo","kzp","kzq","kzr","kzs","kzt","kzu","kzv","kzw","kzx","kzy","kzz","laa","lab","lac","lad","lae","laf","lag","lah","lai","laj","lak","lal","lam","lan","lao","lap","laq","lar","las","lat","lau","lav","law","lax","lay","laz","lba","lbb","lbc","lbe","lbf","lbg","lbi","lbj","lbk","lbl","lbm","lbn","lbo","lbq","lbr","lbs","lbt","lbu","lbv","lbw","lbx","lby","lbz","lcc","lcd","lce","lcf","lch","lcl","lcm","lcp","lcq","lcs","ldb","ldd","ldg","ldh","ldi","ldj","ldk","ldl","ldm","ldn","ldo","ldp","ldq","lea","leb","lec","led","lee","lef","leg","leh","lei","lej","lek","lel","lem","len","leo","lep","leq","ler","les","let","leu","lev","lew","lex","ley","lez","lfa","lfn","lga","lgb","lgg","lgh","lgi","lgk","lgl","lgm","lgn","lgq","lgr","lgt","lgu","lgz","lha","lhh","lhi","lhl","lhm","lhn","lhp","lhs","lht","lhu","lia","lib","lic","lid","lie","lif","lig","lih","lii","lij","lik","lil","lim","lin","lio","lip","liq","lir","lis","lit","liu","liv","liw","lix","liy","liz","lje","lji","ljl","ljp","lka","lkb","lkc","lkd","lke","lkh","lki","lkj","lkl","lkn","lko","lkr","lks","lkt","lky","lla","llb","llc","lld","lle","llf","llg","llh","lli","llk","lll","llm","lln","llo","llp","llq","lls","llu","llx","lma","lmb","lmc","lmd","lme","lmf","lmg","lmh","lmi","lmj","lmk","lml","lmm","lmn","lmo","lmp","lmq","lmr","lmu","lmv","lmw","lmx","lmy","lmz","lna","lnb","lnd","lng","lnh","lni","lnj","lnl","lnm","lnn","lno","lns","lnu","lnz","loa","lob","loc","loe","lof","log","loh","loi","loj","lok","lol","lom","lon","loo","lop","loq","lor","los","lot","lou","lov","low","lox","loy","loz","lpa","lpe","lpn","lpo","lpx","lra","lrc","lre","lrg","lri","lrk","lrl","lrm","lrn","lro","lrr","lrt","lrv","lrz","lsa","lsd","lse","lsg","lsh","lsi","lsl","lsm","lso","lsp","lsr","lss","lst","lsy","ltc","ltg","lti","ltn","lto","lts","ltu","ltz","lua","lub","luc","lud","lue","luf","lug","lui","luj","luk","lul","lum","lun","luo","lup","luq","lur","lus","lut","luu","luv","luw","luy","luz","lva","lvk","lvs","lvu","lwa","lwe","lwg","lwh","lwl","lwm","lwo","lwt","lww","lya","lyg","lyn","lzh","lzl","lzn","lzz","maa","mab","mad","mae","maf","mag","mah","mai","maj","mak","mal","mam","man","maq","mar","mas","mat","mau","mav","maw","max","maz","mba","mbb","mbc","mbd","mbe","mbf","mbh","mbi","mbj","mbk","mbl","mbm","mbn","mbo","mbp","mbq","mbr","mbs","mbt","mbu","mbv","mbw","mbx","mby","mbz","mca","mcb","mcc","mcd","mce","mcf","mcg","mch","mci","mcj","mck","mcl","mcm","mcn","mco","mcp","mcq","mcr","mcs","mct","mcu","mcv","mcw","mcx","mcy","mcz","mda","mdb","mdc","mdd","mde","mdf","mdg","mdh","mdi","mdj","mdk","mdl","mdm","mdn","mdp","mdq","mdr","mds","mdt","mdu","mdv","mdw","mdx","mdy","mdz","mea","meb","mec","med","mee","mef","meg","meh","mei","mej","mek","mel","mem","men","meo","mep","meq","mer","mes","met","meu","mev","mew","mey","mez","mfa","mfb","mfc","mfd","mfe","mff","mfg","mfh","mfi","mfj","mfk","mfl","mfm","mfn","mfo","mfp","mfq","mfr","mfs","mft","mfu","mfv","mfw","mfx","mfy","mfz","mga","mgb","mgc","mgd","mge","mgf","mgg","mgh","mgi","mgj","mgk","mgl","mgm","mgn","mgo","mgp","mgq","mgr","mgs","mgt","mgu","mgv","mgw","mgx","mgy","mgz","mha","mhb","mhc","mhd","mhe","mhf","mhg","mhh","mhi","mhj","mhk","mhl","mhm","mhn","mho","mhp","mhq","mhr","mhs","mht","mhu","mhw","mhx","mhy","mhz","mia","mib","mic","mid","mie","mif","mig","mih","mii","mij","mik","mil","mim","min","mio","mip","miq","mir","mis","mit","miu","miw","mix","miy","miz","mja","mjc","mjd","mje","mjg","mjh","mji","mjj","mjk","mjl","mjm","mjn","mjo","mjp","mjq","mjr","mjs","mjt","mju","mjv","mjw","mjx","mjy","mjz","mka","mkb","mkc","mkd","mke","mkf","mkg","mki","mkj","mkk","mkl","mkm","mkn","mko","mkp","mkq","mkr","mks","mkt","mku","mkv","mkw","mkx","mky","mkz","mla","mlb","mlc","mld","mle","mlf","mlg","mlh","mli","mlj","mlk","mll","mlm","mln","mlo","mlp","mlq","mlr","mls","mlt","mlu","mlv","mlw","mlx","mlz","mma","mmb","mmc","mmd","mme","mmf","mmg","mmh","mmi","mmj","mmk","mml","mmm","mmn","mmo","mmp","mmq","mmr","mmt","mmu","mmv","mmw","mmx","mmy","mmz","mna","mnb","mnc","mnd","mne","mnf","mng","mnh","mni","mnj","mnk","mnl","mnm","mnn","mnp","mnq","mnr","mns","mnt","mnu","mnv","mnw","mnx","mny","mnz","moa","moc","mod","moe","mog","moh","moi","moj","mok","mom","mon","moo","mop","moq","mor","mos","mot","mou","mov","mow","mox","moy","moz","mpa","mpb","mpc","mpd","mpe","mpg","mph","mpi","mpj","mpk","mpl","mpm","mpn","mpo","mpp","mpq","mpr","mps","mpt","mpu","mpv","mpw","mpx","mpy","mpz","mqa","mqb","mqc","mqe","mqf","mqg","mqh","mqi","mqj","mqk","mql","mqm","mqn","mqo","mqp","mqq","mqr","mqs","mqt","mqu","mqv","mqw","mqx","mqy","mqz","mra","mrb","mrc","mrd","mre","mrf","mrg","mrh","mri","mrj","mrk","mrl","mrm","mrn","mro","mrp","mrq","mrr","mrs","mrt","mru","mrv","mrw","mrx","mry","mrz","msa","msb","msc","msd","mse","msf","msg","msh","msi","msj","msk","msl","msm","msn","mso","msp","msq","msr","mss","msu","msv","msw","msx","msy","msz","mta","mtb","mtc","mtd","mte","mtf","mtg","mth","mti","mtj","mtk","mtl","mtm","mtn","mto","mtp","mtq","mtr","mts","mtt","mtu","mtv","mtw","mtx","mty","mua","mub","muc","mud","mue","mug","muh","mui","muj","muk","mul","mum","muo","mup","muq","mur","mus","mut","muu","muv","mux","muy","muz","mva","mvb","mvd","mve","mvf","mvg","mvh","mvi","mvk","mvl","mvm","mvn","mvo","mvp","mvq","mvr","mvs","mvt","mvu","mvv","mvw","mvx","mvy","mvz","mwa","mwb","mwc","mwd","mwe","mwf","mwg","mwh","mwi","mwj","mwk","mwl","mwm","mwn","mwo","mwp","mwq","mwr","mws","mwt","mwu","mwv","mww","mwx","mwy","mwz","mxa","mxb","mxc","mxd","mxe","mxf","mxg","mxh","mxi","mxj","mxk","mxl","mxm","mxn","mxo","mxp","mxq","mxr","mxs","mxt","mxu","mxv","mxw","mxx","mxy","mxz","mya","myb","myc","myd","mye","myf","myg","myh","myi","myj","myk","myl","mym","myo","myp","myq","myr","mys","myu","myv","myw","myx","myy","myz","mza","mzb","mzc","mzd","mze","mzg","mzh","mzi","mzj","mzk","mzl","mzm","mzn","mzo","mzp","mzq","mzr","mzs","mzt","mzu","mzv","mzw","mzx","mzy","mzz","naa","nab","nac","nad","nae","naf","nag","naj","nak","nal","nam","nan","nao","nap","naq","nar","nas","nat","nau","nav","naw","nax","nay","naz","nba","nbb","nbc","nbd","nbe","nbf","nbg","nbh","nbi","nbj","nbk","nbl","nbm","nbn","nbo","nbp","nbq","nbr","nbs","nbt","nbu","nbv","nbw","nbx","nby","nca","ncb","ncc","ncd","nce","ncf","ncg","nch","nci","ncj","nck","ncl","ncm","ncn","nco","ncp","ncr","ncs","nct","ncu","ncx","ncz","nda","ndb","ndc","ndd","nde","ndf","ndg","ndh","ndi","ndj","ndk","ndl","ndm","ndn","ndo","ndp","ndq","ndr","nds","ndt","ndu","ndv","ndw","ndx","ndy","ndz","nea","neb","nec","ned","nee","nef","neg","neh","nei","nej","nek","nem","nen","neo","nep","neq","ner","nes","net","nev","new","nex","ney","nez","nfa","nfd","nfl","nfr","nfu","nga","ngb","ngc","ngd","nge","ngg","ngh","ngi","ngj","ngk","ngl","ngm","ngn","ngo","ngp","ngq","ngr","ngs","ngt","ngu","ngv","ngw","ngx","ngy","ngz","nha","nhb","nhc","nhd","nhe","nhf","nhg","nhh","nhi","nhk","nhm","nhn","nho","nhp","nhq","nhr","nht","nhu","nhv","nhw","nhx","nhy","nhz","nia","nib","nid","nie","nif","nig","nih","nii","nij","nik","nil","nim","nin","nio","niq","nir","nis","nit","niu","niv","niw","nix","niy","niz","nja","njb","njd","njh","nji","njj","njl","njm","njn","njo","njr","njs","njt","nju","njx","njy","nka","nkb","nkc","nkd","nke","nkf","nkg","nkh","nki","nkj","nkk","nkm","nkn","nko","nkp","nkq","nkr","nks","nkt","nku","nkv","nkw","nkx","nkz","nla","nlc","nld","nle","nlg","nli","nlj","nlk","nll","nln","nlo","nlr","nlu","nlv","nlx","nly","nlz","nma","nmb","nmc","nmd","nme","nmf","nmg","nmh","nmi","nmj","nmk","nml","nmm","nmn","nmo","nmp","nmq","nmr","nms","nmt","nmu","nmv","nmw","nmx","nmy","nmz","nna","nnb","nnc","nnd","nne","nnf","nng","nnh","nni","nnj","nnk","nnl","nnm","nnn","nno","nnp","nnq","nnr","nns","nnt","nnu","nnv","nnw","nnx","nny","nnz","noa","nob","noc","nod","noe","nof","nog","noh","noi","noj","nok","nom","non","noo","nop","noq","nor","nos","not","nou","nov","now","noy","noz","npa","npb","nph","npl","npn","npo","nps","npu","npy","nqg","nqk","nqm","nqn","nqo","nra","nrb","nrc","nre","nrg","nri","nrl","nrm","nrn","nrp","nrr","nrt","nrx","nrz","nsa","nsc","nsd","nse","nsg","nsh","nsi","nsk","nsl","nsm","nsn","nso","nsp","nsq","nsr","nss","nst","nsu","nsv","nsw","nsx","nsy","nsz","nte","nti","ntj","ntk","ntm","nto","ntp","ntr","nts","ntu","ntw","nty","ntz","nua","nuc","nud","nue","nuf","nug","nuh","nui","nuj","nul","num","nun","nuo","nup","nuq","nur","nus","nut","nuu","nuv","nuw","nux","nuy","nuz","nvh","nvm","nwa","nwb","nwc","nwe","nwi","nwm","nwr","nwx","nwy","nxa","nxd","nxe","nxg","nxi","nxl","nxm","nxn","nxr","nxu","nxx","nya","nyb","nyc","nyd","nye","nyf","nyg","nyh","nyi","nyj","nyk","nyl","nym","nyn","nyo","nyp","nyq","nyr","nys","nyt","nyu","nyv","nyw","nyx","nyy","nza","nzb","nzi","nzk","nzm","nzs","nzu","nzy","nzz","oaa","oac","oar","oav","obi","obk","obl","obm","obo","obr","obt","obu","oca","och","oci","oco","ocu","oda","odk","odt","odu","ofo","ofs","ofu","ogb","ogc","oge","ogg","ogo","ogu","oht","ohu","oia","oin","ojb","ojc","ojg","oji","ojp","ojs","ojv","ojw","oka","okb","okd","oke","okh","oki","okj","okk","okl","okm","okn","oko","okr","oks","oku","okv","okx","ola","old","ole","olm","olo","olr","oma","omb","omc","ome","omg","omi","omk","oml","omn","omo","omp","omr","omt","omu","omw","omx","ona","onb","one","ong","oni","onj","onk","onn","ono","onp","onr","ons","ont","onu","onw","onx","ood","oog","oon","oor","oos","opa","opk","opm","opo","opt","opy","ora","orc","ore","org","orh","ori","orm","orn","oro","orr","ors","ort","oru","orv","orw","orx","orz","osa","osc","osi","oso","osp","oss","ost","osu","osx","ota","otb","otd","ote","oti","otk","otl","otm","otn","otq","otr","ots","ott","otu","otw","otx","oty","otz","oua","oub","oue","oui","oum","oun","owi","owl","oyb","oyd","oym","oyy","ozm","pab","pac","pad","pae","paf","pag","pah","pai","pak","pal","pam","pan","pao","pap","paq","par","pas","pat","pau","pav","paw","pax","pay","paz","pbb","pbc","pbe","pbf","pbg","pbh","pbi","pbl","pbn","pbo","pbp","pbr","pbs","pbt","pbu","pbv","pby","pbz","pca","pcb","pcc","pcd","pce","pcf","pcg","pch","pci","pcj","pck","pcl","pcm","pcn","pcp","pcr","pcw","pda","pdc","pdi","pdn","pdo","pdt","pdu","pea","peb","ped","pee","pef","peg","peh","pei","pej","pek","pel","pem","peo","pep","peq","pes","pev","pex","pey","pez","pfa","pfe","pfl","pga","pgg","pgi","pgk","pgn","pgs","pgu","pgy","pha","phd","phg","phh","phk","phl","phm","phn","pho","phq","phr","pht","phu","phv","phw","pia","pib","pic","pid","pie","pif","pig","pih","pii","pij","pil","pim","pin","pio","pip","pir","pis","pit","piu","piv","piw","pix","piy","piz","pjt","pka","pkb","pkc","pkg","pkh","pkn","pko","pkp","pkr","pks","pkt","pku","pla","plb","plc","pld","ple","plg","plh","pli","plj","plk","pll","pln","plo","plp","plq","plr","pls","plt","plu","plv","plw","ply","plz","pma","pmb","pmc","pme","pmf","pmh","pmi","pmj","pmk","pml","pmm","pmn","pmo","pmq","pmr","pms","pmt","pmu","pmw","pmx","pmy","pmz","pna","pnb","pnc","pne","png","pnh","pni","pnm","pnn","pno","pnp","pnq","pnr","pns","pnt","pnu","pnv","pnw","pnx","pny","pnz","poc","pod","poe","pof","pog","poh","poi","pok","pol","pom","pon","poo","pop","poq","por","pos","pot","pov","pow","pox","poy","ppa","ppe","ppi","ppk","ppl","ppm","ppn","ppo","ppp","ppq","ppr","pps","ppt","ppu","pqa","pqm","prb","prc","prd","pre","prf","prg","prh","pri","prk","prl","prm","prn","pro","prp","prq","prr","prs","prt","pru","prw","prx","pry","prz","psa","psc","psd","pse","psg","psh","psi","psl","psm","psn","pso","psp","psq","psr","pss","pst","psu","psw","psy","pta","pth","pti","ptn","pto","ptp","ptr","ptt","ptu","ptv","ptw","pty","pua","pub","puc","pud","pue","puf","pug","pui","puj","puk","pum","puo","pup","puq","pur","pus","put","puu","puw","pux","puy","puz","pwa","pwb","pwg","pwm","pwn","pwo","pwr","pww","pxm","pye","pym","pyn","pys","pyu","pyx","pyy","pzn","qua","qub","quc","qud","que","quf","qug","quh","qui","quk","qul","qum","qun","qup","quq","qur","qus","quv","quw","qux","quy","quz","qva","qvc","qve","qvh","qvi","qvj","qvl","qvm","qvn","qvo","qvp","qvs","qvw","qvy","qvz","qwa","qwc","qwh","qwm","qws","qwt","qxa","qxc","qxh","qxl","qxn","qxo","qxp","qxq","qxr","qxs","qxt","qxu","qxw","qya","qyp","raa","rab","rac","rad","raf","rag","rah","rai","raj","rak","ral","ram","ran","rao","rap","raq","rar","ras","rat","rau","rav","raw","rax","ray","raz","rbb","rbk","rbl","rcf","rdb","rea","reb","ree","reg","rei","rej","rel","rem","ren","rer","res","ret","rey","rga","rge","rgk","rgn","rgr","rgs","rgu","rhg","rhp","ria","rie","rif","ril","rim","rin","rir","rit","riu","rjg","rji","rjs","rka","rkb","rkh","rki","rkm","rkt","rma","rmb","rmc","rmd","rme","rmf","rmg","rmh","rmi","rmk","rml","rmm","rmn","rmo","rmp","rmq","rms","rmt","rmu","rmv","rmw","rmx","rmy","rmz","rna","rnd","rng","rnl","rnn","rnp","rnw","rob","roc","rod","roe","rof","rog","roh","rol","rom","ron","roo","rop","ror","rou","row","rpn","rpt","rri","rro","rsb","rsi","rsl","rth","rtm","rtw","rub","ruc","rue","ruf","rug","ruh","rui","ruk","run","ruo","rup","ruq","rus","rut","ruu","ruy","ruz","rwa","rwk","rwm","rwo","rwr","ryn","rys","ryu","saa","sab","sac","sad","sae","saf","sag","sah","saj","sak","sam","san","sao","sap","saq","sar","sas","sat","sau","sav","saw","sax","say","saz","sba","sbb","sbc","sbd","sbe","sbf","sbg","sbh","sbi","sbj","sbk","sbl","sbm","sbn","sbo","sbp","sbq","sbr","sbs","sbt","sbu","sbv","sbw","sbx","sby","sbz","sca","scb","sce","scf","scg","sch","sci","sck","scl","scn","sco","scp","scq","scs","scu","scv","scw","scx","sda","sdb","sdc","sde","sdf","sdg","sdh","sdj","sdk","sdl","sdm","sdn","sdo","sdp","sdr","sds","sdt","sdu","sdx","sdz","sea","seb","sec","sed","see","sef","seg","seh","sei","sej","sek","sel","sen","seo","sep","seq","ser","ses","set","seu","sev","sew","sey","sez","sfb","sfm","sfs","sfw","sga","sgb","sgc","sgd","sge","sgg","sgh","sgi","sgk","sgm","sgo","sgp","sgr","sgs","sgt","sgu","sgw","sgx","sgy","sgz","sha","shb","shc","shd","she","shg","shh","shi","shj","shk","shl","shm","shn","sho","shp","shq","shr","shs","sht","shu","shv","shw","shx","shy","shz","sia","sib","sid","sie","sif","sig","sih","sii","sij","sik","sil","sim","sin","sip","siq","sir","sis","siu","siv","siw","six","siy","siz","sja","sjb","sjd","sje","sjg","sjk","sjl","sjm","sjn","sjo","sjp","sjr","sjs","sjt","sju","sjw","ska","skb","skc","skd","ske","skf","skg","skh","ski","skj","skk","skm","skn","sko","skp","skq","skr","sks","skt","sku","skv","skw","skx","sky","skz","slc","sld","sle","slf","slg","slh","sli","slj","slk","sll","slm","sln","slp","slq","slr","sls","slt","slu","slv","slw","slx","sly","slz","sma","smb","smc","smd","sme","smf","smg","smh","smj","smk","sml","smm","smn","smo","smp","smq","smr","sms","smt","smu","smv","smw","smx","smy","smz","sna","snb","snc","snd","sne","snf","sng","snh","sni","snj","snk","snl","snm","snn","sno","snp","snq","snr","sns","snu","snv","snw","snx","sny","snz","soa","sob","soc","sod","soe","sog","soh","soi","soj","sok","sol","som","soo","sop","soq","sor","sos","sot","sou","sov","sow","sox","soy","soz","spa","spb","spc","spd","spe","spg","spi","spk","spl","spm","spo","spp","spq","spr","sps","spt","spu","spx","spy","sqa","sqh","sqi","sqm","sqn","sqo","sqq","sqr","sqs","sqt","squ","sra","srb","src","srd","sre","srf","srg","srh","sri","srk","srl","srm","srn","sro","srp","srq","srr","srs","srt","sru","srv","srw","srx","sry","srz","ssb","ssc","ssd","sse","ssf","ssg","ssh","ssi","ssj","ssk","ssl","ssm","ssn","sso","ssp","ssq","ssr","sss","sst","ssu","ssv","ssw","ssx","ssy","ssz","sta","stb","std","ste","stf","stg","sth","sti","stj","stk","stl","stm","stn","sto","stp","stq","str","sts","stt","stu","stv","stw","sua","sub","suc","sue","sug","sui","suj","suk","sun","suq","sur","sus","sut","suv","suw","sux","suy","suz","sva","svb","svc","sve","svk","svr","svs","svx","swa","swb","swc","swe","swf","swg","swh","swi","swj","swk","swl","swm","swn","swo","swp","swq","swr","sws","swt","swu","swv","sww","swx","swy","sxb","sxc","sxe","sxg","sxk","sxl","sxm","sxn","sxo","sxr","sxs","sxu","sxw","sya","syb","syc","syi","syk","syl","sym","syn","syo","syr","sys","syw","syy","sza","szb","szc","szd","sze","szg","szl","szn","szp","szv","szw","taa","tab","tac","tad","tae","taf","tag","tah","taj","tak","tal","tam","tan","tao","tap","taq","tar","tas","tat","tau","tav","taw","tax","tay","taz","tba","tbb","tbc","tbd","tbe","tbf","tbg","tbh","tbi","tbj","tbk","tbl","tbm","tbn","tbo","tbp","tbr","tbs","tbt","tbu","tbv","tbw","tbx","tby","tbz","tca","tcb","tcc","tcd","tce","tcf","tcg","tch","tci","tck","tcl","tcm","tcn","tco","tcp","tcq","tcs","tct","tcu","tcw","tcx","tcy","tcz","tda","tdb","tdc","tdd","tde","tdf","tdg","tdh","tdi","tdj","tdk","tdl","tdn","tdo","tdq","tdr","tds","tdt","tdu","tdv","tdx","tdy","tea","teb","tec","ted","tee","tef","teg","teh","tei","tek","tel","tem","ten","teo","tep","teq","ter","tes","tet","teu","tev","tew","tex","tey","tfi","tfn","tfo","tfr","tft","tga","tgb","tgc","tgd","tge","tgf","tgg","tgh","tgi","tgk","tgl","tgn","tgo","tgp","tgq","tgr","tgs","tgt","tgu","tgv","tgw","tgx","tgy","tha","thc","thd","the","thf","thh","thi","thk","thl","thm","thn","thp","thq","thr","ths","tht","thu","thv","thw","thx","thy","thz","tia","tic","tid","tie","tif","tig","tih","tii","tij","tik","til","tim","tin","tio","tip","tiq","tir","tis","tit","tiu","tiv","tiw","tix","tiy","tiz","tja","tjg","tji","tjm","tjn","tjo","tjs","tju","tka","tkb","tkd","tke","tkf","tkk","tkl","tkm","tkn","tkp","tkq","tkr","tks","tkt","tku","tkw","tkx","tkz","tla","tlb","tlc","tld","tlf","tlg","tlh","tli","tlj","tlk","tll","tlm","tln","tlo","tlp","tlq","tlr","tls","tlt","tlu","tlv","tlw","tlx","tly","tma","tmb","tmc","tmd","tme","tmf","tmg","tmh","tmi","tmj","tmk","tml","tmm","tmn","tmo","tmp","tmq","tmr","tms","tmt","tmu","tmv","tmw","tmy","tmz","tna","tnb","tnc","tnd","tne","tng","tnh","tni","tnk","tnl","tnm","tnn","tno","tnp","tnq","tnr","tns","tnt","tnu","tnv","tnw","tnx","tny","tnz","tob","toc","tod","toe","tof","tog","toh","toi","toj","tol","tom","ton","too","top","toq","tor","tos","tou","tov","tow","tox","toy","toz","tpa","tpc","tpe","tpf","tpg","tpi","tpj","tpk","tpl","tpm","tpn","tpo","tpp","tpq","tpr","tpt","tpu","tpv","tpw","tpx","tpy","tpz","tqb","tql","tqm","tqn","tqo","tqp","tqq","tqr","tqt","tqu","tqw","tra","trb","trc","trd","tre","trf","trg","trh","tri","trj","trl","trm","trn","tro","trp","trq","trr","trs","trt","tru","trv","trw","trx","try","trz","tsa","tsb","tsc","tsd","tse","tsf","tsg","tsh","tsi","tsj","tsk","tsl","tsm","tsn","tso","tsp","tsq","tsr","tss","tsu","tsv","tsw","tsx","tsy","tsz","tta","ttb","ttc","ttd","tte","ttf","ttg","tth","tti","ttj","ttk","ttl","ttm","ttn","tto","ttp","ttq","ttr","tts","ttt","ttu","ttv","ttw","tty","ttz","tua","tub","tuc","tud","tue","tuf","tug","tuh","tui","tuj","tuk","tul","tum","tun","tuo","tuq","tur","tus","tuu","tuv","tux","tuy","tuz","tva","tvd","tve","tvk","tvl","tvm","tvn","tvo","tvs","tvt","tvw","tvy","twa","twb","twc","twd","twe","twf","twg","twh","twi","twl","twm","twn","two","twp","twq","twr","twt","twu","tww","twx","twy","txa","txb","txc","txe","txg","txh","txi","txm","txn","txo","txq","txr","txs","txt","txu","txx","txy","tya","tye","tyh","tyi","tyj","tyl","tyn","typ","tyr","tys","tyt","tyu","tyv","tyx","tyz","tza","tzh","tzj","tzm","tzn","tzo","tzx","uam","uan","uar","uba","ubi","ubl","ubr","ubu","uby","uda","ude","udg","udi","udj","udl","udm","udu","ues","ufi","uga","ugb","uge","ugn","ugo","ugy","uha","uhn","uig","uis","uiv","uji","uka","ukg","ukh","ukl","ukp","ukq","ukr","uks","uku","ukw","ula","ulb","ulc","ulf","uli","ulk","ull","ulm","uln","ulu","ulw","uma","umb","umc","umd","umg","umi","umm","umn","umo","ump","umr","ums","umu","una","und","une","ung","unk","unm","unp","unr","unx","unz","uok","upi","upv","ura","urb","urc","urd","ure","urf","urg","urh","uri","urk","url","urm","urn","uro","urp","urr","urt","uru","urv","urw","urx","ury","urz","usa","ush","usi","usk","usp","usu","uta","ute","utp","utr","utu","uum","uun","uur","uuu","uve","uvh","uvl","uwa","uya","uzb","uzn","uzs","vaa","vae","vaf","vag","vah","vai","vaj","val","vam","van","vao","vap","var","vas","vau","vav","vay","vbb","vbk","vec","ved","vel","vem","ven","veo","vep","ver","vgr","vgt","vic","vid","vie","vif","vig","vil","vin","vis","vit","viv","vka","vki","vkj","vkk","vkl","vkm","vko","vkp","vkt","vku","vlp","vls","vma","vmb","vmc","vmd","vme","vmf","vmg","vmh","vmi","vmj","vmk","vml","vmm","vmp","vmq","vmr","vms","vmu","vmv","vmw","vmx","vmy","vmz","vnk","vnm","vnp","vol","vor","vot","vra","vro","vrs","vrt","vsi","vsl","vsv","vto","vum","vun","vut","vwa","waa","wab","wac","wad","wae","waf","wag","wah","wai","waj","wal","wam","wan","wao","wap","waq","war","was","wat","wau","wav","waw","wax","way","waz","wba","wbb","wbe","wbf","wbh","wbi","wbj","wbk","wbl","wbm","wbp","wbq","wbr","wbt","wbv","wbw","wca","wci","wdd","wdg","wdj","wdu","wea","wec","wed","weh","wei","wem","weo","wep","wer","wes","wet","weu","wew","wfg","wga","wgb","wgg","wgi","wgo","wgy","wha","whg","whk","whu","wib","wic","wie","wif","wig","wih","wii","wij","wik","wil","wim","win","wir","wit","wiu","wiv","wiw","wiy","wja","wji","wka","wkb","wkd","wkl","wku","wkw","wla","wlc","wle","wlg","wli","wlk","wll","wlm","wln","wlo","wlr","wls","wlu","wlv","wlw","wlx","wly","wma","wmb","wmc","wmd","wme","wmh","wmi","wmm","wmn","wmo","wms","wmt","wmw","wmx","wnb","wnc","wnd","wne","wng","wni","wnk","wnm","wno","wnp","wnu","woa","wob","woc","wod","woe","wof","wog","woi","wok","wol","wom","won","woo","wor","wos","wow","woy","wpc","wra","wrb","wrd","wrg","wrh","wri","wrl","wrm","wrn","wrp","wrr","wrs","wru","wrv","wrw","wrx","wry","wrz","wsa","wsi","wsk","wsr","wss","wsu","wsv","wtf","wti","wtk","wtm","wtw","wua","wub","wud","wuh","wul","wum","wun","wur","wut","wuu","wuv","wux","wuy","wwa","wwo","wwr","www","wxa","wya","wyb","wym","wyr","wyy","xaa","xab","xac","xad","xae","xag","xai","xal","xam","xan","xao","xap","xaq","xar","xas","xat","xau","xav","xaw","xay","xba","xbb","xbc","xbi","xbm","xbn","xbo","xbr","xbw","xbx","xcb","xcc","xce","xcg","xch","xcl","xcm","xcn","xco","xcr","xct","xcu","xcv","xcw","xcy","xdc","xdm","xdy","xeb","xed","xeg","xel","xem","xep","xer","xes","xet","xeu","xfa","xga","xgf","xgl","xgr","xha","xhc","xhd","xhe","xho","xhr","xht","xhu","xhv","xia","xib","xii","xil","xin","xip","xir","xiv","xiy","xka","xkb","xkc","xkd","xke","xkf","xkg","xkh","xki","xkj","xkk","xkl","xkn","xko","xkp","xkq","xkr","xks","xkt","xku","xkv","xkw","xkx","xky","xkz","xla","xlb","xlc","xld","xle","xlg","xli","xln","xlo","xlp","xls","xlu","xly","xma","xmb","xmc","xmd","xme","xmf","xmg","xmh","xmj","xmk","xml","xmm","xmn","xmo","xmp","xmq","xmr","xms","xmt","xmu","xmv","xmw","xmx","xmy","xmz","xna","xnb","xng","xnh","xnn","xno","xnr","xns","xnt","xoc","xod","xog","xoi","xok","xom","xon","xoo","xop","xor","xow","xpc","xpe","xpg","xpi","xpk","xpm","xpn","xpo","xpp","xpq","xpr","xps","xpu","xpy","xqa","xqt","xra","xrb","xre","xri","xrm","xrn","xrr","xrt","xru","xrw","xsa","xsb","xsc","xsd","xse","xsh","xsi","xsj","xsl","xsm","xsn","xso","xsp","xsq","xsr","xss","xsu","xsv","xsy","xta","xtb","xtc","xtd","xte","xtg","xti","xtj","xtl","xtm","xtn","xto","xtp","xtq","xtr","xts","xtt","xtu","xtw","xty","xtz","xua","xub","xug","xuj","xum","xuo","xup","xur","xut","xuu","xve","xvi","xvn","xvo","xvs","xwa","xwc","xwe","xwg","xwl","xwo","xwr","xxb","xxk","xxr","xxt","xyl","xzh","xzm","xzp","yaa","yab","yac","yad","yae","yaf","yag","yah","yai","yaj","yak","yal","yam","yan","yao","yap","yaq","yar","yas","yat","yau","yav","yaw","yax","yay","yaz","yba","ybb","ybd","ybe","ybh","ybi","ybj","ybk","ybl","ybm","ybn","ybo","ybx","yby","ych","ycl","ycn","ycp","ydd","yde","ydg","ydk","yds","yea","yec","yee","yei","yej","yel","yen","yer","yes","yet","yeu","yev","yey","ygl","ygm","ygp","ygr","ygw","yha","yhd","yhl","yia","yid","yif","yig","yih","yii","yij","yik","yil","yim","yin","yip","yiq","yir","yis","yit","yiu","yiv","yix","yiy","yiz","yka","ykg","yki","ykk","ykl","ykm","yko","ykr","ykt","yky","yla","ylb","yle","ylg","yli","yll","ylm","yln","ylo","ylr","ylu","yly","yma","ymb","ymc","ymd","yme","ymg","ymh","ymi","ymk","yml","ymm","ymn","ymo","ymp","ymq","ymr","yms","ymt","ymx","ymz","yna","ynd","yne","yng","ynh","ynk","ynl","ynn","yno","yns","ynu","yob","yog","yoi","yok","yol","yom","yon","yor","yos","yox","yoy","ypa","ypb","ypg","yph","ypm","ypn","ypo","ypp","ypz","yra","yrb","yre","yri","yrk","yrl","yrn","yrs","yrw","ysc","ysd","ysl","ysn","yso","ysp","ysr","yss","ysy","yta","ytl","ytp","ytw","yua","yub","yuc","yud","yue","yuf","yug","yui","yuj","yuk","yul","yum","yun","yup","yuq","yur","yut","yuu","yuw","yux","yuy","yuz","yva","yvt","ywa","ywl","ywn","ywq","ywr","ywt","ywu","yww","yyu","yyz","yzg","yzk","zaa","zab","zac","zad","zae","zaf","zag","zah","zai","zaj","zak","zal","zam","zao","zap","zaq","zar","zas","zat","zau","zav","zaw","zax","zay","zaz","zbc","zbe","zbl","zbt","zbw","zca","zch","zdj","zea","zeg","zeh","zen","zga","zgb","zgm","zgn","zgr","zha","zhb","zhd","zhi","zhn","zho","zhw","zia","zib","zik","zim","zin","zir","ziw","ziz","zka","zkb","zkg","zkh","zkk","zko","zkp","zkr","zkt","zku","zkv","zkz","zlj","zlm","zln","zlq","zma","zmb","zmc","zmd","zme","zmf","zmg","zmh","zmi","zmj","zmk","zml","zmm","zmn","zmo","zmp","zmq","zmr","zms","zmt","zmu","zmv","zmw","zmx","zmy","zmz","zna","zne","zng","znk","zns","zoc","zoh","zom","zoo","zoq","zor","zos","zpa","zpb","zpc","zpd","zpe","zpf","zpg","zph","zpi","zpj","zpk","zpl","zpm","zpn","zpo","zpp","zpq","zpr","zps","zpt","zpu","zpv","zpw","zpx","zpy","zpz","zqe","zra","zrg","zrn","zro","zrp","zrs","zsa","zsk","zsl","zsm","zsr","zsu","zte","ztg","ztl","ztm","ztn","ztp","ztq","zts","ztt","ztu","ztx","zty","zua","zuh","zul","zum","zun","zuy","zwa","zxx","zyb","zyg","zyj","zyn","zyp","zza","zzj"));

    private static String[] split(String line) {
        int tabcount = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '\t') {
                tabcount++;
            }
        }
        final String[] result = new String[tabcount + 1];
        int i = 0;
        boolean prevTokenWasSeparator = false;
        for (StringTokenizer st = new StringTokenizer(line, "\t", true); st.hasMoreElements();) {
            final String token = st.nextToken();
            if (token.equals("\t")) {
                if (prevTokenWasSeparator) {
                    result[i++] = "";
                }
                prevTokenWasSeparator = true;
            } else {
                result[i++] = token;
                prevTokenWasSeparator = false;
            }
        }
        if (prevTokenWasSeparator) {
            result[i++] = "";
        }
        if (i < result.length) {
            throw new RuntimeException("Line " + line + ": Expected " + result.length + " tokens, found only " + i);
        }
        return result;
    }

    public void addLine(String line, IndexWriter writer) throws IOException {
        final String[] tokens = split(line);
        final int index = Integer.parseInt(tokens[0]);
        if (MiscUtils.isBlank(tokens[1])) {
            System.out.println("Sentence #" + index + " language is missing - ignoring");
            return;
        }
        if (!ISO_639_3_CODES.contains(tokens[1])) {
            System.out.println("Sentence #"+index+": Invalid language code: " + tokens[1] + ", ignoring");
            return;
        }
        if (tokens[1].equals(JAPAN_ISO_639_3_CODE)) {
            sentences.put(index, new Sentences(tokens[2]));
        } else {
            nonJpSentences.add(new Sentence(index, tokens[1], tokens[2]));
        }
    }

    public void onFinish(IndexWriter writer) throws IOException {
        postprocessNonJpSentences(parseLinks());
        parseBLines();
        writeLucene(writer);
    }

    private void writeLucene(IndexWriter writer) throws IOException {
        final Set<String> languages = new HashSet<String>();
        int sc = 0;
        for (final Entry<Integer, Sentences> e : sentences.entrySet()) {
            if (e.getValue().bLine == null) {
                System.out.println("Missing B-Line for sentence " + e.getKey() + ", skipping");
            } else {
                languages.addAll(e.getValue().sentences.keySet());
                final Document doc = new Document();
                doc.add(new Field("japanese", e.getValue().japanese, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("translations", e.getValue().getSentences(), Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("jp-deinflected", e.getValue().bLine.dictionaryFormWordList, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("kana", CompressionTools.compressString(e.getValue().bLine.kana), Field.Store.YES));
                writer.addDocument(doc);
                sc++;
            }
        }
        System.out.println("Lucene indexed " + sc + " example sentences");
        System.out.println("Tatoeba contains sentences in the following languages: " + languages);
    }

    private Map<Integer, Integer> parseLinks() throws IOException {
        System.out.println("Parsing Sentence Links file");
        // maps source link ID to a list of links.
        Map<Integer, Set<Integer>> links = new HashMap<Integer, Set<Integer>>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("links.csv"), cfg.encoding));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String[] tokens = split(line);
                final int index1 = Integer.parseInt(tokens[0]);
                if (index1 < 0) {
                    throw new RuntimeException("Error parsing " + tokens[0]);
                }
                final int index2 = Integer.parseInt(tokens[1]);
                if (index2 < 0) {
                    throw new RuntimeException("Error parsing " + tokens[1]);
                }
                if (index1 != index2) {
                    Set<Integer> list = links.get(index1);
                    if (list == null) {
                        list = new HashSet<Integer>();
                        links.put(index1, list);
                    }
                    list.add(index2);
                    list = links.get(index2);
                    if (list == null) {
                        list = new HashSet<Integer>();
                        links.put(index2, list);
                    }
                    list.add(index1);
                }
            }
        } finally {
            MiscUtils.closeQuietly(reader);
        }
        System.out.println("Constructing sentence sets, please wait");
        final List<Set<Integer>> graphs = new ArrayList<Set<Integer>>(200000);
        while (!links.isEmpty()) {
            final Set<Integer> graph = findCompleteGraph(links);
            if (graph.isEmpty()) {
                throw new RuntimeException("Empty graph");
            }
            graphs.add(graph);
            for (Integer i : graph) {
                links.remove(i);
            }
            if (graphs.size() % 10000 == 0) {
                links = new HashMap<Integer, Set<Integer>>(links);  // force re-hash to increase performance
            }
        }
        System.out.println(graphs.size() + " example sentences found");
        int skipped = 0;
        // construct a map which maps non-JP-lang-sentence ID to a JP-lang-sentence_id
        final Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (Set<Integer> graph : graphs) {
            final Integer jpSentenceId = findJpSentenceId(graph);
            if (jpSentenceId == null) {
                System.out.println("No JP ID in graph " + graph);
                skipped++;
            } else {
                for (Integer i : graph) {
                    if (i.intValue() != jpSentenceId.intValue()) {
                        final Integer prev = result.put(i, jpSentenceId);
                        if (prev != null) {
                            throw new RuntimeException(i + "=>" + jpSentenceId + " defined multiple times, prev: " + i + "=>" + prev);
                        }
                    }
                }
            }
        }
        System.out.println("Link analysis complete; found " + skipped + " sentence graphs without Japanese examples");
        return result;
    }

    private Integer findJpSentenceId(Collection<Integer> ids) {
        for (Integer i : ids) {
            if (sentences.containsKey(i)) {
                return i;
            }
        }
        return null;
    }

    private Set<Integer> findCompleteGraph(Map<Integer, Set<Integer>> graph) {
        final Set<Integer> result = new HashSet<Integer>();
        final Integer index = graph.keySet().iterator().next();
        findCompleteGraph(graph, result, index);
        return result;
    }

    private void findCompleteGraph(Map<Integer, Set<Integer>> graph, Set<Integer> result, Integer index) {
        if (result.contains(index)) {
            return;
        }
        result.add(index);
        for (final Integer i : graph.get(index)) {
            findCompleteGraph(graph, result, i);
        }
    }

    private void postprocessNonJpSentences(Map<Integer, Integer> links) {
        System.out.println("Post-processing non-JP sentences");
        for (Sentence s : nonJpSentences) {
            final Integer jpId = links.get(s.index);
            if (jpId == null) {
                continue;
            }
            final Sentences se = sentences.get(jpId);
            if (se == null) {
                throw new RuntimeException("No Sentences object for #" + jpId);
            }
            se.sentences.put(s.lang, s.sentence);
        }
        nonJpSentences.clear();
    }

    private static class Sentence {

        public final int index;
        public final String lang;
        public final String sentence;

        public Sentence(int index, String lang, String sentence) {
            this.index = index;
            this.lang = lang;
            this.sentence = sentence;
        }

        @Override
        public String toString() {
            return "Sentence{" + "#" + index + " " + lang + " " + sentence + '}';
        }
    }

    private static class Sentences {

        public Sentences(String japanese) {
            this.japanese = japanese;
        }
        /**
         * The japanese original sentence contents.
         */
        public final String japanese;
        public BLineParser bLine;
        /**
         * Maps ISO 639-3 language code to the sentence contents.
         */
        public Map<String, String> sentences = new HashMap<String, String>();

        public String getEngSentence() {
            return sentences.get("eng");
        }

        public String getSentences() {
            final ListBuilder lb = new ListBuilder("\n");
            for (final Entry<String, String> e : sentences.entrySet()) {
                lb.add(e.getKey() + ": " + e.getValue());
            }
            return lb.toString();
        }
    }

    public TatoebaParser(Config cfg) throws IOException {
        edict = Edict.loadFromDefaultLocation(null);
        this.cfg = cfg;
    }

    private void parseBLines() throws IOException {
        System.out.println("Parsing B-Lines");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("jpn_indices.csv"), cfg.encoding));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                final String[] tokens = split(line);
                final int jpSentenceId = Integer.parseInt(tokens[0]);
                final Sentences sentence = sentences.get(jpSentenceId);
                final BLineParser bLine = new BLineParser(edict, sentence.japanese, tokens[2]);
                if (sentence.bLine != null) {
                    System.out.println("WARNING: Duplicite bLine for " + jpSentenceId + ": first one: " + sentence.bLine + ", second one: " + bLine + ", ignoring second one");
                } else {
                    sentence.bLine = bLine;
                }
            }
        } finally {
            MiscUtils.closeQuietly(reader);
        }
    }
}
