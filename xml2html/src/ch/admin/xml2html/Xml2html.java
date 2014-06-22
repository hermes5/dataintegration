package ch.admin.xml2html;
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

/*******************************************************************************
 * Brint einen File Open Dialog und wandelt dann die XML Datei nach HTML um.
 * <p>
 * 
 * Wird zum Review der PrintXML Datei aus HERMES 5 Online benoetigt.
 * 
 * @version $Revision: $ $Date: $
 * @author mbern
 *******************************************************************************/

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Xml2html
{
    public static void main(String[] args) throws Exception
    {
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        
        // User Chooses a Path and Name for the html Output File
        JFileChooser fc = new JFileChooser( "." );
        fc.setDialogType( JFileChooser.OPEN_DIALOG );
        fc.setDialogTitle( "PRINT_XML Datei öffnen" );
        fc.setFileFilter( new FileFilter()
        {
            public String getDescription()
            {
                return ("PRINT XML Datei" );
            }
            public boolean accept(File f)
            {
                if  ( f.isDirectory() || f.getName().toLowerCase().endsWith( ".xml" ))
                    return  ( true );
                return false;
            }
        } );

        int state = fc.showOpenDialog( null );
        String htmlPath;
        String outputPath;
        
        if ( state == JFileChooser.APPROVE_OPTION )
        {
            htmlPath = fc.getSelectedFile().getPath();
            outputPath = htmlPath + ".html";

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer( new StreamSource( Xml2html.class.getResourceAsStream( "printxml.xsl" ) ) );
            transformer.transform( new StreamSource( htmlPath ), new StreamResult( new FileOutputStream( outputPath ) ) );
            JOptionPane.showMessageDialog( null, "Ausgabe Datei " + outputPath + " erstellt" );
        }
        
    }
}
