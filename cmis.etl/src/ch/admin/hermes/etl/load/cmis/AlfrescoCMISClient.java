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

package ch.admin.hermes.etl.load.cmis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.MimetypesFileTypeMap;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/*******************************************************************************
 * CMIS Client Code fuer Alfresco via CMIS Schnittstelle.<p>
 *******************************************************************************/

public class AlfrescoCMISClient
{
    /** Remote Host */
    private String host = "http://alfresco:8080/alfresco/s/cmis";
    /** Default Path wo die Daten abgestellt werden */
    private String path = "/Data Dictionary/Space Templates";
    /** API Path fuer den Zugriff via CMIS */
    private static final String CMIS = "/alfresco/s/cmis";
    /** Username */
    private String user = "admin";
    /** Password */
    private String pass = "admin";
    
    /** Effektive CMIS Session */
    private Session session;
    
    /** Hilfsklasse um den MIME Type zu finden */
    private MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
    /** HTTP Client */
    private DefaultHttpClient client;
    
    /**
     * Default Konstruktor fuer JavaScript
     * @param remote URL 
     * @param user User
     * @param pass Password
     * @throws Exception Allgemeiner I/O Fehler
     */
    public AlfrescoCMISClient(String remote, String user, String pass) throws Exception
    {
        init( remote, user, pass );
    }
    
