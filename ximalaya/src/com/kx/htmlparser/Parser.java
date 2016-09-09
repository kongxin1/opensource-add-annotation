package com.kx.htmlparser;

import java.util.List;

import com.kx.domain.ClawerMessage;

public interface Parser {
	public List<ClawerMessage> parse(String baseURL, ClawerMessage msg) throws Exception;
}
