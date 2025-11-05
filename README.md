## AAGA-Projet — Girvan–Newman (plain Java)

This repository contains a plain-Java implementation of the Girvan–Newman community detection algorithm, and an optimised version which is betweeness sampling algorithme, a modularity calculator, and a small CLI to run experiments. The project intentionally avoids runtime frameworks — it's pure Java (target: Java 17) with Maven used only for build and tests.

What you'll find
- Source: src/main/java/com/thenuber/aaga
- Tests: src/test/java
- Example dataset: data/sample.edgelist

Quick build
- With Maven (recommended):

	mvn -DskipTests package

- Or using the provided Makefile (if you have make installed):

	make package

Run the jar
- Run Girvan–Newman on the sample dataset and write results to the results/ folder:

	java -jar target\\aaga-projet-0.1.0-SNAPSHOT.jar -i data\\sample.edgelist -o results\\sample

- Run the (experimental) betweenness-sampling approximation algorithm:

	java -jar target\\aaga-projet-0.1.0-SNAPSHOT.jar -i data\\sample.edgelist -o results\\sample -a bsa

Makefile targets
- package   — build the project (Maven package)
- test      — run unit tests (mvn test)
- run-sample — run the jar on data/sample.edgelist and write results to results/sample
- run-newman   — example target that runs the jar and writes results to results/big
- clean     — maven clean and remove results/




