package xin.rexy.docubrain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import xin.rexy.docubrain.common.SecurityUtils;
import xin.rexy.docubrain.service.KnowledgeBaseService;
import xin.rexy.docubrain.service.DocumentInfoService;
import xin.rexy.docubrain.entity.KnowledgeBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 页面控制器 - 处理Thymeleaf模板页面路由
 */
@Controller
public class PageController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private DocumentInfoService documentInfoService;

    /**
     * 首页 - 知识库管理
     */
    @GetMapping("/")
    public String index(Model model) {
        // 获取当前用户的知识库列表
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            List<KnowledgeBase> knowledgeBases = knowledgeBaseService.listByUserId(userId);
            model.addAttribute("knowledgeBases", knowledgeBases);
            
            System.out.println("=== 首页统计调试信息 ===");
            System.out.println("用户ID: " + userId);
            System.out.println("知识库数量: " + knowledgeBases.size());
            
            // 为每个知识库计算文档数量 - 安全处理，过滤null值
            Map<Long, Long> documentCounts = knowledgeBases.stream()
                .filter(kb -> kb != null && kb.getId() != null)
                .collect(Collectors.toMap(
                    KnowledgeBase::getId,
                    kb -> {
                        System.out.println("正在统计知识库: " + kb.getId() + " (" + kb.getName() + ")");
                        Long count = documentInfoService.countByKnowledgeBaseId(kb.getId());
                        System.out.println("知识库 " + kb.getId() + " 文档数量: " + count);
                        return count != null ? count : 0L;
                    }
                ));
            model.addAttribute("documentCounts", documentCounts);
            
            // 添加文档统计信息
            long totalDocs = documentInfoService.countByUserId(userId);
            System.out.println("用户总文档数量: " + totalDocs);
            model.addAttribute("totalDocuments", totalDocs);
            
            System.out.println("文档统计结果: " + documentCounts);
        }
        return "index";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * 文档上传页面
     */
    @GetMapping("/upload")
    public String upload(Model model) {
        // 获取当前用户的知识库列表供选择
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            model.addAttribute("knowledgeBases", knowledgeBaseService.listByUserId(userId));
        }
        return "upload";
    }

    /**
     * 智能问答页面
     */
    @GetMapping("/chat")
    public String chat(Model model) {
        // 获取当前用户的知识库列表供选择
        Long userId = SecurityUtils.getUserId();
        List<KnowledgeBase> knowledgeBases = new ArrayList<>();
        Map<Long, Long> documentCounts = new HashMap<>();
        
        System.out.println("=== 聊天页面调试信息 ===");
        System.out.println("当前用户ID: " + userId);
        
        if (userId != null) {
            knowledgeBases = knowledgeBaseService.listByUserId(userId);
            System.out.println("用户知识库数量: " + knowledgeBases.size());
            
            // 为每个知识库计算文档数量 - 安全处理，过滤null值
            documentCounts = knowledgeBases.stream()
                .filter(kb -> kb != null && kb.getId() != null)
                .collect(Collectors.toMap(
                    KnowledgeBase::getId,
                    kb -> {
                        Long count = documentInfoService.countByKnowledgeBaseId(kb.getId());
                        System.out.println("知识库 " + kb.getId() + " (" + kb.getName() + ") 文档数量: " + count);
                        return count != null ? count : 0L;
                    }
                ));
        }
        
        System.out.println("文档数量统计: " + documentCounts);
        
        model.addAttribute("knowledgeBases", knowledgeBases);
        model.addAttribute("documentCounts", documentCounts);
        return "chat";
    }

    /**
     * 知识库详情页面
     */
    @GetMapping("/knowledge/{id}")
    public String knowledgeDetail(@PathVariable Long id, Model model) {
        model.addAttribute("knowledgeBaseId", id);
        return "knowledge-detail";
    }
    
    /**
     * 问答功能调试页面
     */
    @GetMapping("/debug-chat")
    public String debugChat() {
        return "debug-chat";
    }
}