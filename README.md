[![CircleCI](https://img.shields.io/circleci/project/github/yakworks/grails-jasper-reports/master.svg?longCache=true&style=for-the-badge&logo=circleci)](https://circleci.com/gh/yakworks/grails-jasper-reports)
[![9ci](https://img.shields.io/badge/BUILT%20BY-9ci%20Inc-blue.svg?longCache=true&style=for-the-badge)](http://9ci.com)

see branch and versions 3.0.x - Works with Grails 3.2.x

Install for grails 3.3.x

```
compile "org.grails.plugins:jasper-reports:3.2.0"
```


## Table of Contents
<!-- this is using the Sublime MarkdownTOC plugin to auto update it -->
<!-- MarkdownTOC autolink="true" bracket="round" depth="3" style="unordered" indent="  " autoanchor="false" -->

- [Summary](#summary)
- [Quick Start](#quick-start)
- [Usage](#usage)
- [Services](#services)
- [Configuration](#configuration)
- [History](#history)

<!-- /MarkdownTOC -->

## Summary
Jasper reports plugin integrates jasper reports library with grails and makes it easy to generate and render
reports. Plugin also configures jasper spring mvc view resolver which makes it possible to render views as reports easily from controllers. 

## Quick Start

```groovy
class DemoController {
	def index() {
		render view: 'demo.jrxml', model: [name: 'Jeff Beck', instrument: 'Guitar']
	}
}
```
that looks like this:

```html
<html>
<body>
	Name: ${name} <br/>
	Instrument: ${instrument}<br/>
</body>
</html>
```

Use your browser to navigate to [http://localhost:8080/yourApp/demo]()

Done!

## Usage

Plugin configures a spring mvc view resolver which handles ```.jasper``` and ```.jrxml``` files. This makes it extremely easy
to generate reports. the ```.jrxl``` file can be put inside grails-app/views directory just like a gsp view and it will be rendered as jasper report as shown in quick start example above.
 
It is also possible to render reports programmatically using ```JasperService``` as shown in Services section below.

## Services
**JasperService**  
```JasperService``` provides methods to programmatically generate and render reports.

**Example**  

```groovy

class ReportController {

import yakworks.jasper.JasperService
import yakworks.jasper.spring.JasperReportDef
import yakworks.reports.ReportFormat


   def generate(String name) {
        JasperService jasperService
   
        List reportData = [] //prepare list of maps which will be feed to jasper report as data.
            
        JasperReportDef opts = new JasperReportDef(
                name: name, //the name of jrxml or compiled .jasper file
                fileFormat: ReportFormat.PDF,
                parameters: [param1:value1, param2:value2], //pass whatever parameter needed.
                reportData: reportData
        )
   
   	    ByteArrayOutputStream out = jasperService.generateReport(opts)
   		//do some thing with ByteArrayOutputStream, write to file or stream to browser etc.
   }

}


```

## Configuration
Configuring ```ViewResourceLocator``` to add extra directories as report files location.
Plugin configures an instance of ```ViewResourceLocator``` as spring bean with name ```jasperViewResourceLocator```
Which is used internally to locate jasper report template files. So every thing explained in [view tools plugin docs](https://yakworks.github.io/view-tools/)
Applies to jasperViewResourceLocator.

Here is an example of how to use an external directory to store jasper report template files.

grails-app/conf/spring/resources.groovy

```groovy
jasperViewResourceLocator(grails.plugin.viewtools.ViewResourceLocator) { bean ->
   
    searchPaths = [
        "file:/someLoc/my-templates/" //the directory which contains the jasper templates
    ] 

   searchBinaryPlugins = true

   if (!application.warDeployed) { // <- grails2
		grailsViewPaths = ["/grails-app/views"]
		webInfPrefix = ""
    }

}

```


## History

  
  
  
  
  
  
