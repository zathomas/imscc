/*
    Copyright 2010 Zach A. Thomas <zach@aeroplanesoftware.com>
    
    Licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.
*/

def dest = System.getProperty('java.io.tmpdir') + UUID.randomUUID() + File.separator
def ant = new AntBuilder()
ant.unzip(  src:args[0],
            dest:dest,
            overwrite:"false" )

def manifestPath = "${dest}imsmanifest.xml"
println "Reading manifest from $manifestPath"
def manifest = new XmlSlurper().parse("${dest}imsmanifest.xml")

def titles = manifest.organizations.depthFirst().findAll { aNode ->
    aNode.name() == "title" 
    }

println "found ${titles.size()} items (not including items without titles)"
titles.each { println "item title: '${it.text()}'" }           

manifest.resources.resource.each { resourceNode ->
    switch (resourceNode.@type) {
        case "webcontent":
            println "webcontent with id: '${resourceNode.@identifier}'"
            break
        
        case "associatedcontent/imscc_xmlv1p0/learning-application-resource":
            println "associated content with id: '${resourceNode.@identifier}'"
            break
        
        case "imsqti_xmlv1p2/imscc_xmlv1p0/assessment":
            println "QTI assessment with id: '${resourceNode.@identifier}'"
            break
            
        case "imsdt_xmlv1p0":
            def discussionDescriptor = new XmlSlurper().parse(dest + resourceNode.file.@href).declareNamespace(dt: "http://www.imsglobal.org/xsd/imsdt_v1p0")
            
            println "discussion topic: '$discussionDescriptor.title', $discussionDescriptor.text"
            break
            
        case "imswl_xmlv1p0":
            def webLinkDescriptor = new XmlSlurper().parse(dest + resourceNode.file.@href).declareNamespace(wl: "http://www.imsglobal.org/xsd/imswl_v1p0")
            println """web link: <a href="${webLinkDescriptor.url.@href}">$webLinkDescriptor.title</a>"""
            break
            
        default:
            println "Not handling type: ${resourceNode.@type}"
        }
    }