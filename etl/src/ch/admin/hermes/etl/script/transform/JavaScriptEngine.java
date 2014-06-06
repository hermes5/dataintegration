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

package ch.admin.hermes.etl.script.transform;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/*******************************************************************************
 * Script Engine.<p>
 * 
 * @version $Revision: 1.1 $ $Date: 2013/08/12 11:52:31 $
 * @author  mbern
 *******************************************************************************/

public class JavaScriptEngine
{
    private ScriptEngineManager factory;
    // create a JavaScript engine
    private ScriptEngine engine;
    
    /**
     * 
     */
    public JavaScriptEngine()
    {
        factory = new ScriptEngineManager();
        // create a JavaScript engine
        engine = factory.getEngineByName("JavaScript");
    }
    
    /**
     * Setzt die Objecte laut Key mit Value
     * @param objects Liste von Objecten
     */
    public void setObjects( HashMap<String, Object> objects )
    {
        for ( Map.Entry<String, Object> entry : objects.entrySet() )
            engine.put( entry.getKey(), entry.getValue() );
    }
    
    /**
     * Traegt ein einzelnes Object in der Engine ein.
     * @param name Key
     * @param obj Object
     */
    public void put( String name, Object obj )
    {
        engine.put( name, obj );
    }
    
    /**
     * Liefert ein definiertes Object laut Name
     * @param name Name Object
     * @return Object
     */
    public Object get( String name )
    {
        return  ( engine.get( name ));
    }
    
    /**
     * evaluate JavaScript code from String
     * @param script
     * @return
     * @throws ScriptException 
     */
    public void eval( String script ) throws ScriptException
    {
        engine.eval( script );
    }
    
    public void eval( Reader reader ) throws ScriptException
    {
        engine.eval( reader );
    }

    /**
     * Kurzform um ein Script zu laden und eine Methode mit Argumente aufzurufen.
     * @param script Script
     * @param name Name Funktion
     * @param args Argumente
     * @return
     * @throws ScriptException
     * @throws NoSuchMethodException
     */
    public Object call( Reader script, String name, Object... args ) throws ScriptException, NoSuchMethodException
    {
        engine.eval( script );
        Invocable inv = (Invocable) engine;
        return  ( inv.invokeFunction( name, args ) );
    }
}
