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

package ch.admin.searchreplace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/*******************************************************************************
 * Ersetzt Begriffe in Word Dateien laut einer Terms Datei (in Excel Format)
 * <p>
 *
 * @author mbern
 *******************************************************************************/

public class SearchReplaceTerms
{
    /** Liste der zu ersetztenden Terms */
    private static HashMap<String, String> searchReplaceTerms;
    /** Fixer Name welcher als letztes im Ordnerpath der Templates stehen muss */
    private static final String TEXTELEMENTE = "Textelemente";
    /** Path zur Terms Datei */
    private static String termsPath;
    /** Path zu den Vorlagen */
    private static String docsPath;
    private static JProgressBar progress;
    private static int count;
    
    /**
     * Search und Replace der Terms in allen Vorlagen
     * 
     * @param args args[0] Uebersetzungdatei args[1] Vorlagen-Ordner
     * @throws Exception I/O Fehler
     */
    public static void main(String args[]) throws Exception
    {
        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
           
        termsPath = ( args.length >= 1 ) ? args[0] : getTermsFile();
        if  ( termsPath != null )
        {
            docsPath = ( args.length >= 2 ) ? args[1] : getTemplates();
            if  ( docsPath != null )
            {
                // Begriffe zum suchen und ersetzen laden
                searchReplaceTerms = readSearchReplaceTerms( termsPath );
                
                JFrame frame = createProgressDialog();
                // Begriff ersetzen - rekursiv
                searchReplaceDirectories( new File( docsPath ), searchReplaceTerms );
                frame.setVisible( false );
                frame.dispose();
            }
        }
    }

