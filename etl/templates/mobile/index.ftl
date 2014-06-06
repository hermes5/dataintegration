<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Strict//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html manifest="cache.manifest">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">    <!-- stop zoom -->
    <meta name="author" content="Marcel Bernet, Zuerich">
    <title>HERMES 5</title>

	<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.css" />
	<script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
	<script src="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.js"></script>
</head>
<body>

<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE INDEX -->
<!--------------------------------------------------------------------------------------------------------->

    <div data-role="page" id="page-index" data-title="HERMES 5 - Menu">

		<div data-role="header" class="ui-header ui-bar-c">
		    <a href="#left-panel" data-icon="bars" data-iconpos="notext"
		       data-shadow="false" data-iconshadow="false">Menu</a>
		    <h1>HERMES 5</h1>
		    <img src="./img/hermes-logo.png" width="30" class="ui-btn-right"/>
		</div>

        <div data-role="content">
        
            <div class="ui-grid-a">
            
                <div class="ui-block-a">
                    <img src="./img/hermes-logo.png" width="200" />
                </div>
                
                <div class="ui-block-b">
                    <p>
                        <a href="#page-list-role" data-role="button">Rollen</a>
                        <a href="#page-list-task" data-role="button">Aufgaben</a>
                        <a href="#page-list-workproduct" data-role="button">Ergebnisse</a>
                    </p>
                </div>
                
            </div>
            
        </div>

		<#include "footer.ftl">
		<#include "menu.ftl">		

    </div>

<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE LIST Rollen -->
<!--------------------------------------------------------------------------------------------------------->

    <div data-role="page" id="page-list-role" data-title="HERMES 5 - Rollen">
    
		<#include "header.ftl">
		
        <div data-role="content">

            <p><strong>Rollen:</strong></p>

            <ul id="showLocationList" data-role="listview" data-inset="true">
            <#list roles as role>
                <li>
                    <a href="#page-role-${role.getId()}">
                    <img src="./img/hermes-role.png">
                    <h2>${role.getPresentationName()}</h2>
                    <p>${role.getDescription()}</p></a>
                </li>
			</#list>
            </ul>

        </div>	

		<#include "footer.ftl">
		<#include "menu.ftl">
			
	</div>
	
<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE LIST Aufgaben -->
<!--------------------------------------------------------------------------------------------------------->

    <div data-role="page" id="page-list-task" data-title="HERMES 5 - Aufgaben">
    
		<#include "header.ftl">
		
        <div data-role="content">

            <p><strong>Aufgaben:</strong></p>

            <ul id="showLocationList" data-role="listview" data-inset="true">
            
            <#list scenario.getPhase() as phase>
	            <#list phase.getModuleRef() as modul>
		            <#list modul.getTaskRef() as taskRef>
		                <li>
		                    <a href="#page-task-${taskRef.getId()}" data-val="0">
		                    <img src="./img/hermes-task.png">
		                    <h2>${phase.getPresentationName()} - ${library.getTaskById( taskRef.getId() ).getPresentationName()}</h2>
		                    <#if library.getTaskById( taskRef.getId() ).getDescription()??>
			                    <p>${library.getTaskById( taskRef.getId() ).getDescription()}</p>
		                    </#if>
		                    </a>
		                </li>
					</#list>
				</#list>
			</#list>
            </ul>

        </div>	

		<#include "footer.ftl">
		<#include "menu.ftl">
			
	</div>	
	
<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE LIST Ergebnisse -->
<!--------------------------------------------------------------------------------------------------------->

    <div data-role="page" id="page-list-workproduct" data-title="HERMES 5 - Ergebnisse">
    
		<#include "header.ftl">
		
        <div data-role="content">

            <p><strong>Ergebnisse:</strong></p>

            <ul id="showLocationList" data-role="listview" data-inset="true">
            <#list workproducts as wp>
                <li>
                    <a href="#page-wp-${wp.getId()}" data-val="0">
                    <img src="./img/hermes-workproduct.png">
                    <h2>${wp.getPresentationName()}</h2>
                    <#if wp.getDescription()??><p>${wp.getDescription()}</p></#if>
                    </a>
                </li>
			</#list>
            </ul>

        </div>	

		<#include "footer.ftl">
		<#include "menu.ftl">
			
	</div>	
	
