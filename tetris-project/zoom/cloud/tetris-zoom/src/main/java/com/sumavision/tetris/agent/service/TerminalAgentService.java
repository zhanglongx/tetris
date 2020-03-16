package com.sumavision.tetris.agent.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sumavision.tetris.agent.vo.CodecParamVO;
import com.sumavision.tetris.agent.vo.LocalPublishVO;
import com.sumavision.tetris.agent.vo.PassByParamVO;
import com.sumavision.tetris.agent.vo.PassByVO;
import com.sumavision.tetris.agent.vo.RemotePullVO;
import com.sumavision.tetris.agent.vo.RemoteVO;
import com.sumavision.tetris.agent.vo.ResourceVO;
import com.sumavision.tetris.agent.vo.ResponseResourceVO;
import com.sumavision.tetris.agent.vo.StopVO;
import com.sumavision.tetris.bvc.business.dispatch.TetrisDispatchService;
import com.sumavision.tetris.bvc.business.dispatch.bo.PassByBO;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.resouce.feign.resource.ResourceService;
import com.sumavision.tetris.resouce.feign.resource.UserBO;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;
import com.sumavision.tetris.zoom.ZoomDAO;
import com.sumavision.tetris.zoom.ZoomMemberPO;
import com.sumavision.tetris.zoom.ZoomMemberVO;
import com.sumavision.tetris.zoom.ZoomPO;
import com.sumavision.tetris.zoom.ZoomService;
import com.sumavision.tetris.zoom.ZoomVO;

@Service
@Transactional(rollbackFor = Exception.class)
public class TerminalAgentService {

	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private ZoomDAO zoomDao;
	
	@Autowired
	private ZoomService zoomService;
	
	@Autowired
	private TetrisDispatchService tetrisDispatchService;
	
	/**
	 * 获取用户能看的资源，包括用户、会议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月11日 下午2:58:23
	 * @return AgentResponse
	 */
	public ResponseResourceVO queryResources() throws Exception{
			
		ResponseResourceVO response = new ResponseResourceVO();
		
		UserVO user = userQuery.current();
		//用户信息
		List<UserBO> userBOs = resourceService.queryUsersByUserId(user.getId(), "");
		//会议信息
		List<ZoomPO> zooms = zoomDao.findAll();

		response.setResponse_users(new ArrayList<ResourceVO>());
		
		for(UserBO userBO: userBOs){
			ResourceVO userResource = new ResourceVO();
			userResource.setName(userBO.getName())
						.setNumber(userBO.getUserNo())
						.setType("user");
			response.getResponse_users().add(userResource);
		}
		
		for(ZoomPO zoom: zooms){
			ResourceVO meetingResource = new ResourceVO();
			meetingResource.setName(zoom.getName())
						   .setNumber(zoom.getCode())
						   .setType("meeting");
			response.getResponse_users().add(meetingResource);
		}

		return response;
	}
	
	/**
	 * Jv220呼叫用户<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午3:26:26
	 * @param String calledNo 被呼叫用户号码
	 * @return PassByVO
	 */
	public PassByVO callUser(String calledNo) throws Exception{
		
		UserVO callUser = userQuery.current();
		UserVO calledUser = userQuery.findByUserno(calledNo);
		
		//TODO:调用拉人建会接口
		ZoomVO zoom = null;
		
		PassByVO passBy = zoomVo2PassByVO(zoom);
		passBy.setOperate("call_user")
			  .setLocal_user_no(callUser.getUserno())
			  .setRemote_user_no(calledNo);
		
		return passBy;
	}
	
	/**
	 * Jv220加入会议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午3:50:34
	 * @param String code 会议号码
	 * @return PassByVO
	 */
	public PassByVO callMeeting(String code) throws Exception{
		
		UserVO callUser = userQuery.current();
		
		//调用加入会议接口
		ZoomVO zoom = zoomService.join(code, callUser.getNickname(), true, true);
		
		PassByVO passBy = zoomVo2PassByVO(zoom);
		passBy.setOperate("call_meeting")
			  .setLocal_user_no(callUser.getUserno());
		
		return passBy;
	}
	
	/**
	 * 发送passby协议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午4:22:19
	 * @param PassByVO passBy passby协议
	 */
	public void sendPassBy(PassByVO passBy) throws Exception{
		
		//调用消息服务的feign接口
		
	}
	
	/**
	 * jv220退会<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 上午9:03:57
	 * @param Long memberId 会议成员id
	 */
	public void exit(Long memberId) throws Exception{
		zoomService.exit(memberId);
	}
	
