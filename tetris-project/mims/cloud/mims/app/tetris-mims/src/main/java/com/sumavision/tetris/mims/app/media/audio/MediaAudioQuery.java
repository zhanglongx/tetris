package com.sumavision.tetris.mims.app.media.audio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sumavision.tetris.commons.util.wrapper.HashMapWrapper;
import com.sumavision.tetris.mims.app.folder.FolderBreadCrumbVO;
import com.sumavision.tetris.mims.app.folder.FolderDAO;
import com.sumavision.tetris.mims.app.folder.FolderPO;
import com.sumavision.tetris.mims.app.folder.FolderQuery;
import com.sumavision.tetris.mims.app.folder.FolderRolePermissionDAO;
import com.sumavision.tetris.mims.app.folder.FolderRolePermissionPO;
import com.sumavision.tetris.mims.app.folder.FolderType;
import com.sumavision.tetris.mims.app.folder.exception.FolderNotExistException;
import com.sumavision.tetris.mims.app.folder.exception.UserHasNoPermissionForFolderException;
import com.sumavision.tetris.mims.app.media.UploadStatus;
import com.sumavision.tetris.mims.app.media.video.MediaVideoItemType;
import com.sumavision.tetris.mims.app.media.video.MediaVideoPO;
import com.sumavision.tetris.mims.app.media.video.MediaVideoVO;
import com.sumavision.tetris.subordinate.role.SubordinateRoleQuery;
import com.sumavision.tetris.user.UserQuery;
import com.sumavision.tetris.user.UserVO;

/**
 * 音频媒资查询操作<br/>
 * <b>作者:</b>lvdeyang<br/>
 * <b>版本：</b>1.0<br/>
 * <b>日期：</b>2019年1月28日 上午10:38:08
 */
@Component
public class MediaAudioQuery {

	@Autowired
	private MediaAudioDAO mediaAudioDao;
	
	@Autowired
	private UserQuery userQuery;
	
	@Autowired
	private FolderDAO folderDao;
	
	@Autowired
	private FolderQuery folderQuery;
	@Autowired 
	private SubordinateRoleQuery subordinateRoleQuery;
	
	@Autowired
	private FolderRolePermissionDAO folderRolePermissionDAO;	
	/**
	 * 加载文件夹下的音频媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月6日 下午4:03:27
	 * @param folderId 文件夹id
	 * @return rows List<MediaAudioVO> 音频媒资列表
	 * @return breadCrumb FolderBreadCrumbVO 面包屑数据
	 */
	public Map<String, Object> load(Long folderId) throws Exception{
		
		UserVO user = userQuery.current();
		
		//TODO 权限校验
		Long role = subordinateRoleQuery.queryRolesByUserId(user.getId());
		List<Long> folderIdsList = new ArrayList<Long>();
		List<FolderRolePermissionPO> list = folderRolePermissionDAO.findByRoleId(role);
		for (int j = 0; j < list.size(); j++) {
			folderIdsList.add(list.get(j).getFolderId());
		}
		//具有权限的文件夹
		List<FolderPO> permissFolders = folderDao.findByIdIn(folderIdsList);
		//按照文件夹类型过滤
		List<FolderPO> permissFolders1 = new ArrayList<FolderPO>();
		for (int i = 0; i < permissFolders.size(); i++) {
			FolderPO po = permissFolders.get(i);
			if (po.getType() == FolderType.COMPANY_AUDIO) {
				permissFolders1.add(po);
			}
		}		
		if(folderId == null){
			FolderPO folder = folderDao.findCompanyRootFolderByType(user.getGroupId(), FolderType.COMPANY_AUDIO.toString());
			folderId = folder.getId();
		}
		
		FolderPO current = folderDao.findOne(folderId);
		
		if(current == null) throw new FolderNotExistException(folderId);
		
		if(!folderQuery.hasGroupPermission(user.getGroupId(), current.getId())){
			throw new UserHasNoPermissionForFolderException(UserHasNoPermissionForFolderException.CURRENT);
		}
		
		//获取当前文件夹的所有父目录
		List<FolderPO> parentFolders = folderQuery.getParentFolders(current);
		List<FolderPO> parentFolders1 = new ArrayList<FolderPO>();
		for (int i = 0; i < parentFolders.size(); i++) {
			FolderPO po = parentFolders.get(i);
			if (permissFolders1.contains(po)) {
				parentFolders1.add(po);
			}
		}
		
		List<FolderPO> filteredParentFolders = new ArrayList<FolderPO>();
		if(parentFolders1==null || parentFolders1.size()<=0){
			parentFolders = new ArrayList<FolderPO>();
		}
		for(FolderPO parentFolder:parentFolders1){
			if(!FolderType.COMPANY.equals(parentFolder.getType())){
				filteredParentFolders.add(parentFolder);
			}
		}
		filteredParentFolders.add(current);
		//生成面包屑数据
		FolderBreadCrumbVO folderBreadCrumb = folderQuery.generateFolderBreadCrumb(filteredParentFolders);
		
		List<FolderPO> folders = folderDao.findPermissionCompanyFoldersByRoleId(role.toString(), folderId, FolderType.COMPANY_AUDIO.toString());
		
		List<MediaAudioPO> audios = findCompleteByFolderId(current.getId());
		
		List<MediaAudioVO> medias = new ArrayList<MediaAudioVO>();
		if(folders!=null && folders.size()>0){
			for(FolderPO folder:folders){
				medias.add(new MediaAudioVO().set(folder));
			}
		}
		if(audios!=null && audios.size()>0){
			for(MediaAudioPO audio:audios){
				medias.add(new MediaAudioVO().set(audio));
			}
		}
		
		Map<String, Object> result = new HashMapWrapper<String, Object>().put("rows", medias)
																  		 .put("breadCrumb", folderBreadCrumb)
																  		 .getMap();
		
		return result;
		
	}
	
