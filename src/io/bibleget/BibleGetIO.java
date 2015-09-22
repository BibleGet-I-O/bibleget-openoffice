package io.bibleget;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;

public final class BibleGetIO extends WeakBase
   implements com.sun.star.frame.XDispatchProvider,
              com.sun.star.frame.XDispatch,
              com.sun.star.lang.XServiceInfo,
              com.sun.star.lang.XInitialization
{
    public static final double VERSION = 2.7;
    public static final String PLUGINVERSION = "v" + String.valueOf(BibleGetIO.VERSION).replace(".", "_") ;

    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = BibleGetIO.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler" };
    
    private com.sun.star.frame.XController m_xController;
    private com.sun.star.frame.XModel m_xModel;
    private com.sun.star.text.XTextDocument m_xTextDocument;

    private static String packagePath;
    private static BibleGetFrame myFrame;
    private static OptionsFrame myOptionFrame;
    private static DefaultComboBoxModel fontFamilies;
    private static BibleGetHelp myHelpFrame;   
    private static BibleGetSelection quoteFromSelection;
    private static BibleGetDB biblegetDB;
    private static BibleGetAbout bibleGetAbout;
    
    private static String myLocale;
    //public ResourceBundle myMessages;
    
    private static BibleGetIO instance;

    public BibleGetIO( XComponentContext context )
    {
        m_xContext = context;
        XPackageInformationProvider xPackageInformationProvider =
            PackageInformationProvider.get(m_xContext);
        packagePath = xPackageInformationProvider.getPackageLocation(m_implementationName);        
        //System.out.println(packagePath);
        
        //myOptionFrame = OptionsFrame.getInstance(packagePath);
        
        fontFamilies = getFonts();
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                //System.out.println(info.getName());
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BibleGetFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        
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
                } catch (ClassNotFoundException ex) {
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
                        BibleGetIO.myFrame = BibleGetFrame.getInstance(instance.m_xController);
                        BibleGetIO.myFrame.setVisible(true);                
                    }
                } catch (ClassNotFoundException ex) {
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
                        BibleGetIO.myOptionFrame = OptionsFrame.getInstance();
                        BibleGetIO.myOptionFrame.setVisible(true);
                    }
                } catch (ClassNotFoundException | UnsupportedEncodingException ex) {
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
                        BibleGetIO.biblegetDB = BibleGetDB.getInstance();                        
                        BibleGetIO.bibleGetAbout = BibleGetAbout.getInstance();
                        BibleGetIO.bibleGetAbout.setVisible(true);
                    }
                } catch (ClassNotFoundException | UnsupportedEncodingException ex) {
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
                        BibleGetIO.myHelpFrame = BibleGetHelp.getInstance();
                        BibleGetIO.myHelpFrame.setVisible(true);
                    } catch (ClassNotFoundException | UnsupportedEncodingException ex) {
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
        //Font[] fonts = e.getAllFonts(); // Get the fonts as Font objects
        String[] fontfamilies = e.getAvailableFontFamilyNames();
//        for(String font : fonts){
//            System.out.println(font);
//        }
        return new DefaultComboBoxModel(fontfamilies);
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
                    Locale uiLocale = new Locale(BibleGetIO.myLocale);
                    Locale.setDefault(uiLocale);
                    //instance.myMessages = BibleGetI18N.getMessages();
                    BibleGetIO.biblegetDB = BibleGetDB.getInstance();
                    if(BibleGetIO.biblegetDB != null){ 
                        System.out.println("We have an instance of database!"); 
                    }
                    else{ 
                        System.out.println("Sorry, no database instance here."); 
                    }
                    BibleGetIO.myOptionFrame = OptionsFrame.getInstance();
                    BibleGetIO.myFrame = BibleGetFrame.getInstance(instance.m_xController);
                    BibleGetIO.quoteFromSelection = BibleGetSelection.getInstance(instance.m_xController);
                    BibleGetIO.myHelpFrame = BibleGetHelp.getInstance();
                    BibleGetIO.bibleGetAbout =  BibleGetAbout.getInstance();
                    //System.out.println("Assigning myFrame and myOptionFrame while we create instance");        
                } catch (ClassNotFoundException | UnsupportedEncodingException ex) {
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

}