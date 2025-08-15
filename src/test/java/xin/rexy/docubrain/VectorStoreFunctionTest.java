package xin.rexy.docubrain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VectorStore 功能集成测试
 *
 * 这个测试类的目的是验证 VectorStore 的核心 CRUD (创建、读取、更新、删除) 操作，
 * 特别是 Add (添加) 和 SimilaritySearch (相似性搜索) 功能。
 *
 * 我们将模拟真实的使用场景：
 * 1. 向向量数据库中存入一些文档。
 * 2. 提出一个问题（查询）。
 * 3. 验证返回的结果是否与我们存入的、语义最相关的文档一致。
 * 4. 测试完成后清理数据。
 */
@SpringBootTest
public class VectorStoreFunctionTest {

    @Autowired
    private VectorStore vectorStore;

    // 为了确保每次测试都是干净的，我们创建一个唯一的ID来隔离这次测试的数据
    private final String testCollectionId = UUID.randomUUID().toString();

    /**
     * 在每个测试方法运行前，先存入一些基础文档数据。
     */
    @BeforeEach
    public void setUp() {
        List<Document> documents = List.of(
                new Document("春天的花朵最是艳丽，尤其是那盛开的桃花。", Map.of("collectionId", testCollectionId, "category", "nature")),
                new Document("夏天傍晚的雷阵雨过后，空气总是格外清新。", Map.of("collectionId", testCollectionId, "category", "nature")),
                new Document("秋天的枫叶红了，像一团团燃烧的火焰。", Map.of("collectionId", testCollectionId, "category", "nature")),
                new Document("冬天的大雪覆盖了整个世界，一片银装素裹。", Map.of("collectionId", testCollectionId, "category", "nature")),
                new Document("Java 是世界上最流行的编程语言之一。", Map.of("collectionId", testCollectionId, "category", "tech")),
                new Document("Spring Boot 极大地简化了 Java Web 应用的开发。", Map.of("collectionId", testCollectionId, "category", "tech"))
        );

        // 将文档添加到 VectorStore
        vectorStore.add(documents);
    }

    @Test
    public void testSimilaritySearchForNatureTopic() {
        System.out.println("相关主题的相似性搜索......");
        String query = "哪个季节最适合种花";
        SearchRequest searchRequest = SearchRequest.builder()
                //.filterExpression("collectionId == '" + testCollectionId + "'")  //返回精确查找的结果
                .query(query)
                .topK(3)  //返回前3个最相关的结果
                .build();
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        assertThat(results).hasSize(3);//我断言返回了3个结果  assertThat 是一个非常流行的 Java 测试库 AssertJ 中的核心方法。它让编写测试断言（Assertions）变得像写自然语言一样流畅和易读。
        List<String> list = results.stream()     // a. 将 List<Document> 转换为一个流 (Stream)
                .map(Document::getText)          // b. 对流中的每个 Document 对象，调用 getText() 方法，得到一个只包含字符串的流
                .toList();                       // c. 将字符串流转换回一个 List<String>
        System.out.println("查询："+ query);
        // 打印出提取出的文本内容列表，方便在运行测试时人工观察结果。
        System.out.println("AI返回的相关文本内容: " + list);
        System.out.println("============================================================================");
        // 断言1: 返回结果中，必须至少有一条包含“桃花”
        assertThat(list).anyMatch(content -> content.contains("桃花"));
        // 断言2: 返回结果中，绝对不能有任何一条包含“Java”
        assertThat(list).noneMatch(content -> content.contains("Java"));
    }

    @Test
    public void testSimilaritySearchForTechTopic(){
        System.out.println("相关主题的相似性搜索......");
        String query = "Spring Boot 是什么";
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(3)
                .build();
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        assertThat(results).hasSize(3);
        List<String> list = results.stream()
                .map(Document::getText)
                .toList();
        System.out.println("查询："+ query);
        System.out.println("AI返回的相关文本内容: " + list);
        System.out.println("============================================================================");
    }
}