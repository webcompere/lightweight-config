# Lightweight Configuration
A small library for enabling configuration parameters to be loaded in Java

[![Build status](https://ci.appveyor.com/api/projects/status/xi4kb7soojsx6rs3/branch/main?svg=true)](https://ci.appveyor.com/project/ashleyfrieze/lightweight-config/branch/main) [![codecov](https://codecov.io/gh/webcompere/lightweight-config/branch/main/graph/badge.svg?token=HE5JM9TLKL)](https://codecov.io/gh/webcompere/lightweight-config)  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.org.webcompere/lightweight-config/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.org.webcompere/lightweight-config/)

This library is intended for use in small applications for loading parameters
that may be set as system properties or environment variables.

#### YML
Create a YML file to describe the properties to load, and a Java object
to represent them.

Lightweight Config uses SnakeYML as its YML engine and loads the YML after first
pre-processing it to import placeholders relating to environment variables or system
properties.

It also allows some custom logic to be added to handle custom _tags_ in the YML.

#### Properties Files
A thin wrapper around `Properties.load` this pre-processes properties files found in
resources or file system, in order to interpolate environment variables or
system properties.

## Usage

### Installation

```xml
<dependency>
  <groupId>uk.org.webcompere</groupId>
  <artifactId>lightweight-config</artifactId>
  <version>1.2.1</version>
</dependency>
```

### Basic Examples

_Assuming a config file in resources of `config.yml` and a class
that models that file called `Configuration`:_

```java
Configuration configuration =
    ConfigLoader.loadYmlConfigFromResource("config.yml", Configuration.class);
```

This reads `config.yml` from the available resources into an object of the `Configuration`
class.

or

```java
Configuration configuration =
    ConfigLoader.loadYmlConfig(filePath, Configuration.class);
```

Allows the `.yml` file to be on the file system somewhere.


For `Properties` loading:

```java
Properties properties =
    ConfigLoader.loadPropertiesFromResource("examples/interpolation.properties");
```

Properties files can also be loaded from file system via `Path`:

```java
Properties properties = ConfigLoader.loadProperties(Paths.get("src", "test", "resources",
    "subdir", "import.properties"));
```

**Other examples are available in [`ExamplesTest`](src/test/java/uk/org/webcompere/lightweightconfig/examples/ExamplesTest.java).**

### Yml Format
Using the standard YML loader, a file like this:

```yaml
name: 'Audrey'
age: 32
isLoggedIn: true
```

can be loaded into a java class:

```java
// POJO class
// It should have a default constructor and getters/setters
// compatible with loading by SnakeYml
class Config {
    private String name;
    private int age;

    // getters/setters
}
```

The Yaml can express hardcoded values, but can also use _placeholders_
to indicate fields which should be calculated at runtime before the
yaml parser is used.

Example:

```yaml
name: ${USER_NAME:-anonymous}
age: ${USER_AGE:-0}
isLoggedIn: ${USER_LOGGED_IN}
```

### Placeholders

Placeholders can be provided with or without defaults. Their
values will be loaded from the first available value in environment
variables or system properties.

Examples:

- `${some.value}` - replaced by the contents of `some.value` or blank if there isn't one
- `${SOME_VALUE}` - replaced by the contents of `SOME_VALUE` or blank if there isn't one
- `${some.value:-123}` - allows a default of `123` to apply if the `some.value` cannot be found from the context
- `${}` - equates to `$` - allowing you to use things like look like placeholders as strings in the data
  - So to express the actual value `${this-is-not-a-placeholder}` - you can put `${}{this-is-not-a-placeholder}`

### Priority

The lookup evaluates in the order of:

- Environment variable
- System property
- Default
- Blank

Any non-null in the first three will result in the placeholder being filled. This allows
a blank value in an environment variable to supersede a non-blank in a system property.

> Although the convention is `lowercase.system.property` and `UPPER_SNAKE_CASE_ENVIRONMENT_VARIABLE`
> this is not enforced in any way by the library

### Load to `Map`

While a key benefit of this library is its ability to deserialize the configuration
into a POJO, for very simple cases, it may be desirable to load into a `Map`. This
may also cover cases where there's a variable number of properties that cannot be
predicted at development time. E.g. where the configuration is fundamentally
a dictionary.

```java
// Configuration loaded as a map
Map<String, Object> configuration =
    ConfigLoader.loadYmlConfigFromResource("config.yml");
```

### Importing other Files

For reuse of segments of configuration, use the placeholder `#import` followed by a space
and then the filename:

```yaml
#import commonConfig.yml
```

This imports the file `commonConfig.yml` verbatim into wherever in the source file
the `#import` statement was made.

The `#import` statement also allows placeholders to be used, so subsets
of configuration can be loaded, driven by an environment variable. This might
for example, allow different runtime profiles to be in separate files.

Example:

```yaml
#import commonConfig.yml
#import ${runtime.profile}-Config.yml
someVariable: true
someVariableByPlaceholder: ${PLACEHOLDER_VALUE}
```

Here, the contents of the property `runtime.profile` might load
a file such as `dev-Config.yml` or `prod-Config.yml`.

> Note: the parent file is loaded into the Java object, so it may also
> express some values to load into that object.

#### Import File Paths

When `#import` is used within resources, the import path is the full
resource path to the imported file.

With file imports, the paths for `#import` are
relative to the current file. E.g. `#import ../somefile.properties` or
`#import neighbour.properties`.

## Customization

An object of `ConfigLoader` allows customization to be added. Rather than using the `static`
methods on `ConfigLoader` to load the configuration, create a `new ConfigLoader`,
apply any customizations to it, and then use `loadAs` or `load` to load the
configuration into a custom object or a `Map`.

### Customize Resource Provider

```java
Config myConfig = new ConfigLoader()
    .withResourceLoader(StringProvider::fromString)
    .loadAs("concurrency: ${CONCURRENCY}", Config.class);
```

The custom `StringProvider` allows the input resource to be treated as a YML literal.

> Note: when building a custom provider, it's necessary to include placeholder
> parsing and import logic as part of the provider. This can be implemented
> using either `PlaceholderParser` or `ImportAwarePlaceholderResolver`.

### Custom Tags and Functions

**Note: this is a good way to integrate with a password manager**

```java
Config myConfig = new ConfigLoader()
    .withResourceLoader(StringProvider::fromString)
    .withTag("password", myPasswordManager::load)
    .loadAs("password: !password ${PASSWORD_ID}", Config.class);
```

When parsing `!password`, the `String` value to the right of it - here defined by a
placeholder - will be passed to the `load` function of `myPasswordManager`.

For other examples see [`ExamplesTest`](src/test/java/uk/org/webcompere/lightweightconfig/examples/ExamplesTest.java).

## Contributing

If you have any issues or improvements, please
[at least submit an issue](https://github.com/webcompere/lightweight-config/issues).

Please also feel free to fork the project and submit a PR for consideration.

* Please write a test for your change
* Ensure that the build succeeds and there's no drop in code coverage - see the GitHub PR for feedback

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.

### Build

```bash
# to build
./mvnw clean install
```

### Release

> Internal use

```bash
export GPG_TTY=$(tty)
./mvnw clean -Dgpg.executable=gpg -Prelease-sign-artifacts -Dgpg.passphrase=secret release:prepare release:perform -f pom.xml
```
