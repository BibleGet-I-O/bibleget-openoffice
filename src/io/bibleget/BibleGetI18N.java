/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.commons.text.WordUtils;


/**
 *
 * @author Lwangaman
 */
public class BibleGetI18N {
    
    /*
    static final ResourceBundle myResources =
      ResourceBundle.getBundle("io.bibleget.resources.messages");
    */
    //private static String lcl;
    private static ResourceBundle myResource;
    //private static String iso;
    private static final Map<String, String> LANGUAGECODES;
    private static final Map<String, String> LANGUAGES_TRANSLATED;
    static {
        Map<String, String> langcodes = new HashMap<>();
        langcodes.put("AFRIKAANS","af");
        langcodes.put("AKAN","ak");
        langcodes.put("ALBANIAN","sq");
        langcodes.put("AMHARIC","am");
        langcodes.put("ARABIC","ar");
        langcodes.put("ARMENIAN","hy");
        langcodes.put("AZERBAIJANI","az");
        langcodes.put("BASQUE","eu");
        langcodes.put("BELARUSIAN","be");
        langcodes.put("BENGALI","bn");
        langcodes.put("BIHARI","bh");
        langcodes.put("BOSNIAN","bs");
        langcodes.put("BRETON","br");
        langcodes.put("BULGARIAN","bg");
        langcodes.put("CAMBODIAN","km");
        langcodes.put("CATALAN","ca");
        langcodes.put("CHICHEWA","ny");
        langcodes.put("CHINESE","zh");
        langcodes.put("CORSICAN","co");
        langcodes.put("CROATIAN","hr");
        langcodes.put("CZECH","cs");
        langcodes.put("DANISH","da");
        langcodes.put("DUTCH","nl");
        langcodes.put("ENGLISH","en");
        langcodes.put("ESPERANTO","eo");
        langcodes.put("ESTONIAN","et");
        langcodes.put("FAROESE","fo");
        langcodes.put("FILIPINO","tl");
        langcodes.put("FINNISH","fi");
        langcodes.put("FRENCH","fr");
        langcodes.put("FRISIAN","fy");
        langcodes.put("GALICIAN","gl");
        langcodes.put("GEORGIAN","ka");
        langcodes.put("GERMAN","de");
        langcodes.put("GREEK","el");
        langcodes.put("GUARANI","gn");
        langcodes.put("GUJARATI","gu");
        langcodes.put("HAITIAN","ht");
        langcodes.put("CREOLE","ht");
        langcodes.put("HAUSA","ha");
        langcodes.put("HEBREW","iw");
        langcodes.put("HINDI","hi");
        langcodes.put("HUNGARIAN","hu");
        langcodes.put("ICELANDIC","is");
        langcodes.put("IGBO","ig");
        langcodes.put("INDONESIAN","id");
        langcodes.put("INTERLINGUA","ia");
        langcodes.put("IRISH","ga");
        langcodes.put("ITALIAN","it");
        langcodes.put("JAPANESE","ja");
        langcodes.put("JAVANESE","jw");
        langcodes.put("KANNADA","kn");
        langcodes.put("KAZAKH","kk");
        langcodes.put("KINYARWANDA","rw");
        langcodes.put("KIRUNDI","rn");
        langcodes.put("KONGO","kg");
        langcodes.put("KOREAN","ko");
        langcodes.put("KURDISH","ku");
        langcodes.put("KYRGYZ","ky");
        langcodes.put("LAOTHIAN","lo");
        langcodes.put("LATIN","la");
        langcodes.put("LATVIAN","lv");
        langcodes.put("LINGALA","ln");
        langcodes.put("LITHUANIAN","lt");
        langcodes.put("LUGANDA","lg");
        langcodes.put("MACEDONIAN","mk");
        langcodes.put("MALAGASY","mg");
        langcodes.put("MALAY","ms");
        langcodes.put("MALAYALAM","ml");
        langcodes.put("MALTESE","mt");
        langcodes.put("MAORI","mi");
        langcodes.put("MARATHI","mr");
        langcodes.put("MOLDAVIAN","mo");
        langcodes.put("MONGOLIAN","mn");
        langcodes.put("NEPALI","ne");
        langcodes.put("NORWEGIAN","no");
        langcodes.put("OCCITAN","oc");
        langcodes.put("ORIYA","or");
        langcodes.put("OROMO","om");
        langcodes.put("PASHTO","ps");
        langcodes.put("PERSIAN","fa");
        langcodes.put("POLISH","pl");
        langcodes.put("PORTUGUESE","pt");
        langcodes.put("PUNJABI","pa");
        langcodes.put("QUECHUA","qu");
        langcodes.put("ROMANIAN","ro");
        langcodes.put("ROMANSH","rm");
        langcodes.put("RUSSIAN","ru");
        langcodes.put("GAELIC","gd");
        langcodes.put("SERBIAN","sr");
        langcodes.put("SERBO-CROATIAN","sh");
        langcodes.put("SESOTHO","st");
        langcodes.put("SETSWANA","tn");
        langcodes.put("SHONA","sn");
        langcodes.put("SINDHI","sd");
        langcodes.put("SINHALESE","si");
        langcodes.put("SLOVAK","sk");
        langcodes.put("SLOVENIAN","sl");
        langcodes.put("SOMALI","so");
        langcodes.put("SPANISH","es");
        langcodes.put("SUNDANESE","su");
        langcodes.put("SWAHILI","sw");
        langcodes.put("SWEDISH","sv");
        langcodes.put("TAJIK","tg");
        langcodes.put("TAMIL","ta");
        langcodes.put("TATAR","tt");
        langcodes.put("TELUGU","te");
        langcodes.put("THAI","th");
        langcodes.put("TIGRINYA","ti");
        langcodes.put("TONGA","to");
        langcodes.put("TURKISH","tr");
        langcodes.put("TURKMEN","tk");
        langcodes.put("TWI","tw");
        langcodes.put("UIGHUR","ug");
        langcodes.put("UKRAINIAN","uk");
        langcodes.put("URDU","ur");
        langcodes.put("UZBEK","uz");
        langcodes.put("VIETNAMESE","vi");
        langcodes.put("WELSH","cy");
        langcodes.put("WOLOF","wo");
        langcodes.put("XHOSA","xh");
        langcodes.put("YIDDISH","yi");
        langcodes.put("YORUBA","yo");
        langcodes.put("ZULU","zu");
        LANGUAGECODES = Collections.unmodifiableMap(langcodes);
    }
    
