# Flexible Local Clock model

A [BEAST 3](https://github.com/CompEvol/beast3) package for relaxed clock models within local clock models.

## Citation

Fourment M and Darling AE (2018) Local and relaxed clocks: the best of both worlds. PeerJ 6:e5140. DOI: [10.7717/peerj.5140](https://doi.org/10.7717/peerj.5140)

## Maven coordinates

```xml
<dependency>
    <groupId>io.github.compevol</groupId>
    <artifactId>flc</artifactId>
    <version>1.3.0-SNAPSHOT</version>
</dependency>
```

JPMS module name: `flc`

## Building from source

Requires Java 25 and Maven.

```bash
mvn install -DskipTests
```

## Running BEAST with FLC

```bash
mvn exec:exec -Dbeast.args="-overwrite examples/localClock.xml"
```

## License

GPL v3
