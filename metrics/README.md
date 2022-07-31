# Metrics

Provide a wrapper around metric implementations so we can switch depending on our
running environment.

# Usage

```java
final Metrics metricsImplementation = metricsFatory.build();
metricsImplementation.count(1, name(getClass(), "metricName"));
metricsImplementation.close();

// or
threadLocalMetricsFactory.with(() -> {
    threadLocalMetricsFactory.get().count(1, name(getClass(), "metricName"));
        });

```

The project is setup to use Dagger by default, but you can easily use guice, spring, etc.

# Design

![design](docs/design.drawio.svg) [(link)](https://viewer.diagrams.net/?tags=%7B%7D&highlight=0000ff&edit=_blank&layers=1&nav=1&title=Untitled%20Diagram.drawio#R7VnbctsgEP0aP6YjdPHlMU7stNOmzYzbyTMWWGKChILxJfn6ogh0Q7blxI49Gb940LIscPaw7OKOcxOt7zhMwnuGMO3YFlp3nNuObfeBJ39TwUsmAH13kEkCTpCSFYIJecVKaCnpgiA8rygKxqggSVXoszjGvqjIIOdsVVWbMVqdNYEBNgQTH1JT%2BkiQCNW%2BPKuQf8ckCPXMwFI9EdTKSjAPIWKrksgZdZwbzpjIWtH6BtMUPI1LNm68oTdfGMexaDNg2IvpzxF%2BRavx8914RsjT8%2FRKWVlCulAbvseCE3%2Bu1ixeNBCcLWKEU1tWxxmuQiLwJIF%2B2ruSrpeyUERUfgHZVFYxF3i9cbkgB0GyB7NITvwiVfSArsJNMaenPleFF4CGNix5QA%2BDyvFBbrnARjYUPHtAZRtQ%2FV5Qei5w9atogf6p4XI2MetHlNCTw2W754ZXz8CrY3epnHWIyFI2g7SZsy3rkROVOhv0x9AXLF1cs37NCYeF2LVqB9gzEQYNCHvHQrhvIDxdEIrMs4tjdJ1eHfKLJTiWcEjJmFANjvxSFxWwq8ys0jgzjJFxw9QglJOzBffxbnYIyAMsdsVz0yVlzDWDOaZQkGV1bU2gK3MPjMhV5%2B51aicoP1HaRLYnNap8L9UMedYOQ9mmDUNvHMj3%2BH5aDAxagBaMKDm9Roe5XK%2FQ2ojAiMXob0hi3aV1XS0oM0tyZaKmZVyELGAxpKNCOoSUBNLULcWz9AynJ5TIbOVaiadMCBZ9gIm7GWY1U0x5U2cUypnOQbjmgSpFBoNv3vvI1u3tsnRktumdHDbOP3C2lHky111TXlf%2B1NDv2S1u10%2BN%2FcBMdBtgvOUseSSvkKPWyNc8dWLg3XpQPj3wZtr8R4QpU61NqfNhIenWr5fTQ2KmxtsunCll%2FlNKOjgP3wI6qCQk1mkSEvDR%2B%2BKSkmzBzP0KDLEvDDkeQ7wvwBCnLUEu%2FNibH90vwI%2FWAeTCj735YT43%2FZvjbRQpXkLKNKgQ5qxixuVS2Z8U5gtZpeIpMaP7vEj%2FtxjOWCyu5m%2BEuJYKwErWWe2j%2BuvVUKr%2FIUN%2FQ46hLM%2BsX8yHtFRkZZa3115ntpe8ALJq77SNVWSbDR61knL6XoWeV05DJeV%2BaiVlvt0dMYqlDtCjDp0r91uGtd7poho4VFSrG3p3VJOfxd%2BnmXrxJ7Qz%2Bg8%3D)