    static {
        Map<String, String> langcodes = new HashMap<>();
        langcodes.put("af",new Locale("af").getDisplayLanguage());
        langcodes.put("ak",new Locale("ak").getDisplayLanguage());
        langcodes.put("sq",new Locale("sq").getDisplayLanguage());
        langcodes.put("am",new Locale("am").getDisplayLanguage());
        langcodes.put("ar",new Locale("ar").getDisplayLanguage());
        langcodes.put("hy",new Locale("hy").getDisplayLanguage());
        langcodes.put("az",new Locale("az").getDisplayLanguage());
        langcodes.put("eu",new Locale("eu").getDisplayLanguage());
        langcodes.put("be",new Locale("be").getDisplayLanguage());
        langcodes.put("bn",new Locale("bn").getDisplayLanguage());
        langcodes.put("bh",new Locale("bh").getDisplayLanguage());
        langcodes.put("bs",new Locale("bs").getDisplayLanguage());
        langcodes.put("br",new Locale("br").getDisplayLanguage());
        langcodes.put("bg",new Locale("bg").getDisplayLanguage());
        langcodes.put("km",new Locale("km").getDisplayLanguage());
        langcodes.put("ca",new Locale("ca").getDisplayLanguage());
        langcodes.put("ny",new Locale("ny").getDisplayLanguage());
        langcodes.put("zh",new Locale("zh").getDisplayLanguage());
        langcodes.put("co",new Locale("co").getDisplayLanguage());
        langcodes.put("hr",new Locale("hr").getDisplayLanguage());
        langcodes.put("cs",new Locale("cs").getDisplayLanguage());
        langcodes.put("da",new Locale("da").getDisplayLanguage());
        langcodes.put("nl",new Locale("nl").getDisplayLanguage());
        langcodes.put("en",new Locale("en").getDisplayLanguage());
        langcodes.put("eo",new Locale("eo").getDisplayLanguage());
        langcodes.put("et",new Locale("et").getDisplayLanguage());
        langcodes.put("fo",new Locale("fo").getDisplayLanguage());
        langcodes.put("tl",new Locale("tl").getDisplayLanguage());
        langcodes.put("fi",new Locale("fi").getDisplayLanguage());
        langcodes.put("fr",new Locale("fr").getDisplayLanguage());
        langcodes.put("fy",new Locale("fy").getDisplayLanguage());
        langcodes.put("gl",new Locale("gl").getDisplayLanguage());
        langcodes.put("ka",new Locale("ka").getDisplayLanguage());
        langcodes.put("de",new Locale("de").getDisplayLanguage());
        langcodes.put("el",new Locale("el").getDisplayLanguage());
        langcodes.put("gn",new Locale("gn").getDisplayLanguage());
        langcodes.put("gu",new Locale("gu").getDisplayLanguage());
        langcodes.put("ht",new Locale("ht").getDisplayLanguage());
        langcodes.put("ht",new Locale("ht").getDisplayLanguage());
        langcodes.put("ha",new Locale("ha").getDisplayLanguage());
        langcodes.put("iw",new Locale("iw").getDisplayLanguage());
        langcodes.put("hi",new Locale("hi").getDisplayLanguage());
        langcodes.put("hu",new Locale("hu").getDisplayLanguage());
        langcodes.put("is",new Locale("is").getDisplayLanguage());
        langcodes.put("ig",new Locale("ig").getDisplayLanguage());
        langcodes.put("id",new Locale("id").getDisplayLanguage());
        langcodes.put("ia",new Locale("ia").getDisplayLanguage());
        langcodes.put("ga",new Locale("ga").getDisplayLanguage());
        langcodes.put("it",new Locale("it").getDisplayLanguage());
        langcodes.put("ja",new Locale("ja").getDisplayLanguage());
        langcodes.put("jw",new Locale("jw").getDisplayLanguage());
        langcodes.put("kn",new Locale("kn").getDisplayLanguage());
        langcodes.put("kk",new Locale("kk").getDisplayLanguage());
        langcodes.put("rw",new Locale("rw").getDisplayLanguage());
        langcodes.put("rn",new Locale("rn").getDisplayLanguage());
        langcodes.put("kg",new Locale("kg").getDisplayLanguage());
        langcodes.put("ko",new Locale("ko").getDisplayLanguage());
        langcodes.put("ku",new Locale("ku").getDisplayLanguage());
        langcodes.put("ky",new Locale("ky").getDisplayLanguage());
        langcodes.put("lo",new Locale("lo").getDisplayLanguage());
        langcodes.put("la",new Locale("la").getDisplayLanguage());
        langcodes.put("lv",new Locale("lv").getDisplayLanguage());
        langcodes.put("ln",new Locale("ln").getDisplayLanguage());
        langcodes.put("lt",new Locale("lt").getDisplayLanguage());
        langcodes.put("lg",new Locale("lg").getDisplayLanguage());
        langcodes.put("mk",new Locale("mk").getDisplayLanguage());
        langcodes.put("mg",new Locale("mg").getDisplayLanguage());
        langcodes.put("ms",new Locale("ms").getDisplayLanguage());
        langcodes.put("ml",new Locale("ml").getDisplayLanguage());
        langcodes.put("mt",new Locale("mt").getDisplayLanguage());
        langcodes.put("mi",new Locale("mi").getDisplayLanguage());
        langcodes.put("mr",new Locale("mr").getDisplayLanguage());
        langcodes.put("mo",new Locale("mo").getDisplayLanguage());
        langcodes.put("mn",new Locale("mn").getDisplayLanguage());
        langcodes.put("ne",new Locale("ne").getDisplayLanguage());
        langcodes.put("no",new Locale("no").getDisplayLanguage());
        langcodes.put("oc",new Locale("oc").getDisplayLanguage());
        langcodes.put("or",new Locale("or").getDisplayLanguage());
        langcodes.put("om",new Locale("om").getDisplayLanguage());
        langcodes.put("ps",new Locale("ps").getDisplayLanguage());
        langcodes.put("fa",new Locale("fa").getDisplayLanguage());
        langcodes.put("pl",new Locale("pl").getDisplayLanguage());
        langcodes.put("pt",new Locale("pt").getDisplayLanguage());
        langcodes.put("pa",new Locale("pa").getDisplayLanguage());
        langcodes.put("qu",new Locale("qu").getDisplayLanguage());
        langcodes.put("ro",new Locale("ro").getDisplayLanguage());
        langcodes.put("rm",new Locale("rm").getDisplayLanguage());
        langcodes.put("ru",new Locale("ru").getDisplayLanguage());
        langcodes.put("gd",new Locale("gd").getDisplayLanguage());
        langcodes.put("sr",new Locale("sr").getDisplayLanguage());
        langcodes.put("sh",new Locale("sh").getDisplayLanguage());
        langcodes.put("st",new Locale("st").getDisplayLanguage());
        langcodes.put("tn",new Locale("tn").getDisplayLanguage());
        langcodes.put("sn",new Locale("sn").getDisplayLanguage());
        langcodes.put("sd",new Locale("sd").getDisplayLanguage());
        langcodes.put("si",new Locale("si").getDisplayLanguage());
        langcodes.put("sk",new Locale("sk").getDisplayLanguage());
        langcodes.put("sl",new Locale("sl").getDisplayLanguage());
        langcodes.put("so",new Locale("so").getDisplayLanguage());
        langcodes.put("es",new Locale("es").getDisplayLanguage());
        langcodes.put("su",new Locale("su").getDisplayLanguage());
        langcodes.put("sw",new Locale("sw").getDisplayLanguage());
        langcodes.put("sv",new Locale("sv").getDisplayLanguage());
        langcodes.put("tg",new Locale("tg").getDisplayLanguage());
        langcodes.put("ta",new Locale("ta").getDisplayLanguage());
        langcodes.put("tt",new Locale("tt").getDisplayLanguage());
        langcodes.put("te",new Locale("te").getDisplayLanguage());
        langcodes.put("th",new Locale("th").getDisplayLanguage());
        langcodes.put("ti",new Locale("ti").getDisplayLanguage());
        langcodes.put("to",new Locale("to").getDisplayLanguage());
        langcodes.put("tr",new Locale("tr").getDisplayLanguage());
        langcodes.put("tk",new Locale("tk").getDisplayLanguage());
        langcodes.put("tw",new Locale("tw").getDisplayLanguage());
        langcodes.put("ug",new Locale("ug").getDisplayLanguage());
        langcodes.put("uk",new Locale("uk").getDisplayLanguage());
        langcodes.put("ur",new Locale("ur").getDisplayLanguage());
        langcodes.put("uz",new Locale("uz").getDisplayLanguage());
        langcodes.put("vi",new Locale("vi").getDisplayLanguage());
        langcodes.put("cy",new Locale("cy").getDisplayLanguage());
        langcodes.put("wo",new Locale("wo").getDisplayLanguage());
        langcodes.put("xh",new Locale("xh").getDisplayLanguage());
        langcodes.put("yi",new Locale("yi").getDisplayLanguage());
        langcodes.put("yo",new Locale("yo").getDisplayLanguage());
        langcodes.put("zu",new Locale("zu").getDisplayLanguage());
        LANGUAGES_TRANSLATED = Collections.unmodifiableMap(langcodes);
    }
    /**
     *
     * @param s
     * @return
     */
    public static String __(String s) {
        try{
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages",BibleGetIO.getUILocale(),new io.bibleget.UTF8Control());
        } catch(MissingResourceException ex){
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages");
        }
        
        if(myResource.containsKey(s)){            
            String val = myResource.getString(s);
            return val;            
        }
        else{ return s; }
    }
    
    /**
     *
     * @return
     */
    /*
    public static ResourceBundle getMessages() {
        try{
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages",BibleGetIO.getUILocale(),new io.bibleget.UTF8Control());
        } catch (MissingResourceException ex){
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages");
        }
        return myResource;
    }
    */
    /**
     *
     * @param language
     * @return
     */
    public static String localizeLanguage(String language) {
        if(LANGUAGECODES.containsKey(language)){
            String langKey = LANGUAGECODES.get(language);
            String localizedLang = LANGUAGES_TRANSLATED.get(langKey);
            //System.out.println("BibleGetI18N.java: language = "+language+", langKey = "+langKey+", localizedLang = "+localizedLang);
            return WordUtils.capitalizeFully(localizedLang);
        }
        else{
            //System.out.println("BibleGetI18N.java: could not find "+language+" in LANGUAGECODES hashmap, no localization will be done. Returning same value" );
            return WordUtils.capitalizeFully(language);
        }
    }
}

