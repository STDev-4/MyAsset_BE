package io.api.myasset.domain.gpt.prompt;

public class DataPrompt implements PromptTemplate {

    private final String title;
    private final String data;

    public DataPrompt(String title, String data) {
        this.title = title;
        this.data = data;
    }

    @Override
    public String render() {
        return """
            === %s ===
            %s
            """.formatted(title, data);
    }
}
