package testcode.pathtraversal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


@Controller
@RequestMapping("/UPLOAD")
public class Upload {

    private static final String UPLOADED_FOLDER = System.getProperty("user.dir") + "/src/main/resources/static/file/";
    private static Logger log = Logger.getLogger(Upload.class.getName());


    @GetMapping("/upload")
    public String uploadStatus() {
        return "upload";
    }

    @PostMapping("/uploadVul")
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择要上传的文件");
            return "redirect:upload";
        }

        try {
            byte[] bytes = file.getBytes();
            Path dir = Paths.get(UPLOADED_FOLDER);
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(path, bytes);
            log.info("[vul] 上传文件：" + path);
            redirectAttributes.addFlashAttribute("message",
                    "上传成功：" + path + " （不要将绝对路径暴露出来！）");

        } catch (Exception e) {
            return e.toString();
        }
        return "redirect:upload";
    }


    @PostMapping("/uploadSafe")
    public String singleFileUploadSafe(@RequestParam("file") MultipartFile file,
                                       RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择要上传的文件");
            return "redirect:upload";
        }

        try {
            byte[] bytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());

            // 获取文件后缀名，并且索引到最后一个，避免使用.jpg.jsp来绕过
            assert fileName != null;
            String Suffix = fileName.substring(fileName.lastIndexOf("."));

            String[] SuffixSafe = {".jpg", ".png", ".jpeg", ".gif", ".bmp", ".ico"};
            boolean flag = false;

            // 如果满足后缀名单，返回true
            for (String s : SuffixSafe) {
                if (Suffix.toLowerCase().equals(s)) {
                    flag = true;
                    break;
                }
            }

            log.info("[safe] 上传漏洞-白名单模式：" + fileName);

            if (!flag) {
                redirectAttributes.addFlashAttribute("message",
                        "只允许上传图片，[.jpg, .png, .jpeg, .gif, .bmp, .ico]");
            } else {
                Files.write(path, bytes);
                redirectAttributes.addFlashAttribute("message",
                        "上传文件成功：" + path + "");
            }

        } catch (Exception e) {
            return e.toString();
        }
        return "redirect:upload";
    }


}
