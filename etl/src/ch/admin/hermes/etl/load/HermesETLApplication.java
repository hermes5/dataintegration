/*----------------------------------------------------------------------------------------------
 * Copyright 2014 Federal IT Steering Unit FITSU Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors:
 * Marcel Bernet, Zurich - initial implementation
 *---------------------------------------------------------------------------------------------*/

package ch.admin.hermes.etl.load;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

import javax.script.ScriptEngine;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ch.admin.hermes.etl.freemarker.transform.MethodTransform;
import ch.admin.hermes.etl.model.business.ModelExtract;
import ch.admin.hermes.etl.script.transform.JavaScriptEngine;
import ch.admin.hermes.model.schema.Workproduct;
import ch.admin.hermes.model.schema.Workproduct.Template;

/*******************************************************************************
 * HERMES 5 nach Fremdsystem/Format.<p>
 * 
 * Dabei werden die Daten direkt von http://www.hermes.admin.ch geholt.<p>
 * 
 * @author  mbern
 *******************************************************************************/

public class HermesETLApplication
{
    /** HERMES XML Model */
    private static String model = "Hermes/model/de/model_de.xml";
    /** Script um die Daten zu verarbeiten */
    private static String script = "script/model/SharePointZuehlke.js";
    /** Ausgewaehltes Szenario */
    private static String scenario;
    /** Zugangsdaten Fremdsystem */
    private static String site, user, passwd;
    /** Progressbar */
    private static JProgressBar progress;
    /** HTMLCrawler - Zugriff auf HERMES 5 Online Loesung */
    private static HermesOnlineCrawler crawler;
    
