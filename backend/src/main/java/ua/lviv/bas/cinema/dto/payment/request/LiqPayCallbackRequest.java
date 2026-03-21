package ua.lviv.bas.cinema.dto.payment.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record LiqPayCallbackRequest(
		@JsonProperty("data") @Schema(description = "Base64 encoded JSON data from LiqPay", example = "eyJvcmRlcl9pZCI6Ik9SRF9BQkMxMjMiLCJzdGF0dXMiOiJzdWNjZXNzIn0=") String data,

		@JsonProperty("signature") @Schema(description = "SHA1 signature for validation", example = "wBqNnqJ5k8D6eB3vQ2sR7tY9uI0oP4aZ1xC7vE9gH2iK5mN8pQ3sR6tY=") String signature) {
}