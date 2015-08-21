/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.text.WordUtils;

/**
 *
 * @author Lwangaman
 */
public class BibleGetI18N {
    
    /*
    static final ResourceBundle myResources =
      ResourceBundle.getBundle("io.bibleget.resources.messages");
    */
    private static String lcl;
    private static ResourceBundle myResource;
    private static String iso;
    
    
    /**
     *
     * @param s
     * @return
     */
    public static String __(String s) {
        lcl = BibleGetIO.getLocale();
        Locale myLocale;
        myLocale = new Locale(lcl);
        try{
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages",myLocale);
        } catch(MissingResourceException ex){
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages");
        }
        
        if(myResource.containsKey(s)){
            try {
                String val = myResource.getString(s);
                return new String(val.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(BibleGetI18N.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{ return s; }
        return null;
    }
    
    /**
     *
     * @return
     */
    public static ResourceBundle getMessages() {
        lcl = BibleGetIO.getLocale();
        Locale myLocale;
        myLocale = new Locale(lcl);
        try{
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages",myLocale);
        } catch (MissingResourceException ex){
            myResource = ResourceBundle.getBundle("io.bibleget.resources.messages");
        }
        return myResource;
    }
    
    /**
     *
     * @param language
     * @return
     */
    public static String localizeLanguage(String language) {
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
        if(langcodes.containsKey(language)){
            return WordUtils.capitalizeFully(new Locale(langcodes.get(language)).getDisplayLanguage());
        }
        else{
            return WordUtils.capitalizeFully(language);
        }
    }
}
