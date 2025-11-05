# Makefile for AAGA-Projet
# Targets:
#  - package : build project (Maven package)
#  - test    : run unit tests
#  - run-newman : run the girvannewman on data/sample.edgelist
#  - run-sample : run the sampleon data/sample.edgelist
#  - clean   : clean build artifacts

.PHONY: all package test run-sample run-newman clean

all: package

package:
	mvn -DskipTests package

test:
	mvn test

run-sample:
	java -jar target/aaga-projet-0.1.0-SNAPSHOT.jar -i data/sample.edgelist -o results/sample -a bsa

run-newman:
	java -jar target/aaga-projet-0.1.0-SNAPSHOT.jar -i data/sample.edgelist -o results/newman

clean:
	mvn -q clean
	if [ -d results ]; then rm -rf results; fi

