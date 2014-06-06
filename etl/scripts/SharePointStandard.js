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
 * Kopiert die Daten aus HERMES 5 Online nach SharePoint Standard
 * @param site SharePoint Site
 * @param user Username
 * @param passwd Password
 */
function main( site, user, passwd )
{
	// SharePoint Verbindung aufbauen
	var client = new SharePointRESTClient( site, user, passwd );
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Rollen nach SharePoint
  
	// Rollen List anlegen - ausgehenden vom Liste Kontakte
	client.createList( "Rollen", 105 );
	// Rollen einzeln durchlaufen
	for ( var i = roles.iterator(); i.hasNext(); )
	{
		var role = i.next();
		log( "Rolle: " + role.getId() + " " + role.getPresentationName() );
		
		// Array fuer SharePoint aufbauen, d.h. Attribute den SharePoint Feldern zuordnen 
		// der Layout Entspricht der Liste 105 Contacts
		var data = Array();
		var r = 0;
		data[r++] = new Array( "Title", role.getPresentationName() );
		data[r++] = new Array( "FullName", role.getPresentationName() );
		data[r++] = new Array( "JobTitle", role.getPresentationName() );
		data[r++] = new Array( "Comments", role.getDescription() );		
		
		// Rolle in SharePoint abstellen
		client.addData( "Rollen", data );
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Aufgaben nach SharePoint (Phasen und Module werden ignoriert)
    
	// Aufgaben verarbeiten
	client.createList( "Aufgaben", 107 );
	
	// Phasen
	for	( var ii = scenario.getPhase().iterator(); ii.hasNext() ; )
	{
		var phase = ii.next();
		// Module
		for	( var iii = phase.getModuleRef().iterator(); iii.hasNext() ; )
		{
			var modul = iii.next();
			// Tasks
			for	( var iiii = modul.getTaskRef().iterator(); iiii.hasNext() ; )
			{
				var taskRef = iiii.next();
				var task = library.getTaskById( taskRef.getId() );
				
				// Array fuer SharePoint aufbauen, d.h. Attribute den SharePoint Feldern zuordnen 
				// der Layout Entspricht der Liste 107 Tasks
				var data = Array();
				var r = 0;
				data[r++] = new Array( "Title", phase.getPresentationName().toUpperCase() + " - " + task.getPresentationName() );
				
				// Tasks in SharePoint abstellen
				log( phase.getPresentationName() + " - " + task.getPresentationName() );
				client.addData( "Aufgaben", data );
			}				
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Ergebnisse nach SharePoint
  
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
	            log( "Dokument: " + wp.getName() + " -> " + "Freigegebene%20Dokumente/" + template.getName() );
	            client.uploadDocument( "Freigegebene%20Dokumente/", template.getName(), template.getUrl() );
    		}
    	}
	}
}

