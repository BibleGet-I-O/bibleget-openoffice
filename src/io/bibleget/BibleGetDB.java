/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.bibleget;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;


/**
 *
 * @author Lwangaman
 */
public class BibleGetDB {

    //private final String dbPath;
    private static BibleGetDB instance = null;
    private Connection conn = null;
    //private DatabaseMetaData dbMeta = null;
    //private ResultSet rs = null;
    private ResultSet currentTable;
    
    private final List<String> colNames = new ArrayList<>();
    private final List<Class> colDataTypes = new ArrayList<>();
    
    private static final HashMap<String, Entry<Integer, Entry<String, String>>> tableSchemas = new HashMap<>(); //<String tableName, Entry<Integer schemaVersion, Entry<String tableSchema, String tableData>>
    
    
    private BibleGetDB() throws ClassNotFoundException {
        try {
            BibleGetDB.setDBSystemDir();
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        String defaultFont = "";
        if(SystemUtils.IS_OS_WINDOWS){
            defaultFont = "Times New Roman";
        }
        else if(SystemUtils.IS_OS_MAC_OSX){
            defaultFont = "Helvetica";
        }
        else if(SystemUtils.IS_OS_LINUX){
            defaultFont = "Arial";
        }
        
        String optionsTableSchema = "CREATE TABLE OPTIONS ("
                + "PARAGRAPHSTYLES_LINEHEIGHT INT, "
                + "PARAGRAPHSTYLES_LEFTINDENT INT, "
                + "PARAGRAPHSTYLES_RIGHTINDENT INT, "
                + "PARAGRAPHSTYLES_FONTFAMILY VARCHAR(50), "
                + "PARAGRAPHSTYLES_ALIGNMENT INT, "
                + "PARAGRAPHSTYLES_NOVERSIONFORMATTING BOOLEAN, "
                + "PARAGRAPHSTYLES_INTERFACEINCM BOOLEAN, "
                + "BIBLEVERSIONSTYLES_BOLD BOOLEAN, "
                + "BIBLEVERSIONSTYLES_ITALIC BOOLEAN, "
                + "BIBLEVERSIONSTYLES_UNDERLINE BOOLEAN, "
                + "BIBLEVERSIONSTYLES_STRIKETHROUGH BOOLEAN, "
                + "BIBLEVERSIONSTYLES_TEXTCOLOR VARCHAR(15), "
                + "BIBLEVERSIONSTYLES_BGCOLOR VARCHAR(15), "
                + "BIBLEVERSIONSTYLES_FONTSIZE INT, "
                + "BIBLEVERSIONSTYLES_VALIGN INT, "
                + "BOOKCHAPTERSTYLES_BOLD BOOLEAN, "
                + "BOOKCHAPTERSTYLES_ITALIC BOOLEAN, "
                + "BOOKCHAPTERSTYLES_UNDERLINE BOOLEAN, "
                + "BOOKCHAPTERSTYLES_STRIKETHROUGH BOOLEAN, "
                + "BOOKCHAPTERSTYLES_TEXTCOLOR VARCHAR(15), "
                + "BOOKCHAPTERSTYLES_BGCOLOR VARCHAR(15), "
                + "BOOKCHAPTERSTYLES_FONTSIZE INT, "
                + "BOOKCHAPTERSTYLES_VALIGN INT, "
                + "VERSENUMBERSTYLES_BOLD BOOLEAN, "
                + "VERSENUMBERSTYLES_ITALIC BOOLEAN, "
                + "VERSENUMBERSTYLES_UNDERLINE BOOLEAN, "
                + "VERSENUMBERSTYLES_STRIKETHROUGH BOOLEAN, "
                + "VERSENUMBERSTYLES_TEXTCOLOR VARCHAR(15), "
                + "VERSENUMBERSTYLES_BGCOLOR VARCHAR(15), "
                + "VERSENUMBERSTYLES_FONTSIZE INT, "
                + "VERSENUMBERSTYLES_VALIGN INT, "
                + "VERSETEXTSTYLES_BOLD BOOLEAN, "
                + "VERSETEXTSTYLES_ITALIC BOOLEAN, "
                + "VERSETEXTSTYLES_UNDERLINE BOOLEAN, "
                + "VERSETEXTSTYLES_STRIKETHROUGH BOOLEAN, "
                + "VERSETEXTSTYLES_TEXTCOLOR VARCHAR(15), "
                + "VERSETEXTSTYLES_BGCOLOR VARCHAR(15), "
                + "VERSETEXTSTYLES_FONTSIZE INT, "
                + "VERSETEXTSTYLES_VALIGN INT, "
                + "LAYOUTPREFS_BIBLEVERSION_SHOW INT, "
                + "LAYOUTPREFS_BIBLEVERSION_ALIGNMENT INT, "
                + "LAYOUTPREFS_BIBLEVERSION_POSITION INT, "
                + "LAYOUTPREFS_BIBLEVERSION_WRAP INT, "
                + "LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT INT, "
                + "LAYOUTPREFS_BOOKCHAPTER_POSITION INT, "
                + "LAYOUTPREFS_BOOKCHAPTER_WRAP INT, "
                + "LAYOUTPREFS_BOOKCHAPTER_FORMAT INT, "
                + "LAYOUTPREFS_BOOKCHAPTER_FULLQUERY BOOLEAN, "
                + "LAYOUTPREFS_VERSENUMBER_SHOW INT, "
                + "PREFERREDVERSIONS VARCHAR(50)"
                + ")";
                
        String optionsTableInitialData = "INSERT INTO OPTIONS ("
                + "PARAGRAPHSTYLES_LINEHEIGHT, "
                + "PARAGRAPHSTYLES_LEFTINDENT, "
                + "PARAGRAPHSTYLES_RIGHTINDENT, "
                + "PARAGRAPHSTYLES_FONTFAMILY, "
                + "PARAGRAPHSTYLES_ALIGNMENT, "
                + "PARAGRAPHSTYLES_NOVERSIONFORMATTING, "
                + "PARAGRAPHSTYLES_INTERFACEINCM, "
                + "BIBLEVERSIONSTYLES_BOLD, "
                + "BIBLEVERSIONSTYLES_ITALIC, "
                + "BIBLEVERSIONSTYLES_UNDERLINE, "
                + "BIBLEVERSIONSTYLES_STRIKETHROUGH, "
                + "BIBLEVERSIONSTYLES_TEXTCOLOR, "
                + "BIBLEVERSIONSTYLES_BGCOLOR, "
                + "BIBLEVERSIONSTYLES_FONTSIZE, "
                + "BIBLEVERSIONSTYLES_VALIGN, "
                + "BOOKCHAPTERSTYLES_BOLD, "
                + "BOOKCHAPTERSTYLES_ITALIC, "
                + "BOOKCHAPTERSTYLES_UNDERLINE, "
                + "BOOKCHAPTERSTYLES_STRIKETHROUGH, "
                + "BOOKCHAPTERSTYLES_TEXTCOLOR, "
                + "BOOKCHAPTERSTYLES_BGCOLOR, "
                + "BOOKCHAPTERSTYLES_FONTSIZE, "
                + "BOOKCHAPTERSTYLES_VALIGN, "
                + "VERSENUMBERSTYLES_BOLD, "
                + "VERSENUMBERSTYLES_ITALIC, "
                + "VERSENUMBERSTYLES_UNDERLINE, "
                + "VERSENUMBERSTYLES_STRIKETHROUGH, "
                + "VERSENUMBERSTYLES_TEXTCOLOR, "
                + "VERSENUMBERSTYLES_BGCOLOR, "
                + "VERSENUMBERSTYLES_FONTSIZE, "
                + "VERSENUMBERSTYLES_VALIGN, "
                + "VERSETEXTSTYLES_BOLD, "
                + "VERSETEXTSTYLES_ITALIC, "
                + "VERSETEXTSTYLES_UNDERLINE, "
                + "VERSETEXTSTYLES_STRIKETHROUGH, "
                + "VERSETEXTSTYLES_TEXTCOLOR, "
                + "VERSETEXTSTYLES_BGCOLOR, "
                + "VERSETEXTSTYLES_FONTSIZE, "
                + "VERSETEXTSTYLES_VALIGN, "
                + "LAYOUTPREFS_BIBLEVERSION_SHOW, "
                + "LAYOUTPREFS_BIBLEVERSION_ALIGNMENT, "
                + "LAYOUTPREFS_BIBLEVERSION_POSITION, "
                + "LAYOUTPREFS_BIBLEVERSION_WRAP, "
                + "LAYOUTPREFS_BOOKCHAPTER_ALIGNMENT, "
                + "LAYOUTPREFS_BOOKCHAPTER_POSITION, "
                + "LAYOUTPREFS_BOOKCHAPTER_WRAP, "
                + "LAYOUTPREFS_BOOKCHAPTER_FORMAT, "
                + "LAYOUTPREFS_BOOKCHAPTER_FULLQUERY, "
                + "LAYOUTPREFS_VERSENUMBER_SHOW, "
                + "PREFERREDVERSIONS"
                + ") VALUES ("
                + "150,0,0,'"+defaultFont+"'," + BGET.ALIGN.JUSTIFY.getValue() + ",false,false,"            //PARAGRAPH STYLES
                + "true,false,false,false,'#0000FF','#FFFFFF',14," + BGET.VALIGN.NORMAL.getValue() + ","    //BIBLE VERSION STYLES
                + "true,false,false,false,'#AA0000','#FFFFFF',12," + BGET.VALIGN.NORMAL.getValue() + ","    //BOOK CHAPTER STYLES
                + "false,false,false,false,'#AA0000','#FFFFFF',10," + BGET.VALIGN.SUPERSCRIPT.getValue() + ","    //VERSENUMBER STYLES
                + "false,false,false,false,'#696969','#FFFFFF',12," + BGET.VALIGN.NORMAL.getValue() + ","    //VERSETEXT STYLES
                + BGET.VISIBILITY.SHOW.getValue() + "," + BGET.ALIGN.LEFT.getValue() + "," + BGET.POS.TOP.getValue() + "," + BGET.WRAP.NONE.getValue() + "," 
                + BGET.ALIGN.LEFT.getValue() + "," + BGET.POS.TOP.getValue() + "," + BGET.WRAP.NONE.getValue() + ","
                + BGET.FORMAT.BIBLELANG.getValue() + ",true," + BGET.VISIBILITY.SHOW.getValue() + ","
                + "'NVBSE'"
                + ")";
        
        final int OptionsTableSchemaVersion = 2;
        BibleGetDB.tableSchemas.put("OPTIONS", new SimpleEntry<>(OptionsTableSchemaVersion, new SimpleEntry<>(optionsTableSchema,optionsTableInitialData)));
        
    }
    
    public static BibleGetDB getInstance() throws ClassNotFoundException, SQLException, Exception {
        if(instance == null)
        {            
            instance = new BibleGetDB();
            boolean dbInitialized = instance.initialize();
            if(dbInitialized){ 
                System.out.println("Database is initialized too!"); 
            }
            else{ 
                System.out.println("Sorry but database has not been initialized."); 
            }
        }
        return instance;        
    }
    
    public boolean connect() {
        try {
            instance.conn = DriverManager.getConnection(
                    "jdbc:derby:BIBLEGET",
                    "bibleget",
                    "bibleget");
        } catch (SQLException ex) {
            if (ex.getNextException().getErrorCode() ==  45000) {
                //this means we already have a connection, so return true (hoping it's not something else that has connected?)
                return true;
            } else {
                //Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex.getMessage() + " : " + Arrays.toString(ex.getStackTrace()));
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    public void disconnect() {
        if(instance.conn != null){
            try {
                instance.conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void setDBSystemDir() {
        String derbyhome = "";
        if(SystemUtils.IS_OS_WINDOWS){
            derbyhome = "/AppData/Roaming/BibleGetOpenOfficePlugin";
        }
        else if(SystemUtils.IS_OS_MAC_OSX){
            derbyhome = "/Library/Application Support/BibleGetOpenOfficePlugin";
        }
        else if(SystemUtils.IS_OS_LINUX){
            derbyhome = "/.BibleGetOpenOfficePlugin";
        }
        System.setProperty("derby.system.home", System.getProperty("user.home") + derbyhome);
    }
 

    public boolean initialize() throws SQLException, Exception {
    
        try {
            instance.conn = DriverManager.getConnection(
                    "jdbc:derby:BIBLEGET;create=true",
                    "bibleget",
                    "bibleget");
            if(instance.conn==null){ 
                System.out.println("Careful there! Connection not established! BibleGetDB.java line 262");
                return false;
            }
            else{
                System.out.println("instance.conn is not null, which means a connection was correctly established in order to create the BibleGet database.");
                //Since it is confirmed that we are creating the Database, then we obviously need to initialize the schemas
                //(create the tables and populate with data)
                return instance.initializeSchemas();
            }
        } catch (SQLException ex) {
            if( ex.getSQLState().equals("X0Y32") ) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.INFO, null, "Database BIBLEGET already exists.  No need to recreate");
                System.out.println("Database BIBLEGET already exists.  No need to recreate: "+ex.getMessage());
                //return instance.getOrSetDBData("GET");
                return instance.initializeSchemas();
            } else if (ex.getNextException().getErrorCode() ==  45000) {
                //this means another JVM has already connected to the database, which means we cannot use it (perhaps Netbeans)
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, "Another JVM is already connected to the BIBLEGET database. We cannot use it like this.");
                System.out.println("Seems like we already have a connection: "+ex.getMessage()+" "+ex.getNextException().getMessage());
                return false;
            } else {
                //Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex.getMessage() + " : " + Arrays.toString(ex.getStackTrace()));
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Not sure what the problem might be with the database connection: "+ex.getMessage());
                return false;
            }
        }                
    }
   
    
    
    private boolean initializeSchemas() throws SQLException, Exception{
        DatabaseMetaData dbMeta;
        dbMeta = instance.conn.getMetaData();

        //first table to check is the SCHEMA_VERSIONS table so we don't have to check for every other table initialization
        //if it doesn't exist, but the other tables exist, then their schemas obviously need updating
        //the easiest way to do this is simply delete them and recreate them
        //with the adverse side effect that user preferences will be lost
        //not a huge loss, even though it's not the best user experience...
        //for less drastic changes maybe it will be easier to transition user preferences to a new table
        if(instance.tableExists("SCHEMA_VERSIONS") == false){
            instance.createTable("SCHEMA_VERSIONS");
            instance.initializeTable("SCHEMA_VERSIONS");
            //since creating the SCHEMA_VERSIONS table is equivalent to updating the schemas,
            //we also have to empty and re-initialize any other existing tables
            Set<String> keys = BibleGetDB.tableSchemas.keySet();            
            for(String tableName : keys){
                if(instance.tableExists(tableName)){
                    instance.deleteTable(tableName);
                    if(instance.createTable(tableName)){
                        instance.initializeTable(tableName);
                    }
                } else {
                    if(instance.createTable(tableName)){
                        instance.initializeTable(tableName);
                    }
                }
            }
        }

        
        if(instance.tableExists("OPTIONS")){
            System.out.println("Table " + instance.currentTable.getString("TABLE_NAME") + " already exists, now adding Column names to colNames array, and corresponding Data Types to colDataTypes array !!");
            //CHECK THE SCHEMA VERSION!
            
            listColNamesTypes(dbMeta,instance.currentTable);
        } else {
            System.out.println("Table OPTIONS does not yet exist, now attempting to create...");
            if(instance.createTable("OPTIONS")){
                if(instance.initializeTable("OPTIONS")){
                    dbMeta = instance.conn.getMetaData();
                    if(instance.tableExists("OPTIONS")){
                        listColNamesTypes(dbMeta,instance.currentTable);
                    }                
                }
            }
        }
        instance.currentTable.close();
        //System.out.println("Finished with first ResultSet resource, now going on to next...");
        try (ResultSet rs3 = dbMeta.getTables(null, null, "METADATA", null)) {
            if(rs3.next())
            {
                System.out.println("Table "+rs3.getString("TABLE_NAME")+" already exists !!");
            }
            else{
                System.out.println("Table METADATA does not exist, now attempting to create...");
                try (Statement stmt = instance.conn.createStatement()) {
                    String tableCreate = "CREATE TABLE METADATA (";
                    tableCreate += "ID INT, ";
                    for(int i=0;i<73;i++){
                        tableCreate += "BIBLEBOOKS"+Integer.toString(i)+" VARCHAR(2000), ";
                    }
                    tableCreate += "LANGUAGES VARCHAR(500), ";
                    tableCreate += "VERSIONS VARCHAR(2000)";
                    tableCreate += ")";
                    boolean tableCreated = stmt.execute(tableCreate);
                    boolean rowsInserted;
                    int count;
                    if(tableCreated==false){
                        //this is the expected result, is false when it's an update count!
                        count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results.");
                        }
                        else{
                            //this is our expected behaviour: 0 rows affected
                            System.out.println("The Table Creation statement produced results: "+count+" rows affected.");
                            //Insert a dummy row, because you cannot update what has not been inserted!                                
                            try ( Statement stmtX = instance.conn.createStatement()) {
                                stmtX.execute("INSERT INTO METADATA (ID) VALUES (0)");
                                stmtX.close();
                            }

                            HTTPCaller myHTTPCaller = new HTTPCaller();
                            System.out.println(this.getClass().getSimpleName() + " >> We should now have an instance of the HTTPCaller class.");
                            String myResponse;
                            myResponse = myHTTPCaller.getMetaData("biblebooks");
                            if(myResponse != null){
                                System.out.println(this.getClass().getSimpleName() + " >> response from myHTTPCaller.getMetaData(\"biblebooks\") call is:");
                                System.out.println(myResponse);
                                JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                JsonObject json = jsonReader.readObject();
                                JsonArray arrayJson = json.getJsonArray("results");
                                if(arrayJson != null){

                                    ListIterator pIterator = arrayJson.listIterator();
                                    while (pIterator.hasNext())
                                    {
                                        try(Statement stmt2 = instance.conn.createStatement()) {
                                            int index = pIterator.nextIndex();
                                            JsonArray currentJson = (JsonArray) pIterator.next();
                                            //TODO: double check that JsonArray.toString is working as intended!
                                            String biblebooks_str = currentJson.toString(); //.replaceAll("\"", "\\\\\"");
                                            System.out.println("BibleGetDB line 427: BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"'"); 
                                            String stmt_str = "UPDATE METADATA SET BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"' WHERE ID=0";
                                            try{
                                                //System.out.println("executing update: "+stmt_str);
                                                int update = stmt2.executeUpdate(stmt_str);
                                                //System.out.println("executeUpdate resulted in: "+Integer.toString(update));
                                            } catch (SQLException ex){
                                                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            stmt2.close();
                                        }
                                    }
                                }

                                arrayJson = json.getJsonArray("languages");
                                if(arrayJson != null){
                                    try(Statement stmt2 = instance.conn.createStatement()) {
                                        //TODO: double check that JsonArray.toString is working as intended!
                                        String languages_str = arrayJson.toString(); //.replaceAll("\"", "\\\\\"");
                                        String stmt_str = "UPDATE METADATA SET LANGUAGES='"+languages_str+"' WHERE ID=0";
                                        try{
                                            int update = stmt2.executeUpdate(stmt_str);
                                        } catch (SQLException ex){
                                            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        stmt2.close();
                                    }                                    
                                }
                            } else { 
                                System.out.println(this.getClass().getSimpleName() + " >> myResponse is null!!!!");
                            }

                            myResponse = myHTTPCaller.getMetaData("bibleversions");
                            if(myResponse != null){
                                JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                JsonObject json = jsonReader.readObject();
                                JsonObject objJson = json.getJsonObject("validversions_fullname");
                                if(objJson != null){
                                    //TODO: double check that JsonObject.toString is working as intended!
                                    String bibleversions_str = objJson.toString(); //.replaceAll("\"", "\\\\\"");
                                    try(Statement stmt2 = instance.conn.createStatement()){
                                        String stmt_str = "UPDATE METADATA SET VERSIONS='"+bibleversions_str+"' WHERE ID=0";
                                        try{
                                            int update = stmt2.executeUpdate(stmt_str);
                                        } catch (SQLException ex){
                                            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        stmt2.close();
                                    }

                                    Set<String> versionsabbrev = objJson.keySet();
                                    if(!versionsabbrev.isEmpty()){
                                        String versionsabbrev_str = "";
                                        for(String s:versionsabbrev) {
                                            versionsabbrev_str += ("".equals(versionsabbrev_str)?"":",")+s;
                                        }

                                        myResponse = myHTTPCaller.getMetaData("versionindex&versions="+versionsabbrev_str);
                                        if(myResponse != null){
                                            jsonReader = Json.createReader(new StringReader(myResponse));
                                            json = jsonReader.readObject();
                                            objJson = json.getJsonObject("indexes");
                                            if(objJson != null){

                                                for (String name : objJson.keySet()){
                                                    JsonObjectBuilder tempBld = Json.createObjectBuilder();
                                                    JsonObject book_num = objJson.getJsonObject(name);
                                                    tempBld.add("book_num", book_num.getJsonArray("book_num"));
                                                    tempBld.add("chapter_limit", book_num.getJsonArray("chapter_limit"));
                                                    tempBld.add("verse_limit", book_num.getJsonArray("verse_limit"));
                                                    JsonObject temp = tempBld.build();
                                                    //TODO: double check that JsonObject.toString is working as intended!
                                                    String versionindex_str = temp.toString(); //.replaceAll("\"", "\\\\\"");
                                                    //add new column to METADATA table name+"IDX" VARCHAR(5000)
                                                    //update METADATA table SET name+"IDX" = versionindex_str
                                                    try(Statement stmt3 = instance.conn.createStatement()){
                                                        String sql = "ALTER TABLE METADATA ADD COLUMN "+name+"IDX VARCHAR(5000)";
                                                        boolean colAdded = stmt3.execute(sql);
                                                        if(colAdded==false) {
                                                            count = stmt3.getUpdateCount();
                                                            if(count==-1){
                                                                //System.out.println("The result is a ResultSet object or there are no more results.");
                                                            }
                                                            else if(count==0){
                                                                //0 rows affected
                                                                stmt3.close();

                                                                try(Statement stmt4 = instance.conn.createStatement()){
                                                                    String sql1 = "UPDATE METADATA SET "+name+"IDX='"+versionindex_str+"' WHERE ID=0";
                                                                    boolean rowsUpdated = stmt4.execute(sql1);
                                                                    if(rowsUpdated==false) {
                                                                        count = stmt4.getUpdateCount();
                                                                        if(count==-1){
                                                                            //System.out.println("The result is a ResultSet object or there are no more results.");
                                                                        }
                                                                        else{
                                                                            //should have affected only one row
                                                                            if(count==1){
                                                                                //System.out.println(sql1+" seems to have returned true");
                                                                                stmt4.close();
                                                                            }
                                                                        }
                                                                    }
                                                                    else{
                                                                        //returns true only when returning a resultset; should not be the case here
                                                                    }

                                                                }

                                                            }
                                                        }
                                                        else{
                                                            //returns true only when returning a resultset; should not be the case here
                                                        }

                                                        stmt3.close();
                                                    }
                                                }

                                            }
                                        }

                                    }



                                }
                            }

                        }
                    }
                    else{
                        //is true when it returns a resultset, which shouldn't be the case here
                        ResultSet rx = stmt.getResultSet();
                        while(rx.next()){
                            //System.out.println("This isn't going to happen anyways, so...");
                        }
                    }
                    stmt.close();
                }
            }
            rs3.close();
        }
        catch(SQLException ex){
            System.out.println("Error retrieving Metadata table: "+ex.getMessage());
            return false;
        }
        if(instance.conn != null){
            try {
                instance.conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }
        
    private void listColNamesTypes(DatabaseMetaData dbMeta, ResultSet rs) {
        //System.out.println("After Table Creation: Table "+rs.getString("TABLE_NAME")+" exists !!");
        ResultSet cols;
        try {
            cols = dbMeta.getColumns(null, null, rs.getString("TABLE_NAME"), null);
            while(cols.next()){
                //System.out.println(cols.getString("COLUMN_NAME"));
                colNames.add(cols.getString("COLUMN_NAME"));
                int dType = cols.getInt("DATA_TYPE");
                switch(dType){
                    case Types.VARCHAR:
                        colDataTypes.add(String.class); break;
                    case Types.INTEGER:
                        colDataTypes.add(Integer.class); break;
                    case Types.FLOAT:
                        colDataTypes.add(Float.class); break;
                    case Types.DOUBLE:
                    case Types.REAL:
                        colDataTypes.add(Double.class); break;
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        colDataTypes.add(java.sql.Date.class); break;
                    case Types.BOOLEAN:
                        colDataTypes.add(Boolean.class); break;
                    default:
                        colDataTypes.add(String.class); break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public JsonObject getOptions() {
        if(instance.connect()){
            try {
                JsonObjectBuilder myOptionsTable;
                JsonArrayBuilder myRows;
                try (Statement stmt = instance.conn.createStatement(); ResultSet rsOps = stmt.executeQuery("SELECT * FROM OPTIONS")) {
                    Iterator itColNames = colNames.iterator();
                    Iterator itDataTypes = colDataTypes.iterator();
                    myOptionsTable = Json.createObjectBuilder();
                    myRows = Json.createArrayBuilder();
                    while (rsOps.next()) {
                        //System.out.println("Getting a row from the table.");
                        JsonObjectBuilder thisRow = Json.createObjectBuilder();
                        while(itColNames.hasNext() && itDataTypes.hasNext()){
                            String colName = (String) itColNames.next();
                            Class dataType = (Class) itDataTypes.next();
                            if(dataType==String.class){ 
                                thisRow.add(colName, rsOps.getString(colName)); 
                                //System.out.println("BibleGetDB.java: "+colName+" has a string datatype, value is "+rsOps.getString(colName));
                            }
                            if(dataType==Integer.class){ thisRow.add(colName, rsOps.getInt(colName)); }
                            if(dataType==Boolean.class){ thisRow.add(colName, rsOps.getBoolean(colName)); }
                            //System.out.println(colName + " <" + dataType + ">");
                        }
                        JsonObject currentRow = thisRow.build();
                        //System.out.println("BibleGetDB.java: thisRow = "+currentRow.toString());
                        myRows.add(currentRow);
                    }
                }
                instance.disconnect();
                JsonArray currentRows = myRows.build();
                JsonObject finalOptionsJson = myOptionsTable.add("rows", currentRows).build();
                //System.out.println("BibleGetDB.java: finalOptionsJson = "+finalOptionsJson.toString());
                return finalOptionsJson;
                
            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }
    
    public Object getOption(String option){
        option = option.toUpperCase();
        Object returnObj = null;
        if(instance.connect()){
            try{
                if(colNames.contains(option)){
                    int idx = colNames.indexOf(option);
                    Class dataType = colDataTypes.get(idx);
                    //System.out.println(BibleGetDB.class.getName()+" [299]: dataType="+dataType.getName() );
                    try (Statement stmt = instance.conn.createStatement()) {
                        String sqlexec = "SELECT "+option+" FROM OPTIONS";
                        try (ResultSet rsOps = stmt.executeQuery(sqlexec)) {
                            while (rsOps.next()) {
                                //System.out.println(BibleGetDB.class.getName()+" [304]: retrieved a value from DB, about to return it to calling function" );
                                if(dataType==String.class){ returnObj = rsOps.getString(option); }
                                if(dataType==Integer.class){ returnObj = rsOps.getInt(option); }
                                if(dataType==Boolean.class){ returnObj = rsOps.getBoolean(option); }
                            }
                            rsOps.close();
                        }
                        stmt.close();
                    }
                    instance.disconnect();
                    return returnObj;
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }
    
    public String getMetaData(String dataOption){
        dataOption = StringUtils.upperCase(dataOption);
        String metaDataStr = "";
        if(dataOption.startsWith("BIBLEBOOKS") || dataOption.equals("LANGUAGES") || dataOption.equals("VERSIONS") || dataOption.endsWith("IDX")){
            System.out.println("getMetaData received a valid request for "+dataOption);
            if(instance.connect()){
                if(instance.conn == null){
                    System.out.println("What is going on here? Why is connection null?");
                }else{
                    System.out.println("getMetaData has connected to the database...");
                }
                String sqlexec = "SELECT "+dataOption+" FROM METADATA WHERE ID=0";
                try(Statement stmt = instance.conn.createStatement()){
                    try (ResultSet rsOps = stmt.executeQuery(sqlexec)) {
                        System.out.println("query seems to have been successful...");
                        ResultSetMetaData rsMD = rsOps.getMetaData();
                        int cols = rsMD.getColumnCount();
                        String colnm = rsMD.getColumnName(cols);
                        System.out.println("there are "+Integer.toString(cols)+" columns in this resultset and name is: "+colnm+"(requested "+dataOption+")");
                        while(rsOps.next()){
                            metaDataStr = rsOps.getString(dataOption);
                        }
                        rsOps.close();
                    }
                    stmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                }
                instance.disconnect();
            }
        }        
        return metaDataStr;
    }
    
    public boolean setIntOption(String colname, int value) {
        int count;
        colname = colname.toUpperCase();
        if(instance.connect()){
            if(!colNames.contains(colname)){
                boolean result = addColumn(colname,"INT");
                if(result==false){ return false; }
                //System.out.println("Added "+colname+" column of type INT to OPTIONS table");
                colNames.add(colname);
                colDataTypes.add(Integer.class);
            }
            try {
                Statement stmt = instance.conn.createStatement();
                String sqlexec = "UPDATE OPTIONS SET "+colname+" = "+value+"";
                boolean rowsUpdated = stmt.execute(sqlexec);
                if(rowsUpdated==false) {
                    count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results."); 
                        }
                        else{
                            //should have affected only one row
                            if(count==1){ 
                                stmt.close();
                                instance.disconnect();
                                return true; 
                            }
                        }
                }
                else{
                    //returns true only when returning a resultset; should not be the case here
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean setStringOption(String colname, String value) {
        int count;
        colname = colname.toUpperCase();
        if(instance.connect()){
            if(!colNames.contains(colname)){
                boolean result = addColumn(colname,"VARCHAR(50)");
                if(result==false){ return false; }
                //System.out.println("Added "+colname+" column of type VARCHAR(50) to OPTIONS table");
                colNames.add(colname);
                colDataTypes.add(String.class);
            }
            try {
                Statement stmt = instance.conn.createStatement();
                String sqlexec = "UPDATE OPTIONS SET "+colname+" = '"+value+"'";
                boolean rowsUpdated = stmt.execute(sqlexec);
                if(rowsUpdated==false) {
                    count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results."); 
                        }
                        else{
                            //should have affected only one row
                            if(count==1){ 
                                //System.out.println(sqlexec+" seems to have returned true");
                                stmt.close();
                                instance.disconnect();
                                return true; 
                            }
                        }
                }
                else{
                    //returns true only when returning a resultset; should not be the case here
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean setBooleanOption(String colname, boolean value) {
        int count;
        colname = colname.toUpperCase();
        if(instance.connect()){
            if(!colNames.contains(colname)){
                boolean result = addColumn(colname,"BOOLEAN");
                if(result==false){ return false; }
                //System.out.println("Added "+colname+" column of type BOOLEAN to OPTIONS table");
                colNames.add(colname);
                colDataTypes.add(Boolean.class);
            }
            try {
                Statement stmt = instance.conn.createStatement();
                String sqlexec = "UPDATE OPTIONS SET "+colname+" = "+value+"";
                boolean rowsUpdated = stmt.execute(sqlexec);
                if(rowsUpdated==false) {
                    count = stmt.getUpdateCount();
                        if(count==-1){
                            //System.out.println("The result is a ResultSet object or there are no more results."); 
                        }
                        else{
                            //should have affected only one row
                            if(count==1){ 
                                stmt.close();
                                instance.disconnect();
                                return true; 
                            }
                        }
                }
                else{
                    //returns true only when returning a resultset; should not be the case here
                }

            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean addColumn(String colName,String type){
        int count;
        try {
            colName = colName.toUpperCase();
            Statement stmt = instance.conn.createStatement();
            String sqlexec = "ALTER TABLE OPTIONS ADD COLUMN "+colName+" "+type;
            boolean colAdded = stmt.execute(sqlexec);
            
            if(colAdded==false) {
                count = stmt.getUpdateCount();
                    if(count==-1){
                        //System.out.println("The result is a ResultSet object or there are no more results."); 
                    }
                    else if(count==0){
                        //0 rows affected
                        stmt.close();
                        return true;
                    }
            }
            else{
                //returns true only when returning a resultset; should not be the case here
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean renewMetaData(){
        if(instance.connect()){
            try {
                DatabaseMetaData dbMeta;
                dbMeta = instance.conn.getMetaData();
                try (ResultSet rs3 = dbMeta.getTables(null, null, "METADATA", null)) {
                    if(rs3.next())
                    {
                        //System.out.println("Table METADATA exists...");
                        try (Statement stmt = instance.conn.createStatement()) {
                            HTTPCaller myHTTPCaller = new HTTPCaller();
                            String myResponse;
                            myResponse = myHTTPCaller.getMetaData("biblebooks");
                            if(myResponse != null){
                                JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                JsonObject json = jsonReader.readObject();
                                JsonArray arrayJson = json.getJsonArray("results");
                                if(arrayJson != null){                                    
                                    ListIterator pIterator = arrayJson.listIterator();
                                    while (pIterator.hasNext())
                                    {
                                        try(Statement stmt1 = instance.conn.createStatement()) {
                                            int index = pIterator.nextIndex();
                                            JsonArray currentJson = (JsonArray) pIterator.next();
                                            //TODO: double check that JsonArray.toString is working as intended
                                            String biblebooks_str = currentJson.toString(); //.replaceAll("\"", "\\\\\"");
                                            //System.out.println("BibleGetDB line 267: BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"'");
                                            String stmt_str = "UPDATE METADATA SET BIBLEBOOKS"+Integer.toString(index)+"='"+biblebooks_str+"' WHERE ID=0";
                                            //System.out.println("executing update: "+stmt_str);
                                            int update = stmt1.executeUpdate(stmt_str);
                                            //System.out.println("executeUpdate resulted in: "+Integer.toString(update));
                                            stmt1.close();
                                        }
                                    }
                                }
                                
                                arrayJson = json.getJsonArray("languages");
                                if(arrayJson != null){
                                    try(Statement stmt2 = instance.conn.createStatement()) {                                        
                                        //TODO: double check that JsonArray.toString is working as intended
                                        String languages_str = arrayJson.toString(); //.replaceAll("\"", "\\\\\"");
                                        String stmt_str = "UPDATE METADATA SET LANGUAGES='"+languages_str+"' WHERE ID=0";
                                        int update = stmt2.executeUpdate(stmt_str);
                                        stmt2.close();
                                    }
                                }
                            }
                            
                            myResponse = myHTTPCaller.getMetaData("bibleversions");
                            if(myResponse != null){
                                JsonReader jsonReader = Json.createReader(new StringReader(myResponse));
                                JsonObject json = jsonReader.readObject();
                                JsonObject objJson = json.getJsonObject("validversions_fullname");
                                if(objJson != null){
                                    //TODO: double check that JsonObject.toString is working as intended
                                    String bibleversions_str = objJson.toString(); //.replaceAll("\"", "\\\\\"");
                                    try(Statement stmt3 = instance.conn.createStatement()){
                                        String stmt_str = "UPDATE METADATA SET VERSIONS='"+bibleversions_str+"' WHERE ID=0";
                                        int update = stmt3.executeUpdate(stmt_str);
                                        stmt3.close();
                                    }
                                    
                                    Set<String> versionsabbrev = objJson.keySet();
                                    if(!versionsabbrev.isEmpty()){
                                        String versionsabbrev_str = "";
                                        for(String s:versionsabbrev) {
                                            versionsabbrev_str += ("".equals(versionsabbrev_str)?"":",")+s;
                                        }
                                        
                                        myResponse = myHTTPCaller.getMetaData("versionindex&versions="+versionsabbrev_str);
                                        if(myResponse != null){
                                            jsonReader = Json.createReader(new StringReader(myResponse));
                                            json = jsonReader.readObject();
                                            objJson = json.getJsonObject("indexes");
                                            if(objJson != null){
                                                for (String name : objJson.keySet()){
                                                    JsonObjectBuilder tempBld = Json.createObjectBuilder();
                                                    JsonObject book_num = objJson.getJsonObject(name);
                                                    tempBld.add("book_num", book_num.getJsonArray("book_num"));
                                                    tempBld.add("chapter_limit", book_num.getJsonArray("chapter_limit"));
                                                    tempBld.add("verse_limit", book_num.getJsonArray("verse_limit"));
                                                    JsonObject temp = tempBld.build();
                                                    //TODO: double check that JsonObject.toString is working as intended
                                                    String versionindex_str = temp.toString(); //.replaceAll("\"", "\\\\\"");
                                                    //add new column to METADATA table name+"IDX" VARCHAR(5000)
                                                    //update METADATA table SET name+"IDX" = versionindex_str
                                                    try(ResultSet rs1 = dbMeta.getColumns(null, null, "METADATA", name+"IDX")){
                                                        boolean updateFlag = false;
                                                        if(rs1.next()){
                                                            //column already exists
                                                            updateFlag=true;
                                                        }
                                                        else{
                                                            try(Statement stmt4 = instance.conn.createStatement()){
                                                                String sql = "ALTER TABLE METADATA ADD COLUMN "+name+"IDX VARCHAR(5000)";
                                                                boolean colAdded = stmt4.execute(sql);
                                                                if(colAdded==false) {
                                                                    int count = stmt4.getUpdateCount();
                                                                    if(count==-1){
                                                                        //System.out.println("The result is a ResultSet object or there are no more results.");
                                                                    }
                                                                    else if(count==0){
                                                                        //0 rows affected
                                                                        updateFlag=true;
                                                                    }
                                                                }
                                                                stmt4.close();
                                                            }
                                                        }
                                                        if(updateFlag){
                                                            try(Statement stmt5 = instance.conn.createStatement()){
                                                                String sql1 = "UPDATE METADATA SET "+name+"IDX='"+versionindex_str+"' WHERE ID=0";
                                                                boolean rowsUpdated = stmt5.execute(sql1);
                                                                stmt5.close();                                                                    
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                            }
                                        }
                                        
                                    }

                                    
                                    
                                }
                            }
                            
                            stmt.close();
                        }
                    }
                    rs3.close();
                }
                instance.disconnect();
            }   catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }
        return false;
    }
    
    public boolean tableExists(String tableName) throws Exception{
        try {
            DatabaseMetaData dbMeta;
            dbMeta = instance.conn.getMetaData();
            if(!"".equals(tableName)){
                ResultSet rs = dbMeta.getTables(null, null, tableName, null); //catalog, pattern, tablename, types
                if(rs.next()){
                    instance.currentTable = rs;
                    return true;
                }
            } else {
                throw new Exception("Table name cannot be an empty string!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean createTable(String tableName) throws Exception{
        if(!"".equals(tableName)){
            try(Statement stmt = instance.conn.createStatement()) {
                Entry<Integer, Entry<String,String>> tableSchemaEntry = BibleGetDB.tableSchemas.get(tableName);
                //int schemaDefinitionVersion = tableSchemaEntry.getKey();
                Entry<String,String> tableSchemaDefData = tableSchemaEntry.getValue();
                String schemaDefinition = tableSchemaDefData.getKey();
                //String schemaInitialData = tableSchemaDefData.getValue();
                if(stmt.execute(schemaDefinition)==false && stmt.getUpdateCount() != -1){
                    stmt.close();
                    return true;
                }
            } catch (SQLException ex) {
                Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new Exception("Table name cannot be an empty string!");
        }
        return false;
    }
    
    public boolean initializeTable(String tableName) throws Exception{
        if(!"".equals(tableName)){
            if("SCHEMA_VERSIONS".equals(tableName)){
                Set<String> keys = BibleGetDB.tableSchemas.keySet();
                String tableNames[] = keys.toArray(new String[keys.size()]);
                int[] schemaVersions = new int[keys.size()];
                int idx = 0;
                for(String schemaName : tableNames) {
                    Entry<Integer, Entry<String,String>> tableSchemaEntry = BibleGetDB.tableSchemas.get(schemaName);
                    //System.out.println(pair.getKey() + " = " + pair.getValue());
                    schemaVersions[idx++] = tableSchemaEntry.getKey();
                }
                String schemaVersionsStr;
                //schemaVersionsStr = StringUtils.join(ArrayUtils.toObject(schemaVersions), ",");
                //the above is the same as below; below only from JAVA 8
                schemaVersionsStr = Arrays.stream(schemaVersions).mapToObj(String::valueOf)
                    .collect(Collectors.joining(","));
                try(Statement stmt = instance.conn.createStatement()){
                    String initializeSchemaVersions = "INSERT INTO SCHEMA_VERSIONS (" + String.join(",", tableNames) + ") VALUES (" + schemaVersionsStr + ")";
                    if(stmt.execute(initializeSchemaVersions)==false && stmt.getUpdateCount() != -1){
                        stmt.close();
                        return true;
                    }
                } catch (SQLException ex){
                    Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //all other cases
                try(Statement stmt = instance.conn.createStatement()) {
                    Entry<Integer, Entry<String,String>> tableSchemaEntry = BibleGetDB.tableSchemas.get(tableName);
                    //int schemaDefinitionVersion = tableSchemaEntry.getKey();
                    Entry<String,String> tableSchemaDefData = tableSchemaEntry.getValue();
                    //String schemaDefinition = tableSchemaDefData.getKey();
                    String schemaInitialData = tableSchemaDefData.getValue();
                    if(stmt.execute(schemaInitialData)==false && stmt.getUpdateCount() != -1){
                        stmt.close();
                        return true;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } else {
            throw new Exception("Table name cannot be an empty string!");
        }
        return false;
    }
    
    public boolean deleteTable(String tableName) throws Exception{
        try(Statement stmt = instance.conn.createStatement()) {
            stmt.execute("DROP TABLE " + tableName);
            return(!instance.tableExists(tableName));
        } catch(SQLException ex){
            Logger.getLogger(BibleGetDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
}
