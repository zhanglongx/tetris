package com.sumavision.tetris.cms.column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sumavision.tetris.cms.relation.ColumnRelationArticlePO;
import com.sumavision.tetris.commons.util.wrapper.StringBufferWrapper;
import com.sumavision.tetris.user.UserVO;
@Component
public class ColumnQuery {
	
	@Autowired
	private ColumnDAO columnDao;
	
	@Autowired
	private ColumnSubscriptionDAO columnSubscriptionDao;
	
	@Autowired
	private ColumnUserPermissionDAO columnUserPermissionDao;
	
	public List<ColumnVO> queryColumnRoot(UserVO user) throws Exception {

		List<ColumnPO> columns = null;
		if(user.getGroupId() != null){
			columns = columnDao.findByGroupId(user.getGroupId());
		}else if(user.getUuid() != null){
			columns = columnDao.findByUserId(user.getUuid());
		}

		List<ColumnVO> rootColumns = generateRootcolumns(columns);

		return rootColumns;
	}
	
	public List<ColumnPO> queryColumnRootPO(UserVO user) throws Exception {

		List<ColumnPO> columns = null;
		if(user.getGroupId() != null){
			columns = columnDao.findByGroupId(user.getGroupId());
		}else if(user.getUuid() != null){
			columns = columnDao.findByUserId(user.getUuid());
		}

		List<ColumnPO> rootColumns = generateRootcolumnsPO(columns);

		return rootColumns;
	}

	/**
	 * 根据用户查询栏目树<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月27日 上午11:45:09
	 * @param user 用户
	 * @return List<ColumnVO> 栏目树
	 */
	public List<ColumnVO> querycolumnTree(UserVO user) throws Exception {

		List<ColumnPO> columns = null;
		if(user.getGroupId() != null){
			columns = columnDao.findByGroupId(user.getGroupId());
		}else if(user.getUuid() != null){
			columns = columnDao.findByUserId(user.getUuid());
		}

		List<ColumnVO> rootcolumns = generateRootcolumns(columns);

		packcolumnTree(rootcolumns, columns);

		return rootcolumns;
	}
	
	/**
	 * 根据用户查询栏目树<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月27日 上午11:45:09
	 * @param user 用户
	 * @return List<ColumnVO> 栏目树
	 */
	public List<ColumnVO> querySubscriptionColumnTree(UserVO user) throws Exception {

		List<ColumnPO> columns = null;
		if(user.getGroupId() != null){
			columns = columnDao.findByGroupId(user.getGroupId());
		}else if(user.getUuid() != null){
			columns = columnDao.findByUserId(user.getUuid());
		}

		List<ColumnVO> rootcolumns = generateRootcolumns(columns);

		packColumnSubscriptionTree(user.getId(),rootcolumns, columns);
			
		return rootcolumns;
	}
	
	private void packColumnSubscriptionTree(Long userId,List<ColumnVO> rootcolumns, List<ColumnPO> totalcolumns) throws Exception{
		if (rootcolumns == null || rootcolumns.size() <= 0)
			return;
		for (int i = 0; i < rootcolumns.size(); i++) {
			ColumnVO rootcolumn = rootcolumns.get(i);
			List<ColumnVO> columnVOs = new ArrayList<ColumnVO>();
			for (int j = 0; j < totalcolumns.size(); j++) {
				ColumnPO column = totalcolumns.get(j);
				if (column.getParentId() != null && column.getParentId() == rootcolumn.getId()) {
					if (rootcolumn.getSubColumns() == null)
						rootcolumn.setSubColumns(new ArrayList<ColumnVO>());
					ColumnVO columnVO = new ColumnVO().set(column);
					if(columnSubscriptionDao.findByUserIdAndColumnId(userId, column.getId()) != null){
						columnVO.setSubscribed(true);
						rootcolumn.setSubColumnSubscribed(true);
					}
					columnVOs.add(columnVO);
				}
			}
			if (columnVOs.size() > 0) {
				Collections.sort(columnVOs,new ColumnPO.ColumnVOOrderComparator());
				rootcolumn.getSubColumns().addAll(columnVOs);
			}
			if (rootcolumn.getSubColumns() != null && rootcolumn.getSubColumns().size() > 0) {
				packColumnSubscriptionTree(userId,rootcolumn.getSubColumns(), totalcolumns);
			}
		}
	}

	private List<ColumnVO> generateRootcolumns(Collection<ColumnPO> columns) throws Exception {
		if (columns == null || columns.size() <= 0)
			return null;
		List<ColumnVO> rootcolumns = new ArrayList<ColumnVO>();
		for (ColumnPO column : columns) {
			if (column.getParentId() == null) {
				rootcolumns.add(new ColumnVO().set(column));
			}
		}
		Collections.sort(rootcolumns, new ColumnPO.ColumnVOOrderComparator());
		return rootcolumns;
	}
	
	private List<ColumnPO> generateRootcolumnsPO(Collection<ColumnPO> columns) throws Exception {
		if (columns == null || columns.size() <= 0)
			return null;
		List<ColumnPO> rootcolumns = new ArrayList<ColumnPO>();
		for (ColumnPO column : columns) {
			if (column.getParentId() == null) {
				rootcolumns.add(column);
			}
		}
		Collections.sort(rootcolumns, new ColumnPO.ColumnOrderComparator());
		return rootcolumns;
	}

	public void packcolumnTree(List<ColumnVO> rootcolumns, List<ColumnPO> totalcolumns) throws Exception {
		if (rootcolumns == null || rootcolumns.size() <= 0)
			return;
		for (int i = 0; i < rootcolumns.size(); i++) {
			ColumnVO rootcolumn = rootcolumns.get(i);
			List<ColumnVO> columnVOs = new ArrayList<ColumnVO>();
			for (int j = 0; j < totalcolumns.size(); j++) {
				ColumnPO column = totalcolumns.get(j);
				if (column.getParentId() != null && column.getParentId() == rootcolumn.getId()) {
					if (rootcolumn.getSubColumns() == null)
						rootcolumn.setSubColumns(new ArrayList<ColumnVO>());
					columnVOs.add(new ColumnVO().set(column));
				}
			}
			if (columnVOs.size() > 0) {
				Collections.sort(columnVOs,new ColumnPO.ColumnVOOrderComparator());
				rootcolumn.getSubColumns().addAll(columnVOs);
			}
			if (rootcolumn.getSubColumns() != null && rootcolumn.getSubColumns().size() > 0) {
				packcolumnTree(rootcolumn.getSubColumns(), totalcolumns);
			}
		}
	}
	
	
	public List<ColumnPO> findAllSubTags(Long id) throws Exception{
		return columnDao.findAllSubColumns(new StringBufferWrapper().append("%/")
															          .append(id)
															          .toString(), 
											 new StringBufferWrapper().append("%/")
																      .append(id)
																      .append("/%")
																      .toString());
	}
	
	/**
	 * 校验栏目和用户是否匹配<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月28日 下午3:50:13
	 * @param columnId 栏目id
	 * @param user 用户
	 */
	public boolean hasPermission(Long columnId, UserVO user) throws Exception{
		ColumnUserPermissionPO permission = null;
		if(user.getGroupId() != null){
			permission = columnUserPermissionDao.findByColumnIdAndGroupId(columnId, user.getGroupId());
		}else if (user.getUuid() != null) {
			permission = columnUserPermissionDao.findByColumnIdAndUserId(columnId, user.getUuid());
		}
		
		if(permission == null) return false;
		return true;
	}
}
