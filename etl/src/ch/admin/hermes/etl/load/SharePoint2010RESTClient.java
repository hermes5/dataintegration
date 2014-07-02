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
import javax.script.ScriptException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/*******************************************************************************
 * SharePoint REST Client API fuer Version 2010!<p>
 * 
 * SharePoint 2010 stellt, im Gegensatz zu SharePoint 2013 ein sehr
 * eingeschraenkte Funktionalitaet zur Verfuegung.<p>
 * 
 * @version $Revision: 1.9 $ $Date: 2013/09/16 09:36:08 $
 * @author mbern
 *******************************************************************************/

public class SharePoint2010RESTClient
{
    /** Remote Hosts */
    private String remote = "http://win2008s/websites/test1/t1";
    /** Username */
    private String user = "hermes5";
    /** Password */
    private String pass = "point%123-";
    /** HTTP Client */
    private DefaultHttpClient client;
    /** JavaScript engine */
    private ScriptEngine js;
    /** Properites */
    private enum properties { SP_SITE, SP_USER, SP_PASS };
    
    /**
     * Konstruktor mit Default Values - nur zum Testen!
     * @throws IOException Allgemeiner I/O Fehler
     */
    public SharePoint2010RESTClient() throws IOException
    {
        init( null, null, null );
    }
    
    /**
     * Konstruktor mit Default Values - nur zum Testen!
     * @throws IOException Allgemeiner I/O Fehler
     */
    public SharePoint2010RESTClient( String filename ) throws IOException
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
    public SharePoint2010RESTClient(String remote, String user, String pass) throws IOException
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
    }

    /**
     * @param args
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static void main(String[] args) throws Exception
    {
        SharePoint2010RESTClient client = new SharePoint2010RESTClient();
        
        String[][] data = { { "Nachname", "Otto2", } };
        client.addData( "Rollen", data );
        
        String[][] data2 = { { "Titel", "Task 2" } };
        client.addData( "Aufgaben", data2 );
        
        client.createDocument( "Freigegebene%20Dokumente", "Test5.docx", "D:/temp/Test.docx" );
        
        client.uploadDocument( "Freigegebene%20Dokumente", "abnahmeprotokoll.docx", "http://www.hermes.admin.ch/szenarien/szenario_01_IT-Individualanwendung/templates/de/abnahmeprotokoll.docx" );
    }
    
    // //////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  HighLevel Methoden
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        
        out.append( "{" );
        
        for ( int i = 0; i < data.length; i++ )
            if  ( i > 0 )
                out.append( ", " + data[i] [0] + ": '" + data[i] [1] + "'" );
            else
                out.append( data[i] [0] + ": '" + data[i] [1] + "'" );
                
        out.append( "}" );
        
        return  ( post( "/_vti_bin/listdata.svc/" + list, out.toString() ) );
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
        return  ( putFile( path, name, new File(localname) ) );
    } 
    
    public String uploadDocument( String path, String name, String localname ) throws Exception
    {
        if  ( localname.startsWith( "http" ) )
            return  ( putFile( path, name, new URL(localname) ) );
        // locale Datei
        return  ( putFile( path, name, new File(localname) ) );    
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
    protected String post( String uri, String data ) throws IOException
    {
        HttpPost request = new HttpPost( remote + uri );
        request.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/json;odata=verbose;charset=utf-8" );        
        
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
    
    protected String putFile( String uri, String name, URL url ) throws IOException, URISyntaxException
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
        
        HttpPut request = new HttpPut( remote + "/" + uri + "/" + name );
        request.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/msword" );        
        
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
    
    protected String putFile( String uri, String name, File data ) throws IOException
    {
        // bei SharePoint 2010 funktioniert FileEntity nicht, deshalb zuerst in Memory lesen
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if  ( data != null )
        {
            byte buf[] = new byte[128 * 32768];
            InputStream inp = new FileInputStream( data );
            for ( int length; (length = inp.read( buf, 0, buf.length )) > 0; )
                out.write( buf, 0, length );
            out.close();
            inp.close();
        }
        
        HttpPut request = new HttpPut( remote + "/" + uri + "/" + name );
        request.addHeader( "Accept", "application/json;odata=verbose;charset=utf-8" );
        request.addHeader( "Content-Type", "application/msword" );    
        
        if  ( data != null )
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
