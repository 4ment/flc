# Flexible Local Clock model

[![CI & Publish](https://github.com/4ment/flc/actions/workflows/ci-publish.yml/badge.svg)](https://github.com/4ment/flc/actions/workflows/ci-publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.4ment/flc.svg)](https://search.maven.org/artifact/io.github.4ment/flc)
[![Java](https://img.shields.io/badge/Java-25+-blue.svg)](https://openjdk.org/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![BEAST3](https://img.shields.io/badge/BEAST-3.x-brightgreen.svg)](https://github.com/CompEvol/beast3)

A [BEAST 3](https://github.com/CompEvol/beast3) package for relaxed clock models within local clock models.

Time-resolved phylogenetic methods estimate evolutionary rates using dated sequence data. Standard molecular clock models assume a single substitution rate across lineages, while extensions such as local clocks and uncorrelated relaxed clocks allow structured or stochastic rate variation. The Flexible Local Clock (FLC) provides a unified framework that combines both approaches, enabling flexible modelling of rate heterogeneity across phylogenies.

## Citation

Fourment M and Darling AE (2018) Local and relaxed clocks: the best of both worlds. PeerJ 6:e5140. DOI: [10.7717/peerj.5140](https://doi.org/10.7717/peerj.5140)

## Maven coordinates

```xml
<dependency>
    <groupId>io.github.4ment</groupId>
    <artifactId>flc</artifactId>
    <version>1.3.0-beta1</version>
</dependency>
```

JPMS module name: `flc`

## Building from source

Requires Java 25 and Maven.

```bash
mvn install -DskipTests
```

## Running BEAST with FLC
Example:

```bash
mvn exec:exec -Dbeast.args="-overwrite examples/Human.H3.81-98-elc.xml"
```

## License

Distributed under the GPLv3 License. See [COPYING](COPYING) for more information.
