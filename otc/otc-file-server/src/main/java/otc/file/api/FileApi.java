package otc.file.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otc.bean.config.ConfigFile;
import otc.common.PayApiConstant;
import otc.file.feign.ConfigServiceClient;
import otc.file.util.StorageUtil;
import otc.result.Result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>文件处理接口</p>
 *
 * @author kent
 */
@RestController
@RequestMapping(PayApiConstant.File.FILE_API)
public class FileApi {
	Logger log = LoggerFactory.getLogger(FileApi.class);
	@Autowired
	ConfigServiceClient configServiceClientFeignImpl;
	/**
	 * #############################################
	 * 这个地方处理文件的保存和远程读取
	 */
	@Autowired StorageUtil storageUtil;
	@PostMapping(PayApiConstant.File.ADD_FILE)
	public String Storage(String file) throws IOException {
		log.info("【图片服务器接受图片上传】");
		String uploadGatheringCode = storageUtil.uploadGatheringCode(file);
		log.info("【文件名："+uploadGatheringCode+"】");
		return uploadGatheringCode;
	}
	@PostMapping(PayApiConstant.File.FIND_FILE)
	public  Resource loadAsResource(String id) {
		try {
			log.info("【图片查看接口调用，查看接口参数："+id+"】");
			Result config = configServiceClientFeignImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.LOCAL_STORAGE_PATH_BAK);
			String path = config.getResult().toString();
			log.info("【图片查看接口调用，查看图片服务本地路径："+path+"】");
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

	@RequestMapping("/fetch/{id:.+}")
	public ResponseEntity<Resource> fetch(@PathVariable String id) {
		try {
			String fileType = "image/jpeg";
			MediaType mediaType = MediaType.parseMediaType(fileType);
			Result config = configServiceClientFeignImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.LOCAL_STORAGE_PATH);
			String path = config.getResult().toString();
			Path file = Paths.get(path).resolve(id);
			Resource resource;
			resource = new UrlResource(file.toUri());
			if (resource == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok().contentType(mediaType).body(resource);
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		return null;
	}
	@RequestMapping("/bak/{id:.+}")
	public ResponseEntity<Resource> bak(@PathVariable String id) {
		try {
			String fileType = "image/jpeg";
			MediaType mediaType = MediaType.parseMediaType(fileType);
			Result config = configServiceClientFeignImpl.getConfig(ConfigFile.ALIPAY, ConfigFile.Alipay.LOCAL_STORAGE_PATH_BAK);
			String path = config.getResult().toString();
			Path file = Paths.get(path).resolve(id);
			Resource resource;
			resource = new UrlResource(file.toUri());
			if (resource == null) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok().contentType(mediaType).body(resource);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
