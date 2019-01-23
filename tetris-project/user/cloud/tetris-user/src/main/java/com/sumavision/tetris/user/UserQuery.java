package com.sumavision.tetris.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.sumavision.tetris.commons.util.wrapper.ArrayListWrapper;
import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;

@Component
public class UserQuery {
	
	@Autowired
	private UserDAO userDao;
	
	/**
	 * 分页查询用户（前端接口）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月18日 下午5:32:06
	 * @param int currentPage 当前页码
	 * @param int pageSize 每页数据量
	 * @return List<UserVO> 用户列表
	 */
	public Map<String, Object> list(int currentPage, int pageSize) throws Exception{
		List<UserPO> users = findAllOrderByUpdateTimeDesc(currentPage, pageSize);
		List<UserVO> view_users = UserVO.getConverter(UserVO.class).convert(users, UserVO.class);
		long total = userDao.count();
		return new HashMapWrapper<String, Object>().put("total", total)
												   .put("rows", view_users)
												   .getMap();
	}
	
	/**
	 * 分页查询用户（后台接口）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月18日 下午5:32:46
	  * @param int currentPage 当前页码
	 * @param int pageSize 每页数据量
	 * @return List<UserPO> 用户列表
	 */
	public List<UserPO> findAllOrderByUpdateTimeDesc(int currentPage, int pageSize) throws Exception{
		Pageable page = new PageRequest(currentPage-1, pageSize);
		Page<UserPO> users = userDao.findAllOrderByUpdateTimeDesc(page);
		return users.getContent();
	}
	
	/**
	 * 分页查询用户（带例外）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月23日 下午6:37:06
	 * @param Collection<Long> except 例外用户id
	 * @param int currentPage 当前页码
	 * @param int pageSize 每页数据量
	 * @return int total 数据总量
	 * @return List<UserVO> rows 用户列表
	 */
	public Map<String, Object> listWithExcept(Collection<Long> except, int currentPage, int pageSize) throws Exception{
		int total = userDao.countWithExcept(except);
		List<UserPO> users = findWithExceptOrderByUpdateTimeDesc(except, currentPage, pageSize);
		List<UserVO> view_users = UserVO.getConverter(UserVO.class).convert(users, UserVO.class);
		return new HashMapWrapper<String, Object>().put("total", total)
											       .put("rows", view_users)
											       .getMap();
	}
	
	/**
	 * 分页查询用户（带例外）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月23日 下午6:31:48
	 * @param Collection<Long> except 例外用户id列表
	 * @param int currentPage 当前页码
	 * @param int pageSize 每页数据量
	 * @return List<UserPO> 用户列表
	 */
	public List<UserPO> findWithExceptOrderByUpdateTimeDesc(Collection<Long> except, int currentPage, int pageSize) throws Exception{
		Pageable page = new PageRequest(currentPage-1, pageSize);
		Page<UserPO> users = userDao.findWithExceptOrderByUpdateTimeDesc(except, page);
		return users.getContent();
	}
	
	/**************************************************************************
	 **************************************************************************
	 **************************************************************************/
	
	/**
	 * 获取当前登录用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月22日 上午10:14:26
	 * @return UserVO 当前用户
	 */
	@Deprecated
	public UserVO current(){
		return new UserVO().setUuid("1")
						   .setNickname("用户1")
						   .setClassify(UserClassify.MAINTENANCE.toString())
						   .setIcon("")
						   .setGroupId(null)
						   .setGroupName(null);
	}
	
	
	
	/**
	 * 获取用户组下所有的用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月6日 下午4:53:11
	 * @param String groupId 用户组id
	 * @return List<UserVO> 用户列表
	 */
	@Deprecated
	public List<UserVO> list(String groupId){
		return this.users;
	}
	
	/**
	 * 分页获取用户组下的用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月7日 下午5:24:19
	 * @param String groupId 用户组id
	 * @param Integer pageSize 每页数量
	 * @param Integer currentSize 当前页码
	 * @oaran Collection<String> except 例外用户
	 * @return List<UserVO> 用户列表
	 */
	@Deprecated
	public List<UserVO> list(String groupId, Integer pageSize, Integer currentPage, Collection<String> except){
		if(except==null || except.size()<=0) return this.users;
		List<UserVO> users = this.users;
		List<UserVO> filtered = new ArrayList<UserVO>();
		for(UserVO user:users){
			if(!except.contains(user.getUuid())){
				filtered.add(user);
			}
		}
		return filtered;
	}
	
	/**
	 * 计算企业内部用户总数（带例外）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月10日 下午2:19:03
	 * @param String groupId 企业id
	 * @param Collection<String> except 例外用户id列表
	 * @return Integer 总数
	 */
	@Deprecated
	public Integer count(String groupId, Collection<String> except){
		if(except==null || except.size()<=0) return count(groupId);
		List<UserVO> users = this.users;
		int count = 0;
		for(UserVO user:users){
			if(!except.contains(user.getUuid())){
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 获取企业内用户总数<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月7日 下午5:26:09
	 * @param String groupId 用户组id
	 * @return Integer 用户数量
	 */
	@Deprecated
	public Integer count(String groupId){
		return this.users.size();
	}
	
	/**
	 * 根据userId查找用户<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月7日 下午6:20:07
	 * @param Collection<String> userIds 用户id列表
	 * @return List<UserVO> 用户列表
	 */
	@Deprecated
	public List<UserVO> find(Collection<String> userIds){
		List<UserVO> users = this.users;
		List<UserVO> findUsers = new ArrayList<UserVO>();
		for(String userId:userIds){
			for(UserVO user:users){
				if(user.getUuid().equals(userId)){
					findUsers.add(user);
					break;
				}
			}
		}
		return findUsers;
	}
	
	/**
	 * 根据userId分页查询<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月10日 上午10:08:22
	 * @param Collection<String> userIds 用户id列表
	 * @param Integer pageSize 每页数据量
	 * @param Integer currentPage 当前页码
	 * @return List<UserVO> 用户列表
	 */
	@Deprecated
	public List<UserVO> find(Collection<String> userIds, Integer pageSize, Integer currentPage){
		return find(userIds);
	}
	
	/** 测试数据 */
	private List<UserVO> users = new ArrayListWrapper<UserVO>().add(new UserVO().setUuid("1")
																        .setNickname("用户1")
																        .setStatus(UserStatus.ONLINE.getName())
																        .setNumbersOfMessage(0))
														.add(new UserVO().setUuid("2")
															            .setNickname("用户2")
															            .setStatus(UserStatus.OFFLINE.getName())
															            .setNumbersOfMessage(0))
														.add(new UserVO().setUuid("3")
															            .setNickname("用户3")
															            .setStatus(UserStatus.OFFLINE.getName())
															            .setNumbersOfMessage(0))
														.add(new UserVO().setUuid("4")
														                .setNickname("用户4")
															            .setStatus(UserStatus.ONLINE.getName())
															            .setNumbersOfMessage(13))
														.add(new UserVO().setUuid("5")
															            .setNickname("用户5")
															            .setStatus(UserStatus.OFFLINE.getName())
															            .setNumbersOfMessage(0))
														.add(new UserVO().setUuid("6")
														                .setNickname("用户6")
															            .setStatus(UserStatus.OFFLINE.getName())
															            .setNumbersOfMessage(2))
														.getList();
	
}

