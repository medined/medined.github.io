---
layout: post
title: python wtforms - handling dynamic fields
author: David Medinets
categories: python wtforms
year: 2018
---

I wanted to have a web form with a variable number of fields - one for each
day in a month. Since that number varies (i.e., Feb usually has 28, while Jan
Jan has 31), I wanted my form to also have a variable number of input fields.

Naturally, I created a hack. The form has a constant 31 fields, but when
displayed in the jinja2 template, only daysInMonth fields are used.

Here is the form definition:

```python
from flask_wtf import FlaskForm
from wtforms import FieldList, HiddenField, IntegerField, StringField, SubmitField, validators
from wtforms.validators import DataRequired, UUID

class MonthForm(FlaskForm):
    handle = HiddenField('handle', validators=[DataRequired(), UUID()])
    month = HiddenField('month', validators=[DataRequired()])
    year = HiddenField('year', validators=[DataRequired()])
    daysInMonth = HiddenField('daysInMonth', validators=[DataRequired()])
    for n in range(1, 32):
        locals()[''.join("morning"+str(n))] = StringField('', [ validators.Length(min=0, max=4)])
        locals()[''.join("evening"+str(n))] = StringField('', [ validators.Length(min=0, max=4)])
    submit = SubmitField('Save')
    export = SubmitField('Export')

    def getField(self, fieldName):
        for f in self:
            if f.name == fieldName:
                return f
        return None
```

Note that I use a range to avoid the tedious, error-prone duplication of
64 fields. The locals() method is terrific.

The getField() method is needed in order to find the right field to display
in the template.

```
{% raw %}
{% for n in range(1, daysInMonth+1) %}
  <tr>
    <td>{{n}}</td>
    <td>{{ form.getField('morning' + n|string)(size="5") }}</td>
    <td>{{ form.getField('evening' + n|string)(size="5") }}</td>
  </tr>
{% endfor %}
{% endraw %}
```

In a world in which python supports static class members, I'd use a hash to
cache the fields. This would avoid the for loop every time a field is needed.
On the other hand, the cache might be premature optimization. For my little, tiny
application with less than 40 fields running the for loop is not a problem.

Footnote: Use raw and endraw inside {% raw %}{% %}{% endraw %} tags to display
Jinja2 code inside GitHub Markdown.
