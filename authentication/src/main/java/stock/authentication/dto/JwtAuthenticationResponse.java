package stock.authentication.dto;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class JwtAuthenticationResponse {
    private String token;

    @JsonCreator
    public JwtAuthenticationResponse(@JsonProperty("token") String token) {
        this.token = token;
    }
}