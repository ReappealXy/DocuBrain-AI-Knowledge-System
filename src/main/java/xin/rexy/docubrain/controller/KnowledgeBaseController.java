package xin.rexy.docubrain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xin.rexy.docubrain.common.Result;
import xin.rexy.docubrain.common.SecurityUtils;
import xin.rexy.docubrain.entity.KnowledgeBase;
import xin.rexy.docubrain.service.KnowledgeBaseService;

import java.util.List;

/**
 * <p>
 * 知识库表 前端控制器
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@RestController
@RequestMapping("/knowledgeBase")
@Tag(name = "知识库管理模块", description = "知识库表")
public class KnowledgeBaseController {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/create")
    @Operation(summary = "创建知识库")
    public Result<KnowledgeBase> create(@RequestBody KnowledgeBase knowledgeBase) {

        Long UserId = SecurityUtils.getUserId(); // 通过安全上下文取用户Id
        KnowledgeBase createdKb = knowledgeBaseService.createKnowledge(knowledgeBase, UserId);
        return Result.success(createdKb);
    }

    @GetMapping
    @Operation(summary = "获取当前用户的所有知识库")
    public Result<List<KnowledgeBase>> list() {
        Long UserId = SecurityUtils.getUserId(); // 通过安全上下文取用户Id
        List<KnowledgeBase> list = knowledgeBaseService.listByUserId(UserId);
        return Result.success(list);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除知识库")
    public Result<String> deleteKnowledgeBase(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId(); // 通过安全上下文取用户Id
        
        try {
            knowledgeBaseService.deleteKnowledgeBase(id, userId);
            return Result.success("知识库删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

}
