# TX Manager

The project uses [byte-buddy](https://github.com/raphw/byte-buddy) to enhance Java classes as opposed to using runtime
reflection.

The [byte-buddy-plugin](https://github.com/raphw/byte-buddy/tree/master/byte-buddy-maven-plugin) is invoked on build and
transforms the classes as they are compiled and packaged into the JAR.

This removes the requirement to transform them later during runtime.

## Showcase

Run the example with the agent:

```shell
java -javaagent:agent/target/agent-uber.jar=hello -jar example/target/example-uber.jar
```

or without the agent:

```shell
java -jar example/target/example-uber.jar
```