	/**
	 * jv220挂断<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 上午9:15:38
	 * @param String userno
	 */
	public void bye(String userno) throws Exception{
		
		//TODO:根据用户号码查询所有zoomMembers
		List<ZoomMemberPO> members = null;
		for(ZoomMemberPO member: members){
			zoomService.exit(member.getId());
		}
		
	}
	
	/**
	 * jv220恢复<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 上午9:30:34
	 * @param String userno 用户号码
	 * @return List<PassByVO>
	 */
	public List<PassByVO> resume(String userno) throws Exception{
		
		UserVO callUser = userQuery.current();
		
		List<PassByVO> passBys = new ArrayList<PassByVO>();
		
		//TODO：根据用户号码查询所在zoom信息
		List<ZoomVO> zooms = null;
		for(ZoomVO zoom: zooms){
			PassByVO passBy = zoomVo2PassByVO(zoom);
			passBy.setOperate("call_user")
				  .setLocal_user_no(callUser.getUserno());
			passBys.add(passBy);
		}
		
		return passBys;
	}
	
	/**
	 * zoom会议信息生成passby协议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午3:21:08
	 * @param ZoomVO zoom zoom会议信息
	 * @return PassByVO
	 */
	public PassByVO zoomVo2PassByVO(ZoomVO zoom) throws Exception{
		
		PassByVO passBy = new PassByVO();
		
		//自己编码
		ZoomMemberVO me = zoom.getMe();
		LocalPublishVO local = setLocal(me);
		
		//远端解码
		List<RemoteVO> remotes = new ArrayList<RemoteVO>();
		//主席
		int priority = 1;
		ZoomMemberVO chairman = zoom.getChairman();
		if(chairman.getShareScreen()){
			RemoteVO chairmanScreenRemote = setScreenRemote(chairman, priority, "open");
			remotes.add(chairmanScreenRemote);
			priority++;
		}
		RemoteVO chairmanRemote = setRemote(chairman, priority);
		remotes.add(chairmanRemote);
		priority++;
		
		//发言人
		List<ZoomMemberVO> spokesmans = zoom.getSpokesmem();
		for(ZoomMemberVO spokesman: spokesmans){
			if(spokesman.getShareScreen()){
				RemoteVO spokesmanScreenRemote = setScreenRemote(chairman, priority, "open");
				remotes.add(spokesmanScreenRemote);
				priority++;
			}
			RemoteVO spokesmanRemote = setRemote(spokesman, priority);
			remotes.add(spokesmanRemote);
			priority++;
		}
		
		//成员列表（包含主席和发言人） -- 不流调
//		List<ZoomMemberVO> members = zoom.getMembers();
//		for(ZoomMemberVO member: members){
//			RemoteVO memberRemote = setRemote(member, priority);
//			remotes.add(memberRemote);
//			priority++;
//		}
		
		//共享屏幕--不解析
		
		PassByParamVO param = new PassByParamVO().setLocal_publish(local)
											     .setRemote(new ArrayListWrapper<RemoteVO>().addAll(remotes).getList());
		
		passBy.setRemote_meeting_no(zoom.getCode())
			  .setLocal_user_identify(me.getId().toString())
		      .setParam(param);
		
		return passBy;
	}
	
	
	public void push(String code, JSONObject jsonObject, List<ZoomMemberVO> targets, String businessId) throws Exception{
		
		ZoomMemberVO changeMember = jsonObject.parseObject(JSON.toJSONString(jsonObject), ZoomMemberVO.class);
				
		
		String operate = "";
		
		if(businessId.equals("zoomJoin") || businessId.equals("zoomInvitation")){
			operate = "business_call_220_join";
		}
//		else if(businessId.equals("zoomStart")){
//			operate = "business_call_220_start";
//		}
		else if(businessId.equals("zoomStop")){
			operate = "business_stop_220";
		}else if(businessId.equals("zoomExit") || businessId.equals("zoomKickOut")){
			operate = "business_stop_220_channel";
		}else if(businessId.equals("zoomOpenShareScreen")){
			operate = "business_call_220_join";
		}else if(businessId.equals("zoomCloseShareScreen")){
			operate = "business_call_220_stop";
		}
		
		List<PassByBO> passBys = new ArrayList<PassByBO>();
		if(businessId.equals("zoomJoin") || businessId.equals("zoomInvitation")){
			passBys = generateCallMessage(code, changeMember, targets, operate, true, true);
		}
		
		//
		
		if(businessId.equals("zoomOpenShareScreen")){
			passBys = generateCallMessage(code, changeMember, targets, operate, true, false);
		}
		
		if(businessId.equals("zoomStop")){
			passBys = stopAllUsers(code, targets);
		}
		
		if(businessId.equals("zoomExit") || businessId.equals("zoomKickOut")){
			passBys = stopUser(code, changeMember, targets);
		}
		
		//调用消息服务
		tetrisDispatchService.dispatch(passBys);
		
	}
	
