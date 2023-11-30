package app.nzyme.core.rest.responses.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;

@AutoValue
public abstract class Dot11MacAddressResponse {

    @JsonProperty("address")
    public abstract String address();

    @JsonProperty("context")
    @Nullable
    public abstract Dot11MacAddressContextResponse name();

    public static Dot11MacAddressResponse create(String address, Dot11MacAddressContextResponse name) {
        return builder()
                .address(address)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MacAddressResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder address(String address);

        public abstract Builder name(Dot11MacAddressContextResponse name);

        public abstract Dot11MacAddressResponse build();
    }
}