	/**
	 * 加载所有的音频媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年12月6日 下午4:03:27
	 * @return List<MediaAudioVO> 视频媒资列表
	 */
	public List<MediaAudioVO> loadAll() throws Exception{
		
		UserVO user = userQuery.current();
		
		//TODO 权限校验		
		List<FolderPO> folderTree = folderDao.findPermissionCompanyTree(user.getUuid(), FolderType.COMPANY_AUDIO.toString());
		
		List<Long> folderIds = new ArrayList<Long>();
		for(FolderPO folderPO: folderTree){
			folderIds.add(folderPO.getId());
		}
		
		List<MediaAudioPO> audios = mediaAudioDao.findByFolderIdIn(folderIds);
		
		List<FolderPO> roots = folderQuery.findRoots(folderTree);
		
		List<MediaAudioVO> medias = new ArrayList<MediaAudioVO>();
		
		for(FolderPO root:roots){
			medias.add(new MediaAudioVO().set(root));
		}
		
		packMediaVideoTree(medias, folderTree, audios);
		
		return medias;
	}
	
	/**
	 * 查询文件夹下上传完成的音频媒资<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月28日 上午10:38:53
	 * @param Long folderId 文件夹id
	 * @return List<MediaPicturePO> 音频媒资
	 */
	public List<MediaAudioPO> findCompleteByFolderId(Long folderId){
		return mediaAudioDao.findByFolderIdAndUploadStatusOrderByName(folderId, UploadStatus.COMPLETE);
	}
	
	/**
	 * 查询文件夹下上传完成的音频媒资（批量）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年1月29日 下午3:36:37
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @return List<MediaPicturePO> 音频媒资
	 */
	public List<MediaAudioPO> findCompleteByFolderIds(Collection<Long> folderIds){
		return mediaAudioDao.findByFolderIdInAndUploadStatus(folderIds, UploadStatus.COMPLETE);
	}
	
	/**
	 * 获取文件夹（多个）下的音频媒资上传任务（上传未完成的）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月29日 下午1:25:31
	 * @param Collection<Long> folderIds 文件夹id列表
	 * @return List<MediaPicturePO> 上传任务列表
	 */
	public List<MediaAudioPO> findTasksByFolderIds(Collection<Long> folderIds){
		return mediaAudioDao.findByFolderIdInAndUploadStatus(folderIds, UploadStatus.UPLOADING);
	}
	
	/**
	 * 根据uuid查找媒资音频（内存循环）<br/>
	 * <b>作者:</b>lvdeyang<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2018年11月26日 上午11:52:58
	 * @param String uuid 图片uuid
	 * @param Collection<MediaAudioPO> pictures 查找范围
	 * @return MediaPicturePO 查找结果
	 */
	public MediaAudioPO loopForUuid(String uuid, Collection<MediaAudioPO> audios){
		if(audios==null || audios.size()<=0) return null;
		for(MediaAudioPO audio:audios){
			if(audio.getUuid().equals(uuid)){
				return audio;
			}
		}
		return null;
	}
	
