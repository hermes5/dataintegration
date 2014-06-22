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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Demonstrates a VERY simple implementation of a search and replace algortihm for use with XWPF.
 */
public class SearchReplace
{

    private File sourceFile = null;
    private Hashtable<String, String> srTerms = null;
    private XWPFDocument doc = null;

    /**
     * The constructor which opens the named Word file.
     * 
     * @param filename An instance of the String class that encapsulates the name of and path to a Word (.docx)
     *            document.
     * @throws java.io.IOException Thrown if a problem is encountered opening and InutStream onto the Word file.
     */
    public SearchReplace(String filename) throws IOException
    {
        InputStream is = null;
        try
        {
            this.sourceFile = new File( filename );
            // Verify the the Word document exists and can be read
            if ( this.sourceFile.exists() || this.sourceFile.canRead() )
            {

                // Attach a FileInputStream to the Word document file
                // and read it's contents into memory.
                is = new FileInputStream( this.sourceFile );
                this.doc = new XWPFDocument( is );
                this.srTerms = new Hashtable<String, String>();
            } else
            {
                throw new IllegalArgumentException( "The file " + filename + " is not accessible." );
            }
        }
        catch (IOException ioEx)
        {
            System.out.println( "IOException thrown opening Word document." );
            throw new IOException( ioEx );
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException ioEx)
            {
                System.out.println( "IOException thrown closing InputStream." );
                throw new IOException( ioEx );
            }
        }
    }

    /**
     * Add a search term and it's associated replacement text using the following parameters.
     * 
     * @param searchTerm An instance of the String class that encapsulates the word or words that are to be searched
     *            for.
     * @param replacementText An instance of the String class that encapsulates the word or words that will be
     *            substituted in place of the search term wherevener it is found in the document's text.
     */
    public void addSearchTerm(String searchTerm, String replacementText)
    {
        // Simply add the search term and it's asociated replacement text
        // to the Hashtable that is used to store these pairings. Note that
        // no check is made to ensure that either parameter is present.
        this.srTerms.put( searchTerm, replacementText );
    }

    /**
     * Called to perform the search and replace operation. This method will begin with the first character run in
     * the first paragraph of text and process each. through to the end of the document. The process is essentially
     * this; * Get a paragraph from the document. * Get the runs of text from that paragraph. * Enumerate through
     * the keys in the search text/replacement term Hashtable and check to see whether the search tearm (the key)
     * can be found in the run. If it can be found, replace the search term with it's associated replacement text
     * and write the results back into the run. * Store the Word document away agin following processing. Note that
     * there is currently no provision made for writing the resulting document away under a different file name.
     * This would be a trivial change to make however.
     * 
     * @throws java.io.FileNotFoundException Thrown if a problem occurs in the underlying file system whilst trying
     *             to open a FileOutputStream attached to the Word document.
     * @throws java.io.IOException Thrown if a problem occurs in the underlying file system whilst trying to store
     *             the Word document away.
     */
    public void searchReplace() throws FileNotFoundException, IOException
    {

        // Recover an Iterator to step through the XWPFParagraph objects
        // 'contained' with the Word document.
        Iterator<XWPFParagraph> parasIter = this.doc.getParagraphsIterator();
        Iterator<XWPFRun> runsIter = null;
        Enumeration<String> srchTermEnum = null;
        XWPFParagraph para = null;
        XWPFRun run = null;
        List<XWPFRun> runsList = null;
        String searchTerm = null;
        String runText = null;
        String replacementText = null;
        OutputStream os = null;

        // Step through the XWPFParagraph object one at a time.
        while (parasIter.hasNext())
        {
            para = parasIter.next();

            // Recover a List of XWPFRun objects from the XWPFParagraph and an
            // Iterator from this List.
            runsList = para.getRuns();
            runsIter = runsList.iterator();

            // Use the Iterator to step through the XWPFRun objects.
            while (runsIter.hasNext())
            {

                // Get a run and it's text
                run = runsIter.next();
                runText = run.getText( 0 );

                // Recover an Enumeration object 'containing' the set of
                // search terms and then step through these terms looking
                // for match in the run's text
                srchTermEnum = this.srTerms.keys();
                while (srchTermEnum.hasMoreElements())
                {
                    searchTerm = srchTermEnum.nextElement();

                    // If a match is found, use the replacement text to substitute
                    // for the search term and update the runs text.
                    if ( runText.contains( searchTerm ) )
                    {
                        replacementText = this.srTerms.get( searchTerm );

                        // Note the use of the replaceAll() method here. It will,
                        // obviously, replace all occurrences of the search term
                        // with the replacement text. This may not be what is
                        // required but is easy to modify by changing the logic
                        // slightly and using the replaceFirst() or replace()
                        // methods.
                        runText = runText.replaceAll( searchTerm, replacementText );
                        run.setText( runText, 0 );
                    }
                }
            }
        }
        os = new FileOutputStream( this.sourceFile );
        this.doc.write( os );
    }
}
