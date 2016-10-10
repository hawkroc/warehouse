package com.fh.controller.warehouse;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.entity.warehouse.EnterStock;
import com.fh.entity.warehouse.EnterStockDetail;
import com.fh.service.warehouse.ClientsService;
import com.fh.util.CacheUtil;
import com.fh.util.Const;
import com.fh.util.DateUtil;
import com.fh.util.FileUpload;
import com.fh.util.ObjectExcelRead;
import com.fh.util.PageData;
import com.fh.util.PathUtil;

import net.sf.ehcache.Element;

@Controller
@RequestMapping(value = "/enterStock")
public class EnterStockController extends BaseController {

	String menuUrl = "enterStock/enterStock.do"; // 菜单地址(权限用)
	String dep = null;
	@Resource(name = "clientsService")
	private ClientsService clientsService;

	@RequestMapping(value = { "/outStock" })
	public ModelAndView outStock(Page page) {
		ModelAndView rs = null;
		try {
			rs = listEnterStock(page, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;

	}

	/**
	 * 显示入库单列表
	 */
	// enterStock/outStock.do
	@RequestMapping(value = { "/enterStock" })
	public ModelAndView listEnterStock(Page page, @RequestParam(value = "out", required = false) boolean out)
			throws Exception {
		ModelAndView mv = this.getModelAndView();

		// mv.
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("Dept_ID", getDepId());

		List<PageData> wareHousesList = clientsService.listAlLWareHouses(pd); //
		mv.setViewName("warehouse/stock/stockList");
		mv.addObject("wareHousesList", wareHousesList);
		mv = getModel(mv, out);
		// System.out.println("testlist:================"+productsList.size());
		mv.addObject("pd", pd);
		// mv.addObject(Const.SESSION_QX,this.getHC()); //按钮权限
		return mv;
	}

	private ModelAndView getModel(ModelAndView mv, boolean out) {
		if (out) {
			mv.addObject("title", "出库");
			mv.addObject("isout",true);
		} else {
			mv.addObject("title", "入库");
			mv.addObject("isout",false);
		}
		return mv;
	}

	/**
	 * 显示库存总表
	 */
	@RequestMapping(value = "/storeProducts")
	public ModelAndView listtabUsers(Page page, @RequestParam(value = "out", required = false) String out)
			throws Exception {
		ModelAndView mv = this.getModelAndView();
		// PageData pd = new PageData();
		PageData pd = this.getPageData();
		pd.put("Dept_ID", getDepId());
		List<PageData> productsList = clientsService.listStock(pd);
		mv.setViewName("warehouse/stock/stockpileList");
		mv.addObject("productsList", productsList);
		System.out.println("testlist:================" + productsList.size());
		mv.addObject("pd", pd);
		// mv.addObject(Const.SESSION_QX,this.getHC()); //按钮权限
		return mv;
	}

	/**
	 * 
	 * @return
	 */
	private String getDepId() {
		if (StringUtils.isEmpty(dep)) {
			this.dep = getCurrentUser().getDep_id();
		}
		return this.dep;
	}

	@RequestMapping(value = "/search")
	public ModelAndView searchEnterStock(Page page, @RequestParam(value = "out", required = false) boolean out,
			@RequestParam(value = "lastLoginStart", required = false) String start,
			@RequestParam(value = "lastLoginEnd", required = false) String end,
			@RequestParam(value = "EnterStock_ID", required = false) String EnterStock_ID) throws Exception {
		ModelAndView mv = this.getModelAndView();
		
		//mv = getModel(mv, out);
		// mv.
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("Dept_ID", getDepId());

		start = (start == null) ? DateUtil.getDay() : start;
		end = (end == null) ? DateUtil.getDay() : end;

		pd.put("start", start);
		pd.put("end", end);

		List<PageData> wareHousesList = clientsService.listAlLWareHouses(pd);
		String table = "enterstock";// enterstock
		if (out) {
			table = "LeaveStock";
		}
		pd.put("table", table);
		List<PageData> stockList = clientsService.listAlLStockIn(pd);

		if (EnterStock_ID != null) {
			pd.put("EnterStock_ID", EnterStock_ID);
			if(out){
				pd.put("enterstock_detail", "leavestock_detail");
			}else{
				pd.put("enterstock_detail", "enterstock_detail");
				
			}
			List<EnterStockDetail> list = clientsService.listStockDetal(pd);
			mv.addObject("EnterStockDetailList", list);
		}
		mv.setViewName("warehouse/stock/stockList");
		mv.addObject("wareHousesList", wareHousesList);
		mv.addObject("stockList", stockList);
		// System.out.println("testlist:================"+productsList.size());
		mv.addObject("pd", pd);
	
		return mv;
	}

	/**
	 * 从EXCEL导入到数据库
	 */
	@RequestMapping(value = "/readExcel")
	public ModelAndView readExcel(@RequestParam(value = "csv_file", required = false) MultipartFile file,
			@RequestParam(value = "out", required = false) boolean out,
			@RequestParam(value = "choseWarehouse", required = false) String choseWarehouse) throws Exception {
		// ModelAndView mv = this.getModelAndView();
		// PageData pd = this.getPageData();

		ModelAndView modelAndView = new ModelAndView();
		modelAndView = getModel(modelAndView, out);
		modelAndView.setViewName("warehouse/stock/stockList");
		// if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;}

		if (null != file && !file.isEmpty()) {
			String filePath = PathUtil.getClasspath() + Const.FILEPATHFILE; // 文件上传路径
			String fileName = FileUpload.fileUp(file, filePath, "stockexcel"); // 执行上传

			EnterStock enterStock = new EnterStock();
			enterStock.setStoreHouse_ID(Integer.parseInt(choseWarehouse));
			if (getCurrentUser().getDep_id() == null) {
				return modelAndView.addObject("msg", "you are not client");
			}

			enterStock.setDept_ID(Integer.parseInt(getCurrentUser().getDep_id()));

			List<PageData> listPd = (List) ObjectExcelRead.readExcel(filePath, fileName, 2, 0, 0); // 执行读EXCEL操作,读出的数据导入List
																									// 2:从第3行开始；0:从第A列开始；0:第0个sheet

			// List<Role> roleList = roleService.listAllERRoles(); //列出所有二级角色

			// pd.put("ROLE_ID", roleList.get(0).getROLE_ID()); //设置角色ID为随便第一个

			List<EnterStockDetail> list = new ArrayList<>();

			for (PageData pageData : listPd) {
				if (pageData.getString("var0") == null) {
					break;
				}

				EnterStockDetail detail = new EnterStockDetail();
				String sku = pageData.getString("var0").trim();

				Element element = CacheUtil.getCacheObject(sku, "products");
				if (element == null) {
					continue;
				}

				detail.setSKU(sku);
				PageData tmPageData = (PageData) element.getObjectValue();

				int productid = (Integer) tmPageData.get("Product_ID");
				String name = (String) tmPageData.get("Name_CN");

				detail.setProduct_ID(productid);
				detail.setNAME(name);
				System.out.println("list+  " + productid);
				int t = Integer.parseInt(pageData.getString("var1").trim());
				detail.setQuantity(t);
				detail.setRemaining_amount(t);
				detail.setPrice(Double.parseDouble(pageData.getString("var2").trim()));
				detail.setDurability(pageData.getString("var3").trim());
				detail.setInvoiceNum(pageData.getString("var4").trim());
				list.add(detail);

			}
			enterStock.setEnterStockDetailList(list);
			boolean isout = false;
			if (out) {
				isout = true;

			}
			clientsService.saveStock(enterStock, isout);
			// pd.put("EnterStockDetailList", list);
			System.out.println("sizefff is " + list.size());
			modelAndView.addObject("EnterStockDetailList", list);
			/* 存入数据库操作====================================== */

			// mv.addObject("msg","success");
		}

		return modelAndView;
		// mv.setView(view);
		// return mv;
	}

}
