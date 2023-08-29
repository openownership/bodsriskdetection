# BODS Risk Detection

This is a proof-of-concept demonstrating the use of the [Beneficial Ownership Data Standard](https://www.openownership.org/en/topics/beneficial-ownership-data-standard/)
in an RDF format. The objective is to show how this data can be combined with 
other datasets to leverage its graph nature for a series of risk & compliance use cases.

## Use cases
These documents describe the use cases and classes of problems that this proof of concept is addressing. These have been
selected based on the types of problems that [BODS RDF](https://github.com/cosmin-marginean/kbods/tree/main/kbods-rdf)
was initially designed to solve, as well as their relevance in risk, compliance and corporate intelligence processes.

* [Public Contracts](docs/use-cases/public-contracts.md)
* [Risk - PEP & Sanctions](docs/use-cases/risk-pep-sanctions.md)
* [Risk - Registered Address](docs/use-cases/risk-registered-address.md)
* [Ownership & Control](docs/use-cases/ownership-and-control.md) 

## Demos

The demo videos are available in this Google Drive folder: https://drive.google.com/drive/folders/174NwS917uponbrJ1cSYyjySM8gMXAApv?usp=drive_link

* [Public contracts demo](https://drive.google.com/file/d/10VOnx_073Su4KwnDDIgRwIr0Lp4n05hz/view?usp=drive_link)
* [Risk - PEP/Sanctions demo](https://drive.google.com/file/d/1LRqfMuaQriMdqGA8T8jY3Ga6-iwDnHYw/view?usp=drive_link)
* [Risk - Registered address demo](https://drive.google.com/file/d/1FdeKT4ky5tGWdGxfc1qq28exNZTa4p6Y/view?usp=drive_link)
* [Ownership - UBO demo](https://drive.google.com/file/d/18lG9LSm6xAZzXAhBugPA1P-wIPR3ijsq/view?usp=drive_link)
* [Ownership - Ultimate parents demo](https://drive.google.com/file/d/1FPGz1fsTUnfsUxtb9hhAqBpjXJJhjcsI/view?usp=drive_link)
* [Ownership - Subsidiaries demo](https://drive.google.com/file/d/1hINxKzmPTui7MSXCzVpFfIgAS_TaHzqt/view?usp=drive_link)

## Notes on data modeling and system design
The records we ingest from various data sources come in a structure that is suitable for a
document store. However, traversing relationships between entities is a type of problem that is most suitable
for a graph (RDF) database, which in turn becomes the most important constraint for our design.

The problem then becomes about reflecting a document (JSON) as a set of RDF triples. While it's entirely possible
to persist a complete record using RDF, this is an exercise that has several drawbacks:
1. It requires a substantive amount data modeling for creating an RDF vocabulary from a JSON schema (which isn't always available).
2. It produces a high number of triples will affect the size and performance the RDF triple store.
3. Querying document-like structures via an RDF database is unsuitable and costly.

For these reasons, it made sense for the purposes of this PoC to use two separate storage
solutions for the two data retrieval patterns:
[Elasticsearch](https://www.elastic.co/elasticsearch/) and [GraphDB](https://www.ontotext.com/products/graphdb/).

The idea is then to create a data ingestion process which can handle both these aspects:
* Import the complete document (JSON) record in Elasticsearch
* Convert and import the elements of the record which are relevant to graph/network traversal: mainly unique references (IDs/URIs) and relationships.

Finally, the compromise then becomes about sacrificing query performance. The way the system works is by first identifying the
complex ownership structure and relationships using SPARQL (graph) queries. With the entities identified, we then perform
a bulk de-referencing of the complete record from the document store (Elasticsearch) in order to produce the relevant fields
required by the application.

It's important to note this is design choice that was suitable for our PoC and for testing the solutions to the types of problems and use
cases we're addressing. We don't claim for any of this to be suitable for a production-grade system.

## Running the application
### Prerequisites
 * Java (JDK) 11
 * Docker

### Starting the databases
This PoC uses [Graph DB](https://www.ontotext.com/products/graphdb/) and [Elasticsearch 8](https://www.elastic.co/elasticsearch/)
to import and process the [Open Ownership Register](https://register.openownership.org/download) as RDF statements and JSON documents, respectively.

These have been wired using the official Docker image for each of these databases. We have then ran a full data import and
produced two docker images with the complete dataset required for this PoC.

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
The application is built and run using Gradle, and it's wired to run locally using the above Docker images.
To start the application, run
```shell
./run.sh
```

### Datasets in this PoC
* [Open Ownership Register](https://register.openownership.org/download)
* [OpenSanctions](https://www.opensanctions.org/datasets/) default dataset
* [UK Public Contracts data](https://www.contractsfinder.service.gov.uk)

### Importing data in an empty database
TODO

