# Test utilities

## Immutable/Jackson JSON tests

Tests the ability for your Immutable class to be JSON friendly.

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

## Features

* Works with optional, @nullable fields.
* Compatible with @JsonIgnore.
* Handles maps/lists/sets as well.
* Jupiter and AssertJ focused.