    /**
     * erstellt einen Progress Dialog
     * @return
     */
    private static JFrame createProgressDialog()
    {
        JFrame frame = new JFrame("Begriff ersetzen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        progress = new JProgressBar( 0,  255 );
        count = 0;
        
        //Create and set up the content pane.
        progress.setOpaque(true); //content panes must be opaque
        frame.setContentPane(progress );
        frame.setTitle( "Begriffe ersetzen läuft" );
       
        //Display the window.
        frame.pack();
        frame.setSize( 400, 100 );
        frame.setLocation( 100, 100 );
        frame.setVisible(true);
        return frame;
    }

    /**
     * Excel Term Datei selektionieren
     * @return Path Excel Datei
     */
    private static String getTermsFile()
    {
        // User Chooses a Path and Name for the html Output File
        JFileChooser fc = new JFileChooser(".");
        fc.setDialogType( JFileChooser.OPEN_DIALOG );
        fc.setDialogTitle( "HERMES Begriffsliste öffnen" );
        fc.setFileFilter( new javax.swing.filechooser.FileFilter()
        {
            public String getDescription()
            {
                return  ( "Excel" );
            }
            public boolean accept(File f)
            {
                if  ( f.isDirectory() || f.getName().toLowerCase().endsWith( ".xlsx" ))
                    return  ( true );
                return false;
            }
        } );
        
        if ( fc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
            return  ( fc.getSelectedFile().getPath() );
            
        return ( null );
    }
    
    /**
     * Ordner mit den Dateien auswaehlen 
     * @return Ordner Templates
     */
    private static String getTemplates()
    {
        String docsPath = null;
        
        JFileChooser fc = new JFileChooser(".");

        fc.setDialogType( JFileChooser.OPEN_DIALOG  );
        fc.setDialogTitle( "Vorlagen Ordner wählen" );
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        if ( fc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
        {
            docsPath = fc.getSelectedFile().getPath();
            if  ( ! docsPath.endsWith( TEXTELEMENTE ) )
            {
                JOptionPane.showMessageDialog( null, "letzter Ordner muss " + TEXTELEMENTE + " heissen" , "Ordner auswählen", JOptionPane.ERROR_MESSAGE );
                return  ( null );
            }
        }
        return  ( docsPath );
    }
    
    /**
     * Rekursiv Dateien aendern.
     * @param parent
     * @param srTerms
     * @throws Exception
     */
    private static void searchReplaceDirectories( File parent, HashMap<String, String> srTerms ) throws Exception
    {
        if  ( parent.isDirectory() )
        {
            String newRoot = parent.getPath().replace( TEXTELEMENTE,  TEXTELEMENTE + "-patched" );
            new File( newRoot ).mkdirs();
            
            File[] childs = parent.listFiles();
            for ( File child : childs )
            {
                // Ordner?
                if  ( child.isDirectory() )
                    searchReplaceDirectories( child, srTerms );
                // einzelne Datei
                else
                    if  ( child.getName().toLowerCase().endsWith( ".docx" ) )
                    {
                        searchReplaceInFile( child.getAbsolutePath(), newRoot + File.separator + child.getName(), searchReplaceTerms );
                        progress.setValue( count++ );
                    }
            }
        }
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Word Hilfsmethoden fuer Search & Replace
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Ersetzt eine Liste von Ausdruecken in einer Datei und schreibt das Resultat nach Output
     * @param input Inputdatei
     * @param output Outputdatei
     * @param srTerms Begriff
     * @throws IOException I/O Fehler
     * @throws InvalidFormatException OpenXML Document korrupt
     * @throws FileNotFoundException Datei nicht vorhanden
     */
    private static void searchReplaceInFile( String input, String output, HashMap<String, String>  srTerms ) throws IOException, InvalidFormatException, FileNotFoundException
    {
        XWPFDocument doc = new XWPFDocument( OPCPackage.open( input ) );
        for ( XWPFParagraph p : doc.getParagraphs() )
        {
            XWPFRun r = consolidateRuns( p );
            if  ( r != null )
                searchReplace( srTerms, r );
        }
        
        for ( XWPFTable tbl : doc.getTables() )
            for ( XWPFTableRow row : tbl.getRows() )
                for ( XWPFTableCell cell : row.getTableCells() )
                    for ( XWPFParagraph p : cell.getParagraphs() )
                    {
                        XWPFRun r = consolidateRuns( p );
                        if  ( r != null )
                            searchReplace( srTerms, r );
                    }
        
        doc.write( new FileOutputStream( output ) );
    }

    /**
     * Konsolidiert die Runs eines Paragraphen in einen. Ansonsten koennen Texte nicht sauber 
     * ersetzt werden, bzw. werden nicht gefunden, weil ueber mehrere Runs verteilt.
     * @param para Paragraph
     * @return Konsolidierter Run
     */
    private static XWPFRun consolidateRuns(XWPFParagraph para)
    {
        StringBuffer text = new StringBuffer();
        int count = 0;
        for ( XWPFRun r : para.getRuns() )
        {
            text.append( r.getText( 0 ) );
            count++;
        }
        for ( int i = count - 1; i >= 1; i-- )
            para.removeRun( i );
        if  ( count == 0 )
            return  ( null );
            
        XWPFRun r = para.getRuns().get( 0 );
        r.setText( text.toString(), 0 );
        return ( r ); 
    }

    /**
     * @param srTerms
     * @param r
     */
    private static void searchReplace(HashMap<String, String> srTerms, XWPFRun r)
    {
        String text = r.getText( 0 );
        for ( Map.Entry<String, String> sr : srTerms.entrySet() )
        {
            if ( text.contains( sr.getKey() ) )
            {
                text = text.replace( sr.getKey(), sr.getValue() );
                r.setText( text, 0 );
            }
            // Vergleich mit 1. Buchstaben in Kleinbuchstaben
            String search2 = sr.getKey().substring( 0, 1 ).toLowerCase() + sr.getKey().substring( 1 );
            if ( text.contains( search2 ) )
            {
                text = text.replace( search2, sr.getValue().substring( 0, 1 ).toLowerCase() + sr.getValue().substring( 1 ) );
                r.setText( text, 0 );
            }                                
        }
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Excel lesen der Datei mit den Search & Replace Begriffen
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static HashMap<String, String> readSearchReplaceTerms( String input ) throws Exception
    {
        HashMap<String, String> terms = new HashMap<String, String>();
        
        // The package open is instantaneous, as it should be.
        OPCPackage xlsxPackage = OPCPackage.open( input, PackageAccess.READ );
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable( xlsxPackage );
        XSSFReader xssfReader = new XSSFReader( xlsxPackage );
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        
        InputStream stream = iter.next();
        String sheetName = iter.getSheetName();
        System.out.println( sheetName );
        
        InputSource sheetSource = new InputSource( stream );
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        ContentHandler handler = new MyXSSFSheetHandler( styles, strings, terms );
        sheetParser.setContentHandler( handler );
        sheetParser.parse( sheetSource );
        
        stream.close();
        
        return  ( terms );
        
    }
    
    /**
     * The type of the data value is indicated by an attribute on the cell. The value is usually in a "v" element
     * within the cell.
     */
    enum xssfDataType
    {
        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
    }

    /**
     * Derived from http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api
     * <p/>
     * Also see Standard ECMA-376, 1st edition, part 4, pages 1928ff, at
     * http://www.ecma-international.org/publications/standards/Ecma-376.htm
     * <p/>
     * A web-friendly version is http://openiso.org/Ecma/376/Part4
     */
    static class MyXSSFSheetHandler extends DefaultHandler
    {

        /** Table with styles */
        private StylesTable stylesTable;
        /**Table with unique strings   */
        private ReadOnlySharedStringsTable sharedStringsTable;
        // Set when V start element is seen
        private boolean vIsOpen;
        // Set when cell start element is seen;
        // used when cell close element is seen.
        private xssfDataType nextDataType;
        // Used to format numeric cell values.
        private short formatIndex;
        private String formatString;
        private final DataFormatter formatter;
        private int thisColumn = -1;
        // The last column printed to the output stream
        private int lastColumnNumber = -1;
        // Gathers characters as they are seen.
        private StringBuffer value;
        /** Begriffe */
        private HashMap<String, String> terms;
        /** Liste mit Columns */
        private ArrayList<String> cols = new ArrayList<String>();

        /**
         * Accepts objects needed while parsing.
         * 
         * @param styles Table of styles
         * @param strings Table of shared strings
         */
        public MyXSSFSheetHandler(StylesTable styles, ReadOnlySharedStringsTable strings, HashMap<String, String> terms )
        {
            this.stylesTable = styles;
            this.sharedStringsTable = strings;
            this.value = new StringBuffer();
            this.nextDataType = xssfDataType.NUMBER;
            this.formatter = new DataFormatter();
            this.terms = terms;
        }

        /*
         * (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String,
         * java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
        {

            if ( "inlineStr".equals( name ) || "v".equals( name ) )
            {
                vIsOpen = true;
                // Clear contents cache
                value.setLength( 0 );
            }
            // c => cell
            else if ( "c".equals( name ) )
            {
                // Get the cell reference
                String r = attributes.getValue( "r" );
                int firstDigit = -1;
                for ( int c = 0; c < r.length(); ++c )
                {
                    if ( Character.isDigit( r.charAt( c ) ) )
                    {
                        firstDigit = c;
                        break;
                    }
                }
                thisColumn = nameToColumn( r.substring( 0, firstDigit ) );

                // Set up defaults.
                this.nextDataType = xssfDataType.NUMBER;
                this.formatIndex = -1;
                this.formatString = null;
                String cellType = attributes.getValue( "t" );
                String cellStyleStr = attributes.getValue( "s" );
                if ( "b".equals( cellType ) ) nextDataType = xssfDataType.BOOL;
                else if ( "e".equals( cellType ) ) nextDataType = xssfDataType.ERROR;
                else if ( "inlineStr".equals( cellType ) ) nextDataType = xssfDataType.INLINESTR;
                else if ( "s".equals( cellType ) ) nextDataType = xssfDataType.SSTINDEX;
                else if ( "str".equals( cellType ) ) nextDataType = xssfDataType.FORMULA;
                else if ( cellStyleStr != null )
                {
                    // It's a number, but almost certainly one
                    // with a special style or format
                    int styleIndex = Integer.parseInt( cellStyleStr );
                    XSSFCellStyle style = stylesTable.getStyleAt( styleIndex );
                    this.formatIndex = style.getDataFormat();
                    this.formatString = style.getDataFormatString();
                    if ( this.formatString == null ) this.formatString = BuiltinFormats.getBuiltinFormat( this.formatIndex );
                }
            }

        }

        /*
         * (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        public void endElement(String uri, String localName, String name) throws SAXException
        {

            String thisStr = null;

            // v => contents of a cell
            if ( "v".equals( name ) )
            {
                // Process the value contents as required.
                // Do now, as characters() may be called more than once
                switch (nextDataType)
                {

                    case BOOL:
                        char first = value.charAt( 0 );
                        thisStr = first == '0' ? "FALSE" : "TRUE";
                        break;

                    case ERROR:
                        thisStr = "\"ERROR:" + value.toString() + '"';
                        break;

                    case FORMULA:
                        // A formula could result in a string value,
                        // so always add double-quote characters.
                        thisStr = '"' + value.toString() + '"';
                        break;

                    case INLINESTR:
                        XSSFRichTextString rtsi = new XSSFRichTextString( value.toString() );
                        thisStr = '"' + rtsi.toString() + '"';
                        break;

                    case SSTINDEX:
                        String sstIndex = value.toString();
                        try
                        {
                            int idx = Integer.parseInt( sstIndex );
                            XSSFRichTextString rtss = new XSSFRichTextString( sharedStringsTable.getEntryAt( idx ) );
                            thisStr = '"' + rtss.toString() + '"';
                        }
                        catch (NumberFormatException ex)
                        {
                            System.out.println( "Failed to parse SST index '" + sstIndex + "': " + ex.toString() );
                        }
                        break;

                    case NUMBER:
                        String n = value.toString();
                        if ( this.formatString != null ) thisStr = formatter.formatRawCellContents( Double.parseDouble( n ),
                                        this.formatIndex, this.formatString );
                        else
                            thisStr = n;
                        break;

                    default:
                        thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                        break;
                }

                // Output after we've seen the string contents
                // Emit commas for any fields that were missing on this row
                if ( lastColumnNumber == -1 )
                {
                    lastColumnNumber = 0;
                }
                for ( int i = lastColumnNumber; i < thisColumn; ++i )
                    cols.add( "" );

                // Might be the empty string.
                cols.add( thisStr );

                // Update column
                if ( thisColumn > -1 ) lastColumnNumber = thisColumn;

            } 
            else if ( "row".equals( name ) )
            {
                if  ( cols.size() >= 2 && cols.get(0).trim().length() > 0 && cols.get( 2 ).trim().length() > 0 )
                    terms.put( cols.get( 0 ).replace( '"', ' ' ).trim(), cols.get( 2 ).replace( '"', ' ' ).trim() );

                // We're onto a new row
                cols = new ArrayList<String>();
                lastColumnNumber = -1;
            }

        }

        /**
         * Captures characters only if a suitable element is open. Originally was just "v"; extended for inlineStr
         * also.
         */
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            if ( vIsOpen ) value.append( ch, start, length );
        }

        /**
         * Converts an Excel column name like "C" to a zero-based index.
         * 
         * @param name
         * @return Index corresponding to the specified name
         */
        private int nameToColumn(String name)
        {
            int column = -1;
            for ( int i = 0; i < name.length(); ++i )
            {
                int c = name.charAt( i );
                column = ( column + 1 ) * 26 + c - 'A';
            }
            return column;
        }

    }
}
