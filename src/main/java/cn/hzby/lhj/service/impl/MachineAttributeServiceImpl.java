package cn.hzby.lhj.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hzby.lhj.mapper.extend.MachineAttributeMapperExtend;
import cn.hzby.lhj.po.MachineAttribute;
import cn.hzby.lhj.service.MachineAttributeService;

/**
 * @author lhj
 */
@Service
public class MachineAttributeServiceImpl implements MachineAttributeService {

	@Autowired
	private MachineAttributeMapperExtend machineAttributeMapper;
	
	@Override
	public List<MachineAttribute> listAll() throws Exception {
		return machineAttributeMapper.selectByExample(null);
	}

	@Override
	public MachineAttribute getById(Integer id) throws Exception {
		return machineAttributeMapper.selectByPrimaryKey(id);
	}

	@Override
	public int save(MachineAttribute t) throws Exception {
		return machineAttributeMapper.insertSelective(t);
	}

	@Override
	public int removeById(Integer id) throws Exception {
		return machineAttributeMapper.deleteByPrimaryKey(id);
	}


	/**
	 * 根据ID批量查询属性
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<MachineAttribute> listByIds(List<Integer> ids) throws Exception {
		List<MachineAttribute>  attributeList = new ArrayList<MachineAttribute>();
		for(Integer id : ids) {
			attributeList.add(getById(id));
		}
		return attributeList;
	}

	
}
