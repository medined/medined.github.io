---
layout: post
title: Simple Template Example for Go
author: David Medinets
categories: golang
year: 2018
theme: golang
---

The examples that I have seen online about how to use Go template did not
mention that parseFiles method required the full path to the template file while
the ExecuteTemplate method needs just the template name. Below I demonstrate
this point.


First let me show you the `$GOPATH/src/templateExample/templates/index.template`
file to define the template.

```
{% raw %}
Hello {{.UserName}}!
{% endraw %}
```

And here is the Go code. I placed it in a file called
`$GOPATH/src/templateExample/examples/parse-template-file.go`

```
package main

import (
	"html/template"
	"os"
)

type Person struct {
	UserName string
}

func main() {
	p := &Person{UserName: "David"}
	templatePath := "../templates/"
	templateName := "index.template"
	tmpl, err := template.ParseFiles(templatePath + "/" + templateName)
	if err != nil {
		panic(err)
	}
	err = tmpl.ExecuteTemplate(os.Stdout, templateName, p)
	if err != nil {
		panic(err)
	}
}
```

The Go code is executed like this:

```
cd $GOPATH/src/templateExample/examples
go run parse-template-file.go
```
