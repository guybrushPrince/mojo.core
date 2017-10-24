<img src="mojo_1.0.png" width="350px" alt="Mojo Logo" />

Mojo is an analysis tool for business processes. If you are developing, analyzing, or checking processes, Mojo offers you failures with detailed diagnostic information that can support the correction of the processes.

## Installing from source

Mojo Core is build with gradle, https://gradle.org/downloads. To build it, you have to clone this repository and to execute

```bash
./gradlew build
```

It is also possible to build an eclipse project:

```bash
./gradle eclipse
```


## Using Mojo as library with gradle

To include Mojo in your project with gradle, you can use JitPack, https://jitpack.io. Therefore, you have to add the following lines to your `build.gradle` script:

```gradle
repositories {
	...
	maven { url 'https://jitpack.io' }
}

...

dependencies {
	compile 'com.github.guybrushPrince:mojo.core:master'
}
```

## Using Mojo as library with Maven

To include Mojo in your project with Maven, you can also use JitPack, https://jitpack.io. Therefore, you have to add the following lines to your `pom.xml` script:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

...

<dependency>
    <groupId>com.github.guybrushPrince</groupId>
    <artifactId>mojo.core</artifactId>
    <version>master</version>
</dependency>
```


## Mojo Plug-ins

Mojo allows you to add plug-ins and to write your own plug-ins.

### Plug-ins ready to use

There are several Mojo plug-ins which are ready to use:

- https://github.com/guybrushPrince/mojo.reader.pnml
- https://github.com/guybrushPrince/mojo.reader.bpmn
- https://github.com/guybrushPrince/mojo.plan.sese

To use them, you simply had to add them to the Mojo classpath.


### Write your own Mojo Plug-ins

There are two possibilities to create a plug-in:
1. an analysis plug-in or
2. a source plug-in

#### Analysis plug-in

To write your own analysis plug-in, you have to write your own class extending the class `de.jena.uni.mojo.plugin.PlanPlugin`. Furthermore, you have to add a file with name `de.jena.uni.mojo.plugin.PlanPlugin` to the folder `src/main/resources/META-INF/services`.

#### Source plug-in

To write your own source plug-in, you have to write your own class extending the class `de.jena.uni.mojo.plugin.SourcePlugin;`. Furthermore, you have to add a file with name `de.jena.uni.mojo.plugin.SourcePlugin` to the folder `src/main/resources/META-INF/services`.

## Help and contact

If you have any questions, please do not hesitate to contact me: thomas.prinz@uni-jena.de 