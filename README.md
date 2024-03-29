# BODS Risk Detection

This is a proof-of-concept demonstrating the use of the [Beneficial Ownership Data Standard](https://www.openownership.org/en/topics/beneficial-ownership-data-standard/)
in an RDF format. The objective is to show how this data can be combined with 
other datasets to leverage its graph nature for a series of risk & compliance use cases.

## Use cases
These documents describe the use cases and classes of problems that this proof of concept is addressing. These have been
selected based on the types of problems that [BODS RDF](#bods-rdf) was initially designed to solve, as well as their relevance in risk,
compliance and corporate intelligence processes.

* [Public Contracts](docs/use-cases/public-contracts.md)
* [Risk - PEP & Sanctions](docs/use-cases/risk-pep-sanctions.md)
* [Risk - Registered Address](docs/use-cases/risk-registered-address.md)
* [Ownership & Control](docs/use-cases/ownership-and-control.md) 

## BODS RDF
BODS RDF is an extension of the BODS standard leveraging the inherent graph nature of this control & ownership data model.
It proposes a [vocabulary](https://github.com/cosmin-marginean/kbods/tree/main/kbods-rdf/src/main/resources/vocabulary)
that can be used to process and query [Open Ownership Register](https://register.openownership.org/download) data as RDF.

* [BODS RDF Proposal](https://docs.google.com/document/d/1vej-UkK7QtmfKrmU6aD15vceIzJDsCv1jbHCJWgn9hs/edit)
* [BODS RDF Background](https://world.hey.com/cos/an-rdf-vocabulary-for-beneficial-ownership-data-7a762fe1)
* [Tech Showcase](https://github.com/cosmin-marginean/bods-rdf/blob/main/docs/OO-TechShowcase-May2022.pdf)

The [BODS RDF project page](https://github.com/cosmin-marginean/kbods/tree/main/kbods-rdf) contains more information
about the vocabulary and the tools available to convert BODS data from a JSON format to RDF.

<img width="600" src="https://user-images.githubusercontent.com/2995576/216779559-64e9e754-efdb-44bd-8b9a-a1f87c643332.png">

## Demos

The demo videos are available in this Google Drive folder: https://drive.google.com/drive/folders/174NwS917uponbrJ1cSYyjySM8gMXAApv?usp=drive_link

* [Public contracts demo](https://drive.google.com/file/d/10VOnx_073Su4KwnDDIgRwIr0Lp4n05hz/view?usp=drive_link)
* [Risk - PEP/Sanctions demo](https://drive.google.com/file/d/1LRqfMuaQriMdqGA8T8jY3Ga6-iwDnHYw/view?usp=drive_link)
* [Risk - Registered address demo](https://drive.google.com/file/d/1FdeKT4ky5tGWdGxfc1qq28exNZTa4p6Y/view?usp=drive_link)
* [Ownership - UBO demo](https://drive.google.com/file/d/18lG9LSm6xAZzXAhBugPA1P-wIPR3ijsq/view?usp=drive_link)
* [Ownership - Ultimate parents demo](https://drive.google.com/file/d/1FPGz1fsTUnfsUxtb9hhAqBpjXJJhjcsI/view?usp=drive_link)
* [Ownership - Subsidiaries demo](https://drive.google.com/file/d/1hINxKzmPTui7MSXCzVpFfIgAS_TaHzqt/view?usp=drive_link)

## Other documents
 * [Discovery queries](data/discovery)

## What this PoC is not
This is not a robust, production-scale design or a reference implementation for these use cases. It is not designed
to be integrated as a library nor to be deployed as a scalable application. It aims to prove a concept and showcase the
thinking behind the solutions.

While certain elements have been designed with modularity and re-use in mind, many components have a very basic/naive implementation.

## Notes on data modeling and system design
The records we ingest from various data sources come in a structure that is suitable for a
document store. However, traversing relationships between entities is a type of problem that is most suitable
for a graph (RDF) database, which in turn becomes the most important constraint for our design.

The problem is then about reflecting a document (JSON) as a set of RDF triples. While it's entirely possible
to persist a complete record using RDF, this is an exercise that has several drawbacks:
1. It requires a substantive amount data modeling for creating an RDF vocabulary from a JSON schema (which isn't always available).
2. It produces a high number of triples which will affect the size and performance the RDF triple store.
3. Querying document-like structures via an RDF database is unsuitable and costly.

For these reasons, it made sense for the purposes of this PoC to use two separate storage
solutions for the two data retrieval patterns:
[Elasticsearch](https://www.elastic.co/elasticsearch/) and [GraphDB](https://www.ontotext.com/products/graphdb/).

With this approach, we've created a data ingestion process which can handle both these aspects:
* Import the complete document (JSON) record in Elasticsearch, so they're available via search/filters/get-by-id.
* Convert and import only the elements of the JSON (or CSV) record which are relevant to graph/network traversal: mainly unique references (IDs/URIs) and relationships.

Finally, the compromise was about sacrificing query performance in favour of simplicity.
The way the system works is by first identifying the
complex ownership structure and relationships using SPARQL (graph) queries.

With URIs/IDs of the entities in the SPARQL result set we then perform
a bulk de-referencing of the complete records from the document store (Elasticsearch) in order to produce the relevant fields
required by the application (name, address, DOB, etc.)

It's important to note this is a design choice that was suitable for our PoC and for testing the solution against the types
of problems we're showcasing. We don't claim for any of this to be suitable for a production-grade system without further analysis.

## Running the application
### Prerequisites
 * JDK 11
 * Docker

### Starting the databases
This PoC uses [Graph DB](https://www.ontotext.com/products/graphdb/) and [Elasticsearch 8](https://www.elastic.co/elasticsearch/)
to import and process the relevant sources as RDF statements and JSON documents.

These have been wired using the official Docker image for each of them. We have also published Docker images
containing the fully ingested datasets via Docker Hub.

A script in `devops/docker` is used to download the full docker images and startup the containers:
```shell
cd devops/docker
./docker-run-databases.sh
```
Please note that this process can take several minutes. The full Docker images have a total size of about 15GB.
Once this process completes, you should get the following message:
```shell
[+] Running 3/3
 ✔ Network bods-risk_default          Created 0.6s
 ✔ Container bods-risk-elasticsearch  Started 3.9s
 ✔ Container bods-risk-graphdb        Started 3.9s
```
 
### Starting the application
The application is built and run using Gradle, and it's configured by default to use the above Docker images.
To start the application, run the following from the project directory:
```shell
./run.sh
```

**Please note** The first time the application is run it can take several minutes to start.
Upon successful startup the application will print a message similar to this:
```shell
20:31:20.652 [main] INFO  io.micronaut.runtime.Micronaut - Startup completed in 314870ms. Server Running: http://localhost:8080
```

Once the application is started it will be available at http://localhost:8080/.

## Datasets in this PoC
* [Open Ownership Register](https://register.openownership.org/download)
* [OpenSanctions](https://www.opensanctions.org/datasets/) default dataset
* [UK Public Contracts data](https://www.contractsfinder.service.gov.uk)
* [ICIJ Offshore Leaks](https://offshoreleaks.icij.org/)

## Importing data in an empty database
Please note that the import process takes several hours and it shouldn't be required
unless you are also performing code changes that affect how data is ingested and processed.

Firstly wipe the Docker images:
```shell
cd devops/docker
./docker-wipe.sh
```

From the root directory simply start the application, which will trigger a data import
when the database is empty:
```shell
./run.sh
```



