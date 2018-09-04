package com.lhf.gmall.service;

import com.lhf.gmall.bean.BaseCatalog1;
import com.lhf.gmall.bean.BaseCatalog2;
import com.lhf.gmall.bean.BaseCatalog3;

import java.util.List;

/**
 * @author shkstart
 * @create 2018-09-04 19:26
 */
public interface CatalogService {

    List<BaseCatalog1> getCatalog1();

    List<BaseCatalog2> getCatalog2(String catalog1Id);

    List<BaseCatalog3> getCatalog3(String catalog2Id);
}
