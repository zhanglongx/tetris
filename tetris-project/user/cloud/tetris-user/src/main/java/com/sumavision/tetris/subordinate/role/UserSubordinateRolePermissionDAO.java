package com.sumavision.tetris.subordinate.role;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

import com.sumavision.tetris.orm.dao.BaseDAO;

@RepositoryDefinition(domainClass = UserSubordinateRolePermissionPO.class, idClass = Long.class)
public interface UserSubordinateRolePermissionDAO extends BaseDAO<UserSubordinateRolePermissionPO>{
	
	public List<UserSubordinateRolePermissionPO> findByRoleId(Long roleId);

	@Query(value = "SELECT user_id from tetris_subordinate_role_permission where role_id=?1", nativeQuery = true)
	public List<Long> getUserIdsFromRoleId(Long roleId);
	
	/**
	 * 根据用户id移除用户角色关系<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年5月30日 上午11:14:16
	 * @param Long roleId 角色id
	 */
	@Query(value = "delete from tetris_subordinate_role_permission where user_id=?1 ", nativeQuery = true)
	@Modifying
	public void removeByUserId(Long userId);

	@Query(value = "SELECT count(user.id) from tetris_user user LEFT JOIN tetris_subordinate_role_permission permission ON user.id=permission.user_id WHERE permission.role_id=?1", nativeQuery = true)
	public int countByRoleId(Long companyId);

	public List<UserSubordinateRolePermissionPO> findByRoleIdAndUserIdIn(Long roleId, List<Long> userId);
	@Query(value = "SELECT role_id from tetris_subordinate_role_permission where user_id=?1", nativeQuery = true)
	public Long getRoleIdFromUserId(Long userId);
	public UserSubordinateRolePermissionPO findByUserId(Long userId);
}
