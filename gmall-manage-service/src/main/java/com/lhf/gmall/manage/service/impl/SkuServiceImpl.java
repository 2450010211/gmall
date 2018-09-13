package com.lhf.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lhf.gmall.util.RedisUtil;
import com.lhf.gmall.bean.*;
import com.lhf.gmall.manage.mapper.SkuAttrValueMapper;
import com.lhf.gmall.manage.mapper.SkuImageMapper;
import com.lhf.gmall.manage.mapper.SkuInfoMapper;
import com.lhf.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.lhf.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-08 22:37
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<SkuInfo> skuList(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> select = skuInfoMapper.select(skuInfo);
        return select;
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();
        //保存平台属性关联信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() != 0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        //保存销售属性关联信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() != 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }

        //保存sku图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() != 0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
                skuImageMapper.insertSelective(skuImage);
            }
        }
    }

    /**
     * 高并发时可能会出现的问题
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuById(String skuId){

        SkuInfo skuInfo = null;

        try {
            Jedis jedis = redisUtil.getJedis();
            //redis key的命名规则
            String skuKey = "sku:" + skuId + ":info";
            String skuInfoJson  = jedis.get(skuKey);
            if(skuInfoJson == null || skuInfoJson.length() == 0){
                System.out.println(Thread.currentThread().getName() + "缓存未命中!");
                //创建一个缓存锁的key
                String skuLockKey = "sku:" + skuId + ":info";
                //PX参数用于设置存活的毫秒数。
                //NX参数表示当前命令中指定的KEY不存在才行。
                String lock = jedis.set(skuLockKey, "OK", "NX", "PX", 1000);
                if("OK".equals(lock)){
                    System.err.println(Thread.currentThread().getName() + "获得分布式锁");
                    //查询数据库
                    SkuInfo skuByIdDB = getSkuByIdDB(skuId);
                    if(skuByIdDB == null){
                        jedis.setex(skuKey, 60, "empty");
                        return null;
                    }
                    String skuInfoJsonNew  = JSON.toJSONString(skuByIdDB);
                    //100秒后清空redis中的数据
                    jedis.setex(skuKey, 100, skuInfoJsonNew);
                    jedis.close();
                    return skuByIdDB;
                }else{
                    //redis宕机了  去查询数据库
                    System.err.println(Thread.currentThread().getName() + "未获得分布式锁，开始自旋");
                    Thread.sleep(1000);
                    jedis.close();
                    return getSkuByIdDB(skuId);
                }
            }else if("empty".equals(skuInfoJson)){
                return null;
            }else{
                System.out.println(Thread.currentThread().getName() + "缓存以命中");
                skuInfo = JSON.parseObject(skuInfoJson,SkuInfo.class);
                jedis.close();
                return skuInfo;
            }
        }catch (JedisConnectionException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getSkuByIdDB(skuId);
    }
    /**
     * 使用redis根据id查询sku和图片集合 正常情况
     * @param skuId
     * @return
     */
   /* @Override
    public SkuInfo getSkuById(String skuId) {

        Jedis jedis = redisUtil.getJedis();
        String skuKey = "sku:" + skuId + ":info";
        String skuInfoJson = jedis.get(skuKey);
        if(skuInfoJson != null){
            System.out.println(Thread.currentThread().getName() + "命中缓存");
            //将Json字符串转化为相应的对象
            SkuInfo skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
            //一定要关闭jedis
            jedis.close();
            return skuInfo;
        }else{
            System.err.println(Thread.currentThread().getName() + "未命中缓存");
        }
        System.err.println(Thread.currentThread().getName() + "*********查询数据库*********");
        SkuInfo skuByIdDB = getSkuByIdDB(skuId);
        String skuByIdDBJsonStr  = JSON.toJSONString(skuByIdDB);
        //将value关联到key, 并将key的生存时间设为seconds(以秒为单位)。
        //目的将mysql的最新数据及时的同步到redis中
        jedis.setex(skuKey, 600, skuByIdDBJsonStr);
        System.out.println(Thread.currentThread().getName() + "数据库更新完毕");
        jedis.close();
        return skuByIdDB;
    }*/

    /**
     * 根据id查询sku和图片集合
     * @param skuId
     * @return
     */
    public SkuInfo getSkuByIdDB(String skuId){
        //查询sku信息
        SkuInfo skuInfoParam = new SkuInfo();
        skuInfoParam.setId(skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(skuInfoParam);

        //查询图片信息
        SkuImage skuImageParam = new SkuImage();
        skuImageParam.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImageParam);
        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }


    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String spuId, String skuId) {

        List<SpuSaleAttr> spuSaleAttrs = skuAttrValueMapper.selectSpuSaleAttrListCheckBySku(spuId, skuId);
        return spuSaleAttrs;
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo> skuInfos = skuAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuInfos;
    }

    @Override
    public List<SkuInfo> getSkuByCatalog3Id(int catalog3Id) {
        SkuInfo skuInfoParam = new SkuInfo();
        skuInfoParam.setCatalog3Id(catalog3Id + "");
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfoParam);

        //注入平台属性
        for (SkuInfo skuInfo : skuInfos) {
            String skuId = skuInfo.getId();
            SkuAttrValue skuAttrValueParam = new SkuAttrValue();
            skuAttrValueParam.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValueParam);
            skuInfo.setSkuAttrValueList(skuAttrValues);
        }
        return skuInfos;
    }
}
