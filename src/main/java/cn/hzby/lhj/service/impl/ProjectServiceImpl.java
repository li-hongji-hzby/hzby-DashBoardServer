package cn.hzby.lhj.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hzby.lhj.mapper.extend.ProjectMapperExtend;
import cn.hzby.lhj.po.Project;
import cn.hzby.lhj.po.ProjectExample;
import cn.hzby.lhj.service.ProjectHistoryMachineService;
import cn.hzby.lhj.service.ProjectRealtimeMachineService;
import cn.hzby.lhj.service.ProjectService;

/**
 * @author lhj
 */
@Service
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectMapperExtend projectMapper;

	@Autowired
	private ProjectRealtimeMachineService projectRealtimeMachineService;
	
	@Autowired
	private ProjectHistoryMachineService projectHistoryMachineService;
	
	@Override
	public List<Project> listAll() throws Exception {
		return projectMapper.selectByExample(null);
	}

	@Override
	public Project getById(String id) throws Exception {
		return null;
	}

	@Override
	public int save(Project t) throws Exception {
		return projectMapper.insertSelective(t);
	}

	@Override
	public int removeById(String id) throws Exception {
		return 0;
	}

	/**
	 * 根据用户查找对应项目
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public Project getDefaultProjectByUsername(String username) throws Exception {
		ProjectExample example = new ProjectExample();
		ProjectExample.Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		criteria.andIsDefaultEqualTo(1);
		return projectMapper.selectByExample(example).get(0);
	}

	/**
	 * 根据用户获取项目及页面配置
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> getDefaultProjectConfigByUser(String username) throws Exception {
		ProjectExample example = new ProjectExample();
		ProjectExample.Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		criteria.andIsDefaultEqualTo(1);
		List<Project> projectList = projectMapper.selectByExample(example);
		System.out.println(projectList.size());
		Project project = projectList.get(0);
		Map<String,Object> result = new HashMap<String,Object>(16);
		result.put("realtimePage", projectRealtimeMachineService.listByProjectIsshow(project.getProjectNameEn()));
		result.put("historyPage", projectHistoryMachineService.listByProjectIsshow(project.getProjectNameEn()));
		return result;
	}


	/**
	 * 根据用户获取所有项目
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> listProjectByUser(String username) throws Exception {
		ProjectExample example = new ProjectExample();
		ProjectExample.Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		List<Project> projectList = projectMapper.selectByExample(example);
		Map<String, Object> projectMap = new HashMap<>(16);
		projectList.parallelStream().forEach( e -> {
			projectMap.put(e.getProjectNameZh(), e.getProjectNameEn());
		});
		return projectMap;
	}
	
	/**
	 * 根据项目获取配置
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> getConfigByProject(String project) throws Exception{
		Map<String,Object> result = new HashMap<String,Object>(16);
		result.put("realtimePage", projectRealtimeMachineService.listByProjectIsshow(project));
		result.put("historyPage", projectHistoryMachineService.listByProjectIsshow(project));
		return result;
	}
}