	/**
	 * 停止一个会议，停止所有用户<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 下午3:24:51
	 * @param code
	 * @param members
	 * @return
	 * @throws Exception
	 */
	public List<PassByBO> stopAllUsers(
			String code, 
			List<ZoomMemberVO> members) throws Exception{
		
		List<PassByBO> passBys = new ArrayList<PassByBO>();	
		
		for(ZoomMemberVO member: members){
			
			StopVO localStop = stopLocal(member, code);
			
			PassByBO localPassBy = new PassByBO().setLayer_id("layerId")
											     .setBundle_id(member.getBundleId())
											     .setPass_by_content(JSON.toJSONString(localStop));
			
			passBys.add(localPassBy);
		}
		
		return passBys;
	}
	
	/**
	 * 停止一个用户--1.停止该用户所有
	 *         2.停止其他用户中该用户的通道<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 下午3:14:21
	 * @param String code
	 * @param ZoomMemberVO changeMember
	 * @param List<ZoomMemberVO> targets
	 * @return List<PassByBO>
	 */
	public List<PassByBO> stopUser(
			String code, 
			ZoomMemberVO changeMember, 
			List<ZoomMemberVO> targets) throws Exception{
		
		List<PassByBO> passBys = new ArrayList<PassByBO>();	
		
		StopVO localStop = stopLocal(changeMember, code);
		
		PassByBO localPassBy = new PassByBO().setLayer_id("layerId")
										     .setBundle_id(changeMember.getBundleId())
										     .setPass_by_content(JSON.toJSONString(localStop));
		
		passBys.add(localPassBy);
		
		for(ZoomMemberVO target: targets){
			
			if(changeMember.getMyVideo() || changeMember.getMyAudio()){
				StopVO remoteStop = stopRemote(changeMember, target, code);
				PassByBO remotePassBy = new PassByBO().setLayer_id("layerId")
												      .setBundle_id(changeMember.getBundleId())
												      .setPass_by_content(JSON.toJSONString(remoteStop));
				passBys.add(remotePassBy);
			}
			if(changeMember.getShareScreen()){
				StopVO remoteScreenStop = stopScreenRemote(changeMember, target, code);
				PassByBO remoteScreenPassBy = new PassByBO().setLayer_id("layerId")
												      		.setBundle_id(changeMember.getBundleId())
												      		.setPass_by_content(JSON.toJSONString(remoteScreenStop));
				passBys.add(remoteScreenPassBy);
			}
		}
		
		return passBys;
	}
	
	/**
	 * 生成call消息--包含open和stop<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 下午1:57:25
	 * @param String code 会议号码
	 * @param ZoomMemberVO changeMember 变化的成员（加入、踢出、改变视音频屏幕共享）
	 * @param List<ZoomMemberVO> targets 接受消息方
	 * @param String operate 消息标识
	 * @param boolean shareScreen 是否下发屏幕共享
	 * @param boolean videoAudio 是否下发音视频
	 * @return List<PassByBO>
	 */
	public List<PassByBO> generateCallMessage(
			String code, 
			ZoomMemberVO changeMember, 
			List<ZoomMemberVO> targets, 
			String operate,
			boolean shareScreen,
			boolean videoAudio) throws Exception{
		
		List<PassByBO> passBys = new ArrayList<PassByBO>();
		
		List<RemoteVO> remotes = new ArrayList<RemoteVO>();
		
		int priority = 1;
		if(shareScreen){
			if(changeMember.getShareScreen()){
				RemoteVO changeMemberScreenRemote = setScreenRemote(changeMember, priority, "open");
				remotes.add(changeMemberScreenRemote);
				priority++;
			}
		}

		if(videoAudio){
			RemoteVO changeMemberRemote = setRemote(changeMember, priority);
			remotes.add(changeMemberRemote);
			priority++;
		}
		for(ZoomMemberVO target: targets){
			LocalPublishVO local = setLocal(target);
			PassByVO passByVO = new PassByVO().setOperate(operate)
											  //target的userno
											  .setLocal_user_no("")
											  .setLocal_user_identify(target.getId().toString())
											  .setRemote_meeting_no(code)
											  .setParam(new PassByParamVO().setLocal_publish(local)
													  					   .setRemote(new ArrayListWrapper<RemoteVO>().addAll(remotes).getList()));
			
			PassByBO passByBO = new PassByBO().setLayer_id("layerId")
											  .setBundle_id(target.getBundleId())
											  .setPass_by_content(JSON.toJSONString(passByVO));
			
			passBys.add(passByBO);
		}
		
		return passBys;
	}
	
