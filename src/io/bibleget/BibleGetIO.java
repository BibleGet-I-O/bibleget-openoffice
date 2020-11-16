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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.lang3.SystemUtils;
import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.CefSettings.LogSeverity;
import org.cef.OS;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;

public final class BibleGetIO extends WeakBase
   implements com.sun.star.frame.XDispatchProvider,
              com.sun.star.frame.XDispatch,
              com.sun.star.lang.XServiceInfo,
              com.sun.star.lang.XInitialization
{
    public static final double VERSION = 2.8;
    public static final String PLUGINVERSION = "v" + String.valueOf(BibleGetIO.VERSION).replace(".", "_");

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
    public static BGET.ADDONSTATE ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED; //uninitialized until proved otherwise
    public static String nativelibrarypath = "";
    public static String ziplibrarypath = "";
    public static String[] JCEFfiles = null;
    public static String[] JCEFswiftshaderFiles = null;
    public static String[] JCEFlocaleFiles = null;
    private static boolean depsInstalled = true;
    public static String sofficeLaunch = "";
    public static String sofficeLaunchSymlink = "";
    public static List<String> sysPkgsNeeded;
    
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
                    System.out.println("MeasureUnit Class name =" + xNameAccess1.getByName("MeasureUnit").getClass().getName());
                    System.out.println("MeasureUnit Class TypeName =" + xNameAccess1.getByName("MeasureUnit").getClass().getTypeName());
                    /*
                        MM = 1
                        CM = 2
                        INCHES = 8
                        POINT = 6
                        PICA = 7
                    */
                    int mUnit = 8;
                    if(null == xNameAccess1.getByName("MeasureUnit").getClass().getTypeName()){
                        System.out.println("could not determine the type of the MeasureUnit for the document ruler!");
                    } else switch (xNameAccess1.getByName("MeasureUnit").getClass().getTypeName()) {
                        case "com.sun.star.uno.Any":
                            System.out.println("The type of this Any = " + AnyConverter.getType(xNameAccess1.getByName("MeasureUnit")));
                            if(AnyConverter.isInt(xNameAccess1.getByName("MeasureUnit"))){
                                mUnit = AnyConverter.toInt(xNameAccess1.getByName("MeasureUnit"));
                            } else if (AnyConverter.isString(xNameAccess1.getByName("MeasureUnit"))){
                                mUnit = Integer.valueOf( AnyConverter.toString(xNameAccess1.getByName("MeasureUnit")) );
                            }   break;
                        case "java.lang.Integer":
                            System.out.println( (int)xNameAccess1.getByName("MeasureUnit"));
                            mUnit = (int)xNameAccess1.getByName("MeasureUnit");
                            break;
                        default:
                            System.out.println("could not determine the type of the MeasureUnit for the document ruler!");
                            break;
                    }
                    BibleGetIO.measureUnit = BGET.MEASUREUNIT.valueOf(mUnit);
                    try {
                        XViewSettingsSupplier xViewSettings = (XViewSettingsSupplier) UnoRuntime.queryInterface(XViewSettingsSupplier.class,instance.m_xController);
                        XPropertySet viewSettings=xViewSettings.getViewSettings();
                        System.out.println("I got the HorizontalRulerMetric property, here is the type :" + AnyConverter.getType(viewSettings.getPropertyValue("HorizontalRulerMetric")));
                        System.out.println("And here is the value: " + (int)viewSettings.getPropertyValue("HorizontalRulerMetric"));
                        int mUnit1 = (int)viewSettings.getPropertyValue("HorizontalRulerMetric");
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
                    
                    
                    BibleGetIO.biblegetDB = DBHelper.getInstance();
                    if(BibleGetIO.biblegetDB != null){ 
                        
                        try{
                            BibleGetIO.setNativeLibraryDir();
                        } catch (InvocationTargetException ex){
                            System.out.println( ex.getCause() );
                        }
                        /*
                        Map<String,String> envVars = System.getenv();                
                        envVars.entrySet().forEach((envVar) -> {
                            System.out.println(envVar.getKey() + " = " + envVar.getValue());
                        });
                        */
                        //Check from the database if this is a fresh install. 
                        //If it is, don't try to instantiate the JCEF component because it still needs to be installed
                        //this will happen as soon as the user opens the Search for Verses menu item or the Preferences menu item
                        
                        if(BibleGetIO.ADDONSTATE == BGET.ADDONSTATE.JCEFENVREADY && CefApp.startup(new String[]{"--disable-gpu"})){
                            if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {
                                System.out.println("Creating CefSettings...");
                                CefSettings settings = new CefSettings();
                                settings.windowless_rendering_enabled = OS.isLinux();
                                settings.log_severity = LogSeverity.LOGSEVERITY_VERBOSE;
                                cefApp = CefApp.getInstance(new String[]{"--disable-gpu"}, settings);

                                if(cefApp != null){
                                    System.out.println("we seem to have an instance of CefApp:");
                                    System.out.println(cefApp.getVersion());

                                    CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
                                        @Override
                                        public void stateHasChanged(CefAppState state) {
                                            System.out.println("CefAppState has changed : " + state.name());
                                        }
                                    });
                                }
                            } else {
                                cefApp = CefApp.getInstance();
                            }

                            client = cefApp.createClient();
                            if(client != null){
                                System.out.println("we seem to have a client instance of cefApp");

                                client.addLoadHandler(new CefLoadHandlerAdapter() {
                                    @Override
                                    public void onLoadingStateChange(CefBrowser browser, boolean isLoading,
                                            boolean canGoBack, boolean canGoForward) {
                                        if (!isLoading) {
                                            //browser_ready = true;
                                            System.out.println("Browser has finished loading!");
                                        }
                                    }
                                });

                                client.addContextMenuHandler(new JCEFContextMenuHandler());

                            }

                        } else {
                            if(BibleGetIO.ADDONSTATE == BGET.ADDONSTATE.JCEFENVREADY){
                                System.out.println("JCEF startup initialization failed!");
                            } else{
                                System.out.println("Not starting JCEF since this the installation is not yet complete.");
                            }
                        }
                        
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
                        if(BibleGetIO.ADDONSTATE == BGET.ADDONSTATE.JCEFENVREADY && null != client){
                            CefMessageRouter msgRouter = CefMessageRouter.create();
                            msgRouter.addHandler(BibleGetIO.bibleGetSearch.new MessageRouterHandler(), true);
                            client.addMessageRouter(msgRouter);
                        }
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
    
    private static void setNativeLibraryDir() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, InvocationTargetException, InterruptedException {
        System.out.println("setNativeLibraryDir() starting...");
        if(SystemUtils.IS_OS_WINDOWS){
            BibleGetIO.nativelibrarypath = "/AppData/Roaming/BibleGetOpenOfficePlugin/JCEF";
        }
        else if(SystemUtils.IS_OS_LINUX){
            BibleGetIO.nativelibrarypath = "/.BibleGetOpenOfficePlugin/JCEF";
        }
        else if(SystemUtils.IS_OS_MAC_OSX){
            BibleGetIO.nativelibrarypath = "/Library/BibleGetOpenOfficePlugin/jcef_app.app";
            BibleGetIO.ziplibrarypath = "java-cef-build-bin/bin/jcef_app.app"; //(??? double check how macOS is supposed to work)
        }
        
        BibleGetIO.nativelibrarypath = System.getProperty("user.home") + BibleGetIO.nativelibrarypath;
        System.out.println("nativelibrarypath = " + nativelibrarypath);
        //before we proceed, we need to verify the initialization state of the JCEF component

        String[] sysPkgsArr = {"gconf-service","libasound2","libatk1.0-0","libatk-bridge2.0-0","libc6","libcairo2","libcups2","libdbus-1-3","libexpat1","libfontconfig1","libgcc1","libgconf-2-4","libgdk-pixbuf2.0-0","libglib2.0-0","libgbm-dev","libgtk-3-0","libnspr4","libpango-1.0-0","libpangocairo-1.0-0","libstdc++6","libx11-6","libx11-xcb1","libxcb1","libxcomposite1","libxcursor1","libxcursor-dev","libxdamage1","libxext6","libxfixes3","libxi6","libxrandr2","libxrender1","libxss1","libxtst6","ca-certificates","fonts-liberation","libappindicator1","libnss3","lsb-release","xdg-utils"};
        BibleGetIO.sysPkgsNeeded = Arrays.asList(sysPkgsArr);
        
        System.out.println("setNativeLibraryDir : will now proceed to checkJCEFisReady...");
        BibleGetIO.checkJCEFisReady();
        
        if(BibleGetIO.ADDONSTATE == BGET.ADDONSTATE.JCEFENVREADY){
            if(JAVAVERSION == 8){

                final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
                usrPathsField.setAccessible(true);
                //get array of paths
                final String[] paths = (String[])usrPathsField.get(null);
                System.out.println("Java version is 8 and usr_paths at start of runtime = " + String.join(File.separator, paths) );
                //check if the path to add is already present
                for(String path : paths) {
                    if(path.equals(nativelibrarypath)) {
                        return;
                    }
                }
                System.out.println(nativelibrarypath + " was not among the usr_paths, now trying to add it..." );
                //add the new path
                final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
                newPaths[newPaths.length-1] = nativelibrarypath;
                System.setProperty("java.library.path", String.join(";", newPaths) );
                usrPathsField.set(null, newPaths);
                final String[] paths2 = (String[])usrPathsField.get(null);
                System.out.println("usr_paths is now = " + String.join(";", paths2) );
            } else if (JAVAVERSION >= 9){
                try {
                    Lookup cl = MethodHandles.privateLookupIn(ClassLoader.class, MethodHandles.lookup());
                    VarHandle usr_paths = cl.findStaticVarHandle(ClassLoader.class, "usr_paths", String[].class);

                    final String[] paths = (String[]) usr_paths.get();
                    System.out.println("Java version is >= 9 and usr_paths at start of runtime = " + String.join(File.pathSeparator, paths) );
                    for(String path : paths) {
                        if(path.equals(nativelibrarypath)) {
                            return;
                        }
                    }
                    System.out.println(nativelibrarypath + " was not among the usr_paths, now trying to add it..." );
                    //add the new path
                    final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
                    newPaths[newPaths.length-1] = nativelibrarypath;
                    System.setProperty("java.library.path", String.join(File.pathSeparator, newPaths) );
                    usr_paths.set(newPaths);
                    final String[] paths2 = (String[]) usr_paths.get();
                    System.out.println("usr_paths is now = " + String.join(File.pathSeparator, paths2) );
                    System.out.println("java.library.path is now = " + System.getProperty("java.library.path") );
                } catch(ReflectiveOperationException e){
                    System.out.println("If you're seeing this, should you be worried? Native libs are not made available?");
                }
            }
        }
    }
    
    private static void checkJCEFisReady(){
        System.out.println("entered checkJCEFisReady()");
        boolean continueChecking = true;
        if(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_WINDOWS){
            
            if(SystemUtils.IS_OS_WINDOWS){
                BibleGetIO.ziplibrarypath = "win32";
                BibleGetIO.JCEFfiles = new String[]{
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
                BibleGetIO.JCEFswiftshaderFiles = new String[]{
                    "libEGL.dll",
                    "libGLESv2.dll"
                };
            } else if(SystemUtils.IS_OS_LINUX){
                switch(System.getProperty("sun.arch.data.model")){
                    case "64":
                        BibleGetIO.ziplibrarypath = "linux64";
                        break;
                    case "32":
                        BibleGetIO.ziplibrarypath = "linux32";
                        break;
                }
                BibleGetIO.JCEFfiles = new String[]{
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
                BibleGetIO.JCEFswiftshaderFiles = new String[]{
                    "libEGL.so",
                    "libGLESv2.so"
                };
            }

            //Define the files that should be in the 'locales' subfolder on Linux and Windows
            JCEFlocaleFiles = new String[]{
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
            
            //first let's check if the JCEF directory exists in the user's home under our BibleGetOpenOfficePlugin directory
            //check if the 'locales' subfolder exists in the user.path BibleGetIOOpenOffice/JCEF folder
            //check if the 'swiftshader' subfolder exists in the user.path BibleGetIOOpenOffice/JCEF folder
            //and if even one of these three doesn't exist, we know that ADDONSTATE is certainly not JCEFCOPIED
            Path JCEFpath = Paths.get(BibleGetIO.nativelibrarypath);
            Path JCEFlocalespath = Paths.get(BibleGetIO.nativelibrarypath, "locales");
            Path JCEFswshaderpath = Paths.get(BibleGetIO.nativelibrarypath, "swiftshader");
            
            if(Files.notExists(JCEFpath) || Files.notExists(JCEFlocalespath) || Files.notExists(JCEFswshaderpath)){
                System.out.println("checkJCEFisReady: one of the necessary JCEF paths is not present in the user folder! Copy is not complete...");
                BibleGetIO.biblegetDB.setAddonState("JCEFCOPIED", false);
                continueChecking = false;
                //so we know that JCEFCOPIED is false, what about JCEFDOWNLOADED?
                if(BibleGetIO.checkJCEFisDownloaded()){
                    System.out.println("checkJCEFisDownloaded returned true");
                    BibleGetIO.biblegetDB.setAddonState("JCEFDOWNLOADED", true);
                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFDOWNLOADED; // still needs to be verified, might even be unitinitialized
                } else {
                    System.out.println("checkJCEFisDownloaded returned false");
                    BibleGetIO.biblegetDB.setAddonState("JCEFDOWNLOADED", false);
                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED;
                }
            } else {
                System.out.println("the necessary JCEF paths are present in the user home, now checking if all files are present...");
                //if the necessary paths exist, we should still check if all the necessary JCEF files exist
                //in the user.path BibleGetIOOpenOffice/JCEF folder
                if(BibleGetIO.JCEFfiles != null && BibleGetIO.JCEFlocaleFiles != null && BibleGetIO.JCEFswiftshaderFiles != null){
                    for(String fileName : BibleGetIO.JCEFfiles ){
                        Path filePath = Paths.get(BibleGetIO.nativelibrarypath, fileName);
                        //if even one of the files does not exist in the user path JCEF folder, then we know that state is COPIED false
                        if(Files.notExists(filePath) ){
                            System.out.println(filePath.toString() + " is missing, will now proceed to checkJCEFisDownloaded...");
                            BibleGetIO.biblegetDB.setAddonState("JCEFCOPIED", false);
                            continueChecking = false;
                            if(BibleGetIO.checkJCEFisDownloaded()){
                                System.out.println("checkJCEFisDownloaded returned true");
                                BibleGetIO.biblegetDB.setAddonState("JCEFDOWNLOADED", true);
                                BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFDOWNLOADED; // still needs to be verified, might even be unitinitialized
                            } else {
                                System.out.println("checkJCEFisDownloaded returned false");
                                BibleGetIO.biblegetDB.setAddonState("JCEFDOWNLOADED", false);
                                BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED;
                            }
                            break;
                        }
                    }
                    
                    if(continueChecking){
                        //check if the necessary files exist in the 'locales' subfolder of the user.path BibleGetIOOpenOffice/JCEF folder
                        for(String fileName : BibleGetIO.JCEFlocaleFiles ){
                            Path filePath = Paths.get(BibleGetIO.nativelibrarypath, "locales", fileName);
                            //if even one of the files does not exist in the 'locales' subfolder of the user.path BibleGetIOOpenOffice/JCEF folder,
                            //then we know that state is JCEFCOPIED false
                            if(Files.notExists(filePath) ){
                                System.out.println(filePath.toString() + " is missing, will now proceed to checkJCEFisDownloaded...");
                                BibleGetIO.biblegetDB.setAddonState("JCEFCOPIED", false);
                                continueChecking = false;
                                //let's check if the files are at least downloaded?
                                if(BibleGetIO.checkJCEFisDownloaded()){
                                    System.out.println("checkJCEFisDownloaded returned true");
                                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFDOWNLOADED; // still needs to be verified, might even be unitinitialized
                                } else {
                                    System.out.println("checkJCEFisDownloaded returned false");
                                    BibleGetIO.biblegetDB.setAddonState("JCEFDOWNLOADED", false);
                                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED;
                                }
                                break;
                            }
                        }
                    }
                    
                    if(continueChecking){
                        //check if the necessary files exist in the 'swiftshader' subfolder
                        for(String fileName : BibleGetIO.JCEFswiftshaderFiles ){
                            Path filePath = Paths.get(BibleGetIO.nativelibrarypath, "swiftshader", fileName);
                            //if even one of the files does not exist in the 'swiftshader' subfolder  of the user.path BibleGetIOOpenOffice/JCEF folder,
                            //then we know that state is JCEFCOPIED false
                            if(Files.notExists(filePath) ){
                                System.out.println(filePath.toString() + " is missing, will now proceed to checkJCEFisDownloaded...");
                                continueChecking = false;
                                BibleGetIO.biblegetDB.setAddonState("JCEFCOPIED", false);
                                if(BibleGetIO.checkJCEFisDownloaded()){
                                    System.out.println("checkJCEFisDownloaded returned true");
                                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFDOWNLOADED; // still needs to be verified, might even be unitinitialized
                                } else {
                                    System.out.println("checkJCEFisDownloaded returned false");
                                    BibleGetIO.biblegetDB.setAddonState("JCEFDOWNLOADED", false);
                                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED;
                                }
                                break;
                            }
                        }
                    }
                    
                } else {
                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED;
                    Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, "setNativeLibraryDir() : We were not able to determine the correct folder structure for this system in order to ensure the correct functioning of the Chrome Embedded Framework.");
                }
            }
        
            if(continueChecking){
                BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFDOWNLOADED;
                System.out.println("it seems that all necessary JCEF paths and files are present in the user home!");
                System.out.println("BibleGetIO.ADDONSTATE is currently " + BibleGetIO.ADDONSTATE.name() );
                //now that we are sure that the necessary files are all downloaded and copied,
                //we need to check if the right files are executable
                if(Files.exists(Paths.get(System.getProperty("user.home"),".BibleGetOpenOfficePlugin","launch.sh")) && Files.isExecutable(Paths.get(BibleGetIO.nativelibrarypath,"jcef_helper") ) && Files.isExecutable(Paths.get(BibleGetIO.nativelibrarypath,"chrome-sandbox") ) && Files.isExecutable(Paths.get(System.getProperty("user.home"),".BibleGetOpenOfficePlugin","launch.sh") ) ){
                    System.out.println("all the necessary files are not only copied, but those that need to be executable are executable");
                    BibleGetIO.biblegetDB.setAddonState("JCEFCOPIED", true);
                    BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFCOPIED;
                    
                    //so far so good, let's continue checking if the necessary system packages are installed
                    if(BibleGetIO.JCEFisSysDepsSatisfied()){
                        System.out.println("System dependencies have all been met");
                        BibleGetIO.biblegetDB.setAddonState("JCEFDEPENDENCIES", true);
                        BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFDEPENDENCIES;
                        
                        //and now for the last check, if the correct symbolic link to launch OpenOffice is set
                        if(BibleGetIO.JCEFisEnvReady()){
                            BibleGetIO.biblegetDB.setAddonState("JCEFENVREADY", true);
                            BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFENVREADY;
                        } else {
                            BibleGetIO.biblegetDB.setAddonState("JCEFENVREADY", false);
                        }
                        
                    } else {
                        System.out.println("Not all system dependencies have been met");
                        BibleGetIO.biblegetDB.setAddonState("JCEFDEPENDENCIES", false);
                    }
                } else {
                    System.out.println("all the necessary files are copied, but those that need to be executable are not all executable");
                    BibleGetIO.biblegetDB.setAddonState("JCEFCOPIED", false);
                }
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
                BibleGetIO.ADDONSTATE = BGET.ADDONSTATE.JCEFUNINITIALIZED;
            }
            
        }
    }
    
    private static Boolean checkJCEFisDownloaded(){
        System.out.println("entering checkJCEFisDownloaded()");
        String tempDir = System.getProperty("java.io.tmpdir");
        if(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_WINDOWS){
            Path tempFilePathBase = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath);
            Path tempFilePathLocales = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, "locales");
            Path tempFilePathSwiftshader = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, "swiftshader");
            if(Files.notExists(tempFilePathBase) || Files.notExists(tempFilePathLocales) || Files.notExists(tempFilePathSwiftshader) ){
                return false;
            }
            
            for(String fileName : BibleGetIO.JCEFfiles ){
                Path tempFilePath = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, fileName);
                if(Files.notExists(tempFilePath)){
                    return false;
                }
            }
            
            for(String fileName : BibleGetIO.JCEFlocaleFiles ){
                Path tempFilePath = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, "locales", fileName);
                if(Files.notExists(tempFilePath)){
                    return false;
                }
            }
            
            for(String fileName: BibleGetIO.JCEFswiftshaderFiles ){
                Path tempFilePath = Paths.get(tempDir, "BibleGetJCEF", "java-cef-build-bin", "bin", "lib", ziplibrarypath, "swiftshader", fileName);
                if(Files.notExists(tempFilePath)){
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean JCEFisSysDepsSatisfied(){
        System.out.println("entering JCEFisSysDepsSatisfied()...");
        
        try {
            ProcessBuilder builder = new ProcessBuilder().inheritIO();
            String command = "apt -qq list " + String.join(" ", BibleGetIO.sysPkgsNeeded ) ;
            System.out.println("now issuing command:");
            System.out.println(command);
            builder.command("/bin/bash","-c",command);
            //builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            //builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process process = builder.start();
            InputStream errStream = process.getErrorStream();
            InputStream inStream = process.getInputStream();
            OutputStream outStream = process.getOutputStream();
            
            BufferedReader bf = new BufferedReader(new InputStreamReader(inStream));
            bf.lines().forEach(BibleGetIO::checkDependencyInstalled);
            int exitCode = process.waitFor();
            if(exitCode == 0){
                System.out.println("apt -qq list process returned successfully");
                System.out.println(BibleGetIO.sysPkgsNeeded.isEmpty() ? "all packages seem to be installed already" : "we still have " + BibleGetIO.sysPkgsNeeded.size() + " dependencies that need to be met: " + String.join(" ", BibleGetIO.sysPkgsNeeded) );
                return ( BibleGetIO.depsInstalled && BibleGetIO.sysPkgsNeeded.isEmpty() );
            } else {
                System.out.println("apt -qq list process returned an error");
                return false;
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private static void checkDependencyInstalled(String line){
        String pkg = line;
        if(line.contains("/")){
            pkg = line.split("/")[0];
        }
        if(line.contains("[installed]") ){
            BibleGetIO.sysPkgsNeeded.remove(pkg);
            System.out.println(pkg + " is installed!");
        } else {
            BibleGetIO.depsInstalled = false;
            System.out.println(pkg + " is not installed...");
        }
    }
    
    private static boolean JCEFisEnvReady(){
        Path launchFile = Paths.get(System.getProperty("user.home"),".BibleGetOpenOfficePlugin","launch.sh");
        if(Files.exists(launchFile) && Files.isExecutable(launchFile)){
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.command("/bin/bash","-c","which soffice");
                Process process = builder.start();
                BibleGetIO.StreamGobbler streamGobbler = new BibleGetIO.StreamGobbler(process.getInputStream(), BibleGetIO::getLauncherPath);
                Executors.newSingleThreadExecutor().submit(streamGobbler);
                int exitCode = process.waitFor();
                if(exitCode == 0){
                    //we have the path to the soffice launcher, which is probably a symlink
                    System.out.println("soffice launch file = " + BibleGetIO.sofficeLaunch);
                    if(Files.exists(Paths.get(BibleGetIO.sofficeLaunch)) && Files.isSymbolicLink(Paths.get(BibleGetIO.sofficeLaunch)) ){
                        //let's get the target path for the symbolic link
                        Path realPath = Paths.get(BibleGetIO.sofficeLaunch).toRealPath();
                        System.out.println("which is a symbolic link to : " + realPath.toString() + " (should equal: " + launchFile.toString() + ")");
                        return realPath.compareTo(launchFile) == 0;
                    }
                } else {
                    return false;
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(BibleGetIO.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }
    
    public static void getLauncherPath(String line){
        System.out.println("BibleGetIO::getLauncherPath : " + line);
        if(line != null && false == line.isEmpty() ){
            BibleGetIO.sofficeLaunch = line;
            System.out.println("BibleGetIO::getLauncherPath : " + BibleGetIO.sofficeLaunch);
        } else {
            System.out.println("BibleGetIO::getLauncherPath : why does it seem that the value return is null or empty?");
        }
    }
        
    public static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final Consumer<String> consumer;
        private final boolean newline;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
            this.newline = false;
        }
        
        public StreamGobbler(InputStream inputStream, Consumer<String> consumer, boolean newline) {
            this.inputStream = inputStream;
            this.consumer = consumer;
            this.newline = newline;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
              .forEach(line -> { consumer.accept(line); if(this.newline){ consumer.accept(System.lineSeparator()); } } );
        }
    }
    
}
