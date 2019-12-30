package com.sumavision.tetris.resouce.feign.bundle;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.config.feign.FeignConfiguration;

@FeignClient(name = "suma-venus-resource", configuration = FeignConfiguration.class, path = "suma-venus-resource")
public interface BundleFeign {

	/**
	 * 查询转码设备<br/>
	 */
	@RequestMapping(value = "/feign/bundle/queryTranscodeDevice", method = RequestMethod.POST)
	public JSONObject queryTranscodeDevice() throws Exception;

	/**
	 * 查询设备的授权<br/>
	 */
	@RequestMapping(value = "/feign/bundle/queryAuth", method = RequestMethod.POST)
	public JSONObject queryAuth(String bundle_id) throws Exception;

}
