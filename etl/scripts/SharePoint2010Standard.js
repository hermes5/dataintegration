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
 * Kopiert die Daten aus HERMES 5 Online nach SharePoint 2010! Standard
 * @param site SharePoint Site
 * @param user Username
 * @param passwd Password
 */
function main( site, user, passwd )
{
	// SharePoint Verbindung aufbauen
	var client = new SharePoint2010RESTClient( site, user, passwd );
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Rollen nach SharePoint - List mit Name "Rollen" muss vorhanden sein!
  
	// Rollen einzeln durchlaufen
	for ( var i = roles.iterator(); i.hasNext(); )
	{
		var role = i.next();
		log( "Rolle: " + role.getId() + " " + role.getPresentationName() );
		
		// Array fuer SharePoint aufbauen, d.h. Attribute den SharePoint Feldern zuordnen 
		// der Layout Entspricht der Liste 105 Contacts
		var data = Array();
		var r = 0;
		data[r++] = new Array( "Nachname", role.getPresentationName() );
		data[r++] = new Array( "Notizen", role.getDescription() );		
		
		// Rolle in SharePoint abstellen
		client.addData( "Rollen", data );
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Aufgaben nach SharePoint (Phasen und Module werden ignoriert)
	// List "Aufgaben" ist bei einer Teamportal Site bereits vorhanden
	
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
				data[r++] = new Array( "Titel", phase.getPresentationName().toUpperCase() + " - " + task.getPresentationName() );
				data[r++] = new Array( "Beschreibung", task.getPurpose() + "<p/>" + task.getHermesSpecific() );	
				
				// Tasks in SharePoint abstellen
				log( phase.getPresentationName() + " - " + task.getPresentationName() );
				client.addData( "Aufgaben", data );
			}				
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Ergebnisse nach SharePoint
	// Freigegebene%20Dokumente Ordner ist bei Teamportal Site bereits vorhanden.
  
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
	            log( "Dokument: " + wp.getName() + " -> " + "Freigegebene%20Dokumente" + template.getName() );
	            client.uploadDocument( "Freigegebene%20Dokumente", template.getName(), template.getUrl() );
    		}
    	}
	}
}

