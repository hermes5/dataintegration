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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/*******************************************************************************
 * SharePoint REST Client API.
 * <p>
 * 
 * @version $Revision: 1.9 $ $Date: 2013/09/16 09:36:08 $
 * @author mbern
 *******************************************************************************/

public class SharePointRESTClient
{
    /** Remote Hosts */
    private String remote = "http://win2008sp/websites/test1";
    /** Username */
    private String user = "hermes5";
    /** Password */
    private String pass = "point%123-";
    /** HTTP Client */
    private DefaultHttpClient client;
    // RequestDigest - Zugriffstoken zum Schreiben in SharePoint
    private String xRequestDigest;
    /** Script Factory */
    private ScriptEngineManager factory = new ScriptEngineManager();
    /** JavaScript engine */
    private ScriptEngine js;
    /** Properites */
    private enum properties { SP_SITE, SP_USER, SP_PASS };
    
    /**
     * Konstruktor mit Default Values - nur zum Testen!
     * @throws IOException Allgemeiner I/O Fehler
     */
    public SharePointRESTClient() throws IOException
    {
        init( null, null, null );
    }
    
    /**
     * Konstruktor mit Default Values - nur zum Testen!
     * @throws IOException Allgemeiner I/O Fehler
     */
    public SharePointRESTClient( String filename ) throws IOException
    {
        Properties conf = new Properties();
        conf.load(  new InputStreamReader( new FileInputStream( filename ) ) );
        
        init( conf.getProperty( properties.SP_SITE.toString() ), 
              conf.getProperty( properties.SP_USER.toString() ), 
              conf.getProperty( properties.SP_PASS.toString() ) );
    }

    /**
     * Konstruktor mit den minimalen Parametern fuer den SharePoint I/O.<p>
     * Es wird eine erste Verbindung zu SharePoint geoffnet und die FormDigestValue gelesen.
     * @param remote SharePoint Host
     * @param user Username
     * @param pass Password 
     * @throws IOException Allgemeiner I/O Fehler
     */
    public SharePointRESTClient(String remote, String user, String pass) throws IOException
    {
        init( remote, user, pass );
    }

