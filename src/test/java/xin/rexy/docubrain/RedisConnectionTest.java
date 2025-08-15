package xin.rexy.docubrain;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisConnectionTest {

    /*
     * 如果说 StringRedisTemplate 是“基础对话”工具，那么 VectorStore 就是
     * “高级智能对话”的接口。它是我们 AI 系统的大脑与长期记忆库（Redis）
     * 之间的“翻译官”和“档案管理员”。它的职责是：
     * a. 调用一个 AI 模型 (EmbeddingModel)，将文本的“含义”翻译成数字向量。
     * b. 将这些代表“含义”的向量存入 Redis。
     * c. 当用户提问时，能根据问题的“含义”在 Redis 中找到最相关的答案。
     */
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedisConnectionAndVectorStoreInjection() {
        // --- 测试 1: 验证 VectorStore 是否被成功注入 ---
        // 如果程序能运行到这里，说明 vectorStore 已经被成功创建和注入了。
        assertNotNull(vectorStore, "VectorStore (向量存储) Bean 被成功注入。");
        System.out.println("VectorStore Bean 注入成功: " + vectorStore.getClass().getName());

        // --- 测试 2: 验证基本的 Redis 连接和操作 ---
        try {
            String key = "test:junit:key";
            String expectedValue = "Hello Redis from Spring Boot Test!";
            // 写入数据
            redisTemplate.opsForValue().set(key, expectedValue);

            // 读取数据
            String actualValue = redisTemplate.opsForValue().get(key);

            // 使用断言来验证结果
            assertEquals(expectedValue, actualValue, "从 Redis 读取的值应该与写入的值完全匹配。");
            System.out.println("成功连接到 Redis 并完成了一次读写操作。");

            // 清理测试数据
            redisTemplate.delete(key);

        } catch (Exception e) {
            // 如果发生任何异常，测试将失败并打印错误信息
            fail("连接 Redis 或执行操作时失败。", e);
        }
    }

}