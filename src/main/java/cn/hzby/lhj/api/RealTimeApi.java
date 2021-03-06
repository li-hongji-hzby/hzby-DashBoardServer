package cn.hzby.lhj.api;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hzby.lhj.util.TsdbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.hitsdb.client.value.response.LastDataValue;

import cn.hzby.lhj.po.MachineStatus;
import cn.hzby.lhj.po.Project;
import cn.hzby.lhj.po.ProjectRealtimeMachineKey;
import cn.hzby.lhj.service.MachineStatusService;
import cn.hzby.lhj.service.ProjectRealtimeMachineService;
import cn.hzby.lhj.service.ProjectRealtimeSummaryService;
import cn.hzby.lhj.util.RedisUtil;

/**
 * @version: V1.0
 * @author: LHJ
 * @className: RealTimeApi
 * @packageName: api
 * @description: 实时数据API
 * @data: 2020-07-06 09:56
 **/
@CrossOrigin
@RestController
@RequestMapping("/RealTime")
public class RealTimeApi {


	@Autowired
	private MachineStatusService machineStatusService;
	@Autowired
	private ProjectRealtimeMachineService projectRealtimeMachineService;

    @Resource
    private RedisUtil redisUtil;

	/**
	* @version: V1.0
	* @author:  LHJ
	* @methodsName: listRealTimeDatas
	* @description: 从TSDB获取实时数据页机器数据（已废弃）
	* @param: JSONObject jsonObj
	* @return: Map<String, Object>
	* @throws: 
	*/
	@RequestMapping(value = "/listRealTimeDatas",method = RequestMethod.POST)
	public Map<String, Object> listRealTimeDatas(@RequestBody JSONObject jsonObj) throws Exception{
		Map<?, ?> tableMsgMap = JSON.parseObject(JSON.toJSONString(jsonObj.get("tableMsg")),Map.class);
		String project = (String)((Map<?, ?>)jsonObj.get("project")).get("projectNameEn");
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>(16);
		// 根据机器类型取出数据
		for (Entry<?, ?> tableMsgEntry : tableMsgMap.entrySet()) {
			// 取出属性存为List
			ArrayList<String> attrList = new ArrayList<String>();
			Map<?, ?> attrsMap = (JSONObject)(((JSONObject)tableMsgEntry.getValue()).get("attributes"));
			for (Entry<?, ?> attrEntry : attrsMap.entrySet()) {
				attrList.add((String)attrEntry.getValue());
			}
			Map<String, Object> machineMap = (JSONObject)(((JSONObject)tableMsgEntry.getValue()).get("machines"));
			List<Object> resList =new ArrayList<Object>();
			for (Entry<?, ?> machineEntry : machineMap.entrySet()) {
				TsdbUtils tsdbUtils  = new TsdbUtils();
				List<LastDataValue> queryList = tsdbUtils.getLastMulti(attrList,(String)machineEntry.getValue(),project);
				Map<String,Object> mechine = new LinkedHashMap<String, Object>(16);
				mechine.put("machine", machineEntry.getKey());
				ProjectRealtimeMachineKey key = new ProjectRealtimeMachineKey();
				key.setMachineNameEn((String)machineEntry.getValue());
				key.setProjectNameEn(project);
				MachineStatus machineStatus = machineStatusService.getById(Integer.valueOf(projectRealtimeMachineService.getById(key).getMachinePower()));
				LastDataValue pwoer = tsdbUtils.getLastOne(machineStatus.getAttrribute(), machineStatus.getMachineName()).get(0);
				mechine.put("status", ((BigDecimal) pwoer.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() > machineStatus.getMin());
				queryList.stream().forEach( f -> mechine.put(f.getMetric(), ((BigDecimal) f.getValue()).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue()));
				resList.add(mechine);
			}
			resultMap.put((String)tableMsgEntry.getKey(), resList);
		}
		return resultMap;
	}
	
	/**
	* @version: V1.0
	* @author:  LHJ
	* @methodsName: getRealtimeSummary
	* @description: 从TSDB获取实时数据页数据总和
	* @param: JSONObject jsonObj
	* @return: Map<String, Object>
	* @throws: 
	*/
	@SuppressWarnings("serial")
	@RequestMapping(value = "/getRealtimeSummary",method = RequestMethod.POST)
	public String getRealtimeSummary(@RequestBody JSONObject jsonObj) throws Exception{
		Project project = JSON.parseObject(JSON.toJSONString(jsonObj.get("project")), Project.class);
		return redisUtil.hget( project.getProjectNameEn(),"RealTimeSummary").toString();
	}


	/**
	* @version: V1.0
	* @author:  LHJ
	* @methodsName: listRealTimeDatasCache
	* @description: 从Redis获取实时数据页机器数据
	* @param: JSONObject jsonObj
	* @return: Map<String, Object>
	* @throws: 
	*/
	@RequestMapping(value = "/listRealTimeDatasCache",method = RequestMethod.POST)
	public Map<String, Object> listRealTimeDatasCache(@RequestBody JSONObject jsonObj) throws Exception{
		Map<?, ?> tableMsgMap = JSON.parseObject(JSON.toJSONString(jsonObj.get("tableMsg")),Map.class);
		String project = (String)((Map<?, ?>)jsonObj.get("project")).get("projectNameEn");
		Map<String,Object> resultMap = new LinkedHashMap<String, Object>(16);
		// 根据机器类型取出数据
		for (Entry<?, ?> tableMsgEntry : tableMsgMap.entrySet()) {
			// 取出属性存为List
			List<String> attrList = new ArrayList<String>();
			Map<?, ?> attrsMap = (JSONObject)(((JSONObject)tableMsgEntry.getValue()).get("attributes"));
			for (Entry<?, ?> attrEntry : attrsMap.entrySet()) {
				attrList.add((String)attrEntry.getValue());
			}
			Map<String, Object> machineMap = (JSONObject)(((JSONObject)tableMsgEntry.getValue()).get("machines"));
			List<Object> resList =new ArrayList<Object>();
			for (Entry<?, ?> machineEntry : machineMap.entrySet()) {
				List<Object> machineAttrList =new ArrayList<Object>();
				machineAttrList.add(machineEntry.getKey());
				ProjectRealtimeMachineKey key = new ProjectRealtimeMachineKey();
				key.setMachineNameEn((String)machineEntry.getValue());
				key.setProjectNameEn(project);
				MachineStatus machineStatus = machineStatusService.getById(Integer.valueOf(projectRealtimeMachineService.getById(key).getMachinePower()));
				machineAttrList.add(Double.valueOf(String.format("%.2f", Double.valueOf(redisUtil.hget(machineStatus.getMachineName(), machineStatus.getAttrribute()).toString()))) > Double.valueOf(machineStatus.getMin()));
				for(String str : attrList) {
					machineAttrList.add(redisUtil.hget((String) machineEntry.getValue(), str));
				}
				resList.add(machineAttrList);
			}
			resultMap.put((String)tableMsgEntry.getKey(), resList);
		}
		return resultMap;
	}
}
