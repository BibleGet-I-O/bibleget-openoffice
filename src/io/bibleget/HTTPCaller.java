/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.bibleget;

import static io.bibleget.BibleGetI18N.__;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import static javax.json.JsonValue.ValueType.ARRAY;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.StringUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 *
 * @author Lwangaman
 */
public class HTTPCaller {
    
    private static Indexes indexes = null;
    private final List<String> errorMessages = new ArrayList<>();
    private static int counter = 0;
        
    /**
     *
     * @param myQuery
     * @param versions
     * @return
     */
    public String sendGet(String myQuery,String versions) {
        try {
            versions = URLEncoder.encode(versions,"utf-8");
            myQuery = URLEncoder.encode(myQuery,"utf-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HTTPCaller.class.getName()).log(Level.SEVERE, null, ex);
        }
        String url = "https://query.bibleget.io/index.php?query="+myQuery+"&version="+versions+"&return=json&appid=libreoffice&pluginversion="+BibleGetIO.PLUGINVERSION;
        if(counter < 1){
            if(installCert()){
                counter++;
                return getResponse(url);
            }
        }else{
            return getResponse(url);
        }
        return null;
    }
    
    /**
     *
     * @param query
     * @return
     */
    public String getMetaData(String query){
        String url;
        String response;
        url = "https://query.bibleget.io/metadata.php?query="+query;
        if(counter < 1){
            if(installCert()){
                counter++;
                return getResponse(url);
            }
        }else{
            return getResponse(url);
        }
        return null;
     }

    /**
     *
     * @param url
     * @return
     */
    public String getResponse(String url){
        URL obj;
        try {
            obj = new URL(url);
            HttpsURLConnection con;
            con = (HttpsURLConnection) obj.openConnection();            
             // optional default is GET
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Charset", "UTF-8");


            //System.out.println("Sending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + con.getResponseCode());
            int respCode;
            respCode = con.getResponseCode();
            if(HttpsURLConnection.HTTP_OK == respCode) {
                StringBuilder response;
                try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(),"UTF-8"))) 
                {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    con.disconnect();
                    return response.toString();
                }
            }                               
        } catch (IOException ex) {
            Logger.getLogger(HTTPCaller.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        return null;    
    }
    
    //https://stackoverflow.com/a/34111150
    private boolean installCert(){
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"),
                    "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath),
                    "changeit".toCharArray());
            
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream caInput = new BufferedInputStream(
                    // this files is shipped with the application
                    HTTPCaller.class.getResourceAsStream("/io/bibleget/certificate/DSTRootCAX3.cer"))) {
                Certificate crt = cf.generateCertificate(caInput);
                System.out.println("Added Cert for " + ((X509Certificate) crt)
                        .getSubjectDN());
                
                keyStore.setCertificateEntry("DSTRootCAX3", crt);
            }
            
            System.out.println("Truststore now trusting: ");
            PKIXParameters params = new PKIXParameters(keyStore);
            /*
            params.getTrustAnchors().stream()
                    .map(TrustAnchor::getTrustedCert)
                    .map(X509Certificate::getSubjectDN)
                    .forEach(System.out::println);
            System.out.println();
            */
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);
            
        }   catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException | KeyManagementException ex) {
            Logger.getLogger(HTTPCaller.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    /* 
     * FUNCTION idxOf 
     * @var needle
     * @var haystack
     */
    public int idxOf(String needle,JsonArray haystack) {
        int count = 0;
        for(JsonValue i:haystack){
            JsonArray m = (JsonArray)i;
            if(m.get(0).getValueType()==ARRAY){
                for(JsonValue x:m){
                    //System.out.println("looking for '"+needle+"' in "+x.toString());
                    if(x.toString().contains("\""+needle+"\"")){ return count; }
                }
            }
            else{
                if(m.toString().contains("\""+needle+"\"")){ return count; }
            }
            count++;
        }
        return -1;
    }

    /* 
     * FUNCTION isValidBook 
     * @var book
     */
    public int isValidBook(String book) throws SQLException{
        try {
            JsonArrayBuilder biblebooksBldr = Json.createArrayBuilder();
            BibleGetDB bibleGetDB;
            bibleGetDB = BibleGetDB.getInstance();
            for(int i=0;i<73;i++){
                String usrprop = bibleGetDB.getMetaData("BIBLEBOOKS"+Integer.toString(i));
                //System.out.println("value of BIBLEBOOKS"+Integer.toString(i)+": "+usrprop);                
                JsonReader jsonReader = Json.createReader(new StringReader(usrprop));
                JsonArray jsbooks = jsonReader.readArray();
                biblebooksBldr.add(jsbooks);                
            }
            JsonArray biblebooks = biblebooksBldr.build();
            if(!biblebooks.isEmpty()){
                return idxOf(book,biblebooks);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HTTPCaller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     *
     * @param myQuery
     * @param selectedVersions
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.UnsupportedEncodingException
     * @throws java.sql.SQLException
     */
    public boolean integrityCheck(String myQuery,List<String> selectedVersions) throws ClassNotFoundException, UnsupportedEncodingException, SQLException
    {
        //String versionsStr = StringUtils.join(selectedVersions.toArray(), ',');
        //System.out.println("Starting integrity check on query "+myQuery+" for versions: "+versionsStr);
        if(indexes==null) {
            indexes = Indexes.getInstance();
        }
        //build indexes based on versions
        
        //final result is true until proved false
        //set finFlag to false for non-breaking errors, or simply return false for breaking errors
        boolean finFlag = true;
        
        errorMessages.removeAll(errorMessages);
        List<String> queries = new ArrayList<>();
        
        //if english notation is found, translate to european notation
        if(myQuery.contains(":") && myQuery.contains(".")) {
            errorMessages.add(__("Mixed notations have been detected. Please use either english notation or european notation."));
            return false;
        }
        else if(myQuery.contains(":")) {
            if(myQuery.contains(",")) {
                myQuery = myQuery.replace(",",".");
            }
            myQuery = myQuery.replace(":",",");
        }
        
        if(myQuery.isEmpty()==false){
            if(myQuery.contains(";")){
                //System.out.println("We have a semicolon");
                queries.addAll(Arrays.asList(myQuery.split(";")));
                for (Iterator<String> it = queries.iterator(); it.hasNext();) {
                    if (it.next().isEmpty()){
                        it.remove(); // NOTE: Iterator's remove method, not ArrayList's, is used.
                    }
                }
            }
            else{
                //System.out.println("There is no semicolon");
                queries.add(myQuery);
            }
        }
        
        boolean first = true;
        String currBook = "";
        
        if(queries.isEmpty()){ 
            errorMessages.add(__("You cannot send an empty query."));
            return false; 
        }
        for (String querie : queries) {
            //System.out.println(querie);
            querie = toProperCase(querie);
            //System.out.println(querie);
            
            //RULE 1: at least the first query must have a book indicator
            if(first){
                if(querie.matches("^[1-3]{0,1}((\\p{L}\\p{M}*)+)(.*)") == false){ 
                    errorMessages.add(MessageFormat.format(__("The first query <{0}> in the querystring <{1}> must start with a valid book indicator!"),querie,myQuery)); 
                    finFlag = false; 
                }
                first = false;
            }
            
            //RULE 2: for every query that starts with a book indicator, 
            //        the book indicator must be followed by valid chapter indicator;
            //        else query must start with valid chapter indicator
            int bBooksContains;
            int myidx = -1;
            String tempBook = "";
            if(querie.matches("^[1-3]{0,1}((\\p{L}\\p{M}*)+)(.*)") == true){
                //while we're at it, let's capture the book value from the query
                Pattern pattern = Pattern.compile("^[1-3]{0,1}((\\p{L}\\p{M}*)+)",Pattern.UNICODE_CHARACTER_CLASS);
                Matcher matcher = pattern.matcher(querie);
                if(matcher.find()){
                    tempBook = matcher.group();
                    bBooksContains = isValidBook(tempBook);
                    myidx = bBooksContains+1;
                    //if(bBooksContains == false && bBooksAbbrevsContains == false){
                    if(bBooksContains == -1){
                        errorMessages.add(MessageFormat.format(__("The book indicator <{0}> in the query <{1}> is not valid. Please check the documentation for a list of valid book indicators."),tempBook,querie));
                        finFlag = false;
                    }
                    else{
                        //if(bBooksContains)
                        currBook = tempBook;
                        //querie = querie.replace(tempBook,"");
                    }
                }
                
                Pattern pattern1 = Pattern.compile("^[1-3]{0,1}((\\p{L}\\p{M}*)+)",Pattern.UNICODE_CHARACTER_CLASS);
                Pattern pattern2 = Pattern.compile("^[1-3]{0,1}((\\p{L}\\p{M}*)+)[1-9][0-9]{0,2}",Pattern.UNICODE_CHARACTER_CLASS); 
                Matcher matcher1 = pattern1.matcher(querie);
                Matcher matcher2 = pattern2.matcher(querie);
                int count1 = 0;
                while(matcher1.find()){
                    count1++;
                }
                int count2 = 0;
                while(matcher2.find()){
                    count2++;
                }
                if(querie.matches("^[1-3]{0,1}((\\p{L}\\p{M}*)+)[1-9][0-9]{0,2}(.*)") == false || count1 != count2){
                    errorMessages.add(__("You must have a valid chapter following the book indicator!"));
                    finFlag = false;
                }
                querie = querie.replace(tempBook,"");
            }
            else{
                if(querie.matches("^[1-9][0-9]{0,2}(.*)") == false){
                    errorMessages.add(__("A query that doesn't start with a book indicator must however start with a valid chapter indicator!"));
                    finFlag = false;
                }
            }
            
            //RULE 3: Queries with a dot operator must first have a comma operator; and cannot have more commas than dots
            if(querie.contains(".")){
                Pattern pattern11 = Pattern.compile("[,|\\-|\\.][1-9][0-9]{0,2}\\.");
                Matcher matcher11 = pattern11.matcher(querie);
                if(querie.contains(",") == false || matcher11.find() == false){
                    errorMessages.add(__("You cannot use a dot without first using a comma or a dash. A dot is a liason between verses, which are separated from the chapter by a comma."));
                    finFlag = false;
                }
                Pattern pattern3 = Pattern.compile("(?<![0-9])(?=(([1-9][0-9]{0,2})\\.([1-9][0-9]{0,2})))");
                Matcher matcher3 = pattern3.matcher(querie);
                int count = 0;
                while(matcher3.find()){
                    //RULE 4: verse numbers around dot operators must be sequential
                    if(Integer.parseInt(matcher3.group(2)) >= Integer.parseInt(matcher3.group(3))){
                        errorMessages.add(MessageFormat.format(__("Verses concatenated by a dot must be consecutive, instead <{0}> is greater than or equal to <{1}> in the expression <{2}> in the query <{3}>"),matcher3.group(2),matcher3.group(3),matcher3.group(1),querie));
                        finFlag = false;
                    }
                    count++;
                }
                //RULE 5: Dot operators must be preceded and followed by a number from one to three digits, of which the first digit cannot be a 0
                if(count == 0 || count != StringUtils.countMatches(querie,".") ){
                    errorMessages.add(__("A dot must be preceded and followed by 1 to 3 digits of which the first digit cannot be zero.")+" <"+querie+">");
                    finFlag = false;
                }
            }
            
            //RULE 6: Comma operators must be preceded and followed by a number from one to three digits, of which the first digit cannot be 0
            if(querie.contains(",")){
                
                Pattern pattern4 = Pattern.compile("([1-9][0-9]{0,2})\\,[1-9][0-9]{0,2}");
                Matcher matcher4 = pattern4.matcher(querie);
                int count = 0;
                List<Integer> chapters = new ArrayList<>();
                while(matcher4.find()){
                    //System.out.println("group0="+matcher4.group(0)+", group1="+matcher4.group(1));
                    chapters.add(Integer.parseInt(matcher4.group(1)));
                    count++;
                }
                if(count == 0 || count != StringUtils.countMatches(querie,",") ){
                    errorMessages.add(__("A comma must be preceded and followed by 1 to 3 digits of which the first digit cannot be zero.")+" <"+querie+">"+"(count="+Integer.toString(count)+",comma count="+StringUtils.countMatches(querie,",")+"); chapters="+chapters.toString());
                    finFlag = false;
                }
                else{
                    // let's check the validity of the chapter numbers against the version indexes
                    //for each chapter captured in the querystring
                    for(int chapter:chapters){
                        if(indexes.isValidChapter(chapter, myidx, selectedVersions)==false) {
                            int[] chapterLimit = indexes.getChapterLimit(myidx, selectedVersions);
                            errorMessages.add(MessageFormat.format(__("A chapter in the query is out of bounds: there is no chapter <{0}> in the book <{1}> in the requested version <{2}>, the last possible chapter is <{3}>"),Integer.toString(chapter),currBook,StringUtils.join(selectedVersions,","),StringUtils.join(chapterLimit,',')));
                            finFlag = false;
                        }
                    }
                }
            }
            
            if(StringUtils.countMatches(querie,",") > 1) {
                if(!querie.contains("-")) {
                    errorMessages.add(__("You cannot have more than one comma and not have a dash!"));
                    finFlag = false;
                }
                String[] parts = StringUtils.split(querie, "-");
                if(parts.length != 2) {
                    errorMessages.add(__("You seem to have a malformed querystring, there should be only one dash."));
                    finFlag = false;
                }
                for(String p:parts) {
                    Integer[] pp = new Integer[2];
                    String[] tt = StringUtils.split(p,",");
                    int x=0;
                    for(String t:tt){
                        pp[x++] = Integer.parseInt(t);
                    }
                    if(indexes.isValidChapter(pp[0], myidx, selectedVersions)==false) {
                        int[] chapterLimit;
                        chapterLimit = indexes.getChapterLimit(myidx, selectedVersions);
//                        System.out.print("chapterLimit = ");
//                        System.out.println(Arrays.toString(chapterLimit));
                        errorMessages.add(MessageFormat.format(__("A chapter in the query is out of bounds: there is no chapter <{0}> in the book <{1}> in the requested version <{2}>, the last possible chapter is <{3}>"),Integer.toString(pp[0]),currBook,StringUtils.join(selectedVersions,","),StringUtils.join(chapterLimit,',')));
                        finFlag = false;                
                    }
                    else {
                        if(indexes.isValidVerse(pp[1], pp[0], myidx, selectedVersions)==false) {
                            int[] verseLimit = indexes.getVerseLimit(pp[0], myidx, selectedVersions);
//                            System.out.print("verseLimit = ");
//                            System.out.println(Arrays.toString(verseLimit));
                            errorMessages.add(MessageFormat.format(__("A verse in the query is out of bounds: there is no verse <{0}> in the book <{1}> at chapter <{2}> in the requested version <{3}>, the last possible verse is <{4}>"),Integer.toString(pp[1]),currBook,Integer.toString(pp[0]),StringUtils.join(selectedVersions,","),StringUtils.join(verseLimit,',')));
                            finFlag = false;
                        }
                    }
                }
            }
            else if(StringUtils.countMatches(querie,",") == 1) {
                String[] parts = StringUtils.split(querie, ",");
                //System.out.println(Arrays.toString(parts));
                if(indexes.isValidChapter(Integer.parseInt(parts[0]), myidx, selectedVersions)==false) {
                    int[] chapterLimit = indexes.getChapterLimit(myidx, selectedVersions);
                    errorMessages.add(MessageFormat.format(__("A chapter in the query is out of bounds: there is no chapter <{0}> in the book <{1}> in the requested version <{2}>, the last possible chapter is <{3}>"),parts[0],currBook,StringUtils.join(selectedVersions,","),StringUtils.join(chapterLimit,',')));
                    finFlag = false;                
                }
                else {
                    if(parts[1].contains("-")) {
                        Deque<Integer> highverses = new ArrayDeque<>();
                        Pattern pattern11 = Pattern.compile("[,\\.][1-9][0-9]{0,2}\\-([1-9][0-9]{0,2})");
                        Matcher matcher11 = pattern11.matcher(querie);
                        while(matcher11.find()) {
                            highverses.push(Integer.parseInt(matcher11.group(1)));
                        }
                        int highverse = highverses.pop();
                        if(indexes.isValidVerse(highverse, Integer.parseInt(parts[0]), myidx, selectedVersions)==false) {
                            int[] verseLimit = indexes.getVerseLimit(Integer.parseInt(parts[0]), myidx, selectedVersions);
                            errorMessages.add(MessageFormat.format(__("A verse in the query is out of bounds: there is no verse <{0}> in the book <{1}> at chapter <{2}> in the requested version <{3}>, the last possible verse is <{4}>"),highverse,currBook,parts[0],StringUtils.join(selectedVersions,","),StringUtils.join(verseLimit,',')));
                            finFlag = false;                        
                        }
                    }
                    else {
                        Pattern pattern12 = Pattern.compile(",([1-9][0-9]{0,2})");
                        Matcher matcher12 = pattern12.matcher(querie);
                        int highverse = -1;
                        while(matcher12.find()) {
                            highverse = Integer.parseInt(matcher12.group(1));
                            //System.out.println("[line 376]:highverse="+Integer.toString(highverse));
                        }
                        if(highverse!=-1) {
                            //System.out.println("Checking verse validity for book "+myidx+" chapter "+parts[0]+"...");
                            if(indexes.isValidVerse(highverse, Integer.parseInt(parts[0]), myidx, selectedVersions)==false) {
                                int[] verseLimit = indexes.getVerseLimit(Integer.parseInt(parts[0]), myidx, selectedVersions);
                                errorMessages.add(MessageFormat.format(__("A verse in the query is out of bounds: there is no verse <{0}> in the book <{1}> at chapter <{2}> in the requested version <{3}>, the last possible verse is <{4}>"),highverse,currBook,parts[0],StringUtils.join(selectedVersions,","),StringUtils.join(verseLimit,',')));
                                finFlag = false;                        
                            }
                        }
                    }
                    Pattern pattern13 = Pattern.compile("\\.([1-9][0-9]{0,2})$");
                    Matcher matcher13 = pattern13.matcher(querie);
                    int highverse = -1;
                    while(matcher13.find()) {
                        highverse = Integer.parseInt(matcher13.group(1));
                    }
                    if(highverse != -1){
                        if(indexes.isValidVerse(highverse, Integer.parseInt(parts[0]), myidx, selectedVersions)==false) {
                            int[] verseLimit = indexes.getVerseLimit(Integer.parseInt(parts[0]), myidx, selectedVersions);
                            errorMessages.add(MessageFormat.format(__("A verse in the query is out of bounds: there is no verse <{0}> in the book <{1}> at chapter <{2}> in the requested version <{3}>, the last possible verse is <{4}>"),highverse,currBook,parts[0],StringUtils.join(selectedVersions,","),StringUtils.join(verseLimit,',')));
                            finFlag = false;                        
                        }
                    }
                }
            }            
            else { //if there is no comma, it's either a single chapter or an extension of chapters with a dash
                //System.out.println("no comma found");
                String[] parts = StringUtils.split(querie, "-");
                //System.out.println(Arrays.toString(parts));
                int highchapter = Integer.parseInt(parts[parts.length-1]);
                if(indexes.isValidChapter(highchapter, myidx, selectedVersions)==false) {
                    int[] chapterLimit = indexes.getChapterLimit(myidx, selectedVersions);
                    errorMessages.add(MessageFormat.format(__("A chapter in the query is out of bounds: there is no chapter <{0}> in the book <{1}> in the requested version <{2}>, the last possible chapter is <{3}>"),Integer.toString(highchapter),currBook,StringUtils.join(selectedVersions,","),StringUtils.join(chapterLimit,',')));
                    finFlag = false;
                }
            }
            
            if(querie.contains("-")){
                //RULE 7: If there are multiple dashes in a query, there cannot be more dashes than there are dots minus 1
                int dashcount = StringUtils.countMatches(querie, "-");
                int dotcount = StringUtils.countMatches(querie, ".");
                if(dashcount > 1){
                    if(dashcount-1 > dotcount){
                        errorMessages.add(__("There are multiple dashes in the query, but there are not enough dots. There can only be one more dash than dots.")+" <"+querie+">");
                        finFlag = false;
                    }
                }
                
                //RULE 8: Dash operators must be preceded and followed by a number from one to three digits, of which the first digit cannot be 0
                Pattern pattern5 = Pattern.compile("([1-9][0-9]{0,2}\\-[1-9][0-9]{0,2})");
                Matcher matcher5 = pattern5.matcher(querie);
                int count = 0;
                while(matcher5.find()){
                    count++;
                }
                if(count == 0 || count != StringUtils.countMatches(querie,"-") ){
                    errorMessages.add(__("A dash must be preceded and followed by 1 to 3 digits of which the first digit cannot be zero.")+" <"+querie+">");
                    finFlag = false;
                }
                
                //RULE 9: If a comma construct follows a dash, there must also be a comma construct preceding the dash
                Pattern pattern6 = Pattern.compile("\\-([1-9][0-9]{0,2})\\,");
                Matcher matcher6 = pattern6.matcher(querie);
                if(matcher6.find()){
                    Pattern pattern7 = Pattern.compile("\\,[1-9][0-9]{0,2}\\-");
                    Matcher matcher7 = pattern7.matcher(querie);
                    if(matcher7.find() == false){
                        errorMessages.add(__("If there is a chapter-verse construct following a dash, there must also be a chapter-verse construct preceding the same dash.")+" <"+querie+">");
                        finFlag = false;
                    }
                    else{
                        //RULE 10: Chapters before and after dashes must be sequential
                        int chap1 = -1;
                        int chap2 = -1;
                        
                        Pattern pattern8 = Pattern.compile("([1-9][0-9]{0,2})\\,[1-9][0-9]{0,2}\\-");
                        Matcher matcher8 = pattern8.matcher(querie);
                        if(matcher8.find()){
                            chap1 = Integer.parseInt(matcher8.group(1));
                        }
                        Pattern pattern9 = Pattern.compile("\\-([1-9][0-9]{0,2})\\,");
                        Matcher matcher9 = pattern9.matcher(querie);
                        if(matcher9.find()){
                            chap2 = Integer.parseInt(matcher9.group(1));
                        }
                        
                        if(chap1 >= chap2){
                            errorMessages.add(MessageFormat.format(__("Chapters must be consecutive. Instead the first chapter indicator <{0}> is greater than or equal to the second chapter indicator <{1}> in the expression <{2}>"),chap1,chap2,querie));
                            finFlag = false;
                        }
                    }
                }
                else{
                    //if there are no comma constructs immediately following the dash
                    //RULE 11: Verses (or chapters if applicable) around each of the dash operator(s) must be sequential
                    Pattern pattern10 = Pattern.compile("([1-9][0-9]{0,2})\\-([1-9][0-9]{0,2})");
                    Matcher matcher10 = pattern10.matcher(querie);
                    while(matcher10.find()){
                        int num1 = Integer.parseInt(matcher10.group(1));
                        int num2 = Integer.parseInt(matcher10.group(2));
                        if(num1 >= num2){
                            errorMessages.add(MessageFormat.format(__("Verses (or chapters if applicable) around the dash operator must be consecutive. Instead <{0}> is greater than or equal to <{1}> in the expression <{2}>"),num1,num2,querie));
                            finFlag = false;
                        }                        
                    }
                    
                }
            }
            
        }
        
        return finFlag;
    }
    
    /**
     *
     * @return
     */
    public String[] getErrorMessages()
    {
        String[] errorMexs = errorMessages.toArray(new String[errorMessages.size()]);
        return errorMexs;
    }
    
    /**
     *
     * @param txt
     * @return
     */
    public String toProperCase(String txt)
    {
        int idx=0;
        while(String.valueOf(txt.charAt(idx)).matches("[a-zA-Z]") == false){
            if(idx==txt.length()-1){ break; }
            idx++;
        }
        if(idx<txt.length()-2){ return txt.substring(0,idx) + String.valueOf(txt.charAt(idx)).toUpperCase() + txt.substring(idx+1).toLowerCase(); }
        else{ return txt; }
    }
        
}

