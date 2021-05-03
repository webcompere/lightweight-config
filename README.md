# Lightweight Configuration
A small library for enabling configuration parameters to be loaded in Java

[![Build status](https://ci.appveyor.com/api/projects/status/xi4kb7soojsx6rs3/branch/main?svg=true)](https://ci.appveyor.com/project/ashleyfrieze/lightweight-config/branch/main) [![codecov](https://codecov.io/gh/webcompere/lightweight-config/branch/main/graph/badge.svg?token=HE5JM9TLKL)](https://codecov.io/gh/webcompere/lightweight-config)

This library is intended for use in small applications for loading parameters
that may be set as system properties or environment variables.

It allows a YML file to be created that describes how these properties
can be loaded into a Java object. It uses Jackson and YML.

## Usage

### Installation

```xml
<dependency>
  <groupId>uk.org.webcompere</groupId>
  <artifactId>lightweight-config</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Basic Example

_Assuming a config file in resources of `config.yml` and a class
that models that file called `Configuration`:_

```java
Configuration configuration =
    ConfigLoader.loadYmlConfigFromResource("config.yml", Configuration.class);
```

This reads `config.yml` from the available resources into an object of the `Configuration`
class.

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
// or use Jackson to decorate it for the loading of properties
// from the YML
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

Note: the parent file is loaded into the Java object, so it may also
express some values to load into that object.

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
