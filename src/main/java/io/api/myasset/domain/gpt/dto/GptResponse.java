package io.api.myasset.domain.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class GptResponse {
	private List<Choice> choices;

	@Getter
	@NoArgsConstructor
	public static class Choice {
		private Message message;
	}

	@Getter
	@NoArgsConstructor
	public static class Message {
		private String role;
		private String content;
	}

	public String firstContent() {
		if (choices == null || choices.isEmpty())
			return null;
		return choices.get(0).getMessage().getContent();
	}
}
