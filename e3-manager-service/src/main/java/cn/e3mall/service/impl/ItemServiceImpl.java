package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.service.ItemService;
/**
 * 商品管理Service
 * @author wenfei
 *
 */
@Service
public class ItemServiceImpl implements ItemService {
	
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemDescMapper itemDescMapper;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Resource
	private Destination topicDestination;
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${REDIS_ITEM_PRE}")
	private String REDIS_ITEM_PRE;
	@Value("${ITEM_CACHE_EXPIRE}")
	private Integer ITEM_CACHE_EXPIRE;
	
	@Override
	public TbItem getItemById(long id) {
		//查询缓存
		try{
			String json = jedisClient.get(REDIS_ITEM_PRE + ":" + id + ":BASE");
			if(StringUtils.isNotBlank(json)){
				//把json转换为java对象
				TbItem tbItem = JsonUtils.jsonToPojo(json, TbItem.class);
				return tbItem;
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		//如果缓存没有数据，则查询数据库
		TbItem item = itemMapper.selectByPrimaryKey(id);
		try{
			//把数据添加到缓存
			jedisClient.set(REDIS_ITEM_PRE + ":" + id + ":BASE", JsonUtils.objectToJson(item));
			//设置缓存有效期
			jedisClient.expire(REDIS_ITEM_PRE + ":" + id + ":BASE", ITEM_CACHE_EXPIRE);
		} catch(Exception e){
			e.printStackTrace();
		}
		return item;
		
	}

	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbItemExample example = new TbItemExample();
		List<TbItem> list = itemMapper.selectByExample(example);
		//取分页信息
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
		//创建返回结果对象，设置属性值
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal((int)pageInfo.getTotal());
		result.setRows(list);
		return result;
	}

	@Override
	public E3Result addItem(TbItem item, String desc) {
		// 1、生成商品id
		final long id = IDUtils.genItemId();
		item.setId(id);
		//商品状态。1-正常    2-下架   3-删除
		item.setStatus((byte)1);
		item.setUpdated(new Date());
		item.setCreated(new Date());
		//插入商品数据
		itemMapper.insert(item);
		//插入商品描述数据
		TbItemDesc itemDesc = new TbItemDesc();
		itemDesc.setItemId(id);
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(new Date());
		itemDescMapper.insert(itemDesc);
		//发送一个商品添加消息
		jmsTemplate.send(topicDestination, new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage(id + "");
				return textMessage;
			}
		});
		return E3Result.ok();
	}
	
	//通过商品id取得商品描述信息
	@Override
	public TbItemDesc getItemDescById(long id) {
		//查询缓存
		try{
			String json = jedisClient.get(REDIS_ITEM_PRE + ":" + id + ":DESC");
			if(StringUtils.isNotBlank(json)){
				//把json转换为java对象
				TbItemDesc tbItemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
				return tbItemDesc;
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		//如果缓存没有数据，则查询数据库
		TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);
		try{
			//把数据添加到缓存
			jedisClient.set(REDIS_ITEM_PRE + ":" + id + ":DESC", JsonUtils.objectToJson(itemDesc));
			//设置缓存有效期
			jedisClient.expire(REDIS_ITEM_PRE + ":" + id + ":DESC", ITEM_CACHE_EXPIRE);
		} catch(Exception e){
			e.printStackTrace();
		}
		return itemDesc;
	}

	@Override
	public E3Result editItem(TbItem item, String desc) {
		//商品状态。1-正常    2-下架   3-删除
		item.setStatus((byte)1);
		item.setUpdated(new Date());
		item.setCreated(new Date());
		//更新商品数据
		itemMapper.updateByPrimaryKey(item);
		//更新商品描述
		TbItemDesc tbItemDesc = itemDescMapper.selectByPrimaryKey(item.getId());
		if(desc != null){
			tbItemDesc.setItemDesc(desc);
		}
		tbItemDesc.setCreated(new Date());
		tbItemDesc.setUpdated(new Date());
		itemDescMapper.updateByPrimaryKey(tbItemDesc);
		return E3Result.ok();
	}

	@Override
	public E3Result deleteItem(Long[] ids) {
		//设置删除信息
//		TbItemExample example = new TbItemExample();
//		Criteria criteria = example.createCriteria();
		for(long id : ids){
			//criteria.andIdEqualTo(id);
			itemMapper.deleteByPrimaryKey(id);
			itemDescMapper.deleteByPrimaryKey(id);
		}
		//删除商品描述信息
//		TbItemDescExample example1 = new TbItemDescExample();
//		cn.e3mall.pojo.TbItemDescExample.Criteria criteria2 = example1.createCriteria();
//		cn.e3mall.pojo.TbItemDescExample.Criteria andItemIdIn = criteria2.andItemIdIn(ids);
//		itemDescMapper.deleteByExample(example1);
		return E3Result.ok();
	}

	@Override
	public E3Result instockItem(Long[] ids) {
		for(long id : ids){
			TbItem item = itemMapper.selectByPrimaryKey(id);
			item.setStatus((byte) 0);
			itemMapper.updateByPrimaryKey(item);
		}
		return E3Result.ok();
	}

	@Override
	public E3Result reshelfItem(Long[] ids) {
		for(long id : ids){
			TbItem item = itemMapper.selectByPrimaryKey(id);
			item.setStatus((byte) 1);
			itemMapper.updateByPrimaryKey(item);
		}
		return E3Result.ok();
	}

}
