---
title: ""
layout: home
---

# Scala-Result

Rust style `Result` type for Scala

## Context

In the opinion of the author `Either` and `Try` are inferior to the semantic benefits of a dedicated `Result` type.
Unfortunately, prior discussions regarding the addition of a `Result` type to the standard library have not borne fruit. 
[Pre-SIP: Rust-like "Result" Proposal](https://contributors.scala-lang.org/t/pre-sip-proposal-of-introducing-a-rust-like-type-result/3497)
So we'll have to go a with an unofficial solution.

## Documentation

* [API Scala 2.13](https://jsbrucker.dev/scala-result/scala-2.13/api/dev/jsbrucker/result)
* [API Scala 2.12](https://jsbrucker.dev/scala-result/scala-2.12/api/dev/jsbrucker/result)

## Usage

### SBT
Add the following line to the `build.sbt` files:
```scala
libraryDependencies += "dev.jsbrucker" %% "result-core" % "1.0.0"
```

### Bazel (using `maven_install`)
Add `"dev.jsbrucker:result-core_2_13:1.0.0"` to the list of artifacts.

### Maven
```xml
<dependency>
  <groupId>dev.jsbrucker</groupId>
  <artifactId>result-core_2.13</artifactId>
  <version>1.0.0</version>
</dependency>
```