    /**
     * @throws IOException
     */
    private void init(String remote, String user, String pass ) throws IOException
    {
        this.remote = (remote != null) ? remote : this.remote;
        this.user = (user != null) ? user : this.user;
        this.pass = (pass != null) ? pass : this.pass;
        
        client = getHttpClient();
        
        // localhost und domain nicht setzen, gibt Probleme mit https://
        NTCredentials creds = new NTCredentials( this.user, this.pass, "", "" );
        client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
        
        List<String> authpref = new ArrayList<String>();
        authpref.add( AuthPolicy.NTLM );
        client.getParams().setParameter( AuthPNames.TARGET_AUTH_PREF, authpref );
        
        // FormDigestValue holen
        try
        {
            js = factory.getEngineByName( "JavaScript" );
            js.eval( "function getId(rc) { print(rc); return(rc.d.GetContextWebInformation.FormDigestValue); }"  );
            
            String out = post( "/_api/contextinfo", null );
            js.eval( "var rc = " + out + ";" );
            Object rc = js.get( "rc" );
            xRequestDigest = (String) ((Invocable) js).invokeFunction( "getId", rc );
        }
        catch (NoSuchMethodException e)
        {
            throw new IOException( e );
        }
        catch (ScriptException e)
        {
            throw new IOException( e );
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static void main(String[] args) throws Exception
    {
        SharePointRESTClient client = new SharePointRESTClient( args[1] );
        
        
        client.createSubSite( "t4" );
        
        //System.out.println( "load library " + args[0] );
        //MethodExtract extract = new MethodExtract();
        //H5MethodLibrary root = extract.extract( args[0] );
        
/*        for ( H5WorkProduct wp : root.getWorkProducts().values() )
        {
            for ( String file : wp.getTemplates() )
            {
                String name = file.substring( file.lastIndexOf( '/' )+1 );
                client.createDocument( "Test", name, file );
            }
        }*/
        
/*        for ( H5Scenario scenario : root.getScenarios() )
        {
            String path = "Dokumente/" + scenario.getName();
            client.createFolder( path );
            for ( String template : scenario.getTemplates() )
            {
                String name = template.substring( template.lastIndexOf( '/' )+1 );
                client.createDocument( path, name, template );
            }
        }*/
        
        //String out = client.get( "/_api/web/title" );
        //System.out.println( out );
        
        // Alle Listen holen
        //out = client.get( "/_api/lists" );
        //System.out.println( out );
        
        //client.deleteAllItems( "Ergebnisse" );
        
/*        // ein Item holen
        out = client.get( "/_api/lists/getbytitle('Aufgaben')/getItemByStringId('263')" );
        //System.out.println( out );   
        
        out = client.delete( "/_api/Web/Lists(guid'97989600-6765-4161-8557-03740a84c437')/Items(271)", "2" );
        //out = client.delete( "/_api/lists/getbytitle('Aufgaben')/items('263')", "2" );
        System.out.println( out );           
        
        out = client.get( "/_api/lists/getbytitle('Ergebnisse')/items" );
        System.out.println( out );*/
        
        
        
        /*        String rc = client.post( "/_api/lists/getbytitle('Customers')/items')", 
                     "{ '__metadata': { 'type': 'SP.Data.CustomersListItem' }, 'Title': '019', 'Name': 'Hansli', 'Address': 'Bern' }" );
        System.out.println( rc );
        
        String[][] data = { { "Title", "020", }, { "Name", "Otto", }, { "Address", "Basel" } };
        client.addData( "Customers", data );*/
        
/*        client.post( "/_api/lists", "{" +
                                        "'__metadata':{ 'type': 'SP.List' }," +
                                        "'AllowContentTypes': true," +
                                        "'BaseTemplate': 107," + 
                                        "'ContentTypesEnabled': true," + 
                                        "'Description': 'My list description'," +
                                        "'Title': 'RestTest5'" +
                                      "}" );*/
        
        // Folder
        //client.post( "/_api/web/folders", "{ '__metadata': { 'type': 'SP.Folder' }, 'ServerRelativeUrl': 'Test/Test 2' }" );
        
        // Datei schreiben
        //client.post( "/_api/web/GetFolderByServerRelativeUrl('Test/Test%201')/Files/add(url='test1.txt',overwrite=true)", "Das ist ein Test und so weiter");
        
        //client.post( "/_api/lists/getbytitle('MyWiki')/items'", "{ '__metadata': { 'type': 'SP.Data.SitePagesItem' }, 'WikiField' : 'HTML entity coded wiki content goes here' }");
        
        //client.postFile( "/_api/web/GetFolderByServerRelativeUrl('Test/Test%202')/Files/add(url='Projektmanagementplan.docx',overwrite=true)", 
        //                new File("D:/TEMP/hermes5/hermes.core/guidances/examples/resources/Projektmanagementplan.docx") );
        
        //client.createDocument( "Test", "Projektmanagementplan.docx", "D:/TEMP/hermes5/hermes.core/guidances/examples/resources/Projektmanagementplan.docx" );
        //client.postFile( "/_api/web/GetFolderByServerRelativeUrl('Test/Test%202')/Files/add(url='Projektmanagementplan.docx',overwrite=true)", 
        //                new File("D:/TEMP/hermes5/hermes.core/guidances/examples/resources/Projektmanagementplan.docx") );
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  HighLevel Methoden
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Erstellt eine neue Sub Site - FIXME: bringt 400 Status
     * @param list Name der Liste.
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String createSubSite( String name ) throws IOException
    {
        return  ( post( "/_api/web/webinfos/add", "{" +
                        "'__metadata':{ 'type': 'SP.WebInfoCreationInformation' }," +
                        "'Url': '" + name + "'," +
                        "'Title': '" + name + "'," +
                        "'Description': '" + name + "'," +
                        "'Language': 1033," +
                        "'WebTemplate': " + "'sts#0'" + "," + 
                        "'UseUniquePermissions': false" + 
                      "}" ) );
    }
    
    /**
     * Erstellt eine neue Liste
     * @param list Name der Liste.
     * @param type Art der Liste 105 = Kontakte, 107 = Aufgaben
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String createList( String name, int type ) throws IOException
    {
        return  ( post( "/_api/lists", "{" +
                        "'__metadata':{ 'type': 'SP.List' }," +
                        "'AllowContentTypes': true," +
                        "'BaseTemplate': " + type + "," + 
                        "'ContentTypesEnabled': true," + 
                        "'Title': '" + name + "'" +
                      "}" ) );
    }
    
    /**
     * Fuegt Daten zu einer SharePoint Liste hinzu. Die Liste muss vorhanden sein!
     * @param list Name der Liste.
     * @param data Daten in der Form { ColumnName, Daten }
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String addData( String list, String[][] data ) throws IOException
    {
        StringBuffer out = new StringBuffer();
        
        out.append( "{ '__metadata': { 'type': 'SP.Data." + list + "ListItem' }" );
        
        for ( int i = 0; i < data.length; i++ )
            // Sub Array
            if  ( data[i] [1].startsWith( "{" ) )
                out.append( ", '" + data[i] [0] + "': " + data[i] [1] );
            else
                out.append( ", '" + data[i] [0] + "': '" + data[i] [1] + "'" );
        
        out.append( "}" );
        
        return  ( post( "/_api/lists/getbytitle('" + list + "')/items')", out.toString() ) );
    }
    
    /**
     * Loescht alle Items einer Liste
     * @param list Name der Liste z.B. Aufgaben
     * @throws NoSuchMethodException
     * @throws ScriptException
     * @throws IOException
     */
    public void deleteAllItems( String list ) throws NoSuchMethodException, ScriptException, IOException
    {
        String out = get( "/_api/lists/getbytitle('" + list + "')/items" );
        ArrayList items = (ArrayList) getJSObject( "function getItems(rc) { var a = java.util.ArrayList();" + 
                                                                                  "for( var x = 0; x < rc.d.results.length; x++ ) " +
                                                                                       "{ a.add( rc.d.results[x].__metadata.uri); }; " + 
                                                                                 "return(a); }", "getItems", out );
        String etag = null;
        if  ( items.size() > 0 )
            etag = (String) getJSObject( "function getEtag(rc) { return( rc.d.results[0].__metadata.etag ); }", "getEtag", out ); 
        for ( Object uri : items )
            delete( ((String) uri).substring( remote.length() ), etag );
    }
    
    /**
     * Erstellt ein Verzeichnis fuer die Dokumentenablage
     * @param path Pfadangabe ab Root, z.B. "Freigebene Dokumente/Test 1"
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String createFolder( String path ) throws IOException
    {
        return  ( post( "/_api/web/folders", "{ '__metadata': { 'type': 'SP.Folder' }, 'ServerRelativeUrl': '" + path + "' }" ) );
    }
    
    /**
     * Erstellt ein Dokument mit dem Inhalt laut content.
     * @param path Pfadangabe ab Root, z.B. "Freigebene Dokumente/"
     * @param name Dateiname z.B. MeinDokument.txt
     * @param content Inhalt
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String createDocumentContent( String path, String name, String content ) throws IOException
    {
        return  ( post( "/_api/web/GetFolderByServerRelativeUrl('" + path + "')/Files/add(url='" + name + "',overwrite=true)", content) );
    }
    
    /**
     * Erstellt ein Dokument mit dem Inhalt laut content.
     * @param path Pfadangabe ab Root, z.B. "Freigebene Dokumente/"
     * @param name Dateiname z.B. Projektmanagementplan.docx
     * @param localname Lokaler Dateiname inkl. Pfad, z.B. C:/temp/Projektmanagementplan.docx
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String createDocument( String path, String name, String localname ) throws IOException
    {
        return  ( postFile( "/_api/web/GetFolderByServerRelativeUrl('" + path + "')/Files/add(url='" + name + "',overwrite=true)", new File(localname) ) );
    } 
    
    public String uploadDocument( String path, String name, String localname ) throws Exception
    {
        if  ( localname.startsWith( "http" ) )
            return  ( postFile( "/_api/web/GetFolderByServerRelativeUrl('" + path + "')/Files/add(url='" + name + "',overwrite=true)", new URL(localname) ) );
        // lokale Datei
        return  ( postFile( "/_api/web/GetFolderByServerRelativeUrl('" + path + "')/Files/add(url='" + name + "',overwrite=true)", new File(localname) ) );
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
     * LowLevel: Liest ab URI und gibt diesem im JSON Format zurueck. 
     * @param uri - URI ab Seite beginnend mit /_api/...
     * @param data Daten welche geschrieben werden sollen, darf null sein
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String get( String url ) throws IOException
    {
        // Execute a cheap method first. This will trigger NTLM authentication
        HttpGet request = new HttpGet( remote + url );
        request.addHeader( "accept", "application/json;odata=verbose;charset=utf-8" );
        
        HttpResponse response = client.execute( request);
        checkError( response, url );
        
        HttpEntity entity = response.getEntity();
        BufferedReader isr = new BufferedReader (new InputStreamReader(entity.getContent())); 
        
        StringBuffer str = new StringBuffer();
        String line = "";
        while ((line = isr.readLine()) != null) 
          str.append( line );
 
        EntityUtils.consume(entity);
        return  ( str.toString() );
    }
    
    /**
     * LowLevel: Schreibt Daten im JSON Format. Die Daten muessen korrekt aufbereitet sein.
     * @param uri - URI ab Seite beginnend mit /_api/...
     * @param data Daten welche geschrieben werden sollen, darf null sein
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String post( String uri, String data ) throws IOException
    {
        HttpPost request = new HttpPost( remote + uri );
        request.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/json;odata=verbose;charset=utf-8" );        
        
        if  ( xRequestDigest != null )
            request.addHeader( "X-RequestDigest", xRequestDigest );
        
        if  ( data != null )
        {
            StringEntity entity = new StringEntity( data, "UTF-8" );
            request.setEntity( entity ); 
        }
        
        HttpResponse response = client.execute( request );
        checkError( response, uri );
        
        HttpEntity entity = response.getEntity();
        BufferedReader isr = new BufferedReader (new InputStreamReader(entity.getContent())); 
        
        StringBuffer str = new StringBuffer();
        String line = "";
        while ((line = isr.readLine()) != null) 
          str.append( line );
 
        EntityUtils.consume(entity);
        return  ( str.toString() );
    }
    
    public String postFile( String uri, URL url ) throws IOException, URISyntaxException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if  ( url != null )
        {
            HttpGet req = new HttpGet( url.toURI() );
            req.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
            req.addHeader( "Content-Type", "application/msword" );   
            
            HttpResponse resp = client.execute( req);
            checkError( resp, url.toString() );
            
            byte buf[] = new byte[128 * 32768];
            InputStream inp = resp.getEntity().getContent();
            for ( int length; (length = inp.read( buf, 0, buf.length )) > 0; )
                out.write( buf, 0, length );
            out.close();
            
            EntityUtils.consume( resp.getEntity() );   
        }
        
        HttpPost request = new HttpPost( remote + uri );
        request.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/msword" );        
        
        if  ( xRequestDigest != null )
            request.addHeader( "X-RequestDigest", xRequestDigest );
        
        if  ( url != null )
        {
            ByteArrayEntity entity = new ByteArrayEntity( out.toByteArray() );
            request.setEntity( entity ); 
        }
        
        HttpResponse response = client.execute( request );
        checkError( response, uri );
        
        HttpEntity entity = response.getEntity();
        BufferedReader isr = new BufferedReader (new InputStreamReader(entity.getContent())); 
        
        StringBuffer str = new StringBuffer();
        String line = "";
        while ((line = isr.readLine()) != null) 
          str.append( line );
 
        EntityUtils.consume(entity);
        return  ( str.toString() );
    }    
    
    public String postFile( String uri, File data ) throws IOException
    {
        HttpPost request = new HttpPost( remote + uri );
        request.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/msword" );        
        
        if  ( xRequestDigest != null )
            request.addHeader( "X-RequestDigest", xRequestDigest );
        
        if  ( data != null )
        {
             FileEntity entity = new FileEntity( data );
            request.setEntity( entity ); 
        }
        
        HttpResponse response = client.execute( request );
        checkError( response, uri );
        
        HttpEntity entity = response.getEntity();
        BufferedReader isr = new BufferedReader (new InputStreamReader(entity.getContent())); 
        
        StringBuffer str = new StringBuffer();
        String line = "";
        while ((line = isr.readLine()) != null) 
          str.append( line );
 
        EntityUtils.consume(entity);
        return  ( str.toString() );
    }
    
    /**
     * LowLevel: Loescht Daten
     * @param uri - URI ab Seite beginnend mit /_api/...
     * @param etag - Etag zur Absicherung, damit der richtige Eintrag geloescht wird
     * @return body welcher der Remote Host zurueckgibt
     * @throws IOException Allgemeiner I/O Fehler
     */
    public String delete( String uri, String etag ) throws IOException
    {
        HttpDelete request = new HttpDelete( remote + uri );
        request.addHeader( "Accept", "application/son;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/json;odata=verbose;charset=utf-8" );   
        request.addHeader(  "IF-MATCH", etag );
        
        if  ( xRequestDigest != null )
            request.addHeader( "X-RequestDigest", xRequestDigest );
        
        HttpResponse response = client.execute( request );
        checkError( response, uri );
        
        HttpEntity entity = response.getEntity();
        BufferedReader isr = new BufferedReader (new InputStreamReader(entity.getContent())); 
        
        StringBuffer str = new StringBuffer();
        String line = "";
        while ((line = isr.readLine()) != null) 
          str.append( line );
 
        EntityUtils.consume(entity);
        return  ( str.toString() );
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

    /**
     * Daten von JavaScript Array holen
     * @param script JavaScript (eval)
     * @param method JavaScript Methode welche aufgerufen werden soll um einen Wert zu liefern
     * @param data JavaScript Array welche ausgewertet werden soll
     * @return JavaScript Return der Methode
     * @throws ScriptException
     * @throws NoSuchMethodException
     */
    public Object getJSObject( String script, String method, String data ) throws ScriptException, NoSuchMethodException
    {
        js.eval( script  );
        js.eval( "var rc = " + data + ";" );
        Object rc = js.get( "rc" );
        return  ( ((Invocable) js).invokeFunction( method, rc ) );
    }
}
