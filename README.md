## Table of Contents
<!-- this is using the Sublime MarkdownTOC plugin to auto update it -->
<!-- MarkdownTOC autolink="true" bracket="round" depth="2" style="unordered" indent="  " autoanchor="false" -->

- [Summary](#summary)
- [Quick Start](#quick-start)
- [Usage](#usage)
- [Services](#services)
- [Configuration](#configuration)
- [Logging](#logging)
- [History](#history)

<!-- /MarkdownTOC -->

## Summary


## Quick Start

```groovy
class DemoController {
	def index = {
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

## Services


## Configuration

Here is an example config with explanations

```groovy
grails {
    jasper {



    }
}
```

## Logging



## History

  
  
  
  
  
  