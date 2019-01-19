package com.sumavision.tetris.user;

import com.sumavision.tetris.commons.util.date.DateUtil;
import com.sumavision.tetris.mvc.converter.AbstractBaseVO;

/**
 * 登录用户信息<br/>
 * <p>特殊注明：用uuid字段存储userId</p>
 * <b>作者:</b>lvdeyang<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2018年11月19日 下午4:51:51
 */
public class UserVO extends AbstractBaseVO<UserVO, UserPO>{

	/** 用户名 */
	private String username;
	
	/** 用户昵称 */
	private String nickname;
	
	/** 头像 */
	private String icon;
	
	/** 用户状态 */
	private String status;
	
	/** 手机号 */
	private String mobile;
	
	/** 邮箱 */
	private String mail;
	
	/** 是否是自动生成的 */
	private boolean autoGeneration;
	
	/** 用户分类 */
	private String classify;
	
	/** 发送给当前用户的信息数目 */
	private Integer numbersOfMessage;
	
	/** 用户隶属组织id */
	private String groupId;
	
	/** 用户隶属组织名称 */
	private String groupName;
	
	/** 素材库文件夹id */
	private Long rootFolderId;
	
	/** 素材库文件夹名称 */
	private String rootFolderName;

	public String getUsername() {
		return username;
	}

	public UserVO setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getNickname() {
		return nickname;
	}

	public UserVO setNickname(String nickname) {
		this.nickname = nickname;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public UserVO setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public UserVO setStatus(String status) {
		this.status = status;
		return this;
	}
	
	public String getMobile() {
		return mobile;
	}

	public UserVO setMobile(String mobile) {
		this.mobile = mobile;
		return this;
	}

	public String getMail() {
		return mail;
	}

	public UserVO setMail(String mail) {
		this.mail = mail;
		return this;
	}

	public boolean isAutoGeneration() {
		return autoGeneration;
	}

	public UserVO setAutoGeneration(boolean autoGeneration) {
		this.autoGeneration = autoGeneration;
		return this;
	}

	public String getClassify() {
		return classify;
	}

	public UserVO setClassify(String classify) {
		this.classify = classify;
		return this;
	}

	public Integer getNumbersOfMessage() {
		return numbersOfMessage;
	}

	public UserVO setNumbersOfMessage(Integer numbersOfMessage) {
		this.numbersOfMessage = numbersOfMessage;
		return this;
	}

	public String getGroupId() {
		return groupId;
	}

	public UserVO setGroupId(String groupId) {
		this.groupId = groupId;
		return this;
	}

	public String getGroupName() {
		return groupName;
	}

	public UserVO setGroupName(String groupName) {
		this.groupName = groupName;
		return this;
	}
	
	public Long getRootFolderId() {
		return rootFolderId;
	}

	public UserVO setRootFolderId(Long rootFolderId) {
		this.rootFolderId = rootFolderId;
		return this;
	}

	public String getRootFolderName() {
		return rootFolderName;
	}

	public UserVO setRootFolderName(String rootFolderName) {
		this.rootFolderName = rootFolderName;
		return this;
	}

	@Override
	public UserVO set(UserPO entity) throws Exception {
		this.setId(entity.getId())
			.setUuid(entity.getUuid())
			.setUpdateTime(entity.getUpdateTime()==null?"":DateUtil.format(entity.getUpdateTime(), DateUtil.dateTimePattern))
			.setUsername(entity.getUsername())
			.setNickname(entity.getNickname())
			.setMobile(entity.getMobile())
			.setMail(entity.getMail())
			.setIcon(entity.getIcon())
			.setStatus(entity.getStatus()==null?"":entity.getStatus().getName())
			.setAutoGeneration(entity.isAutoGeneration());
		return this;
	}
	
}