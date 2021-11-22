package alipay.manage.api;

import alipay.config.redis.RedisUtil;
import alipay.manage.util.file.StorageUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import otc.result.Result;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SuppressWarnings({"unused", "restriction"})
@Controller
@RequestMapping("/storage")
public class StorageApi {
	Logger log = LoggerFactory.getLogger(StorageApi.class);
	@Autowired
	RedisUtil redisUtil;
	@PostMapping("/uploadPic")
	@ResponseBody
	public Result uploadPic(@RequestParam("file_data") MultipartFile[] files) throws IOException {
		log.info("上传图片");
		if (ObjectUtil.isNull(files)) {
			return Result.buildFailResult("请选择要上传的图片");
		}
		List<String> storageIds = new ArrayList<>();
		for (MultipartFile file : files) {
			String addFile = addFile(file);
            storageIds.add(addFile);
        }
		for (String orderid : storageIds){
			log.info(orderid);
		}
        return Result.buildSuccessResult(storageIds);
    }
    
    /**
	 * <p>查看图片接口</p>
	 * @param id
	 * @return
	 */
	@GetMapping("/fetch/{id:.+}")
	public ResponseEntity<Resource> fetch(@PathVariable String id) {
		String fileType = "image/jpeg";
		MediaType mediaType = MediaType.parseMediaType(fileType);
		Resource file = loadAsResource(id);
		log.info("【查看图片id：" + id + "】");
		if (file == null) {
			file = loadAsResource(id);
			if (file == null) {
				return ResponseEntity.notFound().build();
			}
		}
		return ResponseEntity.ok().contentType(mediaType).body(file);
	}
	/**
	   * <p>查看图片接口</p>
	   * @param id
	   * @return
	   */
	  @GetMapping("/imgbak/{id:.+}")
	  public ResponseEntity<Resource> imgbak(@PathVariable String id) {
		  String fileType = "image/jpeg";
		  MediaType mediaType = MediaType.parseMediaType(fileType);
		  Resource file = loadAsResource(id);
		  log.info("【查看图片id：" + id + "】");
		  if (file == null) {
			  file = loadAsResource(id);
			  if (file == null) {
				  return ResponseEntity.notFound().build();
			  }
		  }
		  return ResponseEntity.ok().contentType(mediaType).body(file);
	  }
    
	String addFile(MultipartFile file) {


	    try {
	    	InputStream inputStream = file.getInputStream(); 
			long size = file.getSize();
			String contentType = file.getContentType();
			String originalFilename = file.getOriginalFilename();
			byte[] data = null;
			log.info("【文件流：" + inputStream + "】");
			log.info("【文件长度：" + size + "】");
			log.info("【文件类型：" + contentType + "】");
			log.info("【文件名字：" + originalFilename + "】");
			data = new byte[inputStream.available()];
			inputStream.read(data);
			inputStream.close();
			String encode = Base64.encodeBase64String(Objects.requireNonNull(data));
			String storageId = StorageUtil.uploadGatheringCode(encode);
			log.info("storageId ::: " + storageId);
			return storageId;
		} catch (IOException e) {
			return "失败";
		}
	}

	public Resource loadAsResource(String id) {
		try {
			log.info("【图片查看接口调用，查看接口参数：" + id + "】");
			String path = "/img";
			log.info("【图片查看接口调用，查看图片服务本地路径：" + path + "】");
			Path file = Paths.get(path).resolve(id);
			Resource resource;
			resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				return null;
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}


}
