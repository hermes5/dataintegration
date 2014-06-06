/**
 * JavaScript um die Daten in den SharePoint Prototype
 * von Zuehlke abzulegen. 
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
importPackage( Packages.ch.admin.hermes.etl.load );

/**
 * Kopiert die Daten aus HERMES 5 Online nach SharePoint - Variante Zuehlke
 * @param site SharePoint Site
 * @param user Username
 * @param passwd Password
 */
function main( site, user, passwd )
{
	// SharePoint Verbindung aufbauen
	var client = new SharePointRESTClient( site, user, passwd );
	
	// zuerst alle bestehenden Items loeschen
	log( "Loesche Rollen" );
    client.deleteAllItems( "Rollenzuteilung" );
    // Aufgaben zweimal aufrufen, weil jeweils nur ca. 90 Elemente mitkommen 
	log( "Loesche Aufgaben" );
    client.deleteAllItems( "Aufgaben" );
    client.deleteAllItems( "Aufgaben" );
    // Ergebnisse zweimal aufrufen, weil jeweils nur ca. 90 Elemente mitkommen 
	log( "Loesche Ergebnisse" );
    client.deleteAllItems( "Ergebnisse" );
    client.deleteAllItems( "Ergebnisse" );
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Rollen nach SharePoint
  
	for ( var i = roles.iterator(); i.hasNext(); )
	{
		var role = i.next();
		
		// Array fuer SharePoint aufbauen, d.h. Attribute den SharePoint Feldern zuordnen 
		// der Layout Entspricht der Liste 105 Contacts
		var data = Array();
		var r = 0;
		data[r++] = new Array( "Title", role.getPresentationName() + "()" );
		data[r++] = new Array( "Rollen", role.getPresentationName() );
		
		// Rolle in SharePoint abstellen
		log( "Rolle: " + role.getId() + " " + role.getPresentationName() );
		client.addData( "Rollenzuteilung", data );
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Szenario nach SharePoint, d.h. Aufgaben und Ergebnisse (Phasen und Module werden ignoriert)
    
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
				var data = Array();
				var r = 0;
				data[r++] = new Array( "Title", task.getPresentationName() );
				data[r++] = new Array( "Aufgabenbeschreibung", task.getPurpose() + "<p/>" + task.getHermesSpecific() );	
				data[r++] = new Array( "Priority", "(2) Normal" );	
				data[r++] = new Array( "Status", "Nicht begonnen" );	
				data[r++] = new Array( "Phase", phase.getPresentationName().toUpperCase() );
				data[r++] = new Array( "Rollen", library.getRoleById( task.getResponsibleRole().getRoleRef().getId() ).getPresentationName() );
				
				// Aufgabe in SharePoint abstellen
				log( "Aufgabe: " + task.getId() + " " + task.getPresentationName() );
				var rc = client.addData( "Aufgaben", data );
				// holt die eindeutige Id des letzten geschriebenen Items, wird unten wieder beim Ergebnis verwendet, siehe AufgabeId
				var id = client.getJSObject( "function getOId() { return ( rc.d.Id ); }", "getOId", rc );
				
				// Ergebnisse sammeln mit den dazugehoerenden Aufgaben
				for	( var iiiii = taskRef.getWorkproductRef().iterator(); iiiii.hasNext() ; )
				{
					var wpRef = iiiii.next();
					if	( ! (wps.containsKey( wpRef.getId() )) )
						wps.put( wpRef.getId(), new ArrayList() );
					var tasks = wps.get( wpRef.getId() );
					tasks.add ( new Integer( id.intValue() ) );
				}
			}				
		}
		// Ende der Phase - Ergebnisse anfuegen (einmal pro Phase!)
		for	( var iiiii = wps.entrySet().iterator(); iiiii.hasNext(); )
		{
			var w = iiiii.next();
			var wp = library.getWorkproductById( w.getKey() );
			var data = Array();
			var r = 0;
			data[r++] = new Array( "Title", wp.getPresentationName() );
			data[r++] = new Array( "Beschreibung", wp.getDescription() );						
			//data[r++] = new Array( "Priority", "(2) Normal" );	
			data[r++] = new Array( "Status", "Nicht begonnen" );	
			data[r++] = new Array( "Phase", phase.getPresentationName().toUpperCase() );	
			data[r++] = new Array( "Rolle", library.getRoleById( wp.getCollaborationRoles().getRoleRef().get(0).getId() ).getPresentationName() );
			data[r++] = new Array( "AufgabeId", "{ 'results': " + w.getValue().toString() + " }" );
			
			// Aufgabe in SharePoint abstellen
			log( "Ergebnis: " + wp.getId() + " " + wp.getPresentationName() );
			client.addData( "Ergebnisse", data );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Vorlagen nach SharePoint
  
	// Ergebnisse einzeln durchlaufen
	for ( var i = workproducts.iterator(); i.hasNext(); )
	{
		var wp = i.next();
		log( "Ergebnis: " + wp.getId() + " " + wp.getPresentationName() );
        // ein Dokument kann mehrere Vorlagen beinhalten
        for	( var ii = wp.getTemplate().iterator() ; ii.hasNext();  )
    	{
        	var template = ii.next();
        	// nur Word Dokumente von Interesse
        	if	( template.getUrl().endsWith( ".docx") )
    		{
	            log( "Dokument: " + wp.getName() + " -> " + "Dokumentenvorlagen/" + template.getName() );
	            client.uploadDocument( "Dokumentenvorlagen/", template.getName(), template.getUrl() );
    		}
    	}
	}
}

