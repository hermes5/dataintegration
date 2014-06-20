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

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*******************************************************************************
 * Daten von http://www.hermes.admin.ch aufbereiten, z.B. Szenarios, Vorlagen etc.<p>
 * 
 * @author  mbern
 *******************************************************************************/

public class HermesOnlineCrawler
{
    /** ROOT URL zur HERMES 5 Online Loesung */
    private String url = "http://www.hermes.admin.ch";
    /** Link zur Szenarioen Uebersicht */
    private String scenarios = "/anwenderloesung/szenarien-overview.xhtml";
    /** Allgemeiner Prefix URL Szenarioen */
    private String scenario_prefix = "/szenarien/szenario_";
    /** Sprache */
    private String lang = "de";
    /** Vorlagen */
    private String templates;
    /** XML Model */
    private String model;
    /** HTTP Client API */
    private HttpClient httpClient = new DefaultHttpClient();
    
    /**
     * Default Konstruktor - Sprache "de"
     */
    public HermesOnlineCrawler()
    {
        this    ( null, null );
    }

    /**
     * Default Konstruktor mit URL und Sprache
     * @param url
     * @param lang
     */
    public HermesOnlineCrawler(String url, String lang )
    {
        if  ( url != null )
            this.url = url;
        if  ( lang != null )
            this.lang = lang;
        
        model = "/model/" + this.lang + "/model_" + this.lang + ".xml";
        templates = "/templates/" + this.lang;
    }
    
    /**
     * Liefert alle Szenarion URL's 
     * @return 
     * @throws Exception Allgemeiner I/O Fehler
     */
    public String[] getScenarios() throws Exception
    {
        ArrayList<String> s = new ArrayList<String>();
        HttpGet get = new HttpGet( url + scenarios );
        
        try
        {
            HttpResponse response = httpClient.execute( get );

            HttpEntity entity = response.getEntity();
            String pageHTML = EntityUtils.toString( entity );
            EntityUtils.consume( entity );

            Document document = Jsoup.parse( pageHTML );
            Elements elements = document.getElementsByAttribute( "href" );
            for ( Element e : elements )
            {
                if  ( e.attr( "href" ).startsWith( "/szenarien" ) )
                {
                    String attr = e.attr( "href" ).substring( scenario_prefix.length()  );
                    attr = attr.substring( 0, attr.lastIndexOf( '/' ) );
                    s.add( attr );
                }
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog( null, "Keine Online Verbindung möglich. Bitte Szenario manuell downloaden, entpacken und bei XMl Model eintragen." , 
                            "Keine Verbindung zu http://www.hermes.admin.ch", JOptionPane.WARNING_MESSAGE );
            
        }
        return  ( s.toArray( new String[s.size()] ));
    }
    
    /**
     * Liefert die URL's zu den Vorlagen 
     * @param scenario Szenario
     * @return
     * @throws Exception Allgemeiner I/O Fehler
     */
    public String[] getTemplatesURL( String scenario ) throws Exception
    {
        ArrayList<String> s = new ArrayList<String>();
        HttpGet get = new HttpGet( url + scenario_prefix + scenario + templates );
        
        HttpResponse response = httpClient.execute( get );

        HttpEntity entity = response.getEntity();
        String pageHTML = EntityUtils.toString( entity );
        EntityUtils.consume( entity );

        Document document = Jsoup.parse( pageHTML );
        Elements elements = document.getElementsByAttribute( "href" );
        for ( Element e : elements )
        {
            String attr = e.attr( "href" );
            if  ( attr.endsWith( ".docx" ) || attr.endsWith( ".xlsx" ) || attr.endsWith( ".pptx" ) )
                s.add( url + scenario_prefix + scenario + templates + attr );
        }
        return  ( s.toArray( new String[s.size()] ));
    }
    
    /**
     * Korrigiert einen Template URL und gibt den korrigierten zurueck.
     * @param scenario
     * @param old
     * @return
     */
    public String getTemplateURL( String scenario, String old )
    {
        int i = 0;
        for ( ; i < url.length(); i++ )
            if  ( old.charAt( i ) != '.' && old.charAt( i ) != '/' )
                break;
        return  ( url + scenario_prefix + scenario + "/" + old.substring( i ) );
    }
    
    /**
     * Liefert den vollstaendigen URL zum XML Model
     * @param scenario Szenario
     * @return
     * @throws Exception Allgemeiner I/O Fehler
     */
    public String getModelURL( String scenario ) throws Exception
    {
        return  ( url + scenario_prefix + scenario + model );
    }
}
