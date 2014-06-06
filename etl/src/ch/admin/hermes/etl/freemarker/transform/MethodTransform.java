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

package ch.admin.hermes.etl.freemarker.transform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/*******************************************************************************
 * Transformiert mittels FreeMarker die Methoden Library in andere Formate.<p>
 * 
 * @author  mbern
 *******************************************************************************/

public class MethodTransform
{
    /**
     * Transformiert eine FreeMarker Template nach out.
     * @param objects wichtige Objecte welche mitels Namen in FreeMarker angesprochen werden können.
     * @param templateName Name FreeMarker Template
     * @param out OutputStream
     * @throws IOException Allgemeiner I/O Fehler
     * @throws TemplateException Fehler in FreeMarker Template
     */
    public void transform( HashMap<String, Object> objects, String templateName, OutputStream out ) throws IOException, TemplateException
    {
        // Configuration
        Configuration cfg = new Configuration();

        // Set Directory for templates
        cfg.setDirectoryForTemplateLoading( new File( "templates" ) );
        // load template
        Template template = cfg.getTemplate( templateName );

        // Also write output to console
        template.process( objects, new OutputStreamWriter( out ) );

        out.flush();
    }

}
