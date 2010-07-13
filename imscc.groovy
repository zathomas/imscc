def dest = System.getProperty('java.io.tmpdir') + UUID.randomUUID() + File.separator
def ant = new AntBuilder()
ant.unzip(  src:args[0],
            dest:dest,
            overwrite:"false" )

def manifestPath = "${dest + File.separator}imsmanifest.xml"
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