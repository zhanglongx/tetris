package com.sumavision.tetris.resouce.feign.bundle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sumavision.tetris.mvc.ext.response.parser.JsonBodyResponseParser;

@Service
@Transactional(rollbackFor = Exception.class)
public class BundleService {

	@Autowired
	private BundleFeign bundleFeign;

	public List<BundleVO> queryTranscodeDevice() throws Exception {

		return JsonBodyResponseParser.parseArray(bundleFeign.queryTranscodeDevice(), BundleVO.class);

	}

}
