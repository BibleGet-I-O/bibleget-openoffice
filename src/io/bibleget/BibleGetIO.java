package io.bibleget;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameAccess;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XViewSettingsSupplier;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;

public final class BibleGetIO extends WeakBase
   implements com.sun.star.frame.XDispatchProvider,
              com.sun.star.frame.XDispatch,
              com.sun.star.lang.XServiceInfo,
              com.sun.star.lang.XInitialization
{
    public static final double VERSION = 2.8;
    public static final String PLUGINVERSION = "v" + String.valueOf(BibleGetIO.VERSION).replace(".", "_") ;

    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = BibleGetIO.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler" };
    
    private com.sun.star.frame.XController m_xController;
    //private com.sun.star.frame.XModel m_xModel;
    //private com.sun.star.text.XTextDocument m_xTextDocument;

    private static String packagePath;
    private static BibleGetQuoteFrame myFrame;
    private static BibleGetOptionsFrame myOptionFrame;
    private static DefaultComboBoxModel fontFamilies;
    private static BibleGetHelpFrame myHelpFrame;   
    private static BibleGetSelection quoteFromSelection;
    private static DBHelper biblegetDB;
    private static BibleGetAboutFrame bibleGetAbout;
    private static BibleGetSearchFrame bibleGetSearch;
    
    private static String myLocale;
    private static Locale uiLocale;
    //public ResourceBundle myMessages;
    public static BGET.MEASUREUNIT measureUnit;
    
    public static int JAVAVERSION;

    private static CefApp cefApp;
    public static CefClient client;
    
    private static BibleGetIO instance;
    
    public BibleGetIO( XComponentContext context )
    {
        m_xContext = context;
    }

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(BibleGetIO.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.frame.XDispatchProvider:

    /**
     *
     * @param aURL
     * @param sTargetFrameName
     * @param iSearchFlags
     * @return
     */
        @Override
    public com.sun.star.frame.XDispatch queryDispatch( com.sun.star.util.URL aURL,
                                                       String sTargetFrameName,
                                                       int iSearchFlags )
    {
        if ( aURL.Protocol.compareTo("io.bibleget.biblegetio:") == 0 )
        {
            if ( aURL.Path.compareTo("QuoteFromSelection") == 0 )
                return this;
            if ( aURL.Path.compareTo("Options") == 0 )
                return this;
            if ( aURL.Path.compareTo("About") == 0 )
                return this;
            if ( aURL.Path.compareTo("QuoteFromInputPrompt") == 0 )
                return this;
            if ( aURL.Path.compareTo("SendFeedback") == 0 )
                return this;
            if ( aURL.Path.compareTo("Help") == 0 )
                return this;
            if ( aURL.Path.compareTo("Contribute") == 0 )
                return this;
            if ( aURL.Path.compareTo("Search") == 0 )
                return this;
        }
        return null;
    }

    // com.sun.star.frame.XDispatchProvider:

    /**
     *
     * @param seqDescriptors
     * @return
     */
        @Override
    public com.sun.star.frame.XDispatch[] queryDispatches(
         com.sun.star.frame.DispatchDescriptor[] seqDescriptors )
    {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
            new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i )
        {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                                             seqDescriptors[i].FrameName,
                                             seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }

    // com.sun.star.frame.XDispatch:

    /**
     *
     * @param aURL
     * @param aArguments
     */
        @Override
     public void dispatch( com.sun.star.util.URL aURL,
                           com.sun.star.beans.PropertyValue[] aArguments )
    {
         if ( aURL.Protocol.compareTo("io.bibleget.biblegetio:") == 0 )
        {
            if ( aURL.Path.compareTo("QuoteFromSelection") == 0 )
            {
                // add your own code here
                //JOptionPane.showMessageDialog(null, "QuoteFromSelection was pressed", "InfoBox: which button?", JOptionPane.INFORMATION_MESSAGE);
                try {
                    if(BibleGetIO.quoteFromSelection != null){
                        BibleGetIO.quoteFromSelection.getQuoteFromSelection();
                    }
                    else{
                        BibleGetIO.quoteFromSelection = BibleGetSelection.getInstance(instance.m_xController);
                        BibleGetIO.quoteFromSelection.getQuoteFromSelection();
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            if ( aURL.Path.compareTo("QuoteFromInputPrompt") == 0 )
            {
                try {
                    // add your own code here
                    if(BibleGetIO.myFrame != null ){
                        BibleGetIO.myFrame.setVisible(true);
                    }
                    else{
                        BibleGetIO.myFrame = BibleGetQuoteFrame.getInstance(instance.m_xController);
                        BibleGetIO.myFrame.setVisible(true);                
                    }
                } catch (ClassNotFoundException | SQLException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            if ( aURL.Path.compareTo("Options") == 0 )
            {
                try {
                    // add your own code here
                    if(BibleGetIO.myOptionFrame != null ){
                        //System.out.println("We already have an options windows, now making it visible");
                        BibleGetIO.myOptionFrame.setVisible(true);
                    }
                    else{
                        BibleGetIO.myOptionFrame = BibleGetOptionsFrame.getInstance(instance.m_xController);
                        BibleGetIO.myOptionFrame.setVisible(true);
                    }
                } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            if ( aURL.Path.compareTo("About") == 0 )
            {
                try {
                    // add your own code here
                    if(BibleGetIO.bibleGetAbout != null ){
                        //System.out.println("We already have an options windows, now making it visible");
                        BibleGetIO.bibleGetAbout.setVisible(true);
                    }
                    else{
                        //make sure we also have the database initialized first
                        BibleGetIO.biblegetDB = DBHelper.getInstance();                        
                        BibleGetIO.bibleGetAbout = BibleGetAboutFrame.getInstance();
                        BibleGetIO.bibleGetAbout.setVisible(true);
                    }
                } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
                //JOptionPane.showMessageDialog(null, msg, __("About this plugin"), JOptionPane.INFORMATION_MESSAGE);
            }
            if ( aURL.Path.compareTo("SendFeedback") == 0 )
            {
                
                try {
                    // add your own code here
                    sendMail();
                } catch (IOException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            if ( aURL.Path.compareTo("Help") == 0 )
            {
                if(BibleGetIO.myHelpFrame != null ){
                    //System.out.println("We already have an options windows, now making it visible");
                    BibleGetIO.myHelpFrame.setVisible(true);
                }
                else{
                    try {
                        BibleGetIO.myHelpFrame = BibleGetHelpFrame.getInstance();
                        BibleGetIO.myHelpFrame.setVisible(true);
                    } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
                        Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (java.lang.Exception ex) {
                        Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return;
            }
            if ( aURL.Path.compareTo("Contribute") == 0 )
            {
                try {
                    // add your own code here
                    //JOptionPane.showMessageDialog(null, "Contribute was pressed", "InfoBox: which button?", JOptionPane.INFORMATION_MESSAGE);
                    URL contributeURL = new URL("https://paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HDS7XQKGFHJ58");
                    openWebpage(contributeURL);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException | URISyntaxException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
            if ( aURL.Path.compareTo("Search") == 0 )
            {
                try {
                    // add your own code here
                    if(BibleGetIO.bibleGetSearch != null ){
                        //System.out.println("We already have an options windows, now making it visible");
                        BibleGetIO.bibleGetSearch.setVisible(true);
                    }
                    else{
                        //make sure we also have the database initialized first
                        //BibleGetIO.biblegetDB = DBHelper.getInstance();                        
                        BibleGetIO.bibleGetSearch = BibleGetSearchFrame.getInstance();
                        BibleGetIO.bibleGetSearch.setVisible(true);
                    }
                } catch (ClassNotFoundException | UnsupportedEncodingException | SQLException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     *
     * @param xControl
     * @param aURL
     */
    @Override
    public void addStatusListener( com.sun.star.frame.XStatusListener xControl,
                                    com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

    /**
     *
     * @param xControl
     * @param aURL
     */
    @Override
    public void removeStatusListener( com.sun.star.frame.XStatusListener xControl,
                                       com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

    // com.sun.star.lang.XServiceInfo:

    /**
     *
     * @return
     */
        @Override
    public String getImplementationName() {
         return m_implementationName;
    }

    /**
     *
     * @param sService
     * @return
     */
    @Override
    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    /**
     *
     * @return
     */
    private DefaultComboBoxModel getFonts(){
        //System.out.println("Getting system fonts...");
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //Font[] fontfamilies = e.getAllFonts(); // Get the fonts as Font objects
        String[] fontfamilies = e.getAvailableFontFamilyNames();
//        for(String font : fonts){
//            System.out.println(font);
//        }
        return new DefaultComboBoxModel<>(fontfamilies);
    }
    
    public static void openWebpage(URI uri) throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            desktop.browse(uri);
        }
    }


    public static void openWebpage(URL url) throws IOException, URISyntaxException {
        openWebpage(url.toURI());
    }

    private static void sendMail() throws IOException {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.MAIL)) {
            String message = "mailto:bibleget.io@gmail.com?subject=feedback%20openoffice%20plugin";
            URI uri = URI.create(message);
            desktop.mail(uri);
        }
    }

    public static String getPackagePath()
    {
        return BibleGetIO.packagePath;
    }
    
    public static String getLocale()
    {
        return BibleGetIO.myLocale;
    }

    /**
     *
     * @return
     */
    public static Locale getUILocale()
    {
        return BibleGetIO.uiLocale;
    }
    
    public static DefaultComboBoxModel getFontFamilies()
    {
        return BibleGetIO.fontFamilies;
    }
    
    public static com.sun.star.frame.XController getXController()
    {
        return instance.m_xController;
    }
// com.sun.star.lang.XInitialization:

    /**
     *
     * @param object
     * @throws Exception
     */
        @Override
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception
    {
        if ( object.length > 0 )
        {
            
            if(instance==null){
                instance = this;
                
                XPackageInformationProvider xPackageInformationProvider =
                    PackageInformationProvider.get(instance.m_xContext);
                BibleGetIO.packagePath = xPackageInformationProvider.getPackageLocation(BibleGetIO.m_implementationName);
                System.out.println("package path = " + BibleGetIO.packagePath);

                //myOptionFrame = BibleGetOptionsFrame.getInstance(packagePath);

                BibleGetIO.fontFamilies = getFonts();

                BibleGetIO.JAVAVERSION = BibleGetIO.getJavaVersion();
                System.out.println("Java version currently used = " + BibleGetIO.JAVAVERSION);

                try {
                    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                        //System.out.println(info.getName());
                        if ("Nimbus".equals(info.getName())) {
                            javax.swing.UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
                    java.util.logging.Logger.getLogger(BibleGetQuoteFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }        
                
                
                instance.m_xFrame = (com.sun.star.frame.XFrame)UnoRuntime.queryInterface(
                com.sun.star.frame.XFrame.class, object[0]);
                instance.m_xController = instance.m_xFrame.getController();
                
                try {
                    XMultiComponentFactory xMultiComponentFactory = instance.m_xContext.getServiceManager(); 
                    Object oProvider = 
                       xMultiComponentFactory.createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", instance.m_xContext); 
                    XMultiServiceFactory xConfigurationServiceFactory =  
                       (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, oProvider); 

                    PropertyValue[] lArgs    = new PropertyValue[1]; 
                    lArgs[0] = new PropertyValue(); 
                    lArgs[0].Name  = "nodepath"; 
                    lArgs[0].Value = "/org.openoffice.Office.Linguistic/General"; 
                    Object configAccess =  xConfigurationServiceFactory.createInstanceWithArguments( 
                       "com.sun.star.configuration.ConfigurationAccess",lArgs); 
                    XNameAccess xNameAccess = (XNameAccess)UnoRuntime.queryInterface(XNameAccess.class, configAccess);
                    
                    PropertyValue[] lArgs1    = new PropertyValue[1]; 
                    lArgs1[0] = new PropertyValue();
                    lArgs1[0].Name = "nodepath";
                    lArgs1[0].Value = "/org.openoffice.Office.Writer/Layout/Other";
                    Object configAccess1 =  xConfigurationServiceFactory.createInstanceWithArguments( 
                       "com.sun.star.configuration.ConfigurationAccess",lArgs1); 
                    XNameAccess xNameAccess1 = (XNameAccess)UnoRuntime.queryInterface(XNameAccess.class, configAccess1);
                    
                    String[] elNames = xNameAccess1.getElementNames();
                    //xNameAccess1.getElementType();
                    System.out.println("elNames = " + Arrays.toString(elNames));
                    //System.out.println(xNameAccess1.getByName("MeasureUnit").toString());
                    System.out.println(AnyConverter.toInt(xNameAccess1.getByName("MeasureUnit")));
                    /*
                        MM = 1
                        CM = 2
                        INCHES = 8
                        PICA = 7
                        POINT = 6
                    */
                    int mUnit = AnyConverter.toInt(xNameAccess1.getByName("MeasureUnit"));
                    BibleGetIO.measureUnit = BGET.MEASUREUNIT.valueOf(mUnit);
                    try {
                        XViewSettingsSupplier xViewSettings = (XViewSettingsSupplier) UnoRuntime.queryInterface(XViewSettingsSupplier.class,instance.m_xController);
                        XPropertySet viewSettings=xViewSettings.getViewSettings();
                        System.out.println("I got the HorizontalRulerMetric property, here is the type :" + AnyConverter.getType(viewSettings.getPropertyValue("HorizontalRulerMetric")));
                        System.out.println("And here is the value: " + AnyConverter.toInt(viewSettings.getPropertyValue("HorizontalRulerMetric")));
                        int mUnit1 = AnyConverter.toInt(viewSettings.getPropertyValue("HorizontalRulerMetric"));
                        if(mUnit1 != mUnit){
                            //the ruler seems to have changed somehow?
                            BibleGetIO.measureUnit = BGET.MEASUREUNIT.valueOf(mUnit1);
                        }
                        //Change listener does not seem to ever fire? Perhaps related to these bugs:
                        //https://bz.apache.org/ooo/show_bug.cgi?id=61522
                        //https://bz.apache.org/ooo/show_bug.cgi?id=121263
                        //XPropertyChangeListener xl = new RulerListener();
                        //viewSettings.addPropertyChangeListener("HorizontalRulerMetric", xl);
                    } catch (UnknownPropertyException ex) {
                        System.out.println(ex);
                    } catch (IllegalArgumentException | Exception ex) {
                        System.out.println(ex);
                    }
                    
                    String mylcl;
                    mylcl = xNameAccess.getByName("UILocale").toString();
                    if(mylcl.isEmpty()){
                       //System.out.println("locale not set in options->languages,UILocale=empty");
                       lArgs[0].Value = "/org.openoffice.Setup/L10N"; 
                       configAccess =  xConfigurationServiceFactory.createInstanceWithArguments( 
                           "com.sun.star.configuration.ConfigurationAccess",lArgs); 

                       xNameAccess = (XNameAccess)UnoRuntime.queryInterface(XNameAccess.class, configAccess); 

                       mylcl = xNameAccess.getByName("ooLocale").toString();
                    }
                    mylcl = mylcl.toLowerCase();
                    mylcl = mylcl.trim();
                    BibleGetIO.myLocale = mylcl.substring(0,2);
                    //System.out.println(mylcl);
                    BibleGetIO.uiLocale = new Locale(BibleGetIO.myLocale);
                    Locale.setDefault(BibleGetIO.uiLocale);
                    //instance.myMessages = BibleGetI18N.getMessages();
                    
                    BibleGetIO.setNativeLibraryDir();
                    
                    CefSettings settings = new CefSettings();
                    settings.windowless_rendering_enabled = OS.isLinux();
                    //settings.windowless_rendering_enabled = true;
                    //settings.log_severity = LogSeverity.LOGSEVERITY_ERROR;
                    cefApp = CefApp.getInstance(settings);
                    client = cefApp.createClient();
                    
                    client.addLoadHandler(new CefLoadHandlerAdapter() {
                        @Override
                        public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                boolean canGoBack, boolean canGoForward) {
                            if (!isLoading) {
                                //browser_ready = true;
                                //System.out.println("Browser has finished loading!");
                                
                                //The following is no longer necessary, was solved by implementing a FocusHandler on the JTextFields!
                                //The following line actually works to give focus to the OpenOffice instance,
                                //but then there is no way to give the focus back to the SearchFrame window
                                //Every attempt either silently falls or creates an exception
                                //m_xFrame.getContainerWindow().setFocus();
                                
                                //SwingUtilities.windowForComponent(browser.getUIComponent()).setVisible(false);
                                //SwingUtilities.windowForComponent(browser.getUIComponent()).setVisible(true);
                            }
                        }
                    });
                    
                    client.addContextMenuHandler(new JCEFContextMenuHandler());
                    
                    CefMessageRouter msgRouter = CefMessageRouter.create();
                    
                    BibleGetIO.biblegetDB = DBHelper.getInstance();
                    if(BibleGetIO.biblegetDB != null){ 
                        //System.out.println("BibleGetIO main class : We have an instance of database!"); 
                        //System.out.println("BibleGetIO main class : Now loading BibleGetIO.myFrame"); 
                        BibleGetIO.myFrame = BibleGetQuoteFrame.getInstance(instance.m_xController);
                        //System.out.println("BibleGetIO main class : Now loading BibleGetIO.quoteFromSelection"); 
                        BibleGetIO.quoteFromSelection = BibleGetSelection.getInstance(instance.m_xController);
                        //System.out.println("BibleGetIO main class : Now loading BibleGetIO.myHelpFrame"); 
                        BibleGetIO.myHelpFrame = BibleGetHelpFrame.getInstance();
                        //System.out.println("BibleGetIO main class : Now loading BibleGetIO.bibleGetAbout"); 
                        BibleGetIO.bibleGetAbout =  BibleGetAboutFrame.getInstance();
                        //System.out.println("BibleGetIO main class : Now loading BibleGetIO.myOptionFrame"); 
                        BibleGetIO.myOptionFrame = BibleGetOptionsFrame.getInstance(instance.m_xController);
                        BibleGetIO.bibleGetSearch = BibleGetSearchFrame.getInstance();
                        msgRouter.addHandler(BibleGetIO.bibleGetSearch.new MessageRouterHandler(), true);
                        client.addMessageRouter(msgRouter);
                    }
                    else{ 
                        System.out.println("Sorry, no database instance here."); 
                    }
                    //System.out.println("Assigning myFrame and myOptionFrame while we create instance");        
                } catch (ClassNotFoundException | SQLException | UnsupportedEncodingException ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            else{
                //System.out.println("instance already initialized...");        
                //System.out.println("myOptionFrame is initialized: " + (myOptionFrame != null) );
                //System.out.println("myFrame is initialized: " + (myFrame != null) );
            }
        }
    }

    /*    
    public com.sun.star.frame.XController getController()
    {
    return m_xFrame.getController();
    }*/   
    
    /**
     * @param value
     * @param input_min
     * @param input_max
     * @param output_min
     * @param output_max
     * @return 
     */
    /*
    public static void main(String args[]) {
    }
    */
    
    static public final int remap(int value, int input_min, int input_max, int output_min, int output_max)
    {
        long factor = 1000000000;

        long output_spread = output_max - output_min;
        long input_spread = input_max - input_min;

        //long l_value = value;

        long zero_value = value - input_min;
        zero_value *= factor;
        long percentage = zero_value / input_spread;

        long zero_output = percentage * output_spread / factor;

        long result = output_min + zero_output;

        return (int)result;
    }
    
    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } 
        return Integer.parseInt(version);
    }
    
    private static void setNativeLibraryDir() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        String nativelibrarypath = "";
        String ziplibrarypath = "";
        String[] JCEFfiles = null;
        String[] JCEFswiftshaderFiles = null;
        if(SystemUtils.IS_OS_WINDOWS){
            nativelibrarypath = "/AppData/Roaming/BibleGetOpenOfficePlugin/JCEF";
            ziplibrarypath = "win32";
            JCEFfiles = new String[]{
                "cef.pak",
                "cef_100_percent.pak",
                "cef_200_percent.pak",
                "cef_extensions.pak",
                "chrome_elf.dll",
                "d3dcompiler_47.dll",
                "devtools_resources.pak",
                "icudtl.dat",
                "jcef.dll",
                "jcef_helper.exe",
                "libEGL.dll",
                "libGLESv2.dll",
                "libcef.dll",
                "snapshot_blob.bin",
                "v8_context_snapshot.bin"
            };
            JCEFswiftshaderFiles = new String[]{
                "libEGL.dll",
                "libGLESv2.dll"
            };
        }
        else if(SystemUtils.IS_OS_LINUX){
            nativelibrarypath = "/.BibleGetOpenOfficePlugin/JCEF";
            switch(System.getProperty("sun.arch.data.model")){
                case "64":
                    ziplibrarypath = "linux64";
                    break;
                case "32":
                    ziplibrarypath = "linux32";
                    break;
            }
            JCEFfiles = new String[]{
                "cef.pak",
                "cef_100_percent.pak",
                "cef_200_percent.pak",
                "cef_extensions.pak",
                "chrome-sandbox",
                "devtools_resources.pak",
                "icudtl.dat",
                "jcef_helper",
                "libcef.so",
                "libEGL.so",
                "libGLESv2.so",
                "libjcef.so",
                "snapshot_blob.bin",
                "v8_context_snapshot.bin"
            };
            JCEFswiftshaderFiles = new String[]{
                "libEGL.so",
                "libGLESv2.so"
            };
        }
        else if(SystemUtils.IS_OS_MAC_OSX){
            nativelibrarypath = "/Library/BibleGetOpenOfficePlugin/jcef_app.app";
            ziplibrarypath = "java-cef-build-bin/bin/jcef_app.app"; //(??? double check how macOS is supposed to work)
        }
        
        nativelibrarypath = System.getProperty("user.home") + nativelibrarypath;
        
        
        if(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_WINDOWS){

            //first let's check if the JCEF directory exists in the user's home under our BibleGetOpenOfficePlugin directory
            //and if not, we create it
            Path JCEFpath = Paths.get(nativelibrarypath);
            if(Files.notExists(JCEFpath)){
                File jcefDirectory = new File(nativelibrarypath);
                jcefDirectory.mkdirs();
            }
        
            String tempDir = System.getProperty("java.io.tmpdir");
            //check if the necessary files exist in the user.path BibleGetIOOpenOffice/JCEF folder
            if(JCEFfiles != null){
                for(String fileName : JCEFfiles ){
                    Path filePath = Paths.get(nativelibrarypath, fileName);
                    if(Files.notExists(filePath) ){
                        Path tempFilePath = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, fileName);
                        if(Files.notExists(tempFilePath)){
                            //if the file doesn't even exist in the temp path then we need to download or re-download the package from github
                            BibleGetIO.downloadJCEF();
                        }
                        //now we can copy the missing file from the tempDir
                        Files.copy(tempFilePath,filePath);
                    }
                }
            } else {
                Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, "setNativeLibraryDir() : We were not able to determine the correct folder structure for this system in order to ensure the correct functioning of the Chrome Embedded Framework.");
            }
            
            //check if the 'locales' subfolder exists in the user.path BibleGetIOOpenOffice/JCEF folder
            //and if not create it
            Path JCEFlocalespath = Paths.get(nativelibrarypath, "locales");
            if(Files.notExists(JCEFlocalespath) ){
                File jcefLocalesDir = new File(nativelibrarypath, "locales");
                jcefLocalesDir.mkdir();
            }
            
            //Define the files that should be in the 'locales' subfolder
            String[] JCEFlocaleFiles = new String[]{
                "am.pak",
                "ar.pak",
                "bg.pak",
                "bn.pak",
                "ca.pak",
                "cs.pak",
                "da.pak",
                "de.pak",
                "el.pak",
                "en-GB.pak",
                "en-US.pak",
                "es-419.pak",
                "es.pak",
                "et.pak",
                "fa.pak",
                "fi.pak",
                "fil.pak",
                "fr.pak",
                "gu.pak",
                "he.pak",
                "hi.pak",
                "hr.pak",
                "hu.pak",
                "id.pak",
                "it.pak",
                "ja.pak",
                "kn.pak",
                "ko.pak",
                "lt.pak",
                "lv.pak",
                "ml.pak",
                "mr.pak",
                "ms.pak",
                "nb.pak",
                "nl.pak",
                "pl.pak",
                "pt-BR.pak",
                "pt-PT.pak",
                "ro.pak",
                "ru.pak",
                "sk.pak",
                "sl.pak",
                "sr.pak",
                "sv.pak",
                "sw.pak",
                "ta.pak",
                "te.pak",
                "th.pak",
                "tr.pak",
                "uk.pak",
                "vi.pak",
                "zh-CN.pak",
                "zh-TW.pak"
            };

            //check if the necessary files exist in the 'locales' subfolder of the user.path BibleGetIOOpenOffice/JCEF folder
            for(String fileName : JCEFlocaleFiles ){
                Path filePath = Paths.get(nativelibrarypath, "locales", fileName);
                if(Files.notExists(filePath) ){
                    Path tempFilePath = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, "locales", fileName);
                    if(Files.notExists(tempFilePath)){
                        //if the file doesn't even exist in the temp path then we need to download or re-download the package from github
                        BibleGetIO.downloadJCEF();
                    }
                    //now we can copy the missing file from the tempDir
                    Files.copy(tempFilePath,filePath);
                }
            }
            
            //check if the 'swiftshader' subfolder exists in the user.path BibleGetIOOpenOffice/JCEF folder
            //and if not create it
            Path JCEFswshaderpath = Paths.get(nativelibrarypath, "swiftshader");
            if(Files.notExists(JCEFswshaderpath) ){
                File jcefSwShaderDir = new File(nativelibrarypath, "swiftshader");
                jcefSwShaderDir.mkdir();
            }
            
            //check if the necessary files exist in the 'swiftshader subfolder
            if(JCEFswiftshaderFiles != null){
                for(String fileName : JCEFswiftshaderFiles ){
                    Path filePath = Paths.get(nativelibrarypath, "swiftshader", fileName);
                    if(Files.notExists(filePath) ){
                        Path tempFilePath = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, "swiftshader", fileName);
                        if(Files.notExists(tempFilePath)){
                            //if the file doesn't even exist in the temp path then we need to download or re-download the package from github
                            BibleGetIO.downloadJCEF();
                        }
                        //now we can copy the missing file from the tempDir
                        Files.copy(tempFilePath,filePath);
                    }
                }
            } else {
                Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, "setNativeLibraryDir() : We were not able to determine the correct folder structure for this system in order to ensure the correct functioning of the Chrome Embedded Framework.");
            }
        } else if(SystemUtils.IS_OS_MAC_OSX){
            //basically we don't nee the TEMP storage, because the app structure is exactly the same between the zip file and the user folder
            
            //first let's check if the JCEF directory exists in the user's home under our BibleGetOpenOfficePlugin directory
            //and if not, we create it
            Path JCEFpath = Paths.get(nativelibrarypath);
            if(Files.notExists(JCEFpath)){
                //File jcefDirectory = new File(nativelibrarypath);
                //jcefDirectory.mkdirs();
                //The directory will be created when reading the zip file!
                BibleGetIO.downloadJCEF();
            }
            
        }
        
        if(JAVAVERSION == 8){
        
            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);
            //get array of paths
            final String[] paths = (String[])usrPathsField.get(null);
            //System.out.println("Java version is 8 and usr_paths at start of runtime = " + String.join(";", paths) );
            //check if the path to add is already present
            for(String path : paths) {
                if(path.equals(nativelibrarypath)) {
                    return;
                }
            }
            //System.out.println(nativelibrarypath + " was not among the usr_paths, now trying to add it..." );
            //add the new path
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[newPaths.length-1] = nativelibrarypath;
            System.setProperty("java.library.path", String.join(";", newPaths) );
            usrPathsField.set(null, newPaths);
            //final String[] paths2 = (String[])usrPathsField.get(null);
            //System.out.println("usr_path is now = " + String.join(";", paths2) );
        }
        
    }
    
    private static void downloadJCEF() throws MalformedURLException, IOException{
        System.out.println("starting downloadJCEF process...");
        //following URLs retrieved via the github api at
        //    https://api.github.com/repos/jcefbuild/jcefbuild/releases/30632029/assets
        // actually the same informatino is present in:
        //    https://api.github.com/repos/jcefbuild/jcefbuild/releases
        // use Accept header = application/vnd.github.v3+json in order to retrieve results in JSON notation
        // the result of this last API endpoint is an ARRAY of objects, the first of which should be the latest release
        // but in order to target a specific release, search for "tag_name" (or "name", should be the same) 
        //     as indicated in the following JCEFbuild String
        //     The object that has this "tag_name" will also have an "assets" property at the same level as "tag_name"
        //     "assets" is again an ARRAY of OBJECTS
        //     We must search for objects that have "name" of: {"linux32.zip", "linux64.zip", "macosx64.zip", "win32.zip"}
        //     and get their "url" and perhaps "content_type"
        
        String JCEFbuild = "v1.0.10-84.3.8+gc8a556f+chromium-84.0.4147.105";
        String[] targetOS = new String[]{"linux32.zip", "linux64.zip", "macosx64.zip", "win32.zip"};
        
        //Until we automate the process, here are the hardcoded URLs for the target OSs, for the indicated release:
        String linux32URL = "https://api.github.com/repos/jcefbuild/jcefbuild/releases/assets/24791124";
        String linux64URL = "https://api.github.com/repos/jcefbuild/jcefbuild/releases/assets/24791106";
        String macosx64URL = "https://api.github.com/repos/jcefbuild/jcefbuild/releases/assets/24791232";
        String win32URL = "https://api.github.com/repos/jcefbuild/jcefbuild/releases/assets/24791067";
        
        URL downloadURL = null;
        if(SystemUtils.IS_OS_WINDOWS){
            downloadURL = new URL(win32URL);
        } else if (SystemUtils.IS_OS_MAC_OSX){
            downloadURL = new URL(macosx64URL);
        } else if (SystemUtils.IS_OS_LINUX){
            switch(System.getProperty("sun.arch.data.model")){
                case "64":
                    downloadURL = new URL(linux64URL);
                    break;
                case "32":
                    downloadURL = new URL(linux32URL);
                    break;
            }
        }
        System.out.println("download URL was detected as " + downloadURL.toString());
        
        HttpsURLConnection con;
        try{
            con = (HttpsURLConnection) downloadURL.openConnection();            
             // optional default is GET
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/octet-stream");


            //System.out.println("Sending 'GET' request to URL : " + url);
            //System.out.println("Response Code : " + con.getResponseCode());
            int respCode;
            respCode = con.getResponseCode();
            if(HttpsURLConnection.HTTP_OK == respCode) {
                Path outDir = Paths.get(System.getProperty("java.io.tmpdir"),"BibleGetJCEF");
                System.out.println("Temp directory where JCEF should or will be stored was detected as " + outDir.toString());
                if(Files.notExists(outDir)){
                    System.out.println("The BibleGetJCEF directory in the temp folder was not found, now creating...");
                    File jcefDirectoryTMP = new File(outDir.toString());
                    jcefDirectoryTMP.mkdir();
                }
                
                byte[] buffer = new byte[2048];
                try ( 
                        InputStream conInStr = con.getInputStream();
                        BufferedInputStream buffInStr = new BufferedInputStream(conInStr);
                        ZipInputStream zipInStream = new ZipInputStream(buffInStr);
                    ) {
                    System.out.println("We seem to have a stream of data from the github assets...");
                    
                    ZipEntry entry;
                    while ((entry = zipInStream.getNextEntry()) != null) {
                        if(entry.isDirectory()){
                            File entryFile = new File(outDir.toString(), entry.getName());
                            if(entryFile.exists() == false){
                                System.out.println("directory " + entryFile.getCanonicalPath() + " did not exist, now creating...");
                                entryFile.mkdirs();
                            }
                        } else {
                            Path filePath = outDir.resolve(entry.getName());
                            //instead of checking whether the file exists, we'll simply overwrite by passing false as second parameter to FileOutputStream
                            //let's keep life simple, it doesn't take that long
                            //File entryFile = new File(outDir.toString(), entry.getName());
                            //if(entryFile.exists() == false){
                                try (FileOutputStream fos = new FileOutputStream(filePath.toFile(), false);
                                        BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {
                                    int len;
                                    while ((len = zipInStream.read(buffer)) > 0) {
                                        bos.write(buffer, 0, len);
                                    }
                                }
                            //}
                        }
                    }
                    
                    if(SystemUtils.IS_OS_MAC_OSX){
                        //in the case of MacOS we can copy the whole directory structure as is...
                        File srcDir = Paths.get(System.getProperty("java.io.tmpdir"),"BibleGetJCEF","macosx64","java-cef-build-bin","bin").toFile();
                        File destDir = Paths.get(System.getProperty("user.home"),"Library","BibleGetOpenOfficePlugin").toFile();
                        FileUtils.copyDirectory(srcDir, destDir);
                    }
                }
            }
            con.disconnect();
        } catch(NullPointerException ex){
            Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, "downloadJCEF() : We were not able to determine the correct URL to communicate with.");
        }
    }
    
}
