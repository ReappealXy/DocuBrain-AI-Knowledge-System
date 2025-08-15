package xin.rexy.docubrain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RagFunctionTest {

    @Autowired
    private VectorStore vectorStore;

    private ChatClient chatClient;
    @Autowired
    public RagFunctionTest(ChatClient.Builder chatClientBuilder){
        this.chatClient = chatClientBuilder.build();
    }

    @BeforeAll
    void setUp() {
        System.out.println("--- [RAG 测试] 正在初始化知识库数据 ---");
        List<Document> documents = List.of(
                new Document("春季是播种的季节，许多花卉如桃花、迎春花都在此时绽放。", Map.of("source", "gardening_guide_p1")),
                new Document("夏季气温高，要注意给植物防晒和补水，适合种植向日葵。", Map.of("source", "gardening_guide_p2")),
                new Document("秋季是收获的季节，也是观赏菊花和枫叶的好时机。", Map.of("source", "gardening_guide_p3")),
                new Document("Spring Boot 是一个流行的 Java Web 开发框架，以其‘约定优于配置’的理念著称。", Map.of("source", "tech_manual_p5"))
        );
        vectorStore.add(documents);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testGardeningQuestionWithRag() {
        System.out.println("\n====================== RAG测试 ======================");
        // 前期的test只有RA，并没有G，因此还需要调用大模型来对检索的答案进行优化
        String userQuery = "春天适合种什么花？";
        System.out.println("提问: " + userQuery);

        // 1. 检索 (Retrieval)
        System.out.println("正在检索相关信息...");
        List<Document> similarDocuments = vectorStore.similaritySearch(userQuery);
        List<String> context = similarDocuments.stream()
                .map(Document::getText)
                .toList();
        System.out.println("检索到的上下文: " + context);

        // 2. 增强 (Augmentation)
        // 3. 生成 (Generation)
        System.out.println("使用流式API构建 Prompt 并请求聊天模型生成回答...");

        // 定义系统消息，给 AI 指令
        String systemMessage = """
                你是一个专业的园艺助手。
                你的回答必须完全基于用户提供的背景知识。
                如果背景知识不足以回答，就直接说“根据现有知识，我无法回答这个问题”。
                """;

        // 定义用户消息模板，它包含了我们检索到的上下文和用户的原始问题
        String userMessageTemplate = """
                请根据以下背景知识来回答我的问题。

                [背景知识]
                {context}

                [我的问题]
                {query}
                """;

        String aiResponse = chatClient.prompt()
                .system(systemMessage) // 设置系统消息
                .user(userSpec -> userSpec // 开始构建用户消息
                        .text(userMessageTemplate) // 使用我们的用户消息模板
                        .param("context", context) // 绑定上下文参数
                        .param("query", userQuery) // 绑定问题参数
                )
                .call() // 发起对 AI 模型的调用
                .content(); // 从返回结果中提取最终的文本内容

        System.out.println("AI 生成的最终回答: ");
        System.out.println(aiResponse);
    }
}