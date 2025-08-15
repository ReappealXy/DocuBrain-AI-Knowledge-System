package xin.rexy.docubrain.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xin.rexy.docubrain.common.Result;
import xin.rexy.docubrain.common.SecurityUtils;
import xin.rexy.docubrain.entity.DocumentInfo;
import xin.rexy.docubrain.service.DocumentInfoService;

/**
 * <p>
 * 文档信息表 前端控制器
 * </p>
 *
 * @author Rexy
 * @since 2025-08-15
 */
@RestController
@RequestMapping("/documentInfo")
@Tag(name = "文档管理模块", description = "文档信息表")
public class DocumentInfoController {

    @Autowired
    private DocumentInfoService documentInfoService;

    @PostMapping(value = "/update",consumes = "multipart/form-data")
    @Operation(summary = "上传并索性文档")
    public Result<?> update(
            @Parameter(description = "要上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "所属知识库的ID") @RequestParam("knowledgeBaseId") Long knowledgeBaseId
    ) {
        Long userId = SecurityUtils.getUserId();
        if(file.isEmpty()){
            return Result.error("上传文件不能为空");
        }
        documentInfoService.uploadAndIndex(file, knowledgeBaseId, userId);
        return Result.success("文件已经上传并且开始索引处理");
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除文档")
    public Result<String> deleteDocument(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        
        try {
            documentInfoService.deleteDocument(id, userId);
            return Result.success("文档删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/reprocess/{knowledgeBaseId}")
    @Operation(summary = "重新处理知识库中的PENDING文档")
    public Result<String> reprocessPendingDocuments(@PathVariable Long knowledgeBaseId) {
        Long userId = SecurityUtils.getUserId();
        
        try {
            documentInfoService.reprocessPendingDocuments(knowledgeBaseId, userId);
            return Result.success("文档重新处理完成");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("重新处理失败: " + e.getMessage());
        }
    }
}
