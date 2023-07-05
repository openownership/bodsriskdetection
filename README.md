# BODS Risk Detection

This is a proof-of-concept demonstrating the use of the [Beneficial Ownership Data Standard](https://www.openownership.org/en/topics/beneficial-ownership-data-standard/)
in an RDF format. The objective is to show how this data can be combined with 
other datasets to leverage its graph nature for a series of risk & compliance use cases.

## Running the application
### Prerequisites
 * Java (JDK) 11
 * Docker

### Starting the databases
This PoC uses [Graph DB](https://www.ontotext.com/products/graphdb/) and [Elasticsearch 8](https://www.elastic.co/elasticsearch/)
to import and process the [Open Ownership register](https://register.openownership.org/download) as RDF statements and JSON documents, respectively.

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
* [Open Ownership register](https://register.openownership.org/download)
* [Open Sanctions](https://www.opensanctions.org/datasets/) default dataset
* [UK Public Contracts data](https://www.contractsfinder.service.gov.uk)

### Importing data in an empty database
TODO
