package com.rebra.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceNewsSummaryService {

    private final ChatModel chatModel;

    public String summarize(String articleText) {
        String systemInstruction = """
            당신은 한국 경제 뉴스를 2~3문장으로 간결하게 요약하는 AI입니다. 아래의 글을 요약하세요
        """;

        OpenAiChatOptions options = OpenAiChatOptions.builder().build();

        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemInstruction), new UserMessage(articleText)),
                options
        );

        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}