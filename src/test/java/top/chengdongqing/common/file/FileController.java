package top.chengdongqing.common.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.chengdongqing.common.file.entity.DownloadFile;
import top.chengdongqing.common.file.entity.FileMetadata;
import top.chengdongqing.common.file.uploader.Uploader;
import top.chengdongqing.common.kit.PathVariableKit;
import top.chengdongqing.common.kit.Ret;
import top.chengdongqing.common.renderer.StreamRenderer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Luyao
 */
@RestController
@Api(tags = "文件相关控制器")
@RequestMapping("/file")
public class FileController {

    @Autowired
    private Uploader uploader;
    @Autowired
    private FileManagerFactory managerFactory;

    @PostMapping
    @ApiOperation("上传图片")
    public Ret<FileMetadata> upload(@ApiParam("图片文件") @RequestPart MultipartFile file,
                                    @ApiParam("文件类型") @RequestParam FileType type) {
        try {
            FileMetadata metadata = uploader.uploadImage(file, type);
            return Ret.ok(metadata);
        } catch (FileException e) {
            return Ret.fail(e.getMessage());
        }
    }

    @GetMapping("/**")
    @ApiOperation("下载文件")
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String originalName = "test.jpg";
            String fileKey = PathVariableKit.getPathVariable(request);
            DownloadFile file = managerFactory.getManager().download(fileKey);
            StreamRenderer.of(file.stream(), file.length(), originalName).render();
        } catch (FileException e) {
            response.sendError(404);
        }
    }

    @DeleteMapping("/**")
    @ApiOperation("删除文件")
    public Ret<Void> delete(HttpServletRequest request) {
        try {
            String fileKey = PathVariableKit.getPathVariable(request);
            managerFactory.getManager().delete(fileKey);
            return Ret.ok();
        } catch (FileException e) {
            return Ret.fail("文件删除失败");
        }
    }
}
