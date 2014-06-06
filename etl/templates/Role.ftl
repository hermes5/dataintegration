<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Strict//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>HERMES 5 Rollen</title>
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/theme.css.xhtml?ln=primefaces-aristo" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/style.css.xhtml?ln=css" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/messages.css.xhtml?ln=css" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/tablestyle.css.xhtml?ln=css" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/menustyle.css.xhtml?ln=css" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/dialogstyle.css.xhtml?ln=css" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/print.css.xhtml?ln=css" media="print" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/watermark/watermark.css.xhtml?ln=primefaces" />
<link type="text/css" rel="stylesheet" href="http://www.hermes.admin.ch/javax.faces.resource/primefaces.css.xhtml?ln=primefaces" />
</head>
<body>

<h1>Rollen</h1>

<h2>Inhaltsverzeichnis</h2>
<ul>
<#list roles as role>
	<li><a href="#${role.getName()}">${role.getPresentationName()}</a></li>
</#list>
</ul>

<#list roles as role>

	<h2 id="${role.getName()}">${role.getPresentationName()}</h2>
	<p>${role.getDescription()}</p>
	
	<h3>Verantwortung</h3>
	<p>${role.getResponsibility()}</p>
	<h3>Kompetenzen</h3>
	<p>${role.getAuthority()}</p>
	<h3>Fähigkeiten</h3>
	<p>${role.getSkills()}</p>

</#list>


</body> 
</html>