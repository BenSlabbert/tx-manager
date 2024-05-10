# Plugin Usage

Update the build as follows:

```xml
<plugin>
  <groupId>net.bytebuddy</groupId>
  <artifactId>byte-buddy-maven-plugin</artifactId>
  <configuration>
    <transformations>
      <transformation>
        <groupId>github.benslabbert.txmanager</groupId>
        <artifactId>plugin</artifactId>
        <version>VERSION</version>
        <plugin>github.benslabbert.txmanager.plugin.TransactionalAdvicePlugin</plugin>
      </transformation>
    </transformations>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>transform</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

