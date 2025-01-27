# Ecosio assessment

> Concept assessment application for Ecosio.

## Prerequisites

This project requires Java 21. [Java](https://www.java.com/en/download/manual.jsp) is really easy to install.
To make sure you have them available on your machine,
try running the following command.

```sh
$ java --version

openjdk 21.0.4 2024-07-16
```

## Table of contents

- [Ecosio assessment](#Ecosioassessment)
    - [Prerequisites](#prerequisites)
    - [Table of contents](#table-of-contents)
    - [Getting Started](#getting-started)
    - [Installation](#installation)
    - [Usage](#usage)
        - [Compiling the project](#compiling-the-project)

    - [Authors](#authors)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

## Installation

**BEFORE YOU INSTALL:** please read the [prerequisites](#prerequisites)

Start with cloning this repo on your local machine:

```sh
$ git clone git@github.com:tebanhdez/ecosio.git 
$ cd ecosio
```

## Usage

### Compiling the project

This example use maven for compilation:
```sh
$ mvn clean compile
```

### Running the app using maven
The app accepts three arguments:
1. domain: mandatory e.g. ecosio.com
2. depth: optional depth of recursion while following the website links
3. time out: optional general timeout in seconds to complete the overall executor.


```sh
$ mvn exec:java -Dexec.mainClass=com.ecosio.crawler.Main -Dexec.args="ecosio.com"
```

### Sample output

Using default settings:
```sh
$ mvn clean compile exec:java -Dexec.mainClass=com.ecosio.crawler.Main -Dexec.args="ecosio.com"
...
e-migration-auf-s/4hana-was-bedeutet-das-fur-edi, https://ecosio.com/de/webinars, https://ecosio.com/de/white-papers, https://ecosio.com/en, https://ecosio.com/es, https://ecosio.com/it
Jan 27, 2025 7:05:28 PM com.ecosio.crawler.WebCrawler crawl
INFO: Total collected links: 48
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.669 s
[INFO] Finished at: 2025-01-27T19:05:28+01:00
[INFO] ------------------------------------------------------------------------
```

Increasing depth:
```sh
$ mvn exec:java -Dexec.mainClass=com.ecosio.crawler.Main -Dexec.args="ecosio.com 2"
...
://ecosio.com/it/soluzioni/integrazione-completa-erp-edi, https://ecosio.com/it/soluzioni/routing-connettivita-reti-van, https://ecosio.com/it/soluzioni/web-edi
Jan 27, 2025 7:07:35 PM com.ecosio.crawler.WebCrawler crawl
INFO: Total collected links: 508
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.296 s
[INFO] Finished at: 2025-01-27T19:07:35+01:00
[INFO] ------------------------------------------------------------------------
```

## Authors

* **Esteban Hernández** - [tebanhdez]( https://github.com/tebanhdez)