	/**
	 * 生成媒资音频树<br/>
	 * <b>作者:</b>ldy<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年3月31日 上午11:29:34
	 * @param List<MediaAudioVO> roots 根
	 * @param List<FolderPO> folders 所有文件夹
	 * @param List<MediaAudioPO> medias 所有视频媒资
	 */
	public void packMediaVideoTree(List<MediaAudioVO> roots, List<FolderPO> folders, List<MediaAudioPO> medias) throws Exception{
		if(roots == null || roots.size() <= 0){
			return;
		}
		for(MediaAudioVO root: roots){
			if(root.getType().equals(MediaVideoItemType.FOLDER.toString())){
				if(root.getChildren() == null) root.setChildren(new ArrayList<MediaAudioVO>());
				for(FolderPO folder: folders){
					if(folder.getParentId() != null && folder.getParentId().equals(root.getId())){
						root.getChildren().add(new MediaAudioVO().set(folder));
					}
				}
				for(MediaAudioPO media: medias){
					if(media.getFolderId() != null && media.getFolderId().equals(root.getId())){
						root.getChildren().add(new MediaAudioVO().set(media));
					}
				}
				if(root.getChildren().size() > 0){
					packMediaVideoTree(root.getChildren(), folders, medias);
				}
			}
		}
	}
	
	/**
	 * 加载文件夹下的音频媒资(供采集app使用)<br/>
	 * <b>作者:</b>lzp<br/>
	 * <b>版本：</b>1.0<br/>
	 * <b>日期：</b>2019年6月6日 下午4:03:27
	 * @param folderId 文件夹id
	 * @return rows List<MediaAudioVO> 音频媒资列表
	 * @return breadCrumb FolderBreadCrumbVO 面包屑数据
	 */
	public Map<String, Object> loadForAndroid(Long folderId) throws Exception{
		
		UserVO user = userQuery.current();
		
		//TODO 权限校验
		
		if(folderId == null){
			FolderPO folder = folderDao.findCompanyRootFolderByType(user.getGroupId(), FolderType.COMPANY_AUDIO.toString());
			folderId = folder.getId();
		}
		
		FolderPO current = folderDao.findOne(folderId);
		
		if(current == null) throw new FolderNotExistException(folderId);
		
		if(!folderQuery.hasGroupPermission(user.getGroupId(), current.getId())){
			throw new UserHasNoPermissionForFolderException(UserHasNoPermissionForFolderException.CURRENT);
		}
		
		//获取当前文件夹的所有父目录
		List<FolderPO> parentFolders = folderQuery.getParentFolders(current);
		
		List<FolderPO> filteredParentFolders = new ArrayList<FolderPO>();
		if(parentFolders==null || parentFolders.size()<=0){
			parentFolders = new ArrayList<FolderPO>();
		}
		for(FolderPO parentFolder:parentFolders){
			if(!FolderType.COMPANY.equals(parentFolder.getType())){
				filteredParentFolders.add(parentFolder);
			}
		}
		filteredParentFolders.add(current);
		
		//生成面包屑数据
		List<FolderBreadCrumbVO> folderBreadCrumb = folderQuery.generateFolderBreadCrumbForAndroid(filteredParentFolders);
		
		List<FolderPO> folders = folderDao.findPermissionCompanyFoldersByParentId(user.getUuid(), folderId, FolderType.COMPANY_AUDIO.toString());
		
		List<MediaAudioPO> audios = findCompleteByFolderId(current.getId());
		
		List<MediaAudioVO> medias = new ArrayList<MediaAudioVO>();
		if(folders!=null && folders.size()>0){
			for(FolderPO folder:folders){
				medias.add(new MediaAudioVO().set(folder));
			}
		}
		if(audios!=null && audios.size()>0){
			for(MediaAudioPO audio:audios){
				medias.add(new MediaAudioVO().set(audio));
			}
		}
		
		Map<String, Object> result = new HashMapWrapper<String, Object>().put("rows", medias)
																  		 .put("breadCrumb", folderBreadCrumb)
																  		 .getMap();
		
		return result;
		
	}
}