<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE Rollen -->
<!--------------------------------------------------------------------------------------------------------->		
	
    <#list roles as role>	
	    <div data-role="page" id="page-role-${role.getId()}" data-title="HERMES 5 - ${role.getPresentationName()}">
	
			<#include "header.ftl">
				
	        <div data-role="content">
	
	            <p><strong>Rolle: ${role.getPresentationName()}</strong></p>
	            <#if role.getDescription()??>
		            <p>${role.getDescription()}</p>
		        </#if>
	
	            <div data-role="collapsible-set" data-theme="c" data-content-theme="d"> 
	
	                <div data-role="collapsible" data-collapsed="true">
	                    <h3>Verantwortung</h3>
	                    <#if role.getResponsibility()??><div>${role.getResponsibility()}</div></#if>
	                </div> 
	                
	                <div data-role="collapsible">
	                    <h3>Kompetenzen</h3>
	                </div> 
	                
	                <div data-role="collapsible">
	                    <h3>Fähigkeiten</h3>
	                </div> 
	                
	            </div>
			</div>

			<#include "footer.ftl">
			<#include "menu.ftl">			
		</div>
	</#list>
	
<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE Aufgaben -->
<!--------------------------------------------------------------------------------------------------------->		
	
    <#list tasks as task>	
	    <div data-role="page" id="page-task-${task.getId()}" data-title="HERMES 5 - ${task.getPresentationName()}">
	
			<#include "header.ftl">
				
	        <div data-role="content">
	
	            <p><strong>Aufgabe: ${task.getPresentationName()}</strong></p>
	            <#if task.getDescription()??><p>${task.getDescription()}</p></#if>
	
	            <div data-role="collapsible-set" data-theme="c" data-content-theme="d"> 
	
	                <div data-role="collapsible" data-collapsed="true">
	                    <h3>Grundidee</h3>
	                    <#if task.getPurpose()??><div>${task.getPurpose()}</div></#if>
	                </div> 
	                
	                <div data-role="collapsible">
	                    <h3>HERMES spezfisch</h3>
	                    <#if task.getHermesSpecific()??><div>${task.getHermesSpecific()}</div></#if>
	                </div> 
	                
	                <div data-role="collapsible">
	                    <h3>Aktivitäten</h3>
	                    <#if task.getActivities()??><div>${task.getActivities()}</div></#if>
	                </div> 
	                
	                <div data-role="collapsible">
	                    <h3>Verantwortlich</h3>
	                    <a href="#page-role-${task.getResponsibleRole().getRoleRef().getId()}" data-val="0">
	                    <div>${library.getRoleById( task.getResponsibleRole().getRoleRef().getId() ).getPresentationName()}</div>
	                    </a>
	                </div> 	                
	
	            </div>
			</div>

			<#include "footer.ftl">
			<#include "menu.ftl">			
		</div>
	</#list>
	
<!--------------------------------------------------------------------------------------------------------->
<!-- PAGE Ergebnisse -->
<!--------------------------------------------------------------------------------------------------------->		
	
    <#list workproducts as wp>	
	    <div data-role="page" id="page-wp-${wp.getId()}" data-title="HERMES 5 - ${wp.getPresentationName()}">
	
			<#include "header.ftl">
				
	        <div data-role="content">
	
	            <p><strong>Ergebnis: ${wp.getPresentationName()}</strong></p>
	            <#if wp.getDescription()??><p>${wp.getDescription()}</p></#if>
	
	            <div data-role="collapsible-set" data-theme="c" data-content-theme="d"> 
	
	                <div data-role="collapsible" data-collapsed="true">
	                    <h3>Inhalt</h3>
	                    <#if wp.getTopic()??><div>${wp.getTopic()}</div></#if>
	                </div> 
	                
	                <div data-role="collapsible">
	                    <h3>Beziehungen</h3>
	                </div> 	                
	
	            </div>
			</div>

		<#include "footer.ftl">
		<#include "menu.ftl">			
		</div>
	</#list>		
	
</body>
</html>