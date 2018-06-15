# Reference implementation for Templates Engine API

This is the reference, declarative implementation for X(HT)ML Templates Engine [API](https://github.com/js-lib-com/api/tree/master/template).
First thing first: declarative part. This templates engine is indeed declarative. It does not embed 
programming language into (X)HTML code. And this - beside being much easier to learn and use - comes 
with important consequence: a complete separation of logic and data presentation. Declarative nature 
forces view to deal only with presentation, there is no chance for developer to mix-in business logic - see code samples.
```
<ul data-list="contacts">
	<li><span data-text="name" /> - <span data-text="phone" /></li>
</ul>
```

Below is a typical templates engine using embedded Groovy. For a user interface developer seasoned with 
HTML this code may look a little more difficult to grasp. But the real thing is concern separation: 
user interface developer need not to know contacts comes from an instance named `client` and that field 
name is `contact`. And because the language allows it, is possible to alter model object values from 
templates which is bad.   
```
<ul>
	#{list contacts:client.contacts, as:'contact'}
		<li>${contact.name} - ${contact.phone}</li>
	#{/list}
</ul>
```

### Temples engine overview
A template is an X(HT)ML file that contains templates operators. An operator is declared into and operates 
on an js.dom.Element and has an opcode and exactly one operand. The most common operator is setter which 
takes care to set element text content or attribute. A short example may clarify: 
```
// model object
class Person {
	String description = "person description";
	Picture picture = new Picture("images/person.png");
};
  
// template fragment
<p data-text="description"></p>
<img data-attr="src:picture;title:description;" />
```

Templates engine enacts *data-text* and *data-attr* operators that set paragraph text to `Person`
`description`, image source to `picture` and image tooltip, i.e. its title attribute, also to `description`.
```
<p>person description</p>
<img src="images/person.png" title="person description" />
```
 
Templates operators are declared using standard X(HT)ML attributes and uses next syntax:
```
operator := 'data' '-' opcode '=' '"' operand '"'
opcode := see <a href="#operators-list">operators list</a>
operand := propertyPath | expression
propertyPath := see <a href="#object-property-path">object property path</a> 
expression := evaluated by operator
```

One may notice operator opcode is prefixed with `data-`. This is indeed on purpose: HTML5 allows for 
element attributes extensions and extensions name start exactly with that prefix.

Since instruction syntax is a subset of supporting template syntax it can be kept into resulting document. This comes with a
big advantage: the same instructions can be used to extract model object from documents generated by templates processor.
This can be seen as an object oriented alternative to micro-formats. So templates processor can be used for both injection 
and extraction of the model object into/from documents.

When processing templates, the DOM tree is traversed in synchro with model object graph. DOM is a tree of elements and
model is a tree of values; model tree is mapped on elements tree. This does necessarily means those trees are congruent, elements
tree can and usually is more complex but its overall structure resemble model tree.    
```
                   e1 
                  /               
     m1          e   
    /           / \ 
  m0          e0   e
    \           \    
     m2          \  
                  e2 
```
 
Templates engine define a function TE: m -> e defined on model with values on elements, in other words model is domain whereas 
elements tree is codomain. Note that TE is not surjective, that is,  not for every element *e* in the codomain there
is an *m* in the domain such that TE(m) = e.
 
### Templates operators list
Templates engine algorithm is generic and operates on operator types, that is, groups of operators. This way adding new operators
does not require algorithm update. This operators list is displayed using that types hierarchy. 

| Operator      | Description                                             |
|---------------|---------------------------------------------------------|
| 1. CONDITIONAL| include or exclude DOM branches based on some condition |
| if            | if value is empty element branch is not included in resulting document |
| exclude       | exclude element and its descendants from resulting document; implementation may choose to hide or completely remove the branch |
| 2. ATTRIBUTE  | set specified attribute to value extracted from content |
| attr          | set attributes values; both attributes name and value are specified into expression operand |
| id            | convenient attribute setter for element ID |
| src           | convenient attribute setter for image source |
| href          | convenient attribute setter for hyper-references; element should be an anchor or a resource link |
| title         | convenient attribute setter for tooltips |
| value         | convenient attribute setter for element value; element should be an input or textarea |
| 3. CONTENT    | operates upon element content be it text content or generated children elements |
| object        | current element is an object with many properties and should have child elements |
| text          | set element text content |
| html          | set element inner HTML, useful for text formatted with HTML tags |
| numbering     | set element text content accordingly numbering format and item index |
| list          | current element is a list and should have a child element that is processed as list item |
| olist         | ordered variant of `list`, that is, list with numbering |
| map           | current element is a map and should have two child elements: first processed as map key and the second as value |
| omap          | ordered variant of `map`, that is, map with numbering |
| 4. FORMATTING | set format instance used to prepare content value before actual insertion |
| format        | set formatter for value and text content setters |

Operators precedence is controller by their types in next order: conditional, inline, attribute, content and scope operators.

### Object property path
Basically templates operators inject values from content into target document. In order to identify the right
piece of information content is regarded as a tree of values; every value can be reached traversing the three,
from node to node following a unique path.
```
 class Person {
     String name;
     Address address {
         Locality locality {
             String name;
         }
     }
 }
```  

In above pseudo-code person and locality names are values; locality name path is `.address.locality.name` whereas person
name is simple `.name`. Both exemplified paths starts with dot, meaning they are absolute path. Is also possible to use 
relative paths, but we need a reference named *scope object* or simply *scope*. So, if scope is Address, locality name path 
is `locality.name`; note missing dot from path start. For this reason all operators receive a scope object into parameters 
list. Also, template scanning starts with scope root, in our case Person instance; there is a scope operator used to change current scope. 
 
### Templates implementations
There are two templates engine implementations: serializer and injector. A serializer traverses DOM tree using 
depth-first algorithm and serialize every node to a given writer. If current node contains templates operators 
execute them. Operator execution act upon model object and result is also serialized to stream. 
```
 TEMPLATE -> DOM -> TP -> WRITER
                     ^
                     |
                  CONTENT
```

An injector templates engine also traverses source DOM using depth-first but model object values are injected directly into
source DOM, altering it.
```                  
              |<-----|       
              v      |  
 TEMPLATE -> DOM -> TP
                     ^
                     |
                  CONTENT   
```

Currently server side implementation is a serializer and client side is an injector for obvious rationales: server should send back
to client documents generated on the fly, serialized on Servlet writer whereas client side should update loaded DOM in order to update
user interface. But both support the same operators with exactly the same behavior so that documents generated on server side or on
client look exactly the same, of course if generated from the same template. 

### Experimental Solution

Uses namespace for operators prefix and o:load operator to load content from server.
```
<tr xmlns:o="http://js-lib.com/dom" o:load="/rest/contradiction">
	<td><img o:src="image" o:title="name" /></td>
		<td o:object="tasks.0" o:css-class="tasks.0:active"> 
			<p o:text="start" o:format="js.format.StandardDateTime"></p>
	. . .
```
