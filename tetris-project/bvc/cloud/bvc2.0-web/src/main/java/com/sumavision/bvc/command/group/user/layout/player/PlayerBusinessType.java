package com.sumavision.bvc.command.group.user.layout.player;

import com.sumavision.tetris.orm.exception.ErrorTypeException;

/**
 * @ClassName: 转发的业务类型：协同，专向，各种点播，
 * @author zsy
 * @date 2019年9月23日 上午10:11:22 
 */
public enum PlayerBusinessType {
	
	//考虑跟ConfigType.java合并
	
	NONE("未使用", "none"),
	BASIC_COMMAND("普通指挥业务", "command"),//主席用commandMember，其他成员用command
	CHAIRMAN_BASIC_COMMAND("主席的普通指挥业务", "commandMember"),//主席用commandMember，其他成员用command
	COOPERATE_COMMAND("协同指挥业务", "cooperation"),
	SECRET_COMMAND("专向指挥业务", "secret"),
//	DEMAND_FORWARD("指挥转发点播", ""),//废弃
	COMMAND_FORWARD_DEVICE("指挥转发设备", "commandForwardDevice"),
	COMMAND_FORWARD_FILE("指挥转发文件", "commandForwardFile"),
	PLAY_COMMAND_RECORD("播放指挥录像", "vodRecordFile"),
	PLAY_FILE("点播文件", "vodResourceFile"),
	PLAY_USER("点播用户", "vodUser"),
	PLAY_USER_ONESELF("本地视频", "vodUser"),//看自己的编码器，给客户端的code也是vodUser，客户端按照普通点播来处理
	PLAY_DEVICE("点播设备", "vodDevice"),
	PLAY_RECORD("播放录像", ""),
	USER_CALL("用户呼叫", "callUser"),
	USER_VOICE("语音对讲", "voiceIntercom");
	
	private String name;
	
	private String code;
	
	private PlayerBusinessType(String name, String code){
		this.name = name;
		this.code = code;
	}

	public String getName(){
		return this.name;
	}
	
	public String getCode() {
		return code;
	}

	/**
	 * @Title: 根据名称获取转发目的类型 
	 * @param name 名称
	 * @throws Exception 
	 * @return ForwardDstType 转发目的类型
	 */
	public static PlayerBusinessType fromName(String name) throws Exception{
		PlayerBusinessType[] values = PlayerBusinessType.values();
		for(PlayerBusinessType value:values){
			if(value.getName().equals(name)){
				return value;
			}
		}
		throw new ErrorTypeException("name", name);
	}
	
}