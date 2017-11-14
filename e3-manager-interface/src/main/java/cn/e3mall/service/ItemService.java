package cn.e3mall.service;

import java.util.List;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemDesc;

public interface ItemService {
	
	TbItem getItemById(long id);
	
	EasyUIDataGridResult getItemList(int page,int rows);
	
	E3Result addItem(TbItem item,String desc);
	
	TbItemDesc getItemDescById(long id);
	
	//修改商品及描述信息
	E3Result editItem(TbItem item, String desc);
	
	//通过商品id删除商品
	E3Result deleteItem(Long[] ids);
	
	//通过商品id下架商品
	E3Result instockItem(Long[] ids);
	
	//通过商品id上架商品
	E3Result reshelfItem(Long[] ids);
}
