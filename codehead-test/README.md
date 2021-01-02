# Test utilities

## Immutable/Jackson JSON tests

Tests the ability for your **Immutables** objects to be JSON friendly. Validates you have all the jackson
annotations setup correctly. It can handle complex structures as well as simple ones. Allows for custom
ObjectMapper to be used for the tests as well. See the state machine project for usage examples.

I've seen variations of this from different places and made my own version that uses junit5 and assertJ.

## Usage

```java
@Value.Immutable
@JsonSerialize(as = ImmutableStandardImmutableModel.class)
@JsonDeserialize(builder = ImmutableStandardImmutableModel.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface StandardImmutableModel {
    @JsonProperty("someString")
    String someString();
}

public class StandardImmutableModelTest extends BaseJacksonTest<StandardImmutableModel> {
    @Override
    protected Class<StandardImmutableModel> getBaseClass() {
        return StandardImmutableModel.class;
    }
    @Override
    protected StandardImmutableModel getInstance() {
        return ImmutableStandardImmutableModel.builder()
            .someString("this string")
            .build();
    }
}
```
## Release



gradle  
```
repositories {
    jcenter()
}
dependencies {
    implementation 'com.codeheadsystems.terrapin:codehead-test:0.9'
}
```


## Features

* Works with optional, @nullable fields.
* Compatible with @JsonIgnore.
* Handles maps/lists/sets as well.
* Jupiter and AssertJ focused.