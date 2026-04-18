package io.api.myasset.domain.gpt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
public class GptRequest {

	private String model = "gpt-4.1-mini";

	@JsonProperty("max_tokens")
	@Setter
	private Integer maxTokens;

	private List<Message> messages;

	public GptRequest(String systemMessage, Integer maxTokens) {
		this.messages = List.of(
			new Message("system", systemMessage));
		this.maxTokens = maxTokens;
	}

	@Getter
	@AllArgsConstructor
	public static class Message {
		private String role;
		private String content;
	}
}
