``` XML 
<?xml version="1.0" encoding="UTF-8"?>
	<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>tribefire.extension.demo</groupId>
        <artifactId>parent</artifactId>
        <version>[${major}.${minor},${major}.${nextMinor})</version>
    </parent>
    <properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>1-pc</revision>
        <archetype>library</archetype>
    </properties>
    <artifactId>tribefire-demo-setup</artifactId>
    <version>${major}.${minor}.${revision}</version>
    <dependencies>
        <dependency>
            <groupId>tribefire.cortex.assets</groupId>
            <artifactId>tribefire-standard-aggregator</artifactId>
            <version>${V.tribefire.cortex.assets}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
        <dependency>
            <groupId>tribefire.extension.demo</groupId>
            <artifactId>tribefire-demo-cartridge-initializer</artifactId>
            <version>${V.tribefire.extension.demo}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
    </dependencies>
</project>
```