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

package ch.admin.hermes.etl.model.business;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPathExpressionException;

import ch.admin.hermes.model.schema.ModelType;
import ch.admin.hermes.model.schema.Module;
import ch.admin.hermes.model.schema.Role;
import ch.admin.hermes.model.schema.Task;
import ch.admin.hermes.model.schema.Workproduct;

/*******************************************************************************
 * Extrahieren und Aufbereiten der XML Datei von Zuehlke.<p>
 * 
 * @author  mbern
 *******************************************************************************/

public class ModelExtract
{
    /** Model */
    private ModelType model;

    /**
     * List die Methoden Library und extrahiert die einzelnen Types nach MethodLibraryRoot
     * @param filename
     * @return aufbereitete Methoden Library
     * @throws FileNotFoundException
     * @throws JAXBException
     * @throws XPathExpressionException
     * @throws MalformedURLException 
     */
    public ModelType extract( String filename ) throws FileNotFoundException, JAXBException, XPathExpressionException, MalformedURLException
    {
        // 1. XML to Java - Methoden Library
        JAXBContext context = JAXBContext.newInstance( ModelType.class );
        Unmarshaller um = context.createUnmarshaller();
        if  ( filename.startsWith( "http" ) )
            model =  (ModelType) um.unmarshal( new URL( filename ) );
        else
            model =  (ModelType) um.unmarshal( new FileReader( filename ) );
        
        return  ( model );
    }
    
    /**
     * Liefert alle wichtigen Objecte um diese dann in FreeMarker oder JavaScript verfuegbar zu machen.
     * @return Map mit allen wichtigen Objecten.
     */
    public HashMap<String, Object> getObjects()
    {
        // Aufbereiten fuer Transform
        HashMap<String, Object> objects = new HashMap<String, Object>();
        objects.put( "library", this );
        objects.put( "roles", model.getRoles().getRole() );
        objects.put( "tasks", model.getTasks().getTask() );
        objects.put( "workproducts", model.getWorkproducts().getWorkproduct() );    
        objects.put( "scenario", model.getScenario() );
        return  ( objects );
    }
    
    /**
     * Liefert das Model laut id
     * @param id Id 
     * @return Element oder null wenn nicht gefunden
     */
    public Module getModuleById( String id )
    {
        for ( Module module : model.getModules().getModule() )
            if  ( module.getId().compareTo( id ) == 0 )
                return  ( module );
        return  ( null );
    } 
    
    /**
     * Liefert die Rolle laut id
     * @param id Id 
     * @return Element oder null wenn nicht gefunden
     */
    public Role getRoleById( String id )
    {
        for ( Role role : model.getRoles().getRole() )
            if  ( role.getId().compareTo( id ) == 0 )
                return  ( role );
        return  ( null );
    }
    
    /**
     * Liefert die Aufgabe laut id
     * @param id Id 
     * @return Element oder null wenn nicht gefunden
     */
    public Task getTaskById( String id )
    {
        for ( Task task : model.getTasks().getTask() )
            if  ( task.getId().compareTo( id ) == 0 )
                return  ( task );
        return  ( null );
    } 
    
    /**
     * Liefert das Ergebnis laut id
     * @param id Id 
     * @return Element oder null wenn nicht gefunden
     */
    public Workproduct getWorkproductById( String id )
    {
        for ( Workproduct wp : model.getWorkproducts().getWorkproduct() )
            if  ( wp.getId().compareTo( id ) == 0 )
                return  ( wp );
        return  ( null );
    }    
}