	/**
	 * zoom成员信息生成本地编码协议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午2:41:59
	 * @param ZoomMemberVO member
	 * @return LocalPublishVO 协议信息
	 */
	public LocalPublishVO setLocal(ZoomMemberVO member) throws Exception{
		LocalPublishVO local = new LocalPublishVO().setStatus("open")
												   .setBundle_id(member.getBundleId())
												   .setVideo1_name(member.getVideoChannelId())
												   .setAudio1_name(member.getAudioChannelId())
												   .setCodec_param(new CodecParamVO());
		return local;
	}
	
	/**
	 * 成员信息生成远端解码协议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午2:21:45
	 * @param ZoomMemberVO member zoom成员信息
	 * @param int priority 优先级
	 * @return RemoteVO
	 */
	public RemoteVO setRemote(ZoomMemberVO member, int priority) throws Exception{
		
		RemoteVO remote = new RemoteVO().setLocal_channel_no(member.getId().toString())
										.setPriority(priority);

		RemotePullVO video = new RemotePullVO().setBundle_id(member.getBundleId())
											   //TODO:需要layerId
											   .setLayer_id("")
										   	   .setChannel_id(member.getVideoChannelId());	
		if(!member.getMyVideo()){
			video.setStatus("stop");
		}else{
			video.setStatus("open");
		}
		
		RemotePullVO audio = new RemotePullVO().setBundle_id(member.getBundleId())			
											   //TODO:需要layerId
								               .setLayer_id("")
								               .setChannel_id(member.getAudioChannelId());
		if(!member.getMyAudio()){
			audio.setStatus("close");
		}else{
			audio.setStatus("open");
		}
		
		remote.setRemote_audio_pull(audio)
		      .setRemote_video_pull(video);
		
		return remote;
	}
	
	/**
	 * zoom成员信息生成屏幕共享解码协议<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月12日 下午2:51:29
	 * @param ZoomMemberVO member zoom成员信息
	 * @param int priority 优先级
	 * @return RemoteVO
	 */
	public RemoteVO setScreenRemote(ZoomMemberVO member, int priority, String status) throws Exception{
		
		RemoteVO remote = new RemoteVO().setLocal_channel_no(new StringBufferWrapper().append(member.getId()).append("-screen").toString())
										.setPriority(priority)
										.setRemote_video_pull(new RemotePullVO().setStatus(status)
																				//TODO:需要layerId	
																				.setLayer_id("")
																				.setBundle_id(member.getBundleId())
																				//TODO:需要屏幕共享videoChannelId
																				.setChannel_id(""))
										.setRemote_audio_pull(new RemotePullVO().setStatus(status)
																				//TODO:需要layerId
												                                .setLayer_id("")
												                                .setBundle_id(member.getBundleId())
												                                //TODO:需要屏幕共享audioChannelId
												                                .setChannel_id(""));

		return remote;
	}
	
	/**
	 * 停止本地220业务<br/>
	 * <b>作者:</b>wjw<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2020年3月13日 下午2:35:12
	 * @param ZoomMemberVO member 成员信息
	 * @param String code 会议号码
	 * @return StopVO
	 */
	public StopVO stopLocal(ZoomMemberVO member, String code) throws Exception{
		
		StopVO local = new StopVO().setOperate("business_stop_220")
				   				   .setLocal_user_no("")
				   				   .setRemote_meeting_no(code)
				   				   .setLocal_user_identify(member.getId().toString());
		return local;
	}
	
	public StopVO stopRemote(ZoomMemberVO member, ZoomMemberVO target, String code) throws Exception{
		
		StopVO remote = new StopVO().setOperate("business_stop_220_channel")
				   				    .setLocal_user_no("")
				   				    .setRemote_meeting_no(code)
				   				    .setLocal_user_identify(target.getId().toString())
				   				    .setRemote_channel_no(member.getId().toString());
		return remote;
	}
	
	public StopVO stopScreenRemote(ZoomMemberVO member, ZoomMemberVO target, String code) throws Exception{
		
		StopVO screenRemote = new StopVO().setOperate("business_stop_220_channel")
				   				    	  .setLocal_user_no("")
				   				    	  .setRemote_meeting_no(code)
				   				    	  .setLocal_user_identify(target.getId().toString())
				   				    	  .setRemote_channel_no(new StringBufferWrapper().append(member.getId()).append("-screen").toString());
		return screenRemote;
	}
	
}
