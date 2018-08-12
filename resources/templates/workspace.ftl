
<#include "macros.ftl">

<!DOCTYPE html>
<html>
<head>
<title>${workspace} (workspace)</title>

<link rel="stylesheet" type="text/css" href="style.css">

</head>
<body>

<script>
function viewSmallTree(system) {
    document.getElementById(system + "-medium").style.display = "none";
    document.getElementById(system + "-large").style.display = "none";
    document.getElementById(system + "-small").style.display = "block";
}

function viewMediumTree(system) {
    document.getElementById(system + "-small").style.display = "none";
    document.getElementById(system + "-large").style.display = "none";
    document.getElementById(system + "-medium").style.display = "block";
}

function viewLargeTree(system) {
    document.getElementById(system + "-small").style.display = "none";
    document.getElementById(system + "-medium").style.display = "none";
    document.getElementById(system + "-large").style.display = "block";
}
</script>

<img src="../logo.png" alt="Polylith" style="width:200px;">

<h1>${workspace}</h1>

<h3>Libraries</h3>
<#list libraries as library>
<div class="library" title="${library.version}">${library.name}</div>
</#list>
<p class="clear"/>

<h3>Interfaces</h3>
<#list interfaces as interface>
<div class="interface" <@link e=interface type="interface"/>>${interface}</div>
</#list>
<p class="clear"/>

<h3>Components</h3>
<#list components as comp>
  <@component c=comp/>
</#list>
<p class="clear"/>

<h3>Bases</h3>
<#list bases as base>
<div class="base" <@link e=base.name type="base"/>>${base.name}</div>
</#list>
<p class="clear"/>

<h3>Environments</h3>
<div class="environments">
<#list environments as environment>
  <h4>${environment.name}:</h4>
  <#list environment.entities as entity>
    <#if entity.type = "base">
    <div class="base" <@link e=entity.name type="base"/>>${entity.name}</div>
    <#else>
      <@component c=entity/>
    </#if>
  </#list>
  <p class="clear"/>
</#list>
</div>

<h3>Systems</h3>
<div class="systems">
<#list systems as system>
  <#if system.entities?has_content>
  <h4 class="missing">${system.name}:</h4>
  <#else>
  <h4 class="top">${system.name}:</h4>
  </#if>

  <button onclick="viewSmallTree('${system.name}')">S</button>
  <button onclick="viewMediumTree('${system.name}')">M</button>
  <button onclick="viewLargeTree('${system.name}')">L</button>
  <p class="clear"/>

  <#list system.entities as entity>
    <#if entity.name = "&nbsp;">
    <@component c=entity title="The interface '${entity.name}' is referenced from '${system.name}' but a component that implements the '${entity.name}' interface also needs to be added to ${system.name}', otherwise it will not compile."/>
    <#else>
    <@component c=entity title="The component '${entity.name}' was added to '${system.name}' but has no references to it in the source code."/>
    </#if>
  </#list>
  <#if system.entities?has_content>
  <p class="clear"/>
  </#if>
  <@table name=system.name table=system.smalltable size="small"/>
  <@table name=system.name table=system.mediumtable size="medium"/>
  <@table name=system.name table=system.largetable size="large"/>
</#list>
</div>
</body>
</html>
