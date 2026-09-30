# Guess Market - Exercise 3 (Client-Server Application)

By Peleg Wurzel and Ben Lutenberg

![Windows smoke test](https://github.com/peleg525/Guess_Market_client-server/actions/workflows/windows-smoke-test.yml/badge.svg)

A client-server implementation of *Guess Market*: a Tomcat-hosted server exposes the Exercise
1/2 engine over HTTP as JSON endpoints, and a JavaFX client - built on Exercise 2's screens and
components - talks to it instead of calling the engine locally.

> Our actual submission readme - our details, the assumptions we made, and a walkthrough of the
> classes - is `README.docx`, since that's the format the assignment requires. This file here is
> just the regular repo readme.

## Modules

- **gm-api** - the client-server contract: the `GmEngine` interface, every DTO, the exceptions,
  and the two enums that appear directly in DTO fields (`OrderSide`, `EventStatus`). No trading
  logic, no XML, no JavaFX.
- **gm-engine** - the passive engine, still unaware of who's calling it. Depends on `gm-api`,
  implements `GmEngine`, loads/validates the Exercise 3 XML, tracks users (created purely through
  login), and runs both trading methods.
- **gm-server** - a WAR of plain `jakarta.servlet` classes wrapping a single shared `GmEngine`
  instance and translating HTTP/JSON to/from its calls.
- **gm-client** - the JavaFX UI, built directly on Exercise 2's `gm-ui` screens/components, now
  driven by an HTTP-backed `GmEngine` implementation instead of a local one. Depends only on
  `gm-api`, never on `gm-engine` itself - the client jar never needs `GmEngineImpl`, the XML
  loader, or any trading-model class.

## Build

Requires JDK 25.

```
mvn clean package
```

Produces `gm-engine/target/gm-engine.jar`, `gm-server/target/gm-server.war`, and
`gm-client/target/gm-client.jar` (plus `gm-client/target/lib/` with the JAXB runtime, Gson, and
Windows-native JavaFX jars).

## Run

1. Drop `gm-server/target/gm-server.war` into a Tomcat 10.1+ installation's `webapps` folder and
   start Tomcat.
2. `java -jar gm-client/target/gm-client.jar`, once per client you want to run - or, from an
   assembled distribution folder (`gm-client.jar` + `lib/` sitting next to each other, as in the
   submission zip):

```
run.bat
```

Requires Java 25 on the PATH (`java -version`).

For local development/testing on a Mac instead of Windows:
`mvn -Pmac-dev package`, or
`mvn -Pmac-dev -pl gm-client org.openjfx:javafx-maven-plugin:0.0.8:run -Djavafx.mainClass=gm.ui.Main`.

Sample XML event files for manual testing are under `testfiles/`.

## Tests

`gm-engine` has JUnit tests covering file upload/validation and accumulation, login/deposit/chat,
the LMSR appendix example, and a step-by-step replay of the lecturer's order-book reference
simulation:

```
mvn -pl gm-engine -am test
```

## CI

`.github/workflows/windows-smoke-test.yml` runs the engine's unit tests, builds all three modules,
deploys the WAR to a real Tomcat instance and hits it over HTTP, and launches the packaged client
jar - all on a real `windows-latest` GitHub Actions runner, on every push.