    /**
     * Hauptprogramm
     * @param args Commandline Argumente
     */
    public static void main(String[] args) 
    {
        JFrame frame = null;
        try
        {
            // Crawler fuer Zugriff auf HERMES 5 Online Loesung initialiseren */
            crawler = new HermesOnlineCrawler();
            
            // CommandLine Argumente aufbereiten
            parseCommandLine( args );
            
            // Methoden Export (Variante Zuehlke) extrahieren
            System.out.println( "load library " + model );
            ModelExtract root = new ModelExtract();
            root.extract( model );
            
            frame = createProgressDialog();
            // wird das XML Model von HERMES Online geholt - URL der Templates korrigieren
            if  ( scenario != null )
            {
                List<Workproduct> workproducts = (List<Workproduct>) root.getObjects().get( "workproducts" );
                for ( Workproduct wp : workproducts )
                    for ( Template t : wp.getTemplate() )
                    {
                        // Template beinhaltet kompletten URL - keine Aenderung
                        if  ( t.getUrl().toLowerCase().startsWith( "http" ) || t.getUrl().toLowerCase().startsWith( "file" ) )
                            continue;
                        // Model wird ab Website geholte
                        if  ( model.startsWith( "http" )  )                            
                            t.setUrl( crawler.getTemplateURL( scenario, t.getUrl() ) );
                        // Model ist lokal - Path aus model und relativem Path Template zusammenstellen
                        else
                        {
                            File m = new File( model );
                            t.setUrl( m.getParentFile() + "/" + t.getUrl() ); 
                        }
                    }
            }
            
            // JavaScript - fuer Import in Fremdsystem
            if  ( script.endsWith( ".js" ) )
            {
                final JavaScriptEngine js = new JavaScriptEngine();
                js.setObjects( root.getObjects() );
                js.put( "progress", progress );
                js.eval( "function log( x ) { println( x ); progress.setString( x ); }" ); 
                progress.setString( "call main() in " + script );
                js.put( ScriptEngine.FILENAME, script );
                js.call( new InputStreamReader( new FileInputStream( script ) ), "main", new Object[] { site, user, passwd } );
            }
            // FreeMarker - fuer Umwandlungen nach HTML
            else if ( script.endsWith( ".ftl" ) )
            {
                FileOutputStream out = new FileOutputStream( new File( script.substring( 0, script.length()-3 ) + "html ") );
                int i = script.indexOf( "templates" );
                if  ( i >= 0 )
                    script = script.substring( i + "templates".length() );
                MethodTransform transform = new MethodTransform();
                transform.transform( root.getObjects(), script, out );
                out.close();
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog( null, e.toString(), "Fehlerhafte Verarbeitung", JOptionPane.WARNING_MESSAGE );
            e.printStackTrace();
        }
        if  ( frame != null )
        {
            frame.setVisible( false );
            frame.dispose();
        }
        System.exit( 0 );
    }

    /**
     * CommandLine parse und fehlende Argumente verlangen
     * @param args Args
     * @throws ParseException
     */
    private static void parseCommandLine(String[] args) throws Exception
    {
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        
        // HACK um UTF-8 CharSet fuer alle Dateien zu setzen (http://stackoverflow.com/questions/361975/setting-the-default-java-character-encoding)
        System.setProperty("file.encoding","UTF-8");
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null,null);
        
        // commandline Options - FremdsystemSite, Username und Password
        Options options = new Options();
        options.addOption( "s", true, "Zielsystem - URL" );
        options.addOption( "u", true, "Zielsystem - Username" );
        options.addOption( "p", true, "Zielsystem - Password" );
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse( options, args);
        site = cmd.getOptionValue( "s" );
        user = cmd.getOptionValue( "u" );
        passwd = cmd.getOptionValue( "p" );
        
        // restliche Argumente pruefen - sonst usage ausgeben
        String[] others = cmd.getArgs();
        if  ( others.length >= 1 && (others[0].endsWith( ".js" ) || others[0].endsWith( ".ftl" )) )
            script = others[0];
        if  ( others.length >= 2 &&  others[1].endsWith( ".xml" ) )
            model = others[1];
        
        // Dialog mit allen Werten zusammenstellen
        JComboBox<String> scenarios = new JComboBox<String>( crawler.getScenarios() );
        
        JTextField tsite = new JTextField( 45 );
        tsite.setText( site );
        JTextField tuser = new JTextField( 16 );
        tuser.setText( user );
        JPasswordField tpasswd = new JPasswordField( 16 );
        tpasswd.setText( passwd );
        final JTextField tscript = new JTextField( 45 );
        tscript.setText( script );
        final JTextField tmodel = new JTextField( 45 );
        tmodel.setText( model );

        JPanel myPanel = new JPanel( new GridLayout(6, 2) );
        myPanel.add( new JLabel( "Szenario (von http://www.hermes.admin.ch):" ) );
        myPanel.add( scenarios );
        
        
        myPanel.add( new JLabel( "XML Model:" ) );
        myPanel.add( tmodel );            
        JPanel pmodel = new JPanel();
        pmodel.add( tmodel );
        JButton bmodel = new JButton( "..." );
        pmodel.add( bmodel );
        bmodel.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                model = getFile( "Szenario XML Model", new String[] { "XML Model" }, new String[] { ".xml" } );
                if  ( model != null )
                    tmodel.setText( model );
            }
        } );
        myPanel.add( pmodel );
        
        scenarios.addItemListener( new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                try
                {
                    Object o = e.getItem();
                    tmodel.setText( crawler.getModelURL( o.toString() ) );
                    scenario = o.toString();
                }
                catch (Exception e1)
                {}
            }
        } );
        
        // Script
        myPanel.add( new JLabel( "Umwandlungs-Script:" ) );
        JPanel pscript = new JPanel();
        pscript.add( tscript );
        JButton bscript = new JButton( "..." );
        pscript.add( bscript );
        myPanel.add( pscript );
        bscript.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                script = getFile( "JavaScript/Freemarker Umwandlungs-Script", new String[] { "JavaScript", "Freemarker" }, new String[] { ".js", ".ftl" } );
                if  ( script != null )
                    tscript.setText( script );
            }
        } );
        
        // Zielsystem Angaben
        myPanel.add( new JLabel( "Zielsystem URL:" ) );
        myPanel.add( tsite );
        myPanel.add( new JLabel( "Zielsystem Benutzer:" ) );
        myPanel.add( tuser );
        myPanel.add( new JLabel( "Zielsystem Password:" ) );
        myPanel.add( tpasswd );
        
        // Trick um Feld scenario und model zu setzen.
        if  ( scenarios.getItemCount() >= 8 )
            scenarios.setSelectedIndex( 8 );

        // Dialog
        int result = JOptionPane.showConfirmDialog( null, myPanel, "HERMES 5 XML Model nach Fremdsystem/Format", JOptionPane.OK_CANCEL_OPTION );
        if ( result == JOptionPane.OK_OPTION )
        {
            site = tsite.getText();
            user = tuser.getText();
            passwd = new String( tpasswd.getPassword() );
            model = tmodel.getText();
            script = tscript.getText();
        }
        else
            System.exit( 1 );
        
        if  ( model == null || script == null || script.trim().length() == 0 )
            usage();
        
        if  ( script.endsWith( ".js" ) )
            if  ( site == null || user == null || passwd == null || user.trim().length() == 0 || passwd.trim().length() == 0 )
                usage();
    }
    
    /**
     * FileOpen Dialog
     * @param title
     * @param name
     * @param ext
     * @return
     */
    private static String getFile( final String title, final String[] name, final String[] ext )
    {
        // User Chooses a Path and Name for the html Output File
        JFileChooser fc = new JFileChooser( "." );
        fc.setDialogType( JFileChooser.OPEN_DIALOG );
        fc.setDialogTitle( title );
        fc.setFileFilter( new FileFilter()
        {
            public String getDescription()
            {
                StringBuffer str = new StringBuffer();
                for ( String n : name )
                    str.append( n + "" );
                return ( str.toString() );
            }
            public boolean accept(File f)
            {
                for ( String e : ext )
                    if  ( f.isDirectory() || f.getName().toLowerCase().endsWith( e ))
                        return  ( true );
                return false;
            }
        } );
            
        int state = fc.showOpenDialog( null );
        
        if ( state == JFileChooser.APPROVE_OPTION )
            return  ( fc.getSelectedFile().getPath() );
        
        return  ( null );
    }
    
    /**
     * erstellt einen Progress Dialog
     * @return
     */
    private static JFrame createProgressDialog()
    {
        JFrame frame = new JFrame("HERMES 5 XML Model nach Fremdsystem/Format");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        progress = new JProgressBar();
        progress.setStringPainted(true);
        
        //Create and set up the content pane.
        progress.setOpaque(true); //content panes must be opaque
        frame.setContentPane(progress );
        frame.setTitle( "Schreibe nach Zielsystem/Format" );
       
        //Display the window.
        frame.pack();
        frame.setSize( 400, 100 );
        frame.setLocation( 100, 100 );
        frame.setVisible(true);
        return frame;
    }    

    /**
     * Ausgabe usage und exit(1)
     */
    private static void usage()
    {
        JOptionPane.showMessageDialog( null, "usage: HermesETL [-p <Zielsystem URL>] [-u <User>] [-p <Password>] <JavaScript> <Szenario XML Model>" , 
                                       "Fehlerhafter Aufruf von HermesETL", JOptionPane.WARNING_MESSAGE );
        System.exit( -1 );
    }
    
}
