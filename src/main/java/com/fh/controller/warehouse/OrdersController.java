package com.fh.controller.warehouse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fh.entity.warehouse.orders.Line_Items;
import com.fh.entity.warehouse.orders.Order;
import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.entity.ProductEntity;
import com.fh.service.warehouse.ProductService;
import com.fh.util.CacheUtil;
import com.fh.util.Const;
import com.fh.util.PageData;

/** 
 * 类名称：UserController
 * 创建人：FH 
 * 创建时间：2016年6月28日
 * @version
 */
@Controller
@RequestMapping(value="/order")
public class OrdersController extends BaseController {
	
	String menuUrl = "order/synOrder.do"; //菜单地址(权限用)
//	@Resource(name="userService")
//	private UserService userService;
//	@Resource(name="roleService")
//	private RoleService roleService;
	@Resource(name="productService")
	private ProductService productService;

	

	@RequestMapping(value="/detail")
	public ModelAndView orderDetail(@RequestParam(value = "orderid", required = false) String orderid){

		ModelAndView mv = this.getModelAndView();
		com.fh.util.PageData 	pd=new com.fh.util.PageData  ();
		pd.put("Original_ID", orderid);
		//pd.put("Dept_ID", super.getDeptId());
	    pd.put("Dept_ID", 2);
	 
		com.fh.util.PageData order=null;
		List<Line_Items> list =null;
		try {
			   if(StringUtils.isNotEmpty(orderid)){
				   order = productService.listDetail(pd);
					if(order!=null){
				
					    list =(List) order .get("line_items");

					}
			   }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			//	
		
		mv.setViewName("warehouse/orders/orderDetail");
		mv.addObject("order", order);
    	mv.addObject("itermList",list);
		//mv.addObject("pd", pd);
//		mv.addObject(Const.SESSION_QX,this.getHC());	//按钮权限
		return mv;
	}
	
	
	
	

	
	@RequestMapping(value="/stackOut")
	public String  orderStackOut(Page page,@RequestParam(value = "order", required = false) String orderid,@RequestParam(value = "sku", required = false) String sku,RedirectAttributes attr){

		ModelAndView mv = this.getModelAndView();
		com.fh.util.PageData 	pd=new com.fh.util.PageData  ();
		System.out.println("sku        "+sku);
		PageData product=(PageData)CacheUtil.getCacheObject(sku, "products").getObjectValue();
		//attr.addFlashAttribute("orderid", orderid); 
		attr.addAttribute("orderid", orderid);
		System.out.println("id  is"+product.get("Product_ID"));
//		pd.put("Original_ID", orderid);
//		//pd.put("Dept_ID", super.getDeptId());
//	    pd.put("Dept_ID", 2);
//	 
//		com.fh.util.PageData order=null;
//		List<Line_Items> list =null;
//		try {
//			   if(StringUtils.isNotEmpty(orderid)){
//				   order = productService.listDetail(pd);
//					if(order!=null){
//				
//					    list =(List) order .get("line_items");
//
//					}
//			   }
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}			//	
		
	
		//mv.addObject("pd", pd);
//		mv.addObject(Const.SESSION_QX,this.getHC());	//按钮权限
		 return "redirect:/order/detail";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 显示用户列表(用户组)
	 */
	@RequestMapping(value="/listorders")
	public ModelAndView listUsers(Page page){
		//String dept=super.getCurrentUser().getDep_id();
		//System.out.println("user id is "+super.getCurrentUser().getDep_id());
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
	   // pd.put("Dept_ID", dept);
	
		pd = this.getPageData();
	  //pd.put("Dept_ID", super.getDeptId());
	    pd.put("Dept_ID", 2);
		//System.out.println("test deptid222======            "+pd.get("Dept_ID"));
		String USERNAME =pd.getString("USERNAME");
		
		
		pd.put("USERNAME", USERNAME);
	
		

		String lastLoginStart = pd.getString("lastLoginStart");
		//lastLoginStart = lastLoginStart+" 00:00:00";
		pd.put("lastLoginStart", lastLoginStart);
		String lastLoginEnd = pd.getString("lastLoginEnd");
		//lastLoginEnd = lastLoginEnd+" 00:00:00";
		pd.put("lastLoginEnd", lastLoginEnd);

		
		page.setPd(pd);
		List<PageData> userList=null;
		try {
			
			
		
			userList = productService.listAllOrdersByClient(pd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			//
	//	List<Role> roleList = roleService.listAllERRoles();						
		
		mv.setViewName("warehouse/orders/orders");
		mv.addObject("userList", userList);
	//	mv.addObject("roleList", roleList);
		mv.addObject("pd", pd);
//		mv.addObject(Const.SESSION_QX,this.getHC());	//按钮权限
		return mv;
	}
	
	
	
	
	
	
	
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
	

	/* ===============================权限================================== */
	public Map<String, String> getHC(){
		Subject currentUser = SecurityUtils.getSubject();  //shiro管理的session
		Session session = currentUser.getSession();
		return (Map<String, String>)session.getAttribute(Const.SESSION_QX);
	}
	/* ===============================权限================================== */
}
