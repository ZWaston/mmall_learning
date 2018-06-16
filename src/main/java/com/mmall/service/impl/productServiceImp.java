package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.PayInfo;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.kerberos.ServicePermission;
import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class productServiceImp implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse saveOrUpdateProduct(Product product) {
        if(product != null) {
            if(StringUtils.isNotBlank(product.getSubImages())) {
                //如果子图不为空，将第一个子图设置为主图，子图以,分隔
                String[] subImagesArray = product.getSubImages().split(",");
                if(subImagesArray.length > 0) {
                    product.setMainImage(subImagesArray[0]);
                }
            }

            //还需要对产品id进行判断，不然不知道更新是哪一个产品
            if(product.getId() != null) {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if(rowCount > 0) {
                    return ServerResponse.createBySuccess("更新产品成功");
                } else {
                    return ServerResponse.createBySuccess("更新产品失败");
                }

            } else {
                int rowCount = productMapper.insert(product);
                if(rowCount > 0) {
                    return ServerResponse.createBySuccess("新增产品成功");
                } else {
                    return ServerResponse.createBySuccess("新增产品失败");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
        }
    }

    public ServerResponse<String> setSaleStatus(Integer productId,Integer status) {
        if(productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0) {
            return ServerResponse.createBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }

    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if(productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        //VO对象--view object
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);

    }

    //pojo （组装成）--> VO对象
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStock(product.getStatus());

        //imageHost  从配置文件读取图片服务器地址
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","localhost"));
        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null) {
            productDetailVo.setCategoryId(0);
        }else {
            productDetailVo.setCategoryId(category.getParentId());
        }
        //createTime  需要对时间进行一个标准转换
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize) {
        //三个步骤
        //1.startpage--start  2.填充自己的sql查询逻辑 3.pagehelper-收尾
        //开始步骤
        PageHelper.startPage(pageNum,pageSize);
        //查询逻辑
        List<Product> productList = productMapper.selectList();

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //收尾
        PageInfo pageResult = new PageInfo(productList);//会对productList(填写查询逻辑那个，会对里面的sql进行填充)进行一个分页处理
        pageResult.setList(productListVoList); //将List重置为productListVoList
        return ServerResponse.createBySuccess(pageResult);

    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo  productListVo = new ProductListVo();
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setId(product.getId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","localhost"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setName(product.getName());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName)) {
            // like %kk% 这条sql表示的是包含kk字符串的所有字符串
            //like'%inger' 将搜索以字母 inger 结尾的所有字符串（如 Ringer、Stringer）
            //like'Mc%' 将搜索以字母 Mc 开头的所有字符串（如 McBadden）。
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        //收尾
        PageInfo pageResult = new PageInfo(productList);//会对productList(填写查询逻辑那个，会对里面的sql进行填充)进行一个分页处理
        pageResult.setList(productListVoList); //将List重置为productListVoList
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        //VO对象--view object
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy) {
        if(StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGAL_ARGUMENT.getCode(),ResponseCode.ILLEAGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类，且没有关键字，返回一个空的结果集
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if(StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,
                categoryIdList.size() == 0?null:categoryIdList);
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServerResponse.createBySuccess(pageInfo);

    }
}