    /**
     * Initialisierung, wird von JavaScript aufgerufen
     * @throws Exception Allgemeiner I/O Fehler
     */
    private void init(String remote, String user, String pass ) throws Exception
    {
        this.user = (user != null) ? user : this.user;
        this.pass = (pass != null) ? pass : this.pass;
        
        // Host und Path trennen, weil unterschiedlich verwendet.
        if  ( remote != null )
        {
            URL url = new URL(  remote );
            this.host = url.getProtocol() + "://" + url.getAuthority();
            if  ( url.getPath() != null && url.getPath().trim().length() > 0 )
                this.path = url.getPath();
        }
        
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

        Map<String, String> params = new HashMap<String, String>();
        params.put( SessionParameter.USER, this.user );
        params.put( SessionParameter.PASSWORD, this.pass );
        params.put( SessionParameter.ATOMPUB_URL, this.host + CMIS );
        params.put( SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value() );

        // session locale
        params.put( SessionParameter.LOCALE_ISO3166_COUNTRY, "" );
        params.put( SessionParameter.LOCALE_ISO639_LANGUAGE, "de_CH" );
        params.put( SessionParameter.LOCALE_VARIANT, "" );

        // Set the alfresco object factory
        params.put( SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl" );

        List<Repository> repos = sessionFactory.getRepositories( params );
        // Alfresco kennt keine Repositories?
        session = repos.get( 0 ).createSession();
        
        // Zugriff auf http://www.hermes.admin.ch
        client = getHttpClient();
    }

    /**
     * Liefert die Pfadangabe, wo die Dateien abgestellt werden sollen.
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Erstellt im Parent ein Folder 
     * @param parentId Id des Parent Folders
     * @param name Name neues Verzeichnis
     * @return neu erstelltes Verzeichnis
     */
    public Folder createFolder( String parentId, String name )
    {
        Folder parent = (Folder) session.getObject( parentId, session.getDefaultContext() );
        Map<String, Object> properties = new HashMap<String, Object>();
        
        properties.put( "cmis:name", name );
        // Alfresco braucht ein F: als Prefix fuer Dossiers
        properties.put( "cmis:objectTypeId", "cmis:folder" ); 
        Folder folder = parent.createFolder( properties );
        return  ( folder );
        
    }
    
    /**
     * Erstellt ein Dokumente im DMS
     * @param parentId Parent ID
     * @param name Name
     * @param properties Eigenschaften 
     * @param mimeType MIME Type z.B. text/html
     * @param content Inhalt der Datei
     * @return erstelles Dokument
     */
    public Document createDocument( String parentId, String name, HashMap<String, Object> properties, String mimeType, String content )
    {
        Folder parent = (Folder) session.getObject( parentId, session.getDefaultContext() );
        
        if  ( properties == null )
            properties = new HashMap<String, Object>();
        
        properties.put( "cmis:name", name );
        // This works because we are using the OpenCMIS extension for Alfresco
        properties.put( "cmis:objectTypeId", "cmis:document,P:cm:titled" );

        ContentStream contentStream = new ContentStreamImpl( name, mimeType, content );
        properties.put( PropertyIds.CONTENT_STREAM_FILE_NAME, name );
        properties.put( PropertyIds.CONTENT_STREAM_LENGTH, content.length() );
        properties.put( PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType );

        Document doc = parent.createDocument( properties, contentStream, VersioningState.MAJOR );
        return ( doc );
    }
    
    /**
     * Laedt ein Dokument in Alfresco hoch.
     * @param parentId Parent Id
     * @param name Name
     * @param properties Eigenschaften
     * @param localname Lokaler Name wenn Datei oder URL wenn von http://www.hermes.admin.ch
     * @return neu erstelles Dokument
     * @throws Exception Allgemeiner I/O Fehler
     */
    public Document uploadDocument( String parentId, String name, HashMap<String, Object> properties, String localname ) throws Exception
    {
        if  ( localname.startsWith( "http" ) )
            return  ( postFile( parentId, name, properties, new URL(localname) ) );
        // lokale Datei
        return  ( postFile( parentId, name, properties, new File(localname) ) );
    }
    
    /**
     * Schreibt eine lokale Datei in Alfresco hoch.
     * @param parentId Parent Id
     * @param name Name
     * @param properties Eigenschaften
     * @param localname Lokaler Name wenn Datei oder URL wenn von http://www.hermes.admin.ch
     * @return neu erstelles Dokument
     * @throws Exception Allgemeiner I/O Fehler
     */
    private Document postFile( String parentId, String name, HashMap<String, Object> properties, File content ) throws IOException
    {
        String mimeType = mimeTypesMap.getContentType( content );
        Folder parent = (Folder) session.getObject( parentId, session.getDefaultContext() );
        
        if  ( properties == null )
            properties = new HashMap<String, Object>();
        
        properties.put( "cmis:name", name );
        // This works because we are using the OpenCMIS extension for Alfresco
        properties.put( "cmis:objectTypeId", "cmis:document,P:cm:titled" );

        FileInputStream in = new FileInputStream( content );
        ContentStream contentStream = new ContentStreamImpl( name, BigInteger.valueOf( content.length() ), mimeType, in );
        properties.put( PropertyIds.CONTENT_STREAM_FILE_NAME, name );
        properties.put( PropertyIds.CONTENT_STREAM_LENGTH, content.length() );
        properties.put( PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType );

        Document doc = parent.createDocument( properties, contentStream, VersioningState.MAJOR );
        in.close();
        return ( doc );
    } 
    
    /**
     * Laedt eine Datei von http://www.hermes.admin.ch in Alfresco hoch.
     * @param parentId Parent Id
     * @param name Name
     * @param properties Eigenschaften
     * @param localname Lokaler Name wenn Datei oder URL wenn von http://www.hermes.admin.ch
     * @return neu erstelles Dokument
     * @throws Exception Allgemeiner I/O Fehler
     */
    private Document postFile( String parentId, String name, HashMap<String, Object> properties, URL url ) throws IOException, URISyntaxException
    {
        Folder parent = (Folder) session.getObject( parentId, session.getDefaultContext() );
        
        // Dokument von http://www.hermes.admin.ch holen
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if  ( url != null )
        {
            HttpGet req = new HttpGet( url.toURI() );
            req.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
            req.addHeader( "Content-Type", "application/octet-stream" );   
            
            HttpResponse resp = client.execute( req);
            checkError( resp, url.toString() );
            
            byte buf[] = new byte[128 * 32768];
            InputStream inp = resp.getEntity().getContent();
            for ( int length; (length = inp.read( buf, 0, buf.length )) > 0; )
                out.write( buf, 0, length );
            out.close();
            
            EntityUtils.consume( resp.getEntity() );   
        }
        String mimeType = mimeTypesMap.getContentType( url.getFile() );
        if  ( properties == null )
            properties = new HashMap<String, Object>();
        
        properties.put( "cmis:name", name );
        // This works because we are using the OpenCMIS extension for Alfresco
        properties.put( "cmis:objectTypeId", "cmis:document,P:cm:titled" );
        
        ByteArrayInputStream in = new ByteArrayInputStream( out.toByteArray() );
        ContentStream contentStream = new ContentStreamImpl( name, BigInteger.valueOf( out.size() ), mimeType, in );
        properties.put( PropertyIds.CONTENT_STREAM_FILE_NAME, name );
        properties.put( PropertyIds.CONTENT_STREAM_LENGTH, out.size() );
        properties.put( PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType );

        Document doc = parent.createDocument( properties, contentStream, VersioningState.MAJOR );
        in.close();
        return ( doc );
    }      
    
    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws Exception
    {
        AlfrescoCMISClient client = new AlfrescoCMISClient( null, null, null);
        
        Folder p = (Folder) client.getNodeByPath( "/Data Dictionary/Space Templates" );
        if  ( p != null )
        {
            Folder project = client.createFolder( p.getId(), "Test 1" );
            
            Folder tasks = client.createFolder( project.getId(), "Aufgaben" );
            client.createFolder( project.getId(), "Ergebnisse" );
            client.createFolder( project.getId(), "Rollen" );
            
            for ( int i = 0; i < 10; i++ )
                client.createDocument( tasks.getId(), "Test " + i + ".html", null, "text/html", client.getHTML( "Test" + i, "Beschreibung" + i, "Inhalt " + i ) );
        }
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Hilfsmethoden
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Liefert die Children zu einer Parent Id. 
     * @param parentId Parent Id oder null wenn die Root-Nodes gelesen werden sollen.
     * @return die Childs
     */
    public List<CmisObject> getChildren( String parentId )
    {
        ArrayList<CmisObject> childs = new ArrayList<CmisObject>();
        Folder parent = null;
        if  ( parentId == null )
            parent = session.getRootFolder();
        else
            parent = (Folder) session.getObject( parentId, session.getDefaultContext() );
        
        for ( CmisObject o : parent.getChildren() )
            childs.add( o );

        return  ( childs );
    }
    
    /**
     * Liefert eine Node anhand eines Pfades in DMS. Dabei kann es sich um ein Verzeichnis
     * oder Dokument handeln.
     * @param path Pfadangabe z.B. "/Data Dictionary/Space Templates"
     * @return Node oder null wenn nicht gefunden.
     */
    public CmisObject getNodeByPath( String path )
    {
        StringTokenizer tok = new StringTokenizer( path, "/" );
        String parentId = null;
        CmisObject node = null;
        
        while   ( tok.hasMoreTokens() )
        {
            String name = tok.nextToken();
            if  ( name.trim().length() == 0 )
                continue;
            
            node = getChildByName( parentId, name );
            if  ( node == null )
                return  ( null );
            parentId = node.getId();
        }
        return  ( node );
    }
    
    /**
     * Liefert die Node innnerhalb eines Verzeichnisses (parent)
     * @param parentId Parent Id
     * @param name Name z.B. "Space Templates"
     * @return Node oder null wenn nicht gefunden.
     */
    public CmisObject getChildByName( String parentId, String name )
    {
        List<CmisObject> childs = getChildren( parentId );
        for ( CmisObject n : childs )
            if  ( n.getName().compareToIgnoreCase( name ) == 0 )
                return  ( n );
        return  ( null );
    }
    
    /**
     * Erstellt HTML Content
     * @param name Name Dokument.
     * @param content Inhalt
     * @return aufbereiteter HTML Inhalt.
     */
    public String getHTML( String name, String description, String content )
    {
        return  ( "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" http://www.w3.org/TR/html4/loose.dtd\">\n" + 
                  "<html>\n" +
                  "<head>\n" + 
                  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" + 
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/theme.css.xhtml?ln=primefaces-aristo\" />\n" +
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/style.css.xhtml?ln=css\" />\n" +
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/messages.css.xhtml?ln=css\" />\n" +
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/tablestyle.css.xhtml?ln=css\" />\n" +
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/menustyle.css.xhtml?ln=css\" />\n" +
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/dialogstyle.css.xhtml?ln=css\" />\n" +
                  "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.hermes.admin.ch/javax.faces.resource/watermark/watermark.css.xhtml?ln=primefaces\" />\n" +
                  "<title>" + name + "</title>\n" +
                  "<meta name=\"DESCRIPTION\" content=\"" + description + "\"/>\n" +
                  "</head>\n" +
                  "<body>\n" +
                  content +
                  "</body>\n" +
                  "</html>\n" );
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  LowLevel Methoden 
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Pruefen auf HTTP Response Fehler, wenn ja wird IOException geworfen
     * @param response Response
     * @param url urspruenglicher URL
     * @throws IOException HTTP Fehler z.B. 400 o.ae.
     */
    private void checkError( HttpResponse response, String url ) throws IOException
    {
        if  ( response.getStatusLine().getStatusCode() < 300 )
            return;
        throw new IOException( response.getStatusLine().getReasonPhrase() + " " + url );
    }
    
    /**
     * Liefert einen Http Client wo alle Zertifikate erlaubt sind. Vermeidet Zertifikatfehler.
     * @return DefaultHttpClient
     */
    private DefaultHttpClient getHttpClient()
    {
        try
        {
            HttpParams httpParams = new BasicHttpParams();

            SSLSocketFactory sf = new SSLSocketFactory( new TrustStrategy()
            {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                    return true;
                }
            }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );

            SchemeRegistry registry = new SchemeRegistry();
            registry.register( new Scheme( "http", 80, PlainSocketFactory.getSocketFactory() ) );
            registry.register( new Scheme( "https", 443, sf ) );

            return new DefaultHttpClient( httpParams );
        }
        catch (Exception e)
        {
            return new DefaultHttpClient();
        }
    }
}
