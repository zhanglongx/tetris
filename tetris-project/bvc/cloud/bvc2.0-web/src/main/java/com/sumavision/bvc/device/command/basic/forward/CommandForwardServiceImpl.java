package com.sumavision.bvc.device.command.basic.forward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suma.venus.resource.pojo.BundlePO;
import com.suma.venus.resource.service.ResourceService;
import com.sumavision.bvc.command.group.basic.CommandGroupAvtplGearsPO;
import com.sumavision.bvc.command.group.basic.CommandGroupAvtplPO;
import com.sumavision.bvc.command.group.basic.CommandGroupMemberPO;
import com.sumavision.bvc.command.group.basic.CommandGroupPO;
import com.sumavision.bvc.command.group.dao.CommandGroupDAO;
import com.sumavision.bvc.command.group.dao.CommandGroupForwardDemandDAO;
import com.sumavision.bvc.command.group.enumeration.ForwardDemandBusinessType;
import com.sumavision.bvc.command.group.enumeration.ForwardDemandStatus;
import com.sumavision.bvc.command.group.enumeration.ForwardDstType;
import com.sumavision.bvc.command.group.enumeration.GroupStatus;
import com.sumavision.bvc.command.group.forward.CommandGroupForwardDemandPO;
import com.sumavision.bvc.command.group.user.layout.player.CommandGroupUserPlayerPO;
import com.sumavision.bvc.command.group.user.layout.player.PlayerBusinessType;
import com.sumavision.bvc.device.command.basic.CommandBasicServiceImpl;
import com.sumavision.bvc.device.command.cast.CommandCastServiceImpl;
import com.sumavision.bvc.device.command.common.CommandCommonServiceImpl;
import com.sumavision.bvc.device.command.common.CommandCommonUtil;
import com.sumavision.bvc.device.group.bo.CodecParamBO;
import com.sumavision.bvc.device.group.bo.LogicBO;
import com.sumavision.bvc.device.group.enumeration.ChannelType;
import com.sumavision.bvc.device.group.service.test.ExecuteBusinessProxy;
import com.sumavision.bvc.device.monitor.live.DstDeviceType;
import com.sumavision.bvc.device.monitor.playback.exception.ResourceNotExistException;
import com.sumavision.bvc.resource.dao.ResourceBundleDAO;
import com.sumavision.bvc.resource.dao.ResourceChannelDAO;
import com.sumavision.bvc.resource.dto.ChannelSchemeDTO;
import com.sumavision.tetris.commons.exception.BaseException;
import com.sumavision.tetris.commons.exception.code.StatusCode;
import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.websocket.message.WebsocketMessageService;
import com.sumavision.tetris.websocket.message.WebsocketMessageType;
import com.sumavision.tetris.websocket.message.WebsocketMessageVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 
* @ClassName: CommandForwardServiceImpl 
* @Description: 指挥转发业务
* @author zsy
* @date 2019年11月14日 上午09:56:48 
*
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class CommandForwardServiceImpl {
	
	/** synchronized锁的前缀 */
	private static final String lockPrefix = "command-group-";
	
	@Autowired
	private CommandGroupDAO commandGroupDao;
	
	@Autowired
	private CommandGroupForwardDemandDAO commandGroupForwardDemandDao;
	
	@Autowired
	private CommandCommonServiceImpl commandCommonServiceImpl;
	
	@Autowired
	private CommandBasicServiceImpl commandBasicServiceImpl;
	
	@Autowired
	private CommandCastServiceImpl commandCastServiceImpl;
	
	@Autowired
	private CommandCommonUtil commandCommonUtil;
	
	@Autowired
	private ExecuteBusinessProxy executeBusiness;
	
	@Autowired
	private ResourceBundleDAO resourceBundleDao;
	
	@Autowired
	private ResourceChannelDAO resourceChannelDao;
	
	@Autowired
	private WebsocketMessageService websocketMessageService;
	
	@Autowired
	private ResourceService resourceService;

	/**
	 * 指挥转发设备<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 下午2:02:44
	 * @param groupId
	 * @param bundleIds 源
	 * @param userIds 目的
	 * @return
	 * @throws Exception
	 */
	public List<ForwardReturnBO> forwardDevice(Long groupId, List<String> bundleIds, List<Long> userIds) throws Exception{
		
		synchronized (new StringBuffer().append(lockPrefix).append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
			}
			
			List<ForwardReturnBO> result = new ArrayList<ForwardReturnBO>();
			
			Set<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			Long creatorUserId = chairmanMember.getUserId();
			CommandGroupAvtplPO g_avtpl = group.getAvtpl();
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			
			List<CommandGroupMemberPO> dstMembers = commandCommonUtil.queryMembersByUserIds(members, userIds);
			List<CommandGroupForwardDemandPO> demands = group.getForwardDemands();
			List<CommandGroupForwardDemandPO> newDemands = new ArrayList<CommandGroupForwardDemandPO>();
			if(demands == null){
				group.setForwardDemands(new ArrayList<CommandGroupForwardDemandPO>());
				demands = group.getForwardDemands();
			}
			
			//从bundleId列表查询所有的bundlePO
			List<BundlePO> srcBundleEntities = resourceBundleDao.findByBundleIds(bundleIds);
			
			//从bundleId列表查询所有的视频编码1通道
			List<ChannelSchemeDTO> videoEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.VIDEOENCODE1.getChannelId());
			
			//从bundleId列表查询所有的音频编码1通道
			List<ChannelSchemeDTO> audioEncode1Channels = resourceChannelDao.findByBundleIdsAndChannelId(bundleIds, ChannelType.AUDIOENCODE1.getChannelId());
			
			for(CommandGroupMemberPO dstMember : dstMembers){
				for(BundlePO bundle : srcBundleEntities){
					String srcVideoChannelId = null;
					String srcAudioChannelId = null;
					//遍历视频通道
					for(ChannelSchemeDTO videoChannel : videoEncode1Channels){
						if(bundle.getBundleId().equals(videoChannel.getBundleId())){
							srcVideoChannelId = videoChannel.getChannelId();
							break;
						}
					}					
					//遍历音频通道
					for(ChannelSchemeDTO audioChannel : audioEncode1Channels){
						if(bundle.getBundleId().equals(audioChannel.getBundleId())){
							srcAudioChannelId = audioChannel.getChannelId();
							break;
						}
					}
					//建立 CommandGroupForwardDemandPO ，发送消息
					CommandGroupForwardDemandPO demand = new CommandGroupForwardDemandPO(
							ForwardDemandBusinessType.FORWARD_DEVICE,
							ForwardDemandStatus.WAITING_FOR_RESPONSE,
							ForwardDstType.USER,
							dstMember.getId(),
							dstMember.getUserName(),
							bundle.getBundleId(),
							bundle.getBundleName(),
							bundle.getBundleType(),
							bundle.getAccessNodeUid(),
							srcVideoChannelId,
							"VenusVideoIn",//videoBaseType,
							bundle.getBundleId(),
							bundle.getBundleName(),
							bundle.getBundleType(),
							bundle.getAccessNodeUid(),
							srcAudioChannelId,
							"VenusAudioIn",//String audioBaseType,
							null,//member.getDstBundleId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstVideoChannelId(),
							"VenusVideoOut",//String dstVideoBaseType,
							null,//member.getDstAudioChannelId(),
							null,//member.getDstBundleName(),
							null,//member.getDstBundleType(),
							null,//member.getDstLayerId(),
							null,//member.getDstAudioChannelId(),
							"VenusAudioOut",//String dstAudioBaseType,
							creatorUserId,
							g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					demand.setGroup(group);
					newDemands.add(demand);
				}
			}
			//save获得id
			demands.addAll(newDemands);
			commandGroupForwardDemandDao.save(newDemands);
			commandGroupDao.save(group);
			
			for(CommandGroupForwardDemandPO demand : newDemands){
				result.add(new ForwardReturnBO().setByDevice(demand));
			}
			
			//发消息（立即消费？）
			List<Long> consumeIds = new ArrayList<Long>();
			for(CommandGroupMemberPO dstMember : dstMembers){
				List<Long> dstMemberIds = new ArrayListWrapper<Long>().add(dstMember.getId()).getList();
				//查出该用户的新点播转发
				List<CommandGroupForwardDemandPO> memberDemands = commandCommonUtil.queryForwardDemandsByDstmemberIds(newDemands, dstMemberIds, null, null);
				JSONArray forwards = new JSONArray();
				for(CommandGroupForwardDemandPO demand : memberDemands){
					JSONObject forward = new JSONObject();
					forward.put("id", demand.getId().toString());
					forward.put("name", demand.getVideoBundleName());
					forwards.add(forward);
				}
				Map<String, Object> map = new HashMapWrapper<String, Object>()
						.put("businessType", PlayerBusinessType.COMMAND_FORWARD_DEVICE.getCode())
						.put("businessId", group.getId().toString())
						.put("forwards", forwards)
						.put("businessInfo", group.getName() + "给你转发了直播视频")
						.getMap();
				
				WebsocketMessageVO ws = websocketMessageService.send(dstMember.getUserId(), JSON.toJSONString(map), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName());
				consumeIds.add(ws.getId());
			}
			websocketMessageService.consumeAll(consumeIds);//（立即消费？）
			
			return result;
		}
	}

	/**
	 * 指挥转发文件<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 下午2:03:20
	 * @param groupId
	 * @param resourceIds 源文件
	 * @param userIds 目的
	 * @return
	 * @throws Exception
	 */
	public List<ForwardReturnBO> forwardFile(Long groupId, List<String> resourceIds, List<Long> userIds) throws Exception{
		
		synchronized (new StringBuffer().append(lockPrefix).append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
			}
			
			//查找资源
			JSONArray files = new JSONArray();
			for(String resourceId : resourceIds){
				JSONObject file = resourceService.queryOnDemandResourceById(resourceId);
				if(file == null) throw new ResourceNotExistException(resourceId);
				files.add(file);				
			}
			
			List<ForwardReturnBO> result = new ArrayList<ForwardReturnBO>();
			
			Set<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			Long creatorUserId = chairmanMember.getUserId();
			CommandGroupAvtplPO g_avtpl = group.getAvtpl();
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			
			List<CommandGroupMemberPO> dstMembers = commandCommonUtil.queryMembersByUserIds(members, userIds);
			List<CommandGroupForwardDemandPO> demands = group.getForwardDemands();
			List<CommandGroupForwardDemandPO> newDemands = new ArrayList<CommandGroupForwardDemandPO>();
			if(demands == null){
				group.setForwardDemands(new ArrayList<CommandGroupForwardDemandPO>());
				demands = group.getForwardDemands();
			}
			
			for(CommandGroupMemberPO dstMember : dstMembers){
				for(int i=0; i<files.size(); i++){
					JSONObject file = files.getJSONObject(i);
					String resourceId = file.getString("resourceId");
					String resourceName = file.getString("name");
					String previewUrl = file.getString("previewUrl");
					
					//建立 CommandGroupForwardDemandPO ，发送消息
					CommandGroupForwardDemandPO demand = new CommandGroupForwardDemandPO(
							ForwardDemandBusinessType.FORWARD_FILE,
							ForwardDemandStatus.WAITING_FOR_RESPONSE,
							ForwardDstType.USER,
							dstMember.getId(),
							dstMember.getUserName(),
							resourceId,
							resourceName,
							previewUrl,
							creatorUserId,
							g_avtpl.getId(),//Long avTplId,
							currentGear.getId(),//Long gearId,
							DstDeviceType.WEBSITE_PLAYER,
							null,//LiveType type,
							null,//Long osdId,
							null//String osdUsername);
							);
					demand.setGroup(group);
					newDemands.add(demand);
				}
			}
			//save获得id
			demands.addAll(newDemands);
			commandGroupForwardDemandDao.save(newDemands);
			commandGroupDao.save(group);
			
			for(CommandGroupForwardDemandPO demand : newDemands){
				result.add(new ForwardReturnBO().setByFile(demand));
			}
			
			//发消息（立即消费？）
			List<Long> consumeIds = new ArrayList<Long>();
			for(CommandGroupMemberPO dstMember : dstMembers){
				List<Long> dstMemberIds = new ArrayListWrapper<Long>().add(dstMember.getId()).getList();
				//查出该用户的新点播转发
				List<CommandGroupForwardDemandPO> memberDemands = commandCommonUtil.queryForwardDemandsByDstmemberIds(newDemands, dstMemberIds, null, null);
				JSONArray forwards = new JSONArray();
				for(CommandGroupForwardDemandPO demand : memberDemands){
					JSONObject forward = new JSONObject();
					forward.put("id", demand.getId().toString());
					forward.put("name", demand.getResourceName());
					forwards.add(forward);
				}
				Map<String, Object> map = new HashMapWrapper<String, Object>()
						.put("businessType", PlayerBusinessType.COMMAND_FORWARD_FILE.getCode())
						.put("businessId", group.getId().toString())
						.put("forwards", forwards)
						.put("businessInfo", group.getName() + "给你转发了文件视频")
						.getMap();
				
				WebsocketMessageVO ws = websocketMessageService.send(dstMember.getUserId(), JSON.toJSONString(map), WebsocketMessageType.COMMAND, chairmanMember.getUserId(), chairmanMember.getUserName());
				consumeIds.add(ws.getId());
			}
			websocketMessageService.consumeAll(consumeIds);//（立即消费？）
			
			return result;
		}
	}

	/**
	 * 同意转发，包括设备和文件<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 上午9:10:22
	 * @param userId 后续优化：这个参数应该去掉，从demandIds里取出dstMemberId
	 * @param groupId
	 * @param demandIds
	 * @return ForwardDeviceAgreeReturnBO 或 ForwardFileAgreeReturnBO
	 * @throws Exception
	 */
	public List<Object> forwardAgree(Long userId, Long groupId, List<Long> demandIds) throws Exception{
		
		synchronized (new StringBuffer().append(lockPrefix).append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
			}
			
			List<Object> result = new ArrayList<Object>();
			
			Set<CommandGroupMemberPO> members = group.getMembers();
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			chairmanMember.getUserId();
			group.getAvtpl();
			
			CommandGroupMemberPO dstMember = commandCommonUtil.queryMemberByUserId(members, userId);
			List<CommandGroupForwardDemandPO> demands = commandGroupForwardDemandDao.findAll(demandIds);
			
			//找播放器
			List<CommandGroupUserPlayerPO> players = commandCommonServiceImpl.userChoseUsefulPlayers(dstMember.getUserId(), PlayerBusinessType.COMMAND_FORWARD_DEVICE, demandIds.size(), true);
			int usefulPlayersCount = players.size();
			if(usefulPlayersCount < demandIds.size()){
				for(CommandGroupForwardDemandPO demand : demands){
					demand.setExecuteStatus(ForwardDemandStatus.NO_AVAILABLE_PLAYER);
				}
				commandGroupDao.save(group);
				throw new BaseException(StatusCode.FORBIDDEN, "没有足够的播放器");
			}
			
			List<CommandGroupForwardDemandPO> needDemands = new ArrayList<CommandGroupForwardDemandPO>();
			List<CommandGroupUserPlayerPO> needPlayers = new ArrayList<CommandGroupUserPlayerPO>();
			for(CommandGroupForwardDemandPO demand : demands){
				if(usefulPlayersCount > 0){
					demand.setExecuteStatus(ForwardDemandStatus.DONE);
					CommandGroupUserPlayerPO player = players.get(players.size() - usefulPlayersCount);
					player.setBusinessId(group.getId().toString() + "-" + demand.getId().toString());
					if(demand.getDemandType().equals(ForwardDemandBusinessType.FORWARD_DEVICE)){
						//转发设备
						player.setBusinessName(group.getName() + "转发" + demand.getVideoBundleName() + "设备");
						player.setPlayerBusinessType(PlayerBusinessType.COMMAND_FORWARD_DEVICE);
						ForwardDeviceAgreeReturnBO bo = new ForwardDeviceAgreeReturnBO().set(player);
						result.add(bo);
					}else if(demand.getDemandType().equals(ForwardDemandBusinessType.FORWARD_FILE)){
						//转发文件
						player.setBusinessName(group.getName() + "转发" + demand.getResourceName() + "文件");
						player.setPlayerBusinessType(PlayerBusinessType.COMMAND_FORWARD_FILE);
						player.setPlayUrl(demand.getPlayUrl());
						ForwardFileAgreeReturnBO bo = new ForwardFileAgreeReturnBO().set(player);
						result.add(bo);
					}
					
					//给转发设置目的
					demand.setDstPlayer(player);
					player.setMember(dstMember);
					usefulPlayersCount--;
					
					needDemands.add(demand);
					needPlayers.add(player);
				}else{
					demand.setExecuteStatus(ForwardDemandStatus.NO_AVAILABLE_PLAYER);
				}
			}
			
			commandGroupDao.save(group);
			
			//生成logic下发协议
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
			LogicBO logic = commandBasicServiceImpl.openBundle(null, needDemands, needPlayers, null, null, codec, group.getUserId());
			LogicBO logicCastDevice = commandCastServiceImpl.openBundleCastDevice(needDemands, null, null, null, null, codec, group.getUserId());
			logic.merge(logicCastDevice);
			
			StringBufferWrapper description = new StringBufferWrapper()
					.append(dstMember.getUserName()).append(" 同意接收转发");
			executeBusiness.execute(logic, description.toString());
			
			return result;
		}
	}
	
	/**
	 * 拒绝转发，包括设备和文件<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 下午2:05:17
	 * @param groupId
	 * @param demandIds
	 */
	public void forwardRefuse(Long groupId, List<Long> demandIds) {
		
		synchronized (new StringBuffer().append(lockPrefix).append(groupId).toString().intern()) {
			
			List<CommandGroupForwardDemandPO> demands = commandGroupForwardDemandDao.findAll(demandIds);
			for(CommandGroupForwardDemandPO demand : demands){
				demand.setExecuteStatus(ForwardDemandStatus.DENY);
			}
			commandGroupForwardDemandDao.save(demands);
			
		}
	}

	/**
	 * 主席停止一个指挥中的多个转发<br/>
	 * <p>详细描述</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 下午2:05:55
	 * @param groupId
	 * @param demandIds
	 * @throws Exception
	 */
	public void stopByChairman(Long groupId, List<Long> demandIds) throws Exception{
		
		//<userId, [{serial:屏幕序号}]>
		HashMap<Long, JSONArray> result = stopDemands(groupId, demandIds);
				
		//发消息
		CommandGroupPO group = commandGroupDao.findOne(groupId);
		List<Long> consumeIds = new ArrayList<Long>();
		for(Map.Entry<Long, JSONArray> entry: result.entrySet()){
			System.out.println("Key: "+ entry.getKey()+ " Value: "+entry.getValue());
			Long userId = entry.getKey();
			JSONArray splits = entry.getValue();
			JSONObject message = new JSONObject();
			message.put("businessType", "commandForwardStop");
			message.put("businessId", group.getId().toString());
			message.put("businessInfo", group.getName() + " 停止了转发");
			message.put("splits", splits);
			WebsocketMessageVO ws = websocketMessageService.send(userId, message.toJSONString(), WebsocketMessageType.COMMAND, group.getUserId(), group.getUserName());
			consumeIds.add(ws.getId());
		}
		websocketMessageService.consumeAll(consumeIds);
	}

	/**
	 * 成员停止多个转发<br/>
	 * <p>支持不同指挥中的转发</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 下午2:07:38
	 * @param userId
	 * @param demandIds
	 * @return
	 * @throws Exception
	 */
	public JSONArray stopByMember(Long userId, List<Long> demandIds) throws Exception{
		
		JSONArray splits = new JSONArray();
		List<Long> groupIds = commandGroupDao.findAllIdsByDemandIds(demandIds);
		
		for(Long groupId : groupIds){
			//这里把demandIds全部传入，因为有groupId限制，所以demandIds传多了也没事
			HashMap<Long, JSONArray> result = stopDemands(groupId, demandIds);
			JSONArray splist1 = result.get(userId);
			if(splist1 != null){
				splits.addAll(splist1);
			}
		}
		
		return splits;
	}	
	
	/**
	 * 停止一个指挥中的多个转发点播<br/>
	 * <p>可能涉及多个成员用户，所以返回map</p>
	 * <b>作者:</b>zsy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年11月15日 上午10:08:43
	 * @param groupId
	 * @param demandIds
	 * @return HashMap<Long, JSONArray> 格式为：<userId, [{serial:屏幕序号}]>
	 * @throws Exception
	 */
	private HashMap<Long, JSONArray> stopDemands(Long groupId, List<Long> demandIds) throws Exception{
		
		synchronized (new StringBuffer().append(lockPrefix).append(groupId).toString().intern()) {
			
			CommandGroupPO group = commandGroupDao.findOne(groupId);
			if(group.getStatus().equals(GroupStatus.STOP)){
				throw new BaseException(StatusCode.FORBIDDEN, group.getName() + " 已停止，无法操作，id: " + group.getId());
			}
			
			//<userId, [{serial:'屏幕序号'}]>
			HashMap<Long, JSONArray> result = new HashMap<Long, JSONArray>();
			List<CommandGroupForwardDemandPO> demands = group.getForwardDemands();
			Set<CommandGroupMemberPO> members = group.getMembers();
			
			CommandGroupMemberPO chairmanMember = commandCommonUtil.queryChairmanMember(members);
			chairmanMember.getUserId();
			group.getAvtpl();
			
			List<CommandGroupForwardDemandPO> stopDemands = commandCommonUtil.queryForwardDemandsByIds(demands, demandIds);
			List<CommandGroupUserPlayerPO> allNeedClosePlayers = new ArrayList<CommandGroupUserPlayerPO>();
			for(CommandGroupForwardDemandPO demand : stopDemands){
				Long dstMemberId = demand.getDstMemberId();
				CommandGroupMemberPO dstMember = commandCommonUtil.queryMemberById(members, dstMemberId);
				List<CommandGroupUserPlayerPO> needFreePlayers = new ArrayList<CommandGroupUserPlayerPO>();
				List<CommandGroupUserPlayerPO> players = dstMember.getPlayers();
				for(CommandGroupUserPlayerPO player : players){
					if(player.getBundleId().equals(demand.getDstVideoBundleId())){
						player.setFree();
						needFreePlayers.add(player);
						JSONObject split = new JSONObject();
						split.put("serial", player.getLocationIndex());
						
						JSONArray dstMemberSplits = result.get(dstMember.getUserId());
						if(dstMemberSplits != null){
							dstMemberSplits.add(split);
						}else{
							JSONArray splits = new JSONArray();
							splits.add(split);
							result.put(dstMember.getUserId(), splits);
						}
						
						break;
					}
				}
				players.remove(needFreePlayers);
				allNeedClosePlayers.addAll(needFreePlayers);
				demand.clearDst();
				demand.setExecuteStatus(ForwardDemandStatus.UNDONE);
			}
			
			demands.removeAll(stopDemands);
			commandGroupDao.save(group);
			
			//发协议
			CommandGroupAvtplGearsPO currentGear = commandCommonUtil.queryCurrentGear(group);
			CodecParamBO codec = new CodecParamBO().set(group.getAvtpl(), currentGear);
			LogicBO logic = commandBasicServiceImpl.closeBundle(null, stopDemands, allNeedClosePlayers, codec, group.getUserId());
			LogicBO logicCastDevice = commandCastServiceImpl.closeBundleCastDevice(stopDemands, null, null, allNeedClosePlayers, codec, group.getUserId());
			logic.merge(logicCastDevice);
			executeBusiness.execute(logic, group.getName() + " 指挥停止多个转发");
			
			return result;			
		}
	}
}