HBT Template Engine

[![Build Status](https://travis-ci.org/HBTGmbH/hte.svg?branch=master)](https://travis-ci.org/HBTGmbH/hte) [![Maven Central](https://img.shields.io/maven-central/v/de.hbt.hte/hte.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22de.hbt.hte%22%20AND%20a:%22hte%22&core=gav) [![Maven Snapshot](https://img.shields.io/nexus/s/https/oss.sonatype.org/de.hbt.hte/hte.svg)](https://oss.sonatype.org/content/repositories/snapshots/de/hbt/hte/hte/)

## What

Modern high-performance Java template engine for server-side rendering. The template syntax is very close to FreeMarker's FTL language, which allows for easy drop-in-replacement.

## Why

To get the best possible template instantiation performance, achieving around 10 to 40 times faster instantiations compared to FreeMarker.

## How

Templates will be directly compiled to JVM Bytecode and loaded as classes implementing the `de.hbt.hte.rt.CompiledTemplate` interface. This differs from FreeMarker's approach of parsing the text into an AST object representation and traversing/interpreting the AST on every template instantiation.

## Use

HTE requires at least a Java 7 JRE and fully runs on Java 7, 8, 9, 10, 11 and 12-EA JVMs.

Template:

```HTML
<!-- Products -->
<h2>${headline}</h2>
<ul>
  <#list products as product>
  <li>
    <div class="product-name">
      ${product.name}
    </div>
    <div class="product-price">
      ${product.price}
    </div>
  </li>
  </#list>
</ul>
```

Java:

```Java
public @lombok.Data class Product {
  private String name;
  private BigDecimal price;
}

public @lombok.Data class TemplateContext {
  private List<Product> products;
  private String headline;
}

/* One-time parse and instantiate of a template */
Reader r = ...obtain Reader for template...;
Environment env = new SimpleEnvironment();
CompiledTemplate ct = TemplateEngine.instantiateTemplate(r, env);

/* Re-use the CompiledTemplate instance as often as possible */
Writer w = ...Writer to receive the final output...;
TemplateContext tc = ...;
ct.write(w, tc);
```
