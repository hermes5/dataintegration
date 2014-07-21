/**
 * JavaScript um die Daten in Alfresco als Dokumente
 * abzulegen. 
 * 
 * Der Code unterliegt der Public Domain und darf kopiert,
 * veraendert und weitergegeben werden.
 *
 * Contributors:
 * Marcel Bernet, Zurich - initial implementation
 */

importPackage( java.util );
importPackage( java.io );
importPackage( java.lang );
importPackage( Packages.ch.admin.hermes.etl.load.cmis );
importPackage( Packages.org.apache.chemistry.opencmis.commons );

/**
 * Kopiert die Daten aus HERMES 5 Online nach Alfresco unter Data Dictionary/Space Templates.<p>
 * Die Struktur ist dem SharePoint Prototyp von Zuehlke nachempfunden.<p>
 * 
 * @param site Alfresco (Version 5) Site
 * @param user Username
 * @param passwd Password
 */
function main( site, user, passwd )
{
	// Alfresco Verbindung aufbauen
	var client = new AlfrescoCMISClient( site, user, passwd );
	
	log( "Erstelle Layout" );
	
	// Die Pfad Angabe im Aufruf bestimmt den Ablageort in Alfresco
    var root 			= client.getNodeByPath( client.getPath() );
    if	( root == null )
    	throw new Exception( "Ungueltige Path Angabe " + site );
    
    var scenarioA 		= client.createFolder( root.getId(), scenario.getName() );
    var decisionA 		= client.createFolder( scenarioA.getId(), "Entscheidungsprozess" );
    var taskA 			= client.createFolder( scenarioA.getId(), "Aufgaben" );
    var workproductsA 	= client.createFolder( scenarioA.getId(), "Ergebnisse" );
    var documentsA 		= client.createFolder( scenarioA.getId(), "Projektablage" );
    var rolesA 			= client.createFolder( scenarioA.getId(), "Rollenzuteilung" );
    var templatesA 		= client.createFolder( scenarioA.getId(), "Dokumentenvorlagen" );
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Rollen nach Alfresco via CMIS als HTML Dokument
  
	for ( var i = roles.iterator(); i.hasNext(); )
	{
		var role = i.next();
		
		// Map fuer Alfresco aufbauen, d.h. Attribute den Alfresco Feldern zuordnen 
		var properties = new HashMap();
		// z.B. 
		//data.put( "cmis:name", role.getPresentationName() );
		// Alfresco Aspect: P:cm:titled Attribute
		properties.put( "cm:title", role.getPresentationName() );
		properties.put( "cm:description", role.getDescription() );
		
		// Inhalt des HTML Dokuments
		var content = client.getHTML( role.getPresentationName(), "<h1>" + role.getPresentationName() + "</h1>" +
				                      role.getDescription() + "<p>" + 
				                      "<h2>Verantwortung</h2>" + role.getResponsibility() + "<p>" +
				                      "<h2>Kompetenzen</h2>" +  role.getAuthority() + "<p>" + 
				                      "<h2>Fähigkeiten</h2>" +  role.getSkills() );
		
		// Rolle in Alfresco abstellen
		log( "Rolle: " + role.getId() + " " + role.getPresentationName() );
		client.createDocument( rolesA.getId(), role.getName() + ".html", properties, "text/html", content );
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Szenario nach Alfresco, d.h. Aufgaben und Ergebnisse (Phasen und Module werden ignoriert)
    
	// Laufnummer - wird an Namen angefuegt um doppelte Eintraege zu verhindern
	var count = 1;
	var wpCount = 1;
	for	( var ii = scenario.getPhase().iterator(); ii.hasNext() ; )
	{
		var phase = ii.next();
		// Sammeln der Ergebnisse pro Phase mit den zugehoerenden Aufgaben
		var wps = new HashMap();
		// Module
		for	( var iii = phase.getModuleRef().iterator(); iii.hasNext() ; )
		{
			var modul = iii.next();
			// Tasks
			for	( var iiii = modul.getTaskRef().iterator(); iiii.hasNext() ; )
			{
				var taskRef = iiii.next();
				var task = library.getTaskById( taskRef.getId() );
				
				// Hilfsdaten aufbereiten
				var role = library.getRoleById( task.getResponsibleRole().getRoleRef().getId() ).getPresentationName();
				// Ergebnisse sammeln
				var wp = "";
				for	( var iiiii = taskRef.getWorkproductRef().iterator(); iiiii.hasNext() ; )
				{
					var wpRef = iiiii.next();
					wp += library.getWorkproductById( wpRef.getId() ).getPresentationName() + "<p>";
				}
				// Map fuer Alfresco aufbauen, d.h. Attribute den Alfresco Feldern zuordnen 
				var properties = new HashMap();
				// Alfresco Aspect: P:cm:titled Attribute
				properties.put( "cm:title", phase.getPresentationName().toUpperCase() + " - " + task.getPresentationName() +
						                    " (" + role + ")" );
				// Grundidee entspricht dem Feld Description
				properties.put( "cm:description", task.getBasicIdea() );
				
				// Inhalt des HTML Dokuments
				var content = client.getHTML( phase.getPresentationName().toUpperCase() + " - " + task.getPresentationName(), 
						                      "<h1>" + phase.getPresentationName().toUpperCase() + " - " + task.getPresentationName() + "</h1>" +
						                      "<h2>Grundidee</h2>" + task.getBasicIdea() + "<p>" +
						                      "<h2>HERMES Spezifisch</h2>" +  task.getHermesSpecific() + "<p>" + 
						                      "<h2>Aktivitäten</h2>" +  task.getActivities() + "<p>" +
						                      "<h2>Verantwortliche Rolle</h2>" + role +
						                      "<h2>Ergebnisse</h2>" + wp );
				
				log( "Aufgabe: " + taskRef.getId() + " " + task.getPresentationName() );
				var rc = null;
				// Entscheide werden in separates Verzeichnis abgestellt
				if	( task.getPresentationName().startsWith("Entscheid") )
					rc = client.createDocument( decisionA.getId(), (count++).toString() + ".html", properties, "text/html", content );
				// alle anderen Tasks
				else
					rc = client.createDocument( taskA.getId(), (count++) + ".html", properties, "text/html", content );

				// holt die eindeutige Id des letzten geschriebenen Items, wird unten wieder beim Ergebnis verwendet, siehe AufgabeId
				var id = rc.getId();
				
				// Ergebnisse sammeln mit den dazugehoerenden Aufgaben
				for	( var iiiii = taskRef.getWorkproductRef().iterator(); iiiii.hasNext() ; )
				{
					var wpRef = iiiii.next();
					if	( ! (wps.containsKey( wpRef.getId() )) )
						wps.put( wpRef.getId(), new ArrayList() );
					var tasks = wps.get( wpRef.getId() );
					tasks.add ( task.getPresentationName() + "<p>" );
				}
			}				
		}
		// Ende der Phase - Ergebnisse anfuegen (einmal pro Phase!)
		for	( var iiiii = wps.entrySet().iterator(); iiiii.hasNext(); )
		{
			var w = iiiii.next();
			var wp = library.getWorkproductById( w.getKey() );
			
			// Map fuer Alfresco aufbauen, d.h. Attribute den Alfresco Feldern zuordnen 
			var properties = new HashMap();
			
			// Hilfsdaten aufbereiten
			var role = library.getRoleById( wp.getCollaborationRoles().getRoleRef().get(0).getId() ).getPresentationName();
			
			// Alfresco Aspect: P:cm:titled Attribute
			properties.put( "cm:title", phase.getPresentationName().toUpperCase() + " - " + wp.getPresentationName() +
					                    " (" + role + ")" );
			properties.put( "cm:description", wp.getDescription() );
			
			// Inhalt des HTML Dokuments
			var content = client.getHTML( phase.getPresentationName().toUpperCase() + " - " + wp.getPresentationName(), 
					                      "<h1>" + phase.getPresentationName().toUpperCase() + " - " + wp.getPresentationName() + "</h1>" +
					                      wp.getDescription() + "<p>" + 
					                      "<h2>Inhalt</h2>" + wp.getTopic() + "<p>" +
					                      "<h2>Verantwortliche Rolle</h2>" + role +
					                      "<h2>Aufgaben</h2>" + w.getValue().toString() );			
		
			// Aufgabe in Alfresco abstellen
			log( "Ergebnis: " + wp.getId() + " " + wp.getPresentationName() );
			client.createDocument( workproductsA.getId(), (wpCount++) + ".html", properties, "text/html", content );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Vorlagen nach Alfresco
  
	// Ergebnisse einzeln durchlaufen
	for ( var i = workproducts.iterator(); i.hasNext(); )
	{
		var wp = i.next();
		log( "Ergebnis: " + wp.getId() + " " + wp.getPresentationName() );
        // ein Dokument kann mehrere Vorlagen beinhalten
        for	( var ii = wp.getTemplate().iterator() ; ii.hasNext();  )
    	{
        	var template = ii.next();
        	// nur MS Office Dokumente von Interesse (Links ingorieren)
        	if	( template.getUrl().endsWith( ".docx") || template.getUrl().endsWith( ".xlsx") || template.getUrl().endsWith( ".pptx") )
    		{
    			// Map fuer Alfresco aufbauen, d.h. Attribute den Alfresco Feldern zuordnen 
    			var properties = new HashMap();
    			properties.put( "cm:title", template.getName() );
    			
    			try
    			{
		            client.uploadDocument( templatesA.getId(), template.getName(), properties, template.getUrl() );
		            log( "Dokument: " + wp.getName() + " -> " + "Dokumentenvorlagen/" + template.getName() );
    			}
    			// doppelte Eintraege ingorieren
    			catch ( e )
    			{}
    		}
    	}
	}